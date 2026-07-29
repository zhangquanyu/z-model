package com.zmodel.dto.request;

import lombok.Data;

@Data
public class OrchestrationUpdateRequest {
    private String name;
    private String description;
    private String status;
}
