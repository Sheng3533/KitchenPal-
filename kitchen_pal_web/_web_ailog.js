async page => {
  await page.goto('http://127.0.0.1:5173/login', { waitUntil: 'networkidle' });
  await page.fill('#account', '13800138000');
  await page.fill('#password', '123456');
  await page.click('button[type=submit]');
  await page.waitForTimeout(3000);
  await page.goto('http://127.0.0.1:5173/ai-logs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2500);
  await page.screenshot({ path: 'shot_ailogs_fixed.png' });
  // 抓取前 3 行的耗时/置信度文本
  const rows = await page.evaluate(() => {
    return [...document.querySelectorAll('table tbody tr')].slice(0,3).map(tr => {
      const tds = [...tr.querySelectorAll('td')].map(td => td.innerText.trim());
      return { 食材: tds[2], 时间: tds[3], 耗时: tds[4], 置信度: tds[5], 状态: tds[6] };
    });
  });
  return rows;
}
