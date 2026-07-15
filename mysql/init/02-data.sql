-- ============================================
-- 小吃店管理系统 - 初始化数据
-- ============================================

USE snack_admin;

-- ============================================
-- 初始化角色
-- ============================================
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '管理员', '系统最高权限，负责账号审核和系统管理'),
('SHOP_MANAGER', '店长', '日常经营，负责菜品、订单、库存、采购和统计管理');

-- ============================================
-- 初始化管理员账号 (密码: admin123)
-- ============================================
-- BCrypt 加密的 "admin123"
INSERT INTO sys_user (username, password, real_name, phone, status) VALUES
('admin', '$2b$12$NdJh1Kzx3HakjM6tIWcx4ejx3YZtp9p7gBYblnZqEac7OJNiMGRwi', '系统管理员', '13800000000', 'ENABLED');

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';
