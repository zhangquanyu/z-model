package com.zmodel.dto.response;

import com.zmodel.entity.PhysicalModel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PhysicalModelDTO {
    private String id;
    private String modelId;
    private String modelName;
    private String name;
    private String code;
    private String description;
    private String tableName;
    private String status;
    private List<PhysicalPropertyDTO> properties;
    private List<PhysicalMethodDTO> methods;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PhysicalModelDTO fromEntity(PhysicalModel entity) {
        return PhysicalModelDTO.builder()
                .id(entity.getId())
                .modelId(entity.getModelId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .tableName(entity.getTableName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PhysicalModelDTO withDetails(String modelName, List<PhysicalPropertyDTO> properties, List<PhysicalMethodDTO> methods) {
        this.modelName = modelName;
        this.properties = properties;
        this.methods = methods;
        return this;
    }
}
