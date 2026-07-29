package com.zmodel.controller;

import com.zmodel.dto.request.MethodCreateRequest;
import com.zmodel.dto.request.MethodUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.MethodDTO;
import com.zmodel.service.MethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models/{modelId}/methods")
@RequiredArgsConstructor
public class MethodController {

    private final MethodService methodService;

    @GetMapping
    public ApiResponse<Page<MethodDTO>> list(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<MethodDTO> result = methodService.listByModelId(modelId, name, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/by-requirement")
    public ApiResponse<List<MethodDTO>> listByRequirement(
            @PathVariable String modelId,
            @RequestParam String requirementId) {
        List<MethodDTO> result = methodService.listByModelIdAndRequirement(modelId, requirementId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{methodId}")
    public ApiResponse<MethodDTO> getById(@PathVariable String modelId, @PathVariable String methodId) {
        MethodDTO result = methodService.getById(modelId, methodId);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<MethodDTO> create(@PathVariable String modelId, @Valid @RequestBody MethodCreateRequest request) {
        MethodDTO result = methodService.create(modelId, request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{methodId}")
    public ApiResponse<MethodDTO> update(@PathVariable String modelId, @PathVariable String methodId,
                                         @Valid @RequestBody MethodUpdateRequest request) {
        MethodDTO result = methodService.update(modelId, methodId, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{methodId}")
    public ApiResponse<Void> delete(@PathVariable String modelId, @PathVariable String methodId) {
        methodService.delete(modelId, methodId);
        return ApiResponse.success("删除成功", null);
    }
}