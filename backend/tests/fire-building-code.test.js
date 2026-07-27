/**
 * Building code auto-generation contract tests (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');

const buildingService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireBuildingServiceImpl.java');
const buildingIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireBuildingService.java');
const companyService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireCompanyServiceImpl.java');
const buildingController = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireBuildingController.java');
const mapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireBuildingMapper.xml');
const domain = read('ruoyi-system/src/main/java/com/ruoyi/fire/domain/FireBuilding.java');
const addPage = read('ruoyi-admin/src/main/resources/templates/fire/building/add.html');
const editPage = read('ruoyi-admin/src/main/resources/templates/fire/building/edit.html');
const listPage = read('ruoyi-admin/src/main/resources/templates/fire/building/building.html');
const companyEdit = read('ruoyi-admin/src/main/resources/templates/fire/company/edit.html');

assert.ok(buildingIface.includes('generateNextBuildingCode'), 'iface exposes generateNextBuildingCode');
assert.ok(buildingService.includes('formatBuildingCode'), 'format helper');
assert.ok(buildingService.includes('selectMaxBuildingCodeSeq'), 'max seq query');
assert.ok(buildingService.includes('CODE_LOCK'), 'concurrency lock');
assert.ok(mapperXml.includes('selectMaxBuildingCodeSeq'), 'mapper max seq');
assert.ok(mapperXml.includes('^B[0-9]+$'), 'only B+digits pattern');

assert.ok(buildingService.includes('building.setBuildingCode(null)'), 'clear client code on insert');
assert.ok(buildingController.includes('building.setBuildingCode(null)'), 'controller clears code');
assert.ok(companyService.includes('buildingService.insertBuilding'), 'company reuses insertBuilding');
assert.ok(!/String\.format\("B%04d"/.test(companyService), 'company no longer formats B code itself');

assert.ok(buildingService.includes('building.setBuildingCode(db.getBuildingCode())'), 'preserve db code on update');
assert.ok(mapperXml.includes('building_code is system-generated; never update here'), 'update SQL comment');
assert.ok(!/update id="updateBuilding"[\s\S]*?building_code = #\{buildingCode\}/.test(mapperXml), 'updateBuilding does not set building_code');

assert.ok(!/@NotBlank\(message = ".*buildingCode|@NotBlank\(message = "\u5efa\u7b51\u7f16\u7801/.test(domain)
  && !domain.includes('@NotBlank(message = "\u5efa\u7b51\u7f16\u7801\u4e0d\u80fd\u4e3a\u7a7a")'), 'buildingCode not required');

assert.ok(!/name="buildingCode"/.test(addPage), 'add page no buildingCode input');
assert.ok(editPage.includes('form-control-static') && editPage.includes('buildingCode'), 'edit shows code readonly');
assert.ok(!/name="buildingCode"/.test(editPage), 'edit page no editable buildingCode input');
assert.ok(listPage.includes("field: 'buildingCode'"), 'list keeps buildingCode column');

assert.ok(companyEdit.includes('buildingId'), 'company edit passes buildingId');
assert.ok(companyService.includes('keepIds'), 'company sync keeps existing buildings');

console.log('fire-building-code tests: all assertions passed');
