async page => {
  const errs = [];
  page.on('console', m => { if (m.type() === 'error') errs.push(m.text().slice(0,150)); });

  await page.goto('http://127.0.0.1:5173/login', { waitUntil: 'networkidle' });
  await page.fill('#account', '13800138000');
  await page.fill('#password', '123456');
  await page.click('button[type=submit]');
  await page.waitForTimeout(3000);
  await page.goto('http://127.0.0.1:5173/assistant', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2500);

  await page.fill('input[placeholder*="输入您的问题"]', '番茄炒蛋怎么做？');
  await page.press('input[placeholder*="输入您的问题"]', 'Enter');
  await page.waitForTimeout(90000);
  const t = await page.evaluate(() => document.body.innerText);
  await page.screenshot({ path: 'shot_ast_chat.png' });
  return { len: t.length, errs: errs.slice(0,3), tail: t.slice(-500) };
}
