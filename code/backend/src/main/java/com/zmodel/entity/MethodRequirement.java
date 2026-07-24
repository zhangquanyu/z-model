package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "method_requirement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodRequirement {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "method_id", length = 36, nullable = false)
    private String methodId;

    @Column(name = "requirement_id", length = 36, nullable = false)
    private String requirementId;
}