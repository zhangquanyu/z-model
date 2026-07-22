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
public class EventValidateRequest {

    @NotBlank(message = "外部流水号一级不能为空")
    @Size(max = 100, message = "外部流水号一级长度不能超过100个字符")
    private String externalFlowNo1;
}
