-- =============================================
-- 添加 record_type 字段到 fire_maintenance_record 表
-- record_type: 0=常规维保 1=消防设施测试
-- =============================================

ALTER TABLE fire_maintenance_record 
ADD COLUMN record_type CHAR(1) DEFAULT '0' COMMENT '记录类型（0=常规维保 1=消防设施测试）';

-- 更新已有记录的 record_type（如果有消防测试模板关联的记录）
UPDATE fire_maintenance_record r
INNER JOIN fire_maintenance_template t ON r.template_id = t.id
SET r.record_type = t.template_type
WHERE t.template_type = '1';
