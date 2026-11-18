import test from 'node:test';
import assert from 'node:assert/strict';
import {verifyAppwriteIdentity} from '../src/appwrite-identity.mjs';
const jwt=['eyJhbGciOiJIUzI1NiJ9','eyJzdWIiOiIxIn0','c2lnbmF0dXJl'].join('.');
test('rejects client supplied email or malformed token without network',async()=>{
  await assert.rejects(verifyAppwriteIdentity('user@example.com',{fetcher:()=>{throw Error('must not fetch')}}),/invalid_identity_token/);
});
test('validates account with project scoped Appwrite request',async()=>{
  let called=false;
  const identity=await verifyAppwriteIdentity(jwt,{fetcher:async(url,init)=>{
    called=true;
    assert.equal(url,'https://fra.cloud.appwrite.io/v1/account');
    assert.equal(init.headers['X-Appwrite-Project'],'codelab-zamcan');
    assert.equal(init.headers['X-Appwrite-JWT'],jwt);
    return {ok:true,json:async()=>({$id:'learner-123',status:true,name:'Learner'})};
  }});
  assert.equal(called,true);
  assert.equal(identity.externalRef,'appwrite:learner-123');
});
test('fails closed for rejected and suspended accounts',async()=>{
  await assert.rejects(verifyAppwriteIdentity(jwt,{fetcher:async()=>({status:401,ok:false})}),/invalid_identity_token/);
  await assert.rejects(verifyAppwriteIdentity(jwt,{fetcher:async()=>({ok:true,json:async()=>({$id:'x',status:false})})}),/invalid_identity_account/);
});
test('rejects provider outage and unexpected project',async()=>{
  await assert.rejects(verifyAppwriteIdentity(jwt,{fetcher:async()=>{throw Error('offline')}}),/identity_provider_unavailable/);
  await assert.rejects(verifyAppwriteIdentity(jwt,{projectId:'other'}),/identity_provider_misconfigured/);
});
