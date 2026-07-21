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
  start.bat                 # һ�����������ڳ�פ��ǰ̨�� node��
  stop.bat                  # һ��ֹͣ
  config.example.env        # ����ģ�壨����Ϊ config.env��
  package.json
  server.js                 # Express ���� + /local API
  public/                   # �豸���ǰ��
  scripts/
    prepare.ps1             # ������װ / �л��� / ��ѡ�����
    start.ps1               # PowerShell ��������ͬ��ǰ̨�� node��
    stop.ps1
    switch-miniprogram-env.ps1
  logs/                     # ������־��gitignore��
  .pids/                    # ���� pid��gitignore��
  state/                    # С���� request.js ���ݵȣ�gitignore��
```

## 3. ��������

�������Ѱ�װ��

| ��� | �汾Ҫ�� | ��; |
|---|---|---|
| Node.js | >= 18���Ƽ� LTS�� | ������������� |
| JDK | **21** | ���� / ������� |
| Maven | 3.x | �״ι��� `ruoyi-admin.jar` |
| MySQL | 5.7+/8.x | ������ݿ⣨�������� `dev_manager`�� |
| ΢�ſ����߹��� | �����ȶ��� | �� `miniprogram/` |

## 4. ����

```powershell
cd local_sever
copy config.example.env config.env
```

�����޸ģ�

- `DB_PASS`���Լ�������Ҫ�� `DB_USER` / `DB_HOST`��
- �������ʱ�� `MINIPROGRAM_BASE_URL` �ĳɵ��Ծ����� IP������ `http://192.168.1.10:83`

���������

- `AUTO_START_BACKEND=1`������ʱ�������� jar
- `AUTO_BUILD_BACKEND=1`��jar ������ʱ�Զ� `mvn clean package -DskipTests`
- `SWITCH_MINIPROGRAM=1`�������б��أ��ֶ� `stop.bat` ʱ�ָ�����
- `DEMO_DEVICES=1`����˲�����ʱ�����ʾ Demo �豸

`config.env` ��Ҫ�ύ���ֿ⡣

## 5. һ������ / ֹͣ

```text
local_sever\start.bat
local_sever\stop.bat
```

`start.bat` ��Ϊ��

1. ���� `prepare.ps1`��npm install����С���򡢿�ѡ����ˣ�
2. **�ڵ�ǰ����ǰִ̨��** `node server.js`�����ڱ��ִ򿪣���־ֱ�������
3. �� `Ctrl+C` �������غ�������

��Ҫ�رոúڴ��ڣ��ص��͵���ͣ������ǰ�����ء�

��������塢������ˣ�

```powershell
cd local_sever
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\prepare.ps1 -SkipBackend
node server.js
```

## 6. ʹ�����

�� `http://127.0.0.1:3080/`��

- ����չʾ���������״̬��С����ǰ `BASE_URL`������Դ����ʵ / Demo��
- ʹ��С����ͬ���˺ŵ�¼���ߴ��� `POST /api/login`��
- ��¼��ˢ���豸�б���״̬ͳ�ƣ����� / Ԥ�� / ���� / ���ڣ�

������飺

```text
GET http://127.0.0.1:3080/local/health
```

## 7. С��������

1. ��΢�ſ����߹��ߵ���ֿ��µ� `miniprogram/`
2. ȷ�������ﲻУ��Ϸ�������`project.private.config.json` ���� `urlCheck=false`��
3. ��ǰ `miniprogram/utils/request.js` �� `BASE_URL` Ӧָ�򱾵أ��� prepare ��д��
4. ģ�������� `127.0.0.1`�������ľ����� IP

�ֶ��л���

```powershell
.\scripts\switch-miniprogram-env.ps1 -Mode local -BaseUrl http://127.0.0.1:83
.\scripts\switch-miniprogram-env.ps1 -Mode prod
```

�ϴ������ / ��ʽ��ǰ����� `stop.bat` ���л� prod������ѱ��ص�ַ�������ϰ���

## 8. �����δ����ʱ

���������

- ��û�� `backend/ruoyi-admin/target/ruoyi-admin.jar`
- MySQL δ��װ�� `dev_manager` δ����
- `DB_PASS` ���� `replace-me`

��ʱ�Կ��������أ�������ʾ Demo �豸������ҵ�������Ҫ MySQL��`fire_*` ���ṹ�� jar��

## 9. �����Ų�

| ���� | �Ų� |
|---|---|
| ˫�� bat ����һ���͹� | ���޸������ڻ�ǰ̨��פ `node`���������ˣ����Ƿ� `node` ���� PATH |
| `node` / `npm` �Ҳ��� | �¿��նˣ�ȷ�� Node ��װ |
| �˿� 3080 ��ռ�� | `stop.bat` �ᰴ�˿���������� `DASHBOARD_PORT` |
| ���򿪵�������� | �� `local_sever/logs/backend.*.log`����� MySQL �� `DB_PASS` |
| С��������ʧ�� | ȷ�� `BASE_URL`�������߹���δУ������������Ѽ��� 83 |

相关文档：[06-开发与部署.md](06-开发与部署.md)、[03-小程序架构与现状.md](03-小程序架构与现状.md)、[04-前后端API契约.md](04-前后端API契约.md)。
