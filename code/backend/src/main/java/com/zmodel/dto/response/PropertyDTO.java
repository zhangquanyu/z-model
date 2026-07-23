package com.zmodel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {

    private String id;

    private String modelId;

    private String modelName;

    private String requirementId;

    private String requirementName;

    private String parentRequirementId;

    private String parentRequirementName;

    private String name;

    private String code;

    private String dataType;

    private String description;

    private Boolean required;

    private String defaultValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}