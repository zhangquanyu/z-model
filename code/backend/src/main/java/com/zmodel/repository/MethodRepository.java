package com.zmodel.repository;

import com.zmodel.entity.Method;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodRepository extends JpaRepository<Method, String> {

    List<Method> findByModelIdOrderByName(String modelId);

    Page<Method> findByModelIdAndNameContaining(String modelId, String name, Pageable pageable);

    boolean existsByModelIdAndCode(String modelId, String code);

    List<Method> findByModelId(String modelId);

    List<Method> findByRequirementId(String requirementId);
}