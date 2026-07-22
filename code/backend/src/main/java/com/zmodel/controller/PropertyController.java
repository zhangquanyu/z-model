package com.zmodel.controller;

import com.zmodel.dto.request.PropertyCreateRequest;
import com.zmodel.dto.request.PropertyUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.PropertyDTO;
import com.zmodel.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models/{modelId}/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ApiResponse<List<PropertyDTO>> list(@PathVariable Long modelId) {
        List<PropertyDTO> result = propertyService.listByModelId(modelId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{propertyId}")
    public ApiResponse<PropertyDTO> getById(@PathVariable Long modelId, @PathVariable Long propertyId) {
        PropertyDTO result = propertyService.getById(modelId, propertyId);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<PropertyDTO> create(@PathVariable Long modelId, @Valid @RequestBody PropertyCreateRequest request) {
        PropertyDTO result = propertyService.create(modelId, request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{propertyId}")
    public ApiResponse<PropertyDTO> update(@PathVariable Long modelId, @PathVariable Long propertyId,
                                           @Valid @RequestBody PropertyUpdateRequest request) {
        PropertyDTO result = propertyService.update(modelId, propertyId, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{propertyId}")
    public ApiResponse<Void> delete(@PathVariable Long modelId, @PathVariable Long propertyId) {
        propertyService.delete(modelId, propertyId);
        return ApiResponse.success("删除成功", null);
    }
}
