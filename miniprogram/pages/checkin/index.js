"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  _easycom_uni_nav_bar2();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
if (!Math) {
  _easycom_uni_nav_bar();
}
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const latitude = common_vendor.ref(21.8567);
    const longitude = common_vendor.ref(111.9632);
    const markers = common_vendor.ref([]);
    const currentAddress = common_vendor.ref("");
    const selectedCompanyName = common_vendor.ref("");
    const selectedCompanyId = common_vendor.ref(null);
    const selectedTaskId = common_vendor.ref(null);
    const selectedTaskName = common_vendor.ref("");
    const recentList = common_vendor.ref([]);
    const taskList = common_vendor.ref([]);
    const showDrawer = common_vendor.ref(false);
    const checkType = common_vendor.ref("0");
    const imageList = common_vendor.ref([]);
    const remark = common_vendor.ref("正常打卡");
    common_vendor.watch(selectedCompanyId, (newVal, oldVal) => {
      if (newVal !== oldVal) {
        selectedTaskId.value = null;
        selectedTaskName.value = "";
        taskList.value = [];
      }
      if (newVal) {
        fetchTasks();
      }
    });
    const fetchTasks = async () => {
      if (!selectedCompanyId.value)
        return;
      try {
        const res = await api_index.api.getCheckInTaskList({
          companyId: selectedCompanyId.value
        });
        if (res && (res.code === 200 || res.code === 0)) {
          taskList.value = res.rows || res.data || [];
          if (selectedTaskId.value) {
            const task = taskList.value.find(
              (t) => t.taskId == selectedTaskId.value
            );
            if (task) {
              selectedTaskName.value = task.taskName;
            } else {
              selectedTaskId.value = null;
              selectedTaskName.value = "";
              common_vendor.index.showToast({ title: "预选任务不可用，请重新选择", icon: "none" });
            }
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/index.vue:212", "获取任务列表失败", e);
      }
    };
    const onTaskChange = (e) => {
      const index = e.detail.value;
      const task = taskList.value[index];
      if (task) {
        selectedTaskId.value = task.taskId;
        selectedTaskName.value = task.taskName;
      }
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHistory = () => {
      common_vendor.index.navigateTo({
        url: "/pages/checkin/history"
      });
    };
    const getLocation = () => {
      currentAddress.value = "正在获取位置...";
      common_vendor.index.getLocation({
        type: "gcj02",
        success: (res) => {
          latitude.value = res.latitude;
          longitude.value = res.longitude;
          updateMarkers();
          reverseGeocode(res.latitude, res.longitude);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/checkin/index.vue:269", "获取位置失败", err);
          currentAddress.value = "获取位置失败，请点击刷新重试";
          common_vendor.index.showToast({
            title: "获取位置失败",
            icon: "none"
          });
        }
      });
    };
    const reverseGeocode = (lat, lng) => {
      const key = "M6CBZ-PLKCQ-LWE5M-2EIAU-OX5T6-7HBLQ";
      const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${lat},${lng}&key=${key}&get_poi=0`;
      common_vendor.index.request({
        url,
        method: "GET",
        success: (res) => {
          if (res.statusCode === 200 && res.data && res.data.status === 0) {
            currentAddress.value = res.data.result.address || `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
          } else {
            common_vendor.index.__f__("error", "at pages/checkin/index.vue:292", "逆地理编码失败", res);
            currentAddress.value = `当前位置: ${lat.toFixed(6)}, ${lng.toFixed(6)}`;
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/checkin/index.vue:297", "请求失败", err);
          currentAddress.value = `当前位置: ${lat.toFixed(6)}, ${lng.toFixed(6)}`;
        }
      });
    };
    const updateMarkers = () => {
      markers.value = [
        {
          id: 1,
          latitude: latitude.value,
          longitude: longitude.value,
          iconPath: "/static/tabbar/info.png",
          // 使用存在的图标
          width: 32,
          height: 32
        }
      ];
    };
    const openDrawer = (type) => {
      checkType.value = type;
      showDrawer.value = true;
      imageList.value = [];
      remark.value = "正常打卡";
      fetchTasks();
    };
    const closeDrawer = () => {
      showDrawer.value = false;
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 2 - imageList.value.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          res.tempFilePaths.forEach((path) => {
            uploadImg(path);
          });
        }
      });
    };
    const uploadImg = async (tempPath) => {
      try {
        const imgObj = { tempPath, serverUrl: "", uploading: true };
        imageList.value.push(imgObj);
        const currentIndex = imageList.value.length - 1;
        const token = common_vendor.index.getStorageSync("token");
        common_vendor.index.uploadFile({
          url: utils_request.BASE_URL + "/api/common/upload",
          filePath: tempPath,
          name: "file",
          formData: {},
          header: {
            Authorization: token ? `Bearer ${token}` : ""
          },
          success: (uploadRes) => {
            try {
              common_vendor.index.__f__("log", "at pages/checkin/index.vue:365", "上传响应原始数据:", uploadRes.data);
              const res = JSON.parse(uploadRes.data);
              common_vendor.index.__f__("log", "at pages/checkin/index.vue:367", "上传响应解析后:", res);
              let url = res.fileName || res.data && res.data.fileName;
              if (!url) {
                url = res.url || res.filePath || res.data && (res.data.url || res.data.filePath);
              }
              common_vendor.index.__f__("log", "at pages/checkin/index.vue:380", "提取的URL:", url);
              if (url) {
                imageList.value[currentIndex].serverUrl = url;
                imageList.value[currentIndex].uploading = false;
                common_vendor.index.__f__("log", "at pages/checkin/index.vue:385", "图片上传成功，URL:", url);
              } else {
                common_vendor.index.__f__("error", "at pages/checkin/index.vue:387", "上传返回无效URL:", res);
                imageList.value.splice(currentIndex, 1);
                common_vendor.index.showToast({ title: res.msg || "上传失败", icon: "none" });
              }
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/checkin/index.vue:392", "解析上传响应失败", e, uploadRes.data);
              imageList.value.splice(currentIndex, 1);
              common_vendor.index.showToast({ title: "上传失败", icon: "none" });
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/checkin/index.vue:398", "上传图片失败", err);
            imageList.value.splice(currentIndex, 1);
            common_vendor.index.showToast({ title: "上传失败", icon: "none" });
          }
        });
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/index.vue:404", "上传图片失败", e);
        common_vendor.index.showToast({ title: "上传失败", icon: "none" });
      }
    };
    const removeImage = (index) => {
      imageList.value.splice(index, 1);
    };
    const previewImage = (imgObj) => {
      common_vendor.index.previewImage({
        urls: imageList.value.map((i) => i.tempPath),
        current: imgObj.tempPath
      });
    };
    const handleSubmit = async () => {
      if (!selectedTaskId.value) {
        return common_vendor.index.showToast({ title: "请选择维保任务", icon: "none" });
      }
      if (imageList.value.length === 0) {
        return common_vendor.index.showToast({ title: "请至少上传一张照片", icon: "none" });
      }
      const pendingImages = imageList.value.filter((img) => !img.serverUrl);
      if (pendingImages.length > 0) {
        return common_vendor.index.showToast({ title: "图片上传中，请稍候", icon: "none" });
      }
      try {
        common_vendor.index.showLoading({ title: "提交中...", mask: true });
        common_vendor.index.__f__("log", "at pages/checkin/index.vue:434", imageList.value);
        const payload = {
          companyId: selectedCompanyId.value,
          companyName: selectedCompanyName.value,
          taskId: selectedTaskId.value,
          checkInType: checkType.value,
          address: currentAddress.value,
          latitude: latitude.value,
          longitude: longitude.value,
          remark: remark.value,
          images: imageList.value.map((img, index) => ({
            imageUrl: img.serverUrl,
            imageName: `${checkType.value === "0" ? "签到" : "签退"}图片${index + 1}`,
            sortOrder: index + 1
          }))
        };
        const res = await api_index.api.addCheckIn(payload);
        if (res && (res.code === 200 || res.code === 0)) {
          common_vendor.wx$1.showToast({ title: "签到成功", icon: "success" });
          if (res.data) {
            common_vendor.index.__f__("log", "at pages/checkin/index.vue:457", "签到ID:", res.data.checkInId);
            common_vendor.index.__f__("log", "at pages/checkin/index.vue:458", "是否在范围内:", res.data.isInRange);
          }
          setTimeout(() => {
            closeDrawer();
            loadRecentList();
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res && res.msg || "操作失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/index.vue:468", "提交失败", e);
        common_vendor.index.showToast({ title: "网络请求失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const loadRecentList = async () => {
      try {
        const res = await api_index.api.getCheckInList({
          companyId: selectedCompanyId.value,
          taskId: selectedTaskId.value,
          pageNum: 1,
          pageSize: 5
        });
        if (res && (res.code === 200 || res.code === 0)) {
          recentList.value = res.rows || res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/index.vue:488", "获取最近记录失败", e);
      }
    };
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "";
      if (typeof timeStr !== "string")
        return String(timeStr);
      return timeStr.substring(0, 19).replace("T", " ");
    };
    common_vendor.onMounted(async () => {
      try {
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1];
        const options = currentPage.options || {};
        if (options.taskId) {
          selectedTaskId.value = options.taskId;
          common_vendor.index.showLoading({ title: "加载任务信息..." });
          try {
            const res = await api_index.api.getTaskDetail(options.taskId);
            if ((res.code === 200 || res.code === 0) && res.data) {
              selectedCompanyId.value = res.data.companyId;
              selectedCompanyName.value = res.data.companyName || "关联公司";
              selectedTaskName.value = res.data.taskName || "";
            }
          } catch (err) {
            common_vendor.index.__f__("error", "at pages/checkin/index.vue:517", "获取任务详情失败", err);
          } finally {
            common_vendor.index.hideLoading();
          }
        } else {
          try {
            const res = await api_index.api.getCurrentCompany();
            if ((res.code === 200 || res.code === 0) && res.data) {
              selectedCompanyId.value = res.data.companyId;
              selectedCompanyName.value = res.data.companyName || "当前公司";
            }
          } catch (err) {
            common_vendor.index.__f__("error", "at pages/checkin/index.vue:530", "获取当前公司失败", err);
            selectedCompanyName.value = "未选择公司";
          }
        }
        getLocation();
        loadRecentList();
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/index.vue:538", "onMounted 错误", e);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "维保签到",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: latitude.value,
        d: longitude.value,
        e: markers.value,
        f: selectedCompanyName.value
      }, selectedCompanyName.value ? {
        g: common_vendor.t(selectedCompanyName.value)
      } : {}, {
        h: common_assets._imports_3,
        i: common_vendor.t(currentAddress.value || "正在获取位置..."),
        j: common_vendor.o(getLocation),
        k: common_vendor.o(($event) => openDrawer("0")),
        l: common_vendor.o(($event) => openDrawer("1")),
        m: common_vendor.f(recentList.value, (item, index, i0) => {
          return {
            a: common_vendor.n(item.checkInType === "0" ? "in" : "out"),
            b: common_vendor.t(item.checkInType === "0" ? "签到" : "签退"),
            c: common_vendor.t(formatTime(item.checkInTime || item.createTime)),
            d: common_vendor.t(item.address || item.checkInAddress),
            e: index
          };
        }),
        n: common_vendor.o(goHistory),
        o: showDrawer.value
      }, showDrawer.value ? common_vendor.e({
        p: common_vendor.t(checkType.value === "0" ? "签到确认" : "签退确认"),
        q: common_vendor.o(closeDrawer),
        r: common_vendor.t(selectedTaskName.value || "请选择任务名称（必选）"),
        s: taskList.value,
        t: common_vendor.o(onTaskChange),
        v: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: img.tempPath,
            b: common_vendor.o(($event) => previewImage(img), idx),
            c: common_vendor.o(($event) => removeImage(idx), idx),
            d: idx
          };
        }),
        w: imageList.value.length < 2
      }, imageList.value.length < 2 ? {
        x: common_vendor.o(chooseImage)
      } : {}, {
        y: remark.value,
        z: common_vendor.o(($event) => remark.value = $event.detail.value),
        A: common_vendor.t(checkType.value === "0" ? "立即签到" : "立即签退"),
        B: common_vendor.o(handleSubmit),
        C: common_vendor.o(() => {
        }),
        D: common_vendor.o(closeDrawer)
      }) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a8aeee56"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/checkin/index.js.map
