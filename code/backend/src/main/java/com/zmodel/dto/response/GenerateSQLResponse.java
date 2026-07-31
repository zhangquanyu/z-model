package com.zmodel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSQLResponse {
    private String physicalModelId;
    private String tableName;
    private String createTableSQL;
    private List<MethodSQL> methodSQLs;
    private String completeSQL;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodSQL {
        private String id;
        private String name;
        private String methodName;
        private String methodType;
        private String code;
        private String sql;
    }
}
