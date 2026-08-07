const { api } = require("../../api/index.js");
const { BASE_URL } = require("../../services/request.js");

function fullImageUrl(url) {
  if (!url) return "";
  return /^https?:\/\//i.test(url) ? url : BASE_URL + (url.charAt(0) === "/" ? url : "/" + url);
}

Page({
  data: {
    companyId: null,
    list: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    if (options && options.companyId) {
      this.setData({ companyId: options.companyId });
    }
  },

  onShow() {
    this.resetAndLoad();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadList(false);
    }
  },

  onPullDownRefresh() {
    this.resetAndLoad().finally(() => wx.stopPullDownRefresh());
  },

  async resetAndLoad() {
    this.setData({ list: [], pageNum: 1, hasMore: true });
    await this.loadList(true);
  },

  async loadList(reset) {
    if (this.data.loading) return;
    if (!reset && !this.data.hasMore) return;

    this.setData({ loading: true });
    try {
      let companyId = this.data.companyId;
      if (!companyId) {
        const companyRes = await api.getCurrentCompany();
        companyId = companyRes && companyRes.data && companyRes.data.companyId;
        this.setData({ companyId: companyId || null });
      }
      if (!companyId) {
        this.setData({ list: [], hasMore: false, loading: false });
        return;
      }

      const pageNum = reset ? 1 : this.data.pageNum;
      const res = await api.getCheckInList({
        companyId: companyId,
        pageNum: pageNum,
        pageSize: this.data.pageSize
      });
      const rows = ((res && (res.rows || res.data)) || []).map((item) => {
        const isOut = String(item.checkInType) === "1";
        const images = (item.images || [])
          .map((img) => fullImageUrl((img && img.imageUrl) || img))
          .filter(Boolean);
        return Object.assign({}, item, {
          typeLabel: isOut ? "签退" : "签到",
          typeClass: isOut ? "badge-warn" : "badge-ok",
          displayTime: this.formatTime(item.checkInTime || item.createTime),
          companyName: item.companyName || "-",
          addressText: item.address || item.checkInAddress || "-",
          previewImages: images
        });
      });
      const total = (res && res.total) || 0;
      const list = reset ? rows : this.data.list.concat(rows);
      const hasMore = list.length < total && rows.length >= this.data.pageSize;
      this.setData({
        list: list,
        pageNum: pageNum + 1,
        hasMore: hasMore
      });
    } catch (e) {
      if (reset) this.setData({ list: [] });
    } finally {
      this.setData({ loading: false });
    }
  },

  formatTime(timeStr) {
    if (!timeStr) return "";
    const s = String(timeStr);
    return s.length >= 19 ? s.substring(0, 19).replace("T", " ") : s;
  },

  previewImage(e) {
    const checkInId = e.currentTarget.dataset.id;
    const current = e.currentTarget.dataset.current;
    const item = (this.data.list || []).find((row) => String(row.checkInId) === String(checkInId));
    const urls = (item && item.previewImages) || [];
    if (!urls.length) return;
    wx.previewImage({ urls: urls, current: current || urls[0] });
  }
});
