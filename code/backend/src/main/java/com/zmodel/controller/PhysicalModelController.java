package com.zmodel.controller;

import com.zmodel.dto.request.PhysicalModelCreateRequest;
import com.zmodel.dto.request.PhysicalModelUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.GenerateSQLResponse;
import com.zmodel.dto.response.PhysicalModelDTO;
import com.zmodel.service.PhysicalModelService;
import com.zmodel.service.SQLGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/physical-models")
@RequiredArgsConstructor
public class PhysicalModelController {

    private final PhysicalModelService physicalModelService;
    private final SQLGeneratorService sqlGeneratorService;

    @GetMapping
    public ApiResponse<Page<PhysicalModelDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(physicalModelService.list(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PhysicalModelDTO> getById(@PathVariable String id) {
        return ApiResponse.success(physicalModelService.getById(id));
    }

    @GetMapping("/model/{modelId}")
    public ApiResponse<?> getByModelId(@PathVariable String modelId) {
        return ApiResponse.success(physicalModelService.getByModelId(modelId));
    }

    @PostMapping
    public ApiResponse<PhysicalModelDTO> create(@RequestBody PhysicalModelCreateRequest request) {
        return ApiResponse.success(physicalModelService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhysicalModelDTO> update(
            @PathVariable String id,
            @RequestBody PhysicalModelUpdateRequest request) {
        return ApiResponse.success(physicalModelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        physicalModelService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/generate-sql")
    public ApiResponse<GenerateSQLResponse> generateSQL(@PathVariable String id) {
        return ApiResponse.success(sqlGeneratorService.generateSQL(id));
    }
}
