# 建筑编码唯一约束说明

## 现状（本地库 `dev_manager`，2026-07-27）

`fire_building.building_code` **存在历史重复值**（`del_flag='0'` 有效记录也会重复），例如：

| building_code | 重复条数 |
|---|---|
| B0009 | 4 |
| B0006 / B0010 / B0011 / B0013 | 3 |
| B0005 / B0012 / B0014 / B0019 | 2 |

另有非 `B+数字` 历史格式：`BLD001`、`036`、`2`、`123456` 等。

## 约定

1. **暂不添加** `UNIQUE(building_code)`，否则数据迁移会直接失败。
2. 新编码统一由 `FireBuildingServiceImpl` 在服务端生成：`B` + 至少 4 位数字；并发靠进程内锁 + 重试。
3. 取号只统计匹配 `^B[0-9]+$` 的编码，忽略 `BLD*` / 纯数字等历史编码。
4. 编辑不覆盖历史编码；历史重复数据保持不动；新码从当前最大 B 序号之后递增。

清理重复后再加唯一约束，可参考：

```sql
-- 数据清理后再执行
ALTER TABLE fire_building ADD UNIQUE KEY uk_fire_building_code (building_code);
```
