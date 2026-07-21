/**
 * Huixiaobao local test gateway
 * - Serves device dashboard on DASHBOARD_PORT (default 3080)
 * - Proxies API traffic to backend (default 127.0.0.1:83)
 * - Exposes /local/* helpers for health and device overview
 *
 * ASCII-only source file (encoding-safe on Windows).
 */
"use strict";

const path = require("path");
const fs = require("fs");
const http = require("http");
const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const dotenv = require("dotenv");
const { openBackendUi, openMiniprogramUi, findWechatCli, buildBackendJar, cleanBackend } = require("./lib/openers");

const ROOT = path.resolve(__dirname);
const REPO_ROOT = path.resolve(__dirname, "..");
const CONFIG_PATH = path.join(ROOT, "config.env");
const EXAMPLE_CONFIG = path.join(ROOT, "config.example.env");

if (!fs.existsSync(CONFIG_PATH) && fs.existsSync(EXAMPLE_CONFIG)) {
  fs.copyFileSync(EXAMPLE_CONFIG, CONFIG_PATH);
  console.warn("[local_sever] config.env created from config.example.env - edit DB_PASS before starting backend.");
}

dotenv.config({ path: CONFIG_PATH });

const DASHBOARD_PORT = Number(process.env.DASHBOARD_PORT || 3080);
const BACKEND_HOST = process.env.BACKEND_HOST || "127.0.0.1";
const BACKEND_PORT = Number(process.env.BACKEND_PORT || 83);
const BACKEND_TARGET = `http://${BACKEND_HOST}:${BACKEND_PORT}`;
const DEMO_DEVICES = String(process.env.DEMO_DEVICES || "1") !== "0";
const MINIPROGRAM_BASE_URL = process.env.MINIPROGRAM_BASE_URL || `http://127.0.0.1:${BACKEND_PORT}`;
const MINIPROGRAM_PROD_URL = process.env.MINIPROGRAM_PROD_URL || "https://huixiaobao-admin.site";
const WECHAT_DEVTOOLS_CLI = process.env.WECHAT_DEVTOOLS_CLI || "";
const PID_DIR = path.join(ROOT, ".pids");
const LOG_DIR = path.join(ROOT, "logs");
const STATE_DIR = path.join(ROOT, "state");
const JAR_PATH = path.join(REPO_ROOT, "backend", "ruoyi-admin", "target", "ruoyi-admin.jar");
const BACKEND_WORK_DIR = path.join(REPO_ROOT, "backend");

function buildDbEnv() {
  const dbHost = process.env.DB_HOST || "127.0.0.1";
  const dbPort = process.env.DB_PORT || "3306";
  const dbName = process.env.DB_NAME || "dev_manager";
  const dbUser = process.env.DB_USER || "root";
  const dbPass = process.env.DB_PASS || "replace-me";
  return {
    DB_URL:
      process.env.DB_URL ||
      `jdbc:mysql://${dbHost}:${dbPort}/${dbName}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8`,
    DB_USER: dbUser,
    DB_PASS: dbPass,
    DRUID_USER: process.env.DRUID_USER || "admin",
    DRUID_PASS: process.env.DRUID_PASS || "replace-me"
  };
}

const DEMO_DEVICE_ROWS = [
  {
    equipmentId: 9001,
    equipmentCode: "DEMO-SMK-001",
    equipmentName: "Smoke Detector A1",
    companyName: "Demo Company",
    buildingName: "Building A",
    floorNo: "3F",
    systemName: "Fire Alarm System",
    location: "Corridor near lift",
    equipmentStatus: "normal",
    status: "0",
    lastCheckDate: "2026-07-01",
    nextCheckDate: "2026-08-01"
  },
  {
    equipmentId: 9002,
    equipmentCode: "DEMO-HYD-002",
    equipmentName: "Indoor Hydrant B2",
    companyName: "Demo Company",
    buildingName: "Building B",
    floorNo: "1F",
    systemName: "Hydrant System",
    location: "Lobby south wall",
    equipmentStatus: "warning",
    status: "0",
    lastCheckDate: "2026-06-15",
    nextCheckDate: "2026-07-15"
  },
  {
    equipmentId: 9003,
    equipmentCode: "DEMO-SPR-003",
    equipmentName: "Sprinkler Head C3",
    companyName: "Demo Company",
    buildingName: "Building C",
    floorNo: "5F",
    systemName: "Sprinkler System",
    location: "Office zone C",
    equipmentStatus: "fault",
    status: "0",
    lastCheckDate: "2026-05-20",
    nextCheckDate: "2026-06-20"
  },
  {
    equipmentId: 9004,
    equipmentCode: "DEMO-EXT-004",
    equipmentName: "Fire Extinguisher D4",
    companyName: "Demo Company",
    buildingName: "Building A",
    floorNo: "B1",
    systemName: "Extinguisher",
    location: "Parking entrance",
    equipmentStatus: "expired",
    status: "1",
    lastCheckDate: "2025-12-01",
    nextCheckDate: "2026-01-01"
  }
];

function summarizeDevices(rows) {
  const summary = { total: rows.length, normal: 0, warning: 0, fault: 0, expired: 0, other: 0, disabled: 0 };
  for (const row of rows) {
    const key = String(row.equipmentStatus || "other").toLowerCase();
    if (Object.prototype.hasOwnProperty.call(summary, key)) {
      summary[key] += 1;
    } else {
      summary.other += 1;
    }
    if (String(row.status) === "1") {
      summary.disabled += 1;
    }
  }
  return summary;
}

function probeBackend(timeoutMs = 2000) {
  return new Promise((resolve) => {
    const req = http.get(
      {
        host: BACKEND_HOST,
        port: BACKEND_PORT,
        path: "/login",
        timeout: timeoutMs
      },
      (res) => {
        res.resume();
        resolve({
          online: true,
          statusCode: res.statusCode,
          target: BACKEND_TARGET
        });
      }
    );
    req.on("timeout", () => {
      req.destroy();
      resolve({ online: false, error: "timeout", target: BACKEND_TARGET });
    });
    req.on("error", (err) => {
      resolve({ online: false, error: err.message, target: BACKEND_TARGET });
    });
  });
}

function readMiniprogramEnv() {
  const requestJs = path.join(REPO_ROOT, "miniprogram", "utils", "request.js");
  let baseUrl = null;
  let mode = "unknown";
  try {
    const text = fs.readFileSync(requestJs, "utf8");
    const match = text.match(/let\s+BASE_URL\s*=\s*["']([^"']+)["']/);
    if (match) {
      baseUrl = match[1];
      if (baseUrl.includes("127.0.0.1") || baseUrl.includes("localhost") || /^http:\/\/\d+\.\d+\.\d+\.\d+/.test(baseUrl)) {
        mode = "local";
      } else if (baseUrl.includes("huixiaobao-admin.site")) {
        mode = "prod";
      } else {
        mode = "custom";
      }
    }
  } catch (err) {
    return { error: err.message, baseUrl, mode };
  }
  return { baseUrl, mode, requestJs };
}

function fetchBackendJson(pathname, options = {}) {
  const body = options.body ? JSON.stringify(options.body) : null;
  const headers = Object.assign(
    {
      Accept: "application/json",
      "Content-Type": "application/json"
    },
    options.headers || {}
  );
  if (body) {
    headers["Content-Length"] = Buffer.byteLength(body);
  }

  return new Promise((resolve, reject) => {
    const req = http.request(
      {
        host: BACKEND_HOST,
        port: BACKEND_PORT,
        path: pathname,
        method: options.method || "GET",
        headers,
        timeout: options.timeout || 8000
      },
      (res) => {
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => {
          const raw = Buffer.concat(chunks).toString("utf8");
          let json = null;
          try {
            json = raw ? JSON.parse(raw) : null;
          } catch (_) {
            json = null;
          }
          resolve({ statusCode: res.statusCode, headers: res.headers, raw, json });
        });
      }
    );
    req.on("timeout", () => {
      req.destroy();
      reject(new Error("backend request timeout"));
    });
    req.on("error", reject);
    if (body) {
      req.write(body);
    }
    req.end();
  });
}

function makeProxy() {
  return createProxyMiddleware({
    target: BACKEND_TARGET,
    changeOrigin: true,
    on: {
      error(err, _req, res) {
        if (res && !res.headersSent && typeof res.writeHead === "function") {
          res.writeHead(502, { "Content-Type": "application/json; charset=utf-8" });
          res.end(JSON.stringify({ code: 502, msg: `Proxy error: ${err.message}`, target: BACKEND_TARGET }));
        }
      }
    }
  });
}

const app = express();
app.disable("x-powered-by");

app.get("/local/health", async (_req, res) => {
  const backend = await probeBackend();
  const mini = readMiniprogramEnv();
  res.json({
    ok: true,
    service: "huixiaobao-local-sever",
    dashboardPort: DASHBOARD_PORT,
    backend,
    miniprogram: mini,
    config: {
      backendTarget: BACKEND_TARGET,
      miniprogramBaseUrl: MINIPROGRAM_BASE_URL,
      miniprogramProdUrl: MINIPROGRAM_PROD_URL,
      demoDevices: DEMO_DEVICES,
      wechatCli: findWechatCli(WECHAT_DEVTOOLS_CLI),
      jarExists: fs.existsSync(JAR_PATH)
    },
    time: new Date().toISOString()
  });
});

app.get("/local/config", (_req, res) => {
  res.json({
    backendTarget: BACKEND_TARGET,
    dashboardPort: DASHBOARD_PORT,
    miniprogramBaseUrl: MINIPROGRAM_BASE_URL,
    miniprogramProdUrl: MINIPROGRAM_PROD_URL,
    demoDevices: DEMO_DEVICES,
    wechatCli: findWechatCli(WECHAT_DEVTOOLS_CLI),
    jarExists: fs.existsSync(JAR_PATH),
    miniprogramDir: path.join(REPO_ROOT, "miniprogram")
  });
});

app.post("/local/launch/backend", async (_req, res) => {
  try {
    const result = await openBackendUi({
      backendHost: BACKEND_HOST,
      backendPort: BACKEND_PORT,
      jarPath: JAR_PATH,
      backendWorkDir: BACKEND_WORK_DIR,
      backendRoot: path.join(REPO_ROOT, "backend"),
      pidFile: path.join(PID_DIR, "backend.pid"),
      outLog: path.join(LOG_DIR, "backend.out.log"),
      errLog: path.join(LOG_DIR, "backend.err.log"),
      mavenLog: path.join(LOG_DIR, "maven-package.log"),
      dbEnv: buildDbEnv()
    });
    res.json({ code: 200, msg: "ok", data: result });
  } catch (err) {
    res.status(500).json({
      code: 500,
      msg: err.message,
      errorCode: err.code || "LAUNCH_BACKEND_FAILED",
      data: {
        jarPath: JAR_PATH,
        backendTarget: BACKEND_TARGET,
        logFile: err.logFile || null
      }
    });
  }
});

app.post("/local/build/backend", async (_req, res) => {
  try {
    console.log("[local_sever] /local/build/backend requested (package only)");
    const result = await buildBackendJar({
      jarPath: JAR_PATH,
      backendRoot: path.join(REPO_ROOT, "backend"),
      mavenLog: path.join(LOG_DIR, "maven-package.log"),
      pidFile: path.join(PID_DIR, "backend.pid"),
      backendPort: BACKEND_PORT
    });
    res.json({ code: 200, msg: "ok", data: result });
  } catch (err) {
    const status = err.code === "MVN_BUSY" ? 409 : 500;
    res.status(status).json({
      code: status,
      msg: err.message,
      errorCode: err.code || "BUILD_BACKEND_FAILED",
      data: {
        jarPath: JAR_PATH,
        logFile: err.logFile || path.join(LOG_DIR, "maven-package.log")
      }
    });
  }
});

app.post("/local/clean/backend", async (_req, res) => {
  try {
    console.log("[local_sever] /local/clean/backend requested (mvn clean)");
    const result = await cleanBackend({
      jarPath: JAR_PATH,
      backendRoot: path.join(REPO_ROOT, "backend"),
      mavenLog: path.join(LOG_DIR, "maven-package.log"),
      pidFile: path.join(PID_DIR, "backend.pid"),
      backendPort: BACKEND_PORT
    });
    res.json({ code: 200, msg: "ok", data: result });
  } catch (err) {
    const status = err.code === "MVN_BUSY" ? 409 : 500;
    res.status(status).json({
      code: status,
      msg: err.message,
      errorCode: err.code || "CLEAN_BACKEND_FAILED",
      data: {
        jarPath: JAR_PATH,
        logFile: err.logFile || path.join(LOG_DIR, "maven-package.log")
      }
    });
  }
});

app.post("/local/launch/miniprogram", async (_req, res) => {
  try {
    const result = await openMiniprogramUi({
      repoRoot: REPO_ROOT,
      stateDir: STATE_DIR,
      miniBaseUrl: MINIPROGRAM_BASE_URL,
      wechatCliConfigured: WECHAT_DEVTOOLS_CLI
    });
    res.json({ code: 200, msg: "ok", data: result });
  } catch (err) {
    res.status(500).json({
      code: 500,
      msg: err.message,
      errorCode: err.code || "LAUNCH_MINIPROGRAM_FAILED",
      data: {
        projectDir: err.projectDir || path.join(REPO_ROOT, "miniprogram"),
        wechatCli: findWechatCli(WECHAT_DEVTOOLS_CLI)
      }
    });
  }
});

app.post("/local/devices", express.json({ limit: "2mb" }), async (req, res) => {
  const auth = req.headers.authorization || "";
  const query = req.body && typeof req.body === "object" ? req.body : {};
  const pageNum = Number(query.pageNum || 1);
  const pageSize = Number(query.pageSize || 50);

  const backend = await probeBackend();
  if (!backend.online) {
    if (!DEMO_DEVICES) {
      return res.status(503).json({
        code: 503,
        msg: "Backend offline",
        backend,
        rows: [],
        summary: summarizeDevices([])
      });
    }
    const rows = DEMO_DEVICE_ROWS;
    return res.json({
      code: 200,
      msg: "Demo devices (backend offline)",
      source: "demo",
      backend,
      total: rows.length,
      rows,
      summary: summarizeDevices(rows)
    });
  }

  try {
    const payload = Object.assign({}, query, { pageNum, pageSize });
    const result = await fetchBackendJson("/api/fire/equipment/list", {
      method: "POST",
      headers: auth ? { Authorization: auth } : {},
      body: payload
    });

    if (result.statusCode === 401 || (result.json && Number(result.json.code) === 401)) {
      return res.status(401).json({
        code: 401,
        msg: "Login required",
        backend,
        rows: [],
        summary: summarizeDevices([])
      });
    }

    const json = result.json || {};
    const rows = Array.isArray(json.rows) ? json.rows : Array.isArray(json.data) ? json.data : [];
    const total = Number(json.total != null ? json.total : rows.length);

    return res.json({
      code: json.code != null ? json.code : 200,
      msg: json.msg || "ok",
      source: "backend",
      backend,
      total,
      rows,
      summary: summarizeDevices(rows)
    });
  } catch (err) {
    if (DEMO_DEVICES) {
      const rows = DEMO_DEVICE_ROWS;
      return res.json({
        code: 200,
        msg: `Demo devices (backend error: ${err.message})`,
        source: "demo",
        backend,
        total: rows.length,
        rows,
        summary: summarizeDevices(rows)
      });
    }
    return res.status(502).json({
      code: 502,
      msg: err.message,
      backend,
      rows: [],
      summary: summarizeDevices([])
    });
  }
});

const proxy = makeProxy();
["/api", "/fire", "/public", "/captcha", "/profile", "/login", "/logout", "/druid", "/register"].forEach((p) => {
  app.use(p, proxy);
});

app.use(express.static(path.join(ROOT, "public"), {
  index: "index.html",
  extensions: ["html"],
  setHeaders(res, filePath) {
    if (filePath.endsWith(".html")) res.setHeader("Content-Type", "text/html; charset=utf-8");
    if (filePath.endsWith(".js")) res.setHeader("Content-Type", "application/javascript; charset=utf-8");
    if (filePath.endsWith(".css")) res.setHeader("Content-Type", "text/css; charset=utf-8");
  }
}));

app.get("*", (req, res, next) => {
  if (req.path.startsWith("/api") || req.path.startsWith("/local")) {
    return next();
  }
  res.sendFile(path.join(ROOT, "public", "index.html"));
});

app.listen(DASHBOARD_PORT, "0.0.0.0", () => {
  console.log(`[local_sever] dashboard  http://127.0.0.1:${DASHBOARD_PORT}/`);
  console.log(`[local_sever] backend    ${BACKEND_TARGET}`);
  console.log(`[local_sever] health     http://127.0.0.1:${DASHBOARD_PORT}/local/health`);
});
