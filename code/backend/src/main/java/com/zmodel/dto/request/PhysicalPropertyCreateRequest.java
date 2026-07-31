package com.zmodel.dto.request;

import lombok.Data;

@Data
public class PhysicalPropertyCreateRequest {
    private String physicalModelId;
    private String sourcePropertyId;
    private String sourceMethodId;
    private String name;
    private String code;
    private String dataType;
    private String dbType;
    private Integer dbLength;
    private Integer dbPrecision;
    private Integer dbScale;
    private Boolean nullable;
    private Boolean isPrimaryKey;
    private Boolean isIndex;
    private String defaultValue;
    private String description;
}
