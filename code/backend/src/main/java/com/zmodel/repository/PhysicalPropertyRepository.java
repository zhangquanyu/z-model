package com.zmodel.repository;

import com.zmodel.entity.PhysicalProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalPropertyRepository extends JpaRepository<PhysicalProperty, String> {
    
    List<PhysicalProperty> findByPhysicalModelId(String physicalModelId);
    
    void deleteByPhysicalModelId(String physicalModelId);
}
