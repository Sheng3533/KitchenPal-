async page => {
  const logs = [];
  page.on('console', m => { if (m.type() === 'error') logs.push('CONSOLE_ERR: ' + m.text()); });
  page.on('pageerror', e => logs.push('PAGE_ERR: ' + e.message));
  const bad = [];
  page.on('response', r => { if (r.status() >= 400) bad.push(r.status() + ' ' + r.url()); });

  await page.goto('http://127.0.0.1:5173/login', { waitUntil: 'networkidle' });
  await page.fill('#account', '13800138000');
  await page.fill('#password', '123456');
  await page.click('button[type=submit]');
  await page.waitForTimeout(3500);
  const path = page.url();
  await page.screenshot({ path: 'shot_dashboard.png', fullPage: false });
  return { path, logs, bad };
}
