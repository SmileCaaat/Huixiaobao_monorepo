/**
 * 故障报修员工工作台：分类白名单与紧急程度展示兜底
 */
const assert = require('assert');

function normalizeWorkbenchCategory(category) {
  if (category === 'processing' || category === 'completed' || category === 'reported'
      || category === 'assignedPending') {
    return category;
  }
  return 'assignedPending';
}

function urgencyDisplay(value) {
  if (value === '0' || value === 0) return '一般';
  if (value === '1' || value === 1) return '紧急';
  if (value === '2' || value === 2) return '特急';
  if (value == null || value === '') return '';
  return '未知';
}

function canStartRepair(row, currentUserId) {
  return String(row.repairUserId) === String(currentUserId)
    && row.repairStatus === '1'
    && !row.startTime;
}

function canCompleteRepair(row, currentUserId) {
  return String(row.repairUserId) === String(currentUserId)
    && row.repairStatus === '1'
    && !!row.startTime;
}

assert.strictEqual(normalizeWorkbenchCategory('processing'), 'processing');
assert.strictEqual(normalizeWorkbenchCategory('hack'), 'assignedPending');
assert.strictEqual(normalizeWorkbenchCategory(null), 'assignedPending');
assert.strictEqual(urgencyDisplay('3'), '未知');
assert.strictEqual(urgencyDisplay('2'), '特急');
assert.strictEqual(canStartRepair({ repairUserId: 7, repairStatus: '1', startTime: null }, 7), true);
assert.strictEqual(canStartRepair({ repairUserId: 7, repairStatus: '1', startTime: null }, 16), false);
assert.strictEqual(canCompleteRepair({ repairUserId: 7, repairStatus: '1', startTime: '2026-01-01' }, 7), true);
assert.strictEqual(canCompleteRepair({ repairUserId: 7, repairStatus: '1', startTime: null }, 7), false);

console.log('fault-repair-employee-workbench.test.js passed');
