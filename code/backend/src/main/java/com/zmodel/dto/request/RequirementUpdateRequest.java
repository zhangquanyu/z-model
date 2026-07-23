package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementUpdateRequest {

    @NotBlank(message = "需求名称不能为空")
    @Size(max = 100, message = "需求名称长度不能超过100个字符")
    private String name;

    @Size(max = 50, message = "需求编号长度不能超过50个字符")
    private String code;

    @Size(max = 65535, message = "需求描述长度不能超过65535个字符")
    private String description;

    @Size(max = 20, message = "状态长度不能超过20个字符")
    private String status;

    @Size(max = 20, message = "优先级长度不能超过20个字符")
    private String priority;

    @Size(max = 10, message = "需求类型长度不能超过10个字符")
    private String requirementType;

    private String parentId;
}