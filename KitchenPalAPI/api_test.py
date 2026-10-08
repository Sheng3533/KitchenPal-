import urllib.request, urllib.error, urllib.parse, json, base64, io, os

BASE = "http://127.0.0.1:18080"

def req(method, path, data=None, headers=None, raw=None, ctype=None, timeout=30):
    url = BASE + path
    if raw is not None:
        body = raw
    elif data is not None:
        body = json.dumps(data).encode("utf-8")
        ctype = ctype or "application/json"
    else:
        body = None
    h = dict(headers or {})
    if body is not None and ctype:
        h["Content-Type"] = ctype
    r = urllib.request.Request(url, data=body, method=method, headers=h)
    try:
        with urllib.request.urlopen(r, timeout=timeout) as resp:
            text = resp.read().decode("utf-8", "replace")
            return resp.status, text
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")
    except Exception as e:
        return -1, f"ERR {type(e).__name__}: {e}"

results = []
def check(name, method, path, **kw):
    status, text = req(method, path, **kw)
    short = text
    try:
        j = json.loads(text)
        short = json.dumps(j, ensure_ascii=False)
        if isinstance(j, dict):
            # try to summarize
            keys = list(j.keys())
            short = f"keys={keys}"
            if "data" in j:
                d = j["data"]
                if isinstance(d, list):
                    short = f"code={j.get('code')} list_len={len(d)}"
                else:
                    short = f"code={j.get('code')} data_keys={list(d.keys()) if isinstance(d,dict) else type(d).__name__}"
    except Exception:
        short = (text[:200])
    results.append((name, status, short))
    print(f"[{status}] {name}: {short[:300]}")
    return status, text

print("===== GET 列表/详情 =====")
check("recipe/list", "GET", "/api/recipe/list")
check("recipe/hero", "GET", "/api/recipe/hero")
check("recipe/categories", "GET", "/api/recipe/categories")
check("recipe/1", "GET", "/api/recipe/1")
check("recipe/category 家常菜", "GET", "/api/recipe/list?category=" + urllib.parse.quote("家常菜"))
check("recipe/keyword 番茄", "GET", "/api/recipe/list?keyword=" + urllib.parse.quote("番茄"))
check("recipe/999 (不存在)", "GET", "/api/recipe/999")
check("user/1", "GET", "/api/user/1")
check("user/list", "GET", "/api/user/list")
check("user/1/profile", "GET", "/api/user/1/profile")
check("favorite/list/1", "GET", "/api/favorite/list/1")
check("favorite/ids/1", "GET", "/api/favorite/ids/1")
check("notification/1", "GET", "/api/notification/1")
check("notification/unread/1", "GET", "/api/notification/unread/1")
check("history/1", "GET", "/api/history/1")
check("stats/user/1", "GET", "/api/stats/user/1")
check("chef/list", "GET", "/api/chef/list")
check("content/published", "GET", "/api/content/published")
check("video/published", "GET", "/api/video/published")
check("feedback/list", "GET", "/api/feedback/list")
check("app/version/check", "GET", "/api/app/version/check?version=1.0.0&platform=android")
check("app/agreement", "GET", "/api/app/agreement")
check("scan/logs/1", "GET", "/api/scan/logs/1")

print("\n===== 写操作：注册/登录 =====")
st, login_text = check("user/login 13800138000", "POST", "/api/user/login", data={"phone":"13800138000","password":"123456"})
# register a new user
st, reg_text = check("user/register 新用户", "POST", "/api/user/register", data={"phone":"13900000001","password":"abc123","nickname":"测试号"})
new_uid = None
try:
    j = json.loads(reg_text)
    d = j.get("data")
    if isinstance(d, dict):
        new_uid = d.get("id")
except Exception:
    pass
print("  新注册用户ID:", new_uid)

print("\n===== 写操作：收藏/历史/反馈/通知 =====")
if new_uid:
    check("favorite/toggle 新增收藏", "POST", "/api/favorite/toggle", data={"userId":new_uid,"recipeId":2})
    check("favorite/list/新用户", "GET", f"/api/favorite/list/{new_uid}")
    check("history/add", "POST", "/api/history/add", data={"userId":new_uid,"recipeId":2,"recipeTitle":"青椒肉丝","cookDate":"今天","cookTime":"12:30","durationMin":15,"score":88,"rating":4})
    check("history/新用户", "GET", f"/api/history/{new_uid}")
    check("notification/read/1", "POST", "/api/notification/read/1")
check("feedback/submit", "POST", "/api/feedback/submit", data={"userId":1,"content":"测试反馈内容","contact":"","type":"建议"})
check("assistant/chat", "POST", "/api/assistant/chat", data={"message":"今天想做番茄炒蛋，给个建议"})

print("\n===== AI 扫描（上传图片，预期 fallback）=====")
# 1x1 PNG
png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M8AAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
png = base64.b64decode(png_b64)
boundary = "----kitchenpaltest"
body = (f"--{boundary}\r\n"
        f"Content-Disposition: form-data; name=\"image\"; filename=\"t.png\"\r\n"
        f"Content-Type: image/png\r\n\r\n").encode() + png + f"\r\n--{boundary}--\r\n".encode()
st, scan_text = check("scan/recognize", "POST", "/api/scan/recognize", raw=body, ctype=f"multipart/form-data; boundary={boundary}", timeout=120)
try:
    j = json.loads(scan_text)
    print("  scan data:", json.dumps(j.get("data"), ensure_ascii=False)[:400])
except Exception:
    print("  scan raw:", scan_text[:300])

# Save summary
with open(r"D:\project\KitchenPal\KitchenPalAPI\test_results.txt","w",encoding="utf-8") as f:
    for n,s,t in results:
        f.write(f"[{s}] {n}\n   {t}\n")
print("\n===== 完成，结果已写入 test_results.txt =====")
