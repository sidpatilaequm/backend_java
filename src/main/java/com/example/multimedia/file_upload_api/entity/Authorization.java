package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "authorization")
public class Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private int authId;

    @Column(name = "auth_name", nullable = false)
    private String authName;

    @Column(name = "auth_key", nullable = false, unique = true)
    private String authKey;

    @Column(name = "is_active")
    private boolean active;
}
