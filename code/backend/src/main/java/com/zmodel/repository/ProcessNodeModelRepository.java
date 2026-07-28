package com.zmodel.repository;

import com.zmodel.entity.ProcessNodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessNodeModelRepository extends JpaRepository<ProcessNodeModel, String> {

    List<ProcessNodeModel> findByProcessId(String processId);

    List<ProcessNodeModel> findByProcessIdAndNodeId(String processId, String nodeId);

    @Modifying
    void deleteByProcessId(String processId);

    @Modifying
    void deleteByProcessIdAndNodeId(String processId, String nodeId);
}
