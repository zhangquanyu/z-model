package com.zmodel.controller;

import com.zmodel.dto.request.MethodCreateRequest;
import com.zmodel.dto.request.MethodUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.MethodDTO;
import com.zmodel.service.MethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models/{modelId}/methods")
@RequiredArgsConstructor
public class MethodController {

    private final MethodService methodService;

    @GetMapping
    public ApiResponse<List<MethodDTO>> list(@PathVariable Long modelId) {
        List<MethodDTO> result = methodService.listByModelId(modelId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{methodId}")
    public ApiResponse<MethodDTO> getById(@PathVariable Long modelId, @PathVariable Long methodId) {
        MethodDTO result = methodService.getById(modelId, methodId);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<MethodDTO> create(@PathVariable Long modelId, @Valid @RequestBody MethodCreateRequest request) {
        MethodDTO result = methodService.create(modelId, request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{methodId}")
    public ApiResponse<MethodDTO> update(@PathVariable Long modelId, @PathVariable Long methodId,
                                         @Valid @RequestBody MethodUpdateRequest request) {
        MethodDTO result = methodService.update(modelId, methodId, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{methodId}")
    public ApiResponse<Void> delete(@PathVariable Long modelId, @PathVariable Long methodId) {
        methodService.delete(modelId, methodId);
        return ApiResponse.success("删除成功", null);
    }
}
