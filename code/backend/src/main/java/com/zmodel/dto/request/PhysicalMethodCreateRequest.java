package com.zmodel.dto.request;

import lombok.Data;

@Data
public class PhysicalMethodCreateRequest {
    private String physicalModelId;
    private String sourceMethodId;
    private String name;
    private String code;
    private String methodType;
    private String description;
    private String sqlTemplate;
}
