package com.zmodel.controller;

import com.zmodel.dto.request.PropertyCreateRequest;
import com.zmodel.dto.request.PropertyUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models/{modelId}/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ApiResponse<Page<PropertyDTO>> list(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<PropertyDTO> result = propertyService.listByModelId(modelId, name, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/by-requirement")
    public ApiResponse<List<PropertyDTO>> listByRequirement(
            @PathVariable String modelId,
            @RequestParam String requirementId) {
        List<PropertyDTO> result = propertyService.listByModelIdAndRequirement(modelId, requirementId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{propertyId}")
    public ApiResponse<PropertyDTO> getById(@PathVariable String modelId, @PathVariable String propertyId) {
        PropertyDTO result = propertyService.getById(modelId, propertyId);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<PropertyDTO> create(@PathVariable String modelId, @Valid @RequestBody PropertyCreateRequest request) {
        PropertyDTO result = propertyService.create(modelId, request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{propertyId}")
    public ApiResponse<PropertyDTO> update(@PathVariable String modelId, @PathVariable String propertyId,
                                           @Valid @RequestBody PropertyUpdateRequest request) {
        PropertyDTO result = propertyService.update(modelId, propertyId, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{propertyId}")
    public ApiResponse<Void> delete(@PathVariable String modelId, @PathVariable String propertyId) {
        propertyService.delete(modelId, propertyId);
        return ApiResponse.success("删除成功", null);
    }
}