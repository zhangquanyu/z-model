package com.zmodel.repository;

import com.zmodel.entity.ModelRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRequirementRepository extends JpaRepository<ModelRequirement, String> {

    List<ModelRequirement> findByModelId(String modelId);

    List<ModelRequirement> findByRequirementId(String requirementId);

    void deleteByModelId(String modelId);

    void deleteByRequirementId(String requirementId);

    boolean existsByModelIdAndRequirementId(String modelId, String requirementId);
}