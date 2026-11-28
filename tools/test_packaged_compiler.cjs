const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm'),zlib=require('node:zlib'),assert=require('node:assert/strict');
const {Worker,isMainThread,parentPort,workerData}=require('node:worker_threads');
const root=path.resolve(__dirname,'../app/src/main/assets/compiler');
// Catch regressions in Android WebView's logical asset URL contract.
const workerSource=fs.readFileSync(path.join(root,'worker.js'),'utf8');
assert.match(workerSource,/fetch\(name, \{credentials:'omit'\}\)/);
assert.doesNotMatch(workerSource,/fetch\(assetPath/);
const cases = [
  {name:'C11 standard headers, math and stdin',language:'c',stdin:'9\n',files:{'main.c':'#include <stdio.h>\n#include <stdlib.h>\n#include <string.h>\n#include <math.h>\nint main(void){double x; if(scanf("%lf",&x)!=1)return 2; char b[8];strcpy(b,"value");printf("%s %.0f %d\\n",b,sqrt(x),atoi("42"));return 0;}'},expected:'value 3 42\n'},
  {name:'C++17 containers, strings, templates and algorithms',language:'c++',files:{'main.cpp':'#include <iostream>\n#include <vector>\n#include <string>\n#include <map>\n#include <algorithm>\n#include <numeric>\ntemplate<class T>T twice(T x){return x+x;} int main(){std::vector<int> v{3,1,2};std::sort(v.begin(),v.end());std::map<std::string,int> m{{"sum",std::accumulate(v.begin(),v.end(),0)}};std::cout<<v.front()<<" "<<m["sum"]<<" "<<twice(4)<<"\\n";}'},expected:'1 6 8\n'},
  {name:'C++17 RAII and smart pointers',language:'c++',files:{'main.cpp':'#include <iostream>\n#include <memory>\nstruct Item{int n;explicit Item(int x):n(x){}};int main(){auto p=std::make_unique<Item>(42);std::cout<<p->n<<"\\n";}'},expected:'42\n'},
  {name:'C multi-file linking and project headers',language:'c',files:{'calc.h':'int add(int,int);','calc.c':'#include "calc.h"\nint add(int a,int b){return a+b;}','main.c':'#include <stdio.h>\n#include "calc.h"\nint main(void){printf("%d\\n",add(2,5));}'},expected:'7\n'},
  {name:'Virtual file round trip',language:'c',files:{'main.c':'#include <stdio.h>\nint main(void){FILE*f=fopen("record.txt","w");if(!f)return 2;fputs("saved",f);fclose(f);char b[8]={0};f=fopen("record.txt","r");if(!f)return 3;fgets(b,8,f);fclose(f);puts(b);}'},expected:'saved\n'},
  {name:'Host filesystem is not mounted',language:'c',files:{'main.c':'#include <stdio.h>\nint main(void){FILE*f=fopen("/etc/passwd","r");if(f){fclose(f);return 1;}puts("isolated");}'},expected:'isolated\n'},
  {name:'Compiler diagnostic rejects syntax error',language:'c',files:{'main.c':'int main(void) { return missing_symbol; }'},compileFailure:true},
  {name:'Real nonzero program status',language:'c',files:{'main.c':'int main(void){return 7;}'},exit:7,expected:''},
  {name:'Infinite loop is terminated by supervisor',language:'c',files:{'main.c':'int main(void){for(;;){} }'},timeout:true},
];

cases.push({name:'Separate stdout and stderr',files:{'main.c':'#include <stdio.h>\nint main(void){fputs("out",stdout);fputs("err",stderr);return 0;}'},expected:'out',stderr:'err'});
cases.push({name:'UTF-8 input and output',files:{'main.c':'#include <stdio.h>\nint main(void){char b[32];fgets(b,32,stdin);printf("%s",b);}'},stdin:'Habari 世界\n',expected:'Habari 世界\n'});
cases.push({name:'Output flood stops at budget',files:{'main.c':'#include <stdio.h>\nint main(void){for(;;)puts("output flood");}'},trap:'Output limit'});
cases.push({name:'Memory exhaustion is bounded',files:{'main.c':'#include <stdlib.h>\n#include <stdio.h>\nint main(void){void*p=malloc(128*1024*1024);puts(p?"unexpected":"bounded");}'},expected:'bounded'});
cases.push({name:'C++17 multiple translation units',files:{'sum.hpp':'int sum(int,int);','sum.cpp':'#include "sum.hpp"\nint sum(int a,int b){return a+b;}','main.cpp':'#include <iostream>\n#include "sum.hpp"\nint main(){std::cout<<sum(20,22);}'},expected:'42'});
cases.push({"name":"Scientific calculator with C++ mathematical library","files":{"main.cpp":"#include <iostream>\n#include <cmath>\nint main(){double x;std::cin>>x;std::cout<<std::pow(x,2)<<\" \"<<std::sqrt(x)<<\" \"<<std::round(std::sin(0.0))<<\" \"<<std::log10(100.0)<<\" \"<<std::ceil(2.1)<<\"\\n\";}"},"stdin":"9\n","expected":"81 3 0 2 3\n"});
cases.push({"name":"ATM menu with PIN, deposits, withdrawals and balance","files":{"main.cpp":"#include <iostream>\nint main(){int pin,choice,amount,balance=1000;std::cin>>pin;if(pin!=1234){std::cout<<\"Denied\";return 0;}while(std::cin>>choice && choice!=0){switch(choice){case 1:std::cout<<balance<<\"\\n\";break;case 2:std::cin>>amount;if(amount>0)balance+=amount;break;case 3:std::cin>>amount;if(amount>0&&amount<=balance)balance-=amount;else std::cout<<\"Rejected\\n\";break;}}}"},"stdin":"1234\n2 500\n3 200\n3 2000\n1\n0\n","expected":"Rejected\n1300\n"});
cases.push({"name":"C++ full line and token input","files":{"main.cpp":"#include <iostream>\n#include <string>\nint main(){std::string name,word;std::getline(std::cin,name);std::cin>>word;std::cout<<name<<\"|\"<<word<<\"\\n\";}"},"stdin":"Amina Ali\nHabari\n","expected":"Amina Ali|Habari\n"});
// --curriculum: compile and run every authored lesson example through the packaged engine.
if(process.argv.includes('--curriculum')){
  const dir=path.join(__dirname,'../app/src/main/assets/curriculum');
  cases.length=0;
  for(const language of ['c','cpp'])for(const file of fs.readdirSync(path.join(dir,language)).sort()){
    if(!/\.(c|cpp)$/.test(file))continue;
    const source=fs.readFileSync(path.join(dir,language,file),'utf8');
    const match=/\/\*\s*expect:\n([\s\S]*?)\*\//.exec(source);
    if(!match)throw new Error('missing expect block: '+file);
    const expected=match[1].split('\n').map(line=>line.replace(/\s+$/,'')).join('\n').replace(/\n+$/,'');
    const stdinPath=path.join(dir,language,file.replace(/\.(c|cpp)$/,'.stdin'));
    cases.push({name:'lesson '+language+'/'+file,files:{[file]:source},
      stdin:fs.existsSync(stdinPath)?fs.readFileSync(stdinPath,'utf8'):'',expected});
  }
}

if(isMainThread){(async()=>{const only=process.argv[2]&&!process.argv[2].startsWith('--')?[cases[Number(process.argv[2])]]:cases;for(const test of only){await new Promise((resolve,reject)=>{
 const w=new Worker(__filename,{workerData:test});let ended=false;
 let timer=setTimeout(()=>done(new Error('Compile timeout')),120000);
 function done(e){if(ended)return;ended=true;clearTimeout(timer);w.terminate().then(()=>e?reject(e):resolve());}
 w.on('message',m=>{if(m.phase==='run'){clearTimeout(timer);timer=setTimeout(()=>done(test.timeout?null:new Error('Run timeout')),3000);}else if(m.done)done();else if(m.error)done(new Error(m.error));});
 w.on('error',done);w.on('exit',code=>{if(!ended)done(new Error('Unexpected worker exit '+code));});
 });console.log('PASS: '+test.name);}console.log('PASS: '+(process.argv[2]&&!process.argv[2].startsWith('--')?1:cases.length)+' packaged-engine integration checks. Android WebView verification remains pending.');})().catch(e=>{console.error(e);process.exitCode=1;});}
else{(async()=>{
 const sandbox={WebAssembly,TextEncoder,TextDecoder,console:{log(){}},setTimeout,clearTimeout};vm.createContext(sandbox);
 vm.runInContext(fs.readFileSync(path.join(root,'shared.js'),'utf8')+'\n'+fs.readFileSync(path.join(root,'engine.js'),'utf8')+'\nglobalThis.runEngine=compileProject;',sandbox);
 let stdout='',stderr='',diagnostics='',result;
 try{await sandbox.runEngine({files:workerData.files,stdin:workerData.stdin||''},async name=>{let b=name==='empty.tar'?fs.readFileSync(path.join(root,name)):zlib.gunzipSync(fs.readFileSync(path.join(root,name+'.gz')));return new Uint8Array(b).buffer;},m=>{
  if(m.type==='phase'&&m.phase==='run')parentPort.postMessage({phase:'run'});
  if(m.type==='output'){if(m.stream==='stdout')stdout+=m.text;else if(m.stream==='stderr')stderr+=m.text;else diagnostics+=m.text;}
  if(m.type==='result')result=m;
 });}catch(e){if(workerData.compileFailure&&e.code===1&&diagnostics.includes('missing_symbol')){parentPort.postMessage({done:true});return;}throw Error(e.stack+'\n'+diagnostics);}
 assert.ok(result,'Missing result');
 if(workerData.trap)assert.ok(result.trap&&result.trap.includes(workerData.trap),JSON.stringify(result));
 else{assert.equal(result.trap,null);assert.equal(result.exitCode,workerData.exit||0);assert.equal(stdout.trimEnd(),(workerData.expected||'').trimEnd());assert.equal(stderr,workerData.stderr||'');}
 parentPort.postMessage({done:true});
})().catch(e=>parentPort.postMessage({error:workerData.name+': '+e.stack}));}
