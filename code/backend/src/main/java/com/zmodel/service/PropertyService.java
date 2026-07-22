package com.zmodel.service;

import com.zmodel.dto.request.PropertyCreateRequest;
import com.zmodel.dto.request.PropertyUpdateRequest;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.entity.Property;
import com.zmodel.entity.Requirement;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.PropertyRepository;
import com.zmodel.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ModelRepository modelRepository;
    private final RequirementRepository requirementRepository;

    @Transactional
    public PropertyDTO create(Long modelId, PropertyCreateRequest request) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }

        if (!requirementRepository.existsById(request.getRequirementId())) {
            throw new RuntimeException("需求不存在: " + request.getRequirementId());
        }

        if (propertyRepository.existsByModelIdAndCode(modelId, request.getCode())) {
            throw new RuntimeException("属性编码已存在: " + request.getCode());
        }

        Property property = Property.builder()
                .modelId(modelId)
                .requirementId(request.getRequirementId())
                .name(request.getName())
                .code(request.getCode())
                .type(request.getType())
                .description(request.getDescription())
                .nullable(request.getNullable())
                .length(request.getLength())
                .build();

        property = propertyRepository.save(property);
        log.info("创建属性: id={}, name={}, modelId={}", property.getId(), property.getName(), modelId);
        return toDTO(property);
    }

    @Transactional(readOnly = true)
    public PropertyDTO getById(Long modelId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));
        
        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }
        return toDTO(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> listByModelId(Long modelId) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        List<Property> properties = propertyRepository.findByModelIdOrderByName(modelId);
        return properties.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PropertyDTO update(Long modelId, Long propertyId, PropertyUpdateRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }

        if (!requirementRepository.existsById(request.getRequirementId())) {
            throw new RuntimeException("需求不存在: " + request.getRequirementId());
        }

        property.setName(request.getName());
        property.setCode(request.getCode());
        property.setType(request.getType());
        property.setDescription(request.getDescription());
        property.setRequirementId(request.getRequirementId());
        
        if (request.getNullable() != null) {
            property.setNullable(request.getNullable());
        }
        if (request.getLength() != null) {
            property.setLength(request.getLength());
        }

        property = propertyRepository.save(property);
        log.info("更新属性: id={}, name={}", property.getId(), property.getName());
        return toDTO(property);
    }

    @Transactional
    public void delete(Long modelId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }

        propertyRepository.deleteById(propertyId);
        log.info("删除属性: id={}", propertyId);
    }

    private PropertyDTO toDTO(Property entity) {
        String requirementName = requirementRepository.findById(entity.getRequirementId())
                .map(Requirement::getName)
                .orElse("");

        return PropertyDTO.builder()
                .id(entity.getId())
                .modelId(entity.getModelId())
                .requirementId(entity.getRequirementId())
                .requirementName(requirementName)
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .description(entity.getDescription())
                .nullable(entity.getNullable())
                .length(entity.getLength())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
