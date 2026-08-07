# 前后端 API 契约

> **全量接口清单以 [12-后端接口手册.md](12-后端接口手册.md) 为唯一真理源。**  
> 本文只保留小程序子集要点与已知断点，避免双份维护。

## 1. 小程序子集

| 能力 | 手册章节 | 路径前缀 |
|---|---|---|
| 登录/用户 | §2 | `/api/login`、`/api/user/*`、`/api/wx/*` |
| 上传 | §3 | `/api/common/upload` |
| 公司/首页 | §4 | `/api/fire/company/*`、`GET /api/fire/home` |
| 建筑/设备/任务 | §5–7 | `/api/fire/building|equipment|task/*` |
| 巡查测试（独立模块） | §8 | `/api/fire/inspection/*`（侧边栏菜单名「巡查测试」） |
| 签到/报修/报告 | §9–11 | `/api/fire/checkIn|repair|report/*` |
| 签到逆地理 | §9 | `GET /api/fire/checkIn/reverseGeocode` |
| 字典/部门 | §11 | `/api/fire/dict/*`、`/api/fire/dept/*` |

鉴权：Header `Authorization: Bearer <Shiro SessionId>`。  
分页：URL query `pageNum`/`pageSize`（见手册 §1）。

## 2. 已知断点（历史 → 现状）

| 断点 | 状态 | 说明 |
|---|---|---|
| 首页打 `GET /fire/home` 得 HTML | **已修** | 改用 `GET /api/fire/home`；禁止假数据兜底 |
| HTTP 401 未清会话 | **已修（原生 request）** | 同时处理 HTTP 401 与 `body.code===401` |
| 分页仅放 JSON body | **约定** | 新客户端走 query；后端 body 仍兼容 |
| 任务 `taskType`/日期未筛 | **后端支持** | `taskType` + `params.beginTime/endTime` |
| 报修前端用 roles 判权 | **约定禁止** | 以后端校验/`msg` 为准；后续可加 `allowedActions` |
| 设备/系统字典前端硬编码 | **约定禁止** | 走 `/api/fire/dict/*`、`templateCategories` |

## 3. Base URL

开发默认见 `miniprogram/config/env.js`（当前 `http://127.0.0.1:83`）。  
生产合法域名需与微信公众平台一致。

## 4. 管理端

Thymeleaf 页面与 `/fire/**` JSON 见手册 §12；小程序不得调用管理端 HTML 路由。

## 5. 签到中文地址

`GET /api/fire/checkIn/reverseGeocode?longitude=&latitude=`

- 小程序仅上传 GCJ-02 经纬度；不携带地图密钥、不直调第三方。
- 响应 `data` 至少含 `longitude`/`latitude`/`address`。
- 密钥仅后端 `AMAP_WEB_KEY`。
- 提交签到时后端以服务端解析地址为准，不完全信任客户端 `address` 文本。

## 6. 小程序报告预览

- `GET /api/fire/report/preview/{id}`：有同名 PDF 优先 PDF，否则下发 DOCX（不再因 Word 直接 400）。
- 响应头 `X-Report-File-Type: pdf|docx`；小程序 `openDocument` 需传 `fileType`。
- 列表 `POST /api/fire/report/list` 额外返回任务侧 `planStartTime`/`planEndTime`/`managerName`/`operatorNames`（LEFT JOIN `fire_maintenance_task`）。
- 关键字 `reportName`：**只模糊匹配展示标题**（`r.task_name` 优先，否则任务表 `t.task_name`），**不匹配** `r.report_name` 文件名；Mapper 统一 collation 避免 `utf8mb4_general_ci` / `utf8mb4_0900_ai_ci` 冲突。
- 小程序：`pages/report/index` 搜索栏「搜索」按钮；点击卡片进 `pages/report/preview`（`fetchReportPreviewFile` / `downloadReport`）。
