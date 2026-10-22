(() => {
 const original=window.fetch.bind(window);
 window.fetch=(url,options={}) => {
  const target=new URL(typeof url==='string'?url:url.url,location.href);
  if(target.origin===location.origin && !['GET','HEAD','OPTIONS'].includes((options.method||'GET').toUpperCase())) {
   const headers=new Headers(options.headers||{});
   const cookie=document.cookie.split('; ').find(v=>v.startsWith('XSRF-TOKEN='));
   if(cookie) headers.set('X-XSRF-TOKEN',decodeURIComponent(cookie.slice(11)));
   options={...options,headers};
  }
  return original(url,options);
 };
})();
