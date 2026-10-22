const {test}=require('node:test');
const assert=require('node:assert/strict');
const vm=require('node:vm');
const fs=require('node:fs');
const source=fs.readFileSync('src/main/resources/static/js/app.js','utf8');
test('history response preserves a newer realtime point and deduplicates matching IDs',async()=>{
 let chart;const pending=[];const subscriptions={};const elements={};
 const element=id=>elements[id] ||= {textContent:'',classList:{add(){},remove(){}},addEventListener(){},replaceChildren(){},appendChild(){},children:[]};
 vm.runInNewContext(source,{
  document:{body:{dataset:{admin:'false'}},getElementById:element,createElement:()=>({appendChild(){},remove(){}})},
  Chart:class {constructor(_,config){this.data=config.data;chart=this;}update(){}},
  SockJS:class {},Stomp:{over:()=>({connect(_,ready){ready();},subscribe(topic,callback){subscriptions[topic]=callback;}})},
  fetch:url=>{
   if(url.startsWith('/api/iot/data'))return new Promise(resolve=>pending.push(resolve));
   return Promise.resolve({ok:true,json:()=>Promise.resolve(url.includes('logs')?[]:{})});
  },setTimeout(){},Date,Map,Number
 });
 subscriptions['/user/queue/data']({body:JSON.stringify({id:2,deviceId:1,temperature:55,humidity:50,time:'2026-10-21T10:00:02Z'})});
 pending.at(-1)({ok:true,json:()=>Promise.resolve([{id:1,deviceId:1,temperature:30,humidity:40,recordedAt:'2026-10-21T10:00:01Z'}])});
 await new Promise(resolve=>setImmediate(resolve));
 assert.deepEqual(Array.from(chart.data.datasets[0].data),[30,55]);
 subscriptions['/user/queue/data']({body:JSON.stringify({id:2,deviceId:1,temperature:55,humidity:50,time:'2026-10-21T10:00:02Z'})});
 assert.deepEqual(Array.from(chart.data.datasets[0].data),[30,55]);
 pending[0]({ok:true,json:()=>Promise.resolve([])});
 await new Promise(resolve=>setImmediate(resolve));
 assert.deepEqual(Array.from(chart.data.datasets[0].data),[30,55]);
});
