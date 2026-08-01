const { api } = require("../../api/index.js");

Page({
  data: {
    companyId: null,
    list: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
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
      const rows = ((res && (res.rows || res.data)) || []).map((item) =>
        Object.assign({}, item, {
          displayTime: this.formatTime(item.checkInTime || item.createTime)
        })
      );
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
  }
});
