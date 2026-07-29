package com.zmodel.repository;

import com.zmodel.entity.BusinessOrchestration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessOrchestrationRepository extends JpaRepository<BusinessOrchestration, String> {
    Page<BusinessOrchestration> findByNameContaining(String keyword, Pageable pageable);
    boolean existsByCode(String code);
    long count();
}
