const { api } = require("../../api/index.js");

function mapUser(u) {
  const name = (u && (u.userName || u.nickName || u.loginName)) || "-";
  const phone = (u && (u.phonenumber || u.phone || u.phonenumber)) || "";
  return {
    userId: u.userId,
    userName: name,
    phonenumber: phone,
    displayPhone: phone || "暂无电话"
  };
}

Page({
  data: {
    repairId: null,
    keyword: "",
    users: [],
    filtered: [],
    loading: true,
    errorMsg: ""
  },

  onLoad(options) {
    const repairId = options.id || options.repairId;
    if (!repairId) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ repairId: String(repairId) });
    this.loadUsers();
  },

  async loadUsers() {
    this.setData({ loading: true, errorMsg: "" });
    try {
      const res = await api.getRepairTransferUsers(this.data.repairId);
      const users = (res.data || res.rows || []).map(mapUser).filter((u) => !!u.userId);
      this.setData({
        users: users,
        filtered: users,
        loading: false,
        errorMsg: users.length ? "" : "暂无同部门可转派人员"
      });
    } catch (e) {
      const msg = (e && e.message) || "加载失败";
      this.setData({
        users: [],
        filtered: [],
        loading: false,
        errorMsg: msg
      });
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
    this.setData({ keyword: keyword, filtered: filtered });
  },

  onSelect(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name || "该员工";
    if (!id) return;
    wx.showModal({
      title: "确认转派",
      content: "确认转派给 " + name + " 吗？",
      success: async (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: "转派中", mask: true });
        try {
          await api.transferRepair({
            repairId: this.data.repairId,
            targetUserId: id
          });
          wx.hideLoading();
          wx.showToast({ title: "转派成功", icon: "success" });
          setTimeout(() => {
            wx.navigateBack({ delta: 1 });
          }, 500);
        } catch (err) {
          wx.hideLoading();
        }
      }
    });
  }
});
