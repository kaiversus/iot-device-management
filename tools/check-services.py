"""Read local config; validate SMTP login and Cloudinary credentials without sending mail/uploading."""
import base64,json,smtplib,ssl,urllib.request,urllib.error
from pathlib import Path
config=dict(line.split('=',1) for line in Path('config/application-local.properties').read_text(encoding='utf-8').splitlines() if '=' in line and not line.startswith('#'))
try:
 with smtplib.SMTP('smtp.gmail.com',587,timeout=15) as smtp:
  smtp.ehlo();smtp.starttls(context=ssl.create_default_context());smtp.ehlo()
  smtp.login(config['spring.mail.username'],config['spring.mail.password'])
 print('SMTP: authentication successful (no message sent)')
except smtplib.SMTPAuthenticationError as e: print('SMTP: authentication rejected, code',e.smtp_code)
except Exception as e: print('SMTP: failed',type(e).__name__)
try:
 auth=base64.b64encode((config['cloudinary.api-key']+':'+config['cloudinary.api-secret']).encode()).decode()
 req=urllib.request.Request('https://api.cloudinary.com/v1_1/'+config['cloudinary.cloud-name']+'/resources/image?max_results=1',headers={'Authorization':'Basic '+auth})
 with urllib.request.urlopen(req,timeout=20) as r:
  json.load(r);print('Cloudinary: authentication successful (no resource changed)')
except urllib.error.HTTPError as e: print('Cloudinary: rejected, HTTP',e.code)
except Exception as e: print('Cloudinary: failed',type(e).__name__)
