const assert = require("assert");
const fs = require("fs");
const path = require("path");

const backend = path.resolve(__dirname, "..");
const read = (file) => fs.readFileSync(file, "utf8");

const migration = read(path.join(backend, "sql/upgrade_maintenance_catalog_449.sql"));
const service = read(path.join(
  backend,
  "ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java"
));
const groupKeys = read(path.join(
  backend,
  "ruoyi-system/src/main/java/com/ruoyi/fire/service/support/FireInspectionTestGroupKeys.java"
));
const pcSystemPage = read(path.join(
  backend,
  "ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html"
));
const miniSystemPage = read(path.resolve(
  backend,
  "../miniprogram/pages/task/system.wxml"
));

const itemCounts = { 1: 0, 2: 0 };
for (const line of migration.split(/\r?\n/)) {
  const call = line.match(
    /^CALL catalog448_add_group\('([12])',[\s\S]*,JSON_ARRAY\((.*)\)\);$/
  );
  if (!call) continue;
  const stringLiterals = call[2].match(/'(?:''|[^'])*'/g) || [];
  itemCounts[call[1]] += stringLiterals.length;
}

assert.strictEqual(itemCounts[1], 182, "测试汇总表必须生成 182 个三级项");
assert.strictEqual(itemCounts[2], 30, "保养汇总表必须生成 30 个三级项");
assert.ok(migration.includes("patrol_count<>237"), "迁移必须校验巡查 237 项");
assert.ok(migration.includes("category_count<>18"), "迁移必须校验 18 个一级类目");
assert.ok(migration.includes("category_detail_mismatch<>0"), "迁移必须逐一级类目校验三种标签数量");
assert.ok(migration.includes("楼层/区域末端试验阀门处压力值"), "自动喷水巡查项已补齐");
assert.ok(migration.includes("分区控制阀外观"), "细水雾附件巡查项已补齐");
assert.ok(migration.includes("SET MESSAGE_TEXT=error_message"), "数量不一致时迁移必须中止");
assert.ok(
  migration.indexOf("CREATE PROCEDURE catalog448_assert") < migration.indexOf("START TRANSACTION"),
  "强校验过程必须在模板事务开始前创建"
);
assert.ok(
  migration.indexOf("COMMIT;") < migration.lastIndexOf("DROP PROCEDURE catalog448_assert"),
  "存储过程清理不能提前提交模板事务"
);

assert.ok(service.includes('generateMissingRecordsForLevel1Ids(taskId, selectedUpkeepIds, "2"'),
  "新任务必须生成保养记录");
assert.ok(service.includes('recordType.equals(normalizeTemplateType(t))'),
  "生成记录时必须隔离巡查、测试和保养模板");
assert.ok(service.includes('return "巡查"'), "后端统一生成巡查标签");
assert.ok(service.includes('return "测试"'), "后端统一生成测试标签");
assert.ok(service.includes('return "保养"'), "后端统一生成保养标签");
assert.ok(groupKeys.includes('return "t:" + type + "|" + key'),
  "同名二级类目必须按标签类型隔离");
assert.ok(pcSystemPage.includes("equipment.recordTypeLabel"), "PC 二级明细展示后端标签");
assert.ok(miniSystemPage.includes("item.recordTypeLabel"), "小程序二级明细展示后端标签");

console.log("maintenance-catalog-449 tests: all assertions passed");
