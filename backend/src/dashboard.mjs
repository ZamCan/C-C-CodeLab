import {ApiError} from './policy.mjs';
import {session,readJson,now} from './security.mjs';
const json=data=>Response.json(data,{headers:{'Cache-Control':'no-store'}});
export async function dashboardRoute(request,env,path){
 if(path==='/v1/dashboard'&&request.method==='GET'){
   const user=await session(request,env);
   const [{results:courses},{results:attempts},{results:certificates}]=await Promise.all([
     env.DB.prepare('SELECT c.id,c.version,c.title_en,c.title_sw,CASE WHEN e.learner_id IS NULL THEN 0 ELSE 1 END AS eligible,b.duration_seconds,b.pass_percent FROM courses c LEFT JOIN eligibility e ON e.course_id=c.id AND e.learner_id=? LEFT JOIN blueprints b ON b.course_id=c.id AND b.active=1').bind(user.id).all(),
     env.DB.prepare('SELECT id,course_id,status,score,passed,started_at,deadline_at FROM attempts WHERE learner_id=? ORDER BY started_at DESC LIMIT 50').bind(user.id).all(),
     env.DB.prepare('SELECT id,course_id,issued_at,status FROM credentials WHERE learner_id=? ORDER BY issued_at DESC LIMIT 100').bind(user.id).all()
   ]);
   return json({courses,attempts,certificates,exams_enabled:env.EXAMS_ENABLED==='true',issuance_enabled:env.ISSUANCE_ENABLED==='true'});
 }
 if(!path.startsWith('/v1/admin/'))return null;
 const user=await session(request,env,path==='/v1/admin/eligibility'?['admin','examiner']:['admin']);
 if(path==='/v1/admin/audit'&&request.method==='GET'){const {results}=await env.DB.prepare('SELECT id,actor_ref,event_type,object_ref,created_at FROM audit_events ORDER BY created_at DESC LIMIT 100').all();return json({events:results});}
 if(request.method!=='POST')throw new ApiError(405,'method_not_allowed');
 const data=await readJson(request);
 if(!/^[A-Za-z0-9_-]{1,100}$/.test(data.learner_id||'')||typeof data.evidence_ref!=='string'||data.evidence_ref.trim().length<5||data.evidence_ref.length>300)throw new ApiError(400,'verified_evidence_required');
 const learner=await env.DB.prepare('SELECT id FROM learners WHERE id=?').bind(data.learner_id).first();if(!learner)throw new ApiError(404,'learner_not_found');
 const audit=event=>env.DB.prepare('INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at,metadata_json) VALUES(?,?,?,?,?,?)').bind(crypto.randomUUID(),user.id,event,learner.id,now(),JSON.stringify({evidence_ref:data.evidence_ref,course_id:data.course_id||null}));
 if(path==='/v1/admin/identity'){
   await env.DB.batch([env.DB.prepare('UPDATE learners SET identity_verified=1 WHERE id=?').bind(learner.id),audit('identity_verified')]);return json({verified:true});
 }
 if(path==='/v1/admin/eligibility'){
   if(!/^[A-Za-z0-9_-]{1,100}$/.test(data.course_id||''))throw new ApiError(400,'invalid_course');
   if(!await env.DB.prepare('SELECT id FROM courses WHERE id=?').bind(data.course_id).first())throw new ApiError(404,'course_not_found');
   await env.DB.batch([env.DB.prepare('INSERT INTO eligibility(learner_id,course_id,approved_at,evidence_ref) VALUES(?,?,?,?) ON CONFLICT(learner_id,course_id) DO UPDATE SET approved_at=excluded.approved_at,evidence_ref=excluded.evidence_ref').bind(learner.id,data.course_id,now(),data.evidence_ref),audit('eligibility_approved')]);return json({eligible:true});
 }
 throw new ApiError(404,'not_found');
}
