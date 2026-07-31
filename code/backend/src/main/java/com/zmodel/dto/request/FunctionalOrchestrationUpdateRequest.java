package com.zmodel.dto.request;

import lombok.Data;

@Data
public class FunctionalOrchestrationUpdateRequest {
    private String name;
    private String code;
    private String description;
    private String status;
}
