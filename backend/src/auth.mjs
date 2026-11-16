import {generateRegistrationOptions,verifyRegistrationResponse,generateAuthenticationOptions,verifyAuthenticationResponse} from '@simplewebauthn/server';
import {ApiError} from './policy.mjs';
import {now,origin,sameOrigin,limit,readJson,randomToken,encode,decode,session,newSession,sessionCookie,digest} from './security.mjs';
const json=(data,headers={})=>Response.json(data,{headers:{'Cache-Control':'no-store',...headers}});
export async function authRoute(request,env,path){
 if(!path.startsWith('/v1/auth/')&&!path.startsWith('/v1/mobile/'))return null;
 if(env.AUTH_ENABLED!=='true')throw new ApiError(503,'authentication_not_enabled');
 await limit(request,env,'auth',20);
 const rp=origin(env);
 if(path==='/v1/auth/me'&&request.method==='GET'){const u=await session(request,env);return json({id:u.id,name:u.display_name,locale:u.locale,role:u.role,identity_verified:u.identity_verified===1,csrf:u.csrf_token});}
 if(path==='/v1/auth/logout'&&request.method==='POST'){const u=await session(request,env);await env.DB.prepare('UPDATE sessions SET revoked=1 WHERE token_hash=?').bind(u.sessionHash).run();return json({signed_out:true},{'Set-Cookie':'__Host-codelab=; Path=/; Secure; HttpOnly; SameSite=Strict; Max-Age=0'});}
 if(request.method!=='POST')throw new ApiError(405,'method_not_allowed');
 if(path!=='/v1/mobile/exchange')sameOrigin(request,env);
 const data=await readJson(request);
 if(path==='/v1/auth/register/options'||path==='/v1/auth/login/options'){
   const register=path.includes('/register/');let pending={};let options;
   if(register){
     await limit(request,env,'register',3);
     const name=typeof data.name==='string'?data.name.normalize('NFC').trim():'';
     if(name.length<2||name.length>100||/[\u0000-\u001f\u007f]/.test(name))throw new ApiError(400,'invalid_name');
     pending={id:crypto.randomUUID(),name,locale:data.locale==='sw'?'sw':'en'};
     options=await generateRegistrationOptions({rpName:'ZamCan Academy',rpID:rp.hostname,userID:new TextEncoder().encode(pending.id),userName:'learner-'+pending.id,userDisplayName:name,attestationType:'none',authenticatorSelection:{residentKey:'required',userVerification:'required'}});
   }else options=await generateAuthenticationOptions({rpID:rp.hostname,userVerification:'required'});
   const id=randomToken();await env.DB.prepare('INSERT INTO auth_challenges(id,challenge,purpose,pending_json,expires_at) VALUES(?,?,?,?,?)').bind(id,options.challenge,register?'register':'login',JSON.stringify(pending),now()+300).run();
   return json({flow_id:id,options});
 }
 if(path==='/v1/auth/register/verify'||path==='/v1/auth/login/verify'){
   const purpose=path.includes('/register/')?'register':'login';
   if(typeof data.flow_id!=='string'||!data.response||typeof data.response.id!=='string')throw new ApiError(400,'invalid_assertion');
   // Atomic consume before verification: an assertion/challenge cannot be replayed or raced.
   const flow=await env.DB.prepare('DELETE FROM auth_challenges WHERE id=? AND purpose=? AND expires_at>? RETURNING *').bind(data.flow_id,purpose,now()).first();
   if(!flow)throw new ApiError(409,'challenge_expired_or_used');
   let learner;
   if(purpose==='register'){
     let result;try{result=await verifyRegistrationResponse({response:data.response,expectedChallenge:flow.challenge,expectedOrigin:rp.origin,expectedRPID:rp.hostname,requireUserVerification:true});}catch{throw new ApiError(401,'passkey_verification_failed');}
     if(!result.verified||!result.registrationInfo)throw new ApiError(401,'passkey_verification_failed');
     const {credential}=result.registrationInfo,pending=JSON.parse(flow.pending_json);learner=pending.id;
     await env.DB.batch([
       env.DB.prepare('INSERT INTO learners(id,account_ref,created_at,display_name,locale) VALUES(?,?,?,?,?)').bind(learner,'passkey:'+learner,now(),pending.name,pending.locale),
       env.DB.prepare('INSERT INTO passkeys(id,learner_id,public_key,counter,transports_json,created_at) VALUES(?,?,?,?,?,?)').bind(credential.id,learner,encode(credential.publicKey),credential.counter,JSON.stringify(credential.transports||[]),now()),
       env.DB.prepare("INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at) VALUES(?,?,'account_registered',?,?)").bind(crypto.randomUUID(),learner,learner,now())
     ]);
   }else{
     const key=await env.DB.prepare('SELECT * FROM passkeys WHERE id=?').bind(data.response.id).first();if(!key)throw new ApiError(401,'passkey_verification_failed');
     let result;try{result=await verifyAuthenticationResponse({response:data.response,expectedChallenge:flow.challenge,expectedOrigin:rp.origin,expectedRPID:rp.hostname,requireUserVerification:true,credential:{id:key.id,publicKey:decode(key.public_key),counter:key.counter,transports:JSON.parse(key.transports_json)}});}catch{throw new ApiError(401,'passkey_verification_failed');}
     if(!result.verified)throw new ApiError(401,'passkey_verification_failed');
     const updated=await env.DB.prepare('UPDATE passkeys SET counter=? WHERE id=? AND counter=? RETURNING learner_id').bind(result.authenticationInfo.newCounter,key.id,key.counter).first();if(!updated)throw new ApiError(409,'authentication_changed');learner=key.learner_id;
   }
   const issued=await newSession(env,learner);return json({authenticated:true,csrf:issued.csrf},{'Set-Cookie':sessionCookie(issued.token)});
 }
 if(path==='/v1/mobile/authorize'){
   const user=await session(request,env);
   if(!/^[A-Za-z0-9_-]{43}$/.test(data.challenge||''))throw new ApiError(400,'invalid_pkce_challenge');
   const code=randomToken();await env.DB.prepare('INSERT INTO mobile_codes(code_hash,learner_id,challenge,expires_at) VALUES(?,?,?,?)').bind(await digest(code),user.id,data.challenge,now()+120).run();return json({code,expires_in:120});
 }
 if(path==='/v1/mobile/exchange'){
   if(!/^[A-Za-z0-9_-]{43}$/.test(data.code||'')||!/^[A-Za-z0-9._~-]{43,128}$/.test(data.verifier||''))throw new ApiError(400,'invalid_pkce_exchange');
   const challenge=encode(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(data.verifier)));
   const row=await env.DB.prepare('DELETE FROM mobile_codes WHERE code_hash=? AND challenge=? AND expires_at>? RETURNING learner_id').bind(await digest(data.code),challenge,now()).first();if(!row)throw new ApiError(401,'invalid_or_expired_code');
   const issued=await newSession(env,row.learner_id,true);return json({access_token:issued.token,token_type:'Bearer',expires_in:604800});
 }
 throw new ApiError(404,'not_found');
}
