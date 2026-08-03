-- 维保任务类目统一升级（以《维保任务类目收集.docx》的二级明细和三张汇总表为准）
-- 目标：18 个一级类目；三级项巡查 237、测试 182、保养 30，合计 449。
-- template_type: 0=巡查，1=测试，2=保养，8=本次升级停用的旧模板。
-- 注意：本脚本只更新模板。历史任务请在部署新版后端后，通过“重建巡查测试检查记录”重新生成。

-- 存储过程必须在事务开始前创建；MySQL 的 CREATE/DROP PROCEDURE 会隐式提交。
-- 这样后续所有模板 DML 和强校验才处在同一个可回滚事务中。
DROP PROCEDURE IF EXISTS catalog448_add_group;
DROP PROCEDURE IF EXISTS catalog448_assert;
DELIMITER $$
CREATE PROCEDURE catalog448_add_group(
    IN p_type CHAR(1), IN p_root_id BIGINT, IN p_root_name VARCHAR(200), IN p_root_sort INT,
    IN p_group_id BIGINT, IN p_group_name VARCHAR(200), IN p_group_sort INT, IN p_items JSON)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE n INT DEFAULT JSON_LENGTH(p_items);
    INSERT INTO fire_maintenance_template
        (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
    VALUES
        (p_root_id,1,NULL,p_root_name,CONCAT('CAT448_',p_type,'_',LPAD(p_root_sort,3,'0')),p_root_sort,p_type,'二级明细标准项')
    ON DUPLICATE KEY UPDATE item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type=VALUES(template_type);
    INSERT INTO fire_maintenance_template
        (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
    VALUES
        (p_group_id,2,p_root_id,p_group_name,CONCAT('CAT448_',p_type,'_',LPAD(p_root_sort,3,'0'),'_',LPAD(p_group_sort,3,'0')),p_group_sort,p_type,'二级明细标准项')
    ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type=VALUES(template_type);
    WHILE i < n DO
        INSERT INTO fire_maintenance_template
            (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
        VALUES
            (p_group_id*100+i+1,3,p_group_id,JSON_UNQUOTE(JSON_EXTRACT(p_items,CONCAT('$[',i,']'))),
             CONCAT('CAT448_',p_type,'_',LPAD(p_root_sort,3,'0'),'_',LPAD(p_group_sort,3,'0'),'_',LPAD(i+1,3,'0')),
             i+1,p_type,IF(p_type='1','测试汇总表','维护保养记录表'))
        ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type=VALUES(template_type);
        SET i=i+1;
    END WHILE;
END$$

CREATE PROCEDURE catalog448_assert()
BEGIN
    DECLARE patrol_count INT;
    DECLARE test_count INT;
    DECLARE upkeep_count INT;
    DECLARE category_count INT;
    DECLARE category_detail_mismatch INT;
    DECLARE error_message VARCHAR(255);
    SELECT COUNT(*) INTO patrol_count FROM fire_maintenance_template WHERE level=3 AND template_type='0';
    SELECT COUNT(*) INTO test_count FROM fire_maintenance_template WHERE level=3 AND template_type='1';
    SELECT COUNT(*) INTO upkeep_count FROM fire_maintenance_template WHERE level=3 AND template_type='2';
    SELECT COUNT(DISTINCT item_name) INTO category_count
      FROM fire_maintenance_template WHERE level=1 AND template_type IN ('0','1','2');
    SELECT COUNT(*) INTO category_detail_mismatch
    FROM (
        SELECT '消防供配电设施' item_name,9 patrol_count,8 test_count,0 upkeep_count UNION ALL
        SELECT '火灾自动报警系统',18,26,1 UNION ALL
        SELECT '电气火灾监控系统',4,0,0 UNION ALL
        SELECT '可燃气体探测报警系统',4,0,0 UNION ALL
        SELECT '消防供水设施',26,19,5 UNION ALL
        SELECT '消火栓（消防炮）灭火系统',20,19,6 UNION ALL
        SELECT '自动喷水灭火系统',22,19,5 UNION ALL
        SELECT '泡沫灭火系统',21,12,6 UNION ALL
        SELECT '气体灭火系统',24,11,1 UNION ALL
        SELECT '防/排烟系统',17,23,4 UNION ALL
        SELECT '应急照明和疏散指示标志',4,5,0 UNION ALL
        SELECT '应急广播系统',7,8,0 UNION ALL
        SELECT '消防专用电话',5,3,0 UNION ALL
        SELECT '防火分隔设施',17,5,1 UNION ALL
        SELECT '消防电梯系统',6,4,0 UNION ALL
        SELECT '细水雾灭火系统',15,15,0 UNION ALL
        SELECT '干粉灭火系统',13,4,0 UNION ALL
        SELECT '灭火器',5,1,1
    ) expected
    LEFT JOIN (
        SELECT l1.item_name,
               SUM(CASE WHEN l3.template_type='0' THEN 1 ELSE 0 END) patrol_count,
               SUM(CASE WHEN l3.template_type='1' THEN 1 ELSE 0 END) test_count,
               SUM(CASE WHEN l3.template_type='2' THEN 1 ELSE 0 END) upkeep_count
        FROM fire_maintenance_template l1
        JOIN fire_maintenance_template l2
          ON l2.parent_id=l1.id AND l2.level=2 AND l2.template_type=l1.template_type
        JOIN fire_maintenance_template l3
          ON l3.parent_id=l2.id AND l3.level=3 AND l3.template_type=l2.template_type
        WHERE l1.level=1 AND l1.template_type IN ('0','1','2')
        GROUP BY l1.item_name
    ) actual ON actual.item_name=expected.item_name
    WHERE COALESCE(actual.patrol_count,-1)<>expected.patrol_count
       OR COALESCE(actual.test_count,-1)<>expected.test_count
       OR COALESCE(actual.upkeep_count,-1)<>expected.upkeep_count;
    IF patrol_count<>237 OR test_count<>182 OR upkeep_count<>30 OR category_count<>18
       OR category_detail_mismatch<>0 THEN
        SET error_message=CONCAT(
            '维保类目校验失败 patrol=',patrol_count,', test=',test_count,
            ', upkeep=',upkeep_count,', categories=',category_count,
            ', category_mismatch=',category_detail_mismatch);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT=error_message;
    END IF;
END$$
DELIMITER ;

START TRANSACTION;

-- 一级类目按参考明细重新排序；远程监控并入火灾自动报警，消防炮并入消火栓。
UPDATE fire_maintenance_template SET item_name='消防供配电设施', sort_order=1  WHERE id=1;
UPDATE fire_maintenance_template SET item_name='火灾自动报警系统', sort_order=2  WHERE id=14;
UPDATE fire_maintenance_template SET item_name='电气火灾监控系统', sort_order=3  WHERE id=54;
UPDATE fire_maintenance_template SET item_name='可燃气体探测报警系统', sort_order=4 WHERE id=47;
UPDATE fire_maintenance_template SET item_name='消防供水设施', sort_order=5  WHERE id=61;
UPDATE fire_maintenance_template SET item_name='消火栓（消防炮）灭火系统', sort_order=6 WHERE id=99;
UPDATE fire_maintenance_template SET item_name='自动喷水灭火系统', sort_order=7 WHERE id=128;
UPDATE fire_maintenance_template SET item_name='泡沫灭火系统', sort_order=8 WHERE id=281;
UPDATE fire_maintenance_template SET item_name='气体灭火系统', sort_order=9 WHERE id=152;
UPDATE fire_maintenance_template SET item_name='防/排烟系统', sort_order=10 WHERE id=187;
UPDATE fire_maintenance_template SET item_name='应急照明和疏散指示标志', sort_order=11 WHERE id=214;
UPDATE fire_maintenance_template SET item_name='应急广播系统', sort_order=12 WHERE id=227;
UPDATE fire_maintenance_template SET item_name='消防专用电话', sort_order=13 WHERE id=239;
UPDATE fire_maintenance_template SET item_name='防火分隔设施', sort_order=14 WHERE id=247;
UPDATE fire_maintenance_template SET item_name='消防电梯系统', sort_order=15 WHERE id=271;
UPDATE fire_maintenance_template SET item_name='细水雾灭火系统', sort_order=16 WHERE id=342;
UPDATE fire_maintenance_template SET item_name='灭火器', sort_order=18 WHERE id=383;

-- 二级明细中的规范名称。
UPDATE fire_maintenance_template SET item_name='屋顶试验消火栓' WHERE id=100;
UPDATE fire_maintenance_template SET item_name='排烟风机' WHERE id=188;
UPDATE fire_maintenance_template SET sort_order=1 WHERE id IN (65,105,221);
UPDATE fire_maintenance_template SET sort_order=2 WHERE id IN (91,100,224);
UPDATE fire_maintenance_template SET sort_order=3 WHERE id IN (71,109);
UPDATE fire_maintenance_template SET sort_order=4 WHERE id=76;
UPDATE fire_maintenance_template SET sort_order=6 WHERE id=82;
UPDATE fire_maintenance_template SET sort_order=7 WHERE id=89;
UPDATE fire_maintenance_template SET sort_order=8 WHERE id=62;
UPDATE fire_maintenance_template SET sort_order=5 WHERE id=114;
UPDATE fire_maintenance_template SET sort_order=1 WHERE id=282;
UPDATE fire_maintenance_template SET sort_order=2 WHERE id=290;
UPDATE fire_maintenance_template SET sort_order=3 WHERE id=295;
UPDATE fire_maintenance_template SET sort_order=4 WHERE id=300;
UPDATE fire_maintenance_template SET sort_order=5 WHERE id=305;
UPDATE fire_maintenance_template SET sort_order=6 WHERE id=333;
UPDATE fire_maintenance_template SET sort_order=7 WHERE id=336;
UPDATE fire_maintenance_template SET sort_order=8 WHERE id=338;
UPDATE fire_maintenance_template SET sort_order=9 WHERE id=340;

-- 修正旧模板中已经确认的错别字，保证三级文字与巡查汇总表逐字一致。
UPDATE fire_maintenance_template SET item_name='设置位置' WHERE id=174;
UPDATE fire_maintenance_template SET item_name='控制装置外观' WHERE id IN (175,183);
UPDATE fire_maintenance_template SET item_name='制冷装置外观' WHERE id=181;
UPDATE fire_maintenance_template SET item_name='灭火控制器工作状态' WHERE id=344;
UPDATE fire_maintenance_template SET item_name='稳压泵工作状态' WHERE id=355;
UPDATE fire_maintenance_template SET item_name='释放指示灯外观' WHERE id=359;

-- 火灾自动报警系统：并入远程监控二级类目，拆出接地装置，停用明细中没有的旧项。
UPDATE fire_maintenance_template SET template_type='8' WHERE id IN (25,26,35,36,37,40);
UPDATE fire_maintenance_template SET parent_id=14, sort_order=6 WHERE id=41;
UPDATE fire_maintenance_template SET parent_id=14, sort_order=7 WHERE id=44;
UPDATE fire_maintenance_template SET item_name='消防控制室', sort_order=9 WHERE id=38;
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES (9002,2,14,'接地装置','CAT448_P_002_008',8,'0','二级明细标准项')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 自动喷水：纠正旧模板中错挂到报警阀组下的“充气设备外观”，补齐喷头和末端试验阀门。
UPDATE fire_maintenance_template SET parent_id=137, sort_order=1, template_type='0' WHERE id=136;
UPDATE fire_maintenance_template SET sort_order=2 WHERE id=138;
UPDATE fire_maintenance_template SET sort_order=3 WHERE id=139;
UPDATE fire_maintenance_template SET sort_order=4 WHERE id=140;
UPDATE fire_maintenance_template SET sort_order=2 WHERE id=130;
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES
    (900501,3,129,'喷头外观','CAT448_P_007_001_001',1,'0','巡查汇总表'),
    (9005,2,128,'末端试验阀门','CAT448_P_007_007',7,'0','二级明细标准项'),
    (900502,3,9005,'楼层/区域末端试验阀门处压力值','CAT448_P_007_007_001',1,'0','巡查汇总表'),
    (900503,3,9005,'楼层/区域末端试验装置现场环境','CAT448_P_007_007_002',2,'0','巡查汇总表'),
    (900504,3,9005,'系统末端试验装置外观','CAT448_P_007_007_003',3,'0','巡查汇总表'),
    (900505,3,9005,'系统末端试验装置现场环境','CAT448_P_007_007_004',4,'0','巡查汇总表')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 气体、防排烟和应急照明按二级明细截图剔除旧模板多出的巡查项。
UPDATE fire_maintenance_template SET template_type='8' WHERE id IN (168,209,215,216,217,218,219,220);
UPDATE fire_maintenance_template SET parent_id=9002, sort_order=1, template_type='0' WHERE id=21;

-- 细水雾：储气瓶只保留“外观”，将汇总表中的“分区控制阀外观”补到附件。
UPDATE fire_maintenance_template SET template_type='8' WHERE id=347;
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES (900601,3,357,'分区控制阀外观','CAT448_P_016_005_005',5,'0','巡查汇总表')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 消防供水设施：水池/水箱按明细各保留 3 项，增加水泵接合器 2 项。
UPDATE fire_maintenance_template SET template_type='8' WHERE id IN (68,69,95,96,97,98);
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES
    (9003,2,61,'水泵接合器','CAT448_P_005_005',5,'0','二级明细标准项'),
    (900301,3,9003,'水泵接合器外观','CAT448_P_005_005_001',1,'0','巡查汇总表'),
    (900302,3,9003,'水泵接合器标识','CAT448_P_005_005_002',2,'0','巡查汇总表')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 消防炮旧树替换为明细中的 8 项，并合并到消火栓一级类目。
UPDATE fire_maintenance_template SET template_type='8' WHERE id BETWEEN 116 AND 127;
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES
    (9004,2,99,'消防炮','CAT448_P_006_004',4,'0','二级明细标准项'),
    (900401,3,9004,'消防炮外观','CAT448_P_006_004_001',1,'0','巡查汇总表'),
    (900402,3,9004,'消防炮周边环境','CAT448_P_006_004_002',2,'0','巡查汇总表'),
    (900403,3,9004,'炮塔外观','CAT448_P_006_004_003',3,'0','巡查汇总表'),
    (900404,3,9004,'炮塔周边环境','CAT448_P_006_004_004',4,'0','巡查汇总表'),
    (900405,3,9004,'现场火灾探测控制装置外观','CAT448_P_006_004_005',5,'0','巡查汇总表'),
    (900406,3,9004,'现场火灾探测控制装置周边环境','CAT448_P_006_004_006',6,'0','巡查汇总表'),
    (900407,3,9004,'回旋装置外观','CAT448_P_006_004_007',7,'0','巡查汇总表'),
    (900408,3,9004,'回旋装置周边环境','CAT448_P_006_004_008',8,'0','巡查汇总表')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 泡沫系统：移除混入巡查树的测试/保养项，按二级明细保留 21 项。
UPDATE fire_maintenance_template SET template_type='8'
WHERE id IN (284,285,286,287,288,289,309,310,311,312)
   OR item_code LIKE 'L2_017_006%' OR item_code LIKE 'L3_017_006_%'
   OR item_code LIKE 'L2_017_007%' OR item_code LIKE 'L3_017_007_%'
   OR item_code LIKE 'L2_017_008%' OR item_code LIKE 'L3_017_008_%'
   OR item_code LIKE 'L2_017_009%' OR item_code LIKE 'L3_017_009_%'
   OR item_code LIKE 'L2_017_010%' OR item_code LIKE 'L3_017_010_%'
   OR item_code LIKE 'L2_017_011%' OR item_code LIKE 'L3_017_011_%'
   OR item_code LIKE 'L2_017_012%' OR item_code LIKE 'L3_017_012_%';

-- 细水雾：后 15 项属于测试，不再混入巡查树。
UPDATE fire_maintenance_template SET template_type='8'
WHERE item_code LIKE 'L2_018_007%' OR item_code LIKE 'L3_018_007_%'
   OR item_code LIKE 'L2_018_008%' OR item_code LIKE 'L3_018_008_%'
   OR item_code LIKE 'L2_018_009%' OR item_code LIKE 'L3_018_009_%'
   OR item_code LIKE 'L2_018_010%' OR item_code LIKE 'L3_018_010_%'
   OR item_code LIKE 'L2_018_011%' OR item_code LIKE 'L3_018_011_%';

-- 干粉灭火系统巡查 13 项。
INSERT INTO fire_maintenance_template
    (id,level,parent_id,item_name,item_code,sort_order,template_type,remark)
VALUES
    (9000,1,NULL,'干粉灭火系统','CAT448_P_017',17,'0','二级明细标准项'),
    (9010,2,9000,'灭火控制器','CAT448_P_017_001',1,'0','二级明细标准项'),
    (901001,3,9010,'灭火控制器工作状态','CAT448_P_017_001_001',1,'0','巡查汇总表'),
    (9011,2,9000,'干粉储存容器','CAT448_P_017_002',2,'0','二级明细标准项'),
    (901101,3,9011,'设备储存间环境','CAT448_P_017_002_001',1,'0','巡查汇总表'),
    (901102,3,9011,'驱动气瓶外观','CAT448_P_017_002_002',2,'0','巡查汇总表'),
    (901103,3,9011,'灭火剂储存装置外观','CAT448_P_017_002_003',3,'0','巡查汇总表'),
    (9012,2,9000,'干粉灭火设备','CAT448_P_017_003',3,'0','二级明细标准项'),
    (901201,3,9012,'选择阀外观','CAT448_P_017_003_001',1,'0','巡查汇总表'),
    (901202,3,9012,'驱动装置外观','CAT448_P_017_003_002',2,'0','巡查汇总表'),
    (901203,3,9012,'紧急启/停按钮外观','CAT448_P_017_003_003',3,'0','巡查汇总表'),
    (901204,3,9012,'放气指示灯外观','CAT448_P_017_003_004',4,'0','巡查汇总表'),
    (901205,3,9012,'放气指示灯运行状态','CAT448_P_017_003_005',5,'0','巡查汇总表'),
    (901206,3,9012,'警报器外观','CAT448_P_017_003_006',6,'0','巡查汇总表'),
    (901207,3,9012,'警报器运行状态','CAT448_P_017_003_007',7,'0','巡查汇总表'),
    (901208,3,9012,'喷嘴外观','CAT448_P_017_003_008',8,'0','巡查汇总表'),
    (9013,2,9000,'防护区','CAT448_P_017_004',4,'0','二级明细标准项'),
    (901301,3,9013,'防护区状况','CAT448_P_017_004_001',1,'0','巡查汇总表')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),item_name=VALUES(item_name),sort_order=VALUES(sort_order),template_type='0';

-- 旧测试模板不再参与新任务；下方按测试汇总表重建 182 项。
UPDATE fire_maintenance_template SET template_type='8' WHERE template_type='1';

-- 测试：消防供配电设施 8。
CALL catalog448_add_group('1',5001,'消防供配电设施',1,5101,'消防配电柜（箱）',1,JSON_ARRAY('主、备电切换功能','主、备电源供电能力测试'));
CALL catalog448_add_group('1',5001,'消防供配电设施',1,5102,'自备发电机组',2,JSON_ARRAY('发电机自动启动功能','发电机手动启动功能','发电机启动电源充、放电功能'));
CALL catalog448_add_group('1',5001,'消防供配电设施',1,5103,'联动试验',3,JSON_ARRAY('非消防电源的联动切断功能'));
CALL catalog448_add_group('1',5001,'消防供配电设施',1,5104,'应急电源',4,JSON_ARRAY('试验应急电源充、放电功能'));
CALL catalog448_add_group('1',5001,'消防供配电设施',1,5105,'储油设施',5,JSON_ARRAY('核对储油量'));

-- 测试：火灾自动报警系统 26。
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5201,'火灾探测器',1,JSON_ARRAY('火灾探测器报警功能'));
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5202,'手动报警按钮',2,JSON_ARRAY('报警功能'));
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5203,'声光报警器',3,JSON_ARRAY('警报功能'));
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5204,'报警控制器',4,JSON_ARRAY('火警报警功能','故障报警功能','火警优先功能','打印机打印功能','自检功能','消音等功能','火灾显示盘报警数量显示功能','CRT显示器报警数量显示功能'));
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5205,'消防联动控制器',5,JSON_ARRAY('手动控制功能','自动控制功能','控制模块手动控制功能','控制模块自动控制功能','控制器显示功能','主、备电源切换功能','备用电源充、放电功能'));
CALL catalog448_add_group('1',5002,'火灾自动报警系统',2,5206,'远程监控系统',6,JSON_ARRAY('信息传输装置显示功能','信息传输装置传输功能','监控主机信息显示功能','告警受理功能','派单/接单功能','远程开锁等功能','主/备电源切换功能','备用电源充/放电功能'));

-- 测试：消防供水设施 19。
CALL catalog448_add_group('1',5005,'消防供水设施',5,5501,'消防水池',1,JSON_ARRAY('储水自动进水阀进水功能'));
CALL catalog448_add_group('1',5005,'消防供水设施',5,5502,'消防水箱',2,JSON_ARRAY('核对储水量','自动进水阀进水功能','消防水箱供水能力','液位检测装置报警功能','模拟消防水箱出水'));
CALL catalog448_add_group('1',5005,'消防供水设施',5,5503,'稳（增）压泵及气压水罐',3,JSON_ARRAY('稳压泵功能','增压泵功能','气压水罐功能','自动启泵压力','自动停泵压力','联动启动主泵的压力工况','主、备泵切换功能'));
CALL catalog448_add_group('1',5005,'消防供水设施',5,5504,'消防水泵及控制柜',4,JSON_ARRAY('手动/自动启泵功能','主、备泵切换功能','消防泵供水时流量','消防泵供水时压力'));
CALL catalog448_add_group('1',5005,'消防供水设施',5,5505,'阀门',5,JSON_ARRAY('控制阀门启闭功能','减压装置减压功能'));

-- 测试：消火栓（消防炮）灭火系统 19。
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5601,'室内消火栓',1,JSON_ARRAY('出水压力','静压','水质'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5602,'消防水喉',2,JSON_ARRAY('射水试验'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5603,'室外消火栓',3,JSON_ARRAY('消火栓出水压力','静压'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5604,'消防炮',4,JSON_ARRAY('手动功能','遥控操作功能'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5605,'消防炮',5,JSON_ARRAY('手动按钮启动功能','消防炮出水功能'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5606,'启泵按钮',6,JSON_ARRAY('远距离启泵功能','信号指示功能'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5607,'联动控制功能',7,JSON_ARRAY('远距离启泵按钮','最不利点消火栓出水压力','最不利点消火栓出水流量','联动控制盘控制按钮'));
CALL catalog448_add_group('1',5006,'消火栓（消防炮）灭火系统',6,5608,'消防炮',8,JSON_ARRAY('最不利点消防炮出水压力','最不利点消防炮出水流量','模拟自动启动'));

-- 测试：自动喷水灭火系统 19。
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5701,'报警阀组',1,JSON_ARRAY('排放阀排水功能','压力开关报警功能','水力警铃报警功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5702,'末端试水装置',2,JSON_ARRAY('工作压力','水流指示器功能','压力开关动作信号','水质情况','楼层末端试验阀功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5703,'水流指示器',3,JSON_ARRAY('核对反馈信号'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5704,'火灾探测器',4,JSON_ARRAY('火灾探测传动装置探测功能','火灾探测传动装置控制功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5705,'现场手动控制装置',5,JSON_ARRAY('手动控制装置控制功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5706,'充气装置',6,JSON_ARRAY('充气功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5707,'排气装置',7,JSON_ARRAY('排气功能'));
CALL catalog448_add_group('1',5007,'自动喷水灭火系统',7,5708,'联动控制功能',8,JSON_ARRAY('系统联动功能','水流指示器','压力开关','水力警铃报警功能','模拟系统自动启动'));

-- 测试：泡沫灭火系统 12。
CALL catalog448_add_group('1',5008,'泡沫灭火系统',8,5801,'泡沫消火栓',1,JSON_ARRAY('出水/出泡沫功能'));
CALL catalog448_add_group('1',5008,'泡沫灭火系统',8,5802,'泡沫泵',2,JSON_ARRAY('手动/自动启动功能','主、备泵切换功能','阀门启闭功能','信号反馈功能'));
CALL catalog448_add_group('1',5008,'泡沫灭火系统',8,5803,'联动控制功能',3,JSON_ARRAY('自动启动','泡沫消火栓出泡沫功能','泡沫喷头出泡沫功能','泡沫产生器出泡沫功能','泡沫比例混合器混合配比功能','泡沫泵供泡沫液能力','水泵供水能力'));

-- 测试：气体灭火系统 11。
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5901,'储气瓶',1,JSON_ARRAY('核对灭火剂储存量','主、备瓶组切换功能'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5902,'检漏装置',2,JSON_ARRAY('称重功能','检漏报警功能'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5903,'紧急启停按钮',3,JSON_ARRAY('紧急启动','紧急停止'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5904,'启动装置',4,JSON_ARRAY('启动功能'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5905,'选择阀',5,JSON_ARRAY('选择阀手动启动功能'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5906,'联动控制功能',6,JSON_ARRAY('系统报警功能','联动功能'));
CALL catalog448_add_group('1',5009,'气体灭火系统',9,5907,'通风换气设备',7,JSON_ARRAY('测试通风换气功能'));

-- 测试：防/排烟系统 23。
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6001,'送风机',1,JSON_ARRAY('送风口手/自动开启功能','送风机手/自动启动停止功能','风量','风速','风压'));
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6002,'排烟风机',2,JSON_ARRAY('手动/自动启动功能','排烟防火阀联动停止功能','排烟风量','排烟风速'));
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6003,'排烟阀',3,JSON_ARRAY('排烟阀手/自动开启功能','电动排烟窗手/自动开启功能','挡烟垂壁释放功能','排烟防火阀动作性能'));
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6004,'自然排烟窗',4,JSON_ARRAY('开启面积','开启方式'));
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6005,'防/排烟设备',5,JSON_ARRAY('报警联动功能','防火阀自动开启功能','送风阀自动开启功能'));
CALL catalog448_add_group('1',5010,'防/排烟系统',10,6006,'防/排烟设备',6,JSON_ARRAY('报警联动功能','挡烟垂壁释放功能','电动排烟阀功能','电动排烟窗功能','排烟风机性能'));

-- 测试：应急照明 5、应急广播 8、消防电话 3。
CALL catalog448_add_group('1',5011,'应急照明和疏散指示标志',11,6101,'应急照明灯',1,JSON_ARRAY('照度','主/备电源切换功能','充电功能','放电功能','应急电源供电时间'));
CALL catalog448_add_group('1',5012,'应急广播系统',12,6201,'扬声器',1,JSON_ARRAY('测试音量/音质','扩音功能','应急强制切换功能'));
CALL catalog448_add_group('1',5012,'应急广播系统',12,6202,'扬声器',2,JSON_ARRAY('扬声器播音质量/音量'));
CALL catalog448_add_group('1',5012,'应急广播系统',12,6203,'卡座',3,JSON_ARRAY('卡座播音/录音功能'));
CALL catalog448_add_group('1',5012,'应急广播系统',12,6204,'分配盘',4,JSON_ARRAY('分配盘分区功能','分配盘选层功能'));
CALL catalog448_add_group('1',5012,'应急广播系统',12,6205,'功放',5,JSON_ARRAY('主/备扩音机切换功能'));
CALL catalog448_add_group('1',5013,'消防专用电话',13,6301,'消防电话主机',1,JSON_ARRAY('通话质量','录音功能','拨打119功能'));

-- 测试：防火分隔 5、消防电梯 4。
CALL catalog448_add_group('1',5014,'防火分隔设施',14,6401,'防火门',1,JSON_ARRAY('启闭功能','密封性能'));
CALL catalog448_add_group('1',5014,'防火分隔设施',14,6402,'电动防火门',2,JSON_ARRAY('自动现场释放功能','信号反馈功能'));
CALL catalog448_add_group('1',5014,'防火分隔设施',14,6403,'防火卷帘',3,JSON_ARRAY('手动控制功能'));
CALL catalog448_add_group('1',5015,'消防电梯系统',15,6501,'消防电梯',1,JSON_ARRAY('回首层功能','应急操作功能','消防电话通话质量','电梯井排水功能'));

-- 测试：细水雾 15、干粉 4、灭火器 1。
CALL catalog448_add_group('1',5016,'细水雾灭火系统',16,6601,'储瓶式',1,JSON_ARRAY('启动装置的启动性能','减压装置减压性能','喷头喷雾性能'));
CALL catalog448_add_group('1',5016,'细水雾灭火系统',16,6602,'泵式',2,JSON_ARRAY('手动启/停泵功能','自动启/停泵功能','主/备泵切换功能','喷头喷雾性能'));
CALL catalog448_add_group('1',5016,'细水雾灭火系统',16,6603,'分区控制阀',3,JSON_ARRAY('手动控制功能','自动控制功能','模拟自动控制功能'));
CALL catalog448_add_group('1',5016,'细水雾灭火系统',16,6604,'细水雾灭火设备',4,JSON_ARRAY('联动控制功能','喷放细水雾功能'));
CALL catalog448_add_group('1',5016,'细水雾灭火系统',16,6605,'末端放水设备',5,JSON_ARRAY('联动功能','水流指示器报警功能','压力开关报警功能'));
CALL catalog448_add_group('1',5017,'干粉灭火系统',17,6701,'驱动气瓶',1,JSON_ARRAY('驱动气瓶压力','干粉储存量'));
CALL catalog448_add_group('1',5017,'干粉灭火系统',17,6702,'干粉灭火设备',2,JSON_ARRAY('报警联动功能','干粉喷放'));
CALL catalog448_add_group('1',5018,'灭火器',18,6801,'灭火器',1,JSON_ARRAY('核对选型、压力和有效期；对同批次灭火器随机抽取一定数量进行灭火、喷射等性能试验'));

-- 保养 30 项（跟随同名巡查一级类目的任务选择）。
CALL catalog448_add_group('2',8002,'火灾自动报警系统',2,8201,'控制室主机',1,JSON_ARRAY('控制室主机清洁'));
CALL catalog448_add_group('2',8005,'消防供水设施',5,8501,'消防水泵',1,JSON_ARRAY('消防水泵外观清洁','消防水泵泵体中心轴保养'));
CALL catalog448_add_group('2',8005,'消防供水设施',5,8502,'消防水泵',2,JSON_ARRAY('消防水泵机械润滑'));
CALL catalog448_add_group('2',8005,'消防供水设施',5,8503,'消防水泵',3,JSON_ARRAY('消防水泵主回路控制回路清洁','消防水泵清洁'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8601,'水泵',1,JSON_ARRAY('水泵清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8602,'控制柜',2,JSON_ARRAY('控制柜清洁'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8603,'阀门丝杆',3,JSON_ARRAY('阀门丝杆清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8604,'室外管道',4,JSON_ARRAY('室外管道清洁、除锈'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8605,'室外消火栓',5,JSON_ARRAY('室外消火栓清洁、除锈'));
CALL catalog448_add_group('2',8006,'消火栓（消防炮）灭火系统',6,8606,'接合器',6,JSON_ARRAY('接合器清洁、除锈'));
CALL catalog448_add_group('2',8007,'自动喷水灭火系统',7,8701,'水泵',1,JSON_ARRAY('水泵清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8007,'自动喷水灭火系统',7,8702,'控制柜',2,JSON_ARRAY('控制柜清洁'));
CALL catalog448_add_group('2',8007,'自动喷水灭火系统',7,8703,'阀门丝杆',3,JSON_ARRAY('阀门丝杆清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8007,'自动喷水灭火系统',7,8704,'室外管道',4,JSON_ARRAY('室外管道清洁、除锈'));
CALL catalog448_add_group('2',8007,'自动喷水灭火系统',7,8705,'接合器',5,JSON_ARRAY('接合器清洁、除锈'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8801,'水泵设备',1,JSON_ARRAY('水泵设备清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8802,'控制柜（箱）',2,JSON_ARRAY('控制柜（箱）清洁'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8803,'阀门丝杆',3,JSON_ARRAY('阀门丝杆清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8804,'室外管道',4,JSON_ARRAY('室外管道清洁、除锈'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8805,'室外消火栓',5,JSON_ARRAY('室外消火栓清洁、除锈'));
CALL catalog448_add_group('2',8008,'泡沫灭火系统',8,8806,'接合器',6,JSON_ARRAY('接合器清洁、除锈'));
CALL catalog448_add_group('2',8009,'气体灭火系统',9,8901,'报警主机',1,JSON_ARRAY('报警主机清洁'));
CALL catalog448_add_group('2',8010,'防/排烟系统',10,90001,'排烟风机',1,JSON_ARRAY('排烟风机清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8010,'防/排烟系统',10,90002,'送风机',2,JSON_ARRAY('送风机清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8010,'防/排烟系统',10,90003,'送风阀',3,JSON_ARRAY('送风阀清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8010,'防/排烟系统',10,90004,'排烟阀',4,JSON_ARRAY('排烟阀清洁、除锈、注润滑油'));
CALL catalog448_add_group('2',8014,'防火分隔设施',14,9401,'防火卷帘',1,JSON_ARRAY('防火卷帘清洁'));
CALL catalog448_add_group('2',8018,'灭火器',18,9801,'灭火器',1,JSON_ARRAY('灭火器清洁'));

-- 强校验：任何一项不符都回滚，避免产生“看起来有标签、实际数量不一致”的半成品。
CALL catalog448_assert();
COMMIT;

DROP PROCEDURE catalog448_add_group;
DROP PROCEDURE catalog448_assert;

SELECT template_type,COUNT(*) AS level3_count
FROM fire_maintenance_template
WHERE level=3 AND template_type IN ('0','1','2')
GROUP BY template_type ORDER BY template_type;
