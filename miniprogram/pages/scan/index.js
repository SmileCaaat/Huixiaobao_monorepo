const { api } = require("../../api/index.js");
const auth = require("../../services/auth.js");

Page({
  data: {
    result: null,
    error: ""
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/login/index" });
    }
  },

  startScan() {
    wx.scanCode({
      onlyFromCamera: false,
      success: async (res) => {
        const raw = (res.result || "").trim();
        const code = this.extractEquipmentCode(raw);
        if (!code) {
          this.setData({ error: "无法识别设备编码", result: null });
          return;
        }
        try {
          const apiRes = await api.scanEquipment(code);
          this.setData({ result: apiRes.data || null, error: "" });
        } catch (e) {
          this.setData({
            result: null,
            error: (e && e.msg) || "查询失败"
          });
        }
      },
      fail: () => {
        this.setData({ error: "扫码取消或失败", result: null });
      }
    });
  },

  extractEquipmentCode(raw) {
    if (!raw) return "";
    const m = raw.match(/equipment\/(?:api\/)?([^/?#]+)/i);
    if (m) return decodeURIComponent(m[1]);
    if (/^[A-Za-z0-9_-]+$/.test(raw)) return raw;
    return raw;
  },

  goDetail() {
    const id = this.data.result && this.data.result.equipmentId;
    if (id) {
      wx.navigateTo({ url: "/pages/equipment/detail?id=" + id });
    }
  }
});
