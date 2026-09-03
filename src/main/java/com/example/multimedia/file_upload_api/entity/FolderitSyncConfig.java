package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "folderit_sync_config")
@Getter
@Setter
public class FolderitSyncConfig {

    @Id
    private Long id = 1L; // Single row config

    private boolean isEnabled = false;
    
    private int intervalMinutes = 60; // default to 60 mins
    
    private LocalDateTime lastRunTime;
    
    private String lastRunStatus = "Never Run";
    
    private int filesProcessedLastRun = 0;
}
