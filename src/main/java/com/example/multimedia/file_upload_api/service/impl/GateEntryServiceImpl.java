package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.gate.*;
import com.example.multimedia.file_upload_api.entity.Asn;
import com.example.multimedia.file_upload_api.entity.AsnItem;
import com.example.multimedia.file_upload_api.entity.GateEntry;
import com.example.multimedia.file_upload_api.entity.GateEntryLine;
import com.example.multimedia.file_upload_api.repository.AsnRepository;
import com.example.multimedia.file_upload_api.repository.GateEntryLineRepository;
import com.example.multimedia.file_upload_api.repository.GateEntryRepository;
import com.example.multimedia.file_upload_api.service.GateEntryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.multimedia.file_upload_api.entity.VendorMaster;
import com.example.multimedia.file_upload_api.repository.VendorMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GateEntryServiceImpl implements GateEntryService {

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private GateEntryRepository gateEntryRepository;

    @Autowired
    private GateEntryLineRepository gateEntryLineRepository;

    @Autowired
    private VendorMasterRepository vendorMasterRepository;

    @Autowired
    private com.example.multimedia.file_upload_api.security.OrgConfigGate orgConfigGate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long extractAsnId(String asnNumberStr) {
        if (asnNumberStr == null || !asnNumberStr.startsWith("ASN-")) {
            return null;
        }
        try {
            String[] parts = asnNumberStr.split("-");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatAsnNumber(Asn asn) {
        int year = asn.getCreatedDate() != null ? asn.getCreatedDate().getYear() : LocalDate.now().getYear();
        return String.format("ASN-%d-%04d", year, asn.getId());
    }

    private String generateGatePassNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long nextId = gateEntryRepository.count() + 1; // Simplistic generation
        return String.format("GE-%s-%03d", datePart, nextId);
    }

    @Override
    public ServiceResponse getExpectedArrivals(LocalDate date, String plant) {
        ServiceResponse response = new ServiceResponse();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Fetch ASNs that are IN_TRANSIT
        List<Asn> inTransitAsns = asnRepository.findAll().stream()
                .filter(asn -> "IN_TRANSIT".equalsIgnoreCase(asn.getStatus()) || "DISPATCHED".equalsIgnoreCase(asn.getStatus()))
                .filter(asn -> asn.getGateStatus() == null || !asn.getGateStatus().equals("ALLOWED"))
                .collect(Collectors.toList());

        List<ExpectedArrivalDto> arrivals = inTransitAsns.stream().map(asn -> {
            ExpectedArrivalDto dto = new ExpectedArrivalDto();
            dto.setAsnNumber(formatAsnNumber(asn));
            dto.setPoNumber(asn.getPurchaseOrder() != null ? asn.getPurchaseOrder().getPoNumber() : null);
            if (asn.getVendorBpno() != null) {
                vendorMasterRepository.findByBpNo(asn.getVendorBpno())
                        .filter(vm -> vm.getSupplierRegistration() != null)
                        .ifPresent(vm -> dto.setVendorName(vm.getSupplierRegistration().getVendorName()));
            }
            dto.setVehicleNo(asn.getVehicleNumber());
            
            LocalDateTime eta = asn.getExpectedDelivery() != null ? asn.getExpectedDelivery().atStartOfDay() : null;
            dto.setEta(eta);
            dto.setEtaTag(eta != null && eta.toLocalDate().isBefore(LocalDate.now()) ? "overdue" : "due");
            
            int totalPackages = 0;
            try {
                if (asn.getPackaging() != null && !asn.getPackaging().isEmpty()) {
                    totalPackages = Integer.parseInt(asn.getPackaging());
                }
            } catch (NumberFormatException ignored) {}
            dto.setTotalPackages(totalPackages);
            return dto;
        }).collect(Collectors.toList());

        response.setStatus("success");
        response.addData("items", arrivals);
        return response;
    }

    @Override
    public ServiceResponse getArrivalDetails(String asnNumber) {
        ServiceResponse response = new ServiceResponse();
        Long asnId = extractAsnId(asnNumber);
        if (asnId == null) {
            response.setStatus("error");
            response.setStatusMsg("Invalid ASN Number");
            return response;
        }

        Optional<Asn> asnOpt = asnRepository.findById(asnId);
        if (asnOpt.isEmpty()) {
            response.setStatus("error");
            response.setStatusMsg("ASN not found");
            return response;
        }

        Asn asn = asnOpt.get();
        ArrivalDetailsDto dto = new ArrivalDetailsDto();
        dto.setAsnNumber(formatAsnNumber(asn));
        dto.setPoNumber(asn.getPurchaseOrder() != null ? asn.getPurchaseOrder().getPoNumber() : null);
        
        if (asn.getVendorBpno() != null) {
            vendorMasterRepository.findByBpNo(asn.getVendorBpno())
                    .filter(vm -> vm.getSupplierRegistration() != null)
                    .ifPresent(vm -> {
                        ArrivalDetailsDto.VendorDto vDto = new ArrivalDetailsDto.VendorDto();
                        vDto.setName(vm.getSupplierRegistration().getVendorName());
                        vDto.setGstin(vm.getSupplierRegistration().getGstNumber());
                        dto.setVendor(vDto);
                    });
        }

        ArrivalDetailsDto.InvoiceDto iDto = new ArrivalDetailsDto.InvoiceDto();
        iDto.setNumber(asn.getInvoiceNumber());
        iDto.setDate(asn.getDispatchDate() != null ? asn.getDispatchDate().toString() : null);
        // value can be fetched from PO if available, skipping for now
        dto.setInvoice(iDto);

        ArrivalDetailsDto.LogisticsDto lDto = new ArrivalDetailsDto.LogisticsDto();
        lDto.setEwb(asn.getEwayBill());
        lDto.setVehicle(asn.getVehicleNumber());
        dto.setLogistics(lDto);

        dto.setDeclaredPackages(asn.getNoOfPackages() != null ? asn.getNoOfPackages() : 0);

        List<ArrivalDetailsDto.LineDto> lines = asn.getItems().stream().map(item -> {
            ArrivalDetailsDto.LineDto line = new ArrivalDetailsDto.LineDto();
            line.setMaterialCode(item.getPartNumber());
            line.setDescription("Material " + item.getPartNumber());
            line.setQty(item.getQuantityShipped());
            line.setUom(item.getPurchaseOrderItem() != null ? item.getPurchaseOrderItem().getUom() : "EA");
            return line;
        }).collect(Collectors.toList());
        dto.setLines(lines);

        response.setStatus("success");
        
        if (asn.getPackages() != null) {
            List<ArrivalDetailsDto.PackageDto> packages = asn.getPackages().stream().map(pkg -> {
                ArrivalDetailsDto.PackageDto pkgDto = new ArrivalDetailsDto.PackageDto();
                pkgDto.setPackageNumber(pkg.getPackageNumber());
                pkgDto.setMaterialDetails(pkg.getMaterialDetails());
                pkgDto.setQuantity(pkg.getQuantity());
                return pkgDto;
            }).collect(Collectors.toList());
            dto.setPackages(packages);
        }

        response.addData("details", dto);
        return response;
    }

    @Override
    @Transactional
    public ServiceResponse processGateEntry(ProcessGateEntryDto processDto, String processedBy) {
        ServiceResponse response = new ServiceResponse();
        Long asnId = extractAsnId(processDto.getAsnNumber());
        Optional<Asn> asnOpt = asnRepository.findById(asnId != null ? asnId : -1L);
        
        if (asnOpt.isEmpty()) {
            response.setStatus("error");
            response.setStatusMsg("ASN not found");
            return response;
        }

        Asn asn = asnOpt.get();
        GateEntry entry = new GateEntry();
        entry.setAsn(asn);
        entry.setCompanyCode(asn.getCompanyCode());
        
        if ("ALLOW".equalsIgnoreCase(processDto.getDecision())) {
            entry.setGatePassNumber(generateGatePassNumber());
            asn.setGatePassNumber(entry.getGatePassNumber());
            asn.setGateStatus("ALLOWED");
        } else if ("HOLD".equalsIgnoreCase(processDto.getDecision())) {
            asn.setGateStatus("HELD");
        } else if ("REJECT".equalsIgnoreCase(processDto.getDecision())) {
            asn.setGateStatus("REJECTED");
        }

        entry.setDecision(processDto.getDecision());
        try {
            entry.setDocuments(objectMapper.writeValueAsString(processDto.getDocuments()));
        } catch (JsonProcessingException e) {
            entry.setDocuments("{}");
        }

        entry.setDeclaredPackages(asn.getNoOfPackages() != null ? asn.getNoOfPackages() : 0);
        if (processDto.getPackageVerification() != null) {
            entry.setCountedPackages(processDto.getPackageVerification().getCounted());
            entry.setPackageRemark(processDto.getPackageVerification().getRemark());
        }

        List<String> holdReasons = new ArrayList<>();
        if (entry.getCountedPackages() != null && entry.getCountedPackages() != entry.getDeclaredPackages()) {
            holdReasons.add("Package count differs from declared");
        }

        entry.setInTime(LocalDateTime.now());
        entry.setProcessedBy(processedBy);
        
        gateEntryRepository.save(entry);

        if (processDto.getLineVerification() != null) {
            for (ProcessGateEntryDto.LineVerificationDto lineDto : processDto.getLineVerification()) {
                GateEntryLine line = new GateEntryLine();
                line.setGateEntry(entry);
                line.setMaterialCode(lineDto.getMaterialCode());
                
                // find declared qty
                BigDecimal declaredQty = asn.getItems().stream()
                        .filter(i -> i.getPartNumber().equals(lineDto.getMaterialCode()))
                        .map(AsnItem::getQuantityShipped)
                        .findFirst().orElse(BigDecimal.ZERO);
                
                line.setDeclaredQty(declaredQty);
                line.setCountedQty(lineDto.getCountedQty());
                line.setRemark(lineDto.getRemark());
                
                if (line.getCountedQty() != null && line.getCountedQty().compareTo(line.getDeclaredQty()) != 0) {
                    holdReasons.add("Line quantity discrepancy for " + line.getMaterialCode());
                }
                
                gateEntryLineRepository.save(line);
            }
        }

        if ("HOLD".equalsIgnoreCase(processDto.getDecision()) && !holdReasons.isEmpty()) {
            entry.setHoldReason(String.join(". ", holdReasons));
            gateEntryRepository.save(entry);
        }

        asnRepository.save(asn);

        Map<String, String> data = new HashMap<>();
        if (entry.getGatePassNumber() != null) {
            data.put("gate_pass_number", entry.getGatePassNumber());
            response.setStatusMsg("Entry allowed — gate pass generated");
        } else {
            response.setStatusMsg("Entry " + processDto.getDecision().toLowerCase());
        }

        response.setStatus("success");
        response.addData("result", data);
        return response;
    }

    @Override
    public ServiceResponse getGeneratedGatePasses(LocalDate date) {
        ServiceResponse response = new ServiceResponse();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<GateEntry> passes = gateEntryRepository.findByInTimeBetween(startOfDay, endOfDay).stream()
                .filter(ge -> "ALLOW".equalsIgnoreCase(ge.getDecision()) && ge.getGatePassNumber() != null)
                .collect(Collectors.toList());

        List<GatePassDto> list = passes.stream().map(ge -> {
            GatePassDto dto = new GatePassDto();
            dto.setGatePassNumber(ge.getGatePassNumber());
            dto.setAsnNumber(formatAsnNumber(ge.getAsn()));
            dto.setVehicleNo(ge.getAsn().getVehicleNumber());
            dto.setInTime(ge.getInTime());
            dto.setProcessedBy(ge.getProcessedBy());
            return dto;
        }).collect(Collectors.toList());

        response.setStatus("success");
        response.addData("items", list);
        return response;
    }

    @Override
    public ServiceResponse getGatePassDetails(String gatePassNumber) {
        ServiceResponse response = new ServiceResponse();
        Optional<GateEntry> geOpt = gateEntryRepository.findByGatePassNumber(gatePassNumber);
        if (geOpt.isEmpty()) {
            response.setStatus("error");
            response.setStatusMsg("Gate pass not found");
            return response;
        }

        GateEntry ge = geOpt.get();
        GatePassDetailsDto dto = new GatePassDetailsDto();
        dto.setGatePassNumber(ge.getGatePassNumber());
        dto.setInTime(ge.getInTime());
        dto.setCountedPackages(ge.getCountedPackages());

        List<GatePassDetailsDto.LineDto> lines = ge.getLines().stream().map(line -> {
            GatePassDetailsDto.LineDto ld = new GatePassDetailsDto.LineDto();
            ld.setMaterialCode(line.getMaterialCode());
            ld.setDeclaredQty(line.getDeclaredQty());
            ld.setCountedQty(line.getCountedQty());
            ld.setUom("EA");
            return ld;
        }).collect(Collectors.toList());
        dto.setLines(lines);

        response.setStatus("success");
        response.addData("details", dto);
        return response;
    }

    @Override
    public ServiceResponse getAuditLogs(LocalDate date) {
        ServiceResponse response = new ServiceResponse();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<GateEntry> entries = gateEntryRepository.findByInTimeBetween(startOfDay, endOfDay);
        
        List<AuditLogDto> logs = entries.stream().map(ge -> {
            AuditLogDto log = new AuditLogDto();
            log.setTime(ge.getInTime() != null ? ge.getInTime().toLocalTime().toString() : "");
            
            if ("ALLOW".equalsIgnoreCase(ge.getDecision())) {
                log.setMessage("Entry allowed — gate pass " + ge.getGatePassNumber() + " issued");
                log.setKind("ok");
            } else if ("HOLD".equalsIgnoreCase(ge.getDecision())) {
                log.setMessage("Vehicle " + ge.getAsn().getVehicleNumber() + " held at gate — supervisor called");
                log.setKind("warn");
            } else {
                log.setMessage("Vehicle " + ge.getAsn().getVehicleNumber() + " rejected at gate");
                log.setKind("error");
            }
            return log;
        }).sorted(Comparator.comparing(AuditLogDto::getTime).reversed()).collect(Collectors.toList());

        response.setStatus("success");
        response.addData("items", logs);
        return response;
    }

    @Override
    public ServiceResponse getHeldVehiclesQueue() {
        ServiceResponse response = new ServiceResponse();
        List<GateEntry> held = gateEntryRepository.findByDecision("HOLD");
        
        List<HeldVehicleDto> list = held.stream()
                .filter(ge -> "HELD".equalsIgnoreCase(ge.getAsn().getGateStatus())) // Only still held ones
                .map(ge -> {
            HeldVehicleDto dto = new HeldVehicleDto();
            dto.setAsnNumber(formatAsnNumber(ge.getAsn()));
            dto.setVehicleNo(ge.getAsn().getVehicleNumber());
            dto.setHoldReason(ge.getHoldReason());
            return dto;
        }).collect(Collectors.toList());

        response.setStatus("success");
        response.addData("items", list);
        return response;
    }

    @Override
    @Transactional
    public ServiceResponse releaseHeldVehicle(String asnNumber, SupervisorReleaseDto releaseDto, String processedBy) {
        ServiceResponse response = new ServiceResponse();
        Long asnId = extractAsnId(asnNumber);
        Optional<GateEntry> geOpt = gateEntryRepository.findFirstByAsnIdOrderByCreatedDateDesc(asnId);
        
        if (geOpt.isEmpty() || !"HOLD".equalsIgnoreCase(geOpt.get().getDecision())) {
            response.setStatus("error");
            response.setStatusMsg("No held gate entry found for ASN");
            return response;
        }

        GateEntry ge = geOpt.get();
        Asn asn = ge.getAsn();
        
        ge.setSupervisorRemark(releaseDto.getSupervisorRemark());
        
        if (releaseDto.getAction() != null && releaseDto.getAction().startsWith("APPROVE")) {
            ge.setDecision("ALLOW");
            ge.setGatePassNumber(generateGatePassNumber());
            asn.setGateStatus("ALLOWED");
            asn.setGatePassNumber(ge.getGatePassNumber());
            
            gateEntryRepository.save(ge);
            asnRepository.save(asn);
            
            Map<String, Object> data = new HashMap<>();
            data.put("gate_pass_number", ge.getGatePassNumber());
            response.addData("result", data);
            response.setStatus("success");
        } else {
            ge.setDecision("REJECT");
            asn.setGateStatus("REJECTED");
            gateEntryRepository.save(ge);
            asnRepository.save(asn);
            response.setStatus("success");
            response.setStatusMsg("Vehicle rejected by supervisor");
        }
        return response;
    }

    @Override
    public ServiceResponse getVendorGateStatus(String email) {
        ServiceResponse response = new ServiceResponse();
        if (email == null) {
            response.setStatus("error");
            response.setStatusMsg("Unauthorized: No email found in token");
            return response;
        }
        if (!orgConfigGate.isGateEntryShowToVendorEnabled()) {
            response.setStatus("error");
            response.setStatusMsg("Gate entry status is not available.");
            return response;
        }

        VendorMaster vendor = vendorMasterRepository.findBySupplierRegistration_Email(email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vendor not found for email: " + email));
        
        String vendorBpno = vendor.getBpNo();

        List<Asn> asns = asnRepository.findByVendorBpno(vendorBpno, null);
        
        List<VendorGateStatusDto> list = asns.stream()
                .filter(asn -> asn.getGateStatus() != null)
                .map(asn -> {
            VendorGateStatusDto dto = new VendorGateStatusDto();
            dto.setAsnNumber(formatAsnNumber(asn));
            dto.setVehicleNo(asn.getVehicleNumber());
            dto.setGateStatus(asn.getGateStatus());
            dto.setGatePassNumber(asn.getGatePassNumber());
            
            Optional<GateEntry> geOpt = gateEntryRepository.findFirstByAsnIdOrderByCreatedDateDesc(asn.getId());
            geOpt.ifPresent(ge -> dto.setProcessedAt(ge.getInTime()));
            
            return dto;
        }).collect(Collectors.toList());

        response.setStatus("success");
        response.addData("items", list);
        return response;
    }

    @Override
    public ServiceResponse getGateDiscrepancyReport(String asnNumber, String email) {
        ServiceResponse response = new ServiceResponse();
        if (email == null) {
            response.setStatus("error");
            response.setStatusMsg("Unauthorized: No email found in token");
            return response;
        }
        if (!orgConfigGate.isGateEntryShowToVendorEnabled()) {
            response.setStatus("error");
            response.setStatusMsg("Gate entry status is not available.");
            return response;
        }

        VendorMaster vendor = vendorMasterRepository.findBySupplierRegistration_Email(email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vendor not found for email: " + email));
        
        String vendorBpno = vendor.getBpNo();

        Long asnId = extractAsnId(asnNumber);
        Optional<GateEntry> geOpt = gateEntryRepository.findFirstByAsnIdOrderByCreatedDateDesc(asnId);
        
        if (geOpt.isEmpty()) {
            response.setStatus("error");
            response.setStatusMsg("No gate entry found");
            return response;
        }
        
        GateEntry ge = geOpt.get();
        if (ge.getAsn().getVendorBpno() == null || !ge.getAsn().getVendorBpno().equals(vendorBpno)) {
            response.setStatus("error");
            response.setStatusMsg("Unauthorized");
            return response;
        }

        GateDiscrepancyDto dto = new GateDiscrepancyDto();
        dto.setAsnNumber(formatAsnNumber(ge.getAsn()));
        dto.setGateStatus(ge.getAsn().getGateStatus());
        
        GateDiscrepancyDto.DiscrepancyDetailsDto details = new GateDiscrepancyDto.DiscrepancyDetailsDto();
        
        // Convert documents map to list of missing documents
        List<String> docIssues = new ArrayList<>();
        try {
            if (ge.getDocuments() != null) {
                Map<String, String> docs = objectMapper.readValue(ge.getDocuments(), Map.class);
                docs.forEach((k, v) -> {
                    if (!"ok".equalsIgnoreCase(v)) {
                        docIssues.add(k + " is " + v);
                    }
                });
            }
        } catch (Exception e) {}
        details.setDocuments(docIssues);
        
        if (ge.getDeclaredPackages() != null && ge.getCountedPackages() != null) {
            if (!ge.getDeclaredPackages().equals(ge.getCountedPackages())) {
                details.setPackages("Declared " + ge.getDeclaredPackages() + ", Counted " + ge.getCountedPackages() + " (Short " + (ge.getDeclaredPackages() - ge.getCountedPackages()) + ")");
            }
        }
        
        List<GateDiscrepancyDto.LineDiscrepancyDto> lines = new ArrayList<>();
        for (GateEntryLine line : ge.getLines()) {
            if (line.getCountedQty() != null && line.getCountedQty().compareTo(line.getDeclaredQty()) != 0) {
                GateDiscrepancyDto.LineDiscrepancyDto lDto = new GateDiscrepancyDto.LineDiscrepancyDto();
                lDto.setMaterialCode(line.getMaterialCode());
                lDto.setDeclared(line.getDeclaredQty());
                lDto.setCounted(line.getCountedQty());
                lDto.setRemark(line.getRemark());
                lines.add(lDto);
            }
        }
        details.setLines(lines);
        
        dto.setDiscrepancies(details);

        response.setStatus("success");
        response.addData("details", dto);
        return response;
    }
}
