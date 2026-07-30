package com.zmodel.repository;

import com.zmodel.entity.OrchestrationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrchestrationNodeRepository extends JpaRepository<OrchestrationNode, String> {
    List<OrchestrationNode> findByOrchestrationIdOrderBySortOrder(String orchestrationId);
    List<OrchestrationNode> findByOrchestrationIdAndParentIdOrderBySortOrder(String orchestrationId, String parentId);
    void deleteByOrchestrationId(String orchestrationId);
    long countByOrchestrationId(String orchestrationId);
    long countByOrchestrationIdAndParentId(String orchestrationId, String parentId);
}
