package com.zmodel.repository;

import com.zmodel.entity.PhysicalMethodParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalMethodParamRepository extends JpaRepository<PhysicalMethodParam, String> {
    
    List<PhysicalMethodParam> findByPhysicalMethodId(String physicalMethodId);
    
    void deleteByPhysicalMethodId(String physicalMethodId);
}
