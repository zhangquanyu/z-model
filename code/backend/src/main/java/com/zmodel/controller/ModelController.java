package com.zmodel.controller;

import com.zmodel.dto.request.ModelCreateRequest;
import com.zmodel.dto.request.ModelUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.ModelDTO;
import com.zmodel.dto.response.RequirementDTO;
import com.zmodel.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @GetMapping
    public ApiResponse<Page<ModelDTO>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ModelDTO> result = modelService.list(keyword, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ModelDTO> getById(@PathVariable String id) {
        ModelDTO result = modelService.getById(id);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/requirements")
    public ApiResponse<List<RequirementDTO>> getModelRequirements(@PathVariable String id) {
        List<RequirementDTO> result = modelService.getModelRequirements(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<ModelDTO> create(@Valid @RequestBody ModelCreateRequest request) {
        ModelDTO result = modelService.create(request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<ModelDTO> update(@PathVariable String id, @Valid @RequestBody ModelUpdateRequest request) {
        ModelDTO result = modelService.update(id, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        modelService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}