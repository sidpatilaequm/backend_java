package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.workflow.WorkflowRequestRO;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Deliberately extends the bare `Repository<T, ID>` marker interface, not `JpaRepository` — same
 * reasoning as ApprovalActionRepository: workflow_requests is a table the separate WorkFlow app
 * owns exclusively, this must stay read-only at the type level.
 */
public interface WorkflowRequestRepository extends Repository<WorkflowRequestRO, Integer> {
    Optional<WorkflowRequestRO> findById(Integer id);
}
