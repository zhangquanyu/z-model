package com.zmodel.dto.response;

import com.zmodel.entity.PhysicalProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PhysicalPropertyDTO {
    private String id;
    private String physicalModelId;
    private String sourcePropertyId;
    private String sourcePropertyName;
    private String sourceMethodId;
    private String sourceMethodName;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PhysicalPropertyDTO fromEntity(PhysicalProperty entity) {
        return PhysicalPropertyDTO.builder()
                .id(entity.getId())
                .physicalModelId(entity.getPhysicalModelId())
                .sourcePropertyId(entity.getSourcePropertyId())
                .sourceMethodId(entity.getSourceMethodId())
                .name(entity.getName())
                .code(entity.getCode())
                .dataType(entity.getDataType())
                .dbType(entity.getDbType())
                .dbLength(entity.getDbLength())
                .dbPrecision(entity.getDbPrecision())
                .dbScale(entity.getDbScale())
                .nullable(entity.getNullable())
                .isPrimaryKey(entity.getIsPrimaryKey())
                .isIndex(entity.getIsIndex())
                .defaultValue(entity.getDefaultValue())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
