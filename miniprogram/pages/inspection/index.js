"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_search_bar2 = common_vendor.resolveComponent("uni-search-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_fab2 = common_vendor.resolveComponent("uni-fab");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_nav_bar2 + _easycom_uni_search_bar2 + _easycom_uni_icons2 + _easycom_uni_fab2 + _easycom_uni_popup2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_search_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-search-bar/uni-search-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_fab = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-fab/uni-fab.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_search_bar + _easycom_uni_icons + _easycom_uni_fab + _easycom_uni_popup)();
}
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const searchKeyword = common_vendor.ref("");
    const inspectionList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const noMore = common_vendor.ref(false);
    const pageNum = common_vendor.ref(1);
    const pageSize = common_vendor.ref(10);
    const currentCompanyId = common_vendor.ref(null);
    const datePopup = common_vendor.ref(null);
    const selectedDate = common_vendor.ref("");
    const dateValue = common_vendor.ref([5, 0, 0]);
    const currentYear = (/* @__PURE__ */ new Date()).getFullYear();
    const years = Array.from({ length: 10 }, (_, i) => currentYear - 5 + i);
    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    const days = common_vendor.computed(() => {
      const yearIndex = dateValue.value[0] || 0;
      const monthIndex = dateValue.value[1] || 0;
      const year = years[yearIndex];
      const month = months[monthIndex];
      const daysInMonth = new Date(year, month, 0).getDate();
      return Array.from({ length: daysInMonth }, (_, i) => i + 1);
    });
    const fabPattern = common_vendor.ref({
      color: "#fff",
      backgroundColor: "#e53935",
      selectedColor: "#fff",
      buttonColor: "#e53935"
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goDetail = (item) => {
      common_vendor.index.navigateTo({
        url: `/pages/inspection/detail?id=${item.inspectionId}`
      });
    };
    const goAdd = () => {
      common_vendor.index.navigateTo({
        url: "/pages/inspection/form"
      });
    };
    const handleSearch = () => {
      pageNum.value = 1;
      noMore.value = false;
      inspectionList.value = [];
      loadList();
    };
    const handleClear = () => {
      searchKeyword.value = "";
      handleSearch();
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          currentCompanyId.value = res.data.companyId;
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/index.vue:219", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadList = async () => {
      if (loading.value || noMore.value)
        return;
      try {
        loading.value = true;
        if (!currentCompanyId.value) {
          await loadCurrentCompany();
        }
        const res = await api_index.api.getMyInspectionList({
          companyId: currentCompanyId.value,
          keyword: searchKeyword.value,
          inspectionDate: selectedDate.value,
          pageNum: pageNum.value,
          pageSize: pageSize.value
        });
        if (res.code === 200 || res.code === 0) {
          const data = res.rows || res.data || [];
          if (pageNum.value === 1) {
            inspectionList.value = data;
          } else {
            inspectionList.value = [...inspectionList.value, ...data];
          }
          if (data.length < pageSize.value) {
            noMore.value = true;
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/index.vue:256", "获取巡检列表失败", e);
        common_vendor.index.showToast({ title: "获取数据失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      if (!noMore.value && !loading.value) {
        pageNum.value++;
        loadList();
      }
    };
    const onDateChange = (e) => {
      dateValue.value = e.detail.value;
    };
    const openDatePicker = () => {
      datePopup.value.open();
    };
    const closeDatePicker = () => {
      datePopup.value.close();
    };
    const confirmDate = () => {
      const year = years[dateValue.value[0]];
      const month = String(months[dateValue.value[1]]).padStart(2, "0");
      const day = String((dateValue.value[2] || 0) + 1).padStart(2, "0");
      selectedDate.value = `${year}-${month}-${day}`;
      datePopup.value.close();
      handleSearch();
    };
    const formatDate = (dateStr) => {
      if (!dateStr)
        return "";
      return dateStr.substring(0, 10);
    };
    const getStatusClass = (status) => {
      switch (String(status)) {
        case "0":
        case "正常":
          return "status-normal";
        case "1":
        case "异常":
          return "status-error";
        default:
          return "status-normal";
      }
    };
    const getStatusText = (status) => {
      switch (String(status)) {
        case "0":
          return "正常";
        case "1":
          return "异常";
        default:
          return "正常";
      }
    };
    const refreshList = () => {
      pageNum.value = 1;
      noMore.value = false;
      inspectionList.value = [];
      loadList();
    };
    common_vendor.onMounted(async () => {
      await loadCurrentCompany();
      loadList();
      common_vendor.index.$on("refreshInspectionList", refreshList);
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("refreshInspectionList", refreshList);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "巡检管理",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.o(handleSearch),
        d: common_vendor.o(handleClear),
        e: common_vendor.o(($event) => searchKeyword.value = $event),
        f: common_vendor.p({
          placeholder: "请输入关键字",
          cancelButton: "none",
          modelValue: searchKeyword.value
        }),
        g: common_vendor.p({
          type: "calendar",
          size: "24",
          color: "#666"
        }),
        h: common_vendor.o(openDatePicker),
        i: common_vendor.f(inspectionList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.equipmentName || item.equipmentType),
            b: item.inspectionType
          }, item.inspectionType ? {
            c: common_vendor.t(item.inspectionTypeName || "测试")
          } : {}, {
            d: common_vendor.t(formatDate(item.inspectionDate || item.createTime)),
            e: common_vendor.t(item.inspectorName || item.createBy),
            f: common_vendor.t(item.quantity || item.equipmentCount || 1),
            g: common_vendor.t(item.location || "-"),
            h: common_vendor.t(item.statusName || getStatusText(item.status)),
            i: common_vendor.n(getStatusClass(item.status)),
            j: common_vendor.t(item.buildingName || "-"),
            k: item.inspectionId,
            l: common_vendor.o(($event) => goDetail(item), item.inspectionId)
          });
        }),
        j: inspectionList.value.length === 0 && !loading.value
      }, inspectionList.value.length === 0 && !loading.value ? {} : {}, {
        k: loading.value
      }, loading.value ? {} : {}, {
        l: noMore.value && inspectionList.value.length > 0
      }, noMore.value && inspectionList.value.length > 0 ? {} : {}, {
        m: common_vendor.o(loadMore),
        n: common_vendor.o(goAdd),
        o: common_vendor.p({
          pattern: fabPattern.value,
          horizontal: "right",
          vertical: "bottom"
        }),
        p: common_vendor.o(closeDatePicker),
        q: common_vendor.o(confirmDate),
        r: common_vendor.f(common_vendor.unref(years), (year, k0, i0) => {
          return {
            a: common_vendor.t(year),
            b: year
          };
        }),
        s: common_vendor.f(common_vendor.unref(months), (month, k0, i0) => {
          return {
            a: common_vendor.t(month),
            b: month
          };
        }),
        t: common_vendor.f(days.value, (day, k0, i0) => {
          return {
            a: common_vendor.t(day),
            b: day
          };
        }),
        v: dateValue.value,
        w: common_vendor.o(onDateChange),
        x: common_vendor.sr(datePopup, "042a83e3-4", {
          "k": "datePopup"
        }),
        y: common_vendor.p({
          type: "bottom"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-042a83e3"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/inspection/index.js.map
