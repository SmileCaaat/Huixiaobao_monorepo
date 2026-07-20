-- =============================================
-- 消防设施测试模板数据插入脚本
-- 版本: v1.0
-- 日期: 2024-01-15
-- 说明: 插入消防设施测试的一级、二级、三级模板数据
--       template_type = '1' 表示消防设施测试
-- =============================================

-- 注意：执行前请确保已经运行了 update_add_selected_systems.sql 脚本
-- 该脚本添加了 template_type 字段

-- =============================================
-- 一级分类（消防设施测试系统）
-- =============================================

-- 1. 灭火器 (ID: 1001)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1001, 1, NULL, '灭火器', 'L1_019', 19, '1', '消防设施测试-灭火器系统');

-- =============================================
-- 二级分类（灭火器的子系统）
-- =============================================

-- 1.1 灭火器 (ID: 1002, 父级: 1001)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1002, 2, 1001, '灭火器', 'L2_019_001', 1, '1', '灭火器检查项');

-- =============================================
-- 三级分类（具体检查项）
-- =============================================

-- 1.1.1 灭火器外观 (ID: 1003, 父级: 1002)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1003, 3, 1002, '灭火器外观', 'L3_019_001_001', 1, '1', '检查灭火器外观是否完好');

-- 1.1.2 灭火器压力表 (ID: 1004, 父级: 1002)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1004, 3, 1002, '灭火器压力表', 'L3_019_001_002', 2, '1', '检查灭火器压力表指示');

-- 1.1.3 维修标示 (ID: 1005, 父级: 1002)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1005, 3, 1002, '维修标示', 'L3_019_001_003', 3, '1', '检查维修标示是否清晰');

-- 1.1.4 设置位置状况 (ID: 1006, 父级: 1002)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1006, 3, 1002, '设置位置状况', 'L3_019_001_004', 4, '1', '检查灭火器设置位置是否合理');

-- 1.1.5 灭火器数量 (ID: 1007, 父级: 1002)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1007, 3, 1002, '灭火器数量', 'L3_019_001_005', 5, '1', '检查灭火器配置数量是否符合要求');

-- =============================================
-- 附件系统（从图片左侧表格提取）
-- =============================================

-- 2. 附件 (ID: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1008, 1, NULL, '附件', 'L1_018', 18, '1', '消防设施测试-附件系统');

-- 2.1 紧急启/停按钮外观 (ID: 1009, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1009, 2, 1008, '紧急启/停按钮外观', 'L2_018_005', 5, '1', '紧急启停按钮外观检查');

-- 2.1.1 紧急启/停按钮外观 (ID: 1010, 父级: 1009)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1010, 3, 1009, '紧急启/停按钮外观', 'L3_018_005_001', 1, '1', '检查紧急启停按钮外观');

-- 2.1.2 程放指示外观 (ID: 1011, 父级: 1009)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1011, 3, 1009, '程放指示外观', 'L3_018_005_002', 2, '1', '检查程放指示外观');

-- 2.1.3 报警器外观 (ID: 1012, 父级: 1009)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1012, 3, 1009, '报警器外观', 'L3_018_005_003', 3, '1', '检查报警器外观');

-- 2.1.4 喷头外观 (ID: 1013, 父级: 1009)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1013, 3, 1009, '喷头外观', 'L3_018_005_004', 4, '1', '检查喷头外观');

-- 2.2 防护区 (ID: 1014, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1014, 2, 1008, '防护区', 'L2_018_006', 6, '1', '防护区检查');

-- 2.2.1 防护区外观 (ID: 1015, 父级: 1014)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1015, 3, 1014, '防护区外观', 'L3_018_006_001', 1, '1', '检查防护区外观');

-- 2.3 储瓶式 (ID: 1016, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1016, 2, 1008, '储瓶式', 'L2_018_007', 7, '1', '储瓶式系统检查');

-- 2.3.1 启动装置的启动性能 (ID: 1017, 父级: 1016)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1017, 3, 1016, '启动装置的启动性能', 'L3_018_007_001', 1, '1', '检查启动装置的启动性能');

-- 2.3.2 减压装置减压性能 (ID: 1018, 父级: 1016)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1018, 3, 1016, '减压装置减压性能', 'L3_018_007_002', 2, '1', '检查减压装置减压性能');

-- 2.3.3 喷头喷雾性能 (ID: 1019, 父级: 1016)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1019, 3, 1016, '喷头喷雾性能', 'L3_018_007_003', 3, '1', '检查喷头喷雾性能');

-- 2.4 泵式 (ID: 1020, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1020, 2, 1008, '泵式', 'L2_018_008', 8, '1', '泵式系统检查');

-- 2.4.1 手动启/停泵功能 (ID: 1021, 父级: 1020)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1021, 3, 1020, '手动启/停泵功能', 'L3_018_008_001', 1, '1', '检查手动启停泵功能');

-- 2.4.2 自动启/停泵功能 (ID: 1022, 父级: 1020)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1022, 3, 1020, '自动启/停泵功能', 'L3_018_008_002', 2, '1', '检查自动启停泵功能');

-- 2.4.3 干/备泵切换功能 (ID: 1023, 父级: 1020)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1023, 3, 1020, '干/备泵切换功能', 'L3_018_008_003', 3, '1', '检查干备泵切换功能');

-- 2.4.4 喷头喷雾性能 (ID: 1024, 父级: 1020)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1024, 3, 1020, '喷头喷雾性能', 'L3_018_008_004', 4, '1', '检查喷头喷雾性能');

-- 2.5 分区控制阀 (ID: 1025, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1025, 2, 1008, '分区控制阀', 'L2_018_009', 9, '1', '分区控制阀检查');

-- 2.5.1 手动控制功能 (ID: 1026, 父级: 1025)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1026, 3, 1025, '手动控制功能', 'L3_018_009_001', 1, '1', '检查手动控制功能');

-- 2.5.2 自动控制功能 (ID: 1027, 父级: 1025)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1027, 3, 1025, '自动控制功能', 'L3_018_009_002', 2, '1', '检查自动控制功能');

-- 2.6 细水雾灭火设备 (ID: 1028, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1028, 2, 1008, '细水雾灭火设备', 'L2_018_010', 10, '1', '细水雾灭火设备检查');

-- 2.6.1 联动控制功能 (ID: 1029, 父级: 1028)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1029, 3, 1028, '联动控制功能', 'L3_018_010_001', 1, '1', '检查联动控制功能');

-- 2.6.2 喷放细水雾功能 (ID: 1030, 父级: 1028)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1030, 3, 1028, '喷放细水雾功能', 'L3_018_010_002', 2, '1', '检查喷放细水雾功能');

-- 2.7 主端放水器 (ID: 1031, 父级: 1008)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1031, 2, 1008, '主端放水器', 'L2_018_011', 11, '1', '主端放水器检查');

-- 2.7.1 联动功能 (ID: 1032, 父级: 1031)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1032, 3, 1031, '联动功能', 'L3_018_011_001', 1, '1', '检查联动功能');

-- 2.7.2 水流指示器报警功能 (ID: 1033, 父级: 1031)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1033, 3, 1031, '水流指示器报警功能', 'L3_018_011_002', 2, '1', '检查水流指示器报警功能');

-- 2.7.3 压力开关报警功能 (ID: 1034, 父级: 1031)
INSERT INTO fire_maintenance_template (id, level, parent_id, item_name, item_code, sort_order, template_type, remark) 
VALUES (1034, 3, 1031, '压力开关报警功能', 'L3_018_011_003', 3, '1', '检查压力开关报警功能');

-- =============================================
-- 验证插入结果
-- =============================================

-- 查询消防设施测试的一级分类
SELECT id, level, item_name, item_code, template_type 
FROM fire_maintenance_template 
WHERE template_type = '1' AND level = 1 
ORDER BY sort_order;

-- 查询消防设施测试的二级分类
SELECT id, level, parent_id, item_name, item_code, template_type 
FROM fire_maintenance_template 
WHERE template_type = '1' AND level = 2 
ORDER BY parent_id, sort_order;

-- 查询消防设施测试的三级分类
SELECT id, level, parent_id, item_name, item_code, template_type 
FROM fire_maintenance_template 
WHERE template_type = '1' AND level = 3 
ORDER BY parent_id, sort_order;

-- 统计各级数量
SELECT 
    level,
    COUNT(*) as count
FROM fire_maintenance_template 
WHERE template_type = '1'
GROUP BY level
ORDER BY level;

-- =============================================
-- 说明
-- =============================================
-- 1. 所有消防设施测试模板的 template_type = '1'
-- 2. 常规维保模板的 template_type = '0' 或 NULL
-- 3. ID范围：1001-1999 为消防设施测试预留
-- 4. 常规维保ID范围：1-999
-- 5. parent_id 关联关系：三级 -> 二级 -> 一级
-- 6. sort_order 用于排序显示

-- =============================================
-- 回滚脚本（如需删除测试数据）
-- =============================================
-- DELETE FROM fire_maintenance_template WHERE template_type = '1';
-- DELETE FROM fire_maintenance_template WHERE id >= 1001 AND id <= 1999;
