"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_uni_nav_bar2 + _easycom_uni_icons2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_icons)();
}
const _sfc_main = {
  __name: "form",
  setup(__props) {
    const submitting = common_vendor.ref(false);
    const imageList = common_vendor.ref([]);
    const equipmentCategories = common_vendor.ref([]);
    const systemTypeCategoriesLabels = common_vendor.ref([]);
    const systemTypeCategoriesMap = common_vendor.ref([]);
    const urgencyOptions = [
      { label: "一般", value: "1" },
      { label: "紧急", value: "2" },
      { label: "特急", value: "3" }
    ];
    const formData = common_vendor.ref({
      companyId: null,
      companyName: "",
      systemTypeId: null,
      systemTypeName: "",
      equipmentId: null,
      equipmentName: "",
      customerAddress: "",
      foundTime: "",
      urgencyLevel: "1",
      faultDescription: "",
      faultImages: ""
    });
    const pad = (num) => String(num).padStart(2, "0");
    const formatDateTime = (date) => {
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
        date.getDate()
      )} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(
        date.getSeconds()
      )}`;
    };
    const pickerDateTimeValue = common_vendor.computed(() => {
      if (!formData.value.foundTime)
        return "";
      return formData.value.foundTime.slice(0, 16);
    });
    const onSystemTypeChange = (e) => {
      const index = e.detail.value;
      const label = systemTypeCategoriesLabels.value[index];
      formData.value.systemTypeName = label;
      const match = systemTypeCategoriesMap.value.find(
        (item) => item.label === label
      );
      if (match) {
        formData.value.systemTypeId = match.value;
      }
    };
    const onEquipmentChange = (e) => {
      const index = e.detail.value;
      formData.value.equipmentName = equipmentCategories.value[index];
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const onDateChange = (e) => {
      const value = e.detail.value;
      formData.value.foundTime = `${value}:00`;
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 4 - imageList.value.length,
        success: async (res) => {
          var _a, _b;
          common_vendor.index.showLoading({ title: "上传中..." });
          const tempPaths = res.tempFilePaths;
          for (const path of tempPaths) {
            try {
              const uploadRes = await api_index.api.uploadFile(path, { name: "file" });
              const url = uploadRes.url || ((_a = uploadRes.data) == null ? void 0 : _a.url) || uploadRes.fileName || ((_b = uploadRes.data) == null ? void 0 : _b.fileName);
              if ((uploadRes.code === 200 || uploadRes.code === 0) && url) {
                imageList.value.push(path);
                formData.value.faultImages = formData.value.faultImages ? `${formData.value.faultImages},${url}` : url;
              } else {
                common_vendor.index.showToast({ title: "鍥剧墖涓婁紶澶辫触", icon: "none" });
              }
            } catch (e) {
              common_vendor.index.showToast({ title: "图片上传失败", icon: "none" });
            }
          }
          common_vendor.index.hideLoading();
        }
      });
    };
    const deleteImage = (index) => {
      imageList.value.splice(index, 1);
      const paths = formData.value.faultImages ? formData.value.faultImages.split(",") : [];
      paths.splice(index, 1);
      formData.value.faultImages = paths.join(",");
    };
    const previewImage = (index) => {
      common_vendor.index.previewImage({
        urls: imageList.value,
        current: index
      });
    };
    const openCompanySelect = () => {
      common_vendor.index.showActionSheet({
        itemList: ["当前项目单位", "其他单位..."],
        success: async (res) => {
          if (res.tapIndex === 0) {
            const current = await api_index.api.getCurrentCompany();
            if (current.data) {
              formData.value.companyId = current.data.companyId;
              formData.value.companyName = current.data.companyName;
            }
          } else {
            common_vendor.index.showToast({ title: "搜索功能暂未开放", icon: "none" });
          }
        }
      });
    };
    const handleSubmit = async () => {
      if (!formData.value.companyId) {
        return common_vendor.index.showToast({ title: "请选择报修单位", icon: "none" });
      }
      if (!formData.value.faultDescription) {
        return common_vendor.index.showToast({ title: "请输入故障描述", icon: "none" });
      }
      submitting.value = true;
      try {
        const submitData = { ...formData.value };
        Object.keys(submitData).forEach((key) => {
          if (submitData[key] === null || submitData[key] === void 0) {
            delete submitData[key];
          }
        });
        const res = await api_index.api.addRepair(submitData);
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "提交成功" });
          setTimeout(() => {
            common_vendor.index.redirectTo({ url: "/pages/repair/index" });
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "提交失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.showToast({ title: "请求失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    };
    common_vendor.onMounted(() => {
      const now = /* @__PURE__ */ new Date();
      formData.value.foundTime = formatDateTime(now);
      api_index.api.getCurrentCompany().then((res) => {
        if (res.data) {
          formData.value.companyId = res.data.companyId;
          formData.value.companyName = res.data.companyName;
        }
      });
      api_index.api.getEquipmentCategories().then((res) => {
        if (res.data) {
          equipmentCategories.value = res.data;
        }
      });
      api_index.api.getSystemTypes().then((res) => {
        if (res.data) {
          systemTypeCategoriesMap.value = res.data;
          systemTypeCategoriesLabels.value = res.data.map((item) => item.label);
        }
      });
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "故障上报",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(formData.value.companyName || "请选择单位"),
        d: !formData.value.companyName ? 1 : "",
        e: common_vendor.p({
          type: "right",
          size: "16",
          color: "#999"
        }),
        f: common_vendor.o(openCompanySelect),
        g: common_vendor.t(formData.value.systemTypeName || "请选择系统"),
        h: !formData.value.systemTypeName ? 1 : "",
        i: common_vendor.p({
          type: "bottom",
          size: "14",
          color: "#999"
        }),
        j: systemTypeCategoriesLabels.value,
        k: common_vendor.o(onSystemTypeChange),
        l: common_vendor.t(formData.value.equipmentName || "请选择设备名称"),
        m: !formData.value.equipmentName ? 1 : "",
        n: common_vendor.p({
          type: "bottom",
          size: "14",
          color: "#999"
        }),
        o: equipmentCategories.value,
        p: common_vendor.o(onEquipmentChange),
        q: formData.value.customerAddress,
        r: common_vendor.o(($event) => formData.value.customerAddress = $event.detail.value),
        s: common_vendor.t(formData.value.foundTime || "请选择日期时间"),
        t: common_vendor.p({
          type: "right",
          size: "16",
          color: "#999"
        }),
        v: pickerDateTimeValue.value,
        w: common_vendor.o(onDateChange),
        x: common_vendor.f(urgencyOptions, (item, index, i0) => {
          return {
            a: common_vendor.t(item.label),
            b: index,
            c: formData.value.urgencyLevel === item.value ? 1 : "",
            d: common_vendor.o(($event) => formData.value.urgencyLevel = item.value, index)
          };
        }),
        y: formData.value.faultDescription,
        z: common_vendor.o(($event) => formData.value.faultDescription = $event.detail.value),
        A: common_vendor.f(imageList.value, (img, index, i0) => {
          return {
            a: img,
            b: common_vendor.o(($event) => previewImage(index), index),
            c: "d88a0b15-5-" + i0,
            d: common_vendor.o(($event) => deleteImage(index), index),
            e: index
          };
        }),
        B: common_vendor.p({
          type: "closeempty",
          size: "12",
          color: "#fff"
        }),
        C: imageList.value.length < 4
      }, imageList.value.length < 4 ? {
        D: common_vendor.p({
          type: "plusempty",
          size: "30",
          color: "#ccc"
        }),
        E: common_vendor.o(chooseImage)
      } : {}, {
        F: common_vendor.o(handleSubmit),
        G: submitting.value
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d88a0b15"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/repair/form.js.map
