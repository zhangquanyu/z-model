package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "functional_orchestration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalOrchestration {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "orchestration_id", length = 36)
    private String orchestrationId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "generated_code", columnDefinition = "LONGTEXT")
    private String generatedCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
