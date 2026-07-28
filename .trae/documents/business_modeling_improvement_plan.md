# Z-Model 业务建模系统优化计划

> **定位**：面向业务/产品人员的业务建模平台，辅助需求梳理与业务流程可视化

---

## 一、现状分析

### 1.1 当前系统定位

Z-Model 当前定位为一个**需求驱动的模型数据管理系统**，核心能力：
- 需求管理（创建/审批/层级结构）
- 模型管理（模型/属性/方法的CRUD）
- 关联追溯（需求↔模型↔属性↔方法的关联）
- 事件跟踪（业务事件流水记录）

### 1.2 目标用户痛点

**业务人员/产品经理在梳理需求时面临的核心痛点**：

| 痛点 | 具体表现 |
|------|---------|
| **说不清业务流程** | 用文字描述复杂流程，容易产生歧义，开发理解偏差 |
| **需求与实现脱节** | 业务需求文档无法直接映射到系统模型，需要"翻译" |
| **跨部门沟通困难** | 不同部门对同一业务的理解不一致，缺乏统一语言 |
| **需求变更影响不明** | 修改一个需求，无法快速定位影响哪些模型和属性 |
| **缺乏结构化梳理** | 需求散落在文档、邮件、会议纪要中，难以系统梳理 |

### 1.3 核心差距

当前 Z-Model 与**业务建模平台**的核心差距：

| 能力维度 | 当前状态 | 业务建模平台标准 |
|---------|---------|----------------|
| **业务流程图** | ❌ 无 | ✅ BPMN拖拽建模、泳道图、流程仿真 |
| **事件风暴** | ❌ 无 | ✅ 协作白板、便利贴建模、AI辅助 |
| **业务术语表** | ❌ 无 | ✅ 统一语言、术语定义、业务字典 |
| **场景建模** | ❌ 无 | ✅ 用户故事地图、业务场景描述 |
| **需求可视化** | ⚠️ 列表展示 | ✅ 需求关系图、依赖图、优先级矩阵 |
| **流程-模型关联** | ❌ 断裂 | ✅ 流程节点↔模型↔属性的完整追溯链 |
| **协作编辑** | ❌ 无 | ✅ 实时协作、评论、版本管理 |

---

## 二、优化方向与目标

### 2.1 核心定位升级

```
当前：需求数据管理系统（CRUD为主）
      ↓
目标：业务建模平台（可视化 + 协作 + 追溯）
```

### 2.2 核心能力目标

| 目标能力 | 描述 | 对标平台 |
|---------|------|---------|
| **BPMN流程建模** | 拖拽式流程图设计，让业务人员能"画出"业务流程 | Bizagi, bpmn.js |
| **事件风暴白板** | 协作式建模工作坊，快速梳理业务领域 | Qlerify, Miro |
| **业务术语管理** | 建立统一语言，消除沟通歧义 | 所有DDD/建模平台 |
| **流程-模型追溯** | 流程图节点关联模型属性，实现端到端追溯 | ContextMapper |
| **需求可视化地图** | 需求依赖关系图、优先级矩阵 | Miro, Aha! |

### 2.3 用户旅程重塑

**业务人员的典型使用流程（优化后）**：

```
1. 梳理业务流程 → BPMN流程图（拖拽设计）
       ↓
2. 识别业务事件 → 事件风暴（便利贴协作）
       ↓
3. 定义业务术语 → 术语表管理
       ↓
4. 拆解需求模型 → 需求↔流程节点自动关联
       ↓
5. 生成系统模型 → 模型/属性/方法自动映射
       ↓
6. 全链路追溯 → 需求→流程→模型→属性→方法
```

---

## 三、优化计划（分阶段实施）

### 第一阶段：BPMN业务流程图建模（优先级：最高）

**目标**：让业务人员能可视化地设计和讨论业务流程

#### 3.1.1 BPMN流程设计器集成

**功能**：拖拽式BPMN 2.0流程图设计器

**技术方案**：
- 前端集成 **bpmn.js**（Camunda开源BPMN渲染器）
- Vue3组件封装 `BpmnDesigner.vue`
- 支持保存BPMN XML格式

**核心元素支持**：
| 元素类型 | 说明 | 业务场景 |
|---------|------|---------|
| 开始/结束事件 | 流程起止点 | 业务发起与完成 |
| 用户任务 | 需要人工处理的步骤 | 审批、审核、填写表单 |
| 服务任务 | 系统自动执行的步骤 | 发送通知、数据同步 |
| 排他网关 | 条件判断分支 | 审核通过/驳回 |
| 并行网关 | 同时执行多个分支 | 同时通知多个部门 |
| 序列流 | 流程连线 | 业务流转方向 |
| 泳道 | 角色/部门分区 | 多角色协作流程 |

**涉及模块**：
- 前端：新增 `src/views/process/` 目录
  - `ProcessList.vue` - 流程图列表
  - `ProcessDesigner.vue` - 流程设计器
  - `ProcessDetail.vue` - 流程详情
- 后端：新增 `process` 模块
  - `ProcessController` - 流程CRUD API
  - `ProcessService` - 流程业务逻辑
  - 数据库表：`bpmn_process`、`bpmn_process_version`

#### 3.1.2 流程与模型关联

**功能**：将BPMN流程节点关联到业务模型

**关联关系**：
```
BPMN节点 → 业务模型 → 模型属性/方法
例：
  "订单审核"节点 → 订单模型 → 审核状态、审核人、审核时间
  "发送通知"节点 → 通知模型 → 通知类型、接收人
```

**技术方案**：
- 在BPMN节点扩展属性中存储关联ID
- 后端新增流程节点与模型的关联表
- 前端在流程设计器中支持"绑定模型"操作

**涉及模块**：
- 数据库：`process_node_model` 关联表
- 前端：`ProcessDesigner.vue` 增加属性面板
- 后端：新增关联管理接口

#### 3.1.3 流程版本管理

**功能**：流程修改历史记录和版本对比

**设计**：
- 自动保存每次修改的BPMN XML快照
- 支持版本间差异对比（节点增删改）
- 支持回滚到历史版本

---

### 第二阶段：事件风暴与业务术语（优先级：高）

**目标**：提供结构化的业务需求梳理工具

#### 3.2.1 事件风暴白板

**功能**：协作式便利贴建模，快速发现业务领域中的关键事件

**设计思路**：
- 参考 Qlerify 的事件风暴三阶段模型
- 支持三种建模模式：

| 阶段 | 目标 | 便利贴类型 |
|------|------|----------|
| Big Picture | 探索整个业务领域 | 领域事件（粉色）、问题（蓝色）、机会（黄色） |
| Process Modeling | 聚焦特定流程 | 领域事件、命令（蓝色）、角色（橙色） |
| Software Design | 落到系统设计 | 领域事件、命令、聚合（黄色）、读模型（绿色） |

**技术方案**：
- 轻量级实现：基于 HTML5 拖拽 + Canvas/SVG
- 存储方式：JSON格式存储白板状态
- 支持保存为模板

**涉及模块**：
- 前端：`src/views/风暴/` 目录
  - `StormBoard.vue` - 风暴白板组件
  - `StormTemplate.vue` - 模板管理
- 后端：`storm` 模块
  - `StormBoardController`
  - 数据库：`storm_board`、`storm_sticky`

#### 3.2.2 业务术语表

**功能**：建立项目级的统一语言体系

**数据库设计**：
```sql
business_term 表:
  - id, term_name, term_code, definition
  - category（术语分类：实体/动作/规则/状态）
  - related_terms（关联术语，JSON）
  - owner（负责人）
  - status（草稿/已确认/已废弃）

business_term_reference 表:
  - term_id, reference_type（需求/模型/属性/方法）
  - reference_id
```

**API设计**：
- `GET /api/terms` - 术语列表（支持分类筛选）
- `POST /api/terms` - 创建术语
- `PUT /api/terms/{id}` - 更新术语
- `GET /api/terms/graph` - 术语关系图
- `POST /api/terms/{id}/bind` - 绑定到需求/模型

**前端页面**：
- 术语列表页（分类管理）
- 术语详情页（定义、关联、引用位置）
- 术语关系图（可视化展示术语网络）
- 在需求/模型详情页支持"引用术语"

**使用场景**：
- 业务人员在梳理需求时，先定义术语，再用术语描述需求
- 开发人员通过术语表快速理解业务含义
- 确保所有人对同一概念使用相同的名称

#### 3.2.3 业务场景描述

**功能**：结构化的业务场景/用例描述模板

**场景模板**：
```
场景名称：
场景描述：
参与者：
前置条件：
主要流程：
  1. ...
  2. ...
异常流程：
业务规则：
关联需求：
关联流程：
```

**涉及模块**：
- 数据库：`business_scenario` 表
- 前端：`ScenarioList.vue`、`ScenarioDetail.vue`
- 后端：`ScenarioController`

---

### 第三阶段：需求可视化与追溯增强（优先级：高）

**目标**：让需求及其关联关系可视化、可追溯

#### 3.3.1 需求关系图

**功能**：可视化展示需求间的依赖和层级关系

**展示内容**：
- 主需求→子需求的层级树
- 需求间的依赖关系（A需求依赖B需求）
- 需求与流程、模型的关联

**技术方案**：
- 使用 **AntV G6** 实现关系图
- 支持节点拖拽、缩放、折叠展开
- 点击节点跳转详情

**涉及模块**：
- 后端：`GET /api/requirements/graph` - 需求关系数据
- 前端：`RequirementGraph.vue` 页面

#### 3.3.2 需求依赖矩阵

**功能**：表格形式展示需求间的依赖关系

**展示内容**：
- 行：被依赖的需求
- 列：依赖方需求
- 标记：强依赖/弱依赖/无依赖

**涉及模块**：
- 前端：`RequirementMatrix.vue` 组件
- 后端：`GET /api/requirements/matrix` 接口

#### 3.3.3 全链路追溯视图

**功能**：从需求到实现的完整追溯链

**追溯路径**：
```
需求 → 业务流程 → 流程节点 → 模型 → 模型属性/方法
```

**交互方式**：
- 在需求详情页显示"追溯链"Tab
- 点击任意节点跳转对应详情
- 支持高亮显示整条链路

---

### 第四阶段：协作与效率提升（优先级：中）

**目标**：提升团队协作效率

#### 4.1 实时协作（简化版）

**功能**：多人同时编辑流程/需求

**技术方案**：
- 使用 WebSocket 实现实时同步
- 简化版：乐观锁 + 定时刷新
- 增强版：OT算法（Operational Transformation）

**涉及模块**：
- 后端：新增 `collaboration` 模块
- 前端：WebSocket客户端

#### 4.2 智能提示与模板

**功能**：降低建模门槛，提升效率

**功能点**：
- 流程模板库（审批流程、订单流程、退款流程等）
- 需求描述模板
- AI辅助：输入业务描述，自动生成流程草稿

**涉及模块**：
- 数据库：`template` 表
- 后端：`TemplateController` + AI服务集成

#### 4.3 快捷键与批量操作

**功能**：提升高频操作效率

**快捷键**：
- Ctrl+N 新建
- Ctrl+S 保存
- Ctrl+D 复制选中项
- Delete 删除
- Ctrl+Z 撤销

---

## 四、技术选型与架构

### 4.1 前端技术选型

| 功能 | 推荐方案 | 备选方案 | 说明 |
|------|---------|---------|------|
| BPMN流程设计 | **bpmn.js** | logicflow | Camunda官方，BPMN标准实现 |
| 关系图渲染 | **AntV G6** | vis.js | 蚂蚁开源，功能完善 |
| 事件风暴白板 | **HTML5拖拽** + 原生JS | Miro SDK | 轻量自研，避免依赖第三方 |
| 协作编辑 | **WebSocket** + 乐观锁 | OT算法 | 简化版足够使用 |

### 4.2 后端技术选型

| 功能 | 推荐方案 | 说明 |
|------|---------|------|
| BPMN存储 | 存储BPMN XML为LONGTEXT | 保持完整信息 |
| 白板存储 | JSON格式存储节点/连线 | 灵活可扩展 |
| 实时协作 | Spring WebSocket | 与Spring Boot无缝集成 |

### 4.3 数据库新增表

```sql
-- BPMN流程表
CREATE TABLE bpmn_process (
  id BIGINT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  code VARCHAR(100) NOT NULL,
  description TEXT,
  bpmn_xml LONGTEXT,
  version INT DEFAULT 1,
  status VARCHAR(20) DEFAULT 'DRAFT',
  created_at DATETIME,
  updated_at DATETIME
);

-- 流程版本表
CREATE TABLE bpmn_process_version (
  id BIGINT PRIMARY KEY,
  process_id BIGINT,
  version INT,
  bpmn_xml LONGTEXT,
  change_note VARCHAR(500),
  created_at DATETIME
);

-- 流程节点与模型关联表
CREATE TABLE process_node_model (
  id BIGINT PRIMARY KEY,
  process_id BIGINT,
  node_id VARCHAR(100),
  model_id BIGINT,
  created_at DATETIME
);

-- 业务术语表
CREATE TABLE business_term (
  id BIGINT PRIMARY KEY,
  term_name VARCHAR(200) NOT NULL,
  term_code VARCHAR(100),
  definition TEXT,
  category VARCHAR(50),
  related_terms JSON,
  owner VARCHAR(100),
  status VARCHAR(20) DEFAULT 'DRAFT',
  created_at DATETIME,
  updated_at DATETIME
);

-- 术语引用表
CREATE TABLE business_term_reference (
  id BIGINT PRIMARY KEY,
  term_id BIGINT,
  reference_type VARCHAR(50),  -- REQUIREMENT/MODEL/PROPERTY/METHOD
  reference_id BIGINT,
  created_at DATETIME
);

-- 事件风暴白板表
CREATE TABLE storm_board (
  id BIGINT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  mode VARCHAR(30),  -- BIG_PICTURE/PROCESS/SOFTWARE
  canvas_data JSON,
  created_at DATETIME,
  updated_at DATETIME
);

-- 风暴便利贴表
CREATE TABLE storm_sticky (
  id BIGINT PRIMARY KEY,
  board_id BIGINT,
  type VARCHAR(30),  -- EVENT/COMMAND/ROLE/AGGREGATE等
  content VARCHAR(500),
  position_x INT,
  position_y INT,
  color VARCHAR(20),
  created_at DATETIME
);

-- 业务场景表
CREATE TABLE business_scenario (
  id BIGINT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  actors VARCHAR(500),
  preconditions TEXT,
  main_flow TEXT,
  exception_flow TEXT,
  business_rules TEXT,
  created_at DATETIME,
  updated_at DATETIME
);
```

### 4.4 前端页面结构

```
src/views/
├── requirement/          # 需求管理（已有）
├── model/                 # 模型管理（已有）
├── method/                # 方法管理（已有）
├── property/              # 属性管理（已有）
├── event/                 # 事件管理（已有）
├── process/               # 【新增】业务流程
│   ├── ProcessList.vue
│   ├── ProcessDesigner.vue
│   └── ProcessDetail.vue
├── term/                  # 【新增】业务术语
│   ├── TermList.vue
│   ├── TermDetail.vue
│   └── TermGraph.vue
├── storm/                 # 【新增】事件风暴
│   ├── StormBoard.vue
│   └── StormTemplate.vue
├── scenario/              # 【新增】业务场景
│   ├── ScenarioList.vue
│   └── ScenarioDetail.vue
└── graph/                 # 【新增】可视化图谱
    ├── RequirementGraph.vue
    └── TraceabilityView.vue
```

---

## 五、实施依赖与优先级

### 5.1 实施顺序

```
第一阶段（BPMN流程） ──→ 第二阶段（术语+风暴） ──→ 第三阶段（可视化追溯）
                                                          │
                                                          ▼
                                            第四阶段（协作增强）
```

### 5.2 优先级矩阵

| 阶段 | 用户价值 | 实现难度 | 建议优先级 |
|------|---------|---------|----------|
| BPMN流程建模 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | **最高** |
| 业务术语表 | ⭐⭐⭐⭐ | ⭐⭐ | **高** |
| 事件风暴白板 | ⭐⭐⭐⭐ | ⭐⭐⭐ | **高** |
| 流程-模型关联 | ⭐⭐⭐⭐⭐ | ⭐⭐ | **高** |
| 需求关系图 | ⭐⭐⭐ | ⭐⭐ | 中 |
| 全链路追溯 | ⭐⭐⭐⭐ | ⭐⭐ | 中 |
| 实时协作 | ⭐⭐⭐ | ⭐⭐⭐⭐ | 低 |
| AI辅助 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 低 |

### 5.3 第一阶段详细实施项

#### Week 1-2: BPMN流程设计器基础

**后端**：
1. 创建 `bpmn_process`、`bpmn_process_version`、`process_node_model` 表
2. 实现 `ProcessController`（CRUD + 版本管理）
3. 实现BPMN XML存储/解析服务

**前端**：
1. 集成 `bpmn.js` 依赖
2. 实现 `ProcessDesigner.vue`（基础设计器）
3. 实现 `ProcessList.vue`（流程列表）
4. 实现 `ProcessDetail.vue`（流程详情）

#### Week 3-4: 流程-模型关联

**后端**：
1. 实现流程节点与模型的关联API
2. 实现追溯链查询API

**前端**：
1. 在设计器中添加属性面板
2. 实现"绑定模型"功能
3. 实现追溯链视图

---

## 六、风险与注意事项

### 6.1 技术风险

| 风险 | 应对措施 |
|------|---------|
| bpmn.js学习曲线 | 参考 bpmn.io 官方文档，使用 vue-bpmn 封装 |
| BPMN XML复杂 | 简化存储，仅支持核心元素 |
| 白板性能（大量节点） | 虚拟滚动 + 按需渲染 |

### 6.2 产品风险

| 风险 | 应对措施 |
|------|---------|
| 用户学习成本 | 提供模板和示例，内置引导教程 |
| 功能过于复杂 | 分阶段上线，先核心后增强 |
| 与现有功能重叠 | 明确各模块边界，流程/需求/术语各归其位 |

### 6.3 数据兼容

- 新增模块独立建表，不影响现有数据结构
- 流程与模型关联通过中间表实现，松耦合设计
- 术语引用支持已有实体（需求/模型/属性/方法）

### 6.4 扩展预留

- BPMN设计器预留自定义节点扩展点
- 术语表预留分类扩展字段
- 事件风暴预留多种建模模式

---

## 七、成功标准

### 7.1 第一阶段验收标准

- [ ] 业务人员可通过拖拽完成BPMN流程图设计
- [ ] 支持保存和加载BPMN XML
- [ ] 流程节点可关联到业务模型
- [ ] 支持流程版本对比和回滚
- [ ] 从需求详情页可查看关联的业务流程

### 7.2 第二阶段验收标准

- [ ] 业务人员可维护项目级术语表
- [ ] 需求/模型详情页可引用术语
- [ ] 支持事件风暴白板的基本操作
- [ ] 提供至少3个行业场景模板

### 7.3 第三阶段验收标准

- [ ] 需求关系图可正常渲染
- [ ] 全链路追溯视图完整展示
- [ ] 需求变更可快速定位影响范围