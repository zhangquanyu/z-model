package com.zmodel.repository;

import com.zmodel.entity.OrchestrationNodeMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrchestrationNodeMethodRepository extends JpaRepository<OrchestrationNodeMethod, String> {
    List<OrchestrationNodeMethod> findByNodeIdOrderBySortOrder(String nodeId);
    void deleteByNodeId(String nodeId);
    void deleteByNodeIdAndMethodId(String nodeId, String methodId);
    Optional<OrchestrationNodeMethod> findByNodeIdAndMethodId(String nodeId, String methodId);
    long countByNodeId(String nodeId);
}
