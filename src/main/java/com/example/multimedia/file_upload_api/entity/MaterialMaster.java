package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "material_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "info_rec")
    private String infoRec;

    private String material;
    
    @Column(name = "material_description")
    private String materialDescription;

    private String type;

    @Column(name = "mat_type_desc")
    private String matTypeDesc;

    @Column(name = "group_name")
    private String groupName;

    private String unit;

    private String vendor;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "pur_org")
    private String purOrg;

    private String plant;

    @Column(name = "name_1")
    private String name1;

    private Double price;

    private String curr;

    @Column(name = "co_code")
    private String coCode;

    @Column(name = "user_id")
    private Long userId;
}
