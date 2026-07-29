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
public class OrchestrationNodeDTO {
    private String id;
    private String orchestrationId;
    private String nodeType;
    private String nodeName;
    private String description;
    private Integer sortOrder;
    private Integer loopCount;
    private List<OrchestrationNodeMethodDTO> methods;
    private LocalDateTime createdAt;
}
