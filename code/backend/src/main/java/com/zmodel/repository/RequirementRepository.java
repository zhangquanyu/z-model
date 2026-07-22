package com.zmodel.repository;

import com.zmodel.entity.Requirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    Page<Requirement> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    @Query("SELECT r FROM Requirement r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
           "(:status IS NULL OR :status = '' OR r.status = :status)")
    Page<Requirement> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}
