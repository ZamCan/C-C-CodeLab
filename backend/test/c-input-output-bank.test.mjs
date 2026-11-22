import test from 'node:test';
import assert from 'node:assert/strict';
import {c17InputOutputDraftBank,auditC17DraftBank} from '../src/c-input-output-bank.mjs';
import {validateQuestion} from '../src/policy.mjs';
test('150 unique C17 input/output draft questions with valid answer choices',()=>{
  const audit=auditC17DraftBank();
  assert.equal(audit.count,150);
  assert.equal(audit.uniqueIds,150);
  assert.equal(audit.correctChoices,true);
  assert.equal(audit.status,'draft_not_authorized_for_certification');
  for(const question of c17InputOutputDraftBank){
    assert.equal(validateQuestion(question),true);
    assert.equal(question.course_id,'c17');
    assert.ok(question.prompt.includes('scanf'));
    assert.ok(question.prompt.includes('printf'));
    assert.ok(question.answer_explanation.length>20);
  }
});
test('draft bank must not be treated as reviewed or deployed exam inventory',()=>{
  assert.ok(c17InputOutputDraftBank.every(q=>q.review_status==='draft'));
});
