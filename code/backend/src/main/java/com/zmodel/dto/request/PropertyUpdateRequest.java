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
public class PropertyUpdateRequest {

    @NotBlank(message = "属性名称不能为空")
    @Size(max = 100, message = "属性名称长度不能超过100个字符")
    private String name;

    @Size(max = 50, message = "属性编码长度不能超过50个字符")
    private String code;

    @NotBlank(message = "属性类型不能为空")
    @Size(max = 20, message = "属性类型长度不能超过20个字符")
    private String dataType;

    @Size(max = 65535, message = "属性描述长度不能超过65535个字符")
    private String description;

    private List<String> parentRequirementIds;

    private Boolean required;

    private String defaultValue;
}