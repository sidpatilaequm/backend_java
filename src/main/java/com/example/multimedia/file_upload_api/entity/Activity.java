package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activity")
public class Activity {

    @Id
    private Long activityId;

    @Column(name = "activity_code", length = 50, unique = true)
    private String activityCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "wbs", length = 50)
    private String wbs;

    @Column(name = "cost_type_code", length = 50)
    private String costTypeCode;

    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "allocated_budget")
    private Double allocatedBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "employee_code")
    private Employee owner;

    @PrePersist
    public void generateCode() {
        if (activityCode == null) {
            activityCode = "ACT-" + System.currentTimeMillis();
        }
    }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getActivityCode() { return activityCode; }
    public void setActivityCode(String activityCode) { this.activityCode = activityCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWbs() { return wbs; }
    public void setWbs(String wbs) { this.wbs = wbs; }

    public String getCostTypeCode() { return costTypeCode; }
    public void setCostTypeCode(String costTypeCode) { this.costTypeCode = costTypeCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Double getAllocatedBudget() { return allocatedBudget; }
    public void setAllocatedBudget(Double allocatedBudget) { this.allocatedBudget = allocatedBudget; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Employee getOwner() { return owner; }
    public void setOwner(Employee owner) { this.owner = owner; }
}
