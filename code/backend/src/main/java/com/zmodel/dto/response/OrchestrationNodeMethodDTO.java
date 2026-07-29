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
public class OrchestrationNodeMethodDTO {
    private String id;
    private String nodeId;
    private String methodId;
    private String methodName;
    private String methodCode;
    private String modelId;
    private String modelName;
    private String requirementId;
    private String requirementName;
    private String requirementCode;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
