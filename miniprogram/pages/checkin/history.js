"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
const pageSize = 10;
const _sfc_main = {
  __name: "history",
  setup(__props) {
    const historyList = common_vendor.ref([]);
    const pageNum = common_vendor.ref(1);
    const loading = common_vendor.ref(false);
    const noMore = common_vendor.ref(false);
    const currentCompanyId = common_vendor.ref(null);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "";
      if (typeof timeStr !== "string")
        return String(timeStr);
      return timeStr.substring(0, 19).replace("T", " ");
    };
    const getFullUrl = (url) => {
      if (!url)
        return "";
      if (url.startsWith("http"))
        return url;
      return utils_request.BASE_URL + url;
    };
    const previewImage = (images, index) => {
      const urls = images.map((img) => getFullUrl(img.imageUrl));
      common_vendor.index.previewImage({
        urls,
        current: urls[index]
      });
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          currentCompanyId.value = res.data.companyId;
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/history.vue:111", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadHistory = async () => {
      if (loading.value || noMore.value)
        return;
      loading.value = true;
      try {
        if (!currentCompanyId.value) {
          await loadCurrentCompany();
        }
        const res = await api_index.api.getCheckInList({
          companyId: currentCompanyId.value,
          pageNum: pageNum.value,
          pageSize
        });
        if (res && (res.code === 200 || res.code === 0)) {
          const rows = res.rows || res.data || [];
          if (rows.length < pageSize) {
            noMore.value = true;
          }
          historyList.value = [...historyList.value, ...rows];
          pageNum.value++;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/checkin/history.vue:140", "获取历史记录失败", e);
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      loadHistory();
    };
    common_vendor.onMounted(async () => {
      await loadCurrentCompany();
      loadHistory();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.f(historyList.value, (item, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.checkInType === "0" ? "签到" : "签退"),
            b: common_vendor.n(item.checkInType === "0" ? "in" : "out"),
            c: common_vendor.t(formatTime(item.checkInTime || item.createTime)),
            d: common_vendor.t(item.companyName || "当前项目"),
            e: common_vendor.t(item.address || "-"),
            f: item.remark
          }, item.remark ? {
            g: common_vendor.t(item.remark)
          } : {}, {
            h: item.images && item.images.length > 0
          }, item.images && item.images.length > 0 ? {
            i: common_vendor.f(item.images, (img, idx, i1) => {
              return {
                a: idx,
                b: getFullUrl(img.imageUrl),
                c: common_vendor.o(($event) => previewImage(item.images, idx), idx)
              };
            })
          } : {}, {
            j: index
          });
        }),
        c: loading.value
      }, loading.value ? {} : noMore.value ? {} : historyList.value.length === 0 ? {} : {}, {
        d: noMore.value,
        e: historyList.value.length === 0,
        f: common_vendor.o(loadMore)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f3aa1d8e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/checkin/history.js.map
