package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vendor_permission")
@Getter
@Setter
public class VendorPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyDetails company;

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
