const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");
const { BASE_URL } = require("../../services/request.js");

const BUILDING_TYPES = [
  { value: "type1_high_rise_civil", label: "一类高层民用建筑" },
  { value: "type2_high_rise_civil", label: "二类高层民用建筑" },
  { value: "high_rise_factory", label: "高层厂房" },
  { value: "high_rise_warehouse", label: "高层库房" },
  { value: "single_multi_civil", label: "单、多层民用建筑" },
  { value: "single_multi_factory", label: "单、多层厂房" },
  { value: "single_multi_warehouse", label: "单、多层库房" },
  { value: "underground", label: "地下建筑" },
  { value: "tunnel_culvert", label: "隧道、涵洞" },
  { value: "other", label: "其他建筑" }
];

function fullImageUrl(url) {
  if (!url) return "";
  return /^https?:\/\//i.test(url) ? url : BASE_URL + (url.charAt(0) === "/" ? url : "/" + url);
}

function toNumOrNull(value) {
  if (value === null || value === undefined || value === "") return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function emptyForm() {
  return {
    buildingName: "",
    autoFireSystem: "0",
    address: "",
    buildingType: "",
    landArea: "",
    area: "",
    buildingHeight: "",
    floorCount: "",
    aboveGroundFloors: "",
    undergroundFloors: "",
    emergencyExits: "",
    evacuationStairs: "",
    fireElevators: "",
    refugeFloor: ""
  };
}

Page({
  data: {
    buildingId: null,
    companyId: null,
    companyName: "",
    form: emptyForm(),
    typeOptions: BUILDING_TYPES,
    typeLabels: BUILDING_TYPES.map((item) => item.label),
    typeIndex: -1,
    images: [],
    saving: false
  },

  onLoad(options) {
    if (options && options.id) {
      this.setData({ buildingId: options.id });
      wx.setNavigationBarTitle({ title: "建筑信息" });
      this.loadDetail(options.id);
    } else {
      wx.setNavigationBarTitle({ title: "建筑登记" });
    }
    this.initCompany();
  },

  noop() {},

  async initCompany() {
    try {
      const res = await api.getCurrentCompany();
      if (res && res.data) {
        this.setData({
          companyId: res.data.companyId,
          companyName: res.data.companyName || ""
        });
      }
    } catch (e) {
      // ignore
    }
  },

  findTypeIndex(typeCode, typeText) {
    if (typeCode) {
      const byValue = BUILDING_TYPES.findIndex((item) => item.value === typeCode);
      if (byValue >= 0) return byValue;
    }
    if (typeText) {
      const byLabel = BUILDING_TYPES.findIndex((item) => item.label === typeText);
      if (byLabel >= 0) return byLabel;
    }
    return -1;
  },

  async loadDetail(id) {
    try {
      const res = await api.getBuildingDetail(id);
      const detail = res.data || {};
      const typeIndex = this.findTypeIndex(detail.buildingType, detail.buildingTypeText);
      const imageUrl = detail.image || "";
      const images = imageUrl
        ? [{ serverUrl: imageUrl, previewUrl: fullImageUrl(imageUrl), uploading: false }]
        : [];
      this.setData({
        companyId: detail.companyId || this.data.companyId,
        companyName: detail.companyName || this.data.companyName,
        typeIndex: typeIndex,
        images: images,
        form: {
          buildingName: detail.buildingName || "",
          autoFireSystem: detail.autoFireSystem === "1" ? "1" : "0",
          address: detail.address || "",
          buildingType: typeIndex >= 0 ? BUILDING_TYPES[typeIndex].value : detail.buildingType || "",
          landArea: detail.landArea != null ? String(detail.landArea) : "",
          area: detail.area != null ? String(detail.area) : "",
          buildingHeight: detail.buildingHeight != null ? String(detail.buildingHeight) : "",
          floorCount: detail.floorCount != null ? String(detail.floorCount) : "",
          aboveGroundFloors: detail.aboveGroundFloors != null ? String(detail.aboveGroundFloors) : "",
          undergroundFloors: detail.undergroundFloors != null ? String(detail.undergroundFloors) : "",
          emergencyExits: detail.emergencyExits != null ? String(detail.emergencyExits) : "",
          evacuationStairs: detail.evacuationStairs != null ? String(detail.evacuationStairs) : "",
          fireElevators: detail.fireElevators != null ? String(detail.fireElevators) : "",
          refugeFloor: detail.refugeFloor || ""
        }
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ ["form." + field]: e.detail.value });
  },

  onAutoFireTap(e) {
    this.setData({ "form.autoFireSystem": e.currentTarget.dataset.value });
  },

  onTypeChange(e) {
    const idx = Number(e.detail.value);
    const item = BUILDING_TYPES[idx];
    if (!item) return;
    this.setData({
      typeIndex: idx,
      "form.buildingType": item.value
    });
  },

  chooseImage() {
    if (this.data.images.length >= 1) return;
    wx.chooseImage({
      count: 1,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => {
        const path = (res.tempFilePaths || [])[0];
        if (path) this.uploadImg(path);
      }
    });
  },

  async uploadImg(tempPath) {
    const images = [{ tempPath: tempPath, serverUrl: "", previewUrl: tempPath, uploading: true }];
    this.setData({ images: images });
    try {
      const body = await uploadFile(tempPath);
      const serverUrl = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!serverUrl) {
        this.setData({ images: [] });
        wx.showToast({ title: "上传失败", icon: "none" });
        return;
      }
      this.setData({
        images: [{
          tempPath: tempPath,
          serverUrl: serverUrl,
          previewUrl: fullImageUrl(serverUrl),
          uploading: false
        }]
      });
    } catch (e) {
      this.setData({ images: [] });
    }
  },

  removeImage() {
    this.setData({ images: [] });
  },

  previewImage() {
    const urls = this.data.images
      .map((item) => item.previewUrl || item.tempPath || fullImageUrl(item.serverUrl))
      .filter(Boolean);
    if (!urls.length) return;
    wx.previewImage({ urls: urls, current: urls[0] });
  },

  async handleSubmit() {
    if (this.data.saving) return;
    const form = this.data.form;
    if (!this.data.companyId) {
      return wx.showToast({ title: "请先选择公司", icon: "none" });
    }
    if (!form.buildingName || !String(form.buildingName).trim()) {
      return wx.showToast({ title: "请输入建筑名称", icon: "none" });
    }
    if (form.buildingHeight === "" || form.buildingHeight == null) {
      return wx.showToast({ title: "请输入建筑高度", icon: "none" });
    }
    if (form.floorCount === "" || form.floorCount == null) {
      return wx.showToast({ title: "请输入建筑层数", icon: "none" });
    }
    if (form.aboveGroundFloors === "" || form.aboveGroundFloors == null) {
      return wx.showToast({ title: "请输入地上层数", icon: "none" });
    }
    if (form.undergroundFloors === "" || form.undergroundFloors == null) {
      return wx.showToast({ title: "请输入地下层数", icon: "none" });
    }
    if (this.data.images.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中", icon: "none" });
    }

    const image = this.data.images[0] && this.data.images[0].serverUrl ? this.data.images[0].serverUrl : "";
    const payload = {
      companyId: this.data.companyId,
      companyName: this.data.companyName || undefined,
      buildingName: String(form.buildingName).trim(),
      autoFireSystem: form.autoFireSystem === "1" ? "1" : "0",
      address: String(form.address || "").trim(),
      buildingType: form.buildingType || "",
      landArea: toNumOrNull(form.landArea),
      area: toNumOrNull(form.area),
      buildingHeight: toNumOrNull(form.buildingHeight),
      floorCount: toNumOrNull(form.floorCount),
      aboveGroundFloors: toNumOrNull(form.aboveGroundFloors),
      undergroundFloors: toNumOrNull(form.undergroundFloors),
      emergencyExits: toNumOrNull(form.emergencyExits),
      evacuationStairs: toNumOrNull(form.evacuationStairs),
      fireElevators: toNumOrNull(form.fireElevators),
      refugeFloor: String(form.refugeFloor || "").trim(),
      image: image
    };

    this.setData({ saving: true });
    try {
      if (this.data.buildingId) {
        payload.buildingId = this.data.buildingId;
        await api.updateBuilding(payload);
      } else {
        await api.addBuilding(payload);
      }
      wx.showToast({ title: "保存成功", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      wx.showToast({ title: (e && e.msg) || "保存失败", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  }
});
