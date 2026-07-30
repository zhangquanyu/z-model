package com.zmodel.service;

import com.zmodel.dto.request.OrchestrationCreateRequest;
import com.zmodel.dto.request.OrchestrationDesignSaveRequest;
import com.zmodel.dto.request.OrchestrationNodeMethodRequest;
import com.zmodel.dto.request.OrchestrationNodeRequest;
import com.zmodel.dto.request.OrchestrationUpdateRequest;
import com.zmodel.dto.response.*;
import com.zmodel.entity.*;
import com.zmodel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final BusinessOrchestrationRepository orchestrationRepository;
    private final OrchestrationNodeRepository nodeRepository;
    private final OrchestrationNodeMethodRepository nodeMethodRepository;
    private final OrchestrationRequirementRepository orchestrationRequirementRepository;
    private final MethodRepository methodRepository;
    private final ModelRepository modelRepository;
    private final RequirementRepository requirementRepository;
    private final MethodRequirementRepository methodRequirementRepository;

    @Transactional
    public OrchestrationDTO create(OrchestrationCreateRequest request) {
        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode();
        } else if (orchestrationRepository.existsByCode(code)) {
            throw new RuntimeException("编排编码已存在: " + code);
        }

        BusinessOrchestration orchestration = BusinessOrchestration.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .status("DRAFT")
                .version(1)
                .build();

        orchestration = orchestrationRepository.save(orchestration);
        log.info("创建编排: id={}, name={}, code={}", orchestration.getId(), orchestration.getName(), orchestration.getCode());
        return toDTO(orchestration);
    }

    @Transactional(readOnly = true)
    public OrchestrationDTO getById(String id) {
        BusinessOrchestration orchestration = orchestrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("编排不存在: " + id));
        return toDTO(orchestration);
    }

    @Transactional(readOnly = true)
    public Page<OrchestrationSummaryDTO> list(String keyword, Pageable pageable) {
        Page<BusinessOrchestration> page;
        if (keyword == null || keyword.isEmpty()) {
            page = orchestrationRepository.findAll(pageable);
        } else {
            page = orchestrationRepository.findByNameContaining(keyword, pageable);
        }
        return page.map(this::toSummaryDTO);
    }

    @Transactional
    public OrchestrationDTO update(String id, OrchestrationUpdateRequest request) {
        BusinessOrchestration orchestration = orchestrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("编排不存在: " + id));

        if (request.getName() != null) {
            orchestration.setName(request.getName());
        }
        if (request.getDescription() != null) {
            orchestration.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            orchestration.setStatus(request.getStatus());
        }

        orchestration = orchestrationRepository.save(orchestration);
        log.info("更新编排: id={}, name={}", orchestration.getId(), orchestration.getName());
        return toDTO(orchestration);
    }

    @Transactional
    public void delete(String id) {
        if (!orchestrationRepository.existsById(id)) {
            throw new RuntimeException("编排不存在: " + id);
        }

        List<OrchestrationNode> nodes = nodeRepository.findByOrchestrationIdOrderBySortOrder(id);
        for (OrchestrationNode node : nodes) {
            nodeMethodRepository.deleteByNodeId(node.getId());
        }
        nodeRepository.deleteByOrchestrationId(id);
        orchestrationRequirementRepository.deleteByOrchestrationId(id);
        orchestrationRepository.deleteById(id);

        log.info("删除编排: id={}", id);
    }

    @Transactional
    public OrchestrationNodeDTO addNode(String orchestrationId, OrchestrationNodeRequest request) {
        BusinessOrchestration orchestration = orchestrationRepository.findById(orchestrationId)
                .orElseThrow(() -> new RuntimeException("编排不存在: " + orchestrationId));

        int maxOrder = (int) nodeRepository.countByOrchestrationId(orchestrationId);

        OrchestrationNode node = OrchestrationNode.builder()
                .id(UUID.randomUUID().toString())
                .orchestrationId(orchestrationId)
                .nodeType(request.getNodeType() != null ? request.getNodeType() : "SERIAL")
                .nodeName(request.getNodeName() != null ? request.getNodeName() : "节点-" + (maxOrder + 1))
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : maxOrder)
                .loopCount(request.getLoopCount())
                .build();

        node = nodeRepository.save(node);
        log.info("添加编排节点: orchestrationId={}, nodeId={}, type={}", orchestrationId, node.getId(), node.getNodeType());
        return toNodeDTO(node);
    }

    @Transactional
    public OrchestrationNodeDTO updateNode(String orchestrationId, String nodeId, OrchestrationNodeRequest request) {
        OrchestrationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));

        if (!node.getOrchestrationId().equals(orchestrationId)) {
            throw new RuntimeException("节点不属于该编排");
        }

        if (request.getNodeName() != null) {
            node.setNodeName(request.getNodeName());
        }
        if (request.getDescription() != null) {
            node.setDescription(request.getDescription());
        }
        if (request.getNodeType() != null) {
            node.setNodeType(request.getNodeType());
        }
        if (request.getSortOrder() != null) {
            node.setSortOrder(request.getSortOrder());
        }
        if (request.getLoopCount() != null) {
            node.setLoopCount(request.getLoopCount());
        }

        node = nodeRepository.save(node);
        log.info("更新编排节点: nodeId={}", nodeId);
        return toNodeDTO(node);
    }

    @Transactional
    public void deleteNode(String orchestrationId, String nodeId) {
        OrchestrationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));

        if (!node.getOrchestrationId().equals(orchestrationId)) {
            throw new RuntimeException("节点不属于该编排");
        }

        nodeMethodRepository.deleteByNodeId(nodeId);
        nodeRepository.deleteById(nodeId);
        log.info("删除编排节点: nodeId={}", nodeId);
    }

    @Transactional
    public OrchestrationNodeMethodDTO addNodeMethod(String orchestrationId, String nodeId, OrchestrationNodeMethodRequest request) {
        BusinessOrchestration orchestration = orchestrationRepository.findById(orchestrationId)
                .orElseThrow(() -> new RuntimeException("编排不存在: " + orchestrationId));

        OrchestrationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));

        if (!node.getOrchestrationId().equals(orchestrationId)) {
            throw new RuntimeException("节点不属于该编排");
        }

        Method method = methodRepository.findById(request.getMethodId())
                .orElseThrow(() -> new RuntimeException("方法不存在: " + request.getMethodId()));

        if (!modelRepository.existsById(method.getModelId())) {
            throw new RuntimeException("方法所属模型不存在");
        }

        String requirementId = request.getRequirementId();

        if (request.getNewRequirementName() != null && !request.getNewRequirementName().isEmpty()) {
            if (request.getParentRequirementId() == null) {
                throw new RuntimeException("创建子需求时必须指定父需求");
            }

            Requirement parentRequirement = requirementRepository.findById(request.getParentRequirementId())
                    .orElseThrow(() -> new RuntimeException("父需求不存在: " + request.getParentRequirementId()));

            String subCode = generateSubRequirementCode(parentRequirement.getCode());

            Requirement subRequirement = Requirement.builder()
                    .id(UUID.randomUUID().toString())
                    .name(request.getNewRequirementName())
                    .code(subCode)
                    .description(request.getNewRequirementDescription())
                    .status(parentRequirement.getStatus() != null ? parentRequirement.getStatus() : "DRAFT")
                    .priority(parentRequirement.getPriority() != null ? parentRequirement.getPriority() : "MEDIUM")
                    .requirementType("SUB")
                    .parentId(request.getParentRequirementId())
                    .build();

            subRequirement = requirementRepository.save(subRequirement);
            requirementId = subRequirement.getId();

            MethodRequirement mr = MethodRequirement.builder()
                    .id(UUID.randomUUID().toString())
                    .methodId(method.getId())
                    .requirementId(subRequirement.getId())
                    .build();
            methodRequirementRepository.save(mr);

            if (!orchestrationRequirementRepository.existsByOrchestrationIdAndRequirementId(orchestrationId, subRequirement.getId())) {
                OrchestrationRequirement orReq = OrchestrationRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .orchestrationId(orchestrationId)
                        .requirementId(subRequirement.getId())
                        .build();
                orchestrationRequirementRepository.save(orReq);
            }

            log.info("创建子需求并绑定: orchestrationId={}, methodId={}, subReqId={}", orchestrationId, method.getId(), subRequirement.getId());
        } else if (requirementId != null && !requirementId.isEmpty()) {
            final String existingReqId = requirementId;
            Requirement subReq = requirementRepository.findById(existingReqId)
                    .orElseThrow(() -> new RuntimeException("需求不存在: " + existingReqId));

            MethodRequirement mr = MethodRequirement.builder()
                    .id(UUID.randomUUID().toString())
                    .methodId(method.getId())
                    .requirementId(requirementId)
                    .build();
            methodRequirementRepository.save(mr);

            if (!orchestrationRequirementRepository.existsByOrchestrationIdAndRequirementId(orchestrationId, requirementId)) {
                OrchestrationRequirement orReq = OrchestrationRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .orchestrationId(orchestrationId)
                        .requirementId(requirementId)
                        .build();
                orchestrationRequirementRepository.save(orReq);
            }
        }

        int maxOrder = (int) nodeMethodRepository.countByNodeId(nodeId);

        OrchestrationNodeMethod nodeMethod = OrchestrationNodeMethod.builder()
                .id(UUID.randomUUID().toString())
                .nodeId(nodeId)
                .methodId(method.getId())
                .requirementId(requirementId)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : maxOrder)
                .build();

        nodeMethod = nodeMethodRepository.save(nodeMethod);
        log.info("添加节点方法: nodeId={}, methodId={}", nodeId, method.getId());
        return toNodeMethodDTO(nodeMethod);
    }

    @Transactional
    public void removeNodeMethod(String orchestrationId, String nodeId, String methodId) {
        OrchestrationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在: " + nodeId));

        if (!node.getOrchestrationId().equals(orchestrationId)) {
            throw new RuntimeException("节点不属于该编排");
        }

        nodeMethodRepository.deleteByNodeIdAndMethodId(nodeId, methodId);
        log.info("移除节点方法: nodeId={}, methodId={}", nodeId, methodId);
    }

    @Transactional(readOnly = true)
    public List<RequirementDTO> getOrchestrationRequirements(String orchestrationId) {
        List<OrchestrationRequirement> orReqs = orchestrationRequirementRepository.findByOrchestrationId(orchestrationId);

        List<RequirementDTO> result = new ArrayList<>();
        for (OrchestrationRequirement orReq : orReqs) {
            requirementRepository.findById(orReq.getRequirementId()).ifPresent(req -> {
                String parentName = null;
                if (req.getParentId() != null) {
                    parentName = requirementRepository.findById(req.getParentId())
                            .map(Requirement::getName)
                            .orElse(null);
                }
                result.add(RequirementDTO.builder()
                        .id(req.getId())
                        .name(req.getName())
                        .code(req.getCode())
                        .description(req.getDescription())
                        .status(req.getStatus())
                        .priority(req.getPriority())
                        .requirementType(req.getRequirementType())
                        .parentId(req.getParentId())
                        .parentName(parentName)
                        .createdAt(req.getCreatedAt())
                        .updatedAt(req.getUpdatedAt())
                        .build());
            });
        }
        return result;
    }

    @Transactional
    public void updateNodeSort(String orchestrationId, List<String> nodeIds) {
        final int[] order = {0};
        for (String nodeId : nodeIds) {
            nodeRepository.findById(nodeId).ifPresent(node -> {
                if (node.getOrchestrationId().equals(orchestrationId)) {
                    node.setSortOrder(order[0]++);
                    nodeRepository.save(node);
                }
            });
        }
        log.info("更新节点排序: orchestrationId={}", orchestrationId);
    }

    /**
     * 批量保存编排设计（一次性提交整个设计结构）
     */
    @Transactional
    public OrchestrationDTO saveDesign(String orchestrationId, OrchestrationDesignSaveRequest request) {
        BusinessOrchestration orchestration = orchestrationRepository.findById(orchestrationId)
                .orElseThrow(() -> new IllegalArgumentException("编排不存在"));

        if (request.getName() != null) orchestration.setName(request.getName());
        if (request.getCode() != null) orchestration.setCode(request.getCode());
        if (request.getDescription() != null) orchestration.setDescription(request.getDescription());
        if (request.getStatus() != null) orchestration.setStatus(request.getStatus());
        orchestrationRepository.save(orchestration);

        // 删除旧的节点和方法绑定
        List<OrchestrationNode> oldNodes = nodeRepository.findByOrchestrationIdOrderBySortOrder(orchestrationId);
        for (OrchestrationNode oldNode : oldNodes) {
            nodeMethodRepository.deleteByNodeId(oldNode.getId());
        }
        nodeRepository.deleteByOrchestrationId(orchestrationId);

        // 收集所有需要关联的需求ID，用于创建 OrchestrationRequirement
        List<String> requirementIdsToLink = new ArrayList<>();

        // 创建新的节点和方法绑定
        if (request.getNodes() != null) {
            int orderCounter = 0;
            for (OrchestrationDesignSaveRequest.NodeDesignItem nodeItem : request.getNodes()) {
                OrchestrationNode node = OrchestrationNode.builder()
                        .id(UUID.randomUUID().toString())
                        .orchestrationId(orchestrationId)
                        .nodeName(nodeItem.getNodeName())
                        .description(nodeItem.getDescription())
                        .nodeType(nodeItem.getNodeType())
                        .sortOrder(nodeItem.getSortOrder() != null ? nodeItem.getSortOrder() : orderCounter++)
                        .loopCount(nodeItem.getLoopCount())
                        .build();
                node = nodeRepository.save(node);

                if (nodeItem.getMethods() != null) {
                    int methodOrder = 0;
                    for (OrchestrationDesignSaveRequest.MethodDesignItem methodItem : nodeItem.getMethods()) {
                        Method method = methodRepository.findById(methodItem.getMethodId())
                                .orElseThrow(() -> new IllegalArgumentException("方法不存在"));

                        String actualRequirementId = null;

                        if (methodItem.getSubRequirementId() != null && !methodItem.getSubRequirementId().isEmpty()) {
                            // 使用已存在的子需求ID
                            actualRequirementId = methodItem.getSubRequirementId();
                        } else if (methodItem.getParentRequirementId() != null && !methodItem.getParentRequirementId().isEmpty()
                                && methodItem.getSubRequirementName() != null && !methodItem.getSubRequirementName().isEmpty()) {
                            // 创建新的子需求
                            Requirement parentRequirement = requirementRepository.findById(methodItem.getParentRequirementId())
                                    .orElseThrow(() -> new IllegalArgumentException("父需求不存在: " + methodItem.getParentRequirementId()));

                            String subCode = generateSubRequirementCode(parentRequirement.getCode());

                            Requirement subRequirement = Requirement.builder()
                                    .id(UUID.randomUUID().toString())
                                    .name(methodItem.getSubRequirementName())
                                    .code(subCode)
                                    .description(methodItem.getSubRequirementDescription())
                                    .status(parentRequirement.getStatus() != null ? parentRequirement.getStatus() : "DRAFT")
                                    .priority(parentRequirement.getPriority() != null ? parentRequirement.getPriority() : "MEDIUM")
                                    .requirementType("SUB")
                                    .parentId(methodItem.getParentRequirementId())
                                    .build();
                            subRequirement = requirementRepository.save(subRequirement);
                            actualRequirementId = subRequirement.getId();

                            // 绑定方法到子需求
                            MethodRequirement mr = MethodRequirement.builder()
                                    .id(UUID.randomUUID().toString())
                                    .methodId(method.getId())
                                    .requirementId(subRequirement.getId())
                                    .build();
                            methodRequirementRepository.save(mr);

                            if (!requirementIdsToLink.contains(subRequirement.getId())) {
                                requirementIdsToLink.add(subRequirement.getId());
                            }
                            if (!requirementIdsToLink.contains(methodItem.getParentRequirementId())) {
                                requirementIdsToLink.add(methodItem.getParentRequirementId());
                            }
                        } else if (methodItem.getRequirementId() != null && !methodItem.getRequirementId().isEmpty()) {
                            // 使用已存在的需求ID
                            actualRequirementId = methodItem.getRequirementId();

                            // 绑定方法到需求（去重）
                            if (!methodRequirementRepository.existsByMethodIdAndRequirementId(method.getId(), actualRequirementId)) {
                                MethodRequirement mr = MethodRequirement.builder()
                                        .id(UUID.randomUUID().toString())
                                        .methodId(method.getId())
                                        .requirementId(actualRequirementId)
                                        .build();
                                methodRequirementRepository.save(mr);
                            }

                            if (!requirementIdsToLink.contains(actualRequirementId)) {
                                requirementIdsToLink.add(actualRequirementId);
                            }
                        }

                        OrchestrationNodeMethod nodeMethod = OrchestrationNodeMethod.builder()
                                .id(UUID.randomUUID().toString())
                                .nodeId(node.getId())
                                .methodId(method.getId())
                                .requirementId(actualRequirementId)
                                .sortOrder(methodOrder++)
                                .build();
                        nodeMethodRepository.save(nodeMethod);
                    }
                }
            }
        }

        // 创建编排-需求关联
        for (String reqId : requirementIdsToLink) {
            if (!orchestrationRequirementRepository.existsByOrchestrationIdAndRequirementId(orchestrationId, reqId)) {
                OrchestrationRequirement orReq = OrchestrationRequirement.builder()
                        .id(UUID.randomUUID().toString())
                        .orchestrationId(orchestrationId)
                        .requirementId(reqId)
                        .build();
                orchestrationRequirementRepository.save(orReq);
            }
        }

        return toDTO(orchestration);
    }

    // ============ 私有方法 ============

    private String generateCode() {
        long count = orchestrationRepository.count();
        return String.format("ORCH-%04d", count + 1);
    }

    private String generateSubRequirementCode(String parentCode) {
        return parentCode + "-SUB-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private OrchestrationSummaryDTO toSummaryDTO(BusinessOrchestration entity) {
        int nodeCount = (int) nodeRepository.countByOrchestrationId(entity.getId());
        List<OrchestrationNode> nodes = nodeRepository.findByOrchestrationIdOrderBySortOrder(entity.getId());
        int methodCount = 0;
        for (OrchestrationNode node : nodes) {
            methodCount += (int) nodeMethodRepository.countByNodeId(node.getId());
        }

        return OrchestrationSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .nodeCount(nodeCount)
                .methodCount(methodCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OrchestrationDTO toDTO(BusinessOrchestration entity) {
        List<OrchestrationNode> nodes = nodeRepository.findByOrchestrationIdOrderBySortOrder(entity.getId());
        List<OrchestrationNodeDTO> nodeDTOs = nodes.stream()
                .map(this::toNodeDTO)
                .collect(Collectors.toList());

        List<OrchestrationRequirement> orReqs = orchestrationRequirementRepository.findByOrchestrationId(entity.getId());
        List<RequirementDTO> reqDTOs = new ArrayList<>();
        for (OrchestrationRequirement orReq : orReqs) {
            requirementRepository.findById(orReq.getRequirementId()).ifPresent(req -> {
                String parentName = null;
                if (req.getParentId() != null) {
                    parentName = requirementRepository.findById(req.getParentId())
                            .map(Requirement::getName)
                            .orElse(null);
                }
                reqDTOs.add(RequirementDTO.builder()
                        .id(req.getId())
                        .name(req.getName())
                        .code(req.getCode())
                        .description(req.getDescription())
                        .status(req.getStatus())
                        .priority(req.getPriority())
                        .requirementType(req.getRequirementType())
                        .parentId(req.getParentId())
                        .parentName(parentName)
                        .createdAt(req.getCreatedAt())
                        .updatedAt(req.getUpdatedAt())
                        .build());
            });
        }

        return OrchestrationDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .nodes(nodeDTOs)
                .requirements(reqDTOs)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OrchestrationNodeDTO toNodeDTO(OrchestrationNode node) {
        List<OrchestrationNodeMethod> methods = nodeMethodRepository.findByNodeIdOrderBySortOrder(node.getId());
        List<OrchestrationNodeMethodDTO> methodDTOs = methods.stream()
                .map(this::toNodeMethodDTO)
                .collect(Collectors.toList());

        return OrchestrationNodeDTO.builder()
                .id(node.getId())
                .orchestrationId(node.getOrchestrationId())
                .nodeType(node.getNodeType())
                .nodeName(node.getNodeName())
                .description(node.getDescription())
                .sortOrder(node.getSortOrder())
                .loopCount(node.getLoopCount())
                .methods(methodDTOs)
                .createdAt(node.getCreatedAt())
                .build();
    }

    private OrchestrationNodeMethodDTO toNodeMethodDTO(OrchestrationNodeMethod entity) {
        String methodName = null;
        String methodCode = null;
        String modelId = null;
        String modelName = null;

        Optional<Method> methodOpt = methodRepository.findById(entity.getMethodId());
        if (methodOpt.isPresent()) {
            Method method = methodOpt.get();
            methodName = method.getName();
            methodCode = method.getCode();
            modelId = method.getModelId();

            if (method.getModelId() != null) {
                modelName = modelRepository.findById(method.getModelId())
                        .map(Model::getName)
                        .orElse(null);
            }
        }

        String requirementName = null;
        String requirementCode = null;
        if (entity.getRequirementId() != null) {
            Optional<Requirement> reqOpt = requirementRepository.findById(entity.getRequirementId());
            if (reqOpt.isPresent()) {
                requirementName = reqOpt.get().getName();
                requirementCode = reqOpt.get().getCode();
            }
        }

        return OrchestrationNodeMethodDTO.builder()
                .id(entity.getId())
                .nodeId(entity.getNodeId())
                .methodId(entity.getMethodId())
                .methodName(methodName)
                .methodCode(methodCode)
                .modelId(modelId)
                .modelName(modelName)
                .requirementId(entity.getRequirementId())
                .requirementName(requirementName)
                .requirementCode(requirementCode)
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
