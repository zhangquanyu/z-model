package com.zmodel.dto.response;

import com.zmodel.entity.PhysicalMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PhysicalMethodDTO {
    private String id;
    private String physicalModelId;
    private String sourceMethodId;
    private String sourceMethodName;
    private String name;
    private String code;
    private String methodType;
    private String description;
    private String sqlTemplate;
    private List<PhysicalMethodParamDTO> params;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PhysicalMethodDTO fromEntity(PhysicalMethod entity) {
        return PhysicalMethodDTO.builder()
                .id(entity.getId())
                .physicalModelId(entity.getPhysicalModelId())
                .sourceMethodId(entity.getSourceMethodId())
                .name(entity.getName())
                .code(entity.getCode())
                .methodType(entity.getMethodType())
                .description(entity.getDescription())
                .sqlTemplate(entity.getSqlTemplate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class PhysicalMethodParamDTO {
        private String id;
        private String physicalMethodId;
        private String physicalPropertyId;
        private String physicalPropertyName;
        private String paramType;
        private Integer sortOrder;
    }
}
