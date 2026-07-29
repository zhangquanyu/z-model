package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orchestration_requirement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationRequirement {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "orchestration_id", nullable = false, length = 36)
    private String orchestrationId;

    @Column(name = "requirement_id", nullable = false, length = 36)
    private String requirementId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
