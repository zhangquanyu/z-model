package com.zmodel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MethodUpdateRequest {

    @NotBlank(message = "方法名称不能为空")
    @Size(max = 100, message = "方法名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "方法编码不能为空")
    @Size(max = 50, message = "方法编码长度不能超过50个字符")
    private String code;

    @Size(max = 5000, message = "方法描述长度不能超过5000个字符")
    private String description;

    @NotNull(message = "需求ID不能为空")
    private Long requirementId;

    private List<Long> inputParams;

    private List<Long> outputParams;
}
