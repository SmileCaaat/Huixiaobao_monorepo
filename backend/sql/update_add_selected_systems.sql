-- =============================================
-- 维保任务系统选择功能 - 数据库升级脚本
-- 版本: v1.3
-- 日期: 2024-01-15
-- 说明: 添加系统选择功能和消防设施测试选项（使用独立模板）
-- =============================================

-- 1. 为模板表添加类型字段，区分常规维保和消防设施测试
ALTER TABLE fire_maintenance_template 
ADD COLUMN template_type CHAR(1) DEFAULT '0' COMMENT '模板类型（0=常规维保 1=消防设施测试）' 
AFTER sort_order;

-- 2. 添加选中系统ID字段
ALTER TABLE fire_maintenance_task 
ADD COLUMN selected_system_ids VARCHAR(500) DEFAULT NULL COMMENT '选中的系统模板IDs（多个用逗号分隔）' 
AFTER building_name;

-- 3. 添加消防设施测试选中系统字段
ALTER TABLE fire_maintenance_task 
ADD COLUMN selected_fire_test_ids VARCHAR(500) DEFAULT NULL COMMENT '选中的消防设施测试模板IDs（多个用逗号分隔）' 
AFTER selected_system_ids;

-- 4. 添加字段说明注释
ALTER TABLE fire_maintenance_template 
MODIFY COLUMN template_type CHAR(1) DEFAULT '0' COMMENT '模板类型（0=常规维保 1=消防设施测试）';

ALTER TABLE fire_maintenance_task 
MODIFY COLUMN selected_system_ids VARCHAR(500) DEFAULT NULL COMMENT '选中的系统模板IDs（多个用逗号分隔，为空表示全部系统）';

ALTER TABLE fire_maintenance_task 
MODIFY COLUMN selected_fire_test_ids VARCHAR(500) DEFAULT NULL COMMENT '选中的消防设施测试模板IDs（多个用逗号分隔，为空表示不包含消防设施测试）';

-- 5. 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_maintenance_template'
    AND COLUMN_NAME = 'template_type';

SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_maintenance_task'
    AND COLUMN_NAME IN ('selected_system_ids', 'selected_fire_test_ids');

-- 说明：
-- 1. template_type 字段用于区分模板类型：
--    '0' = 常规维保模板（默认）
--    '1' = 消防设施测试模板
-- 2. selected_system_ids 字段存储选中的常规维保一级模板（系统）ID，多个ID用逗号分隔
-- 3. selected_fire_test_ids 字段存储选中的消防设施测试一级模板ID，多个ID用逗号分隔
-- 4. 如果 selected_system_ids 为空或NULL，表示使用所有常规维保系统（默认行为）
-- 5. 如果 selected_fire_test_ids 为空或NULL，表示不包含消防设施测试
-- 6. 示例值: selected_system_ids="1,2,5", selected_fire_test_ids="101,102,105"
-- 7. 这些字段只在创建任务时使用，用于控制生成哪些检查记录
-- 8. 修改这些字段不会影响已生成的检查记录

-- 6. 更新现有模板数据的类型（将现有数据标记为常规维保）
UPDATE fire_maintenance_template SET template_type = '0' WHERE template_type IS NULL;

-- 回滚脚本（如需回滚，执行以下语句）
-- ALTER TABLE fire_maintenance_template DROP COLUMN template_type;
-- ALTER TABLE fire_maintenance_task DROP COLUMN selected_system_ids;
-- ALTER TABLE fire_maintenance_task DROP COLUMN selected_fire_test_ids;
