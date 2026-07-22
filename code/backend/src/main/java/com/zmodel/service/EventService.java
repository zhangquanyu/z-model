package com.zmodel.service;

import com.zmodel.dto.request.EventCreateRequest;
import com.zmodel.dto.request.EventUpdateRequest;
import com.zmodel.dto.response.EventDTO;
import com.zmodel.dto.response.EventTotalResponse;
import com.zmodel.entity.Event;
import com.zmodel.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventDTO create(EventCreateRequest request) {
        Event event = Event.builder()
                .externalFlowNo1(request.getExternalFlowNo1())
                .externalFlowNo2(request.getExternalFlowNo2())
                .pointBrandCode(request.getPointBrandCode())
                .sceneCode(request.getSceneCode())
                .mainOrderNo(request.getMainOrderNo())
                .subOrderNo(request.getSubOrderNo())
                .eventTime(request.getEventTime())
                .partnerCode(request.getPartnerCode())
                .memberCardNo(request.getMemberCardNo())
                .salesChannel1(request.getSalesChannel1())
                .salesChannel2(request.getSalesChannel2())
                .entryFlag(request.getEntryFlag())
                .externalFlowNo3(request.getExternalFlowNo3())
                .businessTag(request.getBusinessTag())
                .eventType(request.getEventType())
                .eventAmount(request.getEventAmount())
                .pfrId(request.getPfrId())
                .operator(request.getOperator())
                .remark(request.getRemark())
                .status("PENDING")
                .build();

        event = eventRepository.save(event);
        log.info("登记事件流水: id={}, externalFlowNo1={}", event.getId(), event.getExternalFlowNo1());
        return toDTO(event);
    }

    @Transactional(readOnly = true)
    public EventDTO getById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("事件不存在: " + id));
        return toDTO(event);
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> list(String externalFlowNo1, String memberCardNo, String eventType,
                               LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        Page<Event> page = eventRepository.findByConditions(externalFlowNo1, memberCardNo, eventType,
                startTime, endTime, pageable);
        return page.map(this::toDTO);
    }

    @Transactional
    public EventDTO update(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("事件不存在: " + id));

        if (request.getExternalFlowNo2() != null) {
            event.setExternalFlowNo2(request.getExternalFlowNo2());
        }
        if (request.getPointBrandCode() != null) {
            event.setPointBrandCode(request.getPointBrandCode());
        }
        if (request.getSceneCode() != null) {
            event.setSceneCode(request.getSceneCode());
        }
        if (request.getMainOrderNo() != null) {
            event.setMainOrderNo(request.getMainOrderNo());
        }
        if (request.getSubOrderNo() != null) {
            event.setSubOrderNo(request.getSubOrderNo());
        }
        if (request.getEventTime() != null) {
            event.setEventTime(request.getEventTime());
        }
        if (request.getPartnerCode() != null) {
            event.setPartnerCode(request.getPartnerCode());
        }
        if (request.getMemberCardNo() != null) {
            event.setMemberCardNo(request.getMemberCardNo());
        }
        if (request.getSalesChannel1() != null) {
            event.setSalesChannel1(request.getSalesChannel1());
        }
        if (request.getSalesChannel2() != null) {
            event.setSalesChannel2(request.getSalesChannel2());
        }
        if (request.getEntryFlag() != null) {
            event.setEntryFlag(request.getEntryFlag());
        }
        if (request.getExternalFlowNo3() != null) {
            event.setExternalFlowNo3(request.getExternalFlowNo3());
        }
        if (request.getBusinessTag() != null) {
            event.setBusinessTag(request.getBusinessTag());
        }
        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }
        if (request.getEventAmount() != null) {
            event.setEventAmount(request.getEventAmount());
        }
        if (request.getPfrId() != null) {
            event.setPfrId(request.getPfrId());
        }
        if (request.getOperator() != null) {
            event.setOperator(request.getOperator());
        }
        if (request.getRemark() != null) {
            event.setRemark(request.getRemark());
        }

        event = eventRepository.save(event);
        log.info("更新事件流水: id={}", event.getId());
        return toDTO(event);
    }

    @Transactional
    public EventDTO updateStatus(Long id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("事件不存在: " + id));

        event.setStatus(status);
        event = eventRepository.save(event);
        log.info("事件流水状态变更: id={}, status={}", event.getId(), status);
        return toDTO(event);
    }

    @Transactional(readOnly = true)
    public Boolean validateExternalFlowNo(String externalFlowNo1) {
        return !eventRepository.existsByExternalFlowNo1(externalFlowNo1);
    }

    @Transactional(readOnly = true)
    public EventTotalResponse calculateTotal(String memberCardNo, String eventType,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal totalAmount = eventRepository.sumEventAmount(memberCardNo, eventType, startTime, endTime);
        Long count = eventRepository.count();

        return EventTotalResponse.builder()
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .count(count)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> findByExternalFlowNo1(String externalFlowNo1, Pageable pageable) {
        Page<Event> page = eventRepository.findByExternalFlowNo1Containing(externalFlowNo1, pageable);
        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> findByMemberCardNo(String memberCardNo, Pageable pageable) {
        Page<Event> page = eventRepository.findByMemberCardNoContaining(memberCardNo, pageable);
        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Boolean checkReversible(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("事件不存在: " + eventId));
        return "PENDING".equals(event.getStatus()) || "PROCESSING".equals(event.getStatus());
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> findOriginalEvents(Long eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("事件不存在: " + eventId));
        return eventRepository.findByExternalFlowNo1Containing(event.getExternalFlowNo1(), pageable)
                .map(this::toDTO);
    }

    private EventDTO toDTO(Event entity) {
        return EventDTO.builder()
                .id(entity.getId())
                .externalFlowNo1(entity.getExternalFlowNo1())
                .externalFlowNo2(entity.getExternalFlowNo2())
                .pointBrandCode(entity.getPointBrandCode())
                .sceneCode(entity.getSceneCode())
                .mainOrderNo(entity.getMainOrderNo())
                .subOrderNo(entity.getSubOrderNo())
                .eventTime(entity.getEventTime())
                .partnerCode(entity.getPartnerCode())
                .memberCardNo(entity.getMemberCardNo())
                .salesChannel1(entity.getSalesChannel1())
                .salesChannel2(entity.getSalesChannel2())
                .entryFlag(entity.getEntryFlag())
                .externalFlowNo3(entity.getExternalFlowNo3())
                .businessTag(entity.getBusinessTag())
                .eventType(entity.getEventType())
                .eventAmount(entity.getEventAmount())
                .pfrId(entity.getPfrId())
                .operator(entity.getOperator())
                .remark(entity.getRemark())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
