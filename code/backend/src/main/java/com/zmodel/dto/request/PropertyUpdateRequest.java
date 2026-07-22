package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdateRequest {

    @NotBlank(message = "属性名称不能为空")
    @Size(max = 100, message = "属性名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "属性编码不能为空")
    @Size(max = 50, message = "属性编码长度不能超过50个字符")
    private String code;

    @NotBlank(message = "属性类型不能为空")
    @Size(max = 20, message = "属性类型长度不能超过20个字符")
    private String type;

    @Size(max = 5000, message = "属性描述长度不能超过5000个字符")
    private String description;

    @NotNull(message = "需求ID不能为空")
    private Long requirementId;

    private Boolean nullable;

    private Integer length;
}
