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
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "method_id", nullable = false, length = 36)
    private String methodId;

    @Column(name = "property_id", nullable = false, length = 36)
    private String propertyId;

    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}