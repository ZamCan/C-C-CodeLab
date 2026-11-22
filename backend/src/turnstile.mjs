import {ApiError} from './policy.mjs';
// Cloudflare Turnstile: verify every new examination start at the Worker.
// Tokens are single-use; never trust a client-provided verification flag.
export async function verifyExamChallenge(token,env,ip='',fetcher=globalThis.fetch) {
  if(!env.TURNSTILE_SECRET_KEY || !env.TURNSTILE_SITE_KEY)
    throw new ApiError(503,'exam_challenge_not_configured');
  if(typeof token!=='string' || token.length<10 || token.length>2048)
    throw new ApiError(403,'exam_challenge_required');
  let response;
  try {
    response=await fetcher('https://challenges.cloudflare.com/turnstile/v0/siteverify',{
      method:'POST',
      headers:{'Content-Type':'application/x-www-form-urlencoded'},
      body:new URLSearchParams({secret:env.TURNSTILE_SECRET_KEY,response:token,...(ip?{remoteip:ip}:{})}),
      signal:AbortSignal.timeout(7000)
    });
  }catch{throw new ApiError(503,'exam_challenge_unavailable');}
  if(!response.ok)throw new ApiError(503,'exam_challenge_unavailable');
  let result;
  try{result=await response.json();}catch{throw new ApiError(503,'exam_challenge_unavailable');}
  if(result.success!==true || (env.TURNSTILE_ALLOWED_HOSTNAME &&
      result.hostname!==env.TURNSTILE_ALLOWED_HOSTNAME))
    throw new ApiError(403,'exam_challenge_failed');
  return true;
}
