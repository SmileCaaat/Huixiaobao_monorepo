const request = require("../services/request.js");
const auth = require("../services/auth.js");
const { BASE_URL } = require("../services/request.js");

const login = (data) => request.post("/api/login", data, { loading: true });
const register = (data) => request.post("/api/register", data);
const resolveRegisterInvite = (params) =>
  request.get("/api/register/invite/resolve", params);
const getUserInfo = () => request.get("/api/user/info");
const logout = () => request.post("/api/logout");
const getMenus = () => request.get("/api/user/menus");

const getHomeStats = (params, options) =>
  request.get("/api/fire/home", params || {}, options || {});
const getMyCompanyList = () => request.get("/api/fire/company/myList");
const switchCompany = (data) => request.post("/api/fire/company/switch", data);
const getCompanyDetail = (companyId) =>
  request.get(`/api/fire/company/detail/${companyId}`);
const getCurrentCompany = (query, options) =>
  request.get("/api/fire/company/current", query || {}, options || {});

const getBuildingList = (data) => request.post("/api/fire/building/list", data);
const getBuildingDetail = (id) => request.get(`/api/fire/building/detail/${id}`);
const getBuildingsByCompany = (companyId) =>
  request.get(`/api/fire/building/byCompany/${companyId}`);
const addBuilding = (data) => request.post("/api/fire/building/add", data);
const updateBuilding = (data) => request.post("/api/fire/building/edit", data);

const getEquipmentList = (data) => request.post("/api/fire/equipment/list", data);
const getEquipmentDetail = (id) =>
  request.get(`/api/fire/equipment/detail/${id}`);
const addEquipment = (data) => request.post("/api/fire/equipment/add", data);
const editEquipment = (data) => request.post("/api/fire/equipment/edit", data);
const scanEquipment = (equipmentCode) =>
  request.get(`/api/fire/equipment/scan/${encodeURIComponent(equipmentCode)}`);
const getEquipmentByCompanyAndSystem = (params) =>
  request.get("/api/fire/equipment/byCompanyAndSystem", params);

const getMyTaskList = (data) => request.post("/api/fire/task/myList", data);
const getTaskDetail = (taskId, recordType) =>
  request.get(
    `/api/fire/task/detail/${taskId}`,
    recordType !== undefined ? { recordType } : {}
  );
const getSystemDetail = (recordId) =>
  request.get(`/api/fire/task/system/${recordId}`);
const getDeviceDetail = (recordId) =>
  request.get(`/api/fire/task/equipment/${recordId}`);
const getInspectionTestDetail = (taskId) =>
  request.get(`/api/fire/task/inspectionTest/${taskId}`);
const getInspectionTestSystem = (taskId, categoryKey) =>
  request.get(
    `/api/fire/task/inspectionTest/system/${taskId}/${encodeURIComponent(categoryKey)}`
  );
const markInspectionTestNoDevice = (taskId, categoryKey, equipmentKey) =>
  request.post(
    `/api/fire/task/inspectionTest/noDevice/${taskId}/${encodeURIComponent(categoryKey)}/${encodeURIComponent(equipmentKey)}`,
    {}
  );
const markCategoryAllNormal = (taskId, categoryKey) =>
  request.post(
    `/api/fire/task/inspectionTest/markCategoryAllNormal/${taskId}/${encodeURIComponent(categoryKey)}`,
    {}
  );
const getInspectionTestEquipment = (taskId, categoryKey, equipmentKey) =>
  request.get(
    `/api/fire/task/inspectionTest/equipment/${taskId}/${encodeURIComponent(categoryKey)}/${encodeURIComponent(equipmentKey)}`
  );
const getTaskConclusion = (taskId) =>
  request.get(`/api/fire/task/conclusion/${taskId}`);
const getPreviousTaskConclusion = (taskId) =>
  request.get(`/api/fire/task/conclusion/previous/${taskId}`);
const saveTaskConclusion = (data) =>
  request.post("/api/fire/task/saveConclusion", data);
const updateCheckResult = (data) =>
  request.post("/api/fire/task/updateCheckResult", data);
const updateFaultDesc = (data) =>
  request.post("/api/fire/task/updateFaultDesc", data);
const updateCheckDetail = (data) =>
  request.post("/api/fire/task/updateCheckDetail", data);
const updateMaintenance = (data) =>
  request.post("/api/fire/task/updateMaintenance", data);

const getInspectionList = (data) =>
  request.post("/api/fire/inspection/list", data);
const getMyInspectionList = (data) =>
  request.post("/api/fire/inspection/myList", data);
const getInspectionDetail = (id) =>
  request.get(`/api/fire/inspection/detail/${id}`);
const addInspection = (data) => request.post("/api/fire/inspection/add", data);
const editInspection = (data) => request.post("/api/fire/inspection/edit", data);
const deleteInspection = (id) =>
  request.post(`/api/fire/inspection/delete/${id}`);
const getInspectionTemplateCategories = () =>
  request.get("/api/fire/inspection/templateCategories");
const getInspectionTemplateEquipments = (categoryKey) =>
  request.get(
    `/api/fire/inspection/templateCategories/${encodeURIComponent(categoryKey)}/equipments`
  );
const getInspectionSystemTypes = () =>
  request.get("/api/fire/inspection/systemTypes");
const getInspectionEquipmentTypes = (categoryKey) =>
  request.get(
    `/api/fire/inspection/equipmentTypes/${encodeURIComponent(categoryKey)}`
  );

const getCheckInList = (data, options) =>
  request.post("/api/fire/checkIn/list", data, options || {});
const addCheckIn = (data) => request.post("/api/fire/checkIn/add", data);
const getCheckInDetail = (id) => request.get(`/api/fire/checkIn/detail/${id}`);
const reverseGeocode = (params, options) =>
  request.get("/api/fire/checkIn/reverseGeocode", params || {}, options || {});
const validateLocation = (data) =>
  request.post("/api/fire/checkIn/validateLocation", data);
const listTasksByCompany = (data, options) =>
  request.post("/api/fire/checkIn/listTasksByCompany", data, options || {});
const getCheckInTaskList = listTasksByCompany;

const getRepairList = (data) => request.post("/api/fire/repair/list", data);
const getMyReportedRepairList = (data) =>
  request.post("/api/fire/repair/myReportedList", data);
const getMyAssignedRepairList = (data) =>
  request.post("/api/fire/repair/myAssignedList", data);
const getRepairDetail = (id) => request.get(`/api/fire/repair/detail/${id}`);
const addRepair = (data) => request.post("/api/fire/repair/add", data);
const editRepair = (data) => request.post("/api/fire/repair/edit", data);
const deleteRepair = (id) => request.post(`/api/fire/repair/delete/${id}`);
const getDispatchUsers = (companyId) =>
  request.get(`/api/fire/repair/dispatchUsers/${companyId}`);
const dispatchRepair = (data) => request.post("/api/fire/repair/dispatch", data);
const startRepair = (repairId) =>
  request.post(`/api/fire/repair/start/${repairId}`);
const completeRepair = (data) => request.post("/api/fire/repair/complete", data);
const getRepairStats = () => request.get("/api/fire/repair/statistics");
const getRepairLogs = (id) => request.get(`/api/fire/repair/logs/${id}`);
const getRepairTransferUsers = (id) =>
  request.get(`/api/fire/repair/transferUsers/${id}`);
const transferRepair = (data) => request.post("/api/fire/repair/transfer", data);

const getReportList = (data, options) =>
  request.post("/api/fire/report/list", data, options);
const getReportDetail = (id) => request.get(`/api/fire/report/detail/${id}`);
const getReportPreviewUrl = (id) => `/api/fire/report/preview/${id}`;
const getReportDownloadUrl = (id) => `/api/fire/report/download/${id}`;

function pickHeader(header, name) {
  if (!header) return "";
  const lower = name.toLowerCase();
  const key = Object.keys(header).find((k) => String(k).toLowerCase() === lower);
  return key ? String(header[key] || "") : "";
}

function detectReportFileType(header) {
  const marked = pickHeader(header, "X-Report-File-Type").toLowerCase();
  if (marked === "pdf" || marked === "docx") return marked;
  const contentType = pickHeader(header, "Content-Type").toLowerCase();
  const disposition = pickHeader(header, "Content-Disposition").toLowerCase();
  if (contentType.indexOf("pdf") >= 0 || disposition.indexOf(".pdf") >= 0) return "pdf";
  if (
    contentType.indexOf("wordprocessingml") >= 0 ||
    contentType.indexOf("msword") >= 0 ||
    disposition.indexOf(".docx") >= 0 ||
    disposition.indexOf(".doc") >= 0
  ) {
    return "docx";
  }
  // 本地报告常见为 Word（PDF 转换失败时仍保留 docx）
  return "docx";
}

function openReportDocument(tempFilePath, header) {
  return new Promise((resolve, reject) => {
    wx.openDocument({
      filePath: tempFilePath,
      fileType: detectReportFileType(header),
      showMenu: true,
      success: resolve,
      fail(err) {
        wx.showToast({ title: "无法打开文档", icon: "none" });
        reject(err);
      }
    });
  });
}

function fetchReportPreviewFile(reportId) {
  const url = BASE_URL + getReportPreviewUrl(reportId);
  const token = auth.getToken();
  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url,
      header: token ? { Authorization: "Bearer " + token } : {},
      success(res) {
        if (res.statusCode !== 200) {
          reject(res);
          return;
        }
        resolve({ tempFilePath: res.tempFilePath, header: res.header });
      },
      fail: reject
    });
  });
}

function downloadReportFile(reportId, openAfter) {
  const url = BASE_URL + getReportPreviewUrl(reportId);
  const token = auth.getToken();
  return new Promise((resolve, reject) => {
    wx.showLoading({ title: "加载中...", mask: true });
    wx.downloadFile({
      url,
      header: token ? { Authorization: "Bearer " + token } : {},
      success(res) {
        wx.hideLoading();
        if (res.statusCode !== 200) {
          wx.showToast({ title: "预览失败(" + res.statusCode + ")", icon: "none" });
          reject(res);
          return;
        }
        if (openAfter === false) {
          wx.saveFile({
            tempFilePath: res.tempFilePath,
            success(saveRes) {
              wx.showToast({ title: "下载成功", icon: "success" });
              resolve(saveRes);
            },
            fail(err) {
              wx.showToast({ title: "下载失败", icon: "none" });
              reject(err);
            }
          });
          return;
        }
        openReportDocument(res.tempFilePath, res.header).then(resolve).catch(reject);
      },
      fail(err) {
        wx.hideLoading();
        wx.showToast({ title: "下载失败", icon: "none" });
        reject(err);
      }
    });
  });
}

const previewReport = (reportId) => downloadReportFile(reportId, true);
const downloadReport = (reportId) => {
  const url = BASE_URL + getReportDownloadUrl(reportId);
  const token = auth.getToken();
  return new Promise((resolve, reject) => {
    wx.showLoading({ title: "下载中...", mask: true });
    wx.downloadFile({
      url,
      header: token ? { Authorization: "Bearer " + token } : {},
      success(res) {
        if (res.statusCode !== 200) {
          wx.hideLoading();
          wx.showToast({ title: "下载失败(" + res.statusCode + ")", icon: "none" });
          reject(res);
          return;
        }
        const finishOpen = () => {
          openReportDocument(res.tempFilePath, res.header)
            .then((opened) => {
              wx.showToast({ title: "可在右上角菜单保存", icon: "none" });
              resolve(opened);
            })
            .catch(reject);
        };
        wx.saveFile({
          tempFilePath: res.tempFilePath,
          success(saveRes) {
            wx.hideLoading();
            wx.showToast({ title: "下载成功", icon: "success" });
            // 再打开一次，方便用户转发/另存
            openReportDocument(saveRes.savedFilePath || res.tempFilePath, res.header)
              .then(resolve)
              .catch(() => resolve(saveRes));
          },
          fail() {
            wx.hideLoading();
            finishOpen();
          }
        });
      },
      fail(err) {
        wx.hideLoading();
        wx.showToast({ title: "下载失败", icon: "none" });
        reject(err);
      }
    });
  });
};

const getDictSystemTypes = () => request.get("/api/fire/dict/systemTypes");
const getDictInspectionTypes = () =>
  request.get("/api/fire/dict/inspectionTypes");
const getDictEquipmentCategories = () =>
  request.get("/api/fire/dict/equipmentCategories");
const getDeptList = () => request.get("/api/fire/dept/list");
const getDeptTree = () => request.get("/api/fire/dept/tree");

const api = {
  login,
  register,
  resolveRegisterInvite,
  getUserInfo,
  logout,
  getMenus,
  getHomeStats,
  getMyCompanyList,
  switchCompany,
  getCompanyDetail,
  getCurrentCompany,
  getBuildingList,
  getBuildingDetail,
  getBuildingsByCompany,
  addBuilding,
  updateBuilding,
  getEquipmentList,
  getEquipmentDetail,
  addEquipment,
  editEquipment,
  scanEquipment,
  getEquipmentByCompanyAndSystem,
  getMyTaskList,
  getTaskDetail,
  getSystemDetail,
  getDeviceDetail,
  getInspectionTestDetail,
  getInspectionTestSystem,
  markInspectionTestNoDevice,
  markCategoryAllNormal,
  getInspectionTestEquipment,
  getTaskConclusion,
  getPreviousTaskConclusion,
  saveTaskConclusion,
  updateCheckResult,
  updateFaultDesc,
  updateCheckDetail,
  updateMaintenance,
  getInspectionList,
  getMyInspectionList,
  getInspectionDetail,
  addInspection,
  editInspection,
  deleteInspection,
  getInspectionTemplateCategories,
  getInspectionTemplateEquipments,
  getInspectionSystemTypes,
  getInspectionEquipmentTypes,
  getCheckInList,
  addCheckIn,
  getCheckInDetail,
  reverseGeocode,
  validateLocation,
  listTasksByCompany,
  getCheckInTaskList,
  getRepairList,
  getMyReportedRepairList,
  getMyAssignedRepairList,
  getRepairDetail,
  addRepair,
  editRepair,
  deleteRepair,
  getDispatchUsers,
  dispatchRepair,
  startRepair,
  completeRepair,
  getRepairStats,
  getRepairLogs,
  getRepairTransferUsers,
  transferRepair,
  getReportList,
  getReportDetail,
  getReportPreviewUrl,
  getReportDownloadUrl,
  fetchReportPreviewFile,
  openReportDocument,
  previewReport,
  downloadReport,
  getDictSystemTypes,
  getDictInspectionTypes,
  getDictEquipmentCategories,
  getDeptList,
  getDeptTree
};

module.exports = { api };
