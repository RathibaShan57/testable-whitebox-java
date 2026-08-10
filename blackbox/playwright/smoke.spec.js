// Black Box — Playwright user-journey / API availability smoke
const { test, expect } = require('@playwright/test');

const BASE = process.env.BASE_URL || 'http://127.0.0.1:8089';

test('homepage loads and orders button works', async ({ page }) => {
  await page.goto(BASE + '/');
  await expect(page.locator('h1')).toContainText('Testable');
  await page.click('#go');
  await expect(page.locator('#out')).toContainText('customerId');
});

test('health API available', async ({ request }) => {
  const res = await request.get(BASE + '/health');
  expect(res.status()).toBe(200);
  expect(await res.json()).toMatchObject({ status: 'ok' });
});
