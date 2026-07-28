package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bpmn_process_version")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmnProcessVersion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "process_id", nullable = false, length = 36)
    private String processId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "bpmn_xml", columnDefinition = "LONGTEXT")
    private String bpmnXml;

    @Column(name = "change_note", length = 500)
    private String changeNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
