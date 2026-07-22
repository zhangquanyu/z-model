package com.zmodel.repository;

import com.zmodel.entity.ModelRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRequirementRepository extends JpaRepository<ModelRequirement, Long> {

    List<ModelRequirement> findByModelId(Long modelId);

    void deleteByModelId(Long modelId);

    void deleteByRequirementId(Long requirementId);

    List<ModelRequirement> findByRequirementId(Long requirementId);
}
