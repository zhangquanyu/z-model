package com.zmodel.repository;

import com.zmodel.entity.BpmnProcessVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BpmnProcessVersionRepository extends JpaRepository<BpmnProcessVersion, String> {

    List<BpmnProcessVersion> findByProcessIdOrderByVersionDesc(String processId);

    Optional<BpmnProcessVersion> findByProcessIdAndVersion(String processId, Integer version);

    int countByProcessId(String processId);
}
