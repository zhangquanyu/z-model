package com.zmodel.repository;

import com.zmodel.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByExternalFlowNo1(String externalFlowNo1);

    Optional<Event> findByMainOrderNo(String mainOrderNo);

    Page<Event> findByExternalFlowNo1Containing(String externalFlowNo1, Pageable pageable);

    Page<Event> findByMemberCardNoContaining(String memberCardNo, Pageable pageable);

    Page<Event> findByEventType(String eventType, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "(:externalFlowNo1 IS NULL OR :externalFlowNo1 = '' OR e.externalFlowNo1 LIKE %:externalFlowNo1%) AND " +
           "(:memberCardNo IS NULL OR :memberCardNo = '' OR e.memberCardNo LIKE %:memberCardNo%) AND " +
           "(:eventType IS NULL OR :eventType = '' OR e.eventType = :eventType) AND " +
           "(:startTime IS NULL OR e.eventTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.eventTime <= :endTime)")
    Page<Event> findByConditions(@Param("externalFlowNo1") String externalFlowNo1,
                                  @Param("memberCardNo") String memberCardNo,
                                  @Param("eventType") String eventType,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.eventAmount), 0) FROM Event e WHERE " +
           "(:memberCardNo IS NULL OR :memberCardNo = '' OR e.memberCardNo = :memberCardNo) AND " +
           "(:eventType IS NULL OR :eventType = '' OR e.eventType = :eventType) AND " +
           "(:startTime IS NULL OR e.eventTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.eventTime <= :endTime)")
    BigDecimal sumEventAmount(@Param("memberCardNo") String memberCardNo,
                               @Param("eventType") String eventType,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    boolean existsByExternalFlowNo1(String externalFlowNo1);

    @Query("SELECT e FROM Event e WHERE e.externalFlowNo1 IN " +
           "(SELECT e2.externalFlowNo1 FROM Event e2 WHERE e2.id = :eventId) AND e.id != :eventId")
    List<Event> findRelatedEvents(@Param("eventId") Long eventId);
}
