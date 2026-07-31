package com.zmodel.controller;

import com.zmodel.dto.request.PhysicalPropertyCreateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.PhysicalPropertyDTO;
import com.zmodel.service.PhysicalPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/physical-properties")
@RequiredArgsConstructor
public class PhysicalPropertyController {

    private final PhysicalPropertyService physicalPropertyService;

    @GetMapping("/physical-model/{physicalModelId}")
    public ApiResponse<List<PhysicalPropertyDTO>> listByPhysicalModelId(@PathVariable String physicalModelId) {
        return ApiResponse.success(physicalPropertyService.listByPhysicalModelId(physicalModelId));
    }

    @PostMapping
    public ApiResponse<PhysicalPropertyDTO> create(@RequestBody PhysicalPropertyCreateRequest request) {
        return ApiResponse.success(physicalPropertyService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhysicalPropertyDTO> update(
            @PathVariable String id,
            @RequestBody PhysicalPropertyCreateRequest request) {
        return ApiResponse.success(physicalPropertyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        physicalPropertyService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/sync")
    public ApiResponse<PhysicalPropertyDTO> syncFromSource(@PathVariable String id) {
        return ApiResponse.success(physicalPropertyService.syncFromSource(id));
    }
}
