# local_sever 本地测试服务器

本文说明 monorepo 下 `local_sever/` 的用途、依赖、一键启动方式，以及如何与 `backend` / `miniprogram` 联调。脚本文件均为 ASCII，避免 Windows 控制台中文编码问题。

## 1. 定位

`local_sever` 提供：

1. **Node 网关 + 设备面板**（默认 `http://127.0.0.1:3080/`）
2. **可选自动拉起后端** `ruoyi-admin.jar`（默认端口 `83`）
3. **一键切换小程序** `BASE_URL` 到本地，并写入 `project.private.config.json`（`urlCheck=false`）
4. **设备概览页**：登录后拉取 `POST /api/fire/equipment/list`；后端离线时可显示 Demo 数据

说明：本项目中的「设备」是消防资产台账（`fire_equipment`），不是 IoT 在线传感设备。

## 2. 目录结构

```text
local_sever/
  start.bat                 # 一键启动（当前窗口前台跑 node）
  stop.bat                  # 一键停止
  config.example.env        # 配置模板（复制为 config.env）
  package.json
  server.js                 # Express 网关 + /local API
  public/                   # 设备面板前端
  scripts/
    ensure-deps.ps1         # 检测并自动安装 Node/JDK/Maven/MySQL
    prepare.ps1             # 依赖安装 / 切小程序 / 可选起后端
    start.ps1               # PowerShell 入口（行为同 start.bat）
    stop.ps1
    switch-miniprogram-env.ps1
  logs/                     # 运行日志（gitignore）
  .pids/                    # 进程 pid（gitignore）
  state/                    # 小程序 request.js 备份等（gitignore）
```

## 3. 依赖

首次启动会跑 `scripts/ensure-deps.ps1`：缺什么就尽量自动装。

| 依赖 | 版本要求 | 用途 | 自动安装方式 |
|---|---|---|---|
| Node.js | >= 18（推荐 LTS） | 网关与面板 | winget `OpenJS.NodeJS.LTS` |
| JDK | **21** | 编译 / 运行后端 | winget Temurin 21（或已有本机 JDK 21） |
| Maven | 3.x | 构建 `ruoyi-admin.jar` | 下载到 `%LOCALAPPDATA%\Huixiaobao\tools\` |
| MySQL | 5.7+/8.x | 本地数据库（默认库名 `dev_manager`） | winget `Oracle.MySQL` + `start-mysql.bat` |
| 微信开发者工具 | 较新稳定版 | 打开 `miniprogram/` | 需手动安装 |

也可单独执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\local_sever\scripts\ensure-deps.ps1
```

面板点 **Maven Clean / Package** 前也会再检查一次；缺工具会自动跑 `ensure-deps`。

## 4. 配置

```powershell
cd local_sever
copy config.example.env config.env
```

至少修改：

- `DB_PASS`（本机 root 密码；不要提交）
- 真机调试时把 `MINIPROGRAM_BASE_URL` 改成局域网 IP，例如 `http://192.168.1.10:83`

可选开关：

- `AUTO_START_BACKEND=1`：启动时自动拉起 jar
- `AUTO_BUILD_BACKEND=1`：jar 不存在时自动 `mvn clean package -DskipTests`
- `SWITCH_MINIPROGRAM=1`：切小程序到本地；手动 `stop.bat` 时恢复线上
- `DEMO_DEVICES=1`：后端不可达时面板显示 Demo 设备

`config.env` 不要提交到仓库。

## 5. 一键启动 / 停止

```text
local_sever\start.bat
local_sever\stop.bat
```

`start.bat` 行为：

1. 调用 `prepare.ps1`（ensure-deps、npm install、切小程序、可选起后端）
2. **在当前窗口前台执行** `node server.js`（保持窗口打开，日志直接输出）
3. 按 `Ctrl+C` 会清理网关和后端

不要关闭该黑窗口，关掉就等于停掉网关。

分步调试（不起后端）：

```powershell
cd local_sever
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\prepare.ps1 -SkipBackend
node server.js
```

## 6. 使用面板

打开 `http://127.0.0.1:3080/`：

- 面板展示：后端在线状态、小程序当前 `BASE_URL`、设备来源（真实 / Demo）
- 使用小程序同一套账号登录，或调用 `POST /api/login`
- 登录后刷新设备列表与状态统计（正常 / 预警 / 故障 / 过期）

健康检查：

```text
GET http://127.0.0.1:3080/local/health
```

## 7. 小程序联调

1. 用微信开发者工具打开仓库下的 `miniprogram/`
2. 确认详情里不校验合法域名（`project.private.config.json` 里有 `urlCheck=false`）
3. 当前 `miniprogram/utils/request.js` 的 `BASE_URL` 应指向本地（由 prepare 改写）
4. 模拟器可用 `127.0.0.1`；真机用局域网 IP

手动切换：

```powershell
.\scripts\switch-miniprogram-env.ps1 -Mode local -BaseUrl http://127.0.0.1:83
.\scripts\switch-miniprogram-env.ps1 -Mode prod
```

上传体验版 / 正式版前请先 `stop.bat` 或切换 prod，避免把本地地址打进线上包。

## 8. 后端尚未就绪时

常见原因：

- 还没有 `backend/ruoyi-admin/target/ruoyi-admin.jar`
- MySQL 未安装或 `dev_manager` 未建库
- `DB_PASS` 仍是 `replace-me`

此时仍可打开面板，面板会显示 Demo 设备；完整业务联调需要 MySQL、`fire_*` 表结构与 jar。

## 9. 常见排查

| 现象 | 排查 |
|---|---|
| 双击 bat 闪一下就关 | 改为在现有终端前台常驻 `node`；或看报错是否 `node` 不在 PATH |
| `node` / `npm` 找不到 | 新开终端；确认 Node 已安装；或跑 `ensure-deps.ps1` |
| `mvn` / `java` 找不到 | 跑 `ensure-deps.ps1`，然后重启 `start.bat` |
| 端口 3080 被占用 | `stop.bat` 会按端口清理；或改 `DASHBOARD_PORT` |
| 能打开但后端挂 | 看 `local_sever/logs/backend.*.log`，核对 MySQL 与 `DB_PASS` |
| 小程序请求失败 | 确认 `BASE_URL`、开发者工具未校验域名、后端已监听 83 |

相关文档：[06-开发与部署.md](06-开发与部署.md)、[03-小程序架构与现状.md](03-小程序架构与现状.md)、[04-前后端API契约.md](04-前后端API契约.md)、[10-本地与线上差异说明.md](10-本地与线上差异说明.md)、[11-本地更改与预览规则.md](11-本地更改与预览规则.md)。
