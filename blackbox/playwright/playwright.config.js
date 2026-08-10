const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  timeout: 30000,
  retries: 0,
  use: {
    headless: true,
    baseURL: process.env.BASE_URL || 'http://127.0.0.1:8089',
  },
});
