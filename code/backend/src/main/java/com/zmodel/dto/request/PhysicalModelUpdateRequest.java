package com.zmodel.dto.request;

import lombok.Data;

@Data
public class PhysicalModelUpdateRequest {
    private String name;
    private String code;
    private String description;
    private String tableName;
    private String status;
}
