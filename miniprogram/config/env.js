const ENV = "dev";

const CONFIG = {
  dev: {
    // Prefer IPv4 loopback: WeChat DevTools on Windows may hang resolving "localhost" via IPv6.
    BASE_URL: "http://127.0.0.1:83",
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
