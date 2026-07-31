package com.zmodel.repository;

import com.zmodel.entity.FoNodeMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoNodeMethodRepository extends JpaRepository<FoNodeMethod, String> {
    
    List<FoNodeMethod> findByNodeIdOrderBySortOrderAsc(String nodeId);
    
    void deleteByNodeId(String nodeId);
}
