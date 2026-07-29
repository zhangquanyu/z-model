package com.zmodel.repository;

import com.zmodel.entity.MethodRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodRequirementRepository extends JpaRepository<MethodRequirement, String> {

    List<MethodRequirement> findByMethodId(String methodId);

    List<MethodRequirement> findByRequirementId(String requirementId);

    List<MethodRequirement> findByRequirementIdIn(List<String> requirementIds);

    void deleteByMethodId(String methodId);

    boolean existsByMethodIdAndRequirementId(String methodId, String requirementId);
}