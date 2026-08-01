const { api } = require("../../api/index.js");

Page({
  data: {
    equipmentId: null,
    companyId: null,
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
    if (options && options.id) {
      this.setData({ equipmentId: options.id });
      this.loadDetail(options.id);
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getEquipmentDetail(id);
      const detail = res.data || {};
      this.setData({
        companyId: detail.companyId,
        form: {
          equipmentName: detail.equipmentName || "",
          location: detail.location || "",
          systemName: detail.systemName || "",
          model: detail.model || "",
          manufacturer: detail.manufacturer || "",
          buildingId: detail.buildingId,
          buildingName: detail.buildingName || ""
        }
      });
      if (detail.companyId) {
        await this.loadBuildings(detail.companyId);
      }
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    }
  },

  async loadBuildings(companyId) {
    try {
      const res = await api.getBuildingsByCompany(companyId);
      const rows = res.data || res.rows || [];
      let buildingIndex = 0;
      if (this.data.form.buildingId) {
        const i = rows.findIndex((b) => b.buildingId === this.data.form.buildingId);
        if (i >= 0) buildingIndex = i;
      }
      this.setData({ buildingOptions: rows, buildingIndex });
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
      await api.editEquipment({
        equipmentId: this.data.equipmentId,
        companyId: this.data.companyId,
        buildingId: form.buildingId,
        equipmentName: form.equipmentName.trim(),
        location: form.location,
        systemName: form.systemName,
        model: form.model,
        manufacturer: form.manufacturer
      });
      wx.showToast({ title: "\u4fdd\u5b58\u6210\u529f", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      wx.showToast({ title: "\u4fdd\u5b58\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  }
});
