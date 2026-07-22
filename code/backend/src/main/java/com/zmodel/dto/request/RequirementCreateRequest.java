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
public class RequirementCreateRequest {

    @NotBlank(message = "需求名称不能为空")
    @Size(max = 100, message = "需求名称长度不能超过100个字符")
    private String name;

    @Size(max = 5000, message = "需求描述长度不能超过5000个字符")
    private String description;

    @Size(max = 20, message = "状态长度不能超过20个字符")
    @Builder.Default
    private String status = "DRAFT";

    @Size(max = 20, message = "优先级长度不能超过20个字符")
    @Builder.Default
    private String priority = "MEDIUM";
}
