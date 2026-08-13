package com.example.multimedia.file_upload_api.service.impl;

import com.example.multimedia.file_upload_api.dto.RolePermissionDTO;
import com.example.multimedia.file_upload_api.dto.RolePermissionUpdateRequest;
import com.example.multimedia.file_upload_api.dto.PermissionItemDto;
import com.example.multimedia.file_upload_api.entity.PermissionMaster;
import com.example.multimedia.file_upload_api.entity.RolePermission;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.PermissionMasterRepository;
import com.example.multimedia.file_upload_api.repository.RolePermissionRepository;
import com.example.multimedia.file_upload_api.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMasterRepository permissionMasterRepository;

    @Override
    public List<RolePermissionDTO> getPermissionsByRole(UserType role) {
        List<PermissionMaster> allPermissions = permissionMasterRepository.findAll();
        List<RolePermission> assignedPermissions = rolePermissionRepository.findByRole(role);

        Map<Long, RolePermission> assignedMap = assignedPermissions.stream()
                .collect(Collectors.toMap(rp -> rp.getPermission().getId(), rp -> rp));

        List<RolePermissionDTO> result = new ArrayList<>();
        for (PermissionMaster pm : allPermissions) {
            RolePermissionDTO dto = new RolePermissionDTO();
            dto.setPermissionId(pm.getId());
            dto.setPermissionName(pm.getName());

            if (assignedMap.containsKey(pm.getId())) {
                RolePermission rp = assignedMap.get(pm.getId());
                dto.setCanCreate(rp.getCanCreate());
                dto.setCanView(rp.getCanView());
                dto.setCanEdit(rp.getCanEdit());
                dto.setCanDelete(rp.getCanDelete());
            } else {
                dto.setCanCreate(false);
                dto.setCanView(false);
                dto.setCanEdit(false);
                dto.setCanDelete(false);
            }
            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public void saveRolePermissions(RolePermissionUpdateRequest request) {
        UserType role = request.getRole();
        
        for (RolePermissionDTO dto : request.getPermissions()) {
            Optional<RolePermission> existingOpt = rolePermissionRepository.findByRoleAndPermissionId(role, dto.getPermissionId());
            
            RolePermission rolePermission = existingOpt.orElseGet(RolePermission::new);
            
            if (existingOpt.isEmpty()) {
                PermissionMaster pm = permissionMasterRepository.findById(dto.getPermissionId())
                        .orElseThrow(() -> new RuntimeException("Permission Master not found"));
                rolePermission.setPermission(pm);
                rolePermission.setRole(role);
            }
            
            rolePermission.setCanCreate(dto.getCanCreate() != null ? dto.getCanCreate() : false);
            rolePermission.setCanView(dto.getCanView() != null ? dto.getCanView() : false);
            rolePermission.setCanEdit(dto.getCanEdit() != null ? dto.getCanEdit() : false);
            rolePermission.setCanDelete(dto.getCanDelete() != null ? dto.getCanDelete() : false);
            
            rolePermissionRepository.save(rolePermission);
        }
    }

    @Override
    public List<PermissionItemDto> getRolePermissionsTree(UserType role) {
        // 1. Fetch all master permissions
        List<PermissionMaster> masters = permissionMasterRepository.findAll();
        
        // 2. Fetch all assigned permissions for this role
        List<RolePermission> assigned = rolePermissionRepository.findByRole(role);
        Map<String, RolePermission> assignedMap = assigned.stream()
                .collect(Collectors.toMap(rp -> rp.getPermission().getCode(), rp -> rp));

        // 3. Create DTO map
        Map<String, PermissionItemDto> dtoMap = new HashMap<>();
        for (PermissionMaster m : masters) {
            RolePermission rp = assignedMap.get(m.getCode());
            PermissionItemDto dto = new PermissionItemDto();
            dto.setPermissionCode(m.getCode());
            dto.setPermissionName(m.getName());
            dto.setPermissionType(m.getType().name());
            dto.setView(rp != null && rp.getCanView());
            dto.setCreate(rp != null && rp.getCanCreate());
            dto.setEdit(rp != null && rp.getCanEdit());
            dto.setDelete(rp != null && rp.getCanDelete());
            dto.setChildren(new ArrayList<>());
            dtoMap.put(m.getCode(), dto);
        }

        // 4. Connect children to parents
        List<PermissionItemDto> roots = new ArrayList<>();
        for (PermissionMaster m : masters) {
            PermissionItemDto dto = dtoMap.get(m.getCode());
            if (m.getParent() == null) {
                roots.add(dto);
            } else {
                PermissionItemDto parentDto = dtoMap.get(m.getParent().getCode());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return roots;
    }
}

