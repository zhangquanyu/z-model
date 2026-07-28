package com.zmodel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmnProcessUpdateRequest {

    private String name;

    private String description;

    private String bpmnXml;

    private String status;

    private String changeNote;
}
