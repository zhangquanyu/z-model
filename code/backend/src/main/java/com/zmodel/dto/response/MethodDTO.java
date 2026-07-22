package com.zmodel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodDTO {

    private Long id;

    private Long modelId;

    private Long requirementId;

    private String requirementName;

    private String name;

    private String code;

    private String description;

    private List<PropertyDTO> inputParams;

    private List<PropertyDTO> outputParams;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
