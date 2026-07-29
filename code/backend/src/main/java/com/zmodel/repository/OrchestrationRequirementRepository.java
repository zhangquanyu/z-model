package com.zmodel.repository;

import com.zmodel.entity.OrchestrationRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrchestrationRequirementRepository extends JpaRepository<OrchestrationRequirement, String> {
    List<OrchestrationRequirement> findByOrchestrationId(String orchestrationId);
    void deleteByOrchestrationId(String orchestrationId);
    boolean existsByOrchestrationIdAndRequirementId(String orchestrationId, String requirementId);
}
