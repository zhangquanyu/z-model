package com.zmodel.dto.request;

import lombok.Data;

@Data
public class PhysicalModelCreateRequest {
    private String modelId;
    private String name;
    private String code;
    private String description;
    private String tableName;
}
