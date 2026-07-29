package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orchestration_node")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationNode {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "orchestration_id", nullable = false, length = 36)
    private String orchestrationId;

    @Column(name = "node_type", length = 20)
    private String nodeType;

    @Column(name = "node_name", length = 200)
    private String nodeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "loop_count")
    private Integer loopCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
