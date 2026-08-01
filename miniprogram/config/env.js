const ENV = "dev";

const CONFIG = {
  dev: {
    // WeChat DevTools is more reliable with localhost than 127.0.0.1 on some setups
    BASE_URL: "http://localhost:83",
    ENV_NAME: "dev"
  },
  prod: {
    BASE_URL: "https://huixiaobao-admin.site",
    ENV_NAME: "prod"
  }
};

const current = CONFIG[ENV] || CONFIG.dev;

module.exports = {
  ENV,
  BASE_URL: current.BASE_URL,
  ENV_NAME: current.ENV_NAME
};
