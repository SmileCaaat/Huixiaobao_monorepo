/**
 * Launch helpers for local_sever dashboard buttons.
 * ASCII-only source (encoding-safe on Windows).
 * Chinese path segments use \\u escapes.
 */
"use strict";

const fs = require("fs");
const path = require("path");
const http = require("http");
const { spawn, execSync } = require("child_process");

// "???web?????????" / "???????????.exe"
const WECHAT_DIR = "\u5fae\u4fe1web\u5f00\u53d1\u8005\u5de5\u5177";
const WECHAT_EXE = "\u5fae\u4fe1\u5f00\u53d1\u8005\u5de5\u5177.exe";

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

function probeUrl(host, port, pathname, timeoutMs = 2000) {
  return new Promise((resolve) => {
    const req = http.get({ host, port, path: pathname, timeout: timeoutMs }, (res) => {
      res.resume();
      resolve({ online: true, statusCode: res.statusCode });
    });
    req.on("timeout", () => {
      req.destroy();
      resolve({ online: false, error: "timeout" });
    });
    req.on("error", (err) => resolve({ online: false, error: err.message }));
  });
}

function readPid(file) {
  try {
    const t = fs.readFileSync(file, "utf8").trim();
    if (/^\d+$/.test(t)) return Number(t);
  } catch (_) {}
  return null;
}

function writePid(file, pid) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, String(pid), "utf8");
}

function isPidAlive(pid) {
  if (!pid) return false;
  try {
    process.kill(pid, 0);
    return true;
  } catch (_) {
    return false;
  }
}

function uniqueExisting(paths) {
  const out = [];
  const seen = new Set();
  for (const p of paths) {
    if (!p) continue;
    const n = path.normalize(p);
    if (seen.has(n.toLowerCase())) continue;
    seen.add(n.toLowerCase());
    if (fs.existsSync(n)) out.push(n);
  }
  return out;
}

function findWechatCli(configuredPath) {
  const candidates = [
    configuredPath,
    process.env.WECHAT_DEVTOOLS_CLI,
    path.join("D:", "CriticalApplication", WECHAT_DIR, "cli.bat"),
    path.join("C:", "Program Files (x86)", "Tencent", WECHAT_DIR, "cli.bat"),
    path.join("C:", "Program Files", "Tencent", WECHAT_DIR, "cli.bat"),
    path.join(process.env.LOCALAPPDATA || "", WECHAT_DIR, "cli.bat"),
    path.join(process.env.LOCALAPPDATA || "", "Programs", WECHAT_DIR, "cli.bat")
  ];
  return uniqueExisting(candidates)[0] || null;
}

function findWechatApp(cliPath) {
  if (cliPath) {
    const dir = path.dirname(cliPath);
    for (const name of [WECHAT_EXE, "wechatdevtools.exe", "gui.exe"]) {
      const p = path.join(dir, name);
      if (fs.existsSync(p)) return p;
    }
  }
  return (
    uniqueExisting([
      path.join("D:", "CriticalApplication", WECHAT_DIR, WECHAT_EXE),
      path.join("C:", "Program Files (x86)", "Tencent", WECHAT_DIR, WECHAT_EXE),
      path.join("C:", "Program Files", "Tencent", WECHAT_DIR, WECHAT_EXE)
    ])[0] || null
  );
}

function spawnDetached(command, args, options = {}) {
  const child = spawn(command, args, {
    detached: true,
    stdio: "ignore",
    windowsHide: false,
    shell: options.shell === true,
    cwd: options.cwd,
    env: options.env || process.env
  });
  child.unref();
  return child;
}

function openInBrowser(url) {
  spawnDetached("cmd.exe", ["/c", "start", "", url], { shell: false });
}

function resolveMavenCmd() {
  return process.platform === "win32" ? "mvn.cmd" : "mvn";
}

/**
 * Stop backend before clean+package so Windows can delete the locked jar.
 */
function stopBackendForBuild(ctx) {
  const { pidFile, backendPort } = ctx;
  const stopped = [];

  const pid = pidFile ? readPid(pidFile) : null;
  if (pid && isPidAlive(pid)) {
    process.stdout.write(`[local_sever] stopping backend pid=${pid} before clean/package (jar may be locked)\n`);
    try {
      process.kill(pid);
    } catch (_) {
      try {
        execSync(`taskkill /F /PID ${pid}`, { stdio: "ignore" });
      } catch (_) {}
    }
    stopped.push(`pid:${pid}`);
    try {
      if (pidFile && fs.existsSync(pidFile)) fs.unlinkSync(pidFile);
    } catch (_) {}
  }

  if (backendPort && process.platform === "win32") {
    try {
      const out = execSync(
        `powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort ${Number(backendPort)} -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique"`,
        { encoding: "utf8" }
      );
      const ids = String(out)
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter((s) => /^\d+$/.test(s))
        .map((s) => Number(s));
      for (const id of ids) {
        if (!id) continue;
        process.stdout.write(`[local_sever] stopping listener on port ${backendPort} pid=${id}\n`);
        try {
          process.kill(id);
        } catch (_) {
          try {
            execSync(`taskkill /F /PID ${id}`, { stdio: "ignore" });
          } catch (_) {}
        }
        stopped.push(`port:${backendPort}:pid:${id}`);
      }
    } catch (_) {}
  }

  // Kill any leftover java running ruoyi-admin.jar (locks target jar on Windows)
  if (process.platform === "win32") {
    try {
      const out = execSync(
        `powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \\"Name='java.exe'\\" | Where-Object { $_.CommandLine -match 'ruoyi-admin' } | Select-Object -ExpandProperty ProcessId"`,
        { encoding: "utf8" }
      );
      const ids = String(out)
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter((s) => /^\d+$/.test(s))
        .map((s) => Number(s));
      for (const id of ids) {
        if (!id) continue;
        process.stdout.write(`[local_sever] stopping ruoyi-admin java pid=${id}\n`);
        try {
          execSync(`taskkill /F /PID ${id}`, { stdio: "ignore" });
        } catch (_) {}
        stopped.push(`java:ruoyi-admin:${id}`);
      }
    } catch (_) {}
  }

  return stopped;
}

/**
 * Run Maven goals.
 * On Windows use cmd.exe /c so .cmd works, and stdio inherit so progress
 * shows in the start.bat console.
 */
let mavenRunning = false;

function runMavenGoals(backendRoot, logFile, goals) {
  const goalLabel = goals.join(" ");
  return new Promise((resolve, reject) => {
    if (mavenRunning) {
      const err = new Error("Maven already running. Watch the start.bat console for progress.");
      err.code = "MVN_BUSY";
      reject(err);
      return;
    }
    mavenRunning = true;

    fs.mkdirSync(path.dirname(logFile), { recursive: true });
    const stamp = `\n\n===== mvn ${goalLabel} ${new Date().toISOString()} =====\n`;
    fs.appendFileSync(logFile, stamp, "utf8");
    process.stdout.write(stamp);
    process.stdout.write(`[local_sever] cwd=${backendRoot}\n`);
    process.stdout.write(`[local_sever] running: mvn ${goalLabel}\n`);
    process.stdout.write("[local_sever] >>> Maven progress starts below (same window) <<<\n");

    // Windows: "mvn.cmd" cannot reliably run with shell:false + pipes.
    // Use cmd /c and inherit stdio so the bat console shows live progress.
    const isWin = process.platform === "win32";
    const command = isWin ? "cmd.exe" : resolveMavenCmd();
    const args = isWin ? ["/d", "/s", "/c", "mvn", ...goals] : goals;

    const child = spawn(command, args, {
      cwd: backendRoot,
      shell: false,
      stdio: ["ignore", "inherit", "inherit"],
      env: process.env,
      windowsHide: false
    });

    child.on("error", (err) => {
      mavenRunning = false;
      process.stdout.write(`[local_sever] mvn spawn error: ${err.message}\n`);
      reject(err);
    });

    child.on("close", (code) => {
      mavenRunning = false;
      const endLine = `[local_sever] mvn ${goalLabel} finished with exit code ${code}\n`;
      process.stdout.write(endLine);
      try {
        fs.appendFileSync(logFile, endLine, "utf8");
      } catch (_) {}

      if (code === 0) {
        resolve({ logFile, exitCode: code, goals });
      } else {
        const err = new Error(`mvn ${goalLabel} failed (exit ${code}). See start.bat console.`);
        err.code = "MVN_FAILED";
        err.logFile = logFile;
        reject(err);
      }
    });
  });
}

function runMavenPackage(backendRoot, logFile) {
  return runMavenGoals(backendRoot, logFile, ["package", "-DskipTests"]);
}

function runMavenClean(backendRoot, logFile) {
  return runMavenGoals(backendRoot, logFile, ["clean"]);
}

async function cleanBackend(ctx) {
  const { backendRoot, mavenLog, pidFile, backendPort, jarPath } = ctx;
  const stopped = stopBackendForBuild({ pidFile, backendPort, jarPath });
  if (stopped.length) {
    process.stdout.write(`[local_sever] stopped before clean: ${stopped.join(", ")}\n`);
    await sleep(2000);
  }
  const result = await runMavenClean(backendRoot, mavenLog);
  return {
    ok: true,
    action: "clean-backend",
    stoppedBackend: stopped,
    logFile: result.logFile
  };
}

async function buildBackendJar(ctx) {
  const { jarPath, backendRoot, mavenLog, pidFile, backendPort } = ctx;
  const stopped = stopBackendForBuild({ pidFile, backendPort, jarPath });
  if (stopped.length) {
    process.stdout.write(`[local_sever] stopped before package: ${stopped.join(", ")}\n`);
    await sleep(2000);
  }

  const result = await runMavenPackage(backendRoot, mavenLog);
  if (!fs.existsSync(jarPath)) {
    const err = new Error(`Maven package finished but jar still missing: ${jarPath}`);
    err.code = "NO_JAR_AFTER_BUILD";
    err.logFile = mavenLog;
    throw err;
  }
  let size = 0;
  try {
    size = fs.statSync(jarPath).size;
  } catch (_) {}
  return {
    ok: true,
    action: "package-backend",
    built: true,
    jarPath,
    size,
    stoppedBackend: stopped,
    logFile: result.logFile
  };
}

/**
 * Start backend only. Does NOT auto-run Maven.
 * Fails fast if java exits early (e.g. bad DB_PASS).
 */
async function ensureBackendRunning(ctx) {
  const {
    backendHost,
    backendPort,
    jarPath,
    backendWorkDir,
    pidFile,
    outLog,
    errLog,
    dbEnv
  } = ctx;

  const probe = await probeUrl(backendHost, backendPort, "/login");
  if (probe.online) {
    return { started: false, alreadyOnline: true, built: false, pid: readPid(pidFile) };
  }

  if (!fs.existsSync(jarPath)) {
    const err = new Error(
      `Backend jar not found: ${jarPath}. Click Maven Package first, then start backend.`
    );
    err.code = "NO_JAR";
    throw err;
  }

  const oldPid = readPid(pidFile);
  if (oldPid && isPidAlive(oldPid)) {
    for (let i = 0; i < 15; i++) {
      await sleep(1000);
      const again = await probeUrl(backendHost, backendPort, "/login");
      if (again.online) {
        return { started: false, alreadyOnline: true, built: false, pid: oldPid, waited: true };
      }
      if (!isPidAlive(oldPid)) break;
    }
  }

  fs.mkdirSync(path.dirname(outLog), { recursive: true });
  fs.appendFileSync(outLog, `\n===== start ${new Date().toISOString()} =====\n`, "utf8");
  fs.appendFileSync(errLog, `\n===== start ${new Date().toISOString()} =====\n`, "utf8");
  const outFd = fs.openSync(outLog, "a");
  const errFd = fs.openSync(errLog, "a");

  // Keep attached so we can detect early crash (do not detach/unref until ready)
  const child = spawn("java", ["-jar", jarPath, `--server.port=${backendPort}`], {
    cwd: backendWorkDir,
    detached: false,
    stdio: ["ignore", outFd, errFd],
    env: Object.assign({}, process.env, dbEnv || {}),
    windowsHide: true
  });
  writePid(pidFile, child.pid);
  process.stdout.write(`[local_sever] starting backend pid=${child.pid} port=${backendPort}\n`);

  let exitCode = null;
  child.on("exit", (code) => {
    exitCode = code;
  });

  for (let i = 0; i < 45; i++) {
    await sleep(1000);
    if (exitCode !== null) {
      let tail = "";
      try {
        const text = fs.readFileSync(errLog, "utf8");
        tail = text.slice(-1200);
      } catch (_) {}
      const err = new Error(
        `Backend process exited early (code ${exitCode}). Check DB_PASS in config.env and logs/backend.err.log\n${tail}`
      );
      err.code = "BACKEND_EXITED";
      throw err;
    }
    const again = await probeUrl(backendHost, backendPort, "/login");
    if (again.online) {
      child.unref();
      return { started: true, alreadyOnline: false, built: false, pid: child.pid };
    }
  }

  try {
    child.kill();
  } catch (_) {}
  const err = new Error(
    "Backend did not become ready within 45s. Check logs/backend.err.log and DB settings in config.env"
  );
  err.code = "BACKEND_TIMEOUT";
  err.pid = child.pid;
  throw err;
}

function switchMiniprogramBaseUrl(repoRoot, stateDir, baseUrl) {
  const requestJs = path.join(repoRoot, "miniprogram", "utils", "request.js");
  const backup = path.join(stateDir, "request.js.bak");
  const privateConfig = path.join(repoRoot, "miniprogram", "project.private.config.json");

  if (!fs.existsSync(requestJs)) {
    throw new Error(`Missing ${requestJs}`);
  }
  fs.mkdirSync(stateDir, { recursive: true });
  if (!fs.existsSync(backup)) {
    fs.copyFileSync(requestJs, backup);
  }

  let text = fs.readFileSync(requestJs, "utf8");
  if (!/let\s+BASE_URL\s*=\s*["'][^"']+["']/.test(text)) {
    throw new Error("BASE_URL assignment not found in request.js");
  }
  text = text.replace(/let\s+BASE_URL\s*=\s*["'][^"']+["']/, `let BASE_URL = "${baseUrl}"`);
  fs.writeFileSync(requestJs, text, "utf8");

  fs.writeFileSync(
    privateConfig,
    JSON.stringify(
      {
        description: "Local debug overrides from local_sever",
        setting: { urlCheck: false }
      },
      null,
      2
    ),
    "utf8"
  );

  return { requestJs, baseUrl };
}

async function openBackendUi(ctx) {
  const result = await ensureBackendRunning(ctx);
  const url = `http://${ctx.backendHost}:${ctx.backendPort}/`;
  openInBrowser(url);
  return {
    ok: true,
    action: "open-backend",
    url,
    ...result
  };
}

async function openMiniprogramUi(ctx) {
  const { repoRoot, stateDir, miniBaseUrl, wechatCliConfigured } = ctx;
  const projectDir = path.join(repoRoot, "miniprogram");
  if (!fs.existsSync(projectDir)) {
    throw new Error(`Miniprogram folder not found: ${projectDir}`);
  }

  const switched = switchMiniprogramBaseUrl(repoRoot, stateDir, miniBaseUrl);
  const cli = findWechatCli(wechatCliConfigured);
  const appExe = findWechatApp(cli);

  if (cli) {
    spawnDetached(cli, ["open", "--project", projectDir], { shell: true });
    return {
      ok: true,
      action: "open-miniprogram",
      method: "cli",
      cli,
      projectDir,
      baseUrl: switched.baseUrl,
      hint: "If DevTools did not open the project, enable Service Port in DevTools settings."
    };
  }

  if (appExe) {
    spawnDetached(appExe, [], { shell: false });
    return {
      ok: true,
      action: "open-miniprogram",
      method: "app",
      app: appExe,
      projectDir,
      baseUrl: switched.baseUrl,
      hint: "DevTools launched. Import Project and select the miniprogram folder."
    };
  }

  const err = new Error(
    "WeChat DevTools not found. Set WECHAT_DEVTOOLS_CLI in config.env to your cli.bat path."
  );
  err.code = "NO_WECHAT_TOOLS";
  err.projectDir = projectDir;
  throw err;
}

module.exports = {
  probeUrl,
  findWechatCli,
  openBackendUi,
  openMiniprogramUi,
  openInBrowser,
  switchMiniprogramBaseUrl,
  runMavenPackage,
  runMavenClean,
  buildBackendJar,
  cleanBackend
};
