package com.zmodel.dto.response;

import com.zmodel.entity.FunctionalOrchestration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalOrchestrationDTO {
    private String id;
    private String orchestrationId;
    private String orchestrationName;
    private String name;
    private String code;
    private String description;
    private String status;
    private String generatedCode;
    private List<FoNodeDTO> nodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FunctionalOrchestrationDTO fromEntity(FunctionalOrchestration entity) {
        return FunctionalOrchestrationDTO.builder()
                .id(entity.getId())
                .orchestrationId(entity.getOrchestrationId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .generatedCode(entity.getGeneratedCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoNodeDTO {
        private String id;
        private String orchestrationId;
        private String parentId;
        private String nodeType;
        private String nodeName;
        private String label;
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
        private Double x;
        private Double y;
        private String physicalModelId;
        private List<FoNodeMethodDTO> methods;
        private List<FoNodeDTO> children;
        private FoNodeConfigDTO nodeConfig;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoNodeMethodDTO {
        private String id;
        private String nodeId;
        private String methodId;
        private String methodName;
        private String physicalMethodId;
        private String physicalMethodName;
        private String physicalModelId;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoNodeConfigDTO {
        private String id;
        private String nodeId;
        private String configKey;
        private String configValue;
    }
}
