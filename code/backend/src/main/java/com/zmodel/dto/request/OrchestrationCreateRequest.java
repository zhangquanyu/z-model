package com.zmodel.dto.request;

import lombok.Data;

@Data
public class OrchestrationCreateRequest {
    private String name;
    private String code;
    private String description;
}
