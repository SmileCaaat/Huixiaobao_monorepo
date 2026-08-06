/**
 * Fire inspection module contract tests (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const root = path.resolve(__dirname, '../..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');
const readRoot = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const domain = read('ruoyi-system/src/main/java/com/ruoyi/fire/domain/FireInspection.java');
const mapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireInspectionMapper.xml');
const adminCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireInspectionController.java');
const miniCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const categorySvc = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTemplateCategoryServiceImpl.java');
const permIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireDataPermissionService.java');
const serviceImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireInspectionServiceImpl.java');
const listPage = read('ruoyi-admin/src/main/resources/templates/fire/inspection/inspection.html');
const addPage = read('ruoyi-admin/src/main/resources/templates/fire/inspection/add_new.html');
const upgradeSql = read('sql/upgrade_fire_inspection_module.sql');
const keysSql = read('sql/upgrade_fire_inspection_template_keys.sql');
const rollbackSql = read('sql/rollback_fire_inspection_module.sql');
const miniForm = readRoot('miniprogram/pages/inspection/form.js');
const miniFormWxml = readRoot('miniprogram/pages/inspection/form.wxml');
const miniFormWxss = readRoot('miniprogram/pages/inspection/form.wxss');
const miniApi = readRoot('miniprogram/api/index.js');
const miniIndexHome = readRoot('miniprogram/pages/index/index.js');

assert.ok(domain.includes('categoryKey'), 'domain categoryKey');
assert.ok(domain.includes('equipmentKey'), 'domain equipmentKey');
assert.ok(domain.includes('maintenanceStandard'), 'domain maintenanceStandard');
assert.ok(domain.includes('keyword'), 'domain keyword');
assert.ok(domain.includes('inspectionMonth'), 'domain inspectionMonth');

assert.ok(mapperXml.includes('category_key'), 'mapper category_key');
assert.ok(mapperXml.includes('equipment_key'), 'mapper equipment_key');
assert.ok(mapperXml.includes('maintenance_standard'), 'mapper maintenance_standard');
assert.ok(mapperXml.includes('keyword'), 'mapper keyword filter');
assert.ok(mapperXml.includes("date_format(inspection_time, '%Y-%m')"), 'mapper month filter');
assert.ok(mapperXml.includes("del_flag = '2'"), 'soft delete');

assert.ok(categorySvc.includes('getAllTemplatesWithCache'), 'shared category uses template cache');
assert.ok(categorySvc.includes('listInspectionLevel1Categories'), 'shared level1');
assert.ok(categorySvc.includes('listEquipmentsByCategoryKey'), 'shared equipments');

assert.ok(adminCtrl.includes('templateCategoryService'), 'admin uses template category service');
assert.ok(adminCtrl.includes('/equipmentTypes'), 'admin equipmentTypes api');
assert.ok(adminCtrl.includes('applyInspectionListScope'), 'admin list scope');
assert.ok(adminCtrl.includes('assertCanAccessInspection'), 'admin assert access');
assert.ok(!adminCtrl.includes('FireEquipmentCategory'), 'admin no flat enum');
assert.ok(!adminCtrl.includes('systemTypeService'), 'admin no systemTypeService');

assert.ok(miniCtrl.includes('/inspection/templateCategories'), 'mini templateCategories');
assert.ok(miniCtrl.includes('/inspection/templateCategories/{categoryKey'), 'mini template equipments');
assert.ok(miniCtrl.includes('applyInspectionListScope'), 'mini list scope');
assert.ok(miniCtrl.includes('assertCanAccessInspection'), 'mini detail assert');
assert.ok(!miniCtrl.includes('RequestMapping("/fire/task/inspectionTest")'), 'no task inspectionTest remap');
assert.ok(!miniCtrl.includes('selectTopLevelSystemTypes()'), 'mini no fire_system_type top level for inspection');

assert.ok(permIface.includes('applyInspectionListScope'), 'perm iface');
assert.ok(permIface.includes('assertCanAccessInspection'), 'perm assert');

assert.ok(serviceImpl.includes('enrichSnapshots'), 'service snapshots');
assert.ok(serviceImpl.includes('getMaintenanceStandard'), 'service maintenance standard');
assert.ok(serviceImpl.includes('templateCategoryService'), 'service template category');
assert.ok(serviceImpl.includes('setSystemTypeId(null)'), 'clear systemTypeId');

assert.ok(listPage.includes('name="keyword"'), 'list keyword');
assert.ok(listPage.includes('inspectionMonth'), 'list month');
assert.ok(listPage.includes('maintenanceStandard'), 'list standard');
assert.ok(listPage.includes('modalName: "\u5de1\u67e5\u6d4b\u8bd5"') || listPage.includes('\u5de1\u67e5\u6d4b\u8bd5') || listPage.includes('modalName: "\u5de1\u68c0\u6d4b\u8bd5"') || listPage.includes('\u5de1\u68c0\u6d4b\u8bd5'), 'list title');
assert.ok(addPage.includes('categoryKey'), 'add categoryKey');
assert.ok(addPage.includes('equipmentKey'), 'add equipmentKey');
assert.ok(addPage.includes('linkedTemplateCategory'), 'linked add maps template category');
assert.ok(addPage.includes('linkedTemplateEquipment'), 'linked add maps template equipment');
assert.ok(addPage.includes('linkedBuildingId'), 'linked add prefills building');
assert.ok(adminCtrl.includes('resolveTemplateCategory'), 'admin resolves template category for linked add');
assert.ok(adminCtrl.includes('linkedBuildingId'), 'admin passes linked building id');
assert.ok(adminCtrl.includes('resolveTemplateEquipment'), 'admin resolves template equipment for linked add');
assert.ok(categorySvc.includes('normalizeIncomingCategoryKey'), 'category service accepts encoded keys');
assert.ok(categorySvc.includes('normalizeIncomingEquipmentKey'), 'equipment service accepts typed keys');
assert.ok(addPage.includes('FireInspectionPickers'), 'add uses shared pickers');
assert.ok(addPage.includes('initFloorPicker'), 'add floor picker');
assert.ok(addPage.includes('initDatePicker'), 'add date picker');
const pickerJs = read('ruoyi-admin/src/main/resources/static/ruoyi/js/fire-inspection-pickers.js');
const pickerCss = read('ruoyi-admin/src/main/resources/static/ruoyi/css/fire-inspection-pickers.css');
assert.ok(pickerJs.includes('-5'), 'picker floors include -5F');
assert.ok(pickerJs.includes('i <= 99') || pickerJs.includes('i<=99'), 'picker floors up to 99F');
assert.ok(!/i\s*<=\s*100/.test(pickerJs), 'picker floors not to 100');
assert.ok(pickerJs.includes('????????') || pickerJs.includes('\u8bf7\u9009\u62e9\u4e00\u9879'), 'picker chinese title');
assert.ok(pickerJs.includes('???????') || pickerJs.includes('\u9009\u62e9\u65e5\u671f'), 'picker chinese date title');
assert.ok(pickerJs.includes('daysInMonth'), 'picker leap/month days');
assert.ok(pickerCss.includes('repeat(4, 1fr)'), 'picker floor 4 columns');
assert.ok(!pickerJs.includes("'0F'") && !pickerJs.includes('"0F"'), 'picker no 0F literal');
assert.ok(!pickerJs.includes('\uFFFD'), 'picker js no replacement char');

assert.ok(upgradeSql.includes('system_type_id'), 'upgrade col');
assert.ok(upgradeSql.includes('fire:inspection:view'), 'upgrade menu');
assert.ok(upgradeSql.includes('/fire/inspection'), 'upgrade route');
assert.ok(upgradeSql.includes('NOT EXISTS'), 'upgrade idempotent');
assert.ok(keysSql.includes('category_key'), 'keys sql category_key');
assert.ok(keysSql.includes('equipment_key'), 'keys sql equipment_key');
assert.ok(rollbackSql.includes('DROP COLUMN'), 'rollback columns');

assert.ok(miniForm.includes('\u8bf7\u5148\u9009\u62e9\u7cfb\u7edf\u540d\u79f0'), 'mini tip before equipment');
assert.ok(miniForm.includes('getInspectionTemplateCategories'), 'mini load template categories');
assert.ok(miniForm.includes('getInspectionTemplateEquipments'), 'mini load template equipments');
assert.ok(miniForm.includes('categoryKey'), 'mini form categoryKey');
assert.ok(miniForm.includes('equipmentKey'), 'mini form equipmentKey');
assert.ok(miniForm.includes('buildFloors') || miniForm.includes('-5'), 'mini floors');
assert.ok(miniForm.includes('openFloorSheet'), 'mini floor sheet');
assert.ok(miniForm.includes('openDateSheet'), 'mini date sheet');
assert.ok(miniForm.includes('daysInMonth'), 'mini leap/month days');
assert.ok(miniForm.includes('saving'), 'mini prevent double submit');
assert.ok(miniFormWxml.includes('floor-grid'), 'mini floor grid');
assert.ok(miniFormWxml.includes('\u8bf7\u9009\u62e9\u4e00\u9879'), 'mini floor title');
assert.ok(miniFormWxml.includes('picker-view-column'), 'mini date picker columns');
assert.ok(miniFormWxss.includes('repeat(4, 1fr)'), 'mini floor 4 columns');
assert.ok(miniApi.includes('/api/fire/inspection/templateCategories'), 'api templateCategories');
assert.ok(miniApi.includes('/equipments'), 'api template equipments');
assert.ok(miniIndexHome.includes('url: "/pages/inspection/index"'), 'home entry present');
assert.ok(miniIndexHome.includes('name: "\u5de1\u68c0\u6d4b\u8bd5"'), 'home entry name');

console.log('fire-inspection-module tests: all assertions passed');
