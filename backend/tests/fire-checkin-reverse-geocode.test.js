/**
 * Check-in reverse geocode contract tests (source assertions).
 * Covers success path wiring, illegal params, missing key, timeout, and submit-time re-resolve.
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const root = path.resolve(backend, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');
const readRoot = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const geoImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/AmapGeoCodingServiceImpl.java');
const geoIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IGeoCodingService.java');
const mapProps = read('ruoyi-common/src/main/java/com/ruoyi/common/config/MapProperties.java');
const yml = read('ruoyi-admin/src/main/resources/application.yml');
const checkInSvc = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireCheckInServiceImpl.java');
const apiJs = readRoot('miniprogram/api/index.js');
const pageJs = readRoot('miniprogram/pages/checkin/index.js');
const pageWxml = readRoot('miniprogram/pages/checkin/index.wxml');
const pageWxss = readRoot('miniprogram/pages/checkin/index.wxss');

// --- API surface ---
assert.ok(controller.includes('@GetMapping("/checkIn/reverseGeocode")'), 'controller reverseGeocode route');
assert.ok(controller.includes('geoCodingService.reverseGeocode'), 'controller delegates to geo service');
assert.ok(geoIface.includes('reverseGeocode'), 'iface reverseGeocode');
assert.ok(geoIface.includes('resolveChineseAddress'), 'iface resolveChineseAddress');

// --- success payload keys ---
assert.ok(geoImpl.includes('data.put("longitude"'), 'success returns longitude');
assert.ok(geoImpl.includes('data.put("latitude"'), 'success returns latitude');
assert.ok(geoImpl.includes('data.put("address"'), 'success returns address');
assert.ok(geoImpl.includes('restapi.amap.com/v3/geocode/regeo'), 'uses amap regeo');

// --- illegal params ---
assert.ok(geoImpl.includes('longitude == null || latitude == null'), 'null coordinate rejected');
assert.ok(geoImpl.includes('longitude < -180 || longitude > 180'), 'longitude range');
assert.ok(geoImpl.includes('latitude < -90 || latitude > 90'), 'latitude range');
assert.ok(
  geoImpl.includes('\u7ecf\u7eac\u5ea6\u53c2\u6570\u975e\u6cd5') || geoImpl.includes('经纬度参数非法'),
  'illegal param message'
);

// --- missing key / third-party failure ---
assert.ok(geoImpl.includes('mapProperties.getAmapWebKey()'), 'reads server key');
assert.ok(
  geoImpl.includes('\u5730\u5740\u89e3\u6790\u670d\u52a1\u672a\u914d\u7f6e') || geoImpl.includes('地址解析服务未配置'),
  'missing key message'
);
assert.ok(geoImpl.includes('!"1".equals(status)'), 'third-party status failure');
assert.ok(
  geoImpl.includes('\u5730\u5740\u89e3\u6790\u5931\u8d25') || geoImpl.includes('地址解析失败'),
  'third-party fail message'
);

// --- timeout ---
assert.ok(geoImpl.includes('SocketTimeoutException'), 'timeout catch');
assert.ok(
  geoImpl.includes('\u5730\u5740\u89e3\u6790\u8d85\u65f6') || geoImpl.includes('地址解析超时'),
  'timeout message'
);
assert.ok(geoImpl.includes('setConnectTimeout'), 'connect timeout configured');
assert.ok(geoImpl.includes('setReadTimeout'), 'read timeout configured');

// --- config: key only from env, not hardcoded ---
assert.ok(mapProps.includes('@ConfigurationProperties(prefix = "ruoyi.map")'), 'map properties prefix');
assert.ok(yml.includes('amapWebKey: ${AMAP_WEB_KEY:}'), 'yml binds AMAP_WEB_KEY');
assert.ok(!/amapWebKey:\s*["']?[a-zA-Z0-9]{16,}/.test(yml), 'no hardcoded amap key in yml');
assert.ok(!pageJs.includes('restapi.amap.com'), 'mini program does not call amap');
assert.ok(!apiJs.includes('restapi.amap.com'), 'api layer does not call amap');

// --- submit-time authoritative address ---
assert.ok(checkInSvc.includes('validateAddressForMobile'), 'mobile address validation');
assert.ok(checkInSvc.includes('geoCodingService.resolveChineseAddress'), 'server re-resolves address');
assert.ok(checkInSvc.includes('checkIn.setAddress(resolved)'), 'overwrite client address');

// --- mini program wiring ---
assert.ok(apiJs.includes('/api/fire/checkIn/reverseGeocode'), 'api reverseGeocode path');
assert.ok(pageJs.includes('api.reverseGeocode'), 'page calls reverseGeocode');
assert.ok(pageJs.includes('type: "gcj02"'), 'uses gcj02');
assert.ok(pageJs.includes('ADDR_FAIL_GEO'), 'geo fail copy');
assert.ok(!/addressText:\s*latitude\s*\+/.test(pageJs), 'does not fake address with lat,lng');
assert.ok(pageWxml.includes('checkin-page'), 'scoped page class');
assert.ok(pageWxml.includes('checkin-page__actions'), 'side-by-side actions');
assert.ok(!pageWxml.includes('scroll-view'), 'no scroll-view');
assert.ok(pageWxss.includes('.checkin-page'), 'scoped styles');
assert.ok(pageWxss.includes('height: 100vh'), 'one-screen height');
assert.ok(pageWxss.includes('-webkit-line-clamp: 2'), 'address max 2 lines');
assert.ok(/height:\s*8[89]rpx|height:\s*9[0-6]rpx/.test(pageWxss), 'compact remark height');

console.log('fire-checkin-reverse-geocode tests: all assertions passed');
