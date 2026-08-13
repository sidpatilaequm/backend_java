package com.example.multimedia.file_upload_api.entity;

import com.example.multimedia.file_upload_api.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_permission")
@Getter
@Setter
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionMaster permission;

    @Column(nullable = false)
    private Boolean canView = false;

    @Column(nullable = false)
    private Boolean canCreate = false;

    @Column(nullable = false)
    private Boolean canEdit = false;

    @Column(nullable = false)
    private Boolean canDelete = false;
}
