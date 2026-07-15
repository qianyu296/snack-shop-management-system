-- ============================================
-- 小吃店管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS snack_admin
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE snack_admin;

-- ============================================
-- 1. 系统用户表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL COMMENT '用户名',
    password        VARCHAR(255)    NOT NULL COMMENT '加密密码',
    real_name       VARCHAR(50)     NOT NULL COMMENT '真实姓名',
    phone           VARCHAR(20)     NOT NULL COMMENT '手机号',
    avatar          VARCHAR(500)    DEFAULT NULL COMMENT '头像地址',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '账号状态: PENDING/ENABLED/DISABLED/REJECTED',
    reject_reason   VARCHAR(500)    DEFAULT NULL COMMENT '驳回原因',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================
-- 2. 系统角色表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    role_code       VARCHAR(50)     NOT NULL COMMENT '角色编码: ADMIN/SHOP_MANAGER',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名称',
    description     VARCHAR(200)    DEFAULT NULL COMMENT '角色描述',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ============================================
-- 3. 用户角色关联表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    UNIQUE INDEX uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================
-- 4. 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    module          VARCHAR(50)     NOT NULL COMMENT '操作模块',
    operation       VARCHAR(50)     NOT NULL COMMENT '操作类型',
    method          VARCHAR(200)    DEFAULT NULL COMMENT '请求方法',
    request_url     VARCHAR(500)    DEFAULT NULL COMMENT '请求URL',
    request_params  TEXT            DEFAULT NULL COMMENT '请求参数',
    operator_id     BIGINT          DEFAULT NULL COMMENT '操作人ID',
    operator_name   VARCHAR(50)     DEFAULT NULL COMMENT '操作人姓名',
    operator_ip     VARCHAR(50)     DEFAULT NULL COMMENT '操作IP',
    result          VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果: SUCCESS/FAIL',
    error_msg       TEXT            DEFAULT NULL COMMENT '错误信息',
    cost_time       BIGINT          DEFAULT NULL COMMENT '耗时(ms)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_module (module),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 5. 菜品分类表
-- ============================================
CREATE TABLE IF NOT EXISTS dish_category (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(20)     NOT NULL COMMENT '分类名称',
    sort            INT             NOT NULL DEFAULT 0 COMMENT '排序值',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    remark          VARCHAR(200)    DEFAULT NULL COMMENT '备注',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_name (name),
    INDEX idx_sort (sort),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

-- ============================================
-- 6. 菜品表
-- ============================================
CREATE TABLE IF NOT EXISTS dish (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT          NOT NULL COMMENT '分类ID',
    name            VARCHAR(50)     NOT NULL COMMENT '菜品名称',
    base_price      DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '基础价格',
    image_url       VARCHAR(500)    DEFAULT NULL COMMENT '菜品图片',
    taste           VARCHAR(50)     DEFAULT NULL COMMENT '口味',
    description     VARCHAR(500)    DEFAULT NULL COMMENT '菜品描述',
    recommend_status VARCHAR(20)    NOT NULL DEFAULT 'NORMAL' COMMENT '推荐状态: NORMAL/RECOMMENDED',
    sale_status     VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '销售状态: DRAFT/ON_SALE/OFF_SALE',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_category_id (category_id),
    INDEX idx_sale_status (sale_status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

-- ============================================
-- 7. 菜品规格表
-- ============================================
CREATE TABLE IF NOT EXISTS dish_spec (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    dish_id         BIGINT          NOT NULL COMMENT '菜品ID',
    name            VARCHAR(30)     NOT NULL COMMENT '规格名称',
    price           DECIMAL(10,2)   NOT NULL COMMENT '规格价格',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品规格表';

-- ============================================
-- 8. 原料表
-- ============================================
CREATE TABLE IF NOT EXISTS material (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL COMMENT '原料名称',
    category        VARCHAR(20)     NOT NULL COMMENT '原料分类: STAPLE/VEGETABLE/MEAT/SEASONING/PACKAGING/OTHER',
    unit            VARCHAR(20)     NOT NULL COMMENT '计量单位',
    current_stock   DECIMAL(12,3)   NOT NULL DEFAULT 0 COMMENT '当前库存',
    safe_stock      DECIMAL(12,3)   NOT NULL DEFAULT 0 COMMENT '安全库存',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    remark          VARCHAR(200)    DEFAULT NULL COMMENT '备注',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_name (name),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_stock_warning (current_stock, safe_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='原料表';

-- ============================================
-- 9. 菜品配方表 (菜品-原料关联)
-- ============================================
CREATE TABLE IF NOT EXISTS dish_material (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    dish_id         BIGINT          NOT NULL COMMENT '菜品ID',
    material_id     BIGINT          NOT NULL COMMENT '原料ID',
    quantity        DECIMAL(12,3)   NOT NULL COMMENT '单份用量',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_dish_material (dish_id, material_id),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品配方表';

-- ============================================
-- 10. 订单主表
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(20)     NOT NULL COMMENT '订单编号',
    order_type      VARCHAR(10)     NOT NULL COMMENT '订单类型: DINE_IN/TAKEAWAY',
    table_no        VARCHAR(10)     DEFAULT NULL COMMENT '桌号(堂食)',
    pickup_no       INT             NOT NULL COMMENT '取餐号',
    total_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '订单金额',
    customer_remark VARCHAR(200)    DEFAULT NULL COMMENT '顾客备注',
    internal_remark VARCHAR(200)    DEFAULT NULL COMMENT '内部备注',
    cancel_reason   VARCHAR(500)    DEFAULT NULL COMMENT '取消原因',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING/COOKING/READY/COMPLETED/CANCELLED',
    created_by      BIGINT          NOT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_pickup_no (pickup_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ============================================
-- 11. 订单明细表
-- ============================================
CREATE TABLE IF NOT EXISTS order_item (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL COMMENT '订单ID',
    dish_id         BIGINT          NOT NULL COMMENT '菜品ID',
    dish_name       VARCHAR(50)     NOT NULL COMMENT '菜品名称快照',
    category_name   VARCHAR(20)     DEFAULT NULL COMMENT '分类名称快照',
    spec_name       VARCHAR(30)     DEFAULT NULL COMMENT '规格名称快照',
    unit_price      DECIMAL(10,2)   NOT NULL COMMENT '成交单价',
    quantity        INT             NOT NULL COMMENT '数量',
    amount          DECIMAL(10,2)   NOT NULL COMMENT '小计金额',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================
-- 12. 订单状态日志表
-- ============================================
CREATE TABLE IF NOT EXISTS order_status_log (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL COMMENT '订单ID',
    before_status   VARCHAR(20)    DEFAULT NULL COMMENT '变更前状态',
    after_status    VARCHAR(20)    NOT NULL COMMENT '变更后状态',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    operator_id     BIGINT         NOT NULL COMMENT '操作人ID',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态日志表';

-- ============================================
-- 13. 供应商表
-- ============================================
CREATE TABLE IF NOT EXISTS supplier (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL COMMENT '供应商名称',
    contact_name    VARCHAR(30)     NOT NULL COMMENT '联系人',
    contact_phone   VARCHAR(20)     NOT NULL COMMENT '联系电话',
    address         VARCHAR(200)    DEFAULT NULL COMMENT '联系地址',
    supply_materials VARCHAR(500)   DEFAULT NULL COMMENT '供应原料',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    remark          VARCHAR(200)    DEFAULT NULL COMMENT '备注',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- ============================================
-- 14. 采购单主表
-- ============================================
CREATE TABLE IF NOT EXISTS purchase_order (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    purchase_no     VARCHAR(20)     NOT NULL COMMENT '采购单号',
    supplier_id     BIGINT          NOT NULL COMMENT '供应商ID',
    purchase_date   DATE            NOT NULL COMMENT '采购日期',
    total_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '采购总额',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/WAREHOUSED/CANCELLED',
    cancel_reason   VARCHAR(500)    DEFAULT NULL COMMENT '作废原因',
    remark          VARCHAR(200)    DEFAULT NULL COMMENT '备注',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE INDEX uk_purchase_no (purchase_no),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status),
    INDEX idx_purchase_date (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单主表';

-- ============================================
-- 15. 采购明细表
-- ============================================
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id   BIGINT          NOT NULL COMMENT '采购单ID',
    material_id         BIGINT          NOT NULL COMMENT '原料ID',
    quantity            DECIMAL(12,3)   NOT NULL COMMENT '采购数量',
    unit_price          DECIMAL(10,2)   NOT NULL COMMENT '采购单价',
    amount              DECIMAL(10,2)   NOT NULL COMMENT '小计金额',
    INDEX idx_purchase_order_id (purchase_order_id),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购明细表';

-- ============================================
-- 16. 库存流水表
-- ============================================
CREATE TABLE IF NOT EXISTS inventory_record (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    material_id         BIGINT          NOT NULL COMMENT '原料ID',
    change_type         VARCHAR(20)     NOT NULL COMMENT '变动类型: PURCHASE_IN/ORDER_CONSUME/SURPLUS_ADJUST/LOSS_ADJUST',
    before_stock        DECIMAL(12,3)   NOT NULL COMMENT '变动前库存',
    change_quantity     DECIMAL(12,3)   NOT NULL COMMENT '变动数量(正增加负减少)',
    after_stock         DECIMAL(12,3)   NOT NULL COMMENT '变动后库存',
    business_no         VARCHAR(50)     DEFAULT NULL COMMENT '业务单号',
    remark              VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    operator_id         BIGINT          NOT NULL COMMENT '操作人ID',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_material_id (material_id),
    INDEX idx_change_type (change_type),
    INDEX idx_created_at (created_at),
    INDEX idx_business_no (business_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';
