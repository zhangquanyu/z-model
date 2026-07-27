package com.zmodel.repository;

import com.zmodel.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {

    List<Property> findByModelIdOrderByName(String modelId);

    Page<Property> findByModelIdAndNameContaining(String modelId, String name, Pageable pageable);

    boolean existsByModelIdAndCode(String modelId, String code);

    boolean existsByModelIdAndIdNot(String modelId, String id);

    List<Property> findByModelId(String modelId);
}