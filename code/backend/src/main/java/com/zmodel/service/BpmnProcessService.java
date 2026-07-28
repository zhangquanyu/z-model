package com.zmodel.service;

import com.zmodel.dto.request.BpmnProcessCreateRequest;
import com.zmodel.dto.request.BpmnProcessUpdateRequest;
import com.zmodel.dto.request.NodeModelBindRequest;
import com.zmodel.dto.response.BpmnProcessDTO;
import com.zmodel.dto.response.BpmnProcessVersionDTO;
import com.zmodel.dto.response.NodeModelBindingDTO;
import com.zmodel.entity.BpmnProcess;
import com.zmodel.entity.BpmnProcessVersion;
import com.zmodel.entity.Model;
import com.zmodel.entity.ProcessNodeModel;
import com.zmodel.repository.BpmnProcessRepository;
import com.zmodel.repository.BpmnProcessVersionRepository;
import com.zmodel.repository.ModelRepository;
import com.zmodel.repository.ProcessNodeModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BpmnProcessService {

    private final BpmnProcessRepository bpmnProcessRepository;
    private final BpmnProcessVersionRepository bpmnProcessVersionRepository;
    private final ProcessNodeModelRepository processNodeModelRepository;
    private final ModelRepository modelRepository;

    private static final String DEFAULT_BPMN_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="Process_1" isExecutable="false">
                <bpmn:startEvent id="StartEvent_1">
                  <bpmn:outgoing>Flow_1</bpmn:outgoing>
                </bpmn:startEvent>
                <bpmn:task id="Activity_1" name="任务">
                  <bpmn:incoming>Flow_1</bpmn:incoming>
                  <bpmn:outgoing>Flow_2</bpmn:outgoing>
                </bpmn:task>
                <bpmn:endEvent id="Event_1">
                  <bpmn:incoming>Flow_2</bpmn:incoming>
                </bpmn:endEvent>
                <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Activity_1" />
                <bpmn:sequenceFlow id="Flow_2" sourceRef="Activity_1" targetRef="Event_1" />
              </bpmn:process>
              <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
                  <bpmndi:BPMNShape id="BPMNShape_1" bpmnElement="StartEvent_1">
                    <dc:Bounds x="180" y="160" width="36" height="36" />
                  </bpmndi:BPMNShape>
                  <bpmndi:BPMNShape id="BPMNShape_2" bpmnElement="Activity_1">
                    <dc:Bounds x="270" y="148" width="100" height="60" />
                  </bpmndi:BPMNShape>
                  <bpmndi:BPMNShape id="BPMNShape_3" bpmnElement="Event_1">
                    <dc:Bounds x="422" y="160" width="36" height="36" />
                  </bpmndi:BPMNShape>
                  <bpmndi:BPMNEdge id="BPMNEdge_1" bpmnElement="Flow_1">
                    <di:waypoint x="216" y="178" />
                    <di:waypoint x="270" y="178" />
                  </bpmndi:BPMNEdge>
                  <bpmndi:BPMNEdge id="BPMNEdge_2" bpmnElement="Flow_2">
                    <di:waypoint x="370" y="178" />
                    <di:waypoint x="422" y="178" />
                  </bpmndi:BPMNEdge>
                </bpmndi:BPMNPlane>
              </bpmndi:BPMNDiagram>
            </bpmn:definitions>
            """;

    @Transactional
    public BpmnProcessDTO create(BpmnProcessCreateRequest request) {
        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode();
        } else if (bpmnProcessRepository.existsByCode(code)) {
            throw new RuntimeException("流程编码已存在: " + code);
        }

        BpmnProcess process = BpmnProcess.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .bpmnXml(request.getBpmnXml() != null ? request.getBpmnXml() : DEFAULT_BPMN_XML)
                .version(1)
                .status("DRAFT")
                .build();

        process = bpmnProcessRepository.save(process);

        saveVersion(process, process.getBpmnXml(), "初始化版本");

        log.info("创建流程: id={}, name={}, code={}", process.getId(), process.getName(), process.getCode());
        return toDTO(process);
    }

    @Transactional(readOnly = true)
    public BpmnProcessDTO getById(String id) {
        BpmnProcess process = bpmnProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("流程不存在: " + id));
        return toDTO(process);
    }

    @Transactional(readOnly = true)
    public Page<BpmnProcessDTO> list(String keyword, Pageable pageable) {
        Page<BpmnProcess> page;
        if (keyword == null || keyword.isEmpty()) {
            page = bpmnProcessRepository.findAll(pageable);
        } else {
            page = bpmnProcessRepository.findByNameContaining(keyword, pageable);
        }
        return page.map(this::toDTO);
    }

    @Transactional
    public BpmnProcessDTO update(String id, BpmnProcessUpdateRequest request) {
        BpmnProcess process = bpmnProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("流程不存在: " + id));

        if (request.getName() != null) {
            process.setName(request.getName());
        }
        if (request.getDescription() != null) {
            process.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            process.setStatus(request.getStatus());
        }

        boolean xmlChanged = request.getBpmnXml() != null && !request.getBpmnXml().equals(process.getBpmnXml());
        if (xmlChanged) {
            process.setBpmnXml(request.getBpmnXml());
            process.setVersion(process.getVersion() + 1);
            saveVersion(process, request.getBpmnXml(), request.getChangeNote());
        }

        process = bpmnProcessRepository.save(process);

        log.info("更新流程: id={}, name={}, version={}", process.getId(), process.getName(), process.getVersion());
        return toDTO(process);
    }

    @Transactional
    public void delete(String id) {
        if (!bpmnProcessRepository.existsById(id)) {
            throw new RuntimeException("流程不存在: " + id);
        }
        bpmnProcessVersionRepository.findByProcessIdOrderByVersionDesc(id)
                .forEach(v -> bpmnProcessVersionRepository.deleteById(v.getId()));
        processNodeModelRepository.deleteByProcessId(id);
        bpmnProcessRepository.deleteById(id);
        log.info("删除流程: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<BpmnProcessVersionDTO> getVersions(String processId) {
        BpmnProcess process = bpmnProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("流程不存在: " + processId));
        return bpmnProcessVersionRepository.findByProcessIdOrderByVersionDesc(processId)
                .stream()
                .map(this::toVersionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BpmnProcessDTO rollbackVersion(String processId, Integer targetVersion) {
        BpmnProcess process = bpmnProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("流程不存在: " + processId));

        BpmnProcessVersion version = bpmnProcessVersionRepository
                .findByProcessIdAndVersion(processId, targetVersion)
                .orElseThrow(() -> new RuntimeException("版本不存在: " + targetVersion));

        process.setBpmnXml(version.getBpmnXml());
        process.setVersion(process.getVersion() + 1);
        process = bpmnProcessRepository.save(process);

        saveVersion(process, version.getBpmnXml(), "回滚到版本 " + targetVersion);

        log.info("回滚流程版本: processId={}, targetVersion={}", processId, targetVersion);
        return toDTO(process);
    }

    @Transactional(readOnly = true)
    public List<NodeModelBindingDTO> getNodeBindings(String processId) {
        return processNodeModelRepository.findByProcessId(processId)
                .stream()
                .map(this::toBindingDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NodeModelBindingDTO bindNodeModel(String processId, NodeModelBindRequest request) {
        BpmnProcess process = bpmnProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("流程不存在: " + processId));

        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("模型不存在: " + request.getModelId()));

        List<ProcessNodeModel> existing = processNodeModelRepository
                .findByProcessIdAndNodeId(processId, request.getNodeId());
        if (!existing.isEmpty()) {
            throw new RuntimeException("节点已绑定模型: " + request.getNodeId());
        }

        ProcessNodeModel binding = ProcessNodeModel.builder()
                .id(UUID.randomUUID().toString())
                .processId(processId)
                .nodeId(request.getNodeId())
                .modelId(request.getModelId())
                .build();

        binding = processNodeModelRepository.save(binding);

        log.info("绑定节点模型: processId={}, nodeId={}, modelId={}", processId, request.getNodeId(), request.getModelId());
        return toBindingDTO(binding);
    }

    @Transactional
    public void unbindNodeModel(String processId, String nodeId) {
        processNodeModelRepository.deleteByProcessIdAndNodeId(processId, nodeId);
        log.info("解绑节点模型: processId={}, nodeId={}", processId, nodeId);
    }

    @Transactional(readOnly = true)
    public List<NodeModelBindingDTO> getModelBindingsByNode(String processId, String nodeId) {
        return processNodeModelRepository.findByProcessIdAndNodeId(processId, nodeId)
                .stream()
                .map(this::toBindingDTO)
                .collect(Collectors.toList());
    }

    private void saveVersion(BpmnProcess process, String bpmnXml, String changeNote) {
        BpmnProcessVersion version = BpmnProcessVersion.builder()
                .id(UUID.randomUUID().toString())
                .processId(process.getId())
                .version(process.getVersion())
                .bpmnXml(bpmnXml)
                .changeNote(changeNote)
                .build();
        bpmnProcessVersionRepository.save(version);
    }

    private String generateCode() {
        long count = bpmnProcessRepository.count();
        return String.format("PROC-%04d", count + 1);
    }

    private BpmnProcessDTO toDTO(BpmnProcess entity) {
        List<BpmnProcessVersionDTO> versions = bpmnProcessVersionRepository
                .findByProcessIdOrderByVersionDesc(entity.getId())
                .stream()
                .map(this::toVersionDTO)
                .collect(Collectors.toList());

        List<NodeModelBindingDTO> bindings = processNodeModelRepository.findByProcessId(entity.getId())
                .stream()
                .map(this::toBindingDTO)
                .collect(Collectors.toList());

        return BpmnProcessDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .bpmnXml(entity.getBpmnXml())
                .version(entity.getVersion())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .versions(versions)
                .nodeBindings(bindings)
                .build();
    }

    private BpmnProcessVersionDTO toVersionDTO(BpmnProcessVersion entity) {
        return BpmnProcessVersionDTO.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .version(entity.getVersion())
                .bpmnXml(entity.getBpmnXml())
                .changeNote(entity.getChangeNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private NodeModelBindingDTO toBindingDTO(ProcessNodeModel entity) {
        String[] modelName = {null};
        String[] modelCode = {null};
        modelRepository.findById(entity.getModelId()).ifPresent(m -> {
            modelName[0] = m.getName();
            modelCode[0] = m.getCode();
        });

        return NodeModelBindingDTO.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .nodeId(entity.getNodeId())
                .modelId(entity.getModelId())
                .modelName(modelName[0])
                .modelCode(modelCode[0])
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
