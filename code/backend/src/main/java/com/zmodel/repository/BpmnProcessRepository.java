package com.zmodel.repository;

import com.zmodel.entity.BpmnProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BpmnProcessRepository extends JpaRepository<BpmnProcess, String> {

    Page<BpmnProcess> findByNameContaining(String keyword, Pageable pageable);

    boolean existsByCode(String code);
}
