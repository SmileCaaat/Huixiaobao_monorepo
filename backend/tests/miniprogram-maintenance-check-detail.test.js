const assert = require("assert");
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "../..");
const read = (file) => fs.readFileSync(path.join(root, file), "utf8");

const app = read("miniprogram/app.json");
const deviceWxml = read("miniprogram/pages/task/device.wxml");
const deviceJs = read("miniprogram/pages/task/device.js");
const detailWxml = read("miniprogram/pages/task/check-detail.wxml");
const detailJs = read("miniprogram/pages/task/check-detail.js");

assert.ok(app.includes('"pages/task/check-detail"'), "detail page must be registered");
assert.ok(deviceWxml.includes('bindtap="openCheckDetail"'), "leaf item opens detail page");
assert.ok(deviceJs.includes('"/pages/task/check-detail?taskId="'), "detail navigation keeps task context");
assert.ok(detailWxml.includes("维保详情内容"), "restores full detail title");
assert.ok(detailWxml.includes("是否完好"), "restores intact yes/no field");
assert.ok(detailWxml.includes("设备状态"), "restores device status field");
assert.ok(detailWxml.includes("正常") && detailWxml.includes("故障") && detailWxml.includes("无此设备"), "restores all status choices");
assert.ok(detailWxml.includes("其他说明") && detailWxml.includes("附件"), "restores notes and attachments");
assert.ok(detailJs.includes("api.updateCheckDetail"), "detail form persists through the real API");
assert.ok(detailJs.includes("uploadFile(filePath)"), "attachments use the authenticated upload service");

console.log("miniprogram maintenance check detail tests passed");
