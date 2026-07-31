package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "physical_property")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalProperty {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "physical_model_id", nullable = false, length = 36)
    private String physicalModelId;

    @Column(name = "source_property_id", length = 36)
    private String sourcePropertyId;

    @Column(name = "source_method_id", length = 36)
    private String sourceMethodId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "db_type", length = 50)
    private String dbType;

    @Column(name = "db_length")
    private Integer dbLength;

    @Column(name = "db_precision")
    private Integer dbPrecision;

    @Column(name = "db_scale")
    private Integer dbScale;

    @Column(name = "nullable")
    @Builder.Default
    private Boolean nullable = true;

    @Column(name = "is_primary_key")
    @Builder.Default
    private Boolean isPrimaryKey = false;

    @Column(name = "is_index")
    @Builder.Default
    private Boolean isIndex = false;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

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
