package com.zmodel.dto.request;

import lombok.Data;

@Data
public class OrchestrationNodeMethodRequest {
    private String methodId;
    private String requirementId;
    private String newRequirementName;
    private String newRequirementCode;
    private String newRequirementDescription;
    private String parentRequirementId;
    private Integer sortOrder;
}
