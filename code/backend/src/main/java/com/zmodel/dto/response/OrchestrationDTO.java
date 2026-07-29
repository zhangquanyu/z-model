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
public class OrchestrationDTO {
    private String id;
    private String name;
    private String code;
    private String description;
    private String status;
    private Integer version;
    private List<OrchestrationNodeDTO> nodes;
    private List<RequirementDTO> requirements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
