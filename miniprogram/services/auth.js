const TOKEN_KEY = "token";
const USER_KEY = "userInfo";

function getToken() {
  try {
    return wx.getStorageSync(TOKEN_KEY) || "";
  } catch (e) {
    return "";
  }
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token || "");
}

function removeToken() {
  try {
    wx.removeStorageSync(TOKEN_KEY);
  } catch (e) {}
}

function getUser() {
  try {
    return wx.getStorageSync(USER_KEY) || null;
  } catch (e) {
    return null;
  }
}

function setUser(user) {
  if (user) {
    wx.setStorageSync(USER_KEY, user);
  } else {
    try {
      wx.removeStorageSync(USER_KEY);
    } catch (e) {}
  }
}

function clearSession() {
  removeToken();
  setUser(null);
  try {
    wx.removeStorageSync("currentCompanyId");
  } catch (e) {}
}

function isLoggedIn() {
  return !!getToken();
}

module.exports = {
  TOKEN_KEY,
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  clearSession,
  isLoggedIn
};
