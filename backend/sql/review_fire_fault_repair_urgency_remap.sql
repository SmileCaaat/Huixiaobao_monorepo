-- 故障报修紧急程度编码纠偏方案（可审核，默认不执行）
-- 背景：旧版小程序曾使用 1=一般 / 2=紧急 / 3=特急，与后台标准 0/1/2 错位。
-- 后台标准（UrgencyLevel）：0=一般，1=紧急，2=特急。
--
-- 重要：不要对全部数据执行 1->0、2->1、3->2。
-- 后台合法数据本身就使用 1=紧急、2=特急，盲目迁移会把正确数据改坏。
-- 仅在能通过记录来源或上线时间准确识别“小程序错误写入”时执行。
--
-- 1) 先查看分布
SELECT urgency_level, COUNT(*) AS cnt
FROM fire_fault_repair
GROUP BY urgency_level
ORDER BY urgency_level;

-- 2) 预览疑似错误数据（存在非法值 "3" 的记录）
SELECT repair_id, repair_no, urgency_level, create_by, create_time, is_reported
FROM fire_fault_repair
WHERE urgency_level = '3';

-- 3) 若能确认某批记录来自错误小程序，再按批次迁移。
--    下面语句默认注释；执行前必须替换为可识别错误数据的 WHERE 条件，
--    例如按 create_time / create_by / is_reported 等缩小范围。
-- UPDATE fire_fault_repair
-- SET urgency_level = CASE urgency_level
--         WHEN '1' THEN '0'
--         WHEN '2' THEN '1'
--         WHEN '3' THEN '2'
--         ELSE urgency_level
--     END
-- WHERE urgency_level IN ('1', '2', '3')
--   AND create_time >= '2026-07-01 00:00:00'
--   AND create_time <  '2026-07-24 00:00:00'
--   AND is_reported = '1';

-- 4) 仅修正非法值 "3" 的保守方案（不改动合法 0/1/2）
-- UPDATE fire_fault_repair
-- SET urgency_level = '2'
-- WHERE urgency_level = '3';
