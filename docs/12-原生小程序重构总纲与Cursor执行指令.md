| GET | `/api/fire/task/conclusion/{taskId}` | apiToken | 情况简述 | |
| GET | `/api/fire/task/conclusion/previous/{taskId}` | apiToken | 引用上月结论字段 | |
| POST | `/api/fire/task/saveConclusion` | apiToken | 保存情况简述 | |
| POST | `/api/fire/task/inspectionTest/markCategoryAllNormal/{taskId}/{categoryKey}` | apiToken | 类目级全部正常（排除测试与已保存） | |
# 原生小程序重构总纲与 Cursor 执行指令

> 状态：待执行。本文是交给 Cursor Agent 的总任务书，也是本轮重构的范围、顺序和验收依据。
>
> 执行原则：后端代码是业务事实来源；先固定接口契约，再重构小程序；管理端现有功能不得被破坏。

## 1. 可直接发给 Cursor 的总指令

将下面这段话完整发送给 Cursor Agent：

```text
请完整阅读并严格执行 docs/12-原生小程序重构总纲与Cursor执行指令.md。

目标：全面梳理 backend 的所有 HTTP 接口并把最新文档统一维护到根目录 docs/；以 PC 管理端现有业务能力、字段、权限和状态流转为事实来源，补齐稳定的小程序 REST API；将 miniprogram/ 彻底重构为微信原生小程序，停止使用 Uni-app、Vue、uni-ui 和任何编译产物。所有鉴权、数据权限、查询过滤、分页、校验、统计、状态流转、业务计算和文件处理必须由 backend 完成，小程序只负责界面、输入采集、调用接口和展示结果。

按本文阶段顺序持续执行，不要只输出方案。先完成接口盘点与文档，再做后端契约和测试，然后搭建原生小程序骨架并逐模块迁移。每完成一个阶段都更新 docs/ 中的进度与验证记录。保护工作区已有改动，不覆盖、不回退、不删除无关文件，不使用 git reset --hard 或 git checkout --。遇到普通实现细节请根据当前代码作合理判断并继续；只有缺少真实账号、产品规则或会导致不可逆数据变化时才暂停询问。
```

## 2. 当前仓库事实

开始编码前必须自行复核，不能仅依赖旧文档：

- `backend/` 是 RuoYi 4.8.2 多模块 Maven 工程，PC 管理端主要使用 Spring MVC、Thymeleaf、Shiro、Service、Mapper。
- `backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/` 下同时存在 PC 页面/AJAX Controller、REST Controller、公开接口、系统接口、监控和演示接口。
- `backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/` 与 `FireMiniAppController` 是现有小程序接口的重要来源，但不是唯一事实来源。
- `backend/docs/小程序对接文档.md`、`backend/ruoyi-admin/API接口文档.md` 和根目录旧文档可能已经落后；发生冲突时以 Controller、DTO/Domain、Service、Mapper、Shiro 配置和测试为准。
- `miniprogram/` 是 Uni-app 的 `mp-weixin` 编译产物，不是可持续维护的源码。现有代码含 `common/vendor.js`、`@dcloudio/uni-ui`、`createSSRApp`、`wx.createPage` 包装和 `.vue` 源行号。
- `Huixiaobao_miniprogram/` 当前为空，不能假定这里存在可恢复的 Uni-app 源码。
- 当前微信 AppID/测试号配置位于 `miniprogram/project.config.json`。重构时保留该 AppID 和微信开发者工具所需基础设置，不在文档中新增任何密钥或隐私配置。
- 工作区可能已经包含用户未提交的修改和未跟踪文件。所有这些内容都视为用户资产，不得覆盖或清理。

## 3. 最终目标

### 3.1 后端

- 盘点并记录全部真实 HTTP 映射，不遗漏类级 `@RequestMapping` 与方法级映射组合。
- 明确区分：PC 页面接口、PC AJAX 接口、小程序 REST API、公开匿名接口、系统/监控接口、演示/工具接口。
- 为新原生小程序提供独立、稳定、可测试的 REST 契约。新小程序禁止调用返回 HTML 的 `/fire/**` 页面路由。
- 业务规则只存在于后端 Service/领域逻辑中；PC 与小程序共用同一套规则，Controller 只做协议适配。
- 所有数据权限和状态转换都在后端再次校验，不能依赖前端隐藏按钮或传入的用户、公司、部门范围。
- 为新增或修复的接口补充自动化测试，至少覆盖成功、未登录、无权限、越权、非法状态和分页边界。

### 3.2 小程序

- `miniprogram/` 成为可直接由微信开发者工具打开的原生小程序源码目录。
- 只使用原生 `App`、`Page`、`Component`、WXML、WXSS、原生 JavaScript 和 `wx.*` API。
- 不使用 Uni-app、Vue、npm UI 框架、`common/vendor.js`、`@dcloudio` 或编译生成包装层。
- 小程序只负责：页面路由、视图状态、表单输入、必要的轻量展示格式化、调用 REST API、展示后端结果。
- 小程序不得负责：数据权限、业务状态机、统计口径、业务筛选、跨页业务计算、主数据字典、任务生成、报告生成或最终业务校验。
- 不使用硬编码演示业务数据掩盖接口失败。
- 产品能力和命名以 PC 管理端为基准，但交互要适配移动端，不照搬 PC 表格和弹窗布局。

## 4. 阶段一：完整梳理 backend 接口

这一阶段只做只读盘点、文档和必要的接口测试基线；未完成前不得大规模重写小程序。

### 4.1 盘点来源

至少检查：

- `backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/**/*.java`
- 所有 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`
- `@RequiresPermissions`、匿名访问配置、Shiro Filter、API Token Filter
- 请求 DTO、Domain、校验注解、分页实现、上传下载参数
- Controller 调用的 Service、关键 Mapper 查询及数据范围实现
- `backend/tests/`、Mapper XML、SQL 增量脚本中体现的业务约束
- 现有小程序 `miniprogram/api/index.js` 的调用清单，用于找出“前端调用但后端不存在”和“后端存在但前端未使用”的接口

不要只用正则拼路径后就宣称完成。类级路径、方法级路径、变量路径、重载、权限、请求类型和响应形态必须人工复核。

### 4.2 每个接口必须记录的字段

接口文档至少包含：

| 字段 | 要求 |
|---|---|
| 模块 | 认证、公司、建筑、设备、项目类别、任务、签到、巡检、报修、报告、系统等 |
| 接口类型 | PC 页面 / PC AJAX / 小程序 REST / 公开 / 系统 / 监控 / 演示工具 |
| Method + 完整 Path | 合并类级和方法级映射后的真实路径 |
| Controller 方法 | 完整类名和方法名 |
| 鉴权 | 匿名、Session、Bearer/Shiro Session ID 等 |
| 权限标识 | `@RequiresPermissions` 或实际权限规则 |
| 请求 | Path、Query、Form、JSON、Multipart；字段类型、必填、默认值 |
| 响应 | HTTP 状态、业务码、分页结构、数据字段和样例 |
| 数据范围 | 公司、部门、用户、角色范围如何校验 |
| 业务规则 | 校验、计算、状态前置条件、幂等性、副作用 |
| 调用方 | PC、小程序、公开二维码、内部调用 |
| 当前状态 | 保留、修复、新增、兼容、废弃、未使用 |
| 证据 | 对应源文件和测试文件路径 |

### 4.3 文档交付物

在根目录 `docs/` 新增并维护：

1. `13-backend接口总览.md`
   - 覆盖全部 Controller 的接口目录。
   - 按命名空间和业务模块分组。
   - 演示/工具接口也要列出，但单独标记为非业务接口。
2. `14-小程序REST-API契约-v1.md`
   - 只记录新原生小程序允许调用的稳定 REST API。
   - 包含认证、错误码、分页、上传下载、字段字典和完整请求响应样例。
3. `15-PC功能与小程序功能映射.md`
   - PC 菜单/页面 → Service 规则 → 小程序页面 → REST API 的逐项映射。
   - 明确哪些 PC 管理能力不下放到小程序。
4. `16-原生小程序架构设计.md`
   - 目录、环境配置、请求层、Session、页面、组件、上传下载和错误处理。
5. `17-原生小程序迁移与验收.md`
   - 分阶段进度、旧新页面映射、测试账号/测试环境说明、冒烟测试和发布清单。

同步更新：

- `docs/README.md` 文档索引与当前判断。
- `docs/03-小程序架构与现状.md`，从“编译产物现状”更新为“原生重构状态”。
- `docs/04-前后端API契约.md`，明确其与新 v1 契约的关系；内容过时则标为历史文档，不得继续并行维护两套冲突事实。
- `docs/05-核心业务流程.md`、`docs/07-问题清单与重构路线.md`、`docs/11-本地更改与预览规则.md`。
- `backend/docs/` 中旧接口文档保留为历史资料，文件头注明新的权威文档位置，避免两处继续漂移。

## 5. 阶段二：建立后端优先的稳定 REST 契约

### 5.1 路由边界

- 新原生小程序只调用 `/api/mini/v1/**`。
- 现有 `/api/**` 在迁移期保留兼容，不应直接删除导致旧体验版失效。
- `/fire/**` 保持 PC 管理端用途；小程序不得调用这些 HTML 页面或 PC 专用表格接口。
- 公开二维码等匿名能力继续位于明确的 `/public/**` 范围。
- REST Controller 使用明确的 Request/Response DTO，不直接把持久化 Domain 当作长期公开契约。

如当前项目约束使 `/api/mini/v1/**` 不可行，必须先在 `14-小程序REST-API契约-v1.md` 说明原因和替代前缀，再统一实施；禁止同一新客户端继续混用 `/fire/**`、`/api/fire/**` 和临时路径。

### 5.2 统一协议

新接口统一使用：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

分页数据统一放入 `data`：

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "list": [],
    "pageNum": 1,
    "pageSize": 20,
    "total": 0,
    "hasNext": false
  }
}
```

- 未登录必须同时返回 HTTP 401 和稳定业务码。
- 已登录但无权限返回 HTTP 403。
- 参数错误、非法状态、资源不存在和业务冲突要有稳定错误码与可展示消息。
- 不允许新小程序长期兼容 `code=0`/`code=200`、`rows`/`data.list` 等多套形态。
- 日期时间、金额、经纬度、文件 URL、枚举值和空值策略必须在文档中统一。

### 5.3 必须由后端承担的业务

包括但不限于：

- 登录、Session、401、菜单/能力清单和用户信息。
- 公司/部门/用户数据范围与跨公司越权校验。
- 首页统计、设备状态统计、任务统计和预警列表。
- 所有搜索、筛选、排序、分页和 `hasNext`。
- 建筑编码、设备编码、字典、项目类别和设备类别。
- 设备有效期状态和预警计算。
- 维保任务执行树、检查项结果、全部正常、故障说明和完成条件。
- 签到距离计算、位置合法性、签到/签退状态。
- 巡检类目与设备联动、检查结果合法性。
- 报修派发、接单/开始、完成、撤回等状态机和操作人校验。
- 报告生成、文件定位、预览、下载权限和文件名。
- 上传文件类型、大小、数量、归属和安全校验。

前端可以做即时必填提示以改善体验，但后端必须重复进行权威校验。

## 6. 阶段三：重建微信原生小程序骨架

### 6.1 清理与保留边界

重构前先输出旧文件清单和迁移映射，确认 Git 可以恢复旧版本。之后：

- 保留 `miniprogram/project.config.json` 中当前 AppID/测试号和必要开发者工具设置。
- 谨慎保留 `project.private.config.json` 的本机设置，不把个人配置或密钥写进文档。
- 逐一审计 `static/`，只保留确实仍被新页面使用的品牌图片和图标。
- 删除/替换所有 Uni-app 编译产物、uni-ui 组件和无源码依赖。
- 不把旧编译产物复制到另一个长期维护目录；Git 历史就是回退依据。如需临时备份，迁移完成后清理。

完成后以下检索必须为零（文档引用除外）：

```text
common/vendor.js
@dcloudio
createSSRApp
common_vendor
uni_modules
uni-easyinput
uni-forms
//# sourceMappingURL=...mp-weixin
```

### 6.2 目标目录

```text
miniprogram/
├─ app.js
├─ app.json
├─ app.wxss
├─ sitemap.json
├─ project.config.json
├─ config/
│  ├─ env.js
│  └─ constants.js
├─ services/
│  ├─ request.js
│  ├─ auth.js
│  ├─ home.js
│  ├─ company.js
│  ├─ building.js
│  ├─ equipment.js
│  ├─ task.js
│  ├─ checkin.js
│  ├─ inspection.js
│  ├─ repair.js
│  ├─ report.js
│  └─ upload.js
├─ utils/
│  ├─ storage.js
│  ├─ date.js
│  ├─ route.js
│  └─ validator.js
├─ components/
│  ├─ app-header/
│  ├─ empty-state/
│  ├─ load-more/
│  ├─ status-tag/
│  └─ company-switcher/
└─ pages/
   └─ ...
```

- `services/` 只封装 HTTP 协议，不复制 Service 层业务规则。
- 页面不得直接调用 `wx.request`、`wx.uploadFile`、`wx.downloadFile`；统一通过基础服务。
- 所有环境地址放在 `config/env.js`，支持本地、测试、生产；不得由脚本正则改写业务文件中的硬编码常量。
- 统一处理并发 loading、网络错误、HTTP 401/403、业务错误、超时、重试边界和防重复提交。

### 6.3 第一批基础页面

先搭建能编译运行的最小结构，再迁移业务：

1. 登录/注册邀请。
2. 首页与公司切换。
3. 扫码入口。
4. 我的/退出登录。
5. 原生自定义 TabBar 或原生 `tabBar`，只能选一种稳定实现。

此时只保留测试号和基础导航结构，不得为了“页面看起来完整”填充虚假业务数据。

## 7. 阶段四：按 PC 能力迁移业务模块

迁移顺序：

1. 公司与建筑。
2. 设备列表、详情、新增/编辑、扫码。
3. 维保任务列表和消防维护三级执行树。
4. 签到/签退和历史记录。
5. 独立巡检测试。
6. 故障报修列表、上报、处理和完成。
7. 维保报告列表、详情、预览和下载。
8. 消息等低优先级功能最后评估，不存在真实后端能力时只记录为未实现，不造假接口。

每个模块必须按以下闭环完成后才能进入下一个：

```text
PC 功能与规则盘点
→ 后端 REST DTO/接口实现
→ 权限与业务测试
→ 文档请求响应样例
→ 原生页面与 service
→ 微信开发者工具编译
→ 人工冒烟验收记录
```

移动端可根据操作场景重新设计信息层级，但字段含义、权限、业务状态和统计口径必须与 PC 一致。

## 8. 测试与验收标准

### 8.1 后端

- `mvn -pl ruoyi-admin -am test` 或项目实际可用的等价测试通过。
- 新 REST 接口有 Controller/Service 测试或可重复的集成测试。
- 覆盖 401、403、跨公司访问、非法 ID、非法状态、重复提交、分页第一页/末页/空页。
- 原 PC 页面、列表、增删改、任务、报修、报告流程不回归。
- 接口文档中的样例能用本地环境真实复现，不得凭空编写字段。

### 8.2 小程序

- 微信开发者工具能够直接打开 `miniprogram/` 并编译，无 Uni-app 构建步骤。
- 控制台无缺失模块、组件、页面或资源错误。
- 全项目不再依赖 Uni-app/Vue/uni-ui 运行时。
- 登录过期只触发一次清理和重新登录，不出现重定向风暴。
- 列表分页、空状态、失败状态、重复点击、上传、下载、扫码和弱网提示可用。
- 前端不按当前用户自行裁剪数据，后端返回什么范围就展示什么范围。
- 不存在硬编码统计、演示任务、演示设备或用本地数组替代后端业务结果。

### 8.3 文档

- `docs/README.md` 能作为唯一入口找到所有新文档。
- 所有新接口在总览和 v1 契约中均可定位。
- 每个小程序页面能反查其 REST API，每个 v1 API 能反查调用页面和后端实现。
- 旧文档已明确标注历史状态，不再与新契约互相矛盾。

## 9. Cursor 工作纪律

- 不执行破坏性 Git 命令，不回退用户已有改动。
- 不批量覆盖不相关模块；每次修改保持范围清晰、可验证。
- 不因旧文档写着“已完成”就跳过代码核验。
- 不只做 UI 翻译；发现业务逻辑在旧小程序中时，先迁移到后端并补测试。
- 不为了兼容旧编译产物，把字段别名和成功码兼容继续扩散到新页面。
- 不在日志、文档或代码中写入密码、Session、AppSecret、数据库密码或生产个人信息。
- 每阶段在 `docs/17-原生小程序迁移与验收.md` 更新：完成项、未完成项、风险、验证命令、验证结果。
- 如果一次上下文不足，保留清晰的阶段状态继续执行，不能跳到“已完成”总结。

## 10. 完成定义

只有同时满足以下条件才可宣布重构完成：

- backend 全部 HTTP 接口已被最新文档覆盖并可追溯到源代码。
- 新小程序使用的 v1 REST 契约已固定、实现并测试。
- `miniprogram/` 已完全是微信原生源码，无 Uni-app 运行时和编译产物依赖。
- PC 与小程序共用后端业务规则，关键流程结果一致。
- 小程序只承担前端窗口职责，没有权威业务逻辑。
- 核心业务模块全部通过后端测试、微信开发者工具编译和人工冒烟验收。
- 文档索引、开发联调、测试和发布说明同步更新。

