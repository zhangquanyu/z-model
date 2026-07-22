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

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;

    @Transactional
    public RequirementDTO create(RequirementCreateRequest request) {
        Requirement requirement = Requirement.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .build();
        
        requirement = requirementRepository.save(requirement);
        log.info("创建需求: id={}, name={}", requirement.getId(), requirement.getName());
        return toDTO(requirement);
    }

    @Transactional(readOnly = true)
    public RequirementDTO getById(Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + id));
        return toDTO(requirement);
    }

    @Transactional(readOnly = true)
    public Page<RequirementDTO> list(String keyword, String status, Pageable pageable) {
        Page<Requirement> page = requirementRepository.findByKeywordAndStatus(keyword, status, pageable);
        return page.map(this::toDTO);
    }

    @Transactional
    public RequirementDTO update(Long id, RequirementUpdateRequest request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + id));
        
        requirement.setName(request.getName());
        requirement.setDescription(request.getDescription());
        requirement.setStatus(request.getStatus());
        requirement.setPriority(request.getPriority());
        
        requirement = requirementRepository.save(requirement);
        log.info("更新需求: id={}, name={}", requirement.getId(), requirement.getName());
        return toDTO(requirement);
    }

    @Transactional
    public void delete(Long id) {
        if (!requirementRepository.existsById(id)) {
            throw new RuntimeException("需求不存在: " + id);
        }
        requirementRepository.deleteById(id);
        log.info("删除需求: id={}", id);
    }

    private RequirementDTO toDTO(Requirement entity) {
        return RequirementDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
