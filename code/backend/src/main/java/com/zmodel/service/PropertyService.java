package com.zmodel.service;

import com.zmodel.dto.request.PropertyCreateRequest;
import com.zmodel.dto.request.PropertyUpdateRequest;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.entity.Property;
import com.zmodel.entity.PropertyRequirement;
import com.zmodel.entity.Requirement;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.PropertyRequirementRepository;
import com.zmodel.repository.PropertyRepository;
import com.zmodel.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final PropertyRequirementRepository propertyRequirementRepository;

    @Transactional
    public PropertyDTO create(String modelId, PropertyCreateRequest request) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("主需求不存在: " + parentId));
            if (!"MAIN".equals(parent.getRequirementType())) {
                throw new RuntimeException("只能关联主需求");
            }
        }

        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode(modelId);
        } else if (propertyRepository.existsByModelIdAndCode(modelId, code)) {
            throw new RuntimeException("属性编码已存在: " + code);
        }

        Property property = Property.builder()
                .id(UUID.randomUUID().toString())
                .modelId(modelId)
                .name(request.getName())
                .code(code)
                .dataType(request.getDataType())
                .description(request.getDescription())
                .required(request.getRequired())
                .defaultValue(request.getDefaultValue())
                .build();

        property = propertyRepository.save(property);

        List<Requirement> subRequirements = new ArrayList<>();
        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId).orElse(null);
            if (parent != null) {
                String subRequirementName = "[" + request.getName() + "] 属性描述";
                Requirement subRequirement = Requirement.builder()
                        .id(UUID.randomUUID().toString())
                        .name(subRequirementName)
                        .code(generateSubCode(parent.getCode()))
                        .description(request.getDescription())
                        .status(parent.getStatus())
                        .priority(parent.getPriority())
                        .requirementType("SUB")
                        .parentId(parentId)
                        .build();
                subRequirement = requirementRepository.save(subRequirement);
                subRequirements.add(subRequirement);

                PropertyRequirement pr = PropertyRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .propertyId(property.getId())
                        .requirementId(subRequirement.getId())
                        .build();
                propertyRequirementRepository.save(pr);
            }
        }

        log.info("创建属性: id={}, name={}, modelId={}", property.getId(), property.getName(), modelId);
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
    public Page<PropertyDTO> listByModelId(String modelId, String name, Pageable pageable) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        Page<Property> properties = propertyRepository.findByModelIdAndNameContaining(modelId, name, pageable);
        return properties.map(this::toDTO);
    }

    @Transactional
    public PropertyDTO update(String modelId, String propertyId, PropertyUpdateRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("属性不存在: " + propertyId));

        if (!property.getModelId().equals(modelId)) {
            throw new RuntimeException("属性不属于该模型");
        }

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("主需求不存在: " + parentId));
            if (!"MAIN".equals(parent.getRequirementType())) {
                throw new RuntimeException("只能关联主需求");
            }
        }

        List<PropertyRequirement> existingPRs = propertyRequirementRepository.findByPropertyId(propertyId);
        for (PropertyRequirement pr : existingPRs) {
            requirementRepository.deleteById(pr.getRequirementId());
            propertyRequirementRepository.deleteById(pr.getId());
        }

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId).orElse(null);
            if (parent != null) {
                String subRequirementName = "[" + request.getName() + "] 属性描述";
                Requirement subRequirement = Requirement.builder()
                        .id(UUID.randomUUID().toString())
                        .name(subRequirementName)
                        .code(generateSubCode(parent.getCode()))
                        .description(request.getDescription())
                        .status(parent.getStatus())
                        .priority(parent.getPriority())
                        .requirementType("SUB")
                        .parentId(parentId)
                        .build();
                subRequirement = requirementRepository.save(subRequirement);

                PropertyRequirement pr = PropertyRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .propertyId(property.getId())
                        .requirementId(subRequirement.getId())
                        .build();
                propertyRequirementRepository.save(pr);
            }
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

        List<PropertyRequirement> prs = propertyRequirementRepository.findByPropertyId(propertyId);
        for (PropertyRequirement pr : prs) {
            requirementRepository.deleteById(pr.getRequirementId());
            propertyRequirementRepository.deleteById(pr.getId());
        }

        propertyRepository.deleteById(propertyId);
        log.info("删除属性: id={}", propertyId);
    }

    private PropertyDTO toDTO(Property entity) {
        List<PropertyRequirement> prs = propertyRequirementRepository.findByPropertyId(entity.getId());
        
        List<String> requirementIds = new ArrayList<>();
        List<String> requirementNames = new ArrayList<>();
        List<String> parentRequirementIds = new ArrayList<>();
        List<String> parentRequirementNames = new ArrayList<>();

        for (PropertyRequirement pr : prs) {
            Requirement subReq = requirementRepository.findById(pr.getRequirementId()).orElse(null);
            if (subReq != null) {
                requirementIds.add(subReq.getId());
                requirementNames.add(subReq.getName());
                if (subReq.getParentId() != null) {
                    parentRequirementIds.add(subReq.getParentId());
                    requirementRepository.findById(subReq.getParentId())
                            .ifPresent(parent -> parentRequirementNames.add(parent.getName()));
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
                .requirementIds(requirementIds)
                .requirementNames(requirementNames)
                .parentRequirementIds(parentRequirementIds)
                .parentRequirementNames(parentRequirementNames)
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