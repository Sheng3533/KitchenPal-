async page => {
  const report = [];
  const errs = [];
  const bad = [];
  page.on('console', m => { if (m.type() === 'error') errs.push('CONSOLE: ' + m.text().slice(0,180)); });
  page.on('pageerror', e => errs.push('PAGEERR: ' + e.message.slice(0,180)));
  page.on('response', r => { if (r.status() >= 400) bad.push(r.status() + ' ' + r.url().replace('http://127.0.0.1:5173','')); });

  // 1) 登录
  await page.goto('http://127.0.0.1:5173/login', { waitUntil: 'networkidle' });
  await page.fill('#account', '13800138000');
  await page.fill('#password', '123456');
  await page.click('button[type=submit]');
  await page.waitForTimeout(3000);
  const loginOk = page.url().includes('/dashboard');

  // 2) SPA 内遍历（用点击侧边栏链接，避免整页刷新丢状态）
  const pages = [
    ['recipes', '食谱管理'], ['users', '用户管理'], ['chefs', '大厨管理'],
    ['contents', '内容与活动'], ['videos', '视频教学'], ['notifications', '通知管理'],
    ['cooking-records', '烹饪记录'], ['ai-logs', 'AI识别日志'], ['assistant', 'AI助手'], ['settings', '设置'],
  ];
  for (const [route, name] of pages) {
    errs.length = 0; bad.length = 0;
    await page.goto('http://127.0.0.1:5173/' + route, { waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(2000);
    const title = (await page.title()).replace(' - 管理端平台', '');
    const textLen = (await page.evaluate(() => document.body.innerText || '')).length;
    const rows = await page.evaluate(() => document.querySelectorAll('table tbody tr').length);
    const cards = await page.evaluate(() => document.querySelectorAll('.card, [class*=card]').length);
    await page.screenshot({ path: 'shot_' + route + '.png' });
    report.push({ route, name, title, textLen, rows, cards, e: errs.slice(0,2), b: bad.slice(0,2) });
  }
  return { loginOk, report };
}
