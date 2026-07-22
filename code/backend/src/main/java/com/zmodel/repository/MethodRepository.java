package com.zmodel.repository;

import com.zmodel.entity.Method;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodRepository extends JpaRepository<Method, Long> {

    List<Method> findByModelId(Long modelId);

    List<Method> findByModelIdOrderByName(Long modelId);

    void deleteByModelId(Long modelId);

    void deleteByRequirementId(Long requirementId);

    boolean existsByModelIdAndCode(Long modelId, String code);
}
