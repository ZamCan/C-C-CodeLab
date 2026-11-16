import {officialName} from './exam-identity.mjs';
import {authRoute} from './auth.mjs';
import {credentialRoute} from './credentials.mjs';
import {dashboardRoute} from './dashboard.mjs';
import {session,limit,secure} from './security.mjs';
import { ApiError, selectQuestions, assertLive, grade, publicQuestion } from './policy.mjs';

const json = (data, status = 200) => Response.json(data, {status, headers:{'Cache-Control':'no-store','X-Content-Type-Options':'nosniff'}});
const now = () => Math.floor(Date.now() / 1000);
const idPattern = /^[A-Za-z0-9_-]{1,100}$/;
async function body(request) {
  if (!request.headers.get('content-type')?.startsWith('application/json')) throw new ApiError(415,'json_required');
  const reader=request.body?.getReader(); if (!reader) throw new ApiError(400,'body_required');
  const chunks=[]; let size=0;
  for (;;) { const {done,value}=await reader.read(); if(done)break;size+=value.length;if(size>16384){await reader.cancel();throw new ApiError(413,'body_too_large');}chunks.push(value); }
  const bytes=new Uint8Array(size);let position=0;for(const chunk of chunks){bytes.set(chunk,position);position+=chunk.length;}
  try { const result=JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(bytes));if(!result||Array.isArray(result)||typeof result!=='object')throw Error();return result; }
  catch {throw new ApiError(400,'invalid_json');}
}
async function authenticate(request,env) {
  const user=await session(request,env);
  const quota=await env.DB.prepare('INSERT INTO rate_limits(session_hash,window,requests) VALUES(?,?,1) ON CONFLICT(session_hash,window) DO UPDATE SET requests=requests+1 WHERE requests<60 RETURNING requests').bind(user.sessionHash,Math.floor(now()/60)).first();
  if(!quota)throw new ApiError(429,'rate_limited');return user.id;
}
async function ownAttempt(env,id,learner) {
  const attempt=await env.DB.prepare('SELECT * FROM attempts WHERE id=? AND learner_id=?').bind(id,learner).first();
  if(!attempt)throw new ApiError(404,'attempt_not_found');return attempt;
}
async function resume(env,attempt) {
  const time=now();
  if(attempt.status==='active' && time>=attempt.deadline_at){await env.DB.prepare("UPDATE attempts SET status='expired',finalized_at=? WHERE id=? AND status='active' AND deadline_at<=?").bind(time,attempt.id,time).run();attempt=await env.DB.prepare('SELECT * FROM attempts WHERE id=?').bind(attempt.id).first();}
  const {results}=await env.DB.prepare('SELECT question_id,prompt,choices_json,style FROM attempt_questions WHERE attempt_id=? ORDER BY position').bind(attempt.id).all();
  return json({id:attempt.id,course_id:attempt.course_id,status:attempt.status,server_time:time,deadline_at:attempt.deadline_at,official_name:attempt.official_name,pass_percent:attempt.pass_percent,score:attempt.score,passed:attempt.passed===1,answers:JSON.parse(attempt.answers_json),questions:results.map(publicQuestion)});
}
async function createAttempt(request,env,learner) {
  const input=await body(request);if(!idPattern.test(input.course_id??''))throw new ApiError(400,'invalid_course');
  const time=now();
  // Expire stale attempts before checking the one-active-attempt uniqueness constraint.
  // Otherwise a timed-out attempt can permanently block a learner's next attempt.
  await env.DB.prepare("UPDATE attempts SET status='expired',finalized_at=? WHERE learner_id=? AND course_id=? AND status='active' AND deadline_at<=?").bind(time,learner,input.course_id,time).run();
  const active=await env.DB.prepare("SELECT * FROM attempts WHERE learner_id=? AND course_id=? AND status='active'").bind(learner,input.course_id).first();
  if(active)return resume(env,active);
  const name=officialName(input.official_names);
  if(input.name_attested!==true)throw new ApiError(400,'official_name_attestation_required');
  const identity=await env.DB.prepare('SELECT display_name,identity_verified FROM learners WHERE id=?').bind(learner).first();
  if(identity?.identity_verified!==1||identity.display_name.normalize('NFC').trim()!==name)throw new ApiError(403,'verified_official_name_required');
  const eligible=await env.DB.prepare('SELECT 1 AS authorized FROM eligibility WHERE learner_id=? AND course_id=?').bind(learner,input.course_id).first();
  if(!eligible)throw new ApiError(403,'trusted_eligibility_required');
  const blueprint=await env.DB.prepare('SELECT * FROM blueprints WHERE course_id=? AND active=1').bind(input.course_id).first();
  if(blueprint&&blueprint.pass_percent!==70)throw new ApiError(503,'pass_percent_must_be_70');
  if(!blueprint)throw new ApiError(503,'reviewed_blueprint_required');
  const latest=await env.DB.prepare('SELECT finalized_at,cooldown_seconds FROM attempts WHERE learner_id=? AND course_id=? ORDER BY started_at DESC LIMIT 1').bind(learner,input.course_id).first();
  if(latest?.finalized_at && time<latest.finalized_at+latest.cooldown_seconds)throw new ApiError(429,'retry_cooldown');
  const {results:exposure}=await env.DB.prepare('SELECT aq.question_id FROM attempt_questions aq JOIN attempts a ON a.id=aq.attempt_id WHERE a.learner_id=?').bind(learner).all();
  const {results:bank}=await env.DB.prepare('SELECT * FROM questions WHERE course_id=? AND active=1 LIMIT 10001').bind(input.course_id).all();
  if(bank.length>10000)throw new ApiError(503,'bank_partition_required');
  const selected=selectQuestions(bank,JSON.parse(blueprint.slots_json),new Set(exposure.map(q=>q.question_id)));
  const id=crypto.randomUUID(),deadline=time+blueprint.duration_seconds;
  const statements=[env.DB.prepare("INSERT INTO attempts(id,learner_id,course_id,blueprint_id,policy_version,started_at,deadline_at,status,pass_percent,cooldown_seconds,official_name) VALUES(?,?,?,?,?,?,?,'active',?,?,?)").bind(id,learner,input.course_id,blueprint.id,blueprint.version,time,deadline,70,blueprint.cooldown_seconds,name)];
  selected.forEach((q,index)=>statements.push(env.DB.prepare('INSERT INTO attempt_questions(attempt_id,question_id,position,prompt,choices_json,correct_index,style,objective,difficulty) VALUES(?,?,?,?,?,?,?,?,?)').bind(id,q.id,index,q.prompt,q.choices_json,q.correct_index,q.style,q.objective,q.difficulty)));
  statements.push(env.DB.prepare("INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at) VALUES(?,?,'attempt_started',?,?)").bind(crypto.randomUUID(),learner,id,time));
  try {await env.DB.batch(statements);} catch(error) {const concurrent=await env.DB.prepare("SELECT * FROM attempts WHERE learner_id=? AND course_id=? AND status='active'").bind(learner,input.course_id).first();if(concurrent)return resume(env,concurrent);throw error;}
  return resume(env,await ownAttempt(env,id,learner));
}
async function saveAnswer(request,env,learner,id,questionId) {
  const attempt=await ownAttempt(env,id,learner);assertLive(attempt,now());
  const input=await body(request), question=await env.DB.prepare('SELECT choices_json FROM attempt_questions WHERE attempt_id=? AND question_id=?').bind(id,questionId).first();
  if(!question)throw new ApiError(404,'question_not_found');
  if(!Number.isInteger(input.answer)||input.answer<0||input.answer>=JSON.parse(question.choices_json).length)throw new ApiError(400,'invalid_answer');
  const updated=await env.DB.prepare("UPDATE attempts SET answers_json=json_set(answers_json,?,?) WHERE id=? AND learner_id=? AND status='active' AND deadline_at>? RETURNING id").bind('$."'+questionId+'"',input.answer,id,learner,now()).first();
  if(!updated)throw new ApiError(409,'attempt_closed');return json({saved:true});
}
async function submit(env,learner,id) {
  const attempt=await ownAttempt(env,id,learner);
  if(attempt.status==='submitted')return resume(env,attempt);
  assertLive(attempt,now());
  const {results:questions}=await env.DB.prepare('SELECT question_id,correct_index FROM attempt_questions WHERE attempt_id=?').bind(id).all();
  const result=grade(questions,JSON.parse(attempt.answers_json),attempt.pass_percent),time=now();
  const updated=await env.DB.prepare("UPDATE attempts SET status='submitted',score=?,passed=?,finalized_at=? WHERE id=? AND learner_id=? AND status='active' AND deadline_at>? AND answers_json=? RETURNING id").bind(result.score,result.passed?1:0,time,id,learner,time,attempt.answers_json).first();
  if(!updated){const concurrent=await ownAttempt(env,id,learner);if(concurrent.status==='submitted')return resume(env,concurrent);throw new ApiError(409,'answers_changed_or_attempt_closed');}
  return resume(env,await ownAttempt(env,id,learner));
}
async function route(request,env) {
    try {
      const path=new URL(request.url).pathname;
      if(path==='/health'&&request.method==='GET')return json({service:'zamcan-certification',exams_enabled:env.EXAMS_ENABLED==='true',credential_issuance:env.ISSUANCE_ENABLED==='true',authentication:env.AUTH_ENABLED==='true'});
      if(!path.startsWith('/v1/')&&!path.startsWith('/.well-known/')){
        if(!env.ASSETS)throw new ApiError(503,'dashboard_not_built');
        const url=new URL(request.url);if(path==='/'||path.startsWith('/verify/')||path==='/connect')url.pathname='/index.html';
        return env.ASSETS.fetch(new Request(url,request));
      }
      if(!env.DB)throw new ApiError(503,'database_not_configured');
      await limit(request,env,'api',120);
      for(const handler of [authRoute,credentialRoute,dashboardRoute]){const response=await handler(request,env,path);if(response)return response;}
      if(!env.DB || env.EXAMS_ENABLED!=='true')throw new ApiError(503,'exam_service_not_enabled');
      const learner=await authenticate(request,env);
      if(path==='/v1/exams/attempts'&&request.method==='POST')return await createAttempt(request,env,learner);
      let match=path.match(/^\/v1\/exams\/attempts\/([A-Za-z0-9_-]{1,100})$/);
      if(match&&request.method==='GET')return await resume(env,await ownAttempt(env,match[1],learner));
      match=path.match(/^\/v1\/exams\/attempts\/([A-Za-z0-9_-]{1,100})\/answers\/([A-Za-z0-9_-]{1,100})$/);
      if(match&&request.method==='PUT')return await saveAnswer(request,env,learner,match[1],match[2]);
      match=path.match(/^\/v1\/exams\/attempts\/([A-Za-z0-9_-]{1,100})\/submit$/);
      if(match&&request.method==='POST')return await submit(env,learner,match[1]);
      throw new ApiError(404,'not_found');
    } catch(error) {return json({error:error instanceof ApiError?error.code:'internal_error'},error instanceof ApiError?error.status:500);}
}

export default {
  async fetch(request,env){return secure(await route(request,env));},
  async scheduled(_event,env){
    if(!env.DB)return;
    const time=now();
    await env.DB.batch([
      env.DB.prepare("UPDATE attempts SET status='expired',finalized_at=? WHERE status='active' AND deadline_at<=?").bind(time,time),
      env.DB.prepare('DELETE FROM auth_challenges WHERE expires_at<=?').bind(time),
      env.DB.prepare('DELETE FROM mobile_codes WHERE expires_at<=?').bind(time),
      env.DB.prepare('DELETE FROM sessions WHERE expires_at<=? OR revoked=1').bind(time),
      env.DB.prepare('DELETE FROM edge_limits WHERE window<?').bind(Math.floor(time/60)-1440),
      env.DB.prepare('DELETE FROM rate_limits WHERE window<?').bind(Math.floor(time/60)-1440)
    ]);
  }
};
