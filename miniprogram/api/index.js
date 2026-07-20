"use strict";
const utils_request = require("../utils/request.js");
const login = (data) => {
  return utils_request.post("/api/login", data);
};
const register = (data) => {
  return utils_request.post("/api/register", data);
};
const getUserInfo = () => {
  return utils_request.get("/api/user/info");
};
const logout = () => {
  return utils_request.post("/api/logout");
};
const getMenus = () => {
  return utils_request.get("/api/user/menus");
};
const getHomeStats = (params, options = {}) => {
  return utils_request.get("/fire/home", params, options);
};
const getMyCompanyList = () => {
  return utils_request.get("/api/fire/company/myList");
};
const getCompanyList = (data) => {
  return utils_request.post("/fire/company/list", data);
};
const switchCompany = (data) => {
  return utils_request.post("/api/fire/company/switch", data);
};
const getCompanyDetail = (companyId) => {
  return utils_request.get(`/api/fire/company/detail/${companyId}`);
};
const getCurrentCompany = () => {
  return utils_request.get("/api/fire/company/current");
};
const getEquipmentStats = () => {
  return utils_request.get("/fire/stats/equipment");
};
const getBuildingStats = () => {
  return utils_request.get("/fire/stats/building");
};
const getBuildingList = (data) => {
  return utils_request.post("/api/fire/building/list", data);
};
const getBuildingDetail = (id) => {
  return utils_request.get(`/api/fire/building/detail/${id}`);
};
const addBuilding = (data) => {
  return utils_request.post("/api/fire/building/add", data);
};
const updateBuilding = (data) => {
  return utils_request.post("/api/fire/building/edit", data);
};
const getBuildingsByCompany = (companyId) => {
  return utils_request.get(`/api/fire/building/byCompany/${companyId}`);
};
const getEquipmentList = (data) => {
  return utils_request.post("/api/fire/equipment/list", data);
};
const getEquipmentDetail = (id) => {
  return utils_request.get(`/api/fire/equipment/detail/${id}`);
};
const addEquipment = (data) => {
  return utils_request.post("/api/fire/equipment/add", data);
};
const editEquipment = (data) => {
  return utils_request.post("/api/fire/equipment/edit", data);
};
const scanEquipment = (equipmentCode) => {
  return utils_request.get(`/api/fire/equipment/scan/${equipmentCode}`);
};
const reportFault = (data) => {
  return utils_request.post("/api/fire/equipment/reportFault", data);
};
const getMyTaskList = (data) => {
  return utils_request.post("/api/fire/task/myList", data);
};
const getTaskDetail = (taskId, recordType) => {
  const params = recordType !== void 0 ? { recordType } : {};
  return utils_request.get(`/api/fire/task/detail/${taskId}`, params);
};
const getSystemDetail = (recordId) => {
  return utils_request.get(`/api/fire/task/system/${recordId}`);
};
const getDeviceDetail = (recordId) => {
  return utils_request.get(`/api/fire/task/equipment/${recordId}`);
};
const updateCheckResult = (data) => {
  return utils_request.post("/api/fire/task/updateCheckResult", data);
};
const updateFaultDesc = (data) => {
  return utils_request.post("/api/fire/task/updateFaultDesc", data);
};
const updateCheckDetail = (data) => {
  return utils_request.post("/api/fire/task/updateCheckDetail", data);
};
const updateMaintenance = (data) => {
  return utils_request.post("/api/fire/task/updateMaintenance", data);
};
const getInspectionList = (data) => {
  return utils_request.post("/api/fire/inspection/list", data);
};
const getMyInspectionList = (data) => {
  return utils_request.post("/api/fire/inspection/myList", data);
};
const getInspectionDetail = (id) => {
  return utils_request.get(`/api/fire/inspection/detail/${id}`);
};
const addInspection = (data) => {
  return utils_request.post("/api/fire/inspection/add", data);
};
const editInspection = (data) => {
  return utils_request.post("/api/fire/inspection/edit", data);
};
const deleteInspection = (id) => {
  return utils_request.post(`/api/fire/inspection/delete/${id}`);
};
const getCheckInList = (data) => {
  return utils_request.post("/api/fire/checkIn/list", data);
};
const addCheckIn = (data) => {
  return utils_request.post("/api/fire/checkIn/add", data);
};
const getCheckInDetail = (id) => {
  return utils_request.get(`/api/fire/checkIn/detail/${id}`);
};
const validateLocation = (data) => {
  return utils_request.post("/api/fire/checkIn/validateLocation", data);
};
const getCheckInTaskList = (data) => {
  return utils_request.post("/api/fire/checkIn/listTasksByCompany", data);
};
const uploadFile = (filePath, formData) => {
  return utils_request.upload(filePath, formData);
};
const getReportList = (data) => {
  return utils_request.post("/api/fire/report/list", data);
};
const getReportDetail = (id) => {
  return utils_request.get(`/api/fire/report/detail/${id}`);
};
const getReportDownloadUrl = (id) => {
  return `/api/fire/report/download/${id}`;
};
const getReportPreviewUrl = (id) => {
  return `/api/fire/report/preview/${id}`;
};
const getSystemTypes = () => {
  return utils_request.get("/api/fire/dict/systemTypes");
};
const getEquipmentCategories = () => {
  return utils_request.get("/api/fire/dict/equipmentCategories");
};
const getInspectionTypes = () => {
  return utils_request.get("/api/fire/dict/inspectionTypes");
};
const getRepairList = (data) => {
  return utils_request.post("/api/fire/repair/list", data);
};
const getMyReportedRepairs = (data) => {
  return utils_request.post("/api/fire/repair/myReportedList", data);
};
const getMyAssignedRepairs = (data) => {
  return utils_request.post("/api/fire/repair/myAssignedList", data);
};
const getRepairDetail = (repairId) => {
  return utils_request.get(`/api/fire/repair/detail/${repairId}`);
};
const addRepair = (data) => {
  return utils_request.post("/api/fire/repair/add", data);
};
const editRepair = (data) => {
  return utils_request.post("/api/fire/repair/edit", data);
};
const deleteRepair = (repairId) => {
  return utils_request.post(`/api/fire/repair/delete/${repairId}`);
};
const dispatchRepair = (data) => {
  return utils_request.post("/api/fire/repair/dispatch", data);
};
const completeRepair = (data) => {
  return utils_request.post("/api/fire/repair/complete", data);
};
const getRepairStats = () => {
  return utils_request.get("/api/fire/repair/statistics");
};
const getEquipmentByCompanyAndSystem = (params) => {
  return utils_request.get("/api/fire/equipment/byCompanyAndSystem", params);
};
const getDeptList = () => {
  return utils_request.get("/api/fire/dept/list");
};
const api = {
  // 认证
  login,
  register,
  getUserInfo,
  logout,
  getMenus,
  // 首页
  getHomeStats,
  getCompanyList,
  getMyCompanyList,
  switchCompany,
  getCompanyDetail,
  getCurrentCompany,
  getEquipmentStats,
  getBuildingStats,
  // 建筑
  getBuildingList,
  getBuildingDetail,
  getBuildingsByCompany,
  addBuilding,
  updateBuilding,
  // 设备
  getEquipmentList,
  getEquipmentDetail,
  addEquipment,
  editEquipment,
  scanEquipment,
  reportFault,
  // 任务
  getMyTaskList,
  getTaskDetail,
  getSystemDetail,
  getDeviceDetail,
  updateCheckResult,
  updateFaultDesc,
  updateCheckDetail,
  updateMaintenance,
  // 巡检
  getInspectionList,
  getMyInspectionList,
  getInspectionDetail,
  addInspection,
  editInspection,
  deleteInspection,
  // 签到
  getCheckInList,
  addCheckIn,
  getCheckInDetail,
  validateLocation,
  getCheckInTaskList,
  // 报告
  getReportList,
  getReportDetail,
  getReportDownloadUrl,
  getReportPreviewUrl,
  // 字典
  getSystemTypes,
  getInspectionTypes,
  getEquipmentCategories,
  getDeptList,
  // 故障报修
  getRepairList,
  getMyReportedRepairs,
  getMyAssignedRepairs,
  getRepairDetail,
  addRepair,
  editRepair,
  deleteRepair,
  dispatchRepair,
  completeRepair,
  getRepairStats,
  getEquipmentByCompanyAndSystem,
  // 文件
  uploadFile,
  // 基础
  BASE_URL: utils_request.BASE_URL,
  getToken: utils_request.getToken
};
exports.api = api;
exports.getMyCompanyList = getMyCompanyList;
exports.switchCompany = switchCompany;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/index.js.map
