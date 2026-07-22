package com.zmodel.repository;

import com.zmodel.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    Optional<Model> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT m FROM Model m WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR m.name LIKE %:keyword% OR m.code LIKE %:keyword% OR m.description LIKE %:keyword%)")
    Page<Model> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
