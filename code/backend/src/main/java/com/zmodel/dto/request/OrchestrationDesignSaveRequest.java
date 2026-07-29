package com.zmodel.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrchestrationDesignSaveRequest {
    private String name;
    private String code;
    private String description;
    private String status;
    private List<NodeDesignItem> nodes;

    @Data
    public static class NodeDesignItem {
        private String id;
        private String nodeName;
        private String description;
        private String nodeType;
        private Integer sortOrder;
        private Integer loopCount;
        private List<MethodDesignItem> methods;
    }

    @Data
    public static class MethodDesignItem {
        private String id;
        private String methodId;
        private String subRequirementId;
        private Integer sortOrder;
    }
}
