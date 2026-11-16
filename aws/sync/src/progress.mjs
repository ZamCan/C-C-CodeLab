/** Shared deterministic progress validation and conflict resolution. */
const identifier=/^[A-Za-z0-9_-]{1,100}$/;
export function validateSnapshot(snapshot){
  if(!snapshot||snapshot.schema!==1||!Array.isArray(snapshot.progress)||snapshot.progress.length>10000)throw new Error('invalid_snapshot');
  const unique=new Map();
  for(const entry of snapshot.progress){
    if(!entry||!identifier.test(entry.course)||!identifier.test(entry.lesson)||
       !Number.isSafeInteger(entry.completed_at)||entry.completed_at<=0||
       entry.completed_at>Date.now()+86400000)throw new Error('invalid_progress');
    const key=entry.course+'\u0000'+entry.lesson;
    const prev=unique.get(key);
    if(!prev||entry.completed_at<prev.completed_at)unique.set(key,{course:entry.course,lesson:entry.lesson,completed_at:entry.completed_at});
  }
  return [...unique.values()].sort((a,b)=>a.course.localeCompare(b.course)||a.lesson.localeCompare(b.lesson));
}
export function mergeSnapshots(local,remote){
  return {schema:1,progress:validateSnapshot({schema:1,progress:[...validateSnapshot(local),...validateSnapshot(remote)]})};
}
export function learnerKey(subject){
  if(typeof subject!=='string'||subject.length<3||subject.length>512)throw new Error('invalid_subject');
  return subject;
}
