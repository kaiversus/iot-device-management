"""Non-destructive smoke checks: creates uniquely named fixtures and cleans only those fixtures."""
import json,os,secrets,subprocess,urllib.request,urllib.error
from http.cookiejar import CookieJar
from pathlib import Path
cfg=dict(l.split('=',1) for l in Path('config/application-local.properties').read_text(encoding='utf-8').splitlines() if '=' in l and not l.startswith('#'))
base='http://localhost:8080';jar=CookieJar();client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
def call(path,method='GET',body=None,expected=200):
 headers={'Content-Type':'application/json'}
 csrf=next((c.value for c in jar if c.name=='XSRF-TOKEN'),'')
 if csrf: headers['X-XSRF-TOKEN']=csrf
 request=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
 try:
  with client.open(request,timeout=20) as r: code=r.status;raw=r.read();typ=r.headers.get('Content-Type','')
 except urllib.error.HTTPError as r: code=r.code;raw=r.read();typ=r.headers.get('Content-Type','')
 assert code==expected,(method,path,code,raw.decode()[:250])
 print(method,path,code)
 return json.loads(raw) if 'json' in typ and raw else raw
call('/login');call('/api/auth/login','POST',{'email':cfg['app.bootstrap.email'],'password':cfg['app.bootstrap.password']})
for path in ['/devices','/dashboard','/account','/management/users','/admin/settings','/api/account','/api/admin/logs','/api/management/users']:call(path)
suffix=secrets.token_hex(4);cat=None;dev=None;supplier=None
try:
 cat=call('/api/categories','POST',{'name':'Smoke '+suffix},201)['id']
 body={'name':'Smoke '+suffix,'serialNumber':'SMOKE-'+suffix,'categoryId':cat,'status':'ACTIVE'}
 dev=call('/api/devices','POST',body,201)['id']
 call('/api/devices?q='+suffix)
 body['name']='Updated '+suffix;call('/api/devices/'+str(dev),'PUT',body)
 call('/api/iot/data','POST',{'deviceId':dev,'temperature':60,'humidity':50})
 assert len(call('/api/iot/data?deviceId='+str(dev)))==1
 call('/api/iot/stats');call('/api/iot/logs')
 call('/api/devices/'+str(dev),'DELETE',expected=409)
 supplier=call('/api/admin/suppliers','POST',{'name':'Smoke '+suffix},201)['id']
 call('/api/admin/suppliers/'+str(supplier),'PUT',{'name':'Updated '+suffix,'contact':'test-only'})
 print('MYSQL HTTP SMOKE PASSED')
finally:
 if dev:
  env=os.environ.copy();env['MYSQL_PWD']=cfg['spring.datasource.password']
  # Exact fixture device id only; leave all pre-existing records untouched.
  subprocess.run(['mysql','-u','root','-h','127.0.0.1','iot_management','-e',f'DELETE FROM device_logs WHERE device_id={int(dev)}; DELETE FROM device_data WHERE device_id={int(dev)};'],env=env,check=True)
  call('/api/devices/'+str(dev),'DELETE',expected=204)
 if cat:call('/api/categories/'+str(cat),'DELETE',expected=204)
 if supplier:call('/api/admin/suppliers/'+str(supplier),'DELETE',expected=204)
