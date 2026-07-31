package com.zmodel.service;

import com.zmodel.dto.request.FunctionalOrchestrationCreateRequest;
import com.zmodel.dto.request.FunctionalOrchestrationUpdateRequest;
import com.zmodel.dto.response.FunctionalOrchestrationDTO;
import com.zmodel.entity.FunctionalOrchestration;
import com.zmodel.entity.FoNode;
import com.zmodel.entity.FoNodeMethod;
import com.zmodel.entity.FoNodeConfig;
import com.zmodel.repository.FunctionalOrchestrationRepository;
import com.zmodel.repository.FoNodeConfigRepository;
import com.zmodel.repository.FoNodeMethodRepository;
import com.zmodel.repository.FoNodeRepository;
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
public class FunctionalOrchestrationService {

    private final FunctionalOrchestrationRepository functionalOrchestrationRepository;
    private final FoNodeRepository foNodeRepository;
    private final FoNodeMethodRepository foNodeMethodRepository;
    private final FoNodeConfigRepository foNodeConfigRepository;

    public Page<FunctionalOrchestrationDTO> list(String keyword, Pageable pageable) {
        Page<FunctionalOrchestration> page;
        if (keyword != null && !keyword.isBlank()) {
            page = functionalOrchestrationRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = functionalOrchestrationRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public FunctionalOrchestrationDTO getById(String id) {
        FunctionalOrchestration fo = functionalOrchestrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("功能编排不存在"));
        return toDTO(fo);
    }

    public FunctionalOrchestrationDTO getByOrchestrationId(String orchestrationId) {
        FunctionalOrchestration fo = functionalOrchestrationRepository.findByOrchestrationId(orchestrationId)
                .orElseThrow(() -> new RuntimeException("功能编排不存在"));
        return toDTO(fo);
    }

    @Transactional
    public FunctionalOrchestrationDTO create(FunctionalOrchestrationCreateRequest request) {
        // 如果关联了业务编排，确保同一业务编排只能创建一个功能编排
        if (request.getOrchestrationId() != null && !request.getOrchestrationId().isBlank()) {
            if (functionalOrchestrationRepository.existsByOrchestrationId(request.getOrchestrationId())) {
                throw new RuntimeException("该业务编排已关联功能编排");
            }
        }

        FunctionalOrchestration fo = FunctionalOrchestration.builder()
                .id(UUID.randomUUID().toString())
                .orchestrationId(request.getOrchestrationId())
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .status("DRAFT")
                .build();
        FunctionalOrchestration saved = functionalOrchestrationRepository.save(fo);
        return toDTO(saved);
    }

    @Transactional
    public FunctionalOrchestrationDTO update(String id, FunctionalOrchestrationUpdateRequest request) {
        FunctionalOrchestration fo = functionalOrchestrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("功能编排不存在"));
        
        if (request.getName() != null) fo.setName(request.getName());
        if (request.getCode() != null) fo.setCode(request.getCode());
        if (request.getDescription() != null) fo.setDescription(request.getDescription());
        if (request.getStatus() != null) fo.setStatus(request.getStatus());
        
        FunctionalOrchestration saved = functionalOrchestrationRepository.save(fo);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        // 删除关联的节点配置和方法
        List<FoNode> nodes = foNodeRepository.findByOrchestrationIdOrderBySortOrderAsc(id);
        for (FoNode node : nodes) {
            foNodeMethodRepository.deleteByNodeId(node.getId());
            foNodeConfigRepository.deleteByNodeId(node.getId());
        }
        foNodeRepository.deleteByOrchestrationId(id);
        functionalOrchestrationRepository.deleteById(id);
    }

    @Transactional
    public FunctionalOrchestrationDTO saveNodes(String id, List<FunctionalOrchestrationDTO.FoNodeDTO> nodes) {
        FunctionalOrchestration fo = functionalOrchestrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("功能编排不存在"));

        // 删除现有节点
        foNodeRepository.deleteByOrchestrationId(id);

        // 保存新节点
        int sortOrder = 0;
        for (FunctionalOrchestrationDTO.FoNodeDTO nodeDTO : nodes) {
            FoNode node = FoNode.builder()
                    .id(nodeDTO.getId() != null ? nodeDTO.getId() : UUID.randomUUID().toString())
                    .orchestrationId(id)
                    .nodeType(nodeDTO.getNodeType())
                    .nodeName(nodeDTO.getNodeName() != null ? nodeDTO.getNodeName() : nodeDTO.getLabel())
                    .sortOrder(sortOrder++)
                    .x(nodeDTO.getX())
                    .y(nodeDTO.getY())
                    .physicalModelId(nodeDTO.getPhysicalModelId())
                    .build();
            FoNode savedNode = foNodeRepository.save(node);

            // 保存节点方法关联
            if (nodeDTO.getMethods() != null) {
                for (FunctionalOrchestrationDTO.FoNodeMethodDTO methodDTO : nodeDTO.getMethods()) {
                    FoNodeMethod nodeMethod = FoNodeMethod.builder()
                            .id(UUID.randomUUID().toString())
                            .nodeId(savedNode.getId())
                            .methodId(methodDTO.getMethodId())
                            .physicalMethodId(methodDTO.getPhysicalMethodId())
                            .sortOrder(methodDTO.getSortOrder())
                            .build();
                    foNodeMethodRepository.save(nodeMethod);
                }
            }

            // 保存节点配置
            if (nodeDTO.getNodeConfig() != null) {
                FoNodeConfig config = FoNodeConfig.builder()
                        .id(UUID.randomUUID().toString())
                        .nodeId(savedNode.getId())
                        .configKey(nodeDTO.getNodeConfig().getConfigKey())
                        .configValue(nodeDTO.getNodeConfig().getConfigValue())
                        .build();
                foNodeConfigRepository.save(config);
            }
        }

        return getById(id);
    }

    private FunctionalOrchestrationDTO toDTO(FunctionalOrchestration entity) {
        FunctionalOrchestrationDTO dto = FunctionalOrchestrationDTO.fromEntity(entity);
        
        // 加载节点列表
        List<FoNode> nodes = foNodeRepository.findByOrchestrationIdOrderBySortOrderAsc(entity.getId());
        dto.setNodes(nodes.stream()
                .map(this::toNodeDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    private FunctionalOrchestrationDTO.FoNodeDTO toNodeDTO(FoNode node) {
        FunctionalOrchestrationDTO.FoNodeDTO dto = new FunctionalOrchestrationDTO.FoNodeDTO();
        dto.setId(node.getId());
        dto.setOrchestrationId(node.getOrchestrationId());
        dto.setParentId(node.getParentId());
        dto.setNodeType(node.getNodeType());
        dto.setNodeName(node.getNodeName());
        dto.setLabel(node.getNodeName());
        dto.setDescription(node.getDescription());
        dto.setSortOrder(node.getSortOrder());
        dto.setLoopCount(node.getLoopCount());
        dto.setConditionExpression(node.getConditionExpression());
        dto.setDbOperation(node.getDbOperation());
        dto.setTargetTable(node.getTargetTable());
        dto.setApiUrl(node.getApiUrl());
        dto.setApiMethod(node.getApiMethod());
        dto.setTransformType(node.getTransformType());
        dto.setWidth(node.getWidth());
        dto.setX(node.getX());
        dto.setY(node.getY());
        dto.setPhysicalModelId(node.getPhysicalModelId());
        
        // 加载节点方法
        List<FoNodeMethod> methods = foNodeMethodRepository.findByNodeIdOrderBySortOrderAsc(node.getId());
        dto.setMethods(methods.stream().map(m -> {
            FunctionalOrchestrationDTO.FoNodeMethodDTO methodDTO = new FunctionalOrchestrationDTO.FoNodeMethodDTO();
            methodDTO.setId(m.getId());
            methodDTO.setNodeId(m.getNodeId());
            methodDTO.setMethodId(m.getMethodId());
            methodDTO.setPhysicalMethodId(m.getPhysicalMethodId());
            methodDTO.setSortOrder(m.getSortOrder());
            return methodDTO;
        }).collect(Collectors.toList()));
        
        // 加载节点配置
        foNodeConfigRepository.findFirstByNodeId(node.getId()).ifPresent(config -> {
            FunctionalOrchestrationDTO.FoNodeConfigDTO configDTO = new FunctionalOrchestrationDTO.FoNodeConfigDTO();
            configDTO.setId(config.getId());
            configDTO.setNodeId(config.getNodeId());
            configDTO.setConfigKey(config.getConfigKey());
            configDTO.setConfigValue(config.getConfigValue());
            dto.setNodeConfig(configDTO);
        });
        
        return dto;
    }
}
