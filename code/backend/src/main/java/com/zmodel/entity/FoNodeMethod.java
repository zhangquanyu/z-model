package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fo_node_method")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoNodeMethod {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "node_id", nullable = false, length = 36)
    private String nodeId;

    @Column(name = "method_id", nullable = false, length = 36)
    private String methodId;

    @Column(name = "physical_method_id", length = 36)
    private String physicalMethodId;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
