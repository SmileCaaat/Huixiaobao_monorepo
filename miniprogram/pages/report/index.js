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
const pageSize = 10;
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const reportList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const pageNum = common_vendor.ref(1);
    const total = common_vendor.ref(0);
    const currentCompanyId = common_vendor.ref(null);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const formatFileSize = (bytes) => {
      if (!bytes)
        return "0 B";
      const k = 1024;
      const sizes = ["B", "KB", "MB", "GB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    };
    const formatDate = (dateStr) => {
      if (!dateStr)
        return "--";
      return dateStr.substring(0, 16);
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          currentCompanyId.value = res.data.companyId;
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/report/index.vue:122", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadData = async (refresh = false) => {
      if (loading.value)
        return;
      if (refresh) {
        pageNum.value = 1;
        reportList.value = [];
      }
      loading.value = true;
      try {
        if (!currentCompanyId.value) {
          await loadCurrentCompany();
        }
        if (!currentCompanyId.value) {
          common_vendor.index.showToast({ title: "请先选择公司", icon: "none" });
          return;
        }
        const res = await api_index.api.getReportList({
          companyId: currentCompanyId.value,
          reportName: "",
          params: {
            beginTime: "",
            endTime: ""
          },
          pageNum: pageNum.value,
          pageSize
        });
        if (res.code === 200 || res.code === 0) {
          const rows = res.rows || [];
          if (refresh) {
            reportList.value = rows;
          } else {
            reportList.value = [...reportList.value, ...rows];
          }
          total.value = res.total || 0;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/report/index.vue:167", "获取报告列表失败:", e);
        common_vendor.index.showToast({ title: "获取列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      if (reportList.value.length < total.value) {
        pageNum.value++;
        loadData();
      }
    };
    const handlePreview = async (item) => {
      common_vendor.index.showLoading({ title: "正在加载预览..." });
      try {
        const previewUrl = `${api_index.api.BASE_URL}${api_index.api.getReportPreviewUrl(item.reportId)}`;
        const filePath = `${common_vendor.index.env.USER_DATA_PATH}/${item.reportName}`;
        common_vendor.index.downloadFile({
          url: previewUrl,
          filePath,
          header: {
            Authorization: api_index.api.getToken() ? `Bearer ${api_index.api.getToken()}` : ""
          },
          success: (res) => {
            if (res.statusCode === 200 || res.errMsg.includes("ok")) {
              common_vendor.index.openDocument({
                filePath,
                showMenu: true,
                success: () => {
                  common_vendor.index.__f__("log", "at pages/report/index.vue:205", "预览成功");
                },
                fail: (err) => {
                  common_vendor.index.showToast({ title: "预览失败", icon: "none" });
                  common_vendor.index.__f__("error", "at pages/report/index.vue:209", "预览失败:", err);
                }
              });
            } else {
              common_vendor.index.showToast({ title: "预览请求失败", icon: "none" });
            }
          },
          fail: (err) => {
            common_vendor.index.showToast({ title: "预览下载失败", icon: "none" });
            common_vendor.index.__f__("error", "at pages/report/index.vue:218", "预览下载失败:", err);
          },
          complete: () => {
            common_vendor.index.hideLoading();
          }
        });
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/report/index.vue:226", "预览异常:", e);
        common_vendor.index.showToast({ title: "预览异常", icon: "none" });
      }
    };
    const handleDownload = async (item) => {
      common_vendor.index.showLoading({ title: "准备下载..." });
      try {
        const downloadUrl = `${api_index.api.BASE_URL}${api_index.api.getReportDownloadUrl(item.reportId)}`;
        const filePath = `${common_vendor.index.env.USER_DATA_PATH}/${item.reportName}`;
        common_vendor.index.downloadFile({
          url: downloadUrl,
          filePath,
          header: {
            Authorization: api_index.api.getToken() ? `Bearer ${api_index.api.getToken()}` : ""
          },
          success: (res) => {
            if (res.statusCode === 200 || res.errMsg.includes("ok")) {
              common_vendor.index.showActionSheet({
                itemList: ["保存到本地", "直接打开"],
                success: (action) => {
                  if (action.tapIndex === 0) {
                    common_vendor.index.saveFile({
                      tempFilePath: filePath,
                      success: (saveRes) => {
                        common_vendor.index.showModal({
                          title: "下载成功",
                          content: "文件已保存，路径：" + saveRes.savedFilePath,
                          showCancel: false
                        });
                      }
                    });
                  } else {
                    common_vendor.index.openDocument({
                      filePath,
                      showMenu: true
                    });
                  }
                }
              });
            } else {
              common_vendor.index.showToast({ title: "下载失败", icon: "none" });
            }
          },
          fail: (err) => {
            common_vendor.index.showToast({ title: "任务开启失败", icon: "none" });
            common_vendor.index.__f__("error", "at pages/report/index.vue:278", "下载失败:", err);
          },
          complete: () => {
            common_vendor.index.hideLoading();
          }
        });
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({ title: "下载异常", icon: "none" });
      }
    };
    common_vendor.onMounted(() => {
      loadData(true);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "维保报告",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.f(reportList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.reportName),
            b: common_vendor.t(item.taskName || "无"),
            c: common_vendor.t(item.companyName || "无"),
            d: common_vendor.t(formatFileSize(item.fileSize)),
            e: common_vendor.t(formatDate(item.createTime)),
            f: "804f1809-1-" + i0,
            g: "804f1809-2-" + i0,
            h: common_vendor.o(($event) => handleDownload(item), item.reportId),
            i: item.reportId,
            j: common_vendor.o(($event) => handlePreview(item), item.reportId)
          };
        }),
        d: common_vendor.p({
          type: "eye",
          size: "18",
          color: "#e53935"
        }),
        e: common_vendor.p({
          type: "download",
          size: "18",
          color: "#fff"
        }),
        f: reportList.value.length === 0 && !loading.value
      }, reportList.value.length === 0 && !loading.value ? {} : {}, {
        g: loading.value
      }, loading.value ? {} : {}, {
        h: common_vendor.o(loadMore)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-804f1809"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/report/index.js.map
