package com.zmodel.dto.request;

import lombok.Data;

@Data
public class FunctionalOrchestrationCreateRequest {
    private String orchestrationId;
    private String name;
    private String code;
    private String description;
}
