package com.zmodel.service;

import com.zmodel.dto.request.MethodCreateRequest;
import com.zmodel.dto.request.MethodUpdateRequest;
import com.zmodel.dto.response.MethodDTO;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.entity.Method;
import com.zmodel.entity.MethodParam;
import com.zmodel.entity.MethodRequirement;
import com.zmodel.entity.Property;
import com.zmodel.entity.Requirement;
import com.zmodel.repository.MethodParamRepository;
import com.zmodel.repository.MethodRequirementRepository;
import com.zmodel.repository.MethodRepository;
import com.zmodel.repository.ModelRepository;
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
public class MethodService {

    private final MethodRepository methodRepository;
    private final MethodParamRepository methodParamRepository;
    private final MethodRequirementRepository methodRequirementRepository;
    private final ModelRepository modelRepository;
    private final RequirementRepository requirementRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public MethodDTO create(String modelId, MethodCreateRequest request) {
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
        } else if (methodRepository.existsByModelIdAndCode(modelId, code)) {
            throw new RuntimeException("方法编码已存在: " + code);
        }

        Method method = Method.builder()
                .id(UUID.randomUUID().toString())
                .modelId(modelId)
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .build();

        method = methodRepository.save(method);

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId).orElse(null);
            if (parent != null) {
                String subRequirementName = "[" + request.getName() + "] 方法描述";
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

                MethodRequirement mr = MethodRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .methodId(method.getId())
                        .requirementId(subRequirement.getId())
                        .build();
                methodRequirementRepository.save(mr);
            }
        }

        saveMethodParams(method.getId(), "INPUT", request.getInputParams());
        saveMethodParams(method.getId(), "OUTPUT", request.getOutputParams());

        log.info("创建方法: id={}, name={}, modelId={}", method.getId(), method.getName(), modelId);
        return toDTO(method);
    }

    @Transactional(readOnly = true)
    public MethodDTO getById(String modelId, String methodId) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }
        return toDTO(method);
    }

    @Transactional(readOnly = true)
    public Page<MethodDTO> listByModelId(String modelId, String name, Pageable pageable) {
        if (!modelRepository.existsById(modelId)) {
            throw new RuntimeException("模型不存在: " + modelId);
        }
        Page<Method> methods = methodRepository.findByModelIdAndNameContaining(modelId, name, pageable);
        return methods.map(this::toDTO);
    }

    @Transactional
    public MethodDTO update(String modelId, String methodId, MethodUpdateRequest request) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("主需求不存在: " + parentId));
            if (!"MAIN".equals(parent.getRequirementType())) {
                throw new RuntimeException("只能关联主需求");
            }
        }

        List<MethodRequirement> existingMRs = methodRequirementRepository.findByMethodId(methodId);
        for (MethodRequirement mr : existingMRs) {
            requirementRepository.deleteById(mr.getRequirementId());
            methodRequirementRepository.deleteById(mr.getId());
        }

        for (String parentId : request.getParentRequirementIds()) {
            Requirement parent = requirementRepository.findById(parentId).orElse(null);
            if (parent != null) {
                String subRequirementName = "[" + request.getName() + "] 方法描述";
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

                MethodRequirement mr = MethodRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .methodId(method.getId())
                        .requirementId(subRequirement.getId())
                        .build();
                methodRequirementRepository.save(mr);
            }
        }

        method.setName(request.getName());
        method.setCode(request.getCode());
        method.setDescription(request.getDescription());

        method = methodRepository.save(method);

        methodParamRepository.deleteByMethodId(methodId);

        saveMethodParams(method.getId(), "INPUT", request.getInputParams());
        saveMethodParams(method.getId(), "OUTPUT", request.getOutputParams());

        log.info("更新方法: id={}, name={}", method.getId(), method.getName());
        return toDTO(method);
    }

    @Transactional
    public void delete(String modelId, String methodId) {
        Method method = methodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("方法不存在: " + methodId));

        if (!method.getModelId().equals(modelId)) {
            throw new RuntimeException("方法不属于该模型");
        }

        List<MethodRequirement> mrs = methodRequirementRepository.findByMethodId(methodId);
        for (MethodRequirement mr : mrs) {
            requirementRepository.deleteById(mr.getRequirementId());
            methodRequirementRepository.deleteById(mr.getId());
        }

        methodRepository.deleteById(methodId);
        log.info("删除方法: id={}", methodId);
    }

    private void saveMethodParams(String methodId, String paramType, List<String> propertyIds) {
        if (propertyIds != null && !propertyIds.isEmpty()) {
            int order = 0;
            for (String propertyId : propertyIds) {
                if (!propertyRepository.existsById(propertyId)) {
                    throw new RuntimeException("属性不存在: " + propertyId);
                }
                MethodParam param = MethodParam.builder()
                        .id(UUID.randomUUID().toString())
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
        List<MethodRequirement> mrs = methodRequirementRepository.findByMethodId(entity.getId());
        
        List<String> requirementIds = new ArrayList<>();
        List<String> requirementNames = new ArrayList<>();
        List<String> parentRequirementIds = new ArrayList<>();
        List<String> parentRequirementNames = new ArrayList<>();

        for (MethodRequirement mr : mrs) {
            Requirement subReq = requirementRepository.findById(mr.getRequirementId()).orElse(null);
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
                .modelName(modelName)
                .requirementIds(requirementIds)
                .requirementNames(requirementNames)
                .parentRequirementIds(parentRequirementIds)
                .parentRequirementNames(parentRequirementNames)
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
                .dataType(entity.getDataType())
                .description(entity.getDescription())
                .build();
    }

    private String generateCode(String modelId) {
        long count = methodRepository.findByModelId(modelId).size();
        return String.format("METH-%04d", count + 1);
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