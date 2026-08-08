const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");
const { BASE_URL } = require("../../services/request.js");
const auth = require("../../services/auth.js");

const STATUS_MAP = {
  "0": { text: "待处理", tone: "muted" },
  "1": { text: "处理中", tone: "warn" },
  "2": { text: "已完成", tone: "ok" }
};

function fullImageUrl(url) {
  if (!url) return "";
  const s = String(url).trim();
  if (!s) return "";
  if (/^https?:\/\//i.test(s) || /^wxfile:\/\//i.test(s) || /^data:/i.test(s)) {
    return s;
  }
  return BASE_URL + (s.charAt(0) === "/" ? s : "/" + s);
}

function splitImageUrls(value) {
  if (!value) return [];
  return String(value)
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean)
    .map(fullImageUrl)
    .filter(Boolean);
}

function formatDate(v) {
  if (!v) return "-";
  const s = String(v);
  return s.length >= 10 ? s.slice(0, 10) : s;
}

function formatDateTime(v) {
  if (!v) return "-";
  const s = String(v).replace("T", " ");
  return s.length >= 19 ? s.slice(0, 19) : s;
}

function toDatePick(v) {
  if (!v) return "";
  return String(v).slice(0, 10);
}

function currentUserId() {
  const user = auth.getUser && auth.getUser();
  if (!user) return "";
  const id =
    user.userId ||
    user.id ||
    (user.user && (user.user.userId || user.user.id)) ||
    "";
  return id === "" || id == null ? "" : String(id);
}

async function ensureUserId() {
  let id = currentUserId();
  if (id) return id;
  try {
    const res = await api.getUserInfo();
    const data = (res && res.data) || {};
    if (data.userId) {
      auth.setUser({
        userId: data.userId,
        userName: data.userName,
        loginName: data.loginName,
        phonenumber: data.phonenumber,
        avatar: data.avatar,
        auditStatus: data.auditStatus,
        allowMiniLogin: data.allowMiniLogin,
        wxBound: data.wxBound
      });
      return String(data.userId);
    }
  } catch (e) {
    // ignore
  }
  return "";
}

function buildLogDisplay(item) {
  const name = item.operatorName || "员工";
  const type = String(item.actionType || "");
  if (type === "create") return name + "提交了维修工单";
  if (type === "complete") return name + "完成了维修";
  return item.actionContent || "-";
}

Page({
  data: {
    repairId: null,
    detail: null,
    activeTab: "info",
    statusText: "",
    statusTone: "muted",
    statusCode: "",
    reporterText: "",
    handlerText: "",
    foundTimeText: "",
    startTimeText: "-",
    completeTimeText: "-",
    startTimePick: "",
    completeTimePick: "",
    faultImages: [],
    repairImages: [],
    logs: [],
    submitLog: null,
    completeLog: null,
    progress: { accept: false, started: false, done: false },
    showMaintenance: false,
    isHandler: false,
    canEdit: false,
    canMaintain: false,
    canTransfer: false,
    repairDescription: "",
    descCount: 0,
    completeImages: [],
    submitting: false
  },

  onLoad(options) {
    const repairId = options.id || options.repairId;
    if (!repairId) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ repairId: repairId });
  },

  async onShow() {
    if (!this.data.repairId) return;
    await this.loadDetail();
    await this.loadLogs();
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  async loadDetail() {
    try {
      const res = await api.getRepairDetail(this.data.repairId);
      const detail = res.data || {};
      // 统一成字符串，避免 wxml 比较失败
      const status = String(detail.repairStatus == null ? "0" : detail.repairStatus);
      detail.repairStatus = status;

      const st = STATUS_MAP[status] || STATUS_MAP["0"];
      const faultImages = splitImageUrls(detail.faultImages);
      const repairImageRaws = String(detail.repairImages || "")
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean);
      const repairImages = repairImageRaws.map(fullImageUrl);

      const userId = await ensureUserId();
      const isHandler =
        !!userId &&
        detail.repairUserId != null &&
        String(detail.repairUserId) === userId;
      const isReporter =
        !!userId &&
        detail.reporterId != null &&
        String(detail.reporterId) === userId;
      const started = !!detail.startTime;

      // 处理中/已完成始终展示维修信息板块（对齐参考图）
      const showMaintenance = status === "1" || status === "2" || !!detail.repairUserId;
      // 当前处理人且处理中：可编辑维修信息、转派、底部暂停/完成
      const canMaintain = isHandler && status === "1";
      const canTransfer = canMaintain;
      const canEdit =
        (status === "0" || (status === "1" && !started)) && (isHandler || isReporter);

      const desc = detail.repairDescription || "";
      const existingComplete = repairImageRaws.map((url) => ({
        serverUrl: url,
        tempPath: "",
        previewUrl: fullImageUrl(url),
        uploading: false
      }));

      this.setData({
        detail: detail,
        statusCode: status,
        statusText: status === "1" ? "处理中" : st.text,
        statusTone: st.tone,
        reporterText:
          (detail.reporterName || "-") +
          (detail.reporterPhone ? " " + detail.reporterPhone : ""),
        handlerText:
          (detail.repairPerson || detail.repairUserName || "-") +
          (detail.repairPhone ? " " + detail.repairPhone : ""),
        foundTimeText: formatDate(detail.foundTime),
        startTimeText: formatDate(detail.startTime),
        completeTimeText: formatDate(detail.completeTime),
        startTimePick: toDatePick(detail.startTime),
        completeTimePick: toDatePick(detail.completeTime),
        faultImages: faultImages,
        repairImages: repairImages,
        showMaintenance: showMaintenance,
        isHandler: isHandler,
        canEdit: canEdit,
        canMaintain: canMaintain,
        canTransfer: canTransfer,
        repairDescription: desc,
        descCount: desc.length,
        completeImages: existingComplete,
        progress: {
          accept: !!(detail.acceptTime || detail.dispatchTime || detail.repairUserId),
          started: started || status === "2",
          done: status === "2"
        }
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  async loadLogs() {
    try {
      const res = await api.getRepairLogs(this.data.repairId);
      const raw = res.data || [];
      const logs = raw.map((item) => {
        const operatorName = item.operatorName || "";
        return Object.assign({}, item, {
          operatorName: operatorName,
          displayText: buildLogDisplay(item),
          timeText: formatDateTime(item.createTime)
        });
      });

      let submitLog = logs.find((item) => String(item.actionType) === "create") || null;
      let completeLog = logs.find((item) => String(item.actionType) === "complete") || null;
      const detail = this.data.detail || {};

      if (!submitLog && (detail.reporterName || detail.createTime)) {
        submitLog = {
          operatorName: detail.reporterName || "员工",
          timeText: formatDateTime(detail.createTime || detail.foundTime)
        };
      }
      if (!completeLog && String(detail.repairStatus) === "2") {
        completeLog = {
          operatorName: detail.repairPerson || detail.repairUserName || "员工",
          timeText: formatDateTime(detail.completeTime)
        };
      }

      this.setData({
        logs: logs,
        submitLog: submitLog
          ? {
              operatorName: submitLog.operatorName || "员工",
              timeText: submitLog.timeText || formatDateTime(submitLog.createTime)
            }
          : null,
        completeLog: completeLog
          ? {
              operatorName: completeLog.operatorName || "员工",
              timeText: completeLog.timeText || formatDateTime(completeLog.createTime)
            }
          : null
      });
    } catch (e) {
      this.setData({ logs: [], submitLog: null, completeLog: null });
    }
  },

  previewFaultImage(e) {
    const urls = this.data.faultImages || [];
    if (!urls.length) return;
    wx.previewImage({
      urls: urls,
      current: urls[e.currentTarget.dataset.index] || urls[0]
    });
  },

  previewRepairImage(e) {
    const urls = this.data.repairImages || [];
    if (!urls.length) return;
    wx.previewImage({
      urls: urls,
      current: urls[e.currentTarget.dataset.index] || urls[0]
    });
  },

  goEdit() {
    wx.navigateTo({ url: "/pages/repair/form?id=" + this.data.repairId });
  },

  goTransfer() {
    if (!this.data.repairId) {
      return wx.showToast({ title: "参数缺失", icon: "none" });
    }
    wx.navigateTo({
      url: "/pages/repair/transfer?id=" + this.data.repairId
    });
  },

  onStartTimeChange(e) {
    this.setData({ startTimePick: e.detail.value });
  },

  onCompleteTimeChange(e) {
    this.setData({ completeTimePick: e.detail.value });
  },

  onRepairDescInput(e) {
    const repairDescription = e.detail.value || "";
    this.setData({
      repairDescription: repairDescription,
      descCount: repairDescription.length
    });
  },

  chooseCompleteImage() {
    const remain = 4 - this.data.completeImages.length;
    if (remain <= 0) return;
    wx.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => {
        (res.tempFilePaths || []).forEach((path) => this.uploadCompleteImg(path));
      }
    });
  },

  async uploadCompleteImg(tempPath) {
    const completeImages = this.data.completeImages.concat([
      {
        tempPath: tempPath,
        serverUrl: "",
        previewUrl: tempPath,
        uploading: true
      }
    ]);
    const index = completeImages.length - 1;
    this.setData({ completeImages: completeImages });
    try {
      const body = await uploadFile(tempPath);
      const url =
        body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!url) {
        this.removeCompleteImage(index);
        return;
      }
      this.setData({
        ["completeImages[" + index + "].serverUrl"]: url,
        ["completeImages[" + index + "].previewUrl"]: fullImageUrl(url),
        ["completeImages[" + index + "].uploading"]: false
      });
    } catch (e) {
      this.removeCompleteImage(index);
    }
  },

  removeCompleteImage(e) {
    const index = typeof e === "number" ? e : e.currentTarget.dataset.index;
    const completeImages = this.data.completeImages.slice();
    completeImages.splice(index, 1);
    this.setData({ completeImages: completeImages });
  },

  buildRepairImages() {
    return this.data.completeImages
      .map((i) => {
        let url = i.serverUrl || "";
        if (url.indexOf(BASE_URL) === 0) {
          url = url.slice(BASE_URL.length);
        }
        return url;
      })
      .filter(Boolean)
      .join(",");
  },

  buildProgressPayload() {
    const payload = {
      repairId: this.data.repairId,
      repairDescription: this.data.repairDescription || "",
      repairImages: this.buildRepairImages()
    };
    if (this.data.startTimePick) {
      payload.startTime = this.data.startTimePick + " 00:00:00";
    }
    if (this.data.completeTimePick) {
      payload.completeTime = this.data.completeTimePick + " 00:00:00";
    }
    return payload;
  },

  async ensureStarted() {
    const detail = this.data.detail || {};
    if (!detail.startTime) {
      await api.startRepair(this.data.repairId);
    }
  },

  async handleSaveProgress() {
    if (this.data.submitting) return;
    if (this.data.completeImages.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中", icon: "none" });
    }
    this.setData({ submitting: true });
    try {
      await this.ensureStarted();
      await api.pauseRepair(this.buildProgressPayload());
      wx.showToast({ title: "已保存维修信息", icon: "success" });
      await this.loadDetail();
      await this.loadLogs();
    } catch (e) {
      // toast by request
    } finally {
      this.setData({ submitting: false });
    }
  },

  async handlePause() {
    if (this.data.submitting) return;
    if (this.data.completeImages.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中", icon: "none" });
    }
    this.setData({ submitting: true });
    try {
      await this.ensureStarted();
      await api.pauseRepair(this.buildProgressPayload());
      wx.showToast({ title: "已暂停并保存", icon: "success" });
      await this.loadDetail();
      await this.loadLogs();
    } catch (e) {
      // toast
    } finally {
      this.setData({ submitting: false });
    }
  },

  async handleComplete() {
    if (this.data.submitting) return;
    const repairDescription = (this.data.repairDescription || "").trim();
    if (!repairDescription) {
      return wx.showToast({ title: "请填写维修说明", icon: "none" });
    }
    if (this.data.completeImages.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中", icon: "none" });
    }
    this.setData({ submitting: true });
    try {
      await this.ensureStarted();
      const payload = {
        repairId: this.data.repairId,
        repairDescription: repairDescription,
        repairImages: this.buildRepairImages()
      };
      if (this.data.startTimePick) {
        payload.startTime = this.data.startTimePick + " 00:00:00";
      }
      if (this.data.completeTimePick) {
        payload.completeTime = this.data.completeTimePick + " 00:00:00";
      }
      await api.completeRepair(payload);
      wx.showToast({ title: "已完成", icon: "success" });
      await this.loadDetail();
      await this.loadLogs();
    } catch (e) {
      // toast
    } finally {
      this.setData({ submitting: false });
    }
  }
});
