(() => {
const $=id=>document.getElementById(id);
const notice=(text,error=false)=>{const el=$('message');el.textContent=text;el.className='alert '+(error?'alert-danger':'alert-success');};
const run=fn=>async event=>{event?.preventDefault();try{await fn();}catch(e){notice(e.message,true);}};
async function api(url,method='GET',body) {
 const r=await fetch(url,{method,headers:{'Content-Type':'application/json'},...(body?{body:JSON.stringify(body)}:{})});
 if(r.status===204)return;
 const data=await r.json();if(!r.ok)throw new Error(data.error||Object.values(data).join(', ')||'Không thể thực hiện');return data;
}
function cell(tr,text) {const td=document.createElement('td');td.textContent=text??'—';tr.append(td);return td;}
function button(td,label,action) {const b=document.createElement('button');b.type='button';b.className='btn btn-sm btn-outline-secondary me-2';b.textContent=label;b.onclick=run(action);td.append(b);}
if($('profileForm')) {
 run(async()=>{const u=await api('/api/account');$('fullName').value=u.fullName;$('accountInfo').textContent=u.email+' · '+u.roles.join(', ')+' · '+(u.region||'Chưa có khu vực');})();
 $('profileForm').onsubmit=run(async()=>{await api('/api/account','PUT',{fullName:$('fullName').value});notice('Đã lưu thông tin.');});
 $('passwordForm').onsubmit=run(async()=>{const result=await api('/api/account/password','POST',{currentPassword:$('currentPassword').value,newPassword:$('newPassword').value});notice(result.message);setTimeout(()=>location.href='/login',1500);});
}
if($('usersBody')) {
 let page=0,admin=false,currentUserId=null;
 async function load() {const data=await api('/api/management/users?q='+encodeURIComponent($('query').value)+'&page='+page);$('usersBody').replaceChildren();
  for(const u of data.content) {const tr=document.createElement('tr');[u.email,u.fullName,u.region,u.roles.join(', '),u.locked?'Đã khóa':u.enabled?'Đã xác thực':'Chưa xác thực'].forEach(v=>cell(tr,v));
   const actions=cell(tr,'');
   if(admin || u.roles.every(r=>r==='ROLE_USER'))button(actions,'Sửa',()=>{ $('editUser').hidden=false;$('userId').value=u.id;$('userName').value=u.fullName;$('userRegion').value=u.region||'';$('userRole').value=u.roles[0];$('userLocked').checked=u.locked;$('userRegion').disabled=!admin;$('userRole').disabled=!admin;});
   if(u.id!==currentUserId && !u.roles.includes('ROLE_ADMIN') && (admin || u.roles.every(r=>r==='ROLE_USER')))
    button(actions,'Xóa',async()=>{if(!confirm('Xóa vĩnh viễn tài khoản '+u.email+' cùng toàn bộ thiết bị, dữ liệu đo và cảnh báo của tài khoản này?'))return;await api('/api/management/users/'+u.id,'DELETE');if($('userId').value===String(u.id))$('editUser').hidden=true;notice('Đã xóa vĩnh viễn tài khoản '+u.email+'.');await load();});
   $('usersBody').append(tr);
  }
  $('userPage').textContent='Trang '+(page+1)+' · '+data.totalElements+' người dùng';$('prevUsers').disabled=page===0;$('nextUsers').disabled=data.last;
 }
 run(async()=>{const me=await api('/api/account');admin=me.roles.includes('ROLE_ADMIN');currentUserId=me.id;$('newRegion').defaultValue=me.region||'';$('newRegion').value=me.region||'';$('newRegion').readOnly=!admin;$('newRole').disabled=!admin;await load();})();
 $('showCreateUser').onclick=()=>{$('createUser').hidden=false;$('newEmail').focus();};
 $('cancelCreateUser').onclick=()=>{$('createUser').reset();$('createUser').hidden=true;};
 $('createUser').onsubmit=run(async()=>{
  const created=await api('/api/management/users','POST',{email:$('newEmail').value,fullName:$('newName').value,password:$('newPassword').value,region:$('newRegion').value,role:$('newRole').value});
  $('createUser').reset();$('createUser').hidden=true;page=0;$('query').value='';notice('Đã tạo tài khoản '+created.email+'.');await load();
 });
 $('searchUsers').onsubmit=run(async()=>{page=0;await load();});$('prevUsers').onclick=run(async()=>{page--;await load();});$('nextUsers').onclick=run(async()=>{page++;await load();});
 $('editUser').onsubmit=run(async()=>{await api('/api/management/users/'+$('userId').value,'PUT',{fullName:$('userName').value,region:$('userRegion').value,role:$('userRole').value,locked:$('userLocked').checked});$('editUser').hidden=true;notice('Đã cập nhật; các phiên đăng nhập cũ của tài khoản đã bị thu hồi.');await load();});
}
if($('settingsForm')) {
 let page=0;
 async function logs() {const d=await api('/api/admin/logs?page='+page);$('logsBody').replaceChildren();for(const l of d.content){const tr=document.createElement('tr');[new Date(l.createdAt).toLocaleString(),l.actor,l.action,l.detail].forEach(v=>cell(tr,v));$('logsBody').append(tr);} $('prevLogs').disabled=page===0;$('nextLogs').disabled=d.last;$('logPage').textContent='Trang '+(page+1);}
 async function suppliers() {const list=await api('/api/admin/suppliers');$('suppliersBody').replaceChildren();for(const s of list){const tr=document.createElement('tr');cell(tr,s.name);cell(tr,s.contact);const td=cell(tr,'');button(td,'Sửa',()=>{ $('supplierId').value=s.id;$('supplierName').value=s.name;$('supplierContact').value=s.contact||'';$('supplierDescription').value=s.description||'';});button(td,'Xóa',async()=>{if(confirm('Xóa nhà cung cấp này?')){await api('/api/admin/suppliers/'+s.id,'DELETE');await suppliers();await logs();}});$('suppliersBody').append(tr);}}
 run(async()=>{$('threshold').value=(await api('/api/admin/settings')).threshold;await suppliers();await logs();})();
 $('settingsForm').onsubmit=run(async()=>{await api('/api/admin/settings','PUT',{threshold:Number($('threshold').value)});notice('Đã lưu ngưỡng cảnh báo.');await logs();});
 $('supplierForm').onsubmit=run(async()=>{const id=$('supplierId').value;await api('/api/admin/suppliers'+(id?'/'+id:''),id?'PUT':'POST',{name:$('supplierName').value,contact:$('supplierContact').value,description:$('supplierDescription').value});$('supplierForm').reset();$('supplierId').value='';notice('Đã lưu nhà cung cấp.');await suppliers();await logs();});
 $('prevLogs').onclick=run(async()=>{page--;await logs();});$('nextLogs').onclick=run(async()=>{page++;await logs();});
}
})();
