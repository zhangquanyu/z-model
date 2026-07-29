package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orchestration_node_method")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationNodeMethod {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "node_id", nullable = false, length = 36)
    private String nodeId;

    @Column(name = "method_id", nullable = false, length = 36)
    private String methodId;

    @Column(name = "requirement_id", length = 36)
    private String requirementId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
