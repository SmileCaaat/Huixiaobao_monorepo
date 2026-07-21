# Huixiaobao

智慧消防设备管理系统 monorepo。

## 目录结构

| 目录 | 说明 |
|------|------|
| `backend/` | 后端管理端（若依 RuoYi，Java / Spring Boot） |
| `miniprogram/` | 微信小程序前端 |
| `local_sever/` | 本地测试网关与设备面板（一键启动） |
| `docs/` | 项目文档 |

## 开发说明

- **后端**：在 `backend/` 下使用 Maven 构建与运行。
- **小程序**：用微信开发者工具打开 `miniprogram/` 目录进行开发与上传。
- **本地联调**：进入 `local_sever/`，配置 `config.env` 后运行 `start.bat`。详见 `docs/09-local_sever.md`。

## 分支策略

主分支保留当前可用版本；后续原生重写或迁移工作请开独立分支进行。
