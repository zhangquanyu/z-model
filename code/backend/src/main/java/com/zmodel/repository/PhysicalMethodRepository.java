package com.zmodel.repository;

import com.zmodel.entity.PhysicalMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalMethodRepository extends JpaRepository<PhysicalMethod, String> {
    
    List<PhysicalMethod> findByPhysicalModelId(String physicalModelId);
    
    void deleteByPhysicalModelId(String physicalModelId);
}
