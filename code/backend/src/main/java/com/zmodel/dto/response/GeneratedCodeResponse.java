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
public class GeneratedCodeResponse {
    private String orchestrationId;
    private String entityCode;
    private String mapperCode;
    private String mapperXmlCode;
    private String serviceInterfaceCode;
    private String serviceCode;
    private String controllerCode;
    private String orchestrationCode;
    private LocalDateTime generatedTime;
    private List<GeneratedFile> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedFile {
        private String path;
        private String name;
        private String language;
        private String content;
    }
}
