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
public class BpmnProcessDTO {

    private String id;

    private String name;

    private String code;

    private String description;

    private String bpmnXml;

    private Integer version;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<BpmnProcessVersionDTO> versions;

    private List<NodeModelBindingDTO> nodeBindings;
}
