package com.zmodel.repository;

import com.zmodel.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByModelId(Long modelId);

    List<Property> findByModelIdOrderByName(Long modelId);

    void deleteByModelId(Long modelId);

    void deleteByRequirementId(Long requirementId);

    boolean existsByModelIdAndCode(Long modelId, String code);
}
