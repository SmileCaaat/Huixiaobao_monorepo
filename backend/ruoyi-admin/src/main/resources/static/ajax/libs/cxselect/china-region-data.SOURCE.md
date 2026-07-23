# 中国省市区数据来源说明

- 文件：`china-region-data.json`
- 格式：cxSelect（`n` / `s`），option value 为中文地名
- 覆盖：31 个省级行政区（省/直辖市/自治区）
- 直辖市已补齐三级，例如：北京市 → 北京市 → 朝阳区

## 上游来源

- 项目：[modood/Administrative-divisions-of-China](https://github.com/modood/Administrative-divisions-of-China)
- 使用上游文件：`dist/pca.json`
- URL：https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master/dist/pca.json
- 本地转换为 cxSelect JSON，运行时不依赖 CDN

## 说明

- 数据基于上游省市区（PCA）数据包。
- 禁止用 DemoFormController.cityData 演示数据替换本文件。
