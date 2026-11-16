import {officialName} from './exam-identity.mjs';
import {ApiError} from './policy.mjs';
import {now,origin,encode,decode,digest,session,readJson,limit} from './security.mjs';
const enc=new TextEncoder();
const json=(data,status=200)=>Response.json(data,{status,headers:{'Cache-Control':'no-store'}});
const uuid=/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;
export function publicKey(jwk){if(jwk.kty!=='OKP'||jwk.crv!=='Ed25519'||typeof jwk.x!=='string'||decode(jwk.x).length!==32)throw new Error('Invalid Ed25519 key');return {kty:'OKP',crv:'Ed25519',x:jwk.x};}
export async function signCredential(payload,jwk,kid){
 publicKey(jwk);if(typeof jwk.d!=='string')throw new Error('Private signing key unavailable');
 const key=await crypto.subtle.importKey('jwk',jwk,'Ed25519',false,['sign']);
 const input=encode(enc.encode(JSON.stringify({alg:'EdDSA',typ:'vc+jwt',cty:'vc',kid})))+'.'+encode(enc.encode(JSON.stringify(payload)));
 return input+'.'+encode(await crypto.subtle.sign('Ed25519',key,enc.encode(input)));
}
export async function verifyCredential(token,resolveKey,expectedIssuer,time=Date.now()){
 if(typeof token!=='string'||token.length>32768)throw new Error('Invalid credential');
 const parts=token.split('.');if(parts.length!==3)throw new Error('Invalid envelope');
 const parse=value=>JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(decode(value)));
 const header=parse(parts[0]);
 if(header.alg!=='EdDSA'||header.typ!=='vc+jwt'||header.cty!=='vc'||typeof header.kid!=='string'||Object.keys(header).some(k=>!['alg','typ','cty','kid'].includes(k)))throw new Error('Unsupported proof');
 if(!header.kid.startsWith(expectedIssuer+'/#keys/'))throw new Error('Untrusted key identifier');
 const record=await resolveKey(header.kid);if(!record||!['active','retired'].includes(record.status))throw new Error('Unknown or compromised key');
 const key=await crypto.subtle.importKey('jwk',publicKey(record.jwk),'Ed25519',false,['verify']);
 if(!await crypto.subtle.verify('Ed25519',key,decode(parts[2]),enc.encode(parts[0]+'.'+parts[1])))throw new Error('Invalid signature');
 const payload=parse(parts[1]),issued=Date.parse(payload.validFrom);
 if(payload.issuer?.id!==expectedIssuer||!payload.id?.startsWith(expectedIssuer+'/v1/credentials/')||payload['@context']?.[0]!=='https://www.w3.org/ns/credentials/v2'||!payload.type?.includes('VerifiableCredential')||!payload.credentialSubject?.id||!Number.isFinite(issued)||issued>time+60000)throw new Error('Invalid credential claims');
 if(payload.validUntil&&(!Number.isFinite(Date.parse(payload.validUntil))||Date.parse(payload.validUntil)<=time))throw new Error('Expired credential');
 return {payload,keyId:header.kid};
}
export async function recordState(env,record){
 if(!record)return {state:'invalid'};
 try{
   if(await digest(record.signed_document)!==record.digest)throw Error('Digest mismatch');
   const checked=await verifyCredential(record.signed_document,async id=>{const key=await env.DB.prepare('SELECT * FROM issuer_keys WHERE id=?').bind(id).first();return key?{status:key.status,jwk:JSON.parse(key.public_jwk)}:null;},origin(env).origin);
   if(checked.keyId!==record.key_id||checked.payload.id!==origin(env).origin+'/v1/credentials/'+record.id||checked.payload.credentialSubject.courseId!==record.course_id||checked.payload.credentialSubject.courseVersion!==record.course_version)throw Error('Record mismatch');
   return {state:record.status==='revoked'?'revoked':'active',payload:checked.payload};
 }catch{return {state:'invalid'};}
}
async function issue(request,env){
 if(env.ISSUANCE_ENABLED!=='true')throw new ApiError(503,'issuance_not_enabled');
 const learner=await session(request,env),input=await readJson(request);
 if(!uuid.test(input.attempt_id||''))throw new ApiError(400,'invalid_attempt');
 const attempt=await env.DB.prepare("SELECT a.*,c.version AS course_version,c.criteria_hash,c.title_en,c.title_sw FROM attempts a JOIN courses c ON c.id=a.course_id JOIN eligibility e ON e.course_id=a.course_id AND e.learner_id=a.learner_id WHERE a.id=? AND a.learner_id=? AND a.status='submitted' AND a.passed=1").bind(input.attempt_id,learner.id).first();
 if(!attempt||learner.identity_verified!==1)throw new ApiError(403,'verified_identity_and_pass_required');
 if(!attempt.official_name||attempt.official_name!==learner.display_name.normalize('NFC').trim())throw new ApiError(403,'exam_identity_review_required');
 if(input.name_confirmed!==true||officialName(input.official_names)!==attempt.official_name)throw new ApiError(400,'certificate_name_confirmation_required');
 const prior=await env.DB.prepare('SELECT id,status FROM credentials WHERE attempt_id=?').bind(attempt.id).first();if(prior)return json(prior);
 if(!/^[a-f0-9]{64}$/.test(attempt.criteria_hash)||!attempt.title_en||!attempt.title_sw)throw new ApiError(503,'reviewed_course_metadata_required');
 const issuer=origin(env).origin,kid=env.ISSUER_KEY_ID;
 if(typeof kid!=='string'||!kid.startsWith(issuer+'/#keys/'))throw new ApiError(503,'issuer_key_not_configured');
 const record=await env.DB.prepare("SELECT public_jwk FROM issuer_keys WHERE id=? AND status='active'").bind(kid).first();if(!record||!env.ISSUER_PRIVATE_JWK)throw new ApiError(503,'issuer_key_not_configured');
 const privateKey=JSON.parse(env.ISSUER_PRIVATE_JWK),published=publicKey(JSON.parse(record.public_jwk));
 if(publicKey(privateKey).x!==published.x)throw new ApiError(503,'issuer_key_mismatch');
 const id=crypto.randomUUID(),issued=now();
 const payload={
   '@context':['https://www.w3.org/ns/credentials/v2',{CodeLabCertificate:issuer+'/schema#CodeLabCertificate',courseId:issuer+'/schema#courseId',courseVersion:issuer+'/schema#courseVersion',criteriaHash:issuer+'/schema#criteriaHash',achievement:issuer+'/schema#achievement'}],
   id:issuer+'/v1/credentials/'+id,type:['VerifiableCredential','CodeLabCertificate'],
   issuer:{id:issuer,name:'ZamCan Academy'},validFrom:new Date(issued*1000).toISOString(),
   name:learner.locale==='sw'?'Cheti cha ufaulu':'Certificate of achievement',
   credentialSubject:{id:'urn:uuid:'+crypto.randomUUID(),name:attempt.official_name,courseId:attempt.course_id,courseVersion:attempt.course_version,criteriaHash:attempt.criteria_hash,achievement:learner.locale==='sw'?attempt.title_sw:attempt.title_en}
 };
 const signed=await signCredential(payload,privateKey,kid);
 await verifyCredential(signed,async()=>({status:'active',jwk:published}),issuer);
 await env.DB.batch([env.DB.prepare("UPDATE attempts SET name_confirmed_at=? WHERE id=?").bind(issued,attempt.id),env.DB.prepare("INSERT INTO credentials(id,attempt_id,learner_id,course_id,course_version,issued_at,key_id,digest,signed_document,status) VALUES(?,?,?,?,?,?,?,?,?,'active') ON CONFLICT(attempt_id) DO NOTHING").bind(id,attempt.id,learner.id,attempt.course_id,attempt.course_version,issued,kid,await digest(signed),signed),env.DB.prepare("INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at) VALUES(?,?,'certificate_name_confirmed',?,?)").bind(crypto.randomUUID(),learner.id,attempt.id,issued)]);
 return json(await env.DB.prepare('SELECT id,status FROM credentials WHERE attempt_id=?').bind(attempt.id).first(),201);
}
export async function credentialRoute(request,env,path){
 if(path==='/.well-known/zamcan-issuer-keys.json'&&request.method==='GET'){
   const {results}=await env.DB.prepare('SELECT id,public_jwk,status FROM issuer_keys').all();
   return json({issuer:origin(env).origin,keys:results.map(k=>({...publicKey(JSON.parse(k.public_jwk)),kid:k.id,alg:'EdDSA',use:'sig',status:k.status}))});
 }
 if(!path.startsWith('/v1/credentials'))return null;
 await limit(request,env,'certificates',60);
 if(path==='/v1/credentials/issue'&&request.method==='POST')return issue(request,env);
 if(path==='/v1/credentials'&&request.method==='GET'){const u=await session(request,env);const {results}=await env.DB.prepare('SELECT id,course_id,course_version,issued_at,status FROM credentials WHERE learner_id=? ORDER BY issued_at DESC LIMIT 100').bind(u.id).all();return json({credentials:results});}
 const match=path.match(/^\/v1\/credentials\/([0-9a-f-]{36})(?:\/(status|download|pdf|revoke))?$/);if(!match||!uuid.test(match[1]))throw new ApiError(404,'not_found');
 const record=await env.DB.prepare('SELECT * FROM credentials WHERE id=?').bind(match[1]).first();
 if(match[2]==='status'&&request.method==='GET'){const checked=await recordState(env,record);return json({id:match[1],state:checked.state,...(checked.payload?{issuer:'ZamCan Academy',course:checked.payload.credentialSubject.achievement,issued_at:checked.payload.validFrom}: {})},record?200:404);}
 const user=await session(request,env,match[2]==='revoke'?['admin']:undefined);
 if(!record||(match[2]!=='revoke'&&record.learner_id!==user.id))throw new ApiError(404,'not_found');
 if(match[2]==='revoke'&&request.method==='POST'){
   const data=await readJson(request);if(typeof data.reason!=='string'||data.reason.trim().length<5||data.reason.length>300)throw new ApiError(400,'reason_required');
   await env.DB.batch([
     env.DB.prepare("UPDATE credentials SET status='revoked',revoked_at=? WHERE id=? AND status='active'").bind(now(),record.id),
     env.DB.prepare("INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at,metadata_json) VALUES(?,?,'revocation_requested',?,?,?)").bind(crypto.randomUUID(),user.id,record.id,now(),JSON.stringify({reason:data.reason.trim()}))
   ]);return json({id:record.id,state:'revoked'});
 }
 if(request.method==='GET'&&(match[2]==='download'||match[2]==='pdf')){
   const checked=await recordState(env,record);if(checked.state==='invalid')throw new ApiError(409,'credential_integrity_failed');
   if(match[2]==='pdf'){const {certificatePdf}=await import('./pdf.mjs');return new Response(await certificatePdf(checked.payload,record.signed_document,checked.state,env),{headers:{'Content-Type':'application/pdf','Content-Disposition':'attachment; filename="ZamCan-'+record.id+'.pdf"','Cache-Control':'no-store'}});}
   return new Response(record.signed_document,{headers:{'Content-Type':'application/vc+jwt','Content-Disposition':'attachment; filename="ZamCan-'+record.id+'.vc.jwt"','Cache-Control':'no-store'}});
 }
 throw new ApiError(405,'method_not_allowed');
}
