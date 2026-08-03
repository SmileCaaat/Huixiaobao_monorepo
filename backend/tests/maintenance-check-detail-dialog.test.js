const assert = require("assert");
const fs = require("fs");
const path = require("path");

const templatePath = path.resolve(
  __dirname,
  "../ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_equipment.html"
);
const page = fs.readFileSync(templatePath, "utf8");

assert.ok(page.includes("title: '检查详情'"), "lowest-level item opens the check detail dialog");
assert.ok(page.includes('name="isGood"'), "dialog includes the good-condition radio choice");
assert.ok(page.includes('id="deviceStatus"'), "dialog includes the device status selector");
assert.ok(page.includes('id="otherNotes"'), "dialog includes other notes");
assert.ok(page.includes('id="fileUpload"'), "dialog includes attachment upload");
assert.ok(page.includes('btn: [\'取消\', \'保存\']'), "dialog buttons match the reference order");
assert.ok(page.includes('url: prefix + "/updateCheckDetail"'), "dialog saves through the unified detail API");
assert.ok(page.includes("checkResult: deviceStatus"), "dialog persists device status");
assert.ok(page.includes("faultDescription: faultDescription"), "dialog persists fault details");
assert.ok(page.includes("otherNotes: otherNotes"), "dialog persists other notes");
assert.ok(page.includes("faultImages: imageUrls || ''"), "dialog persists the complete attachment list");
assert.ok(!page.includes('window.location.href = prefix + "/inspectionTestItemDetail/"'),
  "lowest-level item no longer navigates to the standalone other-notes page");

const inlineScriptMatch = page.match(/<script th:inline="javascript">([\s\S]*?)<\/script>/);
assert.ok(inlineScriptMatch, "template contains its inline script");
const parsableScript = inlineScriptMatch[1]
  .replace("[[${checkItems}]]", "[]")
  .replace("[[${task.taskId}]]", "1");
new Function(parsableScript);

console.log("maintenance-check-detail-dialog tests: all assertions passed");
