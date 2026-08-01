const { api } = require("../../api/index.js");

Page({
  data: {
    buildingId: null,
    companyId: null,
    form: {
      buildingName: "",
      address: "",
      buildingType: "",
      floorCount: "",
      area: ""
    },
    typeOptions: ["\u4e00\u7c7b\u9ad8\u5c42", "\u4e8c\u7c7b\u9ad8\u5c42", "\u5730\u4e0b\u5efa\u7b51", "\u5176\u4ed6"],
    typeIndex: -1,
    saving: false
  },

  onLoad(options) {
    if (options && options.id) {
      this.setData({ buildingId: options.id });
      wx.setNavigationBarTitle({ title: "\u7f16\u8f91\u5efa\u7b51" });
      this.loadDetail(options.id);
    } else {
      wx.setNavigationBarTitle({ title: "\u65b0\u589e\u5efa\u7b51" });
    }
    this.initCompany();
  },

  async initCompany() {
    try {
      const res = await api.getCurrentCompany();
      if (res && res.data) {
        this.setData({ companyId: res.data.companyId });
      }
    } catch (e) {
      // ignore
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getBuildingDetail(id);
      const detail = res.data || {};
      const typeIndex = this.data.typeOptions.indexOf(detail.buildingType);
      this.setData({
        companyId: detail.companyId || this.data.companyId,
        form: {
          buildingName: detail.buildingName || "",
          address: detail.address || "",
          buildingType: detail.buildingType || "",
          floorCount: detail.floorCount != null ? String(detail.floorCount) : "",
          area: detail.area != null ? String(detail.area) : ""
        },
        typeIndex: typeIndex >= 0 ? typeIndex : -1
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ ["form." + field]: e.detail.value });
  },

  onTypeChange(e) {
    const idx = Number(e.detail.value);
    this.setData({
      typeIndex: idx,
      "form.buildingType": this.data.typeOptions[idx]
    });
  },

  async handleSubmit() {
    if (this.data.saving) return;
    const form = this.data.form;
    if (!this.data.companyId) {
      return wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
    }
    if (!form.buildingName || !form.buildingName.trim()) {
      return wx.showToast({ title: "\u8bf7\u8f93\u5165\u5efa\u7b51\u540d\u79f0", icon: "none" });
    }
    if (!form.address || !form.address.trim()) {
      return wx.showToast({ title: "\u8bf7\u8f93\u5165\u5730\u5740", icon: "none" });
    }
    this.setData({ saving: true });
    try {
      const payload = {
        companyId: this.data.companyId,
        buildingName: form.buildingName.trim(),
        address: form.address.trim(),
        buildingType: form.buildingType,
        floorCount: form.floorCount ? Number(form.floorCount) : null,
        area: form.area ? Number(form.area) : null
      };
      if (this.data.buildingId) {
        payload.buildingId = this.data.buildingId;
        await api.updateBuilding(payload);
      } else {
        await api.addBuilding(payload);
      }
      wx.showToast({ title: "\u4fdd\u5b58\u6210\u529f", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      wx.showToast({ title: "\u4fdd\u5b58\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  }
});
