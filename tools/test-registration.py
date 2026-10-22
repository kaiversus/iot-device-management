import sys
sys.stdout.reconfigure(encoding="utf-8")
import json,secrets,urllib.request,urllib.error
from pathlib import Path
from http.cookiejar import CookieJar
cfg=dict(l.split('=',1) for l in Path('config/application-local.properties').read_text(encoding='utf-8').splitlines() if '=' in l)
jar=CookieJar();client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar));client.open('http://localhost:8080/register').close()
csrf=next(c.value for c in jar if c.name=='XSRF-TOKEN')
credentials={'email':cfg['spring.mail.username'],'password':secrets.token_urlsafe(15),'fullName':'Thiên Bảo'}
Path('.local').mkdir(exist_ok=True)
record=Path('.local/registration-test.json')
if record.exists():
 print('Registration already attempted; use existing record and resend flow.');raise SystemExit(0)
r=urllib.request.Request('http://localhost:8080/api/auth/register',json.dumps(credentials).encode(),{'Content-Type':'application/json','X-XSRF-TOKEN':csrf})
try:
 with client.open(r,timeout=30) as response:
  print('Registration HTTP',response.status);response.read()
 record.write_text(json.dumps(credentials,ensure_ascii=False),encoding='utf-8')
 print('OTP sent through application; local test credentials saved (not printed).')
except urllib.error.HTTPError as e:print('Registration HTTP',e.code,e.read().decode())
