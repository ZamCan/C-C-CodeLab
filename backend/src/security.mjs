import {ApiError} from './policy.mjs';
export const now=()=>Math.floor(Date.now()/1000);
export const encode=bytes=>btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/=/g,'').replace(/\+/g,'-').replace(/\//g,'_');
export function decode(text){if(typeof text!=='string'||!text.length||!/^[A-Za-z0-9_-]+$/.test(text)||text.length%4===1)throw new ApiError(400,'invalid_encoding');return Uint8Array.from(atob(text.replace(/-/g,'+').replace(/_/g,'/')),c=>c.charCodeAt(0));}
export const randomToken=()=>encode(crypto.getRandomValues(new Uint8Array(32)));
export async function digest(text){return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(text))),b=>b.toString(16).padStart(2,'0')).join('');}
export async function readJson(request,limit=65536){
 if(!request.headers.get('content-type')?.startsWith('application/json'))throw new ApiError(415,'json_required');
 const reader=request.body?.getReader();if(!reader)throw new ApiError(400,'body_required');let size=0;const parts=[];
 for(;;){const {done,value}=await reader.read();if(done)break;size+=value.length;if(size>limit){await reader.cancel();throw new ApiError(413,'body_too_large');}parts.push(value);}
 const bytes=new Uint8Array(size);let offset=0;for(const p of parts){bytes.set(p,offset);offset+=p.length;}
 try{const data=JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(bytes));if(!data||Array.isArray(data)||typeof data!=='object')throw Error();return data;}catch{throw new ApiError(400,'invalid_json');}
}
export function origin(env){let url;try{url=new URL(env.PUBLIC_ORIGIN);}catch{throw new ApiError(503,'public_origin_not_configured');}if(url.protocol!=='https:'||url.origin!==env.PUBLIC_ORIGIN)throw new ApiError(503,'invalid_public_origin');return url;}
export function sameOrigin(request,env){if(request.headers.get('origin')!==origin(env).origin)throw new ApiError(403,'origin_rejected');}
export async function limit(request,env,scope,maximum=60){
 if(!env.DB)throw new ApiError(503,'database_not_configured');
 const ip=request.headers.get('CF-Connecting-IP')||'local';
 const bucket=await digest(scope+':'+ip+':'+Math.floor(now()/86400));
 const row=await env.DB.prepare('INSERT INTO edge_limits(bucket,window,requests) VALUES(?,?,1) ON CONFLICT(bucket,window) DO UPDATE SET requests=requests+1 WHERE requests<? RETURNING requests').bind(bucket,Math.floor(now()/60),maximum).first();
 if(!row)throw new ApiError(429,'rate_limited');
}
export async function session(request,env,roles){
 const bearer=request.headers.get('authorization')?.match(/^Bearer ([A-Za-z0-9_-]{43})$/)?.[1];
 const cookie=request.headers.get('cookie')?.match(/(?:^|;\s*)__Host-codelab=([A-Za-z0-9_-]{43})(?:;|$)/)?.[1];
 const token=bearer||cookie;if(!token)throw new ApiError(401,'authentication_required');
 const hash=await digest(token);
 const user=await env.DB.prepare('SELECT l.*,s.csrf_token FROM sessions s JOIN learners l ON l.id=s.learner_id WHERE s.token_hash=? AND s.revoked=0 AND s.expires_at>?').bind(hash,now()).first();
 if(!user)throw new ApiError(401,'invalid_session');
 if(!bearer&&!['GET','HEAD'].includes(request.method)){sameOrigin(request,env);if(!user.csrf_token||request.headers.get('x-csrf-token')!==user.csrf_token)throw new ApiError(403,'csrf_rejected');}
 if(roles&&!roles.includes(user.role))throw new ApiError(403,'role_required');
 user.sessionHash=hash;return user;
}
export async function newSession(env,learner,mobile=false){const token=randomToken(),csrf=randomToken();await env.DB.prepare('INSERT INTO sessions(token_hash,learner_id,expires_at,csrf_token) VALUES(?,?,?,?)').bind(await digest(token),learner,now()+(mobile?604800:28800),csrf).run();return {token,csrf};}
export const sessionCookie=token=>'__Host-codelab='+token+'; Path=/; Secure; HttpOnly; SameSite=Strict; Max-Age=28800';
export function secure(response){const headers=new Headers(response.headers);headers.set('X-Content-Type-Options','nosniff');headers.set('Referrer-Policy','no-referrer');headers.set('X-Frame-Options','DENY');headers.set('Permissions-Policy','camera=(), microphone=(), geolocation=()');headers.set('Content-Security-Policy',"default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; font-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'");headers.set('Strict-Transport-Security','max-age=31536000');return new Response(response.body,{status:response.status,headers});}
