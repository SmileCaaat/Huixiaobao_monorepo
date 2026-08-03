const assert = require("assert");
const fs = require("fs");
const path = require("path");

const backend = path.resolve(__dirname, "..");
const root = path.resolve(backend, "..");
const read = (file) => fs.readFileSync(file, "utf8");
const label = "\u6d88\u9632\u7ef4\u62a4"; // 消防维护
const legacyLabel = "\u6d88\u9632\u7ef4\u62a4\uff08\u5de1\u67e5\u6d4b\u8bd5\uff09"; // 消防维护（巡查测试）
const patrol = "\u5de1\u67e5";
const fireTest = "\u6d4b\u8bd5";
const upkeep = "\u4fdd\u517b";

const taskPage = read(path.join(backend, "ruoyi-admin/src/main/resources/templates/fire/task/task.html"));
const detailPage = read(path.join(backend, "ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_detail.html"));
const systemPage = read(path.join(backend, "ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html"));
const equipmentPage = read(path.join(backend, "ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_equipment.html"));
const miniDetail = read(path.join(root, "miniprogram/pages/task/detail.js"));
const miniSystem = read(path.join(root, "miniprogram/pages/task/system.wxml"));
const equipmentGroup = read(path.join(backend, "ruoyi-system/src/main/java/com/ruoyi/fire/domain/dto/FireInspectionTestEquipmentGroup.java"));
const taskService = read(path.join(backend, "ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java"));
const groupKeys = read(path.join(backend, "ruoyi-system/src/main/java/com/ruoyi/fire/service/support/FireInspectionTestGroupKeys.java"));
const taskController = read(path.join(backend, "ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java"));

assert.ok(taskPage.includes(label), "task action and modal use 消防维护");
assert.ok(!taskPage.includes(legacyLabel), "task page drops the （巡查测试） suffix");
assert.ok(taskPage.includes("actions.push('<br/>')") || taskPage.includes('actions.push("<br/>")'), "ops column forces a second row");
assert.ok(detailPage.includes(label), "category page uses 消防维护");
assert.ok(!detailPage.includes(legacyLabel), "category page drops the （巡查测试） suffix");
assert.ok(!detailPage.includes('${task.taskName}'), "category page does not append the task name to the title");
assert.ok(systemPage.includes(label), "secondary category page uses 消防维护");
assert.ok(equipmentPage.includes(label), "check item page uses 消防维护");
assert.ok(systemPage.includes("inspection-type-label"), "PC secondary categories render the small type label");
assert.ok(systemPage.includes("equipment.recordTypeLabel"), "PC secondary category labels come from the backend");
assert.ok(
  miniDetail.includes("\\u5de1\\u67e5\\u6d4b\\u8bd5") ||
    fs.readFileSync(path.join(root, "miniprogram/pages/task/detail.json"), "utf8").includes(label),
  "mini task title uses 消防维护"
);
assert.ok(miniSystem.includes("inspection-type-label"), "mini secondary categories render the small type label");
assert.ok(miniSystem.includes("item.recordTypeLabel"), "mini secondary category labels come from the backend");
assert.ok(equipmentGroup.includes("recordTypeLabel"), "equipment DTO exposes the backend-generated type label");
assert.ok(taskService.includes(`return "${patrol}"`), "backend labels patrol records");
assert.ok(taskService.includes(`return "${fireTest}"`), "backend labels test records");
assert.ok(taskService.includes(`return "${upkeep}"`), "backend labels upkeep records");
assert.ok(taskService.includes("categoryBusinessKey(l1)"), "same-name patrol and test systems share one primary category");
assert.ok(taskService.includes("equipmentBusinessKey(l2)"), "secondary categories remain isolated by record type");
assert.ok(taskService.includes("comparingInt(this::recordTypeOrder)"), "patrol secondary categories are listed before test categories");
assert.ok(groupKeys.includes("categoryBusinessKey"), "group keys support name-based primary merging");
assert.ok(groupKeys.includes('return "t:" + type + "|" + key'), "secondary group keys include the patrol/test type");
assert.ok(groupKeys.includes('!"2".equals(type)'), "secondary group keys keep upkeep isolated from patrol and test");
assert.ok(
  taskController.includes("FireInspectionTestKeys.businessKey") ||
    taskController.includes("categoryBusinessKey"),
  "legacy primary links resolve category keys"
);
assert.ok(
  taskController.includes("FireInspectionTestKeys.businessKey") ||
    taskController.includes("equipmentBusinessKey"),
  "legacy secondary links resolve equipment keys"
);

console.log("maintenance-patrol-label tests: all assertions passed");
