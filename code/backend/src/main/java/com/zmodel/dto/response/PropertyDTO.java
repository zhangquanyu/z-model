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
public class PropertyDTO {

    private Long id;

    private Long modelId;

    private Long requirementId;

    private String requirementName;

    private String name;

    private String code;

    private String type;

    private String description;

    private Boolean nullable;

    private Integer length;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
