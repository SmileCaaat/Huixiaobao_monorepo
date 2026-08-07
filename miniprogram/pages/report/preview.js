const { api } = require("../../api/index.js");

function formatDatePart(value) {
  if (!value) return "";
  const text = String(value).replace("T", " ");
  return text.length >= 10 ? text.slice(0, 10) : text;
}

function formatDateTime(value) {
  if (!value) return "-";
  const text = String(value).replace("T", " ");
  return text.length >= 19 ? text.slice(0, 19) : text;
}

function formatPlanTime(start, end) {
  const s = formatDatePart(start);
  const e = formatDatePart(end);
  if (s && e) return s + " 至 " + e;
  if (s) return s;
  if (e) return e;
  return "-";
}

function detectTypeLabel(name) {
  const text = String(name || "").toLowerCase();
  if (text.endsWith(".pdf")) return "PDF";
  if (text.endsWith(".docx") || text.endsWith(".doc")) return "Word";
  return "报告";
}

Page({
  data: {
    reportId: "",
    loading: true,
    errorText: "",
    title: "",
    fileName: "",
    fileTypeLabel: "报告",
    planTimeText: "-",
    managerText: "-",
    operatorText: "-",
    createTimeText: "-",
    previewLoading: false,
    previewReady: false,
    previewError: "",
    downloading: false
  },

  onLoad(query) {
    const reportId = (query && (query.id || query.reportId)) || "";
    this.setData({ reportId: String(reportId || "") });
    this.reload();
  },

  reload() {
    const reportId = this.data.reportId;
    if (!reportId) {
      this.setData({ loading: false, errorText: "报告不存在" });
      return;
    }
    this.setData({
      loading: true,
      errorText: "",
      previewLoading: false,
      previewReady: false,
      previewError: ""
    });
    this.loadDetail()
      .then(() => this.preparePreview(true))
      .catch(() => {});
  },

  async loadDetail() {
    try {
      const res = await api.getReportDetail(this.data.reportId);
      const row = (res && (res.data || res)) || {};
      const title = row.taskName || row.reportName || "维保报告";
      const fileName = row.reportName || row.filePath || "-";
      this.setData({
        loading: false,
        title,
        fileName,
        fileTypeLabel: detectTypeLabel(fileName),
        planTimeText: formatPlanTime(row.planStartTime, row.planEndTime),
        managerText: row.managerName || "-",
        operatorText: row.operatorNames || "-",
        createTimeText: formatDateTime(row.createTime)
      });
      wx.setNavigationBarTitle({ title: "报告预览" });
    } catch (e) {
      this.setData({
        loading: false,
        errorText: (e && (e.msg || e.message)) || "加载失败"
      });
      throw e;
    }
  },

  async preparePreview(autoOpen) {
    if (!this.data.reportId) return;
    this.setData({ previewLoading: true, previewReady: false, previewError: "" });
    try {
      const result = await api.fetchReportPreviewFile(this.data.reportId);
      this._previewPath = result && result.tempFilePath;
      this._previewHeader = result && result.header;
      this.setData({ previewLoading: false, previewReady: !!this._previewPath });
      if (autoOpen && this._previewPath) {
        this.openPreview();
      }
    } catch (e) {
      this.setData({
        previewLoading: false,
        previewReady: false,
        previewError: "预览文件加载失败"
      });
    }
  },

  openPreview() {
    if (this.data.previewLoading) return;
    if (!this._previewPath) {
      this.preparePreview(true);
      return;
    }
    api.openReportDocument(this._previewPath, this._previewHeader).catch(() => {});
  },

  async handleDownload() {
    if (this.data.downloading || !this.data.reportId) return;
    this.setData({ downloading: true });
    try {
      await api.downloadReport(this.data.reportId);
    } catch (e) {
      // toast already handled in api
    } finally {
      this.setData({ downloading: false });
    }
  }
});
