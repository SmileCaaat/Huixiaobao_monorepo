-- 维保任务「情况简述/结论」扩展字段（幂等）
ALTER TABLE fire_maintenance_task
  ADD COLUMN IF NOT EXISTS patrol_summary_remark VARCHAR(500) NULL COMMENT '巡查汇总表备注' AFTER maintenance_time,
  ADD COLUMN IF NOT EXISTS test_summary_remark VARCHAR(500) NULL COMMENT '测试汇总表备注' AFTER patrol_summary_remark,
  ADD COLUMN IF NOT EXISTS upkeep_summary_remark VARCHAR(500) NULL COMMENT '维护保养表备注' AFTER test_summary_remark,
  ADD COLUMN IF NOT EXISTS other_patrol_content TEXT NULL COMMENT '其他巡查内容' AFTER upkeep_summary_remark,
  ADD COLUMN IF NOT EXISTS other_test_content TEXT NULL COMMENT '其他测试内容' AFTER other_patrol_content,
  ADD COLUMN IF NOT EXISTS alarm_host_voucher VARCHAR(1000) NULL COMMENT '消防报警主机测试打印凭证' AFTER other_test_content;

-- MySQL 8.0.12 以下不支持 ADD COLUMN IF NOT EXISTS 时可逐条执行：
-- ALTER TABLE fire_maintenance_task ADD COLUMN patrol_summary_remark VARCHAR(500) NULL COMMENT '巡查汇总表备注';
-- ALTER TABLE fire_maintenance_task ADD COLUMN test_summary_remark VARCHAR(500) NULL COMMENT '测试汇总表备注';
-- ALTER TABLE fire_maintenance_task ADD COLUMN upkeep_summary_remark VARCHAR(500) NULL COMMENT '维护保养表备注';
-- ALTER TABLE fire_maintenance_task ADD COLUMN other_patrol_content TEXT NULL COMMENT '其他巡查内容';
-- ALTER TABLE fire_maintenance_task ADD COLUMN other_test_content TEXT NULL COMMENT '其他测试内容';
-- ALTER TABLE fire_maintenance_task ADD COLUMN alarm_host_voucher VARCHAR(1000) NULL COMMENT '消防报警主机测试打印凭证';
