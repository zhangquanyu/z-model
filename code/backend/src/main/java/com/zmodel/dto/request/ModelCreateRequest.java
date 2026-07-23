package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCreateRequest {

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "模型编码不能为空")
    @Size(max = 50, message = "模型编码长度不能超过50个字符")
    private String code;

    @Size(max = 65535, message = "模型描述长度不能超过65535个字符")
    private String description;

    private List<String> requirementIds;
}