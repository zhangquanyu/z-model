package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "property_requirement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequirement {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "property_id", length = 36, nullable = false)
    private String propertyId;

    @Column(name = "requirement_id", length = 36, nullable = false)
    private String requirementId;
}