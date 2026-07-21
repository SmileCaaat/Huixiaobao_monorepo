# local_sever

Local test gateway for Huixiaobao `backend` + `miniprogram`.

## Prerequisites

- Node.js >= 18
- JDK 21 + Maven (to build/run backend)
- MySQL with `dev_manager` (for real API data)

## Quick start

1. Copy `config.example.env` to `config.env` and set `DB_PASS`.
2. Run `start.bat` and **keep the window open**.
3. Open `http://127.0.0.1:3080/` for the device dashboard.
4. Open WeChat DevTools on `../miniprogram/` (BASE_URL switched to local).

Stop with Ctrl+C in that window, or run `stop.bat`.

Full guide (Chinese): [`docs/09-local_sever.md`](../docs/09-local_sever.md)
