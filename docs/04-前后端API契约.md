# 前后端 API 契约

## 1. 通用约定

### Base URL

当前小程序固定使用：

```text
https://huixiaobao-admin.site
```

### 鉴权 Header

```http
Authorization: Bearer <Shiro Session ID>
Content-Type: application/json
```

### 普通响应

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 分页响应

```json
{
  "code": 0,
  "msg": "查询成功",
  "rows": [],
  "total": 0
}
```

当前前端兼容成功码 `0` 和 `200`。

## 2. API 映射

状态：`已对应` 表示路径和主要数据形态匹配；`需修复` 表示存在代码级断点；`未使用` 表示 API 门面中存在但当前页面没有调用。

### 认证与用户

| 前端函数 | 方法和路径 | 后端实现 | 关键字段 | 状态 |
|---|---|---|---|---|
| `login` | `POST /api/login` | `ApiLoginController.login` | `username,password` → `token,userId,userName` | 已对应 |
| `register` | `POST /api/register` | `SysRegisterController.apiRegister` | `loginName,userName,phonenumber,password` | 需修复：`deptId` 被忽略 |
| `getUserInfo` | `GET /api/user/info` | `ApiLoginController.getUserInfo` | `userId,userName,loginName,deptId` | 已对应 |
| `logout` | `POST /api/logout` | `ApiLoginController.logout` | 当前 Session | 已对应 |
| `getMenus` | `GET /api/user/menus` | 未找到 | - | 未使用/缺失 |
| `getDeptList` | `GET /api/fire/dept/list` | `FireMiniAppController.getDeptList` | `deptId,deptName` | 已对应，匿名 |

### 公司和首页

| 前端函数 | 方法和路径 | 后端实现 | 关键字段 | 状态 |
|---|---|---|---|---|
| `getMyCompanyList` | `GET /api/fire/company/myList` | 同路径 | `companyId,companyName,address` | 已对应 |
| `switchCompany` | `POST /api/fire/company/switch` | 同路径 | `companyId` | 已对应，写入 Session |
| `getCurrentCompany` | `GET /api/fire/company/current` | 同路径 | `companyId` | 已对应 |
| `getCompanyDetail` | `GET /api/fire/company/detail/{id}` | 同路径 | `companyId` | 已对应，需加强权限校验 |
| `getHomeStats` | `GET /fire/home` | `FireHomeController.home` | 前端期待 `monthPlan/deviceStats` | 需修复：返回 HTML |
| `getEquipmentStats` | `GET /fire/stats/equipment` | 未找到 | - | 未使用/缺失 |
| `getBuildingStats` | `GET /fire/stats/building` | 未找到 | - | 未使用/缺失 |

后端现有统计接口是 `GET /fire/stats/home`，返回的是管理端全局统计字段，并非小程序当前期待的结构，也没有明确按当前公司过滤。建议新增 `/api/fire/home`，不要让小程序继续访问 `/fire/**`。

### 建筑与设备

| 模块 | 方法和路径 | 关键字段 | 状态 |
|---|---|---|---|
| 建筑列表 | `POST /api/fire/building/list` | `companyId,pageNum,pageSize` | 路径对应；分页传递需修复 |
| 建筑详情 | `GET /api/fire/building/detail/{buildingId}` | `buildingId` | 已对应 |
| 新增/编辑建筑 | `POST /api/fire/building/add|edit` | `companyId,buildingId,buildingName` | 已对应；`buildingCode` 由后端生成，客户端传入会被忽略；新增成功可回传 `buildingCode`；编辑不可改编码 |
| 公司建筑 | `GET /api/fire/building/byCompany/{companyId}` | `companyId` | 已对应 |
| 设备列表 | `POST /api/fire/equipment/list` | `companyId,buildingId,systemTypeId` | 路径对应；分页传递需修复 |
| 设备详情 | `GET /api/fire/equipment/detail/{equipmentId}` | `equipmentId` | 已对应 |
| 新增/编辑设备 | `POST /api/fire/equipment/add|edit` | `equipmentId,companyId,buildingId` | 已对应，字段别名需收敛 |
| 扫码设备 | `GET /api/fire/equipment/scan/{equipmentCode}` | `equipmentCode` | 已对应 |
| 公开设备详情页 | `GET /public/equipment/{equipmentCode}` | HTML 免登录详情 | 管理端新二维码内容 |
| 公开设备 JSON（兼容） | `GET /public/api/equipment/{equipmentCode}` | JSON；浏览器 `Accept: text/html` 时 302 到公开详情页 | 旧码兼容 |
| 设备报障 | `POST /api/fire/equipment/reportFault` | - | 后端缺失；页面实际使用 repair/add |

### 维保任务

| 前端函数 | 方法和路径 | 关键字段 | 状态 |
|---|---|---|---|
| `getMyTaskList` | `POST /api/fire/task/myList` | `companyId,taskType,taskStatus,pageNum,pageSize` | 需修复：后端只处理 companyId/taskStatus，分页在 JSON body |
| `getTaskDetail` | `GET /api/fire/task/detail/{taskId}` | `taskId` → `systems[]` | 已对应；`recordType` 由前端过滤 |
| `getSystemDetail` | `GET /api/fire/task/system/{recordId}` | `recordId` → `system,equipments[]` | 已对应 |
| `getDeviceDetail` | `GET /api/fire/task/equipment/{recordId}` | `recordId` → `equipment,checkItems[]` | 已对应 |
| `updateCheckResult` | `POST /api/fire/task/updateCheckResult` | `taskId,recordId,checkResult` | 已对应 |
| `updateFaultDesc` | `POST /api/fire/task/updateFaultDesc` | `taskId,recordId,faultDescription` | 已对应 |
| `updateCheckDetail` | `POST /api/fire/task/updateCheckDetail` | `taskId,recordId,otherNotes,faultImages`（可选 `checkResult`/`faultDescription`） | 已对应；仅传说明/附件时不覆盖 `check_result` |
| `updateMaintenance` | `POST /api/fire/task/updateMaintenance` | `taskId,recordId,testResult,sitePhotos` | 后端 API 缺失 |

管理端存在 `POST /fire/task/updateMaintenance`，但它接受表单参数且位于管理端路由，不能直接视为小程序接口。应在 `/api/fire` 下补一个 JSON 版本，并复用 Service。

### 签到、巡检、报修与报告

| 模块 | 主要路径 | 关键字段 | 状态 |
|---|---|---|---|
| 签到 | `/api/fire/checkIn/add/list/detail` | `checkInId,companyId,taskId,latitude,longitude,images` | 已对应 |
| 位置校验 | `/api/fire/checkIn/validateLocation` | `companyId,latitude,longitude` | 已对应；页面未单独调用 |
| 公司任务下拉 | `/api/fire/checkIn/listTasksByCompany` | `companyId` | 已对应 |
| 巡检 | `/api/fire/inspection/myList/detail/add/edit/delete` | `inspectionId,companyId,buildingId` | 已对应 |
| 报修列表 | `/api/fire/repair/myReportedList`、`myAssignedList` | `repairId,reporterId,repairUserId` | 已对应 |
| 报修操作 | `/api/fire/repair/add/edit/delete/dispatch/start/complete` | `repairId,companyId` | 已对应；start/complete 仅当前处理人 |
| 报修列表（PC 员工工作台） | `POST /fire/repair/list` | `params[workbenchCategory]`：`assignedPending`/`processing`/`completed`/`reported` | 已对应；后端注入当前用户，忽略客户端 scope 参数 |
| 报修开始（PC） | `POST /fire/repair/start/{repairId}` | 幂等写 `start_time` | 已对应；权限 `fire:repair:start` |
| 报修完成（PC） | `GET/POST /fire/repair/complete/{repairId}` | `repairDescription,repairImages` | 已对应；权限 `fire:repair:complete` |
| 报修撤回（管理端） | `POST /fire/repair/recall` | `repairId` | 已对应；权限 `fire:repair:accept` |
| 报修统计 | `GET /api/fire/repair/statistics` | `pending,processing,completed` | 已对应 |
| 报告（小程序） | `/api/fire/report/list/detail/download/preview` | `reportId`；按真实文件 | 已对应 |
| 报告生成（管理端） | `POST /fire/report/generate` | `companyId,taskId`；DOCX 必落盘，PDF 可选 | 已对应；校验任务归属客户 |
| 报告联动（管理端） | `GET /fire/report/companies`、`/fire/report/tasks` | `companyId` | 已对应 |
| 报告预览页（管理端） | `GET /fire/report/view/{reportId}`、`/check/{reportId}`、`/preview/{reportId}`、`/download/{reportId}` | 优先 DOCX；前端 [docxjs](https://github.com/VolodymyrBaydalka/docxjs) 渲染 | 已对应 |
| 上传 | `POST /api/common/upload` | multipart `file` | 已对应 |

## 3. 必须先修的契约问题

### HTTP 401

后端 `ApiTokenFilter` 设置 HTTP status 401；前端只在 `statusCode === 200` 时判断 `data.code === 401`。正确逻辑应同时处理：

```javascript
if (res.statusCode === 401 || res.data?.code === 401) {
  clearTokenAndGoLogin()
}
```

### POST 分页

RuoYi `PageUtils.startPage()` 从 URL 查询参数读取 `pageNum/pageSize`，当前小程序把它们放入 JSON body。两种修复方式二选一并全局统一：

1. 前端把分页附加到 query string。
2. 后端 REST Controller 显式接收分页字段并调用 `PageHelper.startPage(pageNum,pageSize)`。

推荐第二种，契约更清晰，也便于后续原生重构。

### 任务筛选

`myTaskList` 应在后端接收并应用：

- `taskType`
- `taskStatus`
- `companyId`
- `beginTime/endTime`
- `pageNum/pageSize`

当前先分页后由前端过滤 `taskType` 会造成漏项和错误的 `hasMore`。

### 成功码

统一 REST API 成功码为一个值，建议保持 RuoYi 现状统一到 `200` 或完整统一到 `0`，不要长期在新代码中同时兼容。


## 员工注册邀请

组织公司/部门 = `sys_dept`（不是 `fire_company`）。注册不写入 `fire_user_company`。

| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | `/register/invite/resolve` | anon | inviteToken 或 inviteCode → 公司/部门名与模式（不返回可篡改组织 ID） |
| POST | `/register` | anon+captcha | 姓名/手机/密码/邀请；**不再要求短信**；邀请码或邀请 token 必填 |
| POST | `/api/register` | anon | JSON；忽略前端 deptId；同 PC，无短信 |
| GET/POST | `/api/register/invite/resolve` | anon | 小程序解析邀请 |
| POST | `/system/dept/registerQrcode/generate` | manage | 7 天有效；明文 token 仅返回一次；**注册码唯一入口在部门管理** |
| GET | `/system/dept/registerQrcode/{deptId}` | view | 元数据 + 短码二维码（由部门管理页调用） |
| POST | `/system/dept/registerQrcode/disable` | manage | 停用 |
| GET | `/system/dept/registerQrcode/download/{deptId}` | download | 不延期 |
| GET | `/system/user/registerRecord` | system:user:registerRecord | 注册记录独立页（仅审核列表；无本部门二维码入口） |
| POST | `/system/user/audit` | system:user:audit | 乐观更新 WHERE audit_status=1；驳回**不强制** `auditRemark`；按钮挂在注册记录下 |
| POST | `/system/user/registerRecords` | system:user:registerRecord | 注册记录列表（默认仅 qr/invite_code/direct；不展示 auditRemark） |

已移除：`POST /register/sms/send`、`/api/register/sms/send`（员工注册不再走短信校验）。

### 用户管理菜单（避免 C 挂 C）

```text
系统管理
└─ 用户管理 (M)
   ├─ 用户列表 (C) /system/user              system:user:view + 原 F 按钮
   └─ 注册记录 (C) /system/user/registerRecord  system:user:registerRecord
        └─ 用户审核 (F) system:user:audit
```

- 用户列表保留左侧 `sys_dept` 组织树；注册记录为独立全宽页，不嵌入用户列表。
- 注册二维码的生成 / 查看 / 停用 / 下载**唯一入口**在部门管理；注册记录页只负责通过 / 驳回，不混入发码。
- 审核驳回：确认后直接 `auditAction=reject`，不强制 `auditRemark`（可写固定备注如「人工驳回」或置空）；`audit_remark` 库字段保留，列表不展示。
- 若把注册记录以 C 挂在用户列表 C 下，侧栏会把「用户管理」渲染为仅展开，无法打开带组织树的 `/system/user`。
- 菜单迁移：`backend/sql/upgrade_user_menu_split_register_record.sql`；回滚：`rollback_user_menu_split_register_record.sql`。

自动通过：有效邀请 + `register_mode=0` + 手机号唯一（含软删除行，见 `uk_sys_user_phonenumber`）→ `audit_status=0`、`dispatchable=1`、组员角色（`audit_remark` 可写「系统自动」，仅库内保留）；默认 `allow_admin_login=1`、`allow_mini_login=1`（可登 PC，权限仍由 `maintenance_member` 菜单约束，不等于管理员）。

重复手机号应返回业务提示「该手机号已注册」，不应再被脱敏成「系统繁忙」。开关：`sys.account.registerUser=true` 才开放 PC/小程序注册入口。

历史邀请码数据修复（示例 LQK78RXF）：`backend/sql/fix_invite_LQK78RXF_allow_admin_login.sql`。
