const { api } = require("../../api/index.js");

Page({
  data: {
    companyId: null,
    category: "",
    form: {
      equipmentName: "",
      location: "",
      systemName: "",
      model: "",
      manufacturer: "",
      buildingId: null,
      buildingName: ""
    },
    buildingOptions: [],
    buildingIndex: 0,
    saving: false
  },

  onLoad(options) {
    const category = options && options.category ? decodeURIComponent(options.category) : "";
    this.setData({ category });
    this.initCompany();
  },

  async initCompany() {
    try {
      const res = await api.getCurrentCompany();
      if (res && res.data) {
        this.setData({ companyId: res.data.companyId });
        await this.loadBuildings(res.data.companyId);
      }
    } catch (e) {
      // ignore
    }
  },

  async loadBuildings(companyId) {
    if (!companyId) return;
    try {
      const res = await api.getBuildingsByCompany(companyId);
      const rows = res.data || res.rows || [];
      this.setData({ buildingOptions: rows });
    } catch (e) {
      // ignore
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ ["form." + field]: e.detail.value });
  },

  onBuildingChange(e) {
    const idx = Number(e.detail.value);
    const building = this.data.buildingOptions[idx];
    if (!building) return;
    this.setData({
      buildingIndex: idx,
      "form.buildingId": building.buildingId,
      "form.buildingName": building.buildingName
    });
  },

  async handleSubmit() {
    if (this.data.saving) return;
    const form = this.data.form;
    if (!this.data.companyId) {
      return wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
    }
    if (!form.equipmentName || !form.equipmentName.trim()) {
      return wx.showToast({ title: "\u8bf7\u8f93\u5165\u8bbe\u5907\u540d\u79f0", icon: "none" });
    }
    if (!form.buildingId) {
      return wx.showToast({ title: "\u8bf7\u9009\u62e9\u5efa\u7b51", icon: "none" });
    }
    this.setData({ saving: true });
    try {
      const payload = {
        companyId: this.data.companyId,
        buildingId: form.buildingId,
        equipmentName: form.equipmentName.trim(),
        location: form.location,
        systemName: form.systemName,
        model: form.model,
        manufacturer: form.manufacturer
      };
      if (this.data.category) payload.projectCategory = this.data.category;
      await api.addEquipment(payload);
      wx.showToast({ title: "\u4fdd\u5b58\u6210\u529f", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      wx.showToast({ title: "\u4fdd\u5b58\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  }
});
