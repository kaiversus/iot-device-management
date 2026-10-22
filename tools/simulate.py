#!/usr/bin/env python3
"""Giả lập thiết bị IoT gửi dữ liệu ngẫu nhiên. Chỉ dùng thư viện chuẩn.

Ví dụ:  python tools/simulate.py --devices 1 2 3 --interval 2 --hot-chance 0.15
"""
import argparse, json, random, time, urllib.request, urllib.error, os
from http.cookiejar import CookieJar

ap = argparse.ArgumentParser()
ap.add_argument("--url", default="http://localhost:8080/api/iot/data")
ap.add_argument("--devices", type=int, nargs="+", default=[1])
ap.add_argument("--interval", type=float, default=2.0, help="giây giữa mỗi lần gửi")
ap.add_argument("--hot-chance", type=float, default=0.1, help="xác suất gửi nhiệt độ > 50 để test cảnh báo")
ap.add_argument("--count", type=int, default=0, help="0 = chạy mãi (Ctrl+C để dừng)")
ap.add_argument("--email", help="Email tài khoản sở hữu thiết bị; mật khẩu đọc từ IOT_PASSWORD")
a = ap.parse_args()
if a.interval < 0 or a.count < 0 or not 0 <= a.hot_chance <= 1 or any(d < 1 for d in a.devices):
    ap.error("Tham số mô phỏng không hợp lệ")
if not a.email or not os.environ.get("IOT_PASSWORD"):
    ap.error("Cần --email và biến môi trường IOT_PASSWORD")
from urllib.parse import urlsplit
base = urlsplit(a.url)
origin = base.scheme+"://"+base.netloc
cookies = CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookies))
login = urllib.request.Request(origin+"/api/auth/login", json.dumps({"email":a.email,"password":os.environ["IOT_PASSWORD"]}).encode(), {"Content-Type":"application/json"})
try:
    opener.open(origin+"/login",timeout=10).close()
    csrf = next((c.value for c in cookies if c.name=="XSRF-TOKEN"), "")
    login.add_header("X-XSRF-TOKEN",csrf)
    opener.open(login, timeout=10).close()
    opener.open(origin+"/dashboard", timeout=10).close()
except urllib.error.URLError as e:
    ap.error("Đăng nhập/kết nối thất bại: "+str(e))
csrf = next((c.value for c in cookies if c.name=="XSRF-TOKEN"), "")

n = 0
while a.count == 0 or n < a.count:
    for dev in a.devices:
        hot = random.random() < a.hot_chance
        body = {
            "deviceId": dev,
            "temperature": round(random.uniform(51, 65) if hot else random.uniform(25, 40), 1),
            "humidity": round(random.uniform(40, 80), 1),
        }
        req = urllib.request.Request(a.url, json.dumps(body).encode(), {"Content-Type": "application/json", "X-XSRF-TOKEN": csrf})
        try:
            with opener.open(req, timeout=5) as r:
                print(r.status, body)
        except urllib.error.HTTPError as e:
            print("HTTP", e.code, e.read().decode()[:200])
        except Exception as e:
            print("Lỗi:", e)
    n += 1
    time.sleep(a.interval)
