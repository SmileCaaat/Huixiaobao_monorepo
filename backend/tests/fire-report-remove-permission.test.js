/**
 * ά������ɾ����ť��hasPermi ���� CSS ���ַ��������ܵ�����ʹ��
 */
const fs = require('fs');
const path = require('path');
const assert = require('assert');

const reportHtml = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/resources/templates/fire/report/report.html'),
  'utf8'
);
const controllerJava = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireReportController.java'),
  'utf8'
);

assert.ok(
  reportHtml.includes("@permission.isPermitted('fire:report:remove')"),
  'report.html should use isPermitted for boolean canRemove'
);
assert.ok(
  reportHtml.includes('var canRemove'),
  'report.html should define canRemove boolean'
);
assert.ok(
  !/if\s*\(\s*removeFlag\s*\)/.test(reportHtml),
  'report.html must not use if (removeFlag)'
);
assert.ok(
  !reportHtml.includes("@permission.hasPermi('fire:report:remove')"),
  'report.html must not use hasPermi for row delete gate'
);
assert.ok(
  /if\s*\(\s*canRemove\s*\)/.test(reportHtml),
  'row delete should be gated by if (canRemove)'
);
assert.ok(
  reportHtml.includes('shiro:hasPermission="fire:report:remove"'),
  'toolbar batch delete should keep shiro:hasPermission'
);
assert.ok(
  /@RequiresPermissions\("fire:report:remove"\)[\s\S]*@PostMapping\("\/remove"\)/.test(controllerJava),
  'Controller remove must keep RequiresPermissions fire:report:remove'
);

// Simulate RuoYi hasPermi vs isPermitted semantics
function shouldShowDeleteWithHasPermiBug(hasPermiCss) {
  // BUG: non-empty "hidden" is truthy
  if (hasPermiCss) {
    return true;
  }
  return false;
}
function shouldShowDeleteWithIsPermitted(canRemove) {
  if (canRemove) {
    return true;
  }
  return false;
}

assert.strictEqual(shouldShowDeleteWithHasPermiBug('hidden'), true, 'documents the bug');
assert.strictEqual(shouldShowDeleteWithIsPermitted(false), false, 'fixed gate hides delete');
assert.strictEqual(shouldShowDeleteWithIsPermitted(true), true, 'fixed gate shows delete');

console.log('fire-report-remove-permission.test.js passed');
