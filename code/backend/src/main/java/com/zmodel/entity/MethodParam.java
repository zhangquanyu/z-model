package com.zmodel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "method_param")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_id", nullable = false)
    private Long methodId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
