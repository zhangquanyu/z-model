package com.zmodel.dto.request;

import lombok.Data;

@Data
public class OrchestrationNodeRequest {
    private String nodeType;
    private String nodeName;
    private String description;
    private Integer sortOrder;
    private Integer loopCount;
}
