package com.zmodel.service;

import com.zmodel.dto.request.ModelCreateRequest;
import com.zmodel.dto.request.ModelUpdateRequest;
import com.zmodel.dto.response.ModelDTO;
import com.zmodel.dto.response.RequirementDTO;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.dto.response.MethodDTO;
import com.zmodel.entity.Model;
import com.zmodel.entity.ModelRequirement;
import com.zmodel.entity.Requirement;
import com.zmodel.entity.Property;
import com.zmodel.entity.Method;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.ModelRequirementRepository;
import com.zmodel.repository.RequirementRepository;
import com.zmodel.repository.PropertyRepository;
import com.zmodel.repository.MethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelRequirementRepository modelRequirementRepository;
    private final RequirementRepository requirementRepository;
    private final PropertyRepository propertyRepository;
    private final MethodRepository methodRepository;
    private final EntityManager entityManager;

    @Transactional
    public ModelDTO create(ModelCreateRequest request) {
        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode();
        } else if (modelRepository.existsByCode(code)) {
            throw new RuntimeException("模型编码已存在: " + code);
        }

        Model model = Model.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .build();

        model = modelRepository.save(model);

        if (request.getRequirementIds() != null && !request.getRequirementIds().isEmpty()) {
            for (String requirementId : request.getRequirementIds()) {
                Requirement requirement = requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));
                if (!"MAIN".equals(requirement.getRequirementType())) {
                    throw new RuntimeException("模型只能关联主需求");
                }
                ModelRequirement mr = ModelRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .modelId(model.getId())
                        .requirementId(requirementId)
                        .build();
                modelRequirementRepository.save(mr);
            }
        }

        log.info("创建模型: id={}, name={}, code={}", model.getId(), model.getName(), model.getCode());
        return toDTO(model);
    }

    @Transactional(readOnly = true)
    public ModelDTO getById(String id) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + id));
        return toDTO(model);
    }

    @Transactional(readOnly = true)
    public Page<ModelDTO> list(String keyword, Pageable pageable) {
        Page<Model> page;
        if (keyword == null || keyword.isEmpty()) {
            page = modelRepository.findAll(pageable);
        } else {
            page = modelRepository.findByNameContaining(keyword, pageable);
        }
        return page.map(this::toDTO);
    }

    public List<ModelDTO> listAll() {
        return modelRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModelDTO update(String id, ModelUpdateRequest request) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + id));

        model.setName(request.getName());
        model.setDescription(request.getDescription());

        model = modelRepository.save(model);

        // 刷新并清除持久化上下文，确保删除操作立即生效
        entityManager.flush();
        entityManager.clear();

        // 始终先删除旧的关联，防止重复
        modelRequirementRepository.deleteByModelId(id);

        // 再次刷新，确保删除操作完成
        entityManager.flush();

        // 插入新的关联，去重处理
        if (request.getRequirementIds() != null && !request.getRequirementIds().isEmpty()) {
            Set<String> uniqueRequirementIds = new LinkedHashSet<>(request.getRequirementIds());
            for (String requirementId : uniqueRequirementIds) {
                Requirement requirement = requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));
                if (!"MAIN".equals(requirement.getRequirementType())) {
                    throw new RuntimeException("模型只能关联主需求");
                }
                ModelRequirement mr = ModelRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .modelId(model.getId())
                        .requirementId(requirementId)
                        .build();
                modelRequirementRepository.save(mr);
            }
        }

        log.info("更新模型: id={}, name={}", model.getId(), model.getName());
        return toDTO(model);
    }

    @Transactional
    public void delete(String id) {
        if (!modelRepository.existsById(id)) {
            throw new RuntimeException("模型不存在: " + id);
        }
        modelRepository.deleteById(id);
        log.info("删除模型: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<ModelDTO> getModelsByRequirement(String requirementId) {
        List<ModelRequirement> mrs = modelRequirementRepository.findByRequirementId(requirementId);
        return mrs.stream()
                .map(mr -> modelRepository.findById(mr.getModelId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequirementDTO> getModelRequirements(String modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
        List<ModelRequirement> mrs = modelRequirementRepository.findByModelId(modelId);
        return mrs.stream()
                .map(mr -> requirementRepository.findById(mr.getRequirementId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(r -> RequirementDTO.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .code(r.getCode())
                        .description(r.getDescription())
                        .status(r.getStatus())
                        .priority(r.getPriority())
                        .requirementType(r.getRequirementType())
                        .build())
                .collect(Collectors.toList());
    }

    private String generateCode() {
        long count = modelRepository.count();
        return String.format("MODEL-%04d", count + 1);
    }

    private ModelDTO toSummaryDTO(Model entity) {
        return ModelDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ModelDTO toDTO(Model entity) {
        List<ModelRequirement> mrs = modelRequirementRepository.findByModelId(entity.getId());
        List<RequirementDTO> requirements = mrs.stream()
                .map(mr -> requirementRepository.findById(mr.getRequirementId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(r -> RequirementDTO.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .code(r.getCode())
                        .description(r.getDescription())
                        .status(r.getStatus())
                        .priority(r.getPriority())
                        .requirementType(r.getRequirementType())
                        .build())
                .collect(Collectors.toList());

        List<PropertyDTO> properties = propertyRepository.findByModelId(entity.getId())
                .stream()
                .map(p -> PropertyDTO.builder()
                        .id(p.getId())
                        .modelId(p.getModelId())
                        .name(p.getName())
                        .code(p.getCode())
                        .dataType(p.getDataType())
                        .description(p.getDescription())
                        .required(p.getRequired())
                        .defaultValue(p.getDefaultValue())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        List<MethodDTO> methods = methodRepository.findByModelId(entity.getId())
                .stream()
                .map(m -> MethodDTO.builder()
                        .id(m.getId())
                        .modelId(m.getModelId())
                        .name(m.getName())
                        .code(m.getCode())
                        .description(m.getDescription())
                        .createdAt(m.getCreatedAt())
                        .updatedAt(m.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ModelDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .requirements(requirements)
                .properties(properties)
                .methods(methods)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}