import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {PDFDocument} from 'pdf-lib';
import {verify as independentVerify} from 'node:crypto';
import {database} from './d1-helper.mjs';
import worker from '../src/worker.mjs';
import {newSession,now,decode} from '../src/security.mjs';
import {verifyCredential} from '../src/credentials.mjs';

async function fixture(){
 const DB=database(),env={DB,PUBLIC_ORIGIN:'https://academy.example',AUTH_ENABLED:'true',EXAMS_ENABLED:'true',ISSUANCE_ENABLED:'true',ISSUER_KEY_ID:'https://academy.example/#keys/test',ASSETS:{async fetch(request){const path=new URL(request.url).pathname;try{return new Response(readFileSync(new URL('../public'+path,import.meta.url)));}catch{return new Response('',{status:404});}}}};
 const keys=await crypto.subtle.generateKey('Ed25519',true,['sign','verify']),publicJwk=await crypto.subtle.exportKey('jwk',keys.publicKey);env.ISSUER_PRIVATE_JWK=JSON.stringify(await crypto.subtle.exportKey('jwk',keys.privateKey));
 DB.raw.prepare('INSERT INTO issuer_keys(id,public_jwk,status,created_at) VALUES(?,?,?,?)').run(env.ISSUER_KEY_ID,JSON.stringify(publicJwk),'active',now());
 for(const [id,role,verified] of [['student','student',1],['other','student',0],['admin','admin',1]])DB.raw.prepare('INSERT INTO learners(id,account_ref,created_at,display_name,role,identity_verified) VALUES(?,?,?,?,?,?)').run(id,id,now(),'Amina Juma Mkwawa',role,verified);
 DB.raw.prepare('INSERT INTO courses(id,version,criteria_hash,title_en,title_sw) VALUES(?,?,?,?,?)').run('c','1.0','a'.repeat(64),'C Programming','Programu za C');
 DB.raw.prepare('INSERT INTO eligibility VALUES(?,?,?,?)').run('student','c',now(),'reviewed-evidence');
 const slots=[{style:'multiple_choice',objective:'syntax',difficulty:'easy',count:13},{style:'true_false',objective:'syntax',difficulty:'easy',count:12}];
 DB.raw.prepare('INSERT INTO blueprints VALUES(?,?,?,?,?,?,?,?)').run('blueprint','c','1',JSON.stringify(slots),900,70,60,1);
 for(let i=0;i<50;i++)DB.raw.prepare('INSERT INTO questions VALUES(?,?,?,?,?,?,?,?,?,?)').run('q'+i,'c',i<26?'multiple_choice':'true_false','syntax','easy','1','Synthetic test question '+i,JSON.stringify(['Correct test option','Incorrect test option']),0,1);
 const sessions={};for(const id of ['student','other','admin'])sessions[id]=await newSession(env,id);
 async function call(path,{user='student',method='GET',body,cookie=false,headers={}}={}){
   const session=sessions[user];const req=new Request(env.PUBLIC_ORIGIN+path,{method,headers:{...(session?(cookie?{Cookie:'__Host-codelab='+session.token}:{Authorization:'Bearer '+session.token}):{}),...(body?{'Content-Type':'application/json'}:{}),...headers},body:body?JSON.stringify(body):undefined});
   const response=await worker.fetch(req,env);const contentType=response.headers.get('Content-Type')||'';return {response,status:response.status,data:contentType.includes('json')?await response.json():null};
 }
 async function pass(){const start=await call('/v1/exams/attempts',{method:'POST',body:{course_id:'c',official_names:['Amina','Juma','Mkwawa'],name_attested:true}});assert.equal(start.status,200);const id=start.data.id;for(const q of start.data.questions)assert.equal((await call('/v1/exams/attempts/'+id+'/answers/'+q.id,{method:'PUT',body:{answer:0}})).status,200);const submitted=await call('/v1/exams/attempts/'+id+'/submit',{method:'POST',body:{}});assert.equal(submitted.data.score,100);return id;}
 return {env,DB,keys,publicJwk,sessions,call,pass};
}
test('server assessment, idempotent signed issuance, independent verification and PDF',async()=>{
 const f=await fixture();try{
 const attempt=await f.pass(),issued=await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}});assert.equal(issued.status,201);const id=issued.data.id;
 const duplicate=await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}});assert.equal(duplicate.data.id,id);
 const record=f.DB.raw.prepare('SELECT * FROM credentials WHERE id=?').get(id),parts=record.signed_document.split('.');
 assert.ok(independentVerify(null,Buffer.from(parts[0]+'.'+parts[1]),{key:f.publicJwk,format:'jwk'},decode(parts[2])));
 const checked=await verifyCredential(record.signed_document,async()=>({status:'active',jwk:f.publicJwk}),f.env.PUBLIC_ORIGIN);assert.equal(checked.payload.credentialSubject.name,'Amina Juma Mkwawa');
 const status=await f.call('/v1/credentials/'+id+'/status',{user:null});assert.equal(status.data.state,'active');assert.equal(JSON.stringify(status.data).includes('Amina'),false);
 const pdf=await f.call('/v1/credentials/'+id+'/pdf');assert.equal(pdf.status,200);const bytes=await pdf.response.arrayBuffer(),doc=await PDFDocument.load(bytes);assert.equal(doc.getPageCount(),1);assert.ok(bytes.byteLength>10000);
 assert.equal(f.DB.raw.prepare("SELECT COUNT(*) AS count FROM audit_events WHERE event_type='credential_issued'").get().count,1);
 }finally{f.DB.close();}
});
test('cross-account reads and issuance are denied',async()=>{const f=await fixture();try{const attempt=await f.pass();assert.equal((await f.call('/v1/exams/attempts/'+attempt,{user:'other'})).status,404);assert.equal((await f.call('/v1/credentials/issue',{user:'other',method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}})).status,403);const issued=await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}});assert.equal((await f.call('/v1/credentials/'+issued.data.id+'/download',{user:'other'})).status,404);}finally{f.DB.close();}});
test('cookie mutations require same origin and CSRF; role escalation rejected',async()=>{const f=await fixture();try{
 const args={cookie:true,method:'POST',body:{learner_id:'other',course_id:'c',evidence_ref:'reviewed proof'}};
 assert.equal((await f.call('/v1/admin/eligibility',args)).status,403);
 assert.equal((await f.call('/v1/admin/eligibility',{...args,headers:{Origin:f.env.PUBLIC_ORIGIN,'X-CSRF-Token':f.sessions.student.csrf}})).status,403);
 assert.equal((await f.call('/v1/admin/eligibility',{...args,user:'admin',headers:{Origin:f.env.PUBLIC_ORIGIN,'X-CSRF-Token':f.sessions.admin.csrf}})).status,200);
 }finally{f.DB.close();}});
test('revocation is authorized, audited and visible; issued records are immutable',async()=>{const f=await fixture();try{const attempt=await f.pass(),issued=await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}}),id=issued.data.id;
 assert.throws(()=>f.DB.raw.prepare("UPDATE credentials SET course_version='forged' WHERE id=?").run(id),/immutable/);
 assert.throws(()=>f.DB.raw.exec('DELETE FROM audit_events'),/append_only/);
 assert.equal((await f.call('/v1/credentials/'+id+'/revoke',{method:'POST',body:{reason:'Verified appeal review'}})).status,403);
 assert.equal((await f.call('/v1/credentials/'+id+'/revoke',{user:'admin',method:'POST',body:{reason:'Verified appeal review'}})).status,200);
 assert.equal((await f.call('/v1/credentials/'+id+'/status',{user:null})).data.state,'revoked');
 }finally{f.DB.close();}});
test('altered token, unknown key, compromised key and future claims fail verification',async()=>{const f=await fixture();try{const attempt=await f.pass(),issued=await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:attempt,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}}),id=issued.data.id,record=f.DB.raw.prepare('SELECT * FROM credentials WHERE id=?').get(id),parts=record.signed_document.split('.');
 const payload=JSON.parse(Buffer.from(decode(parts[1])).toString());payload.credentialSubject.name='Forged';parts[1]=Buffer.from(JSON.stringify(payload)).toString('base64url');
 await assert.rejects(()=>verifyCredential(parts.join('.'),async()=>({status:'active',jwk:f.publicJwk}),f.env.PUBLIC_ORIGIN),/signature/);
 await assert.rejects(()=>verifyCredential(record.signed_document,async()=>null,f.env.PUBLIC_ORIGIN),/Unknown/);
 await assert.rejects(()=>verifyCredential(record.signed_document,async()=>({status:'compromised',jwk:f.publicJwk}),f.env.PUBLIC_ORIGIN),/compromised/);
 await assert.rejects(()=>verifyCredential(record.signed_document,async()=>({status:'active',jwk:f.publicJwk}),f.env.PUBLIC_ORIGIN,0),/claims/);
 assert.ok(await verifyCredential(record.signed_document,async()=>({status:'retired',jwk:f.publicJwk}),f.env.PUBLIC_ORIGIN));
 }finally{f.DB.close();}});
test('expiry cannot be extended by resume and concurrent creates reuse one attempt',async()=>{const f=await fixture();try{
 const results=await Promise.all([f.call('/v1/exams/attempts',{method:'POST',body:{course_id:'c',official_names:['Amina','Juma','Mkwawa'],name_attested:true}}),f.call('/v1/exams/attempts',{method:'POST',body:{course_id:'c',official_names:['Amina','Juma','Mkwawa'],name_attested:true}})]);assert.equal(results[0].data.id,results[1].data.id);
 const id=results[0].data.id;f.DB.raw.prepare('UPDATE attempts SET deadline_at=? WHERE id=?').run(now()-1,id);
 assert.equal((await f.call('/v1/exams/attempts/'+id+'/submit',{method:'POST',body:{}})).status,410);assert.equal((await f.call('/v1/exams/attempts/'+id)).data.status,'expired');
 }finally{f.DB.close();}});
test('logout revokes session and PKCE code is single-use and verifier bound',async()=>{const f=await fixture();try{
 const verifier='a'.repeat(43),challenge=Buffer.from(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(verifier))).toString('base64url');
 const authorize=await f.call('/v1/mobile/authorize',{method:'POST',body:{challenge},headers:{Origin:f.env.PUBLIC_ORIGIN}});assert.equal(authorize.status,200);
 assert.equal((await f.call('/v1/mobile/exchange',{user:null,method:'POST',body:{code:authorize.data.code,verifier:'b'.repeat(43)}})).status,401);
 const exchange=await f.call('/v1/mobile/exchange',{user:null,method:'POST',body:{code:authorize.data.code,verifier}});assert.equal(exchange.status,200);
 assert.equal((await f.call('/v1/mobile/exchange',{user:null,method:'POST',body:{code:authorize.data.code,verifier}})).status,401);
 assert.equal((await f.call('/v1/auth/logout',{method:'POST',body:{}})).status,200);assert.equal((await f.call('/v1/auth/me')).status,401);
 }finally{f.DB.close();}});
test('passkey challenge rejects wrong origin and replay; service defaults stay disabled',async()=>{const f=await fixture();try{
 assert.equal((await f.call('/v1/auth/register/options',{user:null,method:'POST',body:{name:'Amina'},headers:{Origin:'https://evil.example'}})).status,403);
 const options=await f.call('/v1/auth/register/options',{user:null,method:'POST',body:{name:'Amina'},headers:{Origin:f.env.PUBLIC_ORIGIN}});assert.equal(options.status,200);assert.equal(options.data.options.authenticatorSelection.userVerification,'required');
 const invalid={flow_id:options.data.flow_id,response:{id:'invalid'}};
 assert.equal((await f.call('/v1/auth/register/verify',{user:null,method:'POST',body:invalid,headers:{Origin:f.env.PUBLIC_ORIGIN}})).status,401);
 assert.equal((await f.call('/v1/auth/register/verify',{user:null,method:'POST',body:invalid,headers:{Origin:f.env.PUBLIC_ORIGIN}})).status,409);
 f.env.ISSUANCE_ENABLED='false';assert.equal((await f.call('/v1/credentials/issue',{method:'POST',body:{}})).status,503);
 }finally{f.DB.close();}});

test('real passkey registration and signed login create only a student session',async()=>{
 const {generateKeyPairSync,randomBytes,createHash,sign}=await import('node:crypto');
 const {encodeCBOR}=await import('@levischuck/tiny-cbor');
 const f=await fixture();try{
  const headers={Origin:f.env.PUBLIC_ORIGIN};
  const registration=await f.call('/v1/auth/register/options',{user:null,method:'POST',body:{name:'Test Learner',role:'admin'},headers});assert.equal(registration.status,200);
  const key=generateKeyPairSync('ec',{namedCurve:'prime256v1'}),jwk=key.publicKey.export({format:'jwk'}),credentialId=randomBytes(32),id=credentialId.toString('base64url');
  const cose=encodeCBOR(new Map([[1,2],[3,-7],[-1,1],[-2,Buffer.from(jwk.x,'base64url')],[-3,Buffer.from(jwk.y,'base64url')]]));
  const rpHash=createHash('sha256').update('academy.example').digest();
  const authData=Buffer.concat([rpHash,Buffer.from([0x45,0,0,0,0]),Buffer.alloc(16),Buffer.from([0,32]),credentialId,cose]);
  const clientData=Buffer.from(JSON.stringify({type:'webauthn.create',challenge:registration.data.options.challenge,origin:f.env.PUBLIC_ORIGIN,crossOrigin:false}));
  const attestation=encodeCBOR(new Map([['fmt','none'],['authData',authData],['attStmt',new Map()]]));
  const response={id,rawId:id,type:'public-key',clientExtensionResults:{},response:{clientDataJSON:clientData.toString('base64url'),attestationObject:Buffer.from(attestation).toString('base64url'),transports:['internal']}};
  const registered=await f.call('/v1/auth/register/verify',{user:null,method:'POST',body:{flow_id:registration.data.flow_id,response},headers});assert.equal(registered.status,200);assert.match(registered.response.headers.get('set-cookie'),/Secure; HttpOnly; SameSite=Strict/);
  const learner=f.DB.raw.prepare('SELECT l.* FROM learners l JOIN passkeys p ON p.learner_id=l.id WHERE p.id=?').get(id);assert.equal(learner.role,'student');assert.equal(learner.identity_verified,0);
  const login=await f.call('/v1/auth/login/options',{user:null,method:'POST',body:{},headers});
  const loginClient=Buffer.from(JSON.stringify({type:'webauthn.get',challenge:login.data.options.challenge,origin:f.env.PUBLIC_ORIGIN,crossOrigin:false}));
  const loginAuth=Buffer.concat([rpHash,Buffer.from([0x05,0,0,0,1])]);
  const signature=sign('sha256',Buffer.concat([loginAuth,createHash('sha256').update(loginClient).digest()]),key.privateKey);
  const assertion={id,rawId:id,type:'public-key',clientExtensionResults:{},response:{clientDataJSON:loginClient.toString('base64url'),authenticatorData:loginAuth.toString('base64url'),signature:signature.toString('base64url'),userHandle:registration.data.options.user.id}};
  const signedIn=await f.call('/v1/auth/login/verify',{user:null,method:'POST',body:{flow_id:login.data.flow_id,response:assertion},headers});assert.equal(signedIn.status,200);assert.equal(f.DB.raw.prepare('SELECT counter FROM passkeys WHERE id=?').get(id).counter,1);
  assert.equal((await f.call('/v1/auth/login/verify',{user:null,method:'POST',body:{flow_id:login.data.flow_id,response:assertion},headers})).status,409);
 }finally{f.DB.close();}
});

test('official names required before exam and reconfirmed before issuance',async()=>{const f=await fixture();try{
 assert.equal((await f.call('/v1/exams/attempts',{method:'POST',body:{course_id:'c'}})).status,400);
 assert.equal((await f.call('/v1/exams/attempts',{method:'POST',body:{course_id:'c',official_names:['Wrong','Juma','Mkwawa'],name_attested:true}})).status,403);
 const id=await f.pass();
 assert.equal((await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:id}})).status,400);
 assert.equal((await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:id,official_names:['Wrong','Juma','Mkwawa'],name_confirmed:true}})).status,400);
 assert.equal(f.DB.raw.prepare('SELECT COUNT(*) AS n FROM credentials').get().n,0);
 assert.throws(()=>f.DB.raw.prepare('UPDATE blueprints SET pass_percent=69').run(),/must_be_70/);
 assert.throws(()=>f.DB.raw.prepare("UPDATE attempts SET official_name='Changed' WHERE id=?").run(id),/immutable/);
 assert.equal((await f.call('/v1/credentials/issue',{method:'POST',body:{attempt_id:id,official_names:['Amina','Juma','Mkwawa'],name_confirmed:true}})).status,201);
 assert.ok(f.DB.raw.prepare('SELECT name_confirmed_at FROM attempts WHERE id=?').get(id).name_confirmed_at);
 }finally{f.DB.close();}});
