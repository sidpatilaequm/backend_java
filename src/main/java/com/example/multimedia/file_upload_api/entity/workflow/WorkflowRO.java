package com.example.multimedia.file_upload_api.entity.workflow;

import jakarta.persistence.*;

/**
 * Read-only mapping onto the `workflows` table — owned and written to exclusively by the
 * separate Python WorkFlow app (same `multimedia_governance` database). backend_java is a guest
 * reader here: nothing in this package is ever saved. See ApprovalActionRO for why this exists.
 */
@Entity
@Table(name = "workflows")
public class WorkflowRO {

    @Id
    private Integer id;

    private String name;

    private String type;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
}
