package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "process_node_model")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessNodeModel {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "process_id", nullable = false, length = 36)
    private String processId;

    @Column(name = "node_id", nullable = false, length = 100)
    private String nodeId;

    @Column(name = "model_id", nullable = false, length = 36)
    private String modelId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
