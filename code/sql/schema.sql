CREATE TABLE IF NOT EXISTS requirement (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '需求名称',
    code VARCHAR(50) UNIQUE COMMENT '需求编号',
    description LONGTEXT COMMENT '需求描述(富文本HTML)',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态(DRAFT/PENDING/APPROVED/REJECTED)',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级(LOW/MEDIUM/HIGH/CRITICAL)',
    requirement_type VARCHAR(10) DEFAULT 'MAIN' COMMENT '需求类型(MAIN/SUB)',
    parent_id VARCHAR(36) COMMENT '父需求ID(子需求关联主需求)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_parent_id (parent_id),
    KEY idx_requirement_type (requirement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

CREATE TABLE IF NOT EXISTS model (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    code VARCHAR(50) UNIQUE COMMENT '模型编号',
    description LONGTEXT COMMENT '模型描述(富文本HTML)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型表';

CREATE TABLE IF NOT EXISTS model_requirement (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    model_id VARCHAR(36) NOT NULL COMMENT '模型ID',
    requirement_id VARCHAR(36) NOT NULL COMMENT '需求ID',
    UNIQUE KEY uk_model_requirement (model_id, requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型需求关联表';

CREATE TABLE IF NOT EXISTS property (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '属性名称',
    code VARCHAR(50) COMMENT '属性编码',
    data_type VARCHAR(20) NOT NULL COMMENT '数据类型(STRING/INTEGER/LONG/DOUBLE/BOOLEAN/DATE/DATETIME/ENUM/OBJECT/ARRAY)',
    model_id VARCHAR(36) NOT NULL COMMENT '所属模型ID',
    requirement_id VARCHAR(36) COMMENT '关联需求ID',
    required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    default_value VARCHAR(255) COMMENT '默认值',
    description LONGTEXT COMMENT '属性描述(富文本HTML)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='属性表';

CREATE TABLE IF NOT EXISTS method (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '方法名称',
    code VARCHAR(50) COMMENT '方法编码',
    model_id VARCHAR(36) NOT NULL COMMENT '所属模型ID',
    requirement_id VARCHAR(36) COMMENT '关联需求ID',
    description LONGTEXT COMMENT '方法描述(富文本HTML)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='方法表';

CREATE TABLE IF NOT EXISTS method_param (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    method_id VARCHAR(36) NOT NULL COMMENT '方法ID',
    property_id VARCHAR(36) NOT NULL COMMENT '属性ID',
    param_type VARCHAR(20) NOT NULL COMMENT '参数类型(INPUT/OUTPUT)',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    UNIQUE KEY uk_method_property (method_id, property_id, param_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='方法参数表';

CREATE TABLE IF NOT EXISTS event (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '事件名称',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    model_id VARCHAR(36) COMMENT '关联模型ID',
    amount DECIMAL(18,2) DEFAULT 0 COMMENT '金额',
    quantity INT DEFAULT 0 COMMENT '数量',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态(SUCCESS/FAILED/PENDING)',
    event_time DATETIME COMMENT '事件时间',
    description LONGTEXT COMMENT '事件描述(富文本HTML)',
    metadata JSON COMMENT '元数据',
    external_flow_no1 VARCHAR(100) COMMENT '外部流水号一级',
    external_flow_no2 VARCHAR(100) COMMENT '外部流水号二级',
    point_brand_code VARCHAR(50) COMMENT '积分品牌代码',
    scene_code VARCHAR(50) COMMENT '场景码',
    main_order_no VARCHAR(100) COMMENT '主订单号',
    sub_order_no VARCHAR(100) COMMENT '子订单号',
    partner_code VARCHAR(50) COMMENT '合作伙伴代码',
    member_card_no VARCHAR(50) COMMENT '会员卡号',
    sales_channel1 VARCHAR(50) COMMENT '销售渠道一级',
    sales_channel2 VARCHAR(50) COMMENT '销售渠道二级',
    entry_flag INT COMMENT '入账标志',
    external_flow_no3 VARCHAR(100) COMMENT '外部流水号三级',
    business_tag VARCHAR(100) COMMENT '业务标签',
    event_amount DECIMAL(18,2) COMMENT '事件金额',
    pfr_id VARCHAR(100) COMMENT 'PFRID',
    operator VARCHAR(50) COMMENT '操作人',
    remark VARCHAR(5000) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_external_flow_no1 (external_flow_no1),
    KEY idx_member_card_no (member_card_no),
    KEY idx_event_type (event_type),
    KEY idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件流水表';

INSERT INTO requirement (id, name, code, description, status, priority, requirement_type, parent_id) VALUES 
('1', '用户管理需求', 'REQ-001', '<p>实现用户的注册、登录、认证等功能</p>', 'APPROVED', 'HIGH', 'MAIN', NULL),
('2', '订单管理需求', 'REQ-002', '<p>实现订单的创建、支付、发货等流程</p>', 'APPROVED', 'HIGH', 'MAIN', NULL),
('3', '商品管理需求', 'REQ-003', '<p>实现商品的增删改查和库存管理</p>', 'PENDING', 'MEDIUM', 'MAIN', NULL);

INSERT INTO model (id, name, code, description) VALUES 
('1', '用户模型', 'MODEL-001', '用户数据模型'),
('2', '订单模型', 'MODEL-002', '订单数据模型');

INSERT INTO model_requirement (id, model_id, requirement_id) VALUES 
('1', '1', '1'),
('2', '2', '2');

INSERT INTO property (id, name, code, data_type, model_id, requirement_id, required, default_value) VALUES 
('1', '用户名', 'username', 'STRING', '1', '1', TRUE, NULL),
('2', '密码', 'password', 'STRING', '1', '1', TRUE, NULL),
('3', '邮箱', 'email', 'STRING', '1', '1', FALSE, NULL),
('4', '订单号', 'orderNo', 'STRING', '2', '2', TRUE, NULL),
('5', '金额', 'amount', 'DOUBLE', '2', '2', TRUE, '0.0'),
('6', '状态', 'status', 'STRING', '2', '2', TRUE, 'PENDING');

INSERT INTO method (id, name, code, model_id, requirement_id, description) VALUES 
('1', '用户登录', 'login', '1', '1', '用户登录验证'),
('2', '用户注册', 'register', '1', '1', '用户注册'),
('3', '创建订单', 'createOrder', '2', '2', '创建新订单'),
('4', '支付订单', 'payOrder', '2', '2', '支付订单');

INSERT INTO method_param (id, method_id, property_id, param_type, sort_order) VALUES 
('1', '1', '1', 'INPUT', 1),
('2', '1', '2', 'INPUT', 2),
('3', '2', '1', 'INPUT', 1),
('4', '2', '2', 'INPUT', 2),
('5', '2', '3', 'INPUT', 3),
('6', '3', '4', 'OUTPUT', 1),
('7', '3', '5', 'INPUT', 1),
('8', '4', '4', 'INPUT', 1),
('9', '4', '5', 'INPUT', 2),
('10', '4', '6', 'OUTPUT', 1);
