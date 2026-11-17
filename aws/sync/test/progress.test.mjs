import test from 'node:test';
import assert from 'node:assert/strict';
import {validateSnapshot,mergeSnapshots,learnerKey} from '../src/progress.mjs';
const a={schema:1,progress:[{course:'c-foundation',lesson:'1',completed_at:1000}]};
test('merges without losing local progress',()=>{
  const b={schema:1,progress:[{course:'c-foundation',lesson:'1',completed_at:2000},{course:'cpp-foundation',lesson:'2',completed_at:3000}]};
  assert.deepEqual(mergeSnapshots(a,b).progress,[a.progress[0],b.progress[1]]);
});
test('rejects corrupt or future timestamps',()=>assert.throws(()=>validateSnapshot({schema:1,progress:[{course:'c',lesson:'1',completed_at:Date.now()+100000000}]})));
test('rejects unsafe identifiers',()=>assert.throws(()=>validateSnapshot({schema:1,progress:[{course:'../x',lesson:'1',completed_at:1000}]})));
test('rejects unauthenticated subject',()=>assert.throws(()=>learnerKey('')));

test('rejects unsupported schema and missing progress list',()=>{
  assert.throws(()=>validateSnapshot({schema:2,progress:[]}));
  assert.throws(()=>validateSnapshot({schema:1}));
  assert.throws(()=>validateSnapshot(null));
});
test('rejects invalid timestamps and identifiers',()=>{
  for(const completed_at of [0,-1,1.5,'1000',Number.MAX_SAFE_INTEGER+1]){
    assert.throws(()=>validateSnapshot({schema:1,progress:[{course:'c',lesson:'one',completed_at}]}));
  }
  for(const course of ['', '../c', 'a'.repeat(101), 'c space']){
    assert.throws(()=>validateSnapshot({schema:1,progress:[{course,lesson:'one',completed_at:1000}]}));
  }
});
test('normalizes duplicate entries to earliest completion',()=>{
  const result=validateSnapshot({schema:1,progress:[
    {course:'c',lesson:'one',completed_at:9000},
    {course:'c',lesson:'one',completed_at:2000},
    {course:'c',lesson:'one',completed_at:7000}
  ]});
  assert.deepEqual(result,[{course:'c',lesson:'one',completed_at:2000}]);
});
test('merge is commutative and idempotent',()=>{
  const b={schema:1,progress:[
    {course:'c-foundation',lesson:'1',completed_at:500},
    {course:'cpp',lesson:'2',completed_at:3000}
  ]};
  assert.deepEqual(mergeSnapshots(a,b),mergeSnapshots(b,a));
  assert.deepEqual(mergeSnapshots(a,a),{schema:1,progress:a.progress});
  assert.deepEqual(mergeSnapshots(mergeSnapshots(a,b),b),mergeSnapshots(a,b));
});
test('rejects progress list beyond server entry cap',()=>{
  const entry={course:'c',lesson:'1',completed_at:1000};
  assert.throws(()=>validateSnapshot({schema:1,progress:Array(10001).fill(entry)}));
});
