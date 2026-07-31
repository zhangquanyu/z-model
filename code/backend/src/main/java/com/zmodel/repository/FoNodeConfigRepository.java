package com.zmodel.repository;

import com.zmodel.entity.FoNodeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoNodeConfigRepository extends JpaRepository<FoNodeConfig, String> {
    
    List<FoNodeConfig> findByNodeId(String nodeId);
    
    Optional<FoNodeConfig> findFirstByNodeId(String nodeId);
    
    void deleteByNodeId(String nodeId);
}
