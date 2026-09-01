package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.PurchaseRoleDtos.*;
import com.example.multimedia.file_upload_api.entity.*;
import com.example.multimedia.file_upload_api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ported from role-manager's crud.py/schemas.py — same validation rules, same shape. The two
 * composite foreign keys that actually enforce "a grant can't name a company code outside the
 * role's scope" and "a document type must be assigned to that company code" live in the
 * database (see the migration SQL and PurchaseRoleGrant's javadoc); the checks here exist so a
 * caller gets a readable 409 instead of a raw integrity error, exactly like the original.
 */
@Service
public class PurchaseRoleService {

    /** Mirrors role-manager's own ValueError-as-409 pattern — a validation failure the caller
     *  should see as a readable message, not a stack trace. */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    private final CompanyRepository companyRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeCompanyCodeRepository documentTypeCompanyCodeRepository;
    private final AccessLevelRepository accessLevelRepository;
    private final PurchaseRoleRepository purchaseRoleRepository;
    private final PurchaseRoleCompanyCodeRepository purchaseRoleCompanyCodeRepository;
    private final PurchaseRoleGrantRepository purchaseRoleGrantRepository;

    public PurchaseRoleService(CompanyRepository companyRepository,
                                DocumentTypeRepository documentTypeRepository,
                                DocumentTypeCompanyCodeRepository documentTypeCompanyCodeRepository,
                                AccessLevelRepository accessLevelRepository,
                                PurchaseRoleRepository purchaseRoleRepository,
                                PurchaseRoleCompanyCodeRepository purchaseRoleCompanyCodeRepository,
                                PurchaseRoleGrantRepository purchaseRoleGrantRepository) {
        this.companyRepository = companyRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.documentTypeCompanyCodeRepository = documentTypeCompanyCodeRepository;
        this.accessLevelRepository = accessLevelRepository;
        this.purchaseRoleRepository = purchaseRoleRepository;
        this.purchaseRoleCompanyCodeRepository = purchaseRoleCompanyCodeRepository;
        this.purchaseRoleGrantRepository = purchaseRoleGrantRepository;
    }

    // ------------------------------------------------------------ reference

    public List<AccessLevelOut> listAccessLevels(String assigneeType) {
        List<AccessLevel> rows = (assigneeType == null || assigneeType.isBlank())
                ? accessLevelRepository.findAllByOrderByAssigneeTypeAscSortOrderAsc()
                : accessLevelRepository.findByAssigneeTypeOrderBySortOrder(assigneeType);
        return rows.stream().map(this::toAccessLevelOut).toList();
    }

    public List<DocumentTypeOut> listDocumentTypes(List<String> companyCodes) {
        boolean filtering = companyCodes != null && !companyCodes.isEmpty();
        Set<String> wanted = filtering ? new HashSet<>(companyCodes) : Set.of();

        List<DocumentTypeOut> out = new ArrayList<>();
        for (DocumentType dt : documentTypeRepository.findByIsActiveTrueOrderByCode()) {
            List<DocumentTypeCompanyCode> assigns = documentTypeCompanyCodeRepository
                    .findByDocTypeCode(dt.getCode()).stream()
                    .filter(a -> !filtering || wanted.contains(a.getCompanyCode()))
                    .toList();
            if (filtering && assigns.isEmpty()) continue; // not usable in any requested company code

            DocumentTypeOut o = new DocumentTypeOut();
            o.setCode(dt.getCode());
            o.setDescription(dt.getDescription());
            o.setDocCategory(dt.getDocCategory());
            o.setClassification(dt.getClassification());
            o.setSource(dt.getSource());
            o.setAssignments(assigns.stream().map(this::toAssignmentOut).toList());
            out.add(o);
        }
        return out;
    }

    @Transactional
    public DocumentTypeOut createDocumentType(DocumentTypeIn payload) {
        String code = payload.getCode() == null ? "" : payload.getCode().trim().toUpperCase();
        if (!code.matches("^[A-Z0-9]{2,4}$")) {
            throw new ConflictException("document type must be 2 to 4 characters, A-Z or 0-9");
        }
        if (payload.getAssignments() == null || payload.getAssignments().isEmpty()) {
            throw new ConflictException("at least one company code assignment is required");
        }
        Set<String> validCcs = companyRepository.findAll().stream()
                .map(Company::getCompanyCode).collect(Collectors.toSet());
        Set<String> seen = new HashSet<>();
        for (DocTypeAssignmentIn a : payload.getAssignments()) {
            if (!validCcs.contains(a.getCompanyCode())) {
                throw new ConflictException("unknown company code " + a.getCompanyCode());
            }
            if (!seen.add(a.getCompanyCode())) {
                throw new ConflictException("a company code may only be assigned once");
            }
        }

        DocumentType dt = documentTypeRepository.findById(code).orElse(null);
        if (dt == null) {
            dt = new DocumentType();
            dt.setCode(code);
            dt.setDescription(payload.getDescription());
            dt.setDocCategory(payload.getDocCategory());
            dt.setClassification(payload.getClassification());
            dt.setSource("MANUAL");
            dt = documentTypeRepository.save(dt);
        }

        Set<String> already = documentTypeCompanyCodeRepository.findByDocTypeCode(code).stream()
                .map(DocumentTypeCompanyCode::getCompanyCode).collect(Collectors.toSet());
        boolean added = false;
        for (DocTypeAssignmentIn a : payload.getAssignments()) {
            if (already.contains(a.getCompanyCode())) continue;
            DocumentTypeCompanyCode row = new DocumentTypeCompanyCode(code, a.getCompanyCode());
            row.setDefaultPurchGroup(a.getDefaultPurchGroup());
            row.setDocVolume2y(0);
            documentTypeCompanyCodeRepository.save(row);
            added = true;
        }
        if (!added) {
            throw new ConflictException(code + " is already assigned to " + String.join(", ",
                    new TreeSet<>(payload.getAssignments().stream()
                            .map(DocTypeAssignmentIn::getCompanyCode).toList())));
        }

        DocumentTypeOut out = new DocumentTypeOut();
        out.setCode(dt.getCode());
        out.setDescription(dt.getDescription());
        out.setDocCategory(dt.getDocCategory());
        out.setClassification(dt.getClassification());
        out.setSource(dt.getSource());
        out.setAssignments(documentTypeCompanyCodeRepository.findByDocTypeCode(code).stream()
                .map(this::toAssignmentOut).toList());
        return out;
    }

    // ---------------------------------------------------------------- roles

    public List<RoleSummaryOut> listRoles(String assigneeType) {
        List<PurchaseRole> roles = (assigneeType == null || assigneeType.isBlank())
                ? purchaseRoleRepository.findByIsActiveTrueOrderByRoleCode()
                : purchaseRoleRepository.findByIsActiveTrueAndAssigneeTypeOrderByRoleCode(assigneeType);
        return roles.stream().map(this::toSummary).toList();
    }

    public RoleOut getRole(Long roleId) {
        PurchaseRole role = purchaseRoleRepository.findById(roleId).orElse(null);
        if (role == null) return null;

        Map<String, AccessLevel> labels = accessLevelRepository.findAllByOrderByAssigneeTypeAscSortOrderAsc()
                .stream().collect(Collectors.toMap(a -> a.getCode() + "|" + a.getAssigneeType(), a -> a, (x, y) -> x));
        Map<String, DocumentType> types = documentTypeRepository.findAll().stream()
                .collect(Collectors.toMap(DocumentType::getCode, d -> d));

        RoleSummaryOut summary = toSummary(role);
        RoleOut out = new RoleOut();
        out.setId(summary.getId());
        out.setRoleCode(summary.getRoleCode());
        out.setRoleName(summary.getRoleName());
        out.setAssigneeType(summary.getAssigneeType());
        out.setAssigneeRef(summary.getAssigneeRef());
        out.setValidTo(summary.getValidTo());
        out.setCompanyCodes(summary.getCompanyCodes());
        out.setGrantCount(summary.getGrantCount());

        List<PurchaseRoleGrant> grants = purchaseRoleGrantRepository.findByRoleId(roleId).stream()
                .sorted(Comparator.comparing(PurchaseRoleGrant::getCompanyCode)
                        .thenComparing(PurchaseRoleGrant::getDocTypeCode))
                .toList();
        out.setGrants(grants.stream().map(g -> {
            GrantOut go = new GrantOut();
            go.setDocTypeCode(g.getDocTypeCode());
            go.setCompanyCode(g.getCompanyCode());
            go.setAccessLevel(g.getAccessLevel());
            DocumentType dt = types.get(g.getDocTypeCode());
            if (dt != null) {
                go.setDocTypeDescription(dt.getDescription());
                go.setDocCategory(dt.getDocCategory());
            }
            AccessLevel al = labels.get(g.getAccessLevel() + "|" + role.getAssigneeType());
            if (al != null) {
                go.setAccessLabel(al.getLabel());
                go.setActivities(al.getActivities());
            }
            return go;
        }).toList());
        return out;
    }

    @Transactional
    public RoleOut createRole(RoleIn payload) {
        validateShape(payload);
        if (purchaseRoleRepository.findByRoleCode(normalizedCode(payload)).isPresent()) {
            throw new ConflictException("role " + normalizedCode(payload) + " already exists");
        }
        validateAgainstReference(payload);

        PurchaseRole role = new PurchaseRole();
        role.setRoleCode(normalizedCode(payload));
        role.setRoleName(payload.getRoleName());
        role.setAssigneeType(payload.getAssigneeType());
        role = purchaseRoleRepository.save(role);
        apply(role, payload);
        return getRole(role.getId());
    }

    @Transactional
    public RoleOut updateRole(Long roleId, RoleIn payload) {
        PurchaseRole role = purchaseRoleRepository.findById(roleId).orElse(null);
        if (role == null) return null;
        validateShape(payload);
        purchaseRoleRepository.findByRoleCodeAndIdNot(normalizedCode(payload), roleId)
                .ifPresent(r -> { throw new ConflictException("role " + normalizedCode(payload) + " already exists"); });
        validateAgainstReference(payload);

        role.setRoleCode(normalizedCode(payload));
        role.setRoleName(payload.getRoleName());
        role.setAssigneeType(payload.getAssigneeType());
        role.setAssigneeRef(payload.getAssigneeRef());
        role.setValidTo(payload.getValidTo());
        purchaseRoleRepository.save(role);
        apply(role, payload);
        return getRole(roleId);
    }

    @Transactional
    public boolean deleteRole(Long roleId) {
        if (purchaseRoleRepository.findById(roleId).isEmpty()) return false;
        // Deepest first — grants reference role_company_code via a composite FK, so they must go
        // before it, same ordering role-manager's own delete_role uses.
        purchaseRoleGrantRepository.deleteByRoleId(roleId);
        purchaseRoleCompanyCodeRepository.deleteByRoleId(roleId);
        purchaseRoleRepository.deleteById(roleId);
        return true;
    }

    // ------------------------------------------------------------- private

    private String normalizedCode(RoleIn payload) {
        String c = payload.getRoleCode() == null ? "" : payload.getRoleCode().trim().toUpperCase().replace(" ", "_");
        payload.setRoleCode(c);
        return c;
    }

    /** Mirrors schemas.py's RoleIn.coherent model_validator. */
    private void validateShape(RoleIn payload) {
        if (payload.getRoleCode() == null || payload.getRoleCode().trim().length() < 2) {
            throw new ConflictException("role code must be at least 2 characters");
        }
        if (payload.getRoleName() == null || payload.getRoleName().isBlank()) {
            throw new ConflictException("role name is required");
        }
        if (payload.getCompanyCodes() == null || payload.getCompanyCodes().isEmpty()) {
            throw new ConflictException("at least one company code is required");
        }
        if (payload.getGrants() == null || payload.getGrants().isEmpty()) {
            throw new ConflictException("at least one grant is required");
        }
        if ("VENDOR".equals(payload.getAssigneeType())
                && (payload.getAssigneeRef() == null || payload.getAssigneeRef().isBlank())) {
            throw new ConflictException("a vendor role must name the vendor code it is limited to");
        }

        Set<String> scope = new HashSet<>(payload.getCompanyCodes());
        if (scope.size() != payload.getCompanyCodes().size()) {
            throw new ConflictException("duplicate company code in scope");
        }

        Set<String> seenGrants = new HashSet<>();
        for (GrantIn g : payload.getGrants()) {
            if (!scope.contains(g.getCompanyCode())) {
                throw new ConflictException("grant for " + g.getDocTypeCode() + " names company code "
                        + g.getCompanyCode() + ", which is not in the role's scope");
            }
            String key = g.getDocTypeCode() + "|" + g.getCompanyCode();
            if (!seenGrants.add(key)) {
                throw new ConflictException("duplicate grant for " + g.getDocTypeCode() + " / " + g.getCompanyCode());
            }
        }
    }

    /** Mirrors crud.py's _validate_against_reference. */
    private void validateAgainstReference(RoleIn payload) {
        Set<String> validCcs = companyRepository.findAll().stream()
                .map(Company::getCompanyCode).collect(Collectors.toSet());
        Set<String> unknown = payload.getCompanyCodes().stream()
                .filter(cc -> !validCcs.contains(cc)).collect(Collectors.toSet());
        if (!unknown.isEmpty()) {
            throw new ConflictException("unknown company code: " + String.join(", ", new TreeSet<>(unknown)));
        }

        Set<String> levels = accessLevelRepository.findByAssigneeTypeOrderBySortOrder(payload.getAssigneeType())
                .stream().map(AccessLevel::getCode).collect(Collectors.toSet());
        Set<String> assignments = documentTypeCompanyCodeRepository.findAll().stream()
                .map(a -> a.getDocTypeCode() + "|" + a.getCompanyCode()).collect(Collectors.toSet());

        for (GrantIn g : payload.getGrants()) {
            if (!assignments.contains(g.getDocTypeCode() + "|" + g.getCompanyCode())) {
                throw new ConflictException(g.getDocTypeCode() + " is not assigned to company code " + g.getCompanyCode());
            }
            if (!levels.contains(g.getAccessLevel())) {
                throw new ConflictException("access level '" + g.getAccessLevel() + "' is not available to a "
                        + payload.getAssigneeType().toLowerCase() + " role");
            }
        }
    }

    /** Mirrors crud.py's _apply — replace scope and grants wholesale. */
    private void apply(PurchaseRole role, RoleIn payload) {
        purchaseRoleGrantRepository.deleteByRoleId(role.getId());
        purchaseRoleCompanyCodeRepository.deleteByRoleId(role.getId());

        for (String cc : new TreeSet<>(payload.getCompanyCodes())) {
            purchaseRoleCompanyCodeRepository.save(new PurchaseRoleCompanyCode(role.getId(), cc));
        }
        for (GrantIn g : payload.getGrants()) {
            purchaseRoleGrantRepository.save(new PurchaseRoleGrant(
                    role.getId(), g.getDocTypeCode(), g.getCompanyCode(), g.getAccessLevel()));
        }
    }

    private RoleSummaryOut toSummary(PurchaseRole role) {
        RoleSummaryOut s = new RoleSummaryOut();
        s.setId(role.getId());
        s.setRoleCode(role.getRoleCode());
        s.setRoleName(role.getRoleName());
        s.setAssigneeType(role.getAssigneeType());
        s.setAssigneeRef(role.getAssigneeRef());
        s.setValidTo(role.getValidTo());
        s.setCompanyCodes(purchaseRoleCompanyCodeRepository.findByRoleId(role.getId()).stream()
                .map(PurchaseRoleCompanyCode::getCompanyCode).sorted().toList());
        s.setGrantCount(purchaseRoleGrantRepository.findByRoleId(role.getId()).size());
        return s;
    }

    private AccessLevelOut toAccessLevelOut(AccessLevel a) {
        AccessLevelOut o = new AccessLevelOut();
        o.setCode(a.getCode());
        o.setAssigneeType(a.getAssigneeType());
        o.setLabel(a.getLabel());
        o.setActivities(a.getActivities());
        o.setSortOrder(a.getSortOrder());
        return o;
    }

    private DocTypeAssignmentOut toAssignmentOut(DocumentTypeCompanyCode a) {
        DocTypeAssignmentOut o = new DocTypeAssignmentOut();
        o.setCompanyCode(a.getCompanyCode());
        o.setDefaultPurchGroup(a.getDefaultPurchGroup());
        o.setDocVolume2y(a.getDocVolume2y());
        return o;
    }
}
