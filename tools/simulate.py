#!/usr/bin/env python3
"""Giả lập thiết bị IoT gửi dữ liệu ngẫu nhiên. Chỉ dùng thư viện chuẩn.

Ví dụ:  python tools/simulate.py --devices 1 2 3 --interval 2 --hot-chance 0.15
"""
import argparse, json, random, time, urllib.request, urllib.error

ap = argparse.ArgumentParser()
ap.add_argument("--url", default="http://localhost:8080/api/iot/data")
ap.add_argument("--devices", type=int, nargs="+", default=[1])
ap.add_argument("--interval", type=float, default=2.0, help="giây giữa mỗi lần gửi")
ap.add_argument("--hot-chance", type=float, default=0.1, help="xác suất gửi nhiệt độ > 50 để test cảnh báo")
ap.add_argument("--count", type=int, default=0, help="0 = chạy mãi (Ctrl+C để dừng)")
a = ap.parse_args()

n = 0
while a.count == 0 or n < a.count:
    for dev in a.devices:
        hot = random.random() < a.hot_chance
        body = {
            "deviceId": dev,
            "temperature": round(random.uniform(51, 65) if hot else random.uniform(25, 40), 1),
            "humidity": round(random.uniform(40, 80), 1),
        }
        req = urllib.request.Request(a.url, json.dumps(body).encode(), {"Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=5) as r:
                print(r.status, body)
        except urllib.error.HTTPError as e:
            print("HTTP", e.code, e.read().decode()[:200])
        except Exception as e:
            print("Lỗi:", e)
    n += 1
    time.sleep(a.interval)
