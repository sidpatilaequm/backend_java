package com.example.multimedia.file_upload_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "dept_code", length = 20)
    private String deptCode;

    @Column(name = "name", length = 120, nullable = false)
    private String name = "DEFAULT_NAME";

    @Column(name = "dept_name", length = 120, nullable = false)
    private String deptName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @Column(name = "org_code", length = 50, nullable = false)
    private String orgCode = "DEFAULT";

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    @Column(name = "wbs", length = 50, nullable = false)
    private String wbs = "DEFAULT";

    public String getWbs() {
        return wbs;
    }

    public void setWbs(String wbs) {
        this.wbs = wbs;
    }
}
