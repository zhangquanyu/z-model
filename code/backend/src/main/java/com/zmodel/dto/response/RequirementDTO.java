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
public class RequirementDTO {

    private String id;

    private String name;

    private String code;

    private String description;

    private String status;

    private String priority;

    private String requirementType;

    private String parentId;

    private String parentName;

    private List<RequirementDTO> children;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}