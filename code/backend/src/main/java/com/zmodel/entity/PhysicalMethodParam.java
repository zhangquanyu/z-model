package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "physical_method_param")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalMethodParam {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "physical_method_id", nullable = false, length = 36)
    private String physicalMethodId;

    @Column(name = "physical_property_id", nullable = false, length = 36)
    private String physicalPropertyId;

    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;

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
