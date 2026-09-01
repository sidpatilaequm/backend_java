package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.workflow.ApprovalActionRO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Deliberately extends the bare `Repository<T, ID>` marker interface, not `JpaRepository` — this
 * repository must never expose save()/delete(), since ApprovalActionRO maps onto a table the
 * separate WorkFlow app owns exclusively (see ApprovalActionRO's javadoc). Read-only at the type
 * level, not just by convention.
 */
public interface ApprovalActionRepository extends Repository<ApprovalActionRO, Integer> {

    @Query("""
        SELECT aa FROM ApprovalActionRO aa
        WHERE aa.requestStage.workflowRequest.submitter.superAdmin.superAdminId = :tenantId
        ORDER BY aa.actedAt DESC
        """)
    Page<ApprovalActionRO> findApprovalsForTenant(@Param("tenantId") Long tenantId, Pageable pageable);
}
