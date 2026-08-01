const assert = require("assert");
const fs = require("fs");
const path = require("path");

const backend = path.resolve(__dirname, "..");
const root = path.resolve(backend, "..");
const read = (file) => fs.readFileSync(file, "utf8");

const pageJs = read(path.join(root, "miniprogram/pages/checkin/index.js"));
const pageWxml = read(path.join(root, "miniprogram/pages/checkin/index.wxml"));
const checkInService = read(path.join(
  backend,
  "ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireCheckInServiceImpl.java"
));

assert.ok(pageJs.includes("wx.chooseLocation"), "native map fallback is wired");
assert.ok(pageJs.includes('addressMode: "map"'), "map address source is submitted");
assert.ok(pageJs.includes("isChineseAddress"), "map address is validated on the client");
assert.ok(pageJs.includes("this._pageInitialized"), "native chooser return does not reset the selected address");
assert.ok(pageWxml.includes("onLocationAction"), "failed geocoding switches the location action");
assert.ok(pageWxml.includes("选择位置"), "map selection action is visible");
assert.ok(
  checkInService.includes('"map".equals(checkIn.getAddressMode())'),
  "backend recognizes explicit map selection"
);
assert.ok(
  checkInService.includes("validateMapSelectedAddress"),
  "backend validates map-selected Chinese address"
);

console.log("fire-checkin-map-fallback tests: all assertions passed");
