"""Exercise real Cloudinary image upload/replacement/deletion through the local app."""
import base64,json,secrets,urllib.request,urllib.error,urllib.parse,io
from pathlib import Path
from http.cookiejar import CookieJar
from PIL import Image
cfg=dict(l.split('=',1) for l in Path('config/application-local.properties').read_text(encoding='utf-8').splitlines() if '=' in l and not l.startswith('#'))
jar=CookieJar();client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar));base='http://localhost:8080'
def call(path,method='GET',body=None,expected=200,content='application/json'):
 headers={'Content-Type':content,'X-XSRF-TOKEN':next((c.value for c in jar if c.name=='XSRF-TOKEN'),'')}
 data=body if isinstance(body,bytes) else None if body is None else json.dumps(body).encode()
 with client.open(urllib.request.Request(base+path,data=data,headers=headers,method=method),timeout=45) as r:
  assert r.status==expected,(path,r.status)
  raw=r.read();return json.loads(raw) if raw and 'json' in r.headers.get('Content-Type','') else raw
call('/login');call('/api/auth/login','POST',{'email':cfg['app.bootstrap.email'],'password':cfg['app.bootstrap.password']})
def upload(device,color):
 image=io.BytesIO();Image.new('RGB',(32,32),color).save(image,format='PNG')
 boundary='test'+secrets.token_hex(12)
 body=(f'--{boundary}\r\nContent-Disposition: form-data; name="image"; filename="smoke.png"\r\nContent-Type: image/png\r\n\r\n').encode()+image.getvalue()+f'\r\n--{boundary}--\r\n'.encode()
 return call(f'/api/devices/{device}/image','POST',body,content='multipart/form-data; boundary='+boundary)['imageUrl']
def assert_removed(url):
 public_id=url.split('/upload/',1)[1].split('/',1)[1].rsplit('.',1)[0]
 auth=base64.b64encode((cfg['cloudinary.api-key']+':'+cfg['cloudinary.api-secret']).encode()).decode()
 endpoint='https://api.cloudinary.com/v1_1/'+cfg['cloudinary.cloud-name']+'/resources/image/upload/'+urllib.parse.quote(public_id,safe='')
 try:
  urllib.request.urlopen(urllib.request.Request(endpoint,headers={'Authorization':'Basic '+auth}),timeout=20).close()
  raise AssertionError('Cloudinary resource still exists')
 except urllib.error.HTTPError as e:assert e.code==404,e.code
suffix=secrets.token_hex(5);cat=None;dev=None
try:
 cat=call('/api/categories','POST',{'name':'Image test '+suffix},201)['id']
 dev=call('/api/devices','POST',{'name':'Image test '+suffix,'serialNumber':'IMG-'+suffix,'categoryId':cat,'status':'ACTIVE'},201)['id']
 first=upload(dev,'blue');print('Real upload passed')
 second=upload(dev,'green');assert first!=second;assert_removed(first);print('Replacement and old image cleanup passed')
 call('/devices/'+str(dev));call('/devices')
 call('/api/devices/'+str(dev),'DELETE',expected=204);dev=None
 assert_removed(second);print('Device deletion and final image cleanup passed')
finally:
 if dev:call('/api/devices/'+str(dev),'DELETE',expected=204)
 if cat:call('/api/categories/'+str(cat),'DELETE',expected=204)
