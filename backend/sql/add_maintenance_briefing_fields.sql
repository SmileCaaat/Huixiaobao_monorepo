-- 为维保任务表添加维保简报相关字段
-- 执行时间：2026-02-15

USE ruoyi;

-- 添加维保情况简述字段
ALTER TABLE fire_maintenance_task 
ADD COLUMN maintenance_summary TEXT COMMENT '维保情况简述' AFTER building_name;

-- 添加维保时间字段
ALTER TABLE fire_maintenance_task 
ADD COLUMN maintenance_time DATE COMMENT '维保时间' AFTER maintenance_summary;

-- 查看表结构
DESC fire_maintenance_task;
