package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @Column(name = "project_code", length = 50, unique = true)
    private String projectCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "wbs", length = 50)
    private String wbs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_code", referencedColumnName = "dept_code")
    private Department department;

    @PrePersist
    public void generateCode() {
        if (projectCode == null) {
            projectCode = "PRJ-" + System.currentTimeMillis();
        }
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWbs() { return wbs; }
    public void setWbs(String wbs) { this.wbs = wbs; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}
