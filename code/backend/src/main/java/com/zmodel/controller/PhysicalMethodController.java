package com.zmodel.controller;

import com.zmodel.dto.request.PhysicalMethodCreateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.PhysicalMethodDTO;
import com.zmodel.service.PhysicalMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/physical-methods")
@RequiredArgsConstructor
public class PhysicalMethodController {

    private final PhysicalMethodService physicalMethodService;

    @GetMapping("/physical-model/{physicalModelId}")
    public ApiResponse<List<PhysicalMethodDTO>> listByPhysicalModelId(@PathVariable String physicalModelId) {
        return ApiResponse.success(physicalMethodService.listByPhysicalModelId(physicalModelId));
    }

    @PostMapping
    public ApiResponse<PhysicalMethodDTO> create(@RequestBody PhysicalMethodCreateRequest request) {
        return ApiResponse.success(physicalMethodService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhysicalMethodDTO> update(
            @PathVariable String id,
            @RequestBody PhysicalMethodCreateRequest request) {
        return ApiResponse.success(physicalMethodService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        physicalMethodService.delete(id);
        return ApiResponse.success(null);
    }
}
