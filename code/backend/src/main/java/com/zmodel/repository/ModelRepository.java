package com.zmodel.repository;

import com.zmodel.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {

    boolean existsByCode(String code);

    Optional<Model> findByCode(String code);

    Page<Model> findByNameContaining(String name, Pageable pageable);
}