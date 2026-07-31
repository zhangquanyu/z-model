package com.zmodel.service;

import com.zmodel.dto.request.PhysicalMethodCreateRequest;
import com.zmodel.dto.response.PhysicalMethodDTO;
import com.zmodel.entity.Method;
import com.zmodel.entity.PhysicalMethod;
import com.zmodel.entity.PhysicalMethodParam;
import com.zmodel.repository.MethodRepository;
import com.zmodel.repository.PhysicalMethodParamRepository;
import com.zmodel.repository.PhysicalMethodRepository;
import com.zmodel.repository.PhysicalModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhysicalMethodService {

    private final PhysicalMethodRepository physicalMethodRepository;
    private final PhysicalMethodParamRepository physicalMethodParamRepository;
    private final PhysicalModelRepository physicalModelRepository;
    private final MethodRepository methodRepository;

    public List<PhysicalMethodDTO> listByPhysicalModelId(String physicalModelId) {
        return physicalMethodRepository.findByPhysicalModelId(physicalModelId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PhysicalMethodDTO create(PhysicalMethodCreateRequest request) {
        // 验证物理模型存在
        physicalModelRepository.findById(request.getPhysicalModelId())
                .orElseThrow(() -> new RuntimeException("物理模型不存在"));
        
        // 验证源方法存在
        Method sourceMethod = methodRepository.findById(request.getSourceMethodId())
                .orElseThrow(() -> new RuntimeException("源方法不存在"));

        PhysicalMethod method = PhysicalMethod.builder()
                .id(UUID.randomUUID().toString())
                .physicalModelId(request.getPhysicalModelId())
                .sourceMethodId(request.getSourceMethodId())
                .name(request.getName() != null ? request.getName() : sourceMethod.getName())
                .code(request.getCode())
                .methodType(request.getMethodType() != null ? request.getMethodType() : "SELECT")
                .description(request.getDescription())
                .sqlTemplate(request.getSqlTemplate())
                .build();

        // 如果没有提供SQL模板，自动生成基础模板
        if (method.getSqlTemplate() == null || method.getSqlTemplate().isEmpty()) {
            method.setSqlTemplate(generateDefaultSQLTemplate(method.getMethodType(), request.getPhysicalModelId()));
        }

        PhysicalMethod saved = physicalMethodRepository.save(method);
        return toDTO(saved);
    }

    @Transactional
    public PhysicalMethodDTO update(String id, PhysicalMethodCreateRequest request) {
        PhysicalMethod method = physicalMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物理方法不存在"));

        if (request.getName() != null) method.setName(request.getName());
        if (request.getCode() != null) method.setCode(request.getCode());
        if (request.getMethodType() != null) method.setMethodType(request.getMethodType());
        if (request.getDescription() != null) method.setDescription(request.getDescription());
        if (request.getSqlTemplate() != null) method.setSqlTemplate(request.getSqlTemplate());

        PhysicalMethod saved = physicalMethodRepository.save(method);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        physicalMethodParamRepository.deleteByPhysicalMethodId(id);
        physicalMethodRepository.deleteById(id);
    }

    private PhysicalMethodDTO toDTO(PhysicalMethod entity) {
        PhysicalMethodDTO dto = PhysicalMethodDTO.fromEntity(entity);
        
        // 设置源方法名称
        methodRepository.findById(entity.getSourceMethodId())
                .ifPresent(m -> dto.setSourceMethodName(m.getName()));
        
        // 获取参数列表
        List<PhysicalMethodParam> params = physicalMethodParamRepository.findByPhysicalMethodId(entity.getId());
        dto.setParams(params.stream()
                .map(param -> PhysicalMethodDTO.PhysicalMethodParamDTO.builder()
                        .id(param.getId())
                        .physicalMethodId(param.getPhysicalMethodId())
                        .physicalPropertyId(param.getPhysicalPropertyId())
                        .paramType(param.getParamType())
                        .sortOrder(param.getSortOrder())
                        .build())
                .collect(Collectors.toList()));
        
        return dto;
    }

    private String generateDefaultSQLTemplate(String methodType, String physicalModelId) {
        switch (methodType != null ? methodType : "SELECT") {
            case "INSERT":
                return "INSERT INTO table_name (columns) VALUES (values)";
            case "UPDATE":
                return "UPDATE table_name SET column = value WHERE condition";
            case "DELETE":
                return "DELETE FROM table_name WHERE condition";
            case "SELECT":
                return "SELECT columns FROM table_name WHERE condition";
            default:
                return "CUSTOM SQL";
        }
    }
}
