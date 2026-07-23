package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "model_requirement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRequirement {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "model_id", nullable = false, length = 36)
    private String modelId;

    @Column(name = "requirement_id", nullable = false, length = 36)
    private String requirementId;
}