package com.zmodel.controller;

import com.zmodel.dto.request.BpmnProcessCreateRequest;
import com.zmodel.dto.request.BpmnProcessUpdateRequest;
import com.zmodel.dto.request.NodeModelBindRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.BpmnProcessDTO;
import com.zmodel.dto.response.BpmnProcessVersionDTO;
import com.zmodel.dto.response.NodeModelBindingDTO;
import com.zmodel.service.BpmnProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
public class BpmnProcessController {

    private final BpmnProcessService bpmnProcessService;

    @GetMapping
    public ApiResponse<Page<BpmnProcessDTO>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BpmnProcessDTO> result = bpmnProcessService.list(keyword, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<BpmnProcessDTO> getById(@PathVariable String id) {
        BpmnProcessDTO result = bpmnProcessService.getById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<BpmnProcessDTO> create(@Valid @RequestBody BpmnProcessCreateRequest request) {
        BpmnProcessDTO result = bpmnProcessService.create(request);
        return ApiResponse.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<BpmnProcessDTO> update(@PathVariable String id, @RequestBody BpmnProcessUpdateRequest request) {
        BpmnProcessDTO result = bpmnProcessService.update(id, request);
        return ApiResponse.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        bpmnProcessService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<BpmnProcessVersionDTO>> getVersions(@PathVariable String id) {
        List<BpmnProcessVersionDTO> result = bpmnProcessService.getVersions(id);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/rollback")
    public ApiResponse<BpmnProcessDTO> rollbackVersion(
            @PathVariable String id,
            @RequestParam Integer version) {
        BpmnProcessDTO result = bpmnProcessService.rollbackVersion(id, version);
        return ApiResponse.success("回滚成功", result);
    }

    @GetMapping("/{id}/bindings")
    public ApiResponse<List<NodeModelBindingDTO>> getNodeBindings(@PathVariable String id) {
        List<NodeModelBindingDTO> result = bpmnProcessService.getNodeBindings(id);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/bindings/{nodeId}")
    public ApiResponse<List<NodeModelBindingDTO>> getModelBindingsByNode(
            @PathVariable String id,
            @PathVariable String nodeId) {
        List<NodeModelBindingDTO> result = bpmnProcessService.getModelBindingsByNode(id, nodeId);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/bindings")
    public ApiResponse<NodeModelBindingDTO> bindNodeModel(
            @PathVariable String id,
            @RequestBody NodeModelBindRequest request) {
        NodeModelBindingDTO result = bpmnProcessService.bindNodeModel(id, request);
        return ApiResponse.success("绑定成功", result);
    }

    @DeleteMapping("/{id}/bindings/{nodeId}")
    public ApiResponse<Void> unbindNodeModel(
            @PathVariable String id,
            @PathVariable String nodeId) {
        bpmnProcessService.unbindNodeModel(id, nodeId);
        return ApiResponse.success("解绑成功", null);
    }
}
