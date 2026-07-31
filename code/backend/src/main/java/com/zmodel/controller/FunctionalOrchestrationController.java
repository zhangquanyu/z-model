package com.zmodel.controller;

import com.zmodel.dto.request.FunctionalOrchestrationCreateRequest;
import com.zmodel.dto.request.FunctionalOrchestrationUpdateRequest;
import com.zmodel.dto.response.ApiResponse;
import com.zmodel.dto.response.FunctionalOrchestrationDTO;
import com.zmodel.dto.response.GeneratedCodeResponse;
import com.zmodel.service.CodeGeneratorService;
import com.zmodel.service.FunctionalOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/functional-orchestrations")
@RequiredArgsConstructor
public class FunctionalOrchestrationController {

    private final FunctionalOrchestrationService functionalOrchestrationService;
    private final CodeGeneratorService codeGeneratorService;

    @GetMapping
    public ApiResponse<Page<FunctionalOrchestrationDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(functionalOrchestrationService.list(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<FunctionalOrchestrationDTO> getById(@PathVariable String id) {
        return ApiResponse.success(functionalOrchestrationService.getById(id));
    }

    @GetMapping("/orchestration/{orchestrationId}")
    public ApiResponse<FunctionalOrchestrationDTO> getByOrchestrationId(@PathVariable String orchestrationId) {
        return ApiResponse.success(functionalOrchestrationService.getByOrchestrationId(orchestrationId));
    }

    @PostMapping
    public ApiResponse<FunctionalOrchestrationDTO> create(@RequestBody FunctionalOrchestrationCreateRequest request) {
        return ApiResponse.success(functionalOrchestrationService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<FunctionalOrchestrationDTO> update(
            @PathVariable String id,
            @RequestBody FunctionalOrchestrationUpdateRequest request) {
        return ApiResponse.success(functionalOrchestrationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        functionalOrchestrationService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/nodes")
    public ApiResponse<FunctionalOrchestrationDTO> saveNodes(
            @PathVariable String id,
            @RequestBody List<FunctionalOrchestrationDTO.FoNodeDTO> nodes) {
        return ApiResponse.success(functionalOrchestrationService.saveNodes(id, nodes));
    }

    @GetMapping("/{id}/generate-code")
    public ApiResponse<GeneratedCodeResponse> generateCode(@PathVariable String id) {
        FunctionalOrchestrationDTO dto = functionalOrchestrationService.getById(id);
        return ApiResponse.success(codeGeneratorService.generateCode(dto));
    }

    @GetMapping("/generate-by-orchestration/{orchestrationId}")
    public ApiResponse<GeneratedCodeResponse> generateCodeByOrchestrationId(@PathVariable String orchestrationId) {
        return ApiResponse.success(codeGeneratorService.generateCodeByOrchestrationId(orchestrationId));
    }
}
