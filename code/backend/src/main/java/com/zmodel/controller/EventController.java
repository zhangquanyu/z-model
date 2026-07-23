package com.zmodel.controller;

import com.zmodel.dto.request.EventCreateRequest;
import com.zmodel.dto.request.EventStatusUpdateRequest;
import com.zmodel.dto.request.EventUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.EventDTO;
import com.zmodel.dto.response.EventTotalResponse;
import com.zmodel.dto.response.EventValidateResponse;
import com.zmodel.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<Page<EventDTO>> list(
            @RequestParam(defaultValue = "") String externalFlowNo1,
            @RequestParam(defaultValue = "") String memberCardNo,
            @RequestParam(defaultValue = "") String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventDTO> result = eventService.list(externalFlowNo1, memberCardNo, eventType, startTime, endTime, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getById(@PathVariable String id) {
        EventDTO result = eventService.getById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<EventDTO> create(@Valid @RequestBody EventCreateRequest request) {
        EventDTO result = eventService.create(request);
        return ApiResponse.success("登记成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<EventDTO> update(@PathVariable String id, @Valid @RequestBody EventUpdateRequest request) {
        EventDTO result = eventService.update(id, request);
        return ApiResponse.success("更新成功", result);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<EventDTO> updateStatus(@PathVariable String id, @Valid @RequestBody EventStatusUpdateRequest request) {
        EventDTO result = eventService.updateStatus(id, request.getStatus());
        return ApiResponse.success("状态变更成功", result);
    }

    @PostMapping("/validate")
    public ApiResponse<EventValidateResponse> validate(@RequestBody EventCreateRequest request) {
        Boolean isValid = eventService.validateExternalFlowNo(request.getExternalFlowNo1());
        EventValidateResponse response = EventValidateResponse.builder()
                .isValid(isValid)
                .message(isValid ? "外部订单号可用" : "外部订单号已存在")
                .build();
        return ApiResponse.success(response);
    }

    @GetMapping("/total")
    public ApiResponse<EventTotalResponse> calculateTotal(
            @RequestParam(defaultValue = "") String memberCardNo,
            @RequestParam(defaultValue = "") String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        EventTotalResponse result = eventService.calculateTotal(memberCardNo, eventType, startTime, endTime);
        return ApiResponse.success(result);
    }

    @GetMapping("/search/flowNo")
    public ApiResponse<Page<EventDTO>> searchByFlowNo(
            @RequestParam String externalFlowNo1,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventDTO> result = eventService.findByExternalFlowNo1(externalFlowNo1, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/search/memberCard")
    public ApiResponse<Page<EventDTO>> searchByMemberCard(
            @RequestParam String memberCardNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventDTO> result = eventService.findByMemberCardNo(memberCardNo, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/reversible")
    public ApiResponse<Boolean> checkReversible(@PathVariable String id) {
        Boolean result = eventService.checkReversible(id);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/original")
    public ApiResponse<Page<EventDTO>> findOriginalEvents(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventDTO> result = eventService.findOriginalEvents(id, pageable);
        return ApiResponse.success(result);
    }
}