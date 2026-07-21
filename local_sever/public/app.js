(() => {
  const els = {
    heroStatus: document.getElementById("heroStatus"),
    cleanBackendBtn: document.getElementById("cleanBackendBtn"),
    packageBackendBtn: document.getElementById("packageBackendBtn"),
    openBackendBtn: document.getElementById("openBackendBtn"),
    openMiniBtn: document.getElementById("openMiniBtn"),
    refreshBtn: document.getElementById("refreshBtn"),
    launchMsg: document.getElementById("launchMsg"),
    dashUrl: document.getElementById("dashUrl"),
    backendUrl: document.getElementById("backendUrl"),
    adminLink: document.getElementById("adminLink")
  };

  let healthCache = null;

  const actionButtons = [
    els.cleanBackendBtn,
    els.packageBackendBtn,
    els.openBackendBtn,
    els.openMiniBtn,
    els.refreshBtn
  ];

  function setPill(key, text, cls) {
    const node = els.heroStatus.querySelector(`[data-key="${key}"]`);
    if (!node) return;
    node.textContent = text;
    node.className = `pill ${cls || ""}`.trim();
  }

  function setLaunchBusy(busy) {
    const disabled = !!busy;
    actionButtons.forEach((btn) => {
      if (btn) btn.disabled = disabled;
    });
  }

  async function loadHealth() {
    const res = await fetch("/local/health");
    const data = await res.json();
    healthCache = data;

    const online = data.backend && data.backend.online;
    const target = (data.config && data.config.backendTarget) || "-";
    setPill(
      "backend",
      online ? `后端在线 · ${target}` : `后端离线 · ${target}`,
      online ? "ok" : "bad"
    );

    const mini = data.miniprogram || {};
    setPill(
      "mini",
      `小程序 BASE_URL · ${mini.mode || "unknown"} · ${mini.baseUrl || "-"}`,
      mini.mode === "local" ? "ok" : "warn"
    );

    const jarExists = data.config && data.config.jarExists;
    setPill(
      "jar",
      jarExists ? "jar 已就绪" : "jar 未打包",
      jarExists ? "ok" : "warn"
    );

    const port = data.dashboardPort;
    els.dashUrl.textContent = port ? `http://127.0.0.1:${port}/` : "-";
    els.backendUrl.textContent = target;
    els.adminLink.href = target.endsWith("/") ? target : target + "/";
    return data;
  }

  async function launch(kind) {
    const endpoints = {
      clean: "/local/clean/backend",
      package: "/local/build/backend",
      backend: "/local/launch/backend",
      miniprogram: "/local/launch/miniprogram"
    };
    const timeouts = {
      clean: 10 * 60 * 1000,
      package: 20 * 60 * 1000,
      backend: 3 * 60 * 1000,
      miniprogram: 2 * 60 * 1000
    };
    const waiting = {
      clean: "正在 Maven Clean，请看 start.bat 控制台进度…",
      package: "正在 Maven Package，请看 start.bat 控制台进度…",
      backend: "正在启动 / 打开后端管理端…",
      miniprogram: "正在启动 / 打开小程序开发者工具…"
    };

    const endpoint = endpoints[kind];
    const timeoutMs = timeouts[kind];
    if (!endpoint) return;

    els.launchMsg.textContent = waiting[kind] || "处理中…";
    setLaunchBusy(true);

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const res = await fetch(endpoint, { method: "POST", signal: controller.signal });
      const data = await res.json().catch(() => ({}));
      const code = data.code;
      const ok = res.ok && (code === 200 || code === 0);

      if (!ok) {
        els.launchMsg.textContent = data.msg || ("操作失败（" + res.status + "）");
        return;
      }

      const payload = data.data || {};
      if (kind === "clean") {
        els.launchMsg.textContent = data.msg || "Maven Clean 完成。详情见 start.bat 控制台。";
        await loadHealth().catch(() => {});
      } else if (kind === "package") {
        const jarPath = payload.jarPath || payload.path || payload.jar || "";
        const parts = [data.msg || "Maven Package 完成"];
        if (jarPath) parts.push("jar：" + jarPath);
        parts.push("详情见 start.bat 控制台");
        els.launchMsg.textContent = parts.join(" · ");
        await loadHealth().catch(() => {});
      } else if (kind === "backend") {
        const url =
          payload.url ||
          (healthCache && healthCache.config && healthCache.config.backendTarget) ||
          "";
        const parts = [];
        if (payload.started) parts.push("后端已启动");
        else parts.push(data.msg || "后端已就绪");
        if (url) parts.push(url);
        els.launchMsg.textContent = parts.join(" · ");
        setTimeout(() => {
          loadHealth().catch(() => {});
        }, 2000);
      } else {
        els.launchMsg.textContent =
          data.msg ||
          "小程序已触发打开开发者工具。若未弹出，请在开发者工具中导入并打开 miniprogram 目录。";
        await loadHealth().catch(() => {});
      }
    } catch (err) {
      if (err && err.name === "AbortError") {
        const abortMsgs = {
          clean: "Clean 超时（已等待 10 分钟），请查看 start.bat 控制台。",
          package: "Package 超时（已等待 20 分钟），请查看 start.bat 控制台。",
          backend: "启动后端超时（已等待 3 分钟），请查看相关日志。",
          miniprogram: "打开小程序超时（已等待 2 分钟），请检查微信开发者工具。"
        };
        els.launchMsg.textContent = abortMsgs[kind] || "请求超时";
      } else {
        els.launchMsg.textContent = err.message || String(err);
      }
    } finally {
      clearTimeout(timer);
      setLaunchBusy(false);
    }
  }

  els.cleanBackendBtn.addEventListener("click", () => launch("clean"));
  els.packageBackendBtn.addEventListener("click", () => launch("package"));
  els.openBackendBtn.addEventListener("click", () => launch("backend"));
  els.openMiniBtn.addEventListener("click", () => launch("miniprogram"));
  els.refreshBtn.addEventListener("click", () => {
    els.launchMsg.textContent = "正在刷新状态…";
    loadHealth()
      .then(() => {
        els.launchMsg.textContent = "状态已刷新";
      })
      .catch((err) => {
        els.launchMsg.textContent = err.message || String(err);
      });
  });

  loadHealth().catch((err) => {
    els.launchMsg.textContent = err.message || String(err);
  });
  setInterval(() => {
    loadHealth().catch(() => {});
  }, 15000);
})();
