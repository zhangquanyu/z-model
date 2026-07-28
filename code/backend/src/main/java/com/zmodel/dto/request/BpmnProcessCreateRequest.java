package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmnProcessCreateRequest {

    @NotBlank(message = "流程名称不能为空")
    private String name;

    private String code;

    private String description;

    private String bpmnXml;
}
