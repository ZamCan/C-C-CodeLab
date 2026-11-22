import test from 'node:test';
import assert from 'node:assert/strict';
import {selectQuestions,auditQuestionBank,difficultyAfterCancellations,prioritizeDifficulty,assertLive,grade,publicQuestion} from '../src/policy.mjs';
import worker from '../src/worker.mjs';
const slots=[{style:'multiple_choice',objective:'loops',difficulty:'easy',count:13},{style:'true_false',objective:'types',difficulty:'medium',count:12}];
const bank=slots.flatMap((s,g)=>Array.from({length:30},(_,i)=>({id:`q${g}_${i}`,style:s.style,objective:s.objective,difficulty:s.difficulty,prompt:'Server-only reviewed question',choices_json:s.style==='true_false'?'["True","False"]':'["A","B"]',correct_index:0})));
test('selects 25 unique fresh questions with the required coverage',()=>{const selected=selectQuestions(bank,slots,new Set(['q0_0']),()=>0);assert.equal(selected.length,25);assert.equal(new Set(selected.map(q=>q.id)).size,25);assert.ok(!selected.some(q=>q.id==='q0_0'));assert.equal(selected.filter(q=>q.style==='multiple_choice').length,13);});
test('fails closed when freshness cannot be met',()=>assert.throws(()=>selectQuestions(bank.slice(0,10),slots),/insufficient_fresh_questions/));
test('never sends answer keys to the learner',()=>{const question=publicQuestion(bank[0]);assert.deepEqual(Object.keys(question).sort(),['choices','id','prompt','style']);});
test('deadline equality is expired and reopening cannot reset it',()=>{assert.doesNotThrow(()=>assertLive({status:'active',deadline_at:100},99));assert.throws(()=>assertLive({status:'active',deadline_at:100},100),/expired/);assert.throws(()=>assertLive({status:'submitted',deadline_at:100},50),/finalized/);});
test('grading uses exact values and missing answers are wrong',()=>{const questions=Array.from({length:25},(_,i)=>({question_id:'q'+i,correct_index:0}));const answers=Object.fromEntries(questions.slice(0,20).map(q=>[q.question_id,0]));assert.deepEqual(grade(questions,answers,80),{score:80,passed:true});answers.q0='0';assert.deepEqual(grade(questions,answers,80),{score:76,passed:false});});
test('practical coding requires a dedicated grader',()=>assert.throws(()=>selectQuestions(bank,[{...slots[0],style:'practical_coding'},slots[1]]),/unsupported_blueprint/));
test('disabled exam and credential endpoints fail closed',async()=>{for(const path of ['/v1/exams/attempts','/v1/credentials/issue','/v1/auth/session']){const result=await worker.fetch(new Request('https://test.invalid'+path,{method:'POST'}),{});assert.equal(result.status,503);}});
test('health never claims credential issuance is available',async()=>{const result=await worker.fetch(new Request('https://test.invalid/health'),{});assert.equal((await result.json()).credential_issuance,false);});

test('disabled service does not expose exam question bank or answer keys',async()=>{
  for(const path of ['/v1/exams/attempts/example','/v1/exams/attempts/example/submit','/v1/credentials/example/status']){
    const result=await worker.fetch(new Request('https://test.invalid'+path),{EXAMS_ENABLED:'false'});
    assert.equal(result.status,503);
    const payload=await result.json();
    // This fixture has no D1 binding: the database gate precedes routing.
    assert.deepEqual(payload,{error:'database_not_configured'});
  }
});
test('disabled health advertises truthful readiness',async()=>{
  const result=await worker.fetch(new Request('https://test.invalid/health'),{EXAMS_ENABLED:'false'});
  assert.equal(result.status,200);
  const health=await result.json();
  assert.equal(health.exams_enabled,false);
  assert.equal(health.credential_issuance,false);
});
test('a request cannot bypass disabled exam gate by posting a fake learner identity',async()=>{
  const result=await worker.fetch(new Request('https://test.invalid/v1/exams/attempts',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({course_id:'c17',learner_id:'admin',passed:true,score:100})}),{EXAMS_ENABLED:'false'});
  assert.equal(result.status,503);
});

test('malformed blueprint counts fail closed before selecting questions',()=>{
  for(const count of [0,-1,1.5,'13',null,undefined]){
    const malformed=[{...slots[0],count},slots[1]];
    assert.throws(()=>selectQuestions(bank,malformed),/blueprint_requires_25_questions/);
  }
});
test('question bank rejects duplicate identifiers even when different styles are used',()=>{
  const duplicate=[...bank,{...bank[0],style:'true_false'}];
  assert.throws(()=>selectQuestions(duplicate,slots),/invalid_question_bank/);
});
test('question bank rejects unsafe identifiers',()=>{
  const malformed=[...bank,{...bank[0],id:'../../secret'}];
  assert.throws(()=>selectQuestions(malformed,slots),/invalid_question_bank/);
});

test('rejects duplicate normalized choices before exam selection',()=>{
  const altered=bank.map(q=>({...q}));
  altered[0]={...altered[0],choices_json:'["Correct","correct"]'};
  assert.throws(()=>selectQuestions(altered,slots,new Set(),()=>0),/invalid_question/);
});
test('rejects malformed choice JSON without leaking an internal parser error',()=>{
  const altered=bank.map(q=>({...q}));
  altered[0]={...altered[0],choices_json:'not json'};
  assert.throws(()=>selectQuestions(altered,slots,new Set(),()=>0),/invalid_question_choices/);
});
test('rejects ambiguous true-false options',()=>{
  const altered=bank.map(q=>({...q}));
  altered[30]={...altered[30],choices_json:'["Yes","No"]'};
  assert.throws(()=>selectQuestions(altered,slots,new Set(),()=>0),/invalid_true_false_choices/);
});

test('bank audit reports coverage without exposing answer keys',()=>{
  const report=auditQuestionBank(bank,slots);
  assert.equal(report.ready,true);
  assert.equal(report.total,60);
  assert.deepEqual(report.invalid,[]);
  assert.ok(report.coverage.every(c=>c.available>=c.required));
  assert.ok(!JSON.stringify(report).includes('correct_index'));
});
test('bank audit identifies malformed items even when they would not be selected',()=>{
  const altered=bank.map(q=>({...q}));
  altered[29].choices_json='["A","a"]';
  const report=auditQuestionBank(altered,slots);
  assert.equal(report.ready,false);
  assert.deepEqual(report.invalid,[{id:'q0_29',code:'invalid_question'}]);
});
test('bank audit refuses incomplete blueprint coverage',()=>{
  const report=auditQuestionBank(bank.slice(0,10),slots);
  assert.equal(report.ready,false);
  assert.ok(report.coverage.some(c=>c.available<c.required));
});

test('only explicit cancellations escalate difficulty priority',()=>{
  assert.equal(difficultyAfterCancellations(0),'easy');
  assert.equal(difficultyAfterCancellations(1),'easy');
  assert.equal(difficultyAfterCancellations(2),'medium');
  assert.equal(difficultyAfterCancellations(4),'hard');
  assert.throws(()=>difficultyAfterCancellations(-1),/invalid_cancellation_count/);
  const sample=[{difficulty:'easy'},{difficulty:'hard'},{difficulty:'medium'}];
  assert.deepEqual(prioritizeDifficulty(sample,4).map(s=>s.difficulty),['hard','medium','easy']);
});
