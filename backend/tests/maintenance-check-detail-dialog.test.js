const assert = require("assert");
const fs = require("fs");
const path = require("path");

const equipmentTemplatePath = path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_equipment.html"
);
const detailTemplatePath = path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_item_detail.html"
);
const equipmentPage = fs.readFileSync(equipmentTemplatePath, "utf8");
const detailPage = fs.readFileSync(detailTemplatePath, "utf8");
const repairAddPage = fs.readFileSync(path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/resources/templates/fire/repair/add.html"
), "utf8");
const taskController = fs.readFileSync(path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java"
), "utf8");
const repairController = fs.readFileSync(path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java"
), "utf8");

assert.ok(equipmentPage.includes('window.location.href = prefix + "/inspectionTestItemDetail/"'),
  "lowest-level item opens the standalone maintenance detail page");
assert.ok(detailPage.includes("维保详情内容"), "detail page restores the maintenance detail title");
assert.ok(detailPage.includes('name="isGood"'), "detail page includes the good-condition choice");
assert.ok(detailPage.includes('id="deviceStatus"'), "detail page includes device status controls");
assert.ok(detailPage.includes('data-status="1"') && detailPage.includes('data-status="2"') && detailPage.includes('data-status="3"'),
  "detail page includes normal, fault and no-device states");
assert.ok(detailPage.includes('id="otherNotes"'), "detail page includes other notes");
assert.ok(detailPage.includes('id="fileUpload"'), "detail page includes attachment upload");
assert.ok(detailPage.includes('url: prefix + "/updateCheckDetail"'), "detail page saves through the unified detail API");
assert.ok(detailPage.includes("checkResult: selectedCheckResult"), "detail page persists device status");
assert.ok(detailPage.includes("otherNotes: otherNotes"), "detail page persists other notes");
assert.ok(detailPage.includes("faultImages: imageUrls == null ? '' : imageUrls"), "detail page persists attachments");
assert.ok(!detailPage.includes("<h4>基本信息</h4>"), "obsolete basic information card is removed");
assert.ok(detailPage.includes('id="handlingPanel"') && detailPage.includes("现场解决") && detailPage.includes("故障报修"),
  "fault status exposes both handling measures");
assert.ok(detailPage.includes("title: '提示'") && detailPage.includes("是否上报故障？") && detailPage.includes("btn: ['否', '是']"),
  "fault repair asks whether to report with no/yes choices");
assert.ok(detailPage.includes("top.$.modal.open('新增故障报修'"), "confirmed report opens the add-repair function");
assert.ok(detailPage.includes("repairSuggestion: selectedHandlingMeasure"), "handling measure is persisted");
assert.ok(taskController.includes('mmap.put("repairCompanyId"') && taskController.includes('mmap.put("repairSystemTypeName"'),
  "maintenance detail supplies linked repair context");
assert.ok(repairController.includes('@RequestParam(value = "linked"') && repairController.includes('normalizeSystemName'),
  "repair controller resolves linked locked values");
assert.ok(repairAddPage.includes('th:disabled="${linkedRepair}"') && repairAddPage.includes('th:readonly="${linkedRepair}"'),
  "linked repair locks select and text fields");
assert.ok(repairAddPage.includes('<input type="hidden" name="isReported" value="1" th:if="${linkedRepair}">'),
  "linked repair fixes report choice to yes");

console.log("maintenance check detail page tests: all assertions passed");
