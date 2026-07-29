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
public class OrchestrationSummaryDTO {
    private String id;
    private String name;
    private String code;
    private String description;
    private String status;
    private Integer version;
    private Integer nodeCount;
    private Integer methodCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
