import {build} from 'esbuild';
await build({entryPoints:['frontend/main.js'],bundle:true,format:'esm',target:['es2022'],outfile:'public/app.js',minify:true,sourcemap:false,legalComments:'eof'});
console.log('Built bilingual dashboard with locally bundled passkey and verification code.');
