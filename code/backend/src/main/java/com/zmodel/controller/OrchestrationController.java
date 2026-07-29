package com.zmodel.controller;

import com.zmodel.dto.request.OrchestrationCreateRequest;
import com.zmodel.dto.request.OrchestrationDesignSaveRequest;
import com.zmodel.dto.request.OrchestrationNodeMethodRequest;
import com.zmodel.dto.request.OrchestrationNodeRequest;
import com.zmodel.dto.request.OrchestrationUpdateRequest;
import com.zmodel.dto.response.*;
import com.zmodel.service.OrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orchestrations")
@RequiredArgsConstructor
public class OrchestrationController {

    private final OrchestrationService orchestrationService;

    @GetMapping
    public ApiResponse<Page<OrchestrationSummaryDTO>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrchestrationSummaryDTO> result = orchestrationService.list(keyword, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrchestrationDTO> getById(@PathVariable String id) {
        OrchestrationDTO result = orchestrationService.getById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<OrchestrationDTO> create(@RequestBody OrchestrationCreateRequest request) {
        OrchestrationDTO result = orchestrationService.create(request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<OrchestrationDTO> update(@PathVariable String id, @RequestBody OrchestrationUpdateRequest request) {
        OrchestrationDTO result = orchestrationService.update(id, request);
        return ApiResponse.success("更新成功", result);
    }

    @PutMapping("/{id}/design")
    public ApiResponse<OrchestrationDTO> saveDesign(@PathVariable String id,
                                                    @RequestBody OrchestrationDesignSaveRequest request) {
        OrchestrationDTO result = orchestrationService.saveDesign(id, request);
        return ApiResponse.success("设计保存成功", result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        orchestrationService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/{id}/nodes")
    public ApiResponse<OrchestrationNodeDTO> addNode(@PathVariable String id, @RequestBody OrchestrationNodeRequest request) {
        OrchestrationNodeDTO result = orchestrationService.addNode(id, request);
        return ApiResponse.success("添加节点成功", result);
    }

    @PutMapping("/{id}/nodes/{nodeId}")
    public ApiResponse<OrchestrationNodeDTO> updateNode(
            @PathVariable String id,
            @PathVariable String nodeId,
            @RequestBody OrchestrationNodeRequest request) {
        OrchestrationNodeDTO result = orchestrationService.updateNode(id, nodeId, request);
        return ApiResponse.success("更新节点成功", result);
    }

    @DeleteMapping("/{id}/nodes/{nodeId}")
    public ApiResponse<Void> deleteNode(@PathVariable String id, @PathVariable String nodeId) {
        orchestrationService.deleteNode(id, nodeId);
        return ApiResponse.success("删除节点成功", null);
    }

    @PostMapping("/{id}/nodes/{nodeId}/methods")
    public ApiResponse<OrchestrationNodeMethodDTO> addNodeMethod(
            @PathVariable String id,
            @PathVariable String nodeId,
            @RequestBody OrchestrationNodeMethodRequest request) {
        OrchestrationNodeMethodDTO result = orchestrationService.addNodeMethod(id, nodeId, request);
        return ApiResponse.success("添加方法成功", result);
    }

    @DeleteMapping("/{id}/nodes/{nodeId}/methods/{methodId}")
    public ApiResponse<Void> removeNodeMethod(
            @PathVariable String id,
            @PathVariable String nodeId,
            @PathVariable String methodId) {
        orchestrationService.removeNodeMethod(id, nodeId, methodId);
        return ApiResponse.success("移除方法成功", null);
    }

    @PutMapping("/{id}/nodes/sort")
    public ApiResponse<Void> updateNodeSort(
            @PathVariable String id,
            @RequestBody List<String> nodeIds) {
        orchestrationService.updateNodeSort(id, nodeIds);
        return ApiResponse.success("排序更新成功", null);
    }

    @GetMapping("/{id}/requirements")
    public ApiResponse<List<RequirementDTO>> getRequirements(@PathVariable String id) {
        List<RequirementDTO> result = orchestrationService.getOrchestrationRequirements(id);
        return ApiResponse.success(result);
    }
}
