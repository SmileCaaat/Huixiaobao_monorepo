# Word 在线预览库来源说明

维保报告管理端「在线预览」使用浏览器端渲染 DOCX，不再依赖 LibreOffice 转 PDF 才能预览。

## 上游项目

- 项目名：docxjs（npm 包名：`docx-preview`）
- 仓库：https://github.com/VolodymyrBaydalka/docxjs
- Demo：https://volodymyrbaydalka.github.io/docxjs/
- 许可证：以该仓库当前 LICENSE 为准

## 本目录文件

| 文件 | 用途 | 版本来源 |
|---|---|---|
| `docx-preview.min.js` | DOCX → HTML 渲染 | npm `docx-preview@0.3.6`（jsDelivr） |
| `jszip.min.js` | DOCX（zip）解压依赖 | npm `jszip@3.10.1`（jsDelivr，docx-preview 官方依赖） |

## 使用位置

- 页面：`templates/fire/report/preview.html`
- 接口：`GET /fire/report/preview/{reportId}` 返回 DOCX 二进制流，前端 `docx.renderAsync` 渲染到页面容器

## 说明

- 静态资源落在仓库内，运行时不依赖外网 CDN。
- 升级库时请同步更新本文件中的版本号，并做一次真实报告预览回归。
