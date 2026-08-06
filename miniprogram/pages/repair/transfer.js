const { api } = require("../../api/index.js");

Page({
  data: {
    repairId: null,
    keyword: "",
    users: [],
    filtered: []
  },

  onLoad(options) {
    const repairId = options.id || options.repairId;
    if (!repairId) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ repairId });
    this.loadUsers();
  },

  async loadUsers() {
    try {
      const res = await api.getRepairTransferUsers(this.data.repairId);
      const users = res.data || [];
      this.setData({ users, filtered: users });
    } catch (e) {
      this.setData({ users: [], filtered: [] });
    }
  },

  onKeyword(e) {
    const keyword = (e.detail.value || "").trim();
    const filtered = !keyword
      ? this.data.users
      : this.data.users.filter(
          (u) =>
            (u.userName || "").indexOf(keyword) >= 0 ||
            (u.phonenumber || "").indexOf(keyword) >= 0
        );
    this.setData({ keyword, filtered });
  },

  onSelect(e) {
    const { id, name } = e.currentTarget.dataset;
    wx.showModal({
      title: "提示",
      content: "确认转派给 " + name + " 吗？",
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await api.transferRepair({
            repairId: this.data.repairId,
            targetUserId: id
          });
          wx.showToast({ title: "转派成功", icon: "success" });
          setTimeout(() => {
            wx.navigateBack({ delta: 1 });
          }, 600);
        } catch (err) {
          // request layer toast
        }
      }
    });
  }
});
