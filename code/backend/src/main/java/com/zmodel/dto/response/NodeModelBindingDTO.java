package com.zmodel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeModelBindingDTO {

    private String id;

    private String processId;

    private String nodeId;

    private String modelId;

    private String modelName;

    private String modelCode;

    private LocalDateTime createdAt;
}
