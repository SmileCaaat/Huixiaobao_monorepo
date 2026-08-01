const { api } = require("../../api/index.js");

Page({
  data: {
    searchKeyword: "",
    allList: [],
    filteredList: [],
    loading: false
  },

  onShow() {
    this.loadData();
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value || "" });
    this.applyFilter();
  },

  onSearchConfirm() {
    this.applyFilter();
  },

  applyFilter() {
    const keyword = (this.data.searchKeyword || "").trim().toLowerCase();
    if (!keyword) {
      this.setData({ filteredList: this.data.allList });
      return;
    }
    const filteredList = this.data.allList.filter((item) => {
      const name = (item.companyName || "").toLowerCase();
      const addr = (item.address || "").toLowerCase();
      return name.includes(keyword) || addr.includes(keyword);
    });
    this.setData({ filteredList });
  },

  async loadData() {
    if (this.data.loading) return;
    this.setData({ loading: true });
    try {
      const res = await api.getMyCompanyList();
      const allList = (res && res.data) || [];
      this.setData({ allList, filteredList: allList });
      this.applyFilter();
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  onCallPhone(e) {
    e.stopPropagation && e.stopPropagation();
    const phone = e.currentTarget.dataset.phone;
    if (!phone) return;
    wx.makePhoneCall({ phoneNumber: String(phone) });
  },

  async onTapCard(e) {
    const companyId = e.currentTarget.dataset.id;
    const item = this.data.filteredList.find((c) => String(c.companyId) === String(companyId));
    if (!item) return;
    try {
      const res = await api.getCompanyDetail(item.companyId);
      this.showDetailModal((res && res.data) || item);
    } catch (err) {
      this.showDetailModal(item);
    }
  },

  showDetailModal(item) {
    const none = "\u65e0";
    const content = [
      "\u540d\u79f0: " + (item.companyName || none),
      "\u8054\u7cfb\u4eba: " + (item.contactPerson || none),
      "\u5730\u5740: " + (item.address || none),
      "\u7535\u8bdd: " + (item.contactPhone || none)
    ].join("\n");
    wx.showModal({
      title: "\u5ba2\u6237\u8be6\u60c5",
      content: content,
      showCancel: false
    });
  }
});
