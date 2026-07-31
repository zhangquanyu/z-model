package com.zmodel.repository;

import com.zmodel.entity.FoNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoNodeRepository extends JpaRepository<FoNode, String> {
    
    List<FoNode> findByOrchestrationIdOrderBySortOrderAsc(String orchestrationId);
    
    List<FoNode> findByOrchestrationIdAndParentIdOrderBySortOrderAsc(String orchestrationId, String parentId);
    
    void deleteByOrchestrationId(String orchestrationId);
}
