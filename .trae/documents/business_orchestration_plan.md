# 业务编排功能实现计划

## 一、需求概述

在"业务处理"菜单下新增"业务编排"功能模块，核心能力：

1. 支持对模型管理中的\*\*方法（原子服务能力）\*\*进行编排
2. 编排支持三种执行模式：**串行（SERIAL）**、**并行（PARALLEL）**、**循环（LOOP）**
3. 编排过程中选择方法时，支持编写新的**子需求**
4. 新建的子需求同时归属：原需求 + 当前业务编排

## 二、技术栈与现有约定

* **后端**：Java 17 + Spring Boot 3.2.5 + JPA/Hibernate + MySQL + Lombok

* **前端**：Vue3 + TypeScript + Element Plus + Vue Router

* **包名**：`com.zmodel`

* **Entity 约定**：UUID 主键，`@PrePersist`/`@PreUpdate` 自动维护 `createdAt`/`updatedAt`

* **DTO 约定**：响应 DTO 使用 `@Builder`，Service 中 `toDTO()` 转换

* **Controller 约定**：`@RestController` + `@RequestMapping`，返回 `ApiResponse<T>`

* **前端 API 约定**：统一 `request` 封装，导出 typed API 对象

* **前端视图约定**：列表 + 详情 + 设计器三页面模式

## 三、数据库设计

### 3.1 表结构

#### `business_orchestration`（业务编排主表）

| 字段          | 类型           | 说明                          |
| ----------- | ------------ | --------------------------- |
| id          | VARCHAR(36)  | 主键 UUID                     |
| name        | VARCHAR(200) | 编排名称                        |
| code        | VARCHAR(100) | 编排编码，唯一                     |
| description | TEXT         | 描述                          |
| status      | VARCHAR(20)  | 状态：DRAFT/PUBLISHED/ARCHIVED |
| version     | INT          | 版本号，默认1                     |
| created\_at | DATETIME     | 创建时间                        |
| updated\_at | DATETIME     | 更新时间                        |

#### `orchestration_node`（编排节点表）

| 字段                | 类型           | 说明                        |
| ----------------- | ------------ | ------------------------- |
| id                | VARCHAR(36)  | 主键 UUID                   |
| orchestration\_id | VARCHAR(36)  | 编排ID（外键）                  |
| node\_type        | VARCHAR(20)  | 节点类型：SERIAL/PARALLEL/LOOP |
| node\_name        | VARCHAR(200) | 节点名称                      |
| description       | TEXT         | 节点描述                      |
| sort\_order       | INT          | 排序号（同一编排内的顺序）             |
| loop\_count       | INT          | 循环次数（仅 LOOP 类型）           |
| created\_at       | DATETIME     | 创建时间                      |

#### `orchestration_node_method`（节点-方法关联表）

| 字段              | 类型          | 说明                         |
| --------------- | ----------- | -------------------------- |
| id              | VARCHAR(36) | 主键 UUID                    |
| node\_id        | VARCHAR(36) | 节点ID（外键）                   |
| method\_id      | VARCHAR(36) | 方法ID（外键 → method.id）       |
| requirement\_id | VARCHAR(36) | 子需求ID（外键 → requirement.id） |
| sort\_order     | INT         | 排序号                        |
| created\_at     | DATETIME    | 创建时间                       |

#### `orchestration_requirement`（编排-需求关联表，支持双归属）

| 字段                | 类型          | 说明                        |
| ----------------- | ----------- | ------------------------- |
| id                | VARCHAR(36) | 主键 UUID                   |
| orchestration\_id | VARCHAR(36) | 编排ID（外键）                  |
| requirement\_id   | VARCHAR(36) | 需求ID（外键 → requirement.id） |
| created\_at       | DATETIME    | 创建时间                      |

### 3.2 实体关系

* `BusinessOrchestration` 1:N `OrchestrationNode`（按 sort\_order 排序）

* `OrchestrationNode` 1:N `OrchestrationNodeMethod`（节点下编排的方法）

* `BusinessOrchestration` M:N `Requirement`（通过 `orchestration_requirement`，用于双归属关联）

* `OrchestrationNodeMethod` 关联 `Method`（原子方法）和 `Requirement`（子需求）

## 四、后端实现

### 4.1 新增文件清单

**Entity 层**

* `entity/BusinessOrchestration.java`

* `entity/OrchestrationNode.java`

* `entity/OrchestrationNodeMethod.java`

* `entity/OrchestrationRequirement.java`

**Repository 层**

* `repository/BusinessOrchestrationRepository.java`

* `repository/OrchestrationNodeRepository.java`

* `repository/OrchestrationNodeMethodRepository.java`

* `repository/OrchestrationRequirementRepository.java`

**DTO 层**

* `dto/request/OrchestrationCreateRequest.java`

* `dto/request/OrchestrationUpdateRequest.java`

* `dto/request/OrchestrationNodeRequest.java`

* `dto/request/OrchestrationNodeMethodRequest.java`

* `dto/response/OrchestrationDTO.java`

* `dto/response/OrchestrationNodeDTO.java`

* `dto/response/OrchestrationNodeMethodDTO.java`

* `dto/response/OrchestrationSummaryDTO.java`

**Service 层**

* `service/OrchestrationService.java`

**Controller 层**

* `controller/OrchestrationController.java`

### 4.2 核心业务逻辑

#### 4.2.1 编排 CRUD

* 创建编排：自动生成编码 `ORCH-0001`，状态为 DRAFT

* 更新编排：支持更新名称、描述、状态

* 删除编排：级联删除节点、方法绑定、需求关联

* 获取列表：分页 + 关键字搜索（按名称）

#### 4.2.2 节点管理

* 节点按 `sort_order` 排序

* 支持三种节点类型：

  * **SERIAL（串行）**：节点内方法按顺序执行，前一个完成后执行下一个

  * **PARALLEL（并行）**：节点内方法同时执行

  * **LOOP（循环）**：节点内方法重复执行 N 次

* 支持节点的增删改操作

#### 4.2.3 方法绑定与子需求创建

* 选择方法时，可同时创建新子需求

* 子需求创建逻辑：

  1. 创建 Requirement（type=SUB，parentId=原主需求ID）
  2. 创建 OrchestrationRequirement 关联（双归属）
  3. 创建 MethodRequirement 关联（方法 ↔ 子需求）
  4. 创建 OrchestrationNodeMethod 记录

* 如果方法已关联子需求，可直接选择已有子需求

#### 4.2.4 编排校验

* 循环检测：不允许 A→B→C→A 的循环依赖

* 空编排检测：至少包含一个节点

* 节点完整性：每个节点至少包含一个方法

### 4.3 API 端点设计

| HTTP   | 路径                                                           | 说明             |
| ------ | ------------------------------------------------------------ | -------------- |
| GET    | `/api/orchestrations`                                        | 分页获取编排列表       |
| GET    | `/api/orchestrations/{id}`                                   | 获取编排详情（含节点树）   |
| POST   | `/api/orchestrations`                                        | 创建编排           |
| PUT    | `/api/orchestrations/{id}`                                   | 更新编排基本信息       |
| DELETE | `/api/orchestrations/{id}`                                   | 删除编排           |
| POST   | `/api/orchestrations/{id}/nodes`                             | 添加节点           |
| PUT    | `/api/orchestrations/{id}/nodes/{nodeId}`                    | 更新节点           |
| DELETE | `/api/orchestrations/{id}/nodes/{nodeId}`                    | 删除节点           |
| POST   | `/api/orchestrations/{id}/nodes/{nodeId}/methods`            | 为节点添加方法（可带子需求） |
| DELETE | `/api/orchestrations/{id}/nodes/{nodeId}/methods/{methodId}` | 移除节点方法         |
| PUT    | `/api/orchestrations/{id}/nodes/sort`                        | 更新节点排序         |
| GET    | `/api/orchestrations/{id}/requirements`                      | 获取编排关联的需求列表    |

## 五、前端实现

### 5.1 新增文件清单

**API 层**

* `api/orchestration.ts`

**视图层**

* `views/orchestration/OrchestrationList.vue`（编排列表页）

* `views/orchestration/OrchestrationDetail.vue`（编排详情/预览页）

* `views/orchestration/OrchestrationDesigner.vue`（可视化编排设计器）

### 5.2 修改文件清单

* `router/index.ts`（新增编排路由）

* `App.vue`（侧边栏新增菜单项 + 顶部标题映射 + 新建按钮）

### 5.3 路由设计

| 路径                         | 组件                        | 说明         |
| -------------------------- | ------------------------- | ---------- |
| `/orchestrations`          | OrchestrationList.vue     | 编排列表       |
| `/orchestrations/create`   | OrchestrationDesigner.vue | 新建编排       |
| `/orchestrations/:id`      | OrchestrationDetail.vue   | 编排详情（只读预览） |
| `/orchestrations/:id/edit` | OrchestrationDesigner.vue | 编辑编排       |

### 5.4 侧边栏菜单

在"业务处理"子菜单下新增"业务编排"项：

```
业务处理
  ├── 需求管理
  ├── 模型管理
  └── 业务编排  ← 新增
```

### 5.5 各页面设计

#### 5.5.1 OrchestrationList.vue（编排列表页）

* 顶部：标题"业务编排" + 新建按钮

* 搜索框：按名称/编码搜索

* 表格列：编排名称、编码、状态、版本、节点数、方法数、创建时间、操作

* 操作列：查看详情、编辑、删除

#### 5.5.2 OrchestrationDetail.vue（编排详情页）

* 基本信息卡片：名称、编码、状态、版本、创建时间、更新时间

* 编排流程图（只读预览）：

  * 按 sort\_order 显示节点列表

  * 每个节点显示类型标签（串行/并行/循环）和节点名称

  * 节点下展示方法列表（方法名 + 所属模型）

  * LOOP 类型显示循环次数

* 关联需求区域：展示所有关联的主需求和子需求

* 执行顺序预览：用箭头展示节点执行顺序

#### 5.5.3 OrchestrationDesigner.vue（可视化编排设计器）

核心页面，布局采用**左右分栏 + 主画布**模式：

```
┌─────────────────────────────────────────────────┐
│  顶部工具栏：保存、预览、校验、返回                │
├──────────┬──────────────────────────────────────┤
│ 左侧面板  │  主画布（编排设计区）                    │
│          │                                      │
│ 节点类型: │  ┌─[串行节点1]─┐                     │
│ ┌──────┐ │  │ 方法A → 方法B │                    │
│ │ 串行 │ │  └──────────────┘                     │
│ ├──────┤ │          │                            │
│ │ 并行 │ │  ┌─[并行节点2]─┐                     │
│ ├──────┤ │  │ ┌─方法C─┐  │                     │
│ │ 循环 │ │  │ └───────┘  │                     │
│ └──────┘ │  │ ┌─方法D─┐  │                     │
│          │  │ └───────┘  │                     │
│ 方法库:   │  └──────────────┘                     │
│ ┌──────┐ │          │                            │
│ │模型M1│ │  ┌─[循环节点3]─┐                     │
│ │方法A │ │  │ 方法E ×3次   │                    │
│ │方法B │ │  └──────────────┘                     │
│ └──────┘ │                                      │
├──────────┴──────────────────────────────────────┤
│  右侧属性面板（选中节点时显示）                      │
│  节点名称、节点类型、循环次数、方法绑定列表            │
└─────────────────────────────────────────────────┘
```

**交互设计：**

1. **节点操作**：点击左侧节点类型 → 在画布添加新节点（追加到末尾）
2. **节点编辑**：点击画布中的节点 → 右侧面板编辑属性
3. **方法绑定**：

   * 点击节点内的"+ 选择方法"按钮

   * 弹出方法选择弹窗（按模型分组展示方法列表）

   * 选择方法后，弹出"子需求创建"弹窗：

     * 显示方法关联的主需求

     * 输入子需求名称（自动带前缀）

     * 提交后自动创建子需求 + 双归属关联

   * 也可选择已存在的子需求
4. **节点排序**：拖拽节点卡片调整顺序
5. **删除操作**：节点右上角 × 按钮删除节点
6. **执行模式标识**：

   * 串行：→ 箭头连线

   * 并行：分叉连线（同时执行）

   * 循环：圆环标识 + 次数

**子需求创建弹窗：**

```
┌─ 创建子需求 ──────────────────┐
│  关联方法：方法A (模型: 订单模型)  │
│  原主需求：订单支付              │
│  ───────────────────────────  │
│  子需求名称：[                  ]│
│  子需求编码：[自动生成          ]│
│  需求描述：[                  ]│
│  ───────────────────────────  │
│  ⚠ 此子需求将同时归属：         │
│    · 原需求「订单支付」          │
│    · 当前业务编排「xxx」        │
│  ───────────────────────────  │
│         [取消]    [确认创建]     │
└──────────────────────────────┘
```

### 5.6 状态管理

* `designData` reactive 对象，包含：

  * `id`（编排ID，新建时为空）

  * `name`, `code`, `description`, `status`

  * `nodes`: 节点数组，每个节点包含 `id`, `nodeType`, `nodeName`, `description`, `sortOrder`, `loopCount`, `methods[]`

* 画布操作均基于本地状态，保存时一次性提交

## 六、实施步骤

### 第一阶段：后端数据层（Entity + Repository）

1. 创建 `BusinessOrchestration` 实体
2. 创建 `OrchestrationNode` 实体
3. 创建 `OrchestrationNodeMethod` 实体
4. 创建 `OrchestrationRequirement` 实体
5. 创建对应 Repository

### 第二阶段：后端业务层（DTO + Service + Controller）

1. 创建请求/响应 DTO
2. 实现 `OrchestrationService`（CRUD、节点管理、方法绑定、子需求创建）
3. 实现 `OrchestrationController`

### 第三阶段：前端页面

1. 创建 `api/orchestration.ts` API 层
2. 创建 `OrchestrationList.vue` 列表页
3. 创建 `OrchestrationDetail.vue` 详情/预览页
4. 创建 `OrchestrationDesigner.vue` 可视化设计器

### 第四阶段：集成

1. 更新 `router/index.ts` 添加编排路由
2. 更新 `App.vue` 添加侧边栏菜单
3. 编译验证（后端 `mvn compile`，前端 `GetDiagnostics`）

## 七、风险与注意事项

1. **子需求双归属**：`orchestration_requirement` 表实现子需求同时属于原需求和编排，查询时需注意去重
2. **节点排序**：使用 `sort_order` 字段而非邻接表，简化实现；拖拽排序时批量更新
3. **循环依赖检测**：第一阶段不实现自动循环检测（因为节点是线性结构，天然无循环），后续扩展为图结构时再添加
4. **级联删除**：删除编排时需清理所有关联的节点、方法绑定、需求关联
5. **事务管理**：子需求创建涉及多表写入，需使用 `@Transactional` 保证一致性
6. **前端可视化**：由于不引入 vue-flow 等新依赖，采用**卡片式节点 + 箭头连线**的简化设计，满足核心编排需求
7. **编码生成**：编排编码格式 `ORCH-XXXX`，节点内部方法子需求编码格式 `SUB-ORCH-XXXX`

