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
const getCurrentCompany = () => request.get("/api/fire/company/current");

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

const getCheckInList = (data) => request.post("/api/fire/checkIn/list", data);
const addCheckIn = (data) => request.post("/api/fire/checkIn/add", data);
const getCheckInDetail = (id) => request.get(`/api/fire/checkIn/detail/${id}`);
const reverseGeocode = (params, options) =>
  request.get("/api/fire/checkIn/reverseGeocode", params || {}, options || {});
const validateLocation = (data) =>
  request.post("/api/fire/checkIn/validateLocation", data);
const listTasksByCompany = (data) =>
  request.post("/api/fire/checkIn/listTasksByCompany", data);
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

const getReportList = (data) => request.post("/api/fire/report/list", data);
const getReportDetail = (id) => request.get(`/api/fire/report/detail/${id}`);
const getReportPreviewUrl = (id) => `/api/fire/report/preview/${id}`;
const getReportDownloadUrl = (id) => `/api/fire/report/download/${id}`;

function downloadReportFile(reportId, openAfter) {
  const url = BASE_URL + getReportPreviewUrl(reportId);
  const token = auth.getToken();
  return new Promise((resolve, reject) => {
    wx.showLoading({ title: "\u52a0\u8f7d\u4e2d...", mask: true });
    wx.downloadFile({
      url,
      header: token ? { Authorization: "Bearer " + token } : {},
      success(res) {
        wx.hideLoading();
        if (res.statusCode !== 200) {
          wx.showToast({ title: "\u9884\u89c8\u5931\u8d25", icon: "none" });
          reject(res);
          return;
        }
        if (openAfter === false) {
          wx.saveFile({
            tempFilePath: res.tempFilePath,
            success(saveRes) {
              wx.showToast({ title: "\u4e0b\u8f7d\u6210\u529f", icon: "success" });
              resolve(saveRes);
            },
            fail(err) {
              wx.showToast({ title: "\u4e0b\u8f7d\u5931\u8d25", icon: "none" });
              reject(err);
            }
          });
          return;
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          success: resolve,
          fail(err) {
            wx.showToast({ title: "\u65e0\u6cd5\u6253\u5f00\u6587\u6863", icon: "none" });
            reject(err);
          }
        });
      },
      fail(err) {
        wx.hideLoading();
        wx.showToast({ title: "\u4e0b\u8f7d\u5931\u8d25", icon: "none" });
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
    wx.showLoading({ title: "\u52a0\u8f7d\u4e2d...", mask: true });
    wx.downloadFile({
      url,
      header: token ? { Authorization: "Bearer " + token } : {},
      success(res) {
        wx.hideLoading();
        if (res.statusCode !== 200) {
          wx.showToast({ title: "\u4e0b\u8f7d\u5931\u8d25", icon: "none" });
          reject(res);
          return;
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          success: resolve,
          fail(err) {
            wx.showToast({ title: "\u65e0\u6cd5\u6253\u5f00\u6587\u6863", icon: "none" });
            reject(err);
          }
        });
      },
      fail(err) {
        wx.hideLoading();
        wx.showToast({ title: "\u4e0b\u8f7d\u5931\u8d25", icon: "none" });
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
  getReportList,
  getReportDetail,
  getReportPreviewUrl,
  getReportDownloadUrl,
  previewReport,
  downloadReport,
  getDictSystemTypes,
  getDictInspectionTypes,
  getDictEquipmentCategories,
  getDeptList,
  getDeptTree
};

module.exports = { api };
