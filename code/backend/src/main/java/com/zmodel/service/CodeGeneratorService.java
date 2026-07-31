package com.zmodel.service;

import com.zmodel.dto.response.FunctionalOrchestrationDTO;
import com.zmodel.dto.response.GeneratedCodeResponse;
import com.zmodel.entity.FoNode;
import com.zmodel.entity.FoNodeConfig;
import com.zmodel.entity.FoNodeMethod;
import com.zmodel.entity.PhysicalModel;
import com.zmodel.entity.PhysicalProperty;
import com.zmodel.entity.PhysicalMethod;
import com.zmodel.repository.FoNodeConfigRepository;
import com.zmodel.repository.FoNodeMethodRepository;
import com.zmodel.repository.FoNodeRepository;
import com.zmodel.repository.PhysicalMethodRepository;
import com.zmodel.repository.PhysicalModelRepository;
import com.zmodel.repository.PhysicalPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final PhysicalModelRepository physicalModelRepository;
    private final PhysicalPropertyRepository physicalPropertyRepository;
    private final PhysicalMethodRepository physicalMethodRepository;
    private final FoNodeRepository foNodeRepository;
    private final FoNodeMethodRepository foNodeMethodRepository;
    private final FoNodeConfigRepository foNodeConfigRepository;

    public GeneratedCodeResponse generateCode(FunctionalOrchestrationDTO dto) {
        GeneratedCodeResponse response = new GeneratedCodeResponse();
        response.setOrchestrationId(dto.getId());
        response.setGeneratedTime(java.time.LocalDateTime.now());

        // 生成实体类代码
        response.setEntityCode(generateEntityCode(dto));

        // 生成 Mapper 接口
        response.setMapperCode(generateMapperCode(dto));

        // 生成 Service 类
        response.setServiceCode(generateServiceCode(dto));

        // 生成 Service 接口
        response.setServiceInterfaceCode(generateServiceInterfaceCode(dto));

        // 生成 Controller
        response.setControllerCode(generateControllerCode(dto));

        // 生成核心编排逻辑
        response.setOrchestrationCode(generateOrchestrationCode(dto));

        return response;
    }

    public GeneratedCodeResponse generateCodeByOrchestrationId(String orchestrationId) {
        // 从数据库加载完整数据
        List<FoNode> nodes = foNodeRepository.findByOrchestrationIdOrderBySortOrderAsc(orchestrationId);
        
        // 构建 DTO
        FunctionalOrchestrationDTO dto = new FunctionalOrchestrationDTO();
        dto.setId(orchestrationId);
        dto.setName("GeneratedOrchestration");
        
        List<FunctionalOrchestrationDTO.FoNodeDTO> nodeDTOs = nodes.stream()
                .map(this::convertNodeToDTO)
                .collect(Collectors.toList());
        dto.setNodes(nodeDTOs);
        
        return generateCode(dto);
    }

    private FunctionalOrchestrationDTO.FoNodeDTO convertNodeToDTO(FoNode node) {
        FunctionalOrchestrationDTO.FoNodeDTO dto = new FunctionalOrchestrationDTO.FoNodeDTO();
        dto.setId(node.getId());
        dto.setNodeType(node.getNodeType());
        dto.setLabel(node.getNodeName());
        dto.setNodeName(node.getNodeName());
        dto.setSortOrder(node.getSortOrder());
        dto.setX(node.getX());
        dto.setY(node.getY());
        dto.setPhysicalModelId(node.getPhysicalModelId());
        
        // 加载方法
        List<FoNodeMethod> methods = foNodeMethodRepository.findByNodeIdOrderBySortOrderAsc(node.getId());
        dto.setMethods(methods.stream().map(m -> {
            FunctionalOrchestrationDTO.FoNodeMethodDTO methodDTO = new FunctionalOrchestrationDTO.FoNodeMethodDTO();
            methodDTO.setId(m.getId());
            methodDTO.setMethodId(m.getMethodId());
            methodDTO.setPhysicalMethodId(m.getPhysicalMethodId());
            methodDTO.setSortOrder(m.getSortOrder());
            return methodDTO;
        }).collect(Collectors.toList()));
        
        // 加载配置
        foNodeConfigRepository.findFirstByNodeId(node.getId()).ifPresent(config -> {
            FunctionalOrchestrationDTO.FoNodeConfigDTO configDTO = new FunctionalOrchestrationDTO.FoNodeConfigDTO();
            configDTO.setConfigKey(config.getConfigKey());
            configDTO.setConfigValue(config.getConfigValue());
            dto.setNodeConfig(configDTO);
        });
        
        return dto;
    }

    private String generateEntityCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        code.append("package cn.zhangquanyu.entity;\n\n");
        code.append("import com.baomidou.mybatisplus.annotation.*;\n");
        code.append("import lombok.Data;\n");
        code.append("import java.time.LocalDateTime;\n");
        code.append("import java.io.Serializable;\n\n");
        code.append("@Data\n");
        code.append("@TableName(\"\" + getMainTableName(dto) + \"\")\n");
        code.append("public class ").append(getMainEntityName(dto)).append(" implements Serializable {\n\n");
        code.append("    private static final long serialVersionUID = 1L;\n\n");
        code.append("    @TableId(type = IdType.ASSIGN_UUID)\n");
        code.append("    private String id;\n\n");
        
        // 从数据库读写节点的物理模型生成字段
        List<PhysicalModel> physicalModels = getInvolvedPhysicalModels(dto);
        for (PhysicalModel pm : physicalModels) {
            List<PhysicalProperty> properties = physicalPropertyRepository.findByPhysicalModelId(pm.getId());
            for (PhysicalProperty prop : properties) {
                if (!"id".equals(prop.getCode())) {
                    code.append("    /**\n");
                    code.append("     * ").append(prop.getDescription() != null ? prop.getDescription() : prop.getName()).append("\n");
                    code.append("     */\n");
                    code.append("    private ").append(mapDbTypeToJavaType(prop.getDbType())).append(" ")
                            .append(prop.getCode()).append(";\n\n");
                }
            }
        }
        
        code.append("    @TableField(fill = FieldFill.INSERT)\n");
        code.append("    private LocalDateTime createdAt;\n\n");
        code.append("    @TableField(fill = FieldFill.INSERT_UPDATE)\n");
        code.append("    private LocalDateTime updatedAt;\n");
        code.append("}\n");
        
        return code.toString();
    }

    private String generateMapperCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        code.append("package cn.zhangquanyu.mapper;\n\n");
        code.append("import cn.zhangquanyu.entity.").append(getMainEntityName(dto)).append(";\n");
        code.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n");
        code.append("import org.apache.ibatis.annotations.Mapper;\n\n");
        code.append("@Mapper\n");
        code.append("public interface ").append(getMainEntityName(dto)).append("Mapper extends BaseMapper<")
                .append(getMainEntityName(dto)).append("> {\n");
        code.append("}\n");
        return code.toString();
    }

    private String generateServiceInterfaceCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        code.append("package cn.zhangquanyu.service;\n\n");
        code.append("import cn.zhangquanyu.entity.").append(getMainEntityName(dto)).append(";\n");
        code.append("import com.baomidou.mybatisplus.extension.service.IService;\n\n");
        code.append("public interface ").append(getMainEntityName(dto)).append("Service extends IService<")
                .append(getMainEntityName(dto)).append("> {\n");
        code.append("}\n");
        return code.toString();
    }

    private String generateServiceCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        String entityName = getMainEntityName(dto);
        
        code.append("package cn.zhangquanyu.service.impl;\n\n");
        code.append("import cn.zhangquanyu.entity.").append(entityName).append(";\n");
        code.append("import cn.zhangquanyu.mapper.").append(entityName).append("Mapper;\n");
        code.append("import cn.zhangquanyu.service.").append(entityName).append("Service;\n");
        code.append("import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n");
        code.append("import org.springframework.stereotype.Service;\n\n");
        code.append("@Service\n");
        code.append("public class ").append(entityName).append("ServiceImpl extends ServiceImpl<")
                .append(entityName).append("Mapper, ").append(entityName).append("> implements ")
                .append(entityName).append("Service {\n");
        code.append("}\n");
        return code.toString();
    }

    private String generateControllerCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        String entityName = getMainEntityName(dto);
        String basePath = getControllerPath(dto);
        
        code.append("package cn.zhangquanyu.controller;\n\n");
        code.append("import cn.zhangquanyu.entity.").append(entityName).append(";\n");
        code.append("import cn.zhangquanyu.service.").append(entityName).append("Service;\n");
        code.append("import com.baomidou.mybatisplus.core.metadata.IPage;\n");
        code.append("import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n");
        code.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        code.append("import org.springframework.http.ResponseEntity;\n");
        code.append("import org.springframework.web.bind.annotation.*;\n\n");
        code.append("@RestController\n");
        code.append("@RequestMapping(\"").append(basePath).append("\")\n");
        code.append("public class ").append(entityName).append("Controller {\n\n");
        code.append("    @Autowired\n");
        code.append("    private ").append(entityName).append("Service service;\n\n");
        
        // CRUD 接口
        code.append("    @GetMapping\n");
        code.append("    public ResponseEntity<IPage<").append(entityName).append(">> list(\n");
        code.append("            @RequestParam(defaultValue = \"0\") int page,\n");
        code.append("            @RequestParam(defaultValue = \"10\") int size) {\n");
        code.append("        return ResponseEntity.ok(service.page(new Page<>(page, size)));\n");
        code.append("    }\n\n");
        
        code.append("    @GetMapping(\"/{id}\")\n");
        code.append("    public ResponseEntity<").append(entityName).append(">> getById(@PathVariable String id) {\n");
        code.append("        return ResponseEntity.ok(service.getById(id));\n");
        code.append("    }\n\n");
        
        code.append("    @PostMapping\n");
        code.append("    public ResponseEntity<").append(entityName).append(">> create(@RequestBody ").append(entityName).append(" entity) {\n");
        code.append("        service.save(entity);\n");
        code.append("        return ResponseEntity.ok(entity);\n");
        code.append("    }\n\n");
        
        code.append("    @PutMapping(\"/{id}\")\n");
        code.append("    public ResponseEntity<").append(entityName).append(">> update(@PathVariable String id, @RequestBody ").append(entityName).append(" entity) {\n");
        code.append("        entity.setId(id);\n");
        code.append("        service.updateById(entity);\n");
        code.append("        return ResponseEntity.ok(entity);\n");
        code.append("    }\n\n");
        
        code.append("    @DeleteMapping(\"/{id}\")\n");
        code.append("    public ResponseEntity<Void> delete(@PathVariable String id) {\n");
        code.append("        service.removeById(id);\n");
        code.append("        return ResponseEntity.ok().build();\n");
        code.append("    }\n");
        
        code.append("}\n");
        return code.toString();
    }

    private String generateOrchestrationCode(FunctionalOrchestrationDTO dto) {
        StringBuilder code = new StringBuilder();
        code.append("    /**\n");
        code.append("     * 功能编排逻辑\n");
        code.append("     */\n");
        code.append("    public void executeOrchestration(").append(getMainEntityName(dto)).append(" input) {\n");
        
        List<FunctionalOrchestrationDTO.FoNodeDTO> nodes = dto.getNodes();
        if (nodes != null) {
            for (FunctionalOrchestrationDTO.FoNodeDTO node : nodes) {
                code.append(generateNodeExecution(node));
            }
        }
        
        code.append("    }\n");
        return code.toString();
    }

    private String generateNodeExecution(FunctionalOrchestrationDTO.FoNodeDTO node) {
        StringBuilder code = new StringBuilder();
        String indent = "        ";
        String nodeName = node.getNodeType() + "_" + node.getSortOrder();
        
        switch (node.getNodeType()) {
            case "DB_READ":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // 执行数据库读取\n");
                code.append(indent).append("    // TODO: 实现具体的查询逻辑\n");
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"DB_READ节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"数据库读取失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            case "DB_WRITE":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // 执行数据库写入\n");
                code.append(indent).append("    // TODO: 实现具体的写入逻辑\n");
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"DB_WRITE节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"数据库写入失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            case "API_CALL":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // 调用外部API\n");
                if (node.getNodeConfig() != null && "api_url".equals(node.getNodeConfig().getConfigKey())) {
                    code.append(indent).append("    String apiUrl = \"").append(node.getNodeConfig().getConfigValue()).append("\";\n");
                }
                code.append(indent).append("    // TODO: 实现HTTP调用逻辑\n");
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"API_CALL节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"API调用失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            case "TRANSFORM":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // 数据转换处理\n");
                code.append(indent).append("    // TODO: 实现数据转换逻辑\n");
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"TRANSFORM节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"数据转换失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            case "CONDITION":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // 条件判断\n");
                if (node.getNodeConfig() != null && "condition_expression".equals(node.getNodeConfig().getConfigKey())) {
                    code.append(indent).append("    boolean condition = ").append(node.getNodeConfig().getConfigValue()).append(";\n");
                    code.append(indent).append("    if (!condition) {\n");
                    code.append(indent).append("        // 条件不满足，跳过后续处理\n");
                    code.append(indent).append("        return;\n");
                    code.append(indent).append("    }\n");
                }
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"CONDITION节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"条件判断失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            case "CUSTOM":
                code.append(indent).append("// 节点: ").append(node.getLabel() != null ? node.getLabel() : nodeName).append("\n");
                code.append(indent).append("// 自定义节点，请根据需要实现具体逻辑\n");
                code.append(indent).append("try {\n");
                code.append(indent).append("    // TODO: 实现自定义逻辑\n");
                code.append(indent).append("} catch (Exception e) {\n");
                code.append(indent).append("    log.error(\"CUSTOM节点执行失败: \" + e.getMessage(), e);\n");
                code.append(indent).append("    throw new RuntimeException(\"自定义节点执行失败\", e);\n");
                code.append(indent).append("}\n\n");
                break;
                
            default:
                code.append(indent).append("// 节点: ").append(node.getNodeType()).append("\n");
                code.append(indent).append("// 暂未实现的节点类型\n\n");
                break;
        }
        
        return code.toString();
    }

    private List<PhysicalModel> getInvolvedPhysicalModels(FunctionalOrchestrationDTO dto) {
        Set<String> physicalModelIds = new HashSet<>();
        
        if (dto.getNodes() != null) {
            for (FunctionalOrchestrationDTO.FoNodeDTO node : dto.getNodes()) {
                if (node.getMethods() != null) {
                    for (FunctionalOrchestrationDTO.FoNodeMethodDTO method : node.getMethods()) {
                        if (method.getPhysicalModelId() != null) {
                            physicalModelIds.add(method.getPhysicalModelId());
                        }
                    }
                }
            }
        }
        
        return physicalModelRepository.findAllById(physicalModelIds);
    }

    private String getMainTableName(FunctionalOrchestrationDTO dto) {
        String name = dto.getName() != null ? dto.getName() : "main_entity";
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase() + "_table";
    }

    private String getMainEntityName(FunctionalOrchestrationDTO dto) {
        String name = dto.getName() != null ? dto.getName() : "MainEntity";
        // 驼峰命名
        return name.substring(0, 1).toUpperCase() + name.substring(1).replaceAll("([a-z])([A-Z])", "$1$2");
    }

    private String getControllerPath(FunctionalOrchestrationDTO dto) {
        String name = dto.getName() != null ? dto.getName() : "main";
        return "/api/" + name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

    private String mapDbTypeToJavaType(String dbType) {
        if (dbType == null) return "String";
        switch (dbType.toUpperCase()) {
            case "INT":
            case "TINYINT":
            case "SMALLINT":
                return "Integer";
            case "BIGINT":
                return "Long";
            case "DECIMAL":
            case "DOUBLE":
            case "FLOAT":
                return "java.math.BigDecimal";
            case "BOOLEAN":
                return "Boolean";
            case "DATE":
                return "LocalDate";
            case "DATETIME":
                return "LocalDateTime";
            case "JSON":
                return "com.fasterxml.jackson.databind.JsonNode";
            default:
                return "String";
        }
    }
}
