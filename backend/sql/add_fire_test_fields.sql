-- =============================================
-- 为消防设施测试记录添加新字段
-- 版本: v1.4
-- 日期: 2024-01-20
-- 说明: 添加设备位置、测试情况、测试时间、测试结果、现场照片字段
-- =============================================

-- 1. 添加设备位置字段
ALTER TABLE fire_maintenance_record 
ADD COLUMN device_location VARCHAR(200) DEFAULT NULL COMMENT '设备位置' 
AFTER other_notes;

-- 2. 添加测试情况字段
ALTER TABLE fire_maintenance_record 
ADD COLUMN test_situation VARCHAR(500) DEFAULT NULL COMMENT '测试情况' 
AFTER device_location;

-- 3. 添加测试时间字段
ALTER TABLE fire_maintenance_record 
ADD COLUMN test_time DATETIME DEFAULT NULL COMMENT '测试时间' 
AFTER test_situation;

-- 4. 添加测试结果字段
ALTER TABLE fire_maintenance_record 
ADD COLUMN test_result VARCHAR(500) DEFAULT NULL COMMENT '测试结果' 
AFTER test_time;

-- 5. 添加现场照片字段（多个照片用逗号分隔）
ALTER TABLE fire_maintenance_record 
ADD COLUMN site_photos VARCHAR(1000) DEFAULT NULL COMMENT '现场照片（多个用逗号分隔）' 
AFTER test_result;

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_maintenance_record'
    AND COLUMN_NAME IN ('device_location', 'test_situation', 'test_time', 'test_result', 'site_photos')
ORDER BY 
    ORDINAL_POSITION;

-- =============================================
-- 使用说明
-- =============================================
-- 1. device_location: 设备位置，如"1楼大厅"、"2楼走廊"等
-- 2. test_situation: 测试情况描述，记录测试过程中的情况
-- 3. test_time: 测试时间，记录具体的测试时间点
-- 4. test_result: 测试结果，记录测试的详细结果
-- 5. site_photos: 现场照片路径，多个照片用逗号分隔
-- =============================================
