package com.zmodel.service;

import com.zmodel.dto.request.MethodCreateRequest;
import com.zmodel.dto.request.MethodUpdateRequest;
import com.zmodel.dto.response.MethodDTO;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.entity.Method;
import com.zmodel.entity.MethodParam;
import com.zmodel.entity.Property;
import com.zmodel.entity.Requirement;
import com.zmodel.repository.MethodParamRepository;
import com.zmodel.repository.MethodRepository;
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
public class MethodService {

    private final MethodRepository methodRepository;
    private final MethodParamRepository methodParamRepository;
    private final ModelRepository modelRepository;
    private final RequirementRepository requirementRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public MethodDTO create(Long modelId, MethodCreateRequest request) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }

        if (!requirementRepository.existsById(request.getRequirementId())) {
            throw new RuntimeException("需求不存在: " + request.getRequirementId());
        }

        if (methodRepository.existsByModelIdAndCode(modelId, request.getCode())) {
            throw new RuntimeException("方法编码已存在: " + request.getCode());
        }

        Method method = Method.builder()
                .modelId(modelId)
                .requirementId(request.getRequirementId())
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        method = methodRepository.save(method);

        saveMethodParams(method.getId(), "INPUT", request.getInputParams());
        saveMethodParams(method.getId(), "OUTPUT", request.getOutputParams());

        log.info("创建方法: id={}, name={}, modelId={}", method.getId(), method.getName(), modelId);
        return toDTO(method);
    }

    @Transactional(readOnly = true)
    public MethodDTO getById(Long modelId, Long methodId) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }
        return toDTO(method);
    }

    @Transactional(readOnly = true)
    public List<MethodDTO> listByModelId(Long modelId) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        List<Method> methods = methodRepository.findByModelIdOrderByName(modelId);
        return methods.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MethodDTO update(Long modelId, Long methodId, MethodUpdateRequest request) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }

        if (!requirementRepository.existsById(request.getRequirementId())) {
            throw new RuntimeException("需求不存在: " + request.getRequirementId());
        }

        method.setName(request.getName());
        method.setCode(request.getCode());
        method.setDescription(request.getDescription());
        method.setRequirementId(request.getRequirementId());

        method = methodRepository.save(method);

        methodParamRepository.deleteByMethodId(methodId);

        saveMethodParams(method.getId(), "INPUT", request.getInputParams());
        saveMethodParams(method.getId(), "OUTPUT", request.getOutputParams());

        log.info("更新方法: id={}, name={}", method.getId(), method.getName());
        return toDTO(method);
    }

    @Transactional
    public void delete(Long modelId, Long methodId) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }

        methodRepository.deleteById(methodId);
        log.info("删除方法: id={}", methodId);
    }

    private void saveMethodParams(Long methodId, String paramType, List<Long> propertyIds) {
        if (propertyIds != null && !propertyIds.isEmpty()) {
            int order = 0;
            for (Long propertyId : propertyIds) {
                if (!propertyRepository.existsById(propertyId)) {
                    throw new RuntimeException("属性不存在: " + propertyId);
                }
                MethodParam param = MethodParam.builder()
                        .methodId(methodId)
                        .propertyId(propertyId)
                        .paramType(paramType)
                        .sortOrder(order++)
                        .build();
                methodParamRepository.save(param);
            }
        }
    }

    private MethodDTO toDTO(Method entity) {
        String requirementName = requirementRepository.findById(entity.getRequirementId())
                .map(Requirement::getName)
                .orElse("");

        List<PropertyDTO> inputParams = methodParamRepository.findByMethodIdAndParamType(entity.getId(), "INPUT")
                .stream()
                .map(mp -> propertyRepository.findById(mp.getPropertyId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toPropertyDTO)
                .collect(Collectors.toList());

        List<PropertyDTO> outputParams = methodParamRepository.findByMethodIdAndParamType(entity.getId(), "OUTPUT")
                .stream()
                .map(mp -> propertyRepository.findById(mp.getPropertyId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toPropertyDTO)
                .collect(Collectors.toList());

        return MethodDTO.builder()
                .id(entity.getId())
                .modelId(entity.getModelId())
                .requirementId(entity.getRequirementId())
                .requirementName(requirementName)
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .inputParams(inputParams)
                .outputParams(outputParams)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PropertyDTO toPropertyDTO(Property entity) {
        return PropertyDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .description(entity.getDescription())
                .build();
    }
}
