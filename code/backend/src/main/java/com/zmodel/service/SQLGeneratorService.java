package com.zmodel.service;

import com.zmodel.dto.response.GenerateSQLResponse;
import com.zmodel.dto.response.PhysicalMethodDTO;
import com.zmodel.dto.response.PhysicalPropertyDTO;
import com.zmodel.entity.PhysicalModel;
import com.zmodel.entity.PhysicalMethod;
import com.zmodel.entity.PhysicalProperty;
import com.zmodel.repository.PhysicalMethodRepository;
import com.zmodel.repository.PhysicalModelRepository;
import com.zmodel.repository.PhysicalPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SQLGeneratorService {

    private final PhysicalModelRepository physicalModelRepository;
    private final PhysicalPropertyRepository physicalPropertyRepository;
    private final PhysicalMethodRepository physicalMethodRepository;

    private static final Map<String, String> DATA_TYPE_TO_DB_TYPE = new HashMap<>();
    
    static {
        DATA_TYPE_TO_DB_TYPE.put("STRING", "VARCHAR");
        DATA_TYPE_TO_DB_TYPE.put("INTEGER", "INT");
        DATA_TYPE_TO_DB_TYPE.put("LONG", "BIGINT");
        DATA_TYPE_TO_DB_TYPE.put("DOUBLE", "DECIMAL");
        DATA_TYPE_TO_DB_TYPE.put("FLOAT", "FLOAT");
        DATA_TYPE_TO_DB_TYPE.put("BOOLEAN", "TINYINT");
        DATA_TYPE_TO_DB_TYPE.put("DATE", "DATE");
        DATA_TYPE_TO_DB_TYPE.put("DATETIME", "DATETIME");
        DATA_TYPE_TO_DB_TYPE.put("OBJECT", "JSON");
        DATA_TYPE_TO_DB_TYPE.put("ARRAY", "JSON");
    }

    public GenerateSQLResponse generateSQL(String physicalModelId) {
        PhysicalModel model = physicalModelRepository.findById(physicalModelId)
                .orElseThrow(() -> new RuntimeException("物理模型不存在"));

        List<PhysicalProperty> properties = physicalPropertyRepository.findByPhysicalModelId(physicalModelId);
        List<PhysicalMethod> methods = physicalMethodRepository.findByPhysicalModelId(physicalModelId);

        GenerateSQLResponse response = new GenerateSQLResponse();
        response.setPhysicalModelId(physicalModelId);
        response.setTableName(model.getTableName());

        // 生成 CREATE TABLE SQL
        response.setCreateTableSQL(generateCreateTableSQL(model, properties));
        
        // 生成方法 SQL
        response.setMethodSQLs(generateMethodSQLs(model, methods, properties));
        
        // 生成完整 SQL
        response.setCompleteSQL(generateCompleteSQL(model, properties, methods));

        return response;
    }

    public GenerateSQLResponse generateSQLByTableName(String tableName, List<PhysicalProperty> properties, List<PhysicalMethod> methods) {
        GenerateSQLResponse response = new GenerateSQLResponse();
        response.setTableName(tableName);
        response.setCreateTableSQL(generateCreateTableSQL(tableName, properties));
        response.setMethodSQLs(generateMethodSQLs(null, methods, properties));
        response.setCompleteSQL(generateCompleteSQL(null, properties, methods));
        return response;
    }

    private String generateCreateTableSQL(PhysicalModel model, List<PhysicalProperty> properties) {
        return generateCreateTableSQL(model != null ? model.getTableName() : "table", properties);
    }

    private String generateCreateTableSQL(String tableName, List<PhysicalProperty> properties) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
        
        List<String> columnDefs = new ArrayList<>();
        
        // 添加主键
        columnDefs.add("`id` VARCHAR(36) NOT NULL COMMENT '主键ID'");
        
        // 添加业务字段
        for (PhysicalProperty prop : properties) {
            columnDefs.add(generateColumnDefinition(prop));
        }
        
        // 添加创建时间和更新时间
        columnDefs.add("`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        columnDefs.add("`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");
        
        // 主键定义
        columnDefs.add("PRIMARY KEY (`id`)");
        
        sql.append(String.join(",\n", columnDefs));
        sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表';");
        
        return sql.toString();
    }

    private String generateColumnDefinition(PhysicalProperty prop) {
        StringBuilder def = new StringBuilder();
        def.append("`").append(prop.getCode()).append("` ");
        
        // 获取数据库类型
        String dbType = prop.getDbType();
        if (dbType == null || dbType.isEmpty()) {
            dbType = DATA_TYPE_TO_DB_TYPE.getOrDefault(prop.getDataType(), "VARCHAR");
        }
        def.append(dbType);
        
        // 添加长度/精度
        if (prop.getDbLength() != null && prop.getDbLength() > 0) {
            if ("DECIMAL".equals(dbType) && prop.getDbPrecision() != null) {
                def.append("(").append(prop.getDbPrecision()).append(",")
                   .append(prop.getDbScale() != null ? prop.getDbScale() : 2).append(")");
            } else {
                def.append("(").append(prop.getDbLength()).append(")");
            }
        } else if ("VARCHAR".equals(dbType)) {
            def.append("(255)");
        } else if ("DECIMAL".equals(dbType)) {
            def.append("(10,2)");
        }
        
        // 可空性
        if (prop.getNullable() == null || !prop.getNullable()) {
            def.append(" NOT NULL");
        }
        
        // 默认值
        if (prop.getDefaultValue() != null && !prop.getDefaultValue().isEmpty()) {
            def.append(" DEFAULT '").append(prop.getDefaultValue()).append("'");
        }
        
        // 注释
        if (prop.getDescription() != null && !prop.getDescription().isEmpty()) {
            def.append(" COMMENT '").append(prop.getDescription()).append("'");
        }
        
        return def.toString();
    }

    private List<GenerateSQLResponse.MethodSQL> generateMethodSQLs(PhysicalModel model, List<PhysicalMethod> methods, List<PhysicalProperty> properties) {
        List<GenerateSQLResponse.MethodSQL> result = new ArrayList<>();
        String tableName = model != null ? model.getTableName() : "table";
        
        for (PhysicalMethod method : methods) {
            GenerateSQLResponse.MethodSQL methodSQL = new GenerateSQLResponse.MethodSQL();
            methodSQL.setId(method.getId());
            methodSQL.setName(method.getName());
            methodSQL.setCode(method.getCode());
            methodSQL.setMethodType(method.getMethodType());
            methodSQL.setSql(generateMethodSQL(tableName, method, properties));
            result.add(methodSQL);
        }
        
        return result;
    }

    private String generateMethodSQL(String tableName, PhysicalMethod method, List<PhysicalProperty> properties) {
        // 如果有自定义 SQL 模板，使用它
        if (method.getSqlTemplate() != null && !method.getSqlTemplate().isEmpty()) {
            return method.getSqlTemplate().replace("table_name", tableName);
        }
        
        // 根据方法类型生成默认 SQL
        switch (method.getMethodType() != null ? method.getMethodType() : "SELECT") {
            case "INSERT":
                return generateInsertSQL(tableName, properties);
            case "UPDATE":
                return generateUpdateSQL(tableName, properties);
            case "DELETE":
                return generateDeleteSQL(tableName);
            case "SELECT":
                return generateSelectSQL(tableName, properties);
            default:
                return "-- 自定义方法: " + method.getName();
        }
    }

    private String generateInsertSQL(String tableName, List<PhysicalProperty> properties) {
        String columns = properties.stream()
                .map(p -> "`" + p.getCode() + "`")
                .collect(Collectors.joining(", "));
        String values = properties.stream()
                .map(p -> "?")
                .collect(Collectors.joining(", "));
        return String.format("INSERT INTO `%s` (%s) VALUES (%s);", tableName, columns, values);
    }

    private String generateUpdateSQL(String tableName, List<PhysicalProperty> properties) {
        String setClause = properties.stream()
                .map(p -> "`" + p.getCode() + "` = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE `%s` SET %s WHERE `id` = ?;", tableName, setClause);
    }

    private String generateDeleteSQL(String tableName) {
        return String.format("DELETE FROM `%s` WHERE `id` = ?;", tableName);
    }

    private String generateSelectSQL(String tableName, List<PhysicalProperty> properties) {
        String columns = properties.stream()
                .map(p -> "`" + p.getCode() + "`")
                .collect(Collectors.joining(", "));
        return String.format("SELECT %s FROM `%s` WHERE `id` = ?;", columns, tableName);
    }

    private String generateCompleteSQL(PhysicalModel model, List<PhysicalProperty> properties, List<PhysicalMethod> methods) {
        StringBuilder sql = new StringBuilder();
        
        // 添加表结构
        sql.append("-- ============================================\n");
        sql.append("-- 物理模型: ").append(model != null ? model.getName() : "Unknown").append("\n");
        sql.append("-- 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        sql.append("-- ============================================\n\n");
        
        sql.append("-- 1. 创建表结构\n");
        sql.append(generateCreateTableSQL(model, properties));
        sql.append("\n\n");
        
        // 添加索引
        List<PhysicalProperty> indexedProps = properties.stream()
                .filter(p -> p.getIsIndex() != null && p.getIsIndex())
                .collect(Collectors.toList());
        if (!indexedProps.isEmpty()) {
            sql.append("-- 2. 创建索引\n");
            for (PhysicalProperty prop : indexedProps) {
                sql.append(String.format("CREATE INDEX `idx_%s` ON `%s` (`%s`);\n",
                        prop.getCode(),
                        model != null ? model.getTableName() : "table",
                        prop.getCode()));
            }
            sql.append("\n");
        }
        
        // 添加方法 SQL
        sql.append("-- 3. 方法 SQL\n");
        List<GenerateSQLResponse.MethodSQL> methodSQLs = generateMethodSQLs(model, methods, properties);
        for (GenerateSQLResponse.MethodSQL methodSQL : methodSQLs) {
            sql.append("\n-- ").append(methodSQL.getMethodType()).append(": ").append(methodSQL.getName()).append("\n");
            sql.append(methodSQL.getSql()).append("\n");
        }
        
        return sql.toString();
    }
}
