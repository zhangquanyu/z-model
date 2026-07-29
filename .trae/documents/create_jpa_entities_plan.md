# 创建 4 个 JPA 实体文件实施计划

## 概述
在 `/Users/zhangquanyu/mProject/z-model/code/backend/src/main/java/com/zmodel/entity/` 目录下创建 4 个 JPA 实体类，严格遵循现有实体（BpmnProcess.java、Method.java、ProcessNodeModel.java、MethodRequirement.java）的代码风格。

## 现有实体模式分析

| 实体 | 特征 |
|------|------|
| **BpmnProcess.java** | 完整实体：`@PrePersist`/`@PreUpdate` 双回调，`createdAt`+`updatedAt`，`@Builder.Default` 默认值 |
| **Method.java** | 完整实体：同 BpmnProcess，但无 `@Builder.Default` 字段 |
| **ProcessNodeModel.java** | 简单实体：仅 `createdAt` + `@PrePersist`，无 `updatedAt`/`@PreUpdate` |
| **MethodRequirement.java** | 极简关联实体：无时间戳、无生命周期回调 |

## 要创建的 4 个文件

### 1. BusinessOrchestration.java（参照 BpmnProcess.java 模式）
- **表名**: `business_orchestration`
- **字段**: id、name、code、description、status（默认"DRAFT"）、version（默认1）、createdAt、updatedAt
- **回调**: `@PrePersist`（设置 createdAt 和 updatedAt）、`@PreUpdate`（设置 updatedAt）
- **默认值**: status 用 `@Builder.Default` 默认为 "DRAFT"，version 用 `@Builder.Default` 默认为 1

### 2. OrchestrationNode.java（参照 ProcessNodeModel.java 模式）
- **表名**: `orchestration_node`
- **字段**: id、orchestrationId、nodeType、nodeName、description、sortOrder、loopCount、createdAt
- **回调**: 仅 `@PrePersist`（设置 createdAt），无 updatedAt

### 3. OrchestrationNodeMethod.java（参照 ProcessNodeModel.java 模式）
- **表名**: `orchestration_node_method`
- **字段**: id、nodeId、methodId、requirementId、sortOrder、createdAt
- **回调**: 仅 `@PrePersist`（设置 createdAt），无 updatedAt

### 4. OrchestrationRequirement.java（参照 ProcessNodeModel.java 模式）
- **表名**: `orchestration_requirement`
- **字段**: id、orchestrationId、requirementId、createdAt
- **回调**: 仅 `@PrePersist`（设置 createdAt），无 updatedAt

## 通用约定
- 包名: `com.zmodel.entity`
- 注解: `@Entity`、`@Table(name = ...)`、`@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`
- 所有字段使用 `@Column(name = "...")` 显式指定列名
- 主键使用 `@Id` + `@Column(name = "id", length = 36)`
- 导入: `jakarta.persistence.*`、`lombok.*`、`java.time.LocalDateTime`
- 不添加任何注释

## 执行步骤
1. 在 entity 目录下创建 BusinessOrchestration.java
2. 在 entity 目录下创建 OrchestrationNode.java
3. 在 entity 目录下创建 OrchestrationNodeMethod.java
4. 在 entity 目录下创建 OrchestrationRequirement.java
5. 验证文件创建正确
