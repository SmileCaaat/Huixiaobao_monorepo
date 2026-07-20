"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_nav_bar2 + _easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_icons + _easycom_uni_popup)();
}
const _sfc_main = {
  __name: "detail",
  setup(__props) {
    const repairId = common_vendor.ref(null);
    const detail = common_vendor.ref(null);
    const faultImages = common_vendor.ref([]);
    const repairImages = common_vendor.ref([]);
    const userInfo = common_vendor.ref({});
    const completePopup = common_vendor.ref(null);
    const submitting = common_vendor.ref(false);
    const localRepairImages = common_vendor.ref([]);
    const completeData = common_vendor.ref({
      repairId: null,
      repairDescription: "",
      repairImages: ""
    });
    common_vendor.onLoad((options) => {
      repairId.value = options.id;
    });
    const getStatusText = (status) => {
      const map = { 0: "待处理", 1: "处理中", 2: "已完成" };
      return map[status] || "未知";
    };
    const getStatusIcon = (status) => {
      const map = { 0: "info", 1: "refreshempty", 2: "checkmarkempty" };
      return map[status] || "info";
    };
    const getUrgencyText = (level) => {
      const map = { 1: "一般", 2: "紧急", 3: "特急" };
      return map[level] || "一般";
    };
    const isAdmin = common_vendor.computed(
      () => {
        var _a;
        return ((_a = userInfo.value.roles) == null ? void 0 : _a.includes("admin")) || userInfo.value.roleId === 1;
      }
    );
    const isReporter = common_vendor.computed(
      () => {
        var _a;
        return ((_a = detail.value) == null ? void 0 : _a.reporterId) === userInfo.value.userId;
      }
    );
    const isAssignee = common_vendor.computed(
      () => {
        var _a;
        return ((_a = detail.value) == null ? void 0 : _a.repairUserId) === userInfo.value.userId;
      }
    );
    const canManage = common_vendor.computed(() => isAdmin.value || isReporter.value);
    const canAccept = common_vendor.computed(() => isAdmin.value && !isReporter.value);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const normalizeImageUrl = (url) => {
      if (!url)
        return "";
      return url.startsWith("http") ? url : `${api_index.api.BASE_URL}${url}`;
    };
    const fetchData = async () => {
      common_vendor.index.showLoading({ title: "加载中..." });
      try {
        const res = await api_index.api.getRepairDetail(repairId.value);
        if (res.code === 200 || res.code === 0) {
          detail.value = res.data;
          faultImages.value = res.data.faultImages ? res.data.faultImages.split(",").filter(Boolean).map(normalizeImageUrl) : [];
          repairImages.value = res.data.repairImages ? res.data.repairImages.split(",").filter(Boolean).map(normalizeImageUrl) : [];
          completeData.value.repairId = res.data.repairId;
        }
      } catch (e) {
        common_vendor.index.showToast({ title: "详情获取失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const previewImage = (images, index) => {
      common_vendor.index.previewImage({
        urls: images,
        current: index
      });
    };
    const handleAccept = async () => {
      common_vendor.index.showModal({
        title: "确认处理",
        content: "确认受理此报修单并开始处理吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const resAccept = await api_index.api.dispatchRepair({
                repairId: detail.value.repairId,
                repairUserId: userInfo.value.userId
              });
              if (resAccept.code === 200 || resAccept.code === 0) {
                common_vendor.index.showToast({ title: "受理成功" });
                fetchData();
              }
            } catch (e) {
              common_vendor.index.showToast({ title: "受理失败", icon: "none" });
            }
          }
        }
      });
    };
    const handleDelete = () => {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这条报修记录吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const resDel = await api_index.api.deleteRepair(detail.value.repairId);
              if (resDel.code === 200 || resDel.code === 0) {
                common_vendor.index.showToast({ title: "删除成功" });
                setTimeout(() => common_vendor.index.navigateBack(), 1e3);
              }
            } catch (e) {
              common_vendor.index.showToast({ title: "删除失败", icon: "none" });
            }
          }
        }
      });
    };
    const openCompleteForm = () => {
      completePopup.value.open();
    };
    const closeCompleteForm = () => {
      completePopup.value.close();
    };
    const chooseRepairImage = () => {
      common_vendor.index.chooseImage({
        count: 4 - localRepairImages.value.length,
        success: async (res) => {
          var _a, _b;
          for (const path of res.tempFilePaths) {
            try {
              const uploadRes = await api_index.api.uploadFile(path, { name: "file" });
              const url = uploadRes.url || ((_a = uploadRes.data) == null ? void 0 : _a.url) || uploadRes.fileName || ((_b = uploadRes.data) == null ? void 0 : _b.fileName);
              if ((uploadRes.code === 200 || uploadRes.code === 0) && url) {
                localRepairImages.value.push(path);
                completeData.value.repairImages = completeData.value.repairImages ? completeData.value.repairImages + "," + url : url;
              }
            } catch (e) {
            }
          }
        }
      });
    };
    const deleteRepairImage = (index) => {
      localRepairImages.value.splice(index, 1);
      const urls = completeData.value.repairImages.split(",");
      urls.splice(index, 1);
      completeData.value.repairImages = urls.join(",");
    };
    const submitComplete = async () => {
      if (!completeData.value.repairDescription) {
        return common_vendor.index.showToast({ title: "请输入处理说明", icon: "none" });
      }
      submitting.value = true;
      try {
        const res = await api_index.api.completeRepair(completeData.value);
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "工单已完结" });
          closeCompleteForm();
          fetchData();
        }
      } catch (e) {
        common_vendor.index.showToast({ title: "提交失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    };
    common_vendor.onMounted(async () => {
      const userRes = await api_index.api.getUserInfo();
      if (userRes.data) {
        userInfo.value = userRes.data;
      }
      fetchData();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "工单详情",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: detail.value
      }, detail.value ? common_vendor.e({
        d: common_vendor.p({
          type: getStatusIcon(detail.value.repairStatus),
          size: "24",
          color: "#fff"
        }),
        e: common_vendor.t(getStatusText(detail.value.repairStatus)),
        f: common_vendor.n("status-" + detail.value.repairStatus),
        g: common_vendor.t(detail.value.repairId),
        h: common_vendor.t(getUrgencyText(detail.value.urgencyLevel)),
        i: common_vendor.n("urgency-" + detail.value.urgencyLevel),
        j: common_vendor.t(detail.value.createTime),
        k: common_vendor.t(detail.value.reporterName || "系统"),
        l: common_vendor.t(detail.value.companyName),
        m: common_vendor.t(detail.value.systemTypeName || "未指定"),
        n: common_vendor.t(detail.value.equipmentName || "未关联设备"),
        o: common_vendor.t(detail.value.customerAddress || "未填写"),
        p: common_vendor.t(detail.value.faultDescription || "无描述"),
        q: faultImages.value.length > 0
      }, faultImages.value.length > 0 ? {
        r: common_vendor.f(faultImages.value, (img, index, i0) => {
          return {
            a: index,
            b: img,
            c: common_vendor.o(($event) => previewImage(faultImages.value, index), index)
          };
        })
      } : {}, {
        s: detail.value.repairUserId || detail.value.repairStatus !== "0"
      }, detail.value.repairUserId || detail.value.repairStatus !== "0" ? common_vendor.e({
        t: common_vendor.t(detail.value.repairUserName || detail.value.repairPerson || "未分配"),
        v: detail.value.dispatchTime
      }, detail.value.dispatchTime ? {
        w: common_vendor.t(detail.value.dispatchTime)
      } : {}, {
        x: detail.value.repairStatus === "2"
      }, detail.value.repairStatus === "2" ? common_vendor.e({
        y: common_vendor.t(detail.value.updateTime),
        z: common_vendor.t(detail.value.repairDescription || "无总结描述"),
        A: repairImages.value.length > 0
      }, repairImages.value.length > 0 ? {
        B: common_vendor.f(repairImages.value, (img, index, i0) => {
          return {
            a: index,
            b: img,
            c: common_vendor.o(($event) => previewImage(repairImages.value, index), index)
          };
        })
      } : {}) : {}) : {}) : {}, {
        C: detail.value
      }, detail.value ? common_vendor.e({
        D: detail.value.repairStatus === "0"
      }, detail.value.repairStatus === "0" ? common_vendor.e({
        E: canManage.value
      }, canManage.value ? {
        F: common_vendor.o(handleDelete)
      } : {}, {
        G: canAccept.value
      }, canAccept.value ? {
        H: common_vendor.o(handleAccept)
      } : {}) : {}, {
        I: detail.value.repairStatus === "1" && (isAssignee.value || isAdmin.value)
      }, detail.value.repairStatus === "1" && (isAssignee.value || isAdmin.value) ? {
        J: common_vendor.o(openCompleteForm)
      } : {}, {
        K: detail.value.repairStatus === "2"
      }, detail.value.repairStatus === "2" ? {} : {}) : {}, {
        L: common_vendor.o(closeCompleteForm),
        M: common_vendor.p({
          type: "closeempty",
          size: "20"
        }),
        N: completeData.value.repairDescription,
        O: common_vendor.o(($event) => completeData.value.repairDescription = $event.detail.value),
        P: common_vendor.f(localRepairImages.value, (img, index, i0) => {
          return {
            a: img,
            b: "20d3ee06-4-" + i0 + ",20d3ee06-2",
            c: common_vendor.o(($event) => deleteRepairImage(index), index),
            d: index
          };
        }),
        Q: common_vendor.p({
          type: "closeempty",
          size: "12",
          color: "#fff"
        }),
        R: localRepairImages.value.length < 4
      }, localRepairImages.value.length < 4 ? {
        S: common_vendor.p({
          type: "plusempty",
          size: "30",
          color: "#ccc"
        }),
        T: common_vendor.o(chooseRepairImage)
      } : {}, {
        U: common_vendor.o(submitComplete),
        V: submitting.value,
        W: common_vendor.sr(completePopup, "20d3ee06-2", {
          "k": "completePopup"
        }),
        X: common_vendor.p({
          type: "bottom"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-20d3ee06"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/repair/detail.js.map
