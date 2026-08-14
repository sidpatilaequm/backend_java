package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sub_activity")
public class SubActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subActivityId;

    @Column(name = "subactivity_code", length = 50, unique = true)
    private String subActivityCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "wbs", length = 50)
    private String wbs;

    @Column(name = "level")
    private Integer level;

    @Column(name = "cost_type_code", length = 50)
    private String costTypeCode;

    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "allocated_budget")
    private Double allocatedBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_code", referencedColumnName = "activity_code")
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "employee_code")
    private Employee owner;

    @PrePersist
    public void generateCode() {
        if (subActivityCode == null) {
            subActivityCode = "SUBACT-" + System.currentTimeMillis();
        }
    }

    public Long getSubActivityId() { return subActivityId; }
    public void setSubActivityId(Long subActivityId) { this.subActivityId = subActivityId; }

    public String getSubActivityCode() { return subActivityCode; }
    public void setSubActivityCode(String subActivityCode) { this.subActivityCode = subActivityCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWbs() { return wbs; }
    public void setWbs(String wbs) { this.wbs = wbs; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getCostTypeCode() { return costTypeCode; }
    public void setCostTypeCode(String costTypeCode) { this.costTypeCode = costTypeCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Double getAllocatedBudget() { return allocatedBudget; }
    public void setAllocatedBudget(Double allocatedBudget) { this.allocatedBudget = allocatedBudget; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public Employee getOwner() { return owner; }
    public void setOwner(Employee owner) { this.owner = owner; }
}
