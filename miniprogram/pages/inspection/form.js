const api = require("../../api/index.js").api;
const request = require("../../utils/request.js");

function pad(n) {
  return String(n).padStart(2, "0");
}

function daysInMonth(year, month) {
  return new Date(year, month, 0).getDate();
}

function buildFloors() {
  const floors = [];
  for (let i = -5; i <= -1; i++) floors.push({ label: `${i}F`, value: `${i}F` });
  for (let i = 1; i <= 99; i++) floors.push({ label: `${i}F`, value: `${i}F` });
  return floors;
}

function buildYears() {
  const now = new Date().getFullYear();
  const years = [];
  for (let y = now - 10; y <= now + 1; y++) years.push(y);
  return years;
}

function buildMonths() {
  const months = [];
  for (let m = 1; m <= 12; m++) months.push(m);
  return months;
}

function buildDays(year, month) {
  const days = [];
  const total = daysInMonth(year, month);
  for (let d = 1; d <= total; d++) days.push(d);
  return days;
}

function nowDateParts() {
  const d = new Date();
  return {
    year: d.getFullYear(),
    month: d.getMonth() + 1,
    day: d.getDate(),
    timePart: `${pad(d.getHours())}:${pad(d.getMinutes())}`
  };
}

Page({
  data: {
    linked: false,
    customerAddress: "",
    form: {
      taskId: null,
      companyId: null,
      companyName: "",
      inspectionType: "1",
      buildingId: null,
      buildingName: "",
      floor: "",
      categoryKey: "",
      systemType: "",
      systemName: "",
      equipmentKey: "",
      equipmentName: "",
      equipmentCount: 1,
      location: "",
      inspectionTime: "",
      equipmentStatus: "0",
      remark: ""
    },
    typeOptions: [
      { label: "测试", value: "0" },
      { label: "巡查", value: "1" },
      { label: "保养", value: "2" }
    ],
    datePart: "",
    timePart: "",
    images: [],
    saving: false,
    buildingOptions: [],
    systemOptions: [],
    equipmentOptions: [],
    floorOptions: buildFloors(),
    pickerVisible: false,
    pickerTitle: "",
    pickerType: "",
    pickerOptions: [],
    pickerIndex: [0],
    floorSheetVisible: false,
    dateSheetVisible: false,
    years: buildYears(),
    months: buildMonths(),
    days: [],
    datePickerValue: [0, 0, 0],
    tempYear: 0,
    tempMonth: 0,
    tempDay: 0
  },

  onLoad(options) {
    const parts = nowDateParts();
    const datePart = `${parts.year}-${pad(parts.month)}-${pad(parts.day)}`;
    this.setData({
      datePart,
      timePart: parts.timePart,
      "form.inspectionTime": `${datePart} ${parts.timePart}:00`,
      days: buildDays(parts.year, parts.month)
    });
    this.initDatePickerTo(parts.year, parts.month, parts.day);
    if (options && options.linked === "1") {
      this.initLinkedContext(options);
    } else {
      this.initCompanyAndOptions();
    }
  },

  async initLinkedContext(options) {
    const decode = (value) => value ? decodeURIComponent(value) : "";
    const companyId = options.companyId || null;
    const categoryKey = decode(options.categoryKey);
    this.setData({
      linked: true,
      customerAddress: decode(options.customerAddress),
      "form.taskId": options.taskId || null,
      "form.companyId": companyId,
      "form.companyName": decode(options.companyName),
      "form.inspectionType": "0",
      "form.categoryKey": categoryKey,
      "form.systemType": categoryKey,
      "form.systemName": decode(options.systemName),
      "form.equipmentKey": decode(options.equipmentKey),
      "form.equipmentName": decode(options.equipmentName),
      "form.equipmentStatus": "0"
    });
    await Promise.all([this.loadBuildings(companyId), this.loadSystemTypes()]);
    await this.loadEquipmentTypes(categoryKey);
  },

  async initCompanyAndOptions() {
    try {
      const companyRes = await api.getCurrentCompany();
      if (companyRes && companyRes.data) {
        this.setData({
          "form.companyId": companyRes.data.companyId,
          "form.companyName": companyRes.data.companyName || "当前公司"
        });
        await this.loadBuildings(companyRes.data.companyId);
      }
      await this.loadSystemTypes();
    } catch (e) {
      this.setData({ "form.companyName": "未选择公司" });
    }
  },

  async loadBuildings(companyId) {
    if (!companyId) return;
    try {
      const res = await api.getBuildingsByCompany(companyId);
      const rows = res.data || res.rows || [];
      this.setData({
        buildingOptions: rows.map((b) => ({
          label: b.buildingName,
          value: b.buildingId,
          raw: b
        }))
      });
    } catch (e) {
      // ignore
    }
  },

  async loadSystemTypes() {
    const res = await api.getInspectionTemplateCategories();
    const rows = res.data || [];
    this.setData({
      systemOptions: rows.map((t) => ({
        label: t.categoryName || t.typeName || t.label,
        value: t.categoryKey || t.typeId || t.value,
        raw: t
      }))
    });
  },

  async loadEquipmentTypes(categoryKey) {
    if (!categoryKey) {
      this.setData({ equipmentOptions: [] });
      return;
    }
    const res = await api.getInspectionTemplateEquipments(categoryKey);
    const rows = res.data || [];
    this.setData({
      equipmentOptions: rows.map((t) => ({
        label: t.equipmentName || t.typeName || t.label,
        value: t.equipmentKey || t.typeId || t.value,
        raw: t
      }))
    });
  },

  onTypeChange(e) {
    this.setData({ "form.inspectionType": e.detail.value });
  },

  onStatusChange(e) {
    const status = e.detail.value ? "0" : "1";
    this.setData({ "form.equipmentStatus": status });
    if (this.data.linked && status === "1") {
      wx.showModal({
        title: "提示",
        content: "是否上报故障？",
        cancelText: "否",
        confirmText: "是",
        success: (res) => {
          if (res.confirm) this.openLinkedRepair();
        }
      });
    }
  },

  openLinkedRepair() {
    const form = this.data.form;
    const params = {
      linked: "1",
      taskId: form.taskId || "",
      companyId: form.companyId || "",
      companyName: form.companyName || "",
      systemTypeName: form.systemName || "",
      equipmentName: form.equipmentName || "",
      customerAddress: this.data.customerAddress || "",
      isReported: "1",
      faultDescription: (form.equipmentName || "设备") + "巡查测试异常"
    };
    const query = Object.keys(params).map((key) => key + "=" + encodeURIComponent(params[key])).join("&");
    wx.navigateTo({ url: "/pages/repair/form?" + query });
  },

  onLocationInput(e) {
    this.setData({ "form.location": e.detail.value });
  },

  onRemarkInput(e) {
    this.setData({ "form.remark": e.detail.value });
  },

  syncInspectionTime(datePart, timePart) {
    this.setData({
      datePart,
      timePart,
      "form.inspectionTime": `${datePart} ${timePart}:00`
    });
  },

  initDatePickerTo(year, month, day) {
    const years = this.data.years.length ? this.data.years : buildYears();
    const months = this.data.months.length ? this.data.months : buildMonths();
    const days = buildDays(year, month);
    let yi = years.indexOf(year);
    if (yi < 0) yi = years.length - 2;
    let mi = months.indexOf(month);
    if (mi < 0) mi = 0;
    let di = days.indexOf(day);
    if (di < 0) di = days.length - 1;
    this.setData({
      years,
      months,
      days,
      tempYear: years[yi],
      tempMonth: months[mi],
      tempDay: days[di],
      datePickerValue: [yi, mi, di]
    });
  },

  openDateSheet() {
    const parts = (this.data.datePart || "").split("-");
    let year = parseInt(parts[0], 10);
    let month = parseInt(parts[1], 10);
    let day = parseInt(parts[2], 10);
    const now = nowDateParts();
    if (!year) year = now.year;
    if (!month) month = now.month;
    if (!day) day = now.day;
    this.initDatePickerTo(year, month, day);
    this.setData({ dateSheetVisible: true });
  },

  closeDateSheet() {
    this.setData({ dateSheetVisible: false });
  },

  onDatePickerChange(e) {
    const val = e.detail.value || [0, 0, 0];
    const year = this.data.years[val[0]] || this.data.years[0];
    const month = this.data.months[val[1]] || 1;
    let days = this.data.days;
    if (year !== this.data.tempYear || month !== this.data.tempMonth) {
      days = buildDays(year, month);
    }
    let dayIndex = val[2];
    if (dayIndex >= days.length) dayIndex = days.length - 1;
    const day = days[dayIndex];
    this.setData({
      days,
      tempYear: year,
      tempMonth: month,
      tempDay: day,
      datePickerValue: [val[0], val[1], dayIndex]
    });
  },

  confirmDateSheet() {
    const year = this.data.tempYear;
    const month = this.data.tempMonth;
    const day = this.data.tempDay;
    const datePart = `${year}-${pad(month)}-${pad(day)}`;
    this.syncInspectionTime(datePart, this.data.timePart || "00:00");
    this.closeDateSheet();
  },

  decCount() {
    const n = Math.max(1, (this.data.form.equipmentCount || 1) - 1);
    this.setData({ "form.equipmentCount": n });
  },

  incCount() {
    const n = Math.min(999, (this.data.form.equipmentCount || 1) + 1);
    this.setData({ "form.equipmentCount": n });
  },

  openBuildingPicker() {
    this.openPicker("building", "选择建筑", this.data.buildingOptions);
  },

  openFloorSheet() {
    this.setData({ floorSheetVisible: true });
  },

  closeFloorSheet() {
    this.setData({ floorSheetVisible: false });
  },

  selectFloor(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ "form.floor": value, floorSheetVisible: false });
  },

  openSystemPicker() {
    if (this.data.linked) return wx.showToast({ title: "系统名称已关联锁定", icon: "none" });
    this.openPicker("system", "选择系统名称", this.data.systemOptions);
  },

  openEquipmentPicker() {
    if (this.data.linked) return wx.showToast({ title: "设备名称已关联锁定", icon: "none" });
    if (!this.data.form.categoryKey) {
      wx.showToast({ title: "请先选择系统名称", icon: "none" });
      return;
    }
    this.openPicker("equipment", "选择设备名称", this.data.equipmentOptions);
  },

  openPicker(type, title, options) {
    if (!options || options.length === 0) {
      wx.showToast({ title: "暂无可选项", icon: "none" });
      return;
    }
    let index = 0;
    if (type === "building" && this.data.form.buildingId) {
      const i = options.findIndex((o) => o.value === this.data.form.buildingId);
      if (i >= 0) index = i;
    } else if (type === "system" && this.data.form.categoryKey) {
      const i = options.findIndex((o) => o.value === this.data.form.categoryKey);
      if (i >= 0) index = i;
    } else if (type === "equipment" && this.data.form.equipmentKey) {
      const i = options.findIndex((o) => o.value === this.data.form.equipmentKey);
      if (i >= 0) index = i;
    }
    this.setData({
      pickerVisible: true,
      pickerType: type,
      pickerTitle: title,
      pickerOptions: options,
      pickerIndex: [index]
    });
  },

  closePicker() {
    this.setData({ pickerVisible: false });
  },

  onPickerChange(e) {
    this.setData({ pickerIndex: e.detail.value });
  },

  async confirmPicker() {
    const idx = (this.data.pickerIndex && this.data.pickerIndex[0]) || 0;
    const selected = this.data.pickerOptions[idx];
    if (!selected) {
      this.closePicker();
      return;
    }
    const type = this.data.pickerType;
    if (type === "building") {
      this.setData({
        "form.buildingId": selected.value,
        "form.buildingName": selected.label
      });
    } else if (type === "system") {
      this.setData({
        "form.categoryKey": selected.value,
        "form.systemType": selected.value,
        "form.systemName": selected.label,
        "form.equipmentKey": "",
        "form.equipmentName": ""
      });
      await this.loadEquipmentTypes(selected.value);
    } else if (type === "equipment") {
      this.setData({
        "form.equipmentKey": selected.value,
        "form.equipmentName": selected.label
      });
    }
    this.closePicker();
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

  uploadImg(tempPath) {
    const images = this.data.images.concat([{ tempPath, serverUrl: "", uploading: true }]);
    const index = images.length - 1;
    this.setData({ images });
    const token = wx.getStorageSync("token");
    wx.uploadFile({
      url: request.BASE_URL + "/api/common/upload",
      filePath: tempPath,
      name: "file",
      header: { Authorization: token ? `Bearer ${token}` : "" },
      success: (uploadRes) => {
        try {
          const body = JSON.parse(uploadRes.data);
          const url = body.url || body.fileName || (body.data && body.data.url);
          if (url) {
            const key = `images[${index}].serverUrl`;
            const key2 = `images[${index}].uploading`;
            this.setData({ [key]: url, [key2]: false });
          } else {
            this.removeImageByIndex(index);
            wx.showToast({ title: body.msg || "上传失败", icon: "none" });
          }
        } catch (e) {
          this.removeImageByIndex(index);
          wx.showToast({ title: "上传失败", icon: "none" });
        }
      },
      fail: () => {
        this.removeImageByIndex(index);
        wx.showToast({ title: "上传失败", icon: "none" });
      }
    });
  },

  removeImageByIndex(index) {
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images });
  },

  removeImage(e) {
    this.removeImageByIndex(e.currentTarget.dataset.index);
  },

  previewImage(e) {
    const urls = this.data.images.map((i) => i.tempPath || i.serverUrl);
    wx.previewImage({ urls, current: urls[e.currentTarget.dataset.index] });
  },

  async handleSave() {
    if (this.data.saving) return;
    const form = this.data.form;
    if (!form.companyId) return wx.showToast({ title: "请先选择公司", icon: "none" });
    if (!form.buildingId) return wx.showToast({ title: "请选择建筑名称", icon: "none" });
    if (!form.floor) return wx.showToast({ title: "请选择所在楼层", icon: "none" });
    if (!form.categoryKey) return wx.showToast({ title: "请选择系统名称", icon: "none" });
    if (!form.equipmentKey) return wx.showToast({ title: "请选择设备名称", icon: "none" });
    if (!form.location || !form.location.trim()) return wx.showToast({ title: "请输入具体位置", icon: "none" });
    if (this.data.images.some((img) => img.uploading)) {
      return wx.showToast({ title: "图片上传中，请稍候", icon: "none" });
    }

    this.setData({ saving: true });
    try {
      const payload = {
        taskId: form.taskId || null,
        companyId: form.companyId,
        inspectionType: form.inspectionType,
        buildingId: form.buildingId,
        floor: form.floor,
        categoryKey: form.categoryKey,
        systemType: form.systemType || form.categoryKey,
        systemName: form.systemName,
        equipmentKey: form.equipmentKey,
        equipmentName: form.equipmentName,
        equipmentCount: form.equipmentCount,
        location: form.location.trim(),
        inspectionTime: form.inspectionTime,
        equipmentStatus: form.equipmentStatus,
        remark: form.remark,
        imageUrls: this.data.images.map((img) => img.serverUrl).filter(Boolean).join(",")
      };
      const res = await api.addInspection(payload);
      if (res.code === 200 || res.code === 0) {
        wx.showToast({ title: "保存成功", icon: "success" });
        setTimeout(() => wx.navigateBack(), 1200);
      } else {
        wx.showToast({ title: res.msg || "保存失败", icon: "none" });
      }
    } catch (e) {
      wx.showToast({ title: "保存失败", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  },

  noop() {}
});
