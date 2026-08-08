const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");

function todayStr() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return d.getFullYear() + "-" + m + "-" + day;
}

function normalizeSystems(raw) {
  const list = Array.isArray(raw) ? raw : [];
  return list
    .map((item, index) => {
      if (typeof item === "string") {
        return { label: item, value: item, key: "s-" + index };
      }
      const label =
        item.categoryName ||
        item.typeName ||
        item.systemName ||
        item.label ||
        item.dictLabel ||
        item.name ||
        "";
      const value = item.categoryKey || item.typeId || item.value || label || ("s-" + index);
      return label ? { label: label, value: String(value), key: String(value) } : null;
    })
    .filter(Boolean);
}

function normalizeEquipments(raw) {
  const list = Array.isArray(raw) ? raw : [];
  return list
    .map((item, index) => {
      if (typeof item === "string") {
        return { label: item, value: item, key: "e-" + index };
      }
      const label = item.equipmentName || item.typeName || item.label || item.name || "";
      const value = item.equipmentKey || item.typeId || item.value || label || ("e-" + index);
      return label ? { label: label, value: String(value), key: String(value) } : null;
    })
    .filter(Boolean);
}

Page({
  data: {
    repairId: null,
    companyId: null,
    companyName: "",
    taskId: null,
    linked: false,
    systemOptions: [],
    categoryKey: "",
    systemTypeId: null,
    systemTypeName: "",
    equipmentOptions: [],
    equipmentKey: "",
    equipmentId: null,
    equipmentName: "",
    foundTime: "",
    isReported: "1",
    urgencyLevel: "0",
    faultDescription: "",
    customerAddress: "",
    images: [],
    saving: false,
    pickerVisible: false,
    pickerType: "",
    pickerTitle: "",
    pickerOptions: [],
    pickerIndex: [0]
  },

  noop() {},

  onLoad(options) {
    const opts = options || {};
    if (opts.id) {
      this.setData({ repairId: opts.id });
      wx.setNavigationBarTitle({ title: "编辑报修" });
      this.loadDetail(opts.id);
      return;
    }
    this.setData({ foundTime: todayStr() });
    if (opts.linked === "1") {
      const decode = (value) => (value ? decodeURIComponent(value) : "");
      this.setData({
        linked: true,
        taskId: opts.taskId || null,
        companyId: opts.companyId || null,
        companyName: decode(opts.companyName),
        categoryKey: decode(opts.categoryKey),
        systemTypeName: decode(opts.systemTypeName),
        equipmentKey: decode(opts.equipmentKey),
        equipmentName: decode(opts.equipmentName),
        customerAddress: decode(opts.customerAddress),
        isReported: "1",
        faultDescription: decode(opts.faultDescription)
      });
      this.loadSystems().then(async () => {
        this.syncSystemFromName();
        if (this.data.categoryKey) {
          await this.loadEquipments(this.data.categoryKey);
          this.syncEquipmentFromName();
        }
      });
    } else {
      this.initCompany();
    }
  },

  async initCompany() {
    try {
      const res = await api.getCurrentCompany();
      if (res && res.data) {
        this.setData({
          companyId: res.data.companyId,
          companyName: res.data.companyName || "当前公司",
          customerAddress: res.data.address || res.data.companyAddress || ""
        });
        await this.loadSystems();
      }
    } catch (e) {
      this.setData({ companyName: "未选择公司" });
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getRepairDetail(id);
      const detail = res.data || {};
      const images = detail.faultImages
        ? String(detail.faultImages)
            .split(",")
            .filter(Boolean)
            .map((url) => ({ serverUrl: url, tempPath: "", uploading: false }))
        : [];
      this.setData({
        companyId: detail.companyId,
        companyName: detail.companyName || "",
        taskId: detail.taskId || null,
        systemTypeId: detail.systemTypeId || null,
        systemTypeName: detail.systemTypeName || "",
        categoryKey: "",
        equipmentId: detail.equipmentId || null,
        equipmentName: detail.equipmentName || "",
        equipmentKey: "",
        foundTime: detail.foundTime ? String(detail.foundTime).slice(0, 10) : todayStr(),
        isReported: detail.isReported === "0" ? "0" : "1",
        urgencyLevel: String(detail.urgencyLevel == null ? "0" : detail.urgencyLevel),
        faultDescription: detail.faultDescription || "",
        customerAddress: detail.customerAddress || "",
        images: images
      });
      await this.loadSystems();
      this.syncSystemFromName();
      if (this.data.categoryKey) {
        await this.loadEquipments(this.data.categoryKey);
        this.syncEquipmentFromName();
      }
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  async loadSystems() {
    try {
      // 与巡查测试一致：消防维护模板一级类目
      const res = await api.getInspectionTemplateCategories();
      this.setData({ systemOptions: normalizeSystems(res.data || res.rows || []) });
    } catch (e) {
      try {
        const fallback = await api.getDictSystemTypes();
        this.setData({ systemOptions: normalizeSystems(fallback.data || fallback.rows || []) });
      } catch (err) {
        this.setData({ systemOptions: [] });
      }
    }
  },

  syncSystemFromName() {
    const name = this.data.systemTypeName;
    const key = this.data.categoryKey;
    if (!name && !key) return;
    const hit = this.data.systemOptions.find(
      (item) => item.value === key || item.label === name || item.value === name
    );
    if (hit) {
      this.setData({
        categoryKey: hit.value,
        systemTypeName: hit.label
      });
    }
  },

  async loadEquipments(categoryKey) {
    const key = categoryKey || this.data.categoryKey;
    if (!key) {
      this.setData({ equipmentOptions: [] });
      return;
    }
    try {
      // 与巡查测试一致：该系统下的模板二级设备类目
      const res = await api.getInspectionTemplateEquipments(key);
      this.setData({ equipmentOptions: normalizeEquipments(res.data || res.rows || []) });
    } catch (e) {
      this.setData({ equipmentOptions: [] });
    }
  },

  syncEquipmentFromName() {
    const name = this.data.equipmentName;
    const key = this.data.equipmentKey;
    if (!name && !key) return;
    const hit = this.data.equipmentOptions.find(
      (item) => item.value === key || item.label === name
    );
    if (hit) {
      this.setData({
        equipmentKey: hit.value,
        equipmentName: hit.label
      });
    }
  },

  openSystemPicker() {
    const options = this.data.systemOptions;
    if (!options.length) {
      return wx.showToast({ title: "暂无系统可选", icon: "none" });
    }
    let idx = options.findIndex(
      (item) => item.value === this.data.categoryKey || item.label === this.data.systemTypeName
    );
    if (idx < 0) idx = 0;
    this.setData({
      pickerVisible: true,
      pickerType: "system",
      pickerTitle: "选择系统名称",
      pickerOptions: options,
      pickerIndex: [idx]
    });
  },

  openEquipmentPicker() {
    if (!this.data.categoryKey && !this.data.systemTypeName) {
      return wx.showToast({ title: "请先选择系统名称", icon: "none" });
    }
    const options = this.data.equipmentOptions;
    if (!options.length) {
      return wx.showToast({ title: "该系统下暂无设备类目，可直接输入", icon: "none" });
    }
    let idx = options.findIndex(
      (item) => item.value === this.data.equipmentKey || item.label === this.data.equipmentName
    );
    if (idx < 0) idx = 0;
    this.setData({
      pickerVisible: true,
      pickerType: "equipment",
      pickerTitle: "选择设备名称",
      pickerOptions: options,
      pickerIndex: [idx]
    });
  },

  closePicker() {
    this.setData({ pickerVisible: false, pickerType: "", pickerOptions: [] });
  },

  onPickerChange(e) {
    const val = e.detail.value || [0];
    this.setData({ pickerIndex: val });
  },

  async confirmPicker() {
    const idx = (this.data.pickerIndex && this.data.pickerIndex[0]) || 0;
    const item = this.data.pickerOptions[idx];
    if (!item) {
      this.closePicker();
      return;
    }
    if (this.data.pickerType === "system") {
      const changed = item.value !== this.data.categoryKey;
      this.setData({
        categoryKey: item.value,
        systemTypeName: item.label,
        systemTypeId: null,
        pickerVisible: false,
        pickerType: "",
        pickerOptions: []
      });
      if (changed) {
        this.setData({
          equipmentId: null,
          equipmentKey: "",
          equipmentName: "",
          equipmentOptions: []
        });
        await this.loadEquipments(item.value);
      }
      return;
    }
    if (this.data.pickerType === "equipment") {
      this.setData({
        equipmentKey: item.value,
        equipmentName: item.label,
        equipmentId: null,
        pickerVisible: false,
        pickerType: "",
        pickerOptions: []
      });
    }
  },

  onEquipmentInput(e) {
    const name = e.detail.value || "";
    const hit = this.data.equipmentOptions.find((item) => item.label === name);
    this.setData({
      equipmentName: name,
      equipmentKey: hit ? hit.value : "",
      equipmentId: null
    });
  },

  onFoundTimeChange(e) {
    this.setData({ foundTime: e.detail.value });
  },

  clearFoundTime() {
    this.setData({ foundTime: "" });
  },

  onReportedChange(e) {
    this.setData({ isReported: e.detail.value ? "1" : "0" });
  },

  onUrgencyTap(e) {
    this.setData({ urgencyLevel: e.currentTarget.dataset.value });
  },

  onDescInput(e) {
    this.setData({ faultDescription: e.detail.value || "" });
  },

  chooseImage() {
    const remain = 4 - this.data.images.length;
    if (remain <= 0) return;
    wx.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => {
        (res.tempFilePaths || []).forEach((path) => this.uploadImg(path));
      }
    });
  },

  async uploadImg(tempPath) {
    const images = this.data.images.concat([{ tempPath: tempPath, serverUrl: "", uploading: true }]);
    const index = images.length - 1;
    this.setData({ images: images });
    try {
      const body = await uploadFile(tempPath);
      const url = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!url) {
        this.removeImage(index);
        wx.showToast({ title: "上传失败", icon: "none" });
        return;
      }
      this.setData({
        ["images[" + index + "].serverUrl"]: url,
        ["images[" + index + "].uploading"]: false
      });
    } catch (e) {
      this.removeImage(index);
    }
  },

  removeImage(e) {
    const index = typeof e === "number" ? e : e.currentTarget.dataset.index;
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images: images });
  },

  previewImage(e) {
    const urls = this.data.images.map((i) => i.tempPath || i.serverUrl).filter(Boolean);
    wx.previewImage({ urls: urls, current: urls[e.currentTarget.dataset.index] });
  },

  async submit() {
    if (this.data.saving) return;
    if (!this.data.companyId) {
      return wx.showToast({ title: "请先选择公司", icon: "none" });
    }
    if (!this.data.systemTypeName || !this.data.categoryKey) {
      return wx.showToast({ title: "请选择系统名称", icon: "none" });
    }
    if (!(this.data.equipmentName || "").trim()) {
      return wx.showToast({ title: "请输入或选择设备名称", icon: "none" });
    }
    const faultDescription = (this.data.faultDescription || "").trim();
    if (!faultDescription) {
      return wx.showToast({ title: "请填写故障说明", icon: "none" });
    }
    if (this.data.images.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中", icon: "none" });
    }

    const payload = {
      companyId: this.data.companyId,
      companyName: this.data.companyName,
      taskId: this.data.taskId || null,
      systemTypeId: this.data.systemTypeId || null,
      systemTypeName: this.data.systemTypeName || "",
      equipmentId: this.data.equipmentId || null,
      equipmentName: (this.data.equipmentName || "").trim(),
      customerAddress: this.data.customerAddress || "",
      foundTime: this.data.foundTime ? this.data.foundTime + " 00:00:00" : null,
      isReported: this.data.isReported === "1" ? "1" : "0",
      urgencyLevel: this.data.urgencyLevel,
      faultDescription: faultDescription,
      faultImages: this.data.images.map((i) => i.serverUrl).filter(Boolean).join(",")
    };

    this.setData({ saving: true });
    try {
      if (this.data.repairId) {
        payload.repairId = this.data.repairId;
        await api.editRepair(payload);
      } else {
        await api.addRepair(payload);
      }
      wx.showToast({ title: "提交成功", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      // request layer toast
    } finally {
      this.setData({ saving: false });
    }
  }
});
