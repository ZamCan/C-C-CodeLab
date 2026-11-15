/* Research-only probe of a pinned external toolchain. Not an Android runner. */
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const {Worker, isMainThread, parentPort, workerData} = require('node:worker_threads');
const root = isMainThread ? process.argv[2] : workerData.root;
if (!root) throw new Error('Usage: node tools/probe_wasm_toolchain.cjs /path/to/binji-wasm-clang');

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

if (isMainThread) {
  (async()=>{
    const pinned={
      clang:'2a466f0e990329d3230b869d04fc20803eae96a7feb3a3f6c93e25a77b8aed1d',
      lld:'36419ed202011765222098d7701218378b67f634d50f0a4625059ae2c9860f48',
      memfs:'2c72ee42bd9430029dda8c6bafc9f37143f6fe88d5f1ea950a70259ab748bcfe',
      'sysroot.tar':'2435a7b549af30c2be7ec249c405bc2e911ab0c6003012f0909ec3c131bff867',
      'shared.js':'4de0be862bda1a7571c1f0632abe3c62c47a56e734cc6b703155805a4b160b1c'
    };
    const manifest={};
    for(const name of ['clang','lld','memfs','sysroot.tar','shared.js']) {
      const data=fs.readFileSync(path.join(root,name));
      manifest[name]={bytes:data.length,sha256:crypto.createHash('sha256').update(data).digest('hex')};
    }
    for(const [name,digest] of Object.entries(pinned))assert.equal(manifest[name].sha256,digest,'Unreviewed toolchain artifact: '+name);
    console.log(JSON.stringify({candidate:'binji/wasm-clang',commit:'648c4a89997a351eef75cdaec3ef5b89d4937dec',files:manifest},null,2));
    for(const test of cases) {
      await new Promise((resolve,reject)=>{
        const worker=new Worker(__filename,{workerData:{root,test}});
        let ended=false;
        let timer=setTimeout(()=>finish(new Error('Compiler setup exceeded 120 seconds')),120000);
        function finish(error){if(ended)return;ended=true;clearTimeout(timer);worker.terminate().then(()=>error?reject(error):resolve());}
        worker.on('message',message=>{
          if(message.phase==='execute') {clearTimeout(timer);timer=setTimeout(()=>test.timeout?finish():finish(new Error('Unexpected program timeout')),2000);}
          else if(message.error)finish(new Error(test.name+': '+message.error));
          else if(message.done)finish(test.timeout?new Error('Loop unexpectedly returned'):null);
        });
        worker.on('error',finish);
        worker.on('exit',code=>{if(!ended)finish(new Error('Worker exited prematurely: '+code));});
      });
      console.log('PASS: '+test.name);
    }
    console.log('Research probe passed. This is NOT Android WebView, APK integration, or a security certification.');
  })().catch(error=>{console.error(error);process.exitCode=1;});
} else {
  let diagnostics='';
  (async()=>{
    const {test}=workerData;
    let output='',phase='compile';
    const sandbox={WebAssembly,console:{log(){}},TextEncoder,TextDecoder,setTimeout,clearTimeout};
    vm.createContext(sandbox);
    vm.runInContext(fs.readFileSync(path.join(root,'shared.js'),'utf8')+'\nglobalThis.ToolchainAPI=API;',sandbox);
    const modules=new Map();
    const api=new sandbox.ToolchainAPI({
      readBuffer:async name=>new Uint8Array(fs.readFileSync(path.join(root,name))).buffer,
      compileStreaming:async name=>{
        if(!modules.has(name))modules.set(name,await WebAssembly.compile(fs.readFileSync(path.join(root,name))));
        return modules.get(name);
      },
      hostWrite:text=>{if(phase!=='run')diagnostics=(diagnostics+text).slice(-16000);if(phase==='run'){output+=text;if(output.length>65536)throw new Error('Output limit exceeded');}}
    });
    api.hostLog=()=>{};
    await api.ready;
    for(const [name,source] of Object.entries(test.files))api.memfs.addFile(name,source);
    const objects=[];
    try {
      const clang=await api.getModule('clang');
      for(const name of Object.keys(test.files).filter(n=>/\.(c|cpp)$/.test(n))) {
        const object=name+'.o';objects.push(object);
        await api.run(clang,'clang','-cc1','-emit-obj',...api.clangCommonArgs.filter(a=>a!=='-fcolor-diagnostics'),'-O0','-std='+ (test.language==='c'?'c11':'c++17'),'-I.','-o',object,'-x',test.language,name);
      }
    } catch(error) {if(test.compileFailure && error.code===1 && diagnostics.includes('missing_symbol')){parentPort.postMessage({done:true});return;}throw error;}
    if(test.compileFailure)throw new Error('Invalid source unexpectedly compiled');
    const linker=await api.getModule('lld');
    await api.run(linker,'wasm-ld','--no-threads','-z','stack-size=1048576','--max-memory=67108864','-Llib/wasm32-wasi','lib/wasm32-wasi/crt1.o',...objects,'-lc','-lc++','-lc++abi','lib/clang/8.0.1/lib/wasi/libclang_rt.builtins-wasm32.a','-o','program.wasm');
    const module=await WebAssembly.compile(api.memfs.getFileContents('program.wasm'));
    api.memfs.setStdinStr(test.stdin||'');
    phase='run';parentPort.postMessage({phase:'execute'});
    let status=0;
    try {await api.run(module,'program.wasm');}catch(error){if(typeof error.code!=='number')throw error;status=error.code;}
    assert.equal(status,test.exit||0);
    if(!test.exit)assert.equal(output.trimEnd(),test.expected.trimEnd());
    parentPort.postMessage({done:true});
  })().catch(error=>parentPort.postMessage({error:error.stack+'\n'+diagnostics}));
}
