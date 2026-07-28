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
public class MethodUpdateRequest {

    @NotBlank(message = "方法名称不能为空")
    @Size(max = 100, message = "方法名称长度不能超过100个字符")
    private String name;

    @Size(max = 50, message = "方法编码长度不能超过50个字符")
    private String code;

    @Size(max = 65535, message = "方法描述长度不能超过65535个字符")
    private String description;

    private List<String> parentRequirementIds;

    private List<String> inputParams;

    private List<String> outputParams;
}