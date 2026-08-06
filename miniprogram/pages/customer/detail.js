const { api } = require("../../api/index.js");

const BUILDING_TYPE_LABEL = {
  "type1_high_rise_civil": "一类高层民用建筑",
  "type2_high_rise_civil": "二类高层民用建筑",
  "high_rise_factory": "高层厂房",
  "high_rise_warehouse": "高层库房",
  "single_multi_civil": "单、多层民用建筑",
  "single_multi_factory": "单、多层厂房",
  "single_multi_warehouse": "单、多层库房",
  "underground": "地下建筑",
  "tunnel_culvert": "隧道、涵洞",
  "other": "其他建筑"
};

function textOrDash(value) {
  if (value === 0 || value === "0") return String(value);
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

function formatArea(value) {
  if (value === null || value === undefined || value === "") return "-";
  return textOrDash(value) + " m\u00B2";
}

function formatHeight(value) {
  if (value === null || value === undefined || value === "") return "-";
  return textOrDash(value) + " m";
}

function formatBuildingType(value) {
  if (value === null || value === undefined || value === "") return "-";
  return BUILDING_TYPE_LABEL[value] || String(value);
}

function formatKeyUnit(value) {
  if (value === "1" || value === 1 || value === true) return "\u662f";
  if (value === "0" || value === 0 || value === false) return "\u5426";
  return textOrDash(value);
}

Page({
  data: {
    companyId: null,
    loading: false,
    detail: {
      companyName: "-",
      isKeyUnitText: "-",
      contactPerson: "-",
      contactPhone: "",
      address: "-",
      buildingType: "-",
      totalLandAreaText: "-",
      totalBuildingAreaText: "-",
      buildingFloorCountText: "-",
      buildingHeightText: "-"
    }
  },

  onLoad(options) {
    const companyId = options.companyId || options.id;
    if (!companyId) {
      wx.showToast({ title: "\u5ba2\u6237\u4e0d\u5b58\u5728", icon: "none" });
      return;
    }
    this.setData({ companyId });
    this.loadDetail(companyId);
  },

  async loadDetail(companyId) {
    this.setData({ loading: true });
    try {
      const res = await api.getCompanyDetail(companyId);
      const item = (res && res.data) || {};
      this.setData({
        detail: {
          companyName: textOrDash(item.companyName),
          isKeyUnitText: formatKeyUnit(item.isKeyUnit),
          contactPerson: textOrDash(item.contactPerson),
          contactPhone: item.contactPhone || "",
          address: textOrDash(item.address),
          buildingType: formatBuildingType(item.buildingType),
          totalLandAreaText: formatArea(item.totalLandArea),
          totalBuildingAreaText: formatArea(item.totalBuildingArea),
          buildingFloorCountText: textOrDash(item.buildingFloorCount),
          buildingHeightText: formatHeight(item.buildingHeight)
        }
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  onCallPhone() {
    const phone = this.data.detail.contactPhone;
    if (!phone) return;
    wx.makePhoneCall({ phoneNumber: String(phone) });
  }
});
