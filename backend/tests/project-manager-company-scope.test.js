const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backendRoot = path.resolve(__dirname, '..');
const read = (relativePath) =>
    fs.readFileSync(path.join(backendRoot, relativePath), 'utf8');

const aspect = read(
    'ruoyi-framework/src/main/java/com/ruoyi/framework/aspectj/DataScopeAspect.java'
);
const role = read(
    'ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysRole.java'
);
const dataScopePage = read(
    'ruoyi-admin/src/main/resources/templates/system/role/dataScope.html'
);
const rolePage = read(
    'ruoyi-admin/src/main/resources/templates/system/role/role.html'
);
const deptController = read(
    'ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysDeptController.java'
);
const migration = read('sql/upgrade_project_manager_company_scope.sql');

assert.match(
    aspect,
    /DATA_SCOPE_COMPANY_AND_CHILD\s*=\s*"6"/,
    '应定义所属公司及以下数据范围'
);
assert.match(
    aspect,
    /resolveCompanyDeptId\(user\)/,
    '数据过滤应解析当前用户的公司根部门'
);
assert.match(
    aspect,
    /find_in_set\(\s*\{\}\s*,\s*ancestors\s*\)/i,
    '公司范围应包含公司根部门的全部下级部门'
);
assert.match(
    aspect,
    /OR 1 = 0/,
    '无法确定所属公司时必须拒绝返回数据'
);
assert.match(role, /6=所属公司及以下数据权限/);
assert.match(dataScopePage, /value="6"[^>]*>所属公司及以下数据权限</);
assert.match(rolePage, /item\.dataScope == '6'/);
assert.match(
    deptController,
    /addSave\([^)]*\)[\s\S]*?checkDeptDataScope\(dept\.getParentId\(\)\)/,
    '新增部门时应校验父部门是否属于当前公司的可见范围'
);
assert.match(
    deptController,
    /editSave\([^)]*\)[\s\S]*?checkDeptDataScope\(dept\.getParentId\(\)\)/,
    '移动部门时应校验新父部门是否属于当前公司的可见范围'
);
assert.match(migration, /SET data_scope = '6'/);
assert.doesNotMatch(
    migration,
    /(?:INSERT|UPDATE|DELETE)[\s\S]*sys_(?:menu|role_menu|user|dept)\b/i,
    '迁移不应修改菜单、用户或部门'
);

console.log('project-manager-company-scope tests passed');
