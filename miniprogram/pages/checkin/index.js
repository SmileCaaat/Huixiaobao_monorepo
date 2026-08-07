const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");

const ADDR_LOCATING = "正在获取位置...";
const ADDR_RESOLVING = "正在解析地址...";
const ADDR_CURRENT = "当前位置";
const ADDR_FAIL_LOCATE = "获取位置失败";
const TEXT_REFRESH = "刷新";
const TEXT_LOCATING = "定位中...";
const TEXT_SIGN_IN = "签到";
const TEXT_SIGN_OUT = "签退";
const TEXT_CONFIRM_IN = "签到确认";
const TEXT_CONFIRM_OUT = "签退确认";
const TEXT_PICK_TASK = "请选择关联任务";
const TEXT_NORMAL = "正常打卡";
const TEXT_DO_IN = "立即签到";
const TEXT_DO_OUT = "立即签退";

Page({
  data: {
    companyId: null,
    companyName: "",
    latitude: null,
    longitude: null,
    mapLatitude: 23.129112,
    mapLongitude: 113.264385,
    placeTitle: ADDR_LOCATING,
    coordText: "-",
    markers: [],
    addressText: "",
    addressMode: "auto",
    addressReady: false,
    locating: false,
    refreshClass: "refresh-btn",
    refreshText: TEXT_REFRESH,
    checkInType: "0",
    confirmTitle: TEXT_CONFIRM_IN,
    submitText: TEXT_DO_IN,
    submitClass: "confirm-btn",
    showConfirm: false,
    taskList: [],
    taskIndex: -1,
    taskPickerValue: 0,
    taskPickerText: TEXT_PICK_TASK,
    selectedTaskId: null,
    selectedTaskName: "",
    images: [],
    canAddImage: true,
    remark: TEXT_NORMAL,
    submitting: false,
    historyList: [],
    historyLoading: false
  },

  onLoad(options) {
    if (options && options.taskId) this.setData({ selectedTaskId: options.taskId });
  },

  onShow() {
    if (this._pageInitialized) {
      this.loadHistory();
      return;
    }
    this._pageInitialized = true;
    this.initPage();
  },

  noop() {},

  syncUiState(patch) {
    const p = Object.assign({}, patch || {});
    const next = Object.assign({}, this.data, p);
    const locating = p.locating != null ? !!p.locating : !!next.locating;
    const checkInType = (p.checkInType != null ? p.checkInType : next.checkInType) === '1' ? '1' : '0';
    const selectedTaskName = p.selectedTaskName != null ? p.selectedTaskName : (next.selectedTaskName || '');
    const taskIndex = p.taskIndex != null ? p.taskIndex : (next.taskIndex == null ? -1 : next.taskIndex);
    const images = p.images != null ? p.images : (next.images || []);
    const submitting = p.submitting != null ? !!p.submitting : !!next.submitting;
    const lat = p.latitude !== undefined ? p.latitude : next.latitude;
    const lng = p.longitude !== undefined ? p.longitude : next.longitude;
    p.locating = locating;
    p.checkInType = checkInType;
    p.refreshClass = locating ? "refresh-btn disabled" : "refresh-btn";
    p.refreshText = locating ? TEXT_LOCATING : TEXT_REFRESH;
    p.confirmTitle = checkInType === '1' ? TEXT_CONFIRM_OUT : TEXT_CONFIRM_IN;
    p.submitText = checkInType === '1' ? TEXT_DO_OUT : TEXT_DO_IN;
    p.submitClass = submitting ? "confirm-btn disabled" : "confirm-btn";
    p.taskPickerValue = taskIndex >= 0 ? taskIndex : 0;
    p.taskPickerText = selectedTaskName || TEXT_PICK_TASK;
    p.canAddImage = images.length < 3;
    if (lat != null) p.mapLatitude = lat;
    if (lng != null) p.mapLongitude = lng;
    this.setData(p);
  },

  async initPage() {
    await this.loadCompany();
    this.getLocation();
    if (this.data.companyId) await this.fetchTasks();
    if (this.data.selectedTaskId && this.data.taskList.length) this.syncTaskSelection(this.data.selectedTaskId);
    this.loadHistory();
  },

  async loadCompany() {
    try {
      const res = await api.getCurrentCompany({}, { loading: false, showError: false });
      if (res && res.data) {
        this.syncUiState({ companyId: res.data.companyId, companyName: res.data.companyName || '' });
        return;
      }
    } catch (e) {}
    this.syncUiState({ companyId: null, companyName: '' });
  },

  buildMarkers(lat, lng) {
    if (lat == null || lng == null) return [];
    return [{ id: 1, latitude: lat, longitude: lng, width: 28, height: 28 }];
  },

  formatCoord(lat, lng) {
    if (lat == null || lng == null) return "-";
    return Number(lat).toFixed(6) + ", " + Number(lng).toFixed(6);
  },

  shortPlaceName(address) {
    const value = String(address || "").trim();
    if (!value) return ADDR_CURRENT;
    return value.length > 24 ? value.slice(0, 24) : value;
  },

  getLocation() {
    if (this.data.locating) return;
    this.syncUiState({
      locating: true,
      addressMode: "auto",
      addressReady: false,
      placeTitle: ADDR_LOCATING,
      coordText: "-"
    });
    wx.getLocation({
      type: "gcj02",
      isHighAccuracy: true,
      success: (res) => {
        const lat = Number(Number(res.latitude).toFixed(6));
        const lng = Number(Number(res.longitude).toFixed(6));
        this.syncUiState({
          latitude: lat,
          longitude: lng,
          coordText: this.formatCoord(lat, lng),
          markers: this.buildMarkers(lat, lng),
          placeTitle: ADDR_RESOLVING,
          locating: true
        });
        this.resolveAddress(lng, lat);
      },
      fail: () => {
        this.syncUiState({
          locating: false,
          addressReady: false,
          placeTitle: ADDR_FAIL_LOCATE,
          coordText: "-",
          latitude: null,
          longitude: null,
          markers: []
        });
        wx.showToast({ title: ADDR_FAIL_LOCATE, icon: "none" });
      }
    });
  },

  async resolveAddress(longitude, latitude) {
    try {
      const res = await api.reverseGeocode({ longitude: longitude, latitude: latitude }, { loading: false, showError: false });
      const address = res && res.data && res.data.address;
      if (!address || this.isCoordText(address) || !this.isChineseAddress(address)) {
        this.applySoftGeoFallback();
        return;
      }
      const lat = res.data.latitude != null ? res.data.latitude : latitude;
      const lng = res.data.longitude != null ? res.data.longitude : longitude;
      this.syncUiState({
        locating: false,
        addressMode: "auto",
        addressReady: true,
        addressText: address,
        placeTitle: this.shortPlaceName(address),
        latitude: lat,
        longitude: lng,
        coordText: this.formatCoord(lat, lng),
        markers: this.buildMarkers(lat, lng)
      });
    } catch (e) {
      this.applySoftGeoFallback();
    }
  },

  applySoftGeoFallback() {
    this.syncUiState({
      locating: false,
      addressMode: "auto",
      addressReady: false,
      addressText: "",
      placeTitle: ADDR_CURRENT
    });
  },

  onRefreshTap() {
    if (this.data.locating) return;
    this.getLocation();
  },

  goHistory() {
    const companyId = this.data.companyId || "";
    wx.navigateTo({
      url: "/pages/checkin/history" + (companyId ? "?companyId=" + companyId : "")
    });
  },

  onAddressTap() { this.chooseMapLocation(); },

  chooseMapLocation() {
    if (this.data.locating) return;
    this.syncUiState({ locating: true });
    wx.chooseLocation({
      latitude: this.data.latitude || undefined,
      longitude: this.data.longitude || undefined,
      success: (result) => {
        const baseAddress = String(result.address || "").trim();
        const placeName = String(result.name || "").trim();
        const address = baseAddress && placeName && baseAddress.indexOf(placeName) < 0 ? baseAddress + placeName : (baseAddress || placeName);
        const lat = Number(Number(result.latitude).toFixed(6));
        const lng = Number(Number(result.longitude).toFixed(6));
        if (!this.isChineseAddress(address)) {
          this.syncUiState({ locating: false });
          wx.showToast({ title: "请先选择签到位置", icon: "none" });
          return;
        }
        this.syncUiState({
          latitude: lat,
          longitude: lng,
          coordText: this.formatCoord(lat, lng),
          markers: this.buildMarkers(lat, lng),
          addressText: address,
          placeTitle: placeName || this.shortPlaceName(address),
          addressMode: "map",
          addressReady: true,
          locating: false
        });
      },
      fail: (error) => {
        this.syncUiState({ locating: false });
        const message = String((error && error.errMsg) || "");
        if (message.indexOf("cancel") < 0) wx.showToast({ title: "选择位置失败", icon: "none" });
      }
    });
  },

  isChineseAddress(text) {
    const value = String(text || "").trim();
    return value.length >= 2 && value.length <= 255 && /[\u4e00-\u9fff]/.test(value) && !this.isCoordText(value);
  },

  isCoordText(text) {
    return /^-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?$/.test(String(text || "").trim());
  },

  async fetchTasks() {
    if (!this.data.companyId) return;
    try {
      const res = await api.listTasksByCompany({ companyId: this.data.companyId }, { loading: false, showError: false });
      const taskList = (res && (res.rows || res.data)) || [];
      this.syncUiState({ taskList: taskList });
      if (this.data.selectedTaskId) this.syncTaskSelection(this.data.selectedTaskId);
    } catch (e) {
      this.syncUiState({ taskList: [] });
    }
  },

  syncTaskSelection(taskId) {
    const idx = this.data.taskList.findIndex((t) => String(t.taskId) === String(taskId));
    if (idx >= 0) {
      const task = this.data.taskList[idx];
      this.syncUiState({ taskIndex: idx, selectedTaskId: task.taskId, selectedTaskName: task.taskName || '' });
    } else {
      this.syncUiState({ taskIndex: -1, selectedTaskId: null, selectedTaskName: '' });
    }
  },

  onTaskChange(e) {
    const idx = Number(e.detail.value);
    const task = this.data.taskList[idx];
    if (!task) return;
    this.syncUiState({ taskIndex: idx, selectedTaskId: task.taskId, selectedTaskName: task.taskName || '' });
  },

  openConfirm(e) {
    const type = e.currentTarget.dataset.type;
    if (!this.data.companyId) { wx.showToast({ title: "请先选择公司", icon: "none" }); return; }
    if (this.data.latitude == null || this.data.longitude == null) { wx.showToast({ title: "请先获取定位", icon: "none" }); return; }
    if (!this.data.addressReady) {
      wx.showModal({
        title: "选择位置",
        content: "自动地址解析不可用，请在地图中选择当前位置",
        confirmText: "选择位置",
        success: (r) => { if (r.confirm) this.chooseMapLocation(); }
      });
      return;
    }
    this.syncUiState({
      checkInType: type === '1' ? '1' : '0',
      showConfirm: true,
      images: [],
      remark: TEXT_NORMAL,
      submitting: false
    });
  },

  closeConfirm() { this.syncUiState({ showConfirm: false }); },

  chooseImage() {
    const remain = 3 - this.data.images.length;
    if (remain <= 0) return;
    wx.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => { (res.tempFilePaths || []).forEach((p) => this.uploadImg(p)); }
    });
  },

  async uploadImg(tempPath) {
    const images = this.data.images.concat([{ tempPath: tempPath, serverUrl: "", uploading: true }]);
    const index = images.length - 1;
    this.syncUiState({ images: images });
    try {
      const body = await uploadFile(tempPath);
      const url = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!url) { this.removeImageByIndex(index); return; }
      const next = this.data.images.slice();
      if (!next[index]) return;
      next[index] = Object.assign({}, next[index], { serverUrl: url, uploading: false });
      this.syncUiState({ images: next });
    } catch (e) {
      this.removeImageByIndex(index);
    }
  },

  removeImageByIndex(index) {
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.syncUiState({ images: images });
  },

  removeImage(e) { this.removeImageByIndex(e.currentTarget.dataset.index); },

  previewImage(e) {
    const idx = e.currentTarget.dataset.index;
    const urls = this.data.images.map((i) => i.tempPath || i.serverUrl);
    wx.previewImage({ urls: urls, current: urls[idx] });
  },

  onRemarkInput(e) { this.setData({ remark: e.detail.value || '' }); },

  formatTime(timeStr) {
    if (!timeStr) return "";
    const s = String(timeStr);
    return s.length >= 19 ? s.substring(0, 19).replace("T", " ") : s;
  },

  async loadHistory() {
    if (!this.data.companyId) { this.setData({ historyList: [] }); return; }
    this.setData({ historyLoading: true });
    try {
      const res = await api.getCheckInList({ companyId: this.data.companyId, pageNum: 1, pageSize: 20 }, { loading: false, showError: false });
      const rows = ((res && (res.rows || res.data)) || []).map((item) => {
        const isOut = String(item.checkInType) === '1';
        const lat = item.latitude;
        const lng = item.longitude;
        return Object.assign({}, item, {
          displayTime: this.formatTime(item.checkInTime || item.createTime),
          coordText: lat != null && lng != null ? this.formatCoord(lat, lng) : (item.address || '-'),
          typeLabel: isOut ? TEXT_SIGN_OUT : TEXT_SIGN_IN,
          typeClass: isOut ? "is-out" : "is-in",
          dotClass: isOut ? "is-out" : "is-in"
        });
      });
      this.setData({ historyList: rows });
    } catch (e) {
      this.setData({ historyList: [] });
    } finally {
      this.setData({ historyLoading: false });
    }
  },

  async handleSubmit() {
    if (this.data.submitting) return;
    if (!this.data.companyId) { wx.showToast({ title: "请先选择公司", icon: "none" }); return; }
    if (this.data.latitude == null || this.data.longitude == null) { wx.showToast({ title: "请先获取定位", icon: "none" }); return; }
    if (!this.data.addressReady || !this.isChineseAddress(this.data.addressText)) { wx.showToast({ title: "请先选择签到位置", icon: "none" }); return; }
    if (!this.data.images.length) { wx.showToast({ title: "请上传现场照片", icon: "none" }); return; }
    if (this.data.images.some((img) => img.uploading || !img.serverUrl)) { wx.showToast({ title: "图片上传中", icon: "none" }); return; }

    const typeLabel = this.data.checkInType === '0' ? TEXT_SIGN_IN : TEXT_SIGN_OUT;
    const payload = {
      companyId: this.data.companyId,
      companyName: this.data.companyName,
      taskId: this.data.selectedTaskId || null,
      checkInType: this.data.checkInType,
      address: this.data.addressText,
      locatedAddress: this.data.addressText,
      addressMode: this.data.addressMode === "map" ? "map" : "auto",
      latitude: this.data.latitude,
      longitude: this.data.longitude,
      remark: this.data.remark,
      images: this.data.images.map((img, index) => ({
        imageUrl: img.serverUrl,
        imageName: typeLabel + "图片" + (index + 1),
        sortOrder: index + 1
      }))
    };

    this.syncUiState({ submitting: true });
    wx.showLoading({ title: "提交中...", mask: true });
    try {
      await api.addCheckIn(payload);
      wx.showToast({ title: typeLabel + "成功", icon: "success" });
      this.syncUiState({ showConfirm: false, images: [], remark: TEXT_NORMAL, submitting: false });
      this.loadHistory();
    } catch (e) {
      wx.showToast({ title: (e && e.msg) || "操作失败", icon: "none" });
      this.syncUiState({ submitting: false });
    } finally {
      wx.hideLoading();
    }
  }
});
