package com.zmodel.service;

import com.zmodel.dto.request.RequirementCreateRequest;
import com.zmodel.dto.request.RequirementUpdateRequest;
import com.zmodel.dto.response.RequirementDTO;
import com.zmodel.entity.Requirement;
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
public class RequirementService {

    private final RequirementRepository requirementRepository;

    @Transactional
    public RequirementDTO create(RequirementCreateRequest request) {
        String code = request.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode();
        } else if (requirementRepository.existsByCode(code)) {
            throw new RuntimeException("需求编号已存在: " + code);
        }

        Requirement requirement = Requirement.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .requirementType("MAIN")
                .parentId(null)
                .build();

        requirement = requirementRepository.save(requirement);
        log.info("创建主需求: id={}, name={}", requirement.getId(), requirement.getName());
        return toDTO(requirement);
    }

    @Transactional
    public RequirementDTO createSubRequirement(String parentId, String name, String description) {
        Requirement parent = requirementRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("主需求不存在: " + parentId));

        if (!"MAIN".equals(parent.getRequirementType())) {
            throw new RuntimeException("只能在主需求下创建子需求");
        }

        String code = generateSubCode(parent.getCode());

        Requirement subRequirement = Requirement.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .code(code)
                .description(description)
                .status(parent.getStatus())
                .priority(parent.getPriority())
                .requirementType("SUB")
                .parentId(parentId)
                .build();

        subRequirement = requirementRepository.save(subRequirement);
        log.info("创建子需求: id={}, name={}, parentId={}", subRequirement.getId(), subRequirement.getName(), parentId);
        return toDTO(subRequirement);
    }

    @Transactional(readOnly = true)
    public RequirementDTO getById(String id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + id));
        return toDTOWithChildren(requirement);
    }

    @Transactional(readOnly = true)
    public Page<RequirementDTO> list(String keyword, String status, Pageable pageable) {
        Page<Requirement> page = requirementRepository.findByKeywordAndStatus(keyword, status, pageable);
        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RequirementDTO> listMainRequirements(String keyword) {
        List<Requirement> mainRequirements = requirementRepository.findMainRequirements(keyword);
        return mainRequirements.stream()
                .map(this::toDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequirementDTO> listSubRequirements(String parentId) {
        List<Requirement> subRequirements = requirementRepository.findByParentId(parentId);
        return subRequirements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequirementDTO update(String id, RequirementUpdateRequest request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + id));

        if ("SUB".equals(requirement.getRequirementType())) {
            throw new RuntimeException("子需求只能通过关联模型字段或方法来更新");
        }

        requirement.setName(request.getName());
        requirement.setDescription(request.getDescription());
        requirement.setStatus(request.getStatus());
        requirement.setPriority(request.getPriority());

        if (request.getCode() != null && !request.getCode().isEmpty() && !request.getCode().equals(requirement.getCode())) {
            if (requirementRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("需求编号已存在: " + request.getCode());
            }
            requirement.setCode(request.getCode());
        }

        requirement = requirementRepository.save(requirement);
        log.info("更新需求: id={}, name={}", requirement.getId(), requirement.getName());
        return toDTO(requirement);
    }

    @Transactional
    public void delete(String id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + id));

        if ("MAIN".equals(requirement.getRequirementType())) {
            List<Requirement> subRequirements = requirementRepository.findByParentId(id);
            if (!subRequirements.isEmpty()) {
                throw new RuntimeException("该主需求下存在子需求，无法删除");
            }
        }

        requirementRepository.deleteById(id);
        log.info("删除需求: id={}", id);
    }

    @Transactional
    public void updateSubRequirement(String id, String description) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("子需求不存在: " + id));

        if (!"SUB".equals(requirement.getRequirementType())) {
            throw new RuntimeException("只能更新子需求");
        }

        requirement.setDescription(description);
        requirementRepository.save(requirement);
        log.info("更新子需求描述: id={}", id);
    }

    private RequirementDTO toDTO(Requirement entity) {
        String parentName = null;
        if (entity.getParentId() != null) {
            parentName = requirementRepository.findById(entity.getParentId())
                    .map(Requirement::getName)
                    .orElse(null);
        }

        return RequirementDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .requirementType(entity.getRequirementType())
                .parentId(entity.getParentId())
                .parentName(parentName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private RequirementDTO toDTOWithChildren(Requirement entity) {
        RequirementDTO dto = toDTO(entity);
        if ("MAIN".equals(entity.getRequirementType())) {
            List<Requirement> children = requirementRepository.findByParentId(entity.getId());
            dto.setChildren(children.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private String generateCode() {
        long count = requirementRepository.count();
        return String.format("REQ-%04d", count + 1);
    }

    private String generateSubCode(String parentCode) {
        List<Requirement> subRequirements = requirementRepository.findByParentId(
                requirementRepository.findByCode(parentCode).getId());
        return parentCode + "-" + (subRequirements.size() + 1);
    }
}