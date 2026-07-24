const assert = require('assert');
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const read = (p) => fs.readFileSync(path.join(root, p), 'utf8');

const indexHtml = read('ruoyi-admin/src/main/resources/templates/index.html');
const indexTop = read('ruoyi-admin/src/main/resources/templates/index-topnav.html');
const indexJs = read('ruoyi-admin/src/main/resources/static/ruoyi/index.js');
const commonJs = read('ruoyi-admin/src/main/resources/static/ruoyi/js/common.js');
const loginJs = read('ruoyi-admin/src/main/resources/static/ruoyi/login.js');
const loginCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java');
const detailHtml = read('ruoyi-admin/src/main/resources/templates/fire/repair/detail.html');
const ryUi = read('ruoyi-admin/src/main/resources/static/ruoyi/js/ry-ui.js');

assert.ok(indexHtml.includes('fa-angle-double-left'), 'left scroll icon');
assert.ok(indexHtml.includes('tabLeft') && /tabLeft[^>]*title=/.test(indexHtml.replace(/\n/g, ' ')), 'left scroll has title');
assert.ok(indexHtml.includes('tabBack'), 'top back button');
assert.ok(indexTop.includes('fa-angle-double-left') && indexTop.includes('tabBack'), 'topnav scroll+back');
assert.ok(indexJs.includes('scrollTabLeft'), 'keep scrollTabLeft');
assert.ok(indexJs.includes("$('.tabBack')"), 'bind tabBack');
assert.ok(indexJs.includes('history.replaceState'), 'hash mode uses replaceState');
assert.ok(loginJs.includes("location.replace(ctx + 'index')"), 'login uses replace');
assert.ok(!/location\.href\s*=\s*ctx\s*\+\s*'index'/.test(loginJs), 'login must not use href to index');
assert.ok(loginCtrl.includes('isAuthenticated()') && loginCtrl.includes('redirect:/index'), 'auth login redirect');
assert.ok(commonJs.includes('goBackPage') && commonJs.includes('isUnsafeNavUrl'), 'safe back helpers');
assert.ok(!/history\.back\s*\(/.test(commonJs.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '')), 'no history.back call in common');
assert.ok(detailHtml.includes('goBackPage'), 'repair detail uses goBackPage');
assert.ok(ryUi.includes('back: function'), '$.modal/operate back api');

console.log('nav-back tests: all assertions passed');
