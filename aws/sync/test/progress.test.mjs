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
