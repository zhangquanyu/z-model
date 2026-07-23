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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ModelRepository modelRepository;
    private final RequirementRepository requirementRepository;

    @Transactional
    public PropertyDTO create(String modelId, PropertyCreateRequest request) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }

        Requirement parentRequirement = requirementRepository.findById(request.getParentRequirementId())
                .orElseThrow(() -> new RuntimeException("主需求不存在: " + request.getParentRequirementId()));

        if (!"MAIN".equals(parentRequirement.getRequirementType())) {
            throw new RuntimeException("只能关联主需求");
        }

        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode(modelId);
        } else if (propertyRepository.existsByModelIdAndCode(modelId, code)) {
            throw new RuntimeException("属性编码已存在: " + code);
        }

        String subRequirementName = "[" + request.getName() + "] 属性描述";
        Requirement subRequirement = Requirement.builder()
                .id(UUID.randomUUID().toString())
                .name(subRequirementName)
                .code(generateSubCode(parentRequirement.getCode()))
                .description(request.getDescription())
                .status(parentRequirement.getStatus())
                .priority(parentRequirement.getPriority())
                .requirementType("SUB")
                .parentId(request.getParentRequirementId())
                .build();
        subRequirement = requirementRepository.save(subRequirement);

        Property property = Property.builder()
                .id(UUID.randomUUID().toString())
                .modelId(modelId)
                .requirementId(subRequirement.getId())
                .name(request.getName())
                .code(code)
                .dataType(request.getDataType())
                .description(request.getDescription())
                .required(request.getRequired())
                .defaultValue(request.getDefaultValue())
                .build();

        property = propertyRepository.save(property);
        log.info("创建属性: id={}, name={}, modelId={}, requirementId={}", 
                property.getId(), property.getName(), modelId, subRequirement.getId());
        return toDTO(property);
    }

    @Transactional(readOnly = true)
    public PropertyDTO getById(String modelId, String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }
        return toDTO(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> listByModelId(String modelId) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        List<Property> properties = propertyRepository.findByModelIdOrderByName(modelId);
        return properties.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PropertyDTO update(String modelId, String propertyId, PropertyUpdateRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }

        Requirement currentSubRequirement = requirementRepository.findById(property.getRequirementId())
                .orElseThrow(() -> new RuntimeException("关联的子需求不存在"));

        String newParentId = request.getParentRequirementId();
        if (!newParentId.equals(currentSubRequirement.getParentId())) {
            Requirement newParentRequirement = requirementRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("主需求不存在: " + newParentId));

            if (!"MAIN".equals(newParentRequirement.getRequirementType())) {
                throw new RuntimeException("只能关联主需求");
            }

            String subRequirementName = "[" + request.getName() + "] 属性描述";
            Requirement newSubRequirement = Requirement.builder()
                    .id(UUID.randomUUID().toString())
                    .name(subRequirementName)
                    .code(generateSubCode(newParentRequirement.getCode()))
                    .description(request.getDescription())
                    .status(newParentRequirement.getStatus())
                    .priority(newParentRequirement.getPriority())
                    .requirementType("SUB")
                    .parentId(newParentId)
                    .build();
            newSubRequirement = requirementRepository.save(newSubRequirement);

            requirementRepository.deleteById(currentSubRequirement.getId());
            property.setRequirementId(newSubRequirement.getId());
        } else {
            currentSubRequirement.setName("[" + request.getName() + "] 属性描述");
            currentSubRequirement.setDescription(request.getDescription());
            requirementRepository.save(currentSubRequirement);
        }

        property.setName(request.getName());
        property.setCode(request.getCode());
        property.setDataType(request.getDataType());
        property.setDescription(request.getDescription());
        property.setRequired(request.getRequired());
        property.setDefaultValue(request.getDefaultValue());

        property = propertyRepository.save(property);
        log.info("更新属性: id={}, name={}", property.getId(), property.getName());
        return toDTO(property);
    }

    @Transactional
    public void delete(String modelId, String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }

        if (property.getRequirementId() != null) {
            requirementRepository.deleteById(property.getRequirementId());
        }

        propertyRepository.deleteById(propertyId);
        log.info("删除属性: id={}", propertyId);
    }

    private PropertyDTO toDTO(Property entity) {
        Requirement subRequirement = null;
        String requirementName = "";
        String parentRequirementId = null;
        final String[] parentRequirementName = {""};

        if (entity.getRequirementId() != null) {
            subRequirement = requirementRepository.findById(entity.getRequirementId()).orElse(null);
            if (subRequirement != null) {
                requirementName = subRequirement.getName();
                if (subRequirement.getParentId() != null) {
                    parentRequirementId = subRequirement.getParentId();
                    String parentId = subRequirement.getParentId();
                    requirementRepository.findById(parentId)
                            .ifPresent(parent -> parentRequirementName[0] = parent.getName());
                }
            }
        }

        String modelName = modelRepository.findById(entity.getModelId())
                .map(m -> m.getName())
                .orElse("");

        return PropertyDTO.builder()
                .id(entity.getId())
                .modelId(entity.getModelId())
                .modelName(modelName)
                .requirementId(entity.getRequirementId())
                .requirementName(requirementName)
                .parentRequirementId(parentRequirementId)
                .parentRequirementName(parentRequirementName[0])
                .name(entity.getName())
                .code(entity.getCode())
                .dataType(entity.getDataType())
                .description(entity.getDescription())
                .required(entity.getRequired())
                .defaultValue(entity.getDefaultValue())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String generateCode(String modelId) {
        long count = propertyRepository.findByModelId(modelId).size();
        return String.format("PROP-%04d", count + 1);
    }

    private String generateSubCode(String parentCode) {
        Requirement parent = requirementRepository.findByCode(parentCode);
        if (parent == null) {
            return parentCode + "-1";
        }
        List<Requirement> subRequirements = requirementRepository.findByParentId(parent.getId());
        return parentCode + "-" + (subRequirements.size() + 1);
    }
}