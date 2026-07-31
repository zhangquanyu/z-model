package com.zmodel.dto.request;

import lombok.Data;

@Data
public class FoNodeCreateRequest {
    private String orchestrationId;
    private String parentId;
    private String nodeType;
    private String nodeName;
    private String description;
    private Integer sortOrder;
    private Integer loopCount;
    private String conditionExpression;
    private String dbOperation;
    private String targetTable;
    private String apiUrl;
    private String apiMethod;
    private String transformType;
    private Integer width;
}
