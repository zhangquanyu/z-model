package com.zmodel.controller;

import com.zmodel.dto.request.RequirementCreateRequest;
import com.zmodel.dto.request.RequirementUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.RequirementDTO;
import com.zmodel.service.RequirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping
    public ApiResponse<Page<RequirementDTO>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequirementDTO> result = requirementService.list(keyword, status, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/main")
    public ApiResponse<List<RequirementDTO>> listMainRequirements(
            @RequestParam(defaultValue = "") String keyword) {
        List<RequirementDTO> result = requirementService.listMainRequirements(keyword);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<RequirementDTO> getById(@PathVariable String id) {
        RequirementDTO result = requirementService.getById(id);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/sub")
    public ApiResponse<List<RequirementDTO>> listSubRequirements(@PathVariable String id) {
        List<RequirementDTO> result = requirementService.listSubRequirements(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<RequirementDTO> create(@Valid @RequestBody RequirementCreateRequest request) {
        RequirementDTO result = requirementService.create(request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<RequirementDTO> update(@PathVariable String id, @Valid @RequestBody RequirementUpdateRequest request) {
        RequirementDTO result = requirementService.update(id, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        requirementService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}