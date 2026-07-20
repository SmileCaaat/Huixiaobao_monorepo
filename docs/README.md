# 汇消宝项目文档

本文档面向临时接手项目的开发者，依据当前仓库代码和根目录《汇消宝管理后台更新部署文档.pdf》整理。结论以代码为第一事实来源；原有接口文档和部署 PDF 仅作为补充，因为其中部分路径已经与当前实现不一致。

## 一句话架构

汇消宝是一个以 RuoYi 后端为核心、微信小程序为现场作业端的消防维保系统：后端业务模型和管理能力相对完整，小程序则是缺少源码的 uni-app 编译产物，适合在稳定接口契约后逐步或整体重写为原生小程序。

## 阅读顺序

| 文档 | 内容 |
|---|---|
| [01-项目总览.md](01-项目总览.md) | 仓库边界、技术栈、模块依赖和整体架构 |
| [02-后端架构.md](02-后端架构.md) | Maven 模块、分层、鉴权、数据库和基础设施 |
| [03-小程序架构与现状.md](03-小程序架构与现状.md) | 页面、请求、Token、uni-app 痕迹及重构边界 |
| [04-前后端API契约.md](04-前后端API契约.md) | 小程序 API 到后端 Controller 的映射及已知断点 |
| [05-核心业务流程.md](05-核心业务流程.md) | 登录、公司上下文、任务、设备、巡检、签到和报修流程 |
| [06-开发与部署.md](06-开发与部署.md) | 本地开发、微信开发者工具、生产部署、日志和回滚 |
| [07-问题清单与重构路线.md](07-问题清单与重构路线.md) | P0/P1/P2 问题和 `feature/native-rewrite` 实施顺序 |
| [08-接手检查清单.md](08-接手检查清单.md) | 需要向前任或运维确认的信息、首周建议和发布检查表 |

## 当前判断

- `backend/` 是项目核心，业务数据模型、管理端、消防业务 Service/Mapper 和大部分小程序 REST API 都已存在。
- `miniprogram/` 不是 uni-app 源工程，而是 `mp-weixin` 编译结果；仓库中没有 `.vue`、`pages.json`、`manifest.json`、`package.json` 或有效 sourcemap。
- 小程序可以整体原生重构，但应先固定 API 契约，否则会把当前接口漂移复制进新代码。
- 现有消防业务 SQL 主要是增量脚本，缺少从零创建全部 `fire_*` 表的完整基线。
- 生产部署 PDF 只覆盖 JAR 更新和日志查看，没有提供服务器、数据库、Nginx、备份或回滚信息。

## 事实来源优先级

发生冲突时按以下顺序判断：

1. 当前 Java/Mapper/小程序调用代码。
2. `application.yml`、`application-druid.yml`、POM 和 Dockerfile。
3. `backend/sql/` 中可执行脚本。
4. `miniprogram/改动记录.md`。
5. `backend/docs/小程序对接文档.md`、`backend/ruoyi-admin/API接口文档.md`。
6. 根目录部署 PDF。

## 快速入口

- 后端启动类：[`backend/ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java`](../backend/ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java)
- 后端主配置：[`backend/ruoyi-admin/src/main/resources/application.yml`](../backend/ruoyi-admin/src/main/resources/application.yml)
- 小程序页面注册：[`miniprogram/app.json`](../miniprogram/app.json)
- 小程序 API：[`miniprogram/api/index.js`](../miniprogram/api/index.js)
- 小程序请求封装：[`miniprogram/utils/request.js`](../miniprogram/utils/request.js)
- 小程序 REST 聚合 Controller：[`FireMiniAppController.java`](../backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java)
- 原始部署说明：[`汇消宝管理后台更新部署文档.pdf`](../汇消宝管理后台更新部署文档.pdf)

