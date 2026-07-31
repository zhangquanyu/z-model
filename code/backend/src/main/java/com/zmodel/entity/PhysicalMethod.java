package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "physical_method")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalMethod {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "physical_model_id", nullable = false, length = 36)
    private String physicalModelId;

    @Column(name = "source_method_id", nullable = false, length = 36)
    private String sourceMethodId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "method_type", length = 30)
    private String methodType;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "sql_template", columnDefinition = "LONGTEXT")
    private String sqlTemplate;

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
