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
public class BpmnProcessVersionDTO {

    private String id;

    private String processId;

    private Integer version;

    private String bpmnXml;

    private String changeNote;

    private LocalDateTime createdAt;
}
