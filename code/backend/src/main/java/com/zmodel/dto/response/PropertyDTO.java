package com.zmodel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {

    private String id;

    private String modelId;

    private String modelName;

    private List<String> requirementIds;

    private List<String> requirementNames;

    private List<String> parentRequirementIds;

    private List<String> parentRequirementNames;

    private List<String> parentRequirementCodes;

    private List<String> parentRequirementStatuses;

    private String name;

    private String code;

    private String dataType;

    private String description;

    private Boolean required;

    private String defaultValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}