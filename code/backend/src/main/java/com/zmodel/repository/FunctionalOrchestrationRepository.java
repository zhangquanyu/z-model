package com.zmodel.repository;

import com.zmodel.entity.FunctionalOrchestration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FunctionalOrchestrationRepository extends JpaRepository<FunctionalOrchestration, String> {
    
    Page<FunctionalOrchestration> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code, Pageable pageable);
    
    Optional<FunctionalOrchestration> findByOrchestrationId(String orchestrationId);
    
    boolean existsByOrchestrationId(String orchestrationId);
    
    boolean existsByCode(String code);
}
