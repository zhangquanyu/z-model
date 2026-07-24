package com.zmodel.repository;

import com.zmodel.entity.PropertyRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRequirementRepository extends JpaRepository<PropertyRequirement, String> {

    List<PropertyRequirement> findByPropertyId(String propertyId);

    List<PropertyRequirement> findByRequirementId(String requirementId);

    void deleteByPropertyId(String propertyId);

    boolean existsByPropertyIdAndRequirementId(String propertyId, String requirementId);
}