package com.zmodel.repository;

import com.zmodel.entity.PhysicalModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhysicalModelRepository extends JpaRepository<PhysicalModel, String> {
    
    Page<PhysicalModel> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code, Pageable pageable);
    
    List<PhysicalModel> findByModelId(String modelId);
    
    boolean existsByCode(String code);
}
