package com.zmodel.service;

import com.zmodel.dto.request.ModelCreateRequest;
import com.zmodel.dto.request.ModelUpdateRequest;
import com.zmodel.dto.response.ModelDTO;
import com.zmodel.dto.response.RequirementDTO;
import com.zmodel.entity.Model;
import com.zmodel.entity.ModelRequirement;
import com.zmodel.entity.Requirement;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.ModelRequirementRepository;
import com.zmodel.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelRequirementRepository modelRequirementRepository;
    private final RequirementRepository requirementRepository;

    @Transactional
    public ModelDTO create(ModelCreateRequest request) {
        if (modelRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("模型编码已存在: " + request.getCode());
        }

        Model model = Model.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        model = modelRepository.save(model);

        if (request.getRequirementIds() != null && !request.getRequirementIds().isEmpty()) {
            for (Long requirementId : request.getRequirementIds()) {
                if (!requirementRepository.existsById(requirementId)) {
                    throw new RuntimeException("需求不存在: " + requirementId);
                }
                ModelRequirement mr = ModelRequirement.builder()
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
    public ModelDTO getById(Long id) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + id));
        return toDTO(model);
    }

    @Transactional(readOnly = true)
    public Page<ModelDTO> list(String keyword, Pageable pageable) {
        Page<Model> page = modelRepository.findByKeyword(keyword, pageable);
        return page.map(this::toDTO);
    }

    @Transactional
    public ModelDTO update(Long id, ModelUpdateRequest request) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + id));

        model.setName(request.getName());
        model.setDescription(request.getDescription());

        model = modelRepository.save(model);

        modelRequirementRepository.deleteByModelId(id);

        if (request.getRequirementIds() != null && !request.getRequirementIds().isEmpty()) {
            for (Long requirementId : request.getRequirementIds()) {
                if (!requirementRepository.existsById(requirementId)) {
                    throw new RuntimeException("需求不存在: " + requirementId);
                }
                ModelRequirement mr = ModelRequirement.builder()
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
    public void delete(Long id) {
        if (!modelRepository.existsById(id)) {
            throw new RuntimeException("模型不存在: " + id);
        }
        modelRepository.deleteById(id);
        log.info("删除模型: id={}", id);
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
                        .description(r.getDescription())
                        .status(r.getStatus())
                        .priority(r.getPriority())
                        .build())
                .collect(Collectors.toList());

        return ModelDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .requirements(requirements)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
