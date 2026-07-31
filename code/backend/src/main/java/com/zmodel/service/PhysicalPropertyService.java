package com.zmodel.service;

import com.zmodel.dto.request.PhysicalPropertyCreateRequest;
import com.zmodel.dto.response.PhysicalPropertyDTO;
import com.zmodel.entity.Property;
import com.zmodel.entity.PhysicalModel;
import com.zmodel.entity.PhysicalProperty;
import com.zmodel.entity.Method;
import com.zmodel.repository.MethodRepository;
import com.zmodel.repository.PhysicalModelRepository;
import com.zmodel.repository.PhysicalPropertyRepository;
import com.zmodel.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhysicalPropertyService {

    private final PhysicalPropertyRepository physicalPropertyRepository;
    private final PhysicalModelRepository physicalModelRepository;
    private final PropertyRepository propertyRepository;
    private final MethodRepository methodRepository;

    public List<PhysicalPropertyDTO> listByPhysicalModelId(String physicalModelId) {
        return physicalPropertyRepository.findByPhysicalModelId(physicalModelId).stream()
                .map(PhysicalPropertyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PhysicalPropertyDTO create(PhysicalPropertyCreateRequest request) {
        // 验证物理模型存在
        physicalModelRepository.findById(request.getPhysicalModelId())
                .orElseThrow(() -> new RuntimeException("物理模型不存在"));

        PhysicalProperty property = PhysicalProperty.builder()
                .id(UUID.randomUUID().toString())
                .physicalModelId(request.getPhysicalModelId())
                .sourcePropertyId(request.getSourcePropertyId())
                .sourceMethodId(request.getSourceMethodId())
                .name(request.getName())
                .code(request.getCode())
                .dataType(request.getDataType())
                .dbType(request.getDbType())
                .dbLength(request.getDbLength())
                .dbPrecision(request.getDbPrecision())
                .dbScale(request.getDbScale())
                .nullable(request.getNullable() != null ? request.getNullable() : true)
                .isPrimaryKey(request.getIsPrimaryKey() != null ? request.getIsPrimaryKey() : false)
                .isIndex(request.getIsIndex() != null ? request.getIsIndex() : false)
                .defaultValue(request.getDefaultValue())
                .description(request.getDescription())
                .build();

        PhysicalProperty saved = physicalPropertyRepository.save(property);
        PhysicalPropertyDTO dto = PhysicalPropertyDTO.fromEntity(saved);
        enrichWithSourceInfo(dto);
        return dto;
    }

    @Transactional
    public PhysicalPropertyDTO update(String id, PhysicalPropertyCreateRequest request) {
        PhysicalProperty property = physicalPropertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物理属性不存在"));

        if (request.getSourcePropertyId() != null) property.setSourcePropertyId(request.getSourcePropertyId());
        if (request.getSourceMethodId() != null) property.setSourceMethodId(request.getSourceMethodId());
        if (request.getName() != null) property.setName(request.getName());
        if (request.getCode() != null) property.setCode(request.getCode());
        if (request.getDataType() != null) property.setDataType(request.getDataType());
        if (request.getDbType() != null) property.setDbType(request.getDbType());
        if (request.getDbLength() != null) property.setDbLength(request.getDbLength());
        if (request.getDbPrecision() != null) property.setDbPrecision(request.getDbPrecision());
        if (request.getDbScale() != null) property.setDbScale(request.getDbScale());
        if (request.getNullable() != null) property.setNullable(request.getNullable());
        if (request.getIsPrimaryKey() != null) property.setIsPrimaryKey(request.getIsPrimaryKey());
        if (request.getIsIndex() != null) property.setIsIndex(request.getIsIndex());
        if (request.getDefaultValue() != null) property.setDefaultValue(request.getDefaultValue());
        if (request.getDescription() != null) property.setDescription(request.getDescription());

        PhysicalProperty saved = physicalPropertyRepository.save(property);
        PhysicalPropertyDTO dto = PhysicalPropertyDTO.fromEntity(saved);
        enrichWithSourceInfo(dto);
        return dto;
    }

    @Transactional
    public void delete(String id) {
        physicalPropertyRepository.deleteById(id);
    }

    public PhysicalPropertyDTO syncFromSource(String id) {
        PhysicalProperty property = physicalPropertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物理属性不存在"));

        // 如果有关联的源属性，同步其配置
        if (property.getSourcePropertyId() != null) {
            propertyRepository.findById(property.getSourcePropertyId()).ifPresent(source -> {
                property.setName(source.getName());
                property.setCode(source.getCode());
                property.setDataType(source.getDataType());
                property.setDescription(source.getDescription());
                if (property.getDbType() == null) {
                    property.setDbType(mapDataTypeToDbType(source.getDataType()));
                }
            });
        }

        PhysicalProperty saved = physicalPropertyRepository.save(property);
        return PhysicalPropertyDTO.fromEntity(saved);
    }

    private void enrichWithSourceInfo(PhysicalPropertyDTO dto) {
        if (dto.getSourcePropertyId() != null) {
            propertyRepository.findById(dto.getSourcePropertyId())
                    .ifPresent(p -> dto.setSourcePropertyName(p.getName()));
        }
        if (dto.getSourceMethodId() != null) {
            methodRepository.findById(dto.getSourceMethodId())
                    .ifPresent(m -> dto.setSourceMethodName(m.getName()));
        }
    }

    private String mapDataTypeToDbType(String dataType) {
        if (dataType == null) return "VARCHAR";
        switch (dataType) {
            case "STRING": return "VARCHAR";
            case "INTEGER": return "INT";
            case "LONG": return "BIGINT";
            case "DOUBLE": return "DECIMAL";
            case "BOOLEAN": return "TINYINT";
            case "DATE": return "DATE";
            case "DATETIME": return "DATETIME";
            case "OBJECT":
            case "ARRAY": return "JSON";
            default: return "VARCHAR";
        }
    }
}
