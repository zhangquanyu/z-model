package com.zmodel.service;

import com.zmodel.dto.request.PhysicalModelCreateRequest;
import com.zmodel.dto.request.PhysicalModelUpdateRequest;
import com.zmodel.dto.response.PhysicalModelDTO;
import com.zmodel.dto.response.PhysicalPropertyDTO;
import com.zmodel.dto.response.PhysicalMethodDTO;
import com.zmodel.entity.Model;
import com.zmodel.entity.PhysicalModel;
import com.zmodel.entity.PhysicalMethod;
import com.zmodel.entity.PhysicalProperty;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.PhysicalMethodRepository;
import com.zmodel.repository.PhysicalModelRepository;
import com.zmodel.repository.PhysicalPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhysicalModelService {

    private final PhysicalModelRepository physicalModelRepository;
    private final PhysicalPropertyRepository physicalPropertyRepository;
    private final PhysicalMethodRepository physicalMethodRepository;
    private final ModelRepository modelRepository;

    public Page<PhysicalModelDTO> list(String keyword, Pageable pageable) {
        Page<PhysicalModel> page;
        if (keyword != null && !keyword.isBlank()) {
            page = physicalModelRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = physicalModelRepository.findAll(pageable);
        }
        return page.map(PhysicalModelDTO::fromEntity);
    }

    public PhysicalModelDTO getById(String id) {
        PhysicalModel model = physicalModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物理模型不存在"));
        PhysicalModelDTO dto = PhysicalModelDTO.fromEntity(model);
        enrichWithDetails(dto);
        return dto;
    }

    public List<PhysicalModelDTO> getByModelId(String modelId) {
        return physicalModelRepository.findByModelId(modelId).stream()
                .map(PhysicalModelDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PhysicalModelDTO create(PhysicalModelCreateRequest request) {
        PhysicalModel model = PhysicalModel.builder()
                .id(UUID.randomUUID().toString())
                .modelId(request.getModelId())
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .tableName(request.getTableName())
                .status("DRAFT")
                .build();
        PhysicalModel saved = physicalModelRepository.save(model);
        return PhysicalModelDTO.fromEntity(saved);
    }

    @Transactional
    public PhysicalModelDTO update(String id, PhysicalModelUpdateRequest request) {
        PhysicalModel model = physicalModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物理模型不存在"));
        
        if (request.getName() != null) model.setName(request.getName());
        if (request.getCode() != null) model.setCode(request.getCode());
        if (request.getDescription() != null) model.setDescription(request.getDescription());
        if (request.getTableName() != null) model.setTableName(request.getTableName());
        if (request.getStatus() != null) model.setStatus(request.getStatus());
        
        PhysicalModel saved = physicalModelRepository.save(model);
        return PhysicalModelDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(String id) {
        physicalPropertyRepository.deleteByPhysicalModelId(id);
        physicalMethodRepository.deleteByPhysicalModelId(id);
        physicalModelRepository.deleteById(id);
    }

    private void enrichWithDetails(PhysicalModelDTO dto) {
        // 设置业务模型名称
        modelRepository.findById(dto.getModelId()).ifPresent(m -> dto.setModelName(m.getName()));
        
        // 设置扩展属性
        List<PhysicalProperty> properties = physicalPropertyRepository.findByPhysicalModelId(dto.getId());
        dto.setProperties(properties.stream()
                .map(PhysicalPropertyDTO::fromEntity)
                .collect(Collectors.toList()));
        
        // 设置扩展方法
        List<PhysicalMethod> methods = physicalMethodRepository.findByPhysicalModelId(dto.getId());
        dto.setMethods(methods.stream()
                .map(PhysicalMethodDTO::fromEntity)
                .collect(Collectors.toList()));
    }
}
