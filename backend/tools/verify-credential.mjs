import {readFileSync} from 'node:fs';
import {verifyCredential} from '../src/credentials.mjs';
const [credentialFile,keyFile,issuer]=process.argv.slice(2);
if(!credentialFile||!keyFile||!issuer)throw Error('Usage: node tools/verify-credential.mjs credential.vc.jwt trusted-issuer-keys.json https://trusted-issuer');
const keys=JSON.parse(readFileSync(keyFile,'utf8')).keys;
const result=await verifyCredential(readFileSync(credentialFile,'utf8').trim(),async kid=>{const key=keys.find(k=>k.kid===kid);return key?{status:key.status,jwk:key}:null;},issuer);
console.log(JSON.stringify({signature:'valid',credential_id:result.payload.id,issuer:result.payload.issuer.id,current_status:'offline_not_checked'},null,2));
