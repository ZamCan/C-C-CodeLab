import test from 'node:test';
import assert from 'node:assert/strict';
import {verifyExamChallenge} from '../src/turnstile.mjs';
const env={TURNSTILE_SITE_KEY:'site',TURNSTILE_SECRET_KEY:'secret',TURNSTILE_ALLOWED_HOSTNAME:'exam.example.org'};
const token='a'.repeat(30);
test('fails closed without Turnstile configuration or a valid token',async()=>{
  await assert.rejects(verifyExamChallenge(token,{}),/exam_challenge_not_configured/);
  await assert.rejects(verifyExamChallenge('',env),/exam_challenge_required/);
});
test('accepts verified Cloudflare challenge with expected hostname',async()=>{
  const fetcher=async(url,options)=>{
    assert.match(url,/challenges.cloudflare.com/);
    assert.equal(options.method,'POST');
    assert.equal(new URLSearchParams(options.body).get('response'),token);
    return {ok:true,json:async()=>({success:true,hostname:'exam.example.org'})};
  };
  assert.equal(await verifyExamChallenge(token,env,'',fetcher),true);
});
test('rejects bot failure, hostname mismatch and unavailable verification',async()=>{
  await assert.rejects(verifyExamChallenge(token,env,'',async()=>({ok:true,json:async()=>({success:false})})),/exam_challenge_failed/);
  await assert.rejects(verifyExamChallenge(token,env,'',async()=>({ok:true,json:async()=>({success:true,hostname:'evil.example.org'})})),/exam_challenge_failed/);
  await assert.rejects(verifyExamChallenge(token,env,'',async()=>{throw Error('network');}),/exam_challenge_unavailable/);
});
