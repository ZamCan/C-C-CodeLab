import {DynamoDBClient} from '@aws-sdk/client-dynamodb';
import {DynamoDBDocumentClient,GetCommand,PutCommand} from '@aws-sdk/lib-dynamodb';
import {createRemoteJWKSet,jwtVerify} from 'jose';
import {validateSnapshot,mergeSnapshots} from './progress.mjs';

const client=DynamoDBDocumentClient.from(new DynamoDBClient({}),{marshallOptions:{removeUndefinedValues:true}});
const json=(status,data)=>({statusCode:status,headers:{'content-type':'application/json','cache-control':'no-store','access-control-allow-origin':'null'},body:JSON.stringify(data)});
const issuer=process.env.COGNITO_ISSUER;
const audience=process.env.COGNITO_CLIENT_ID;
const table=process.env.PROGRESS_TABLE;
const jwks=issuer?createRemoteJWKSet(new URL(issuer.replace(/\/$/,'')+'/.well-known/jwks.json')):null;
/** API Gateway HTTP API Lambda proxy. Cognito user-pool federates Google and Login with Amazon.
 * The token MUST be a Cognito ID token; raw Google/Amazon tokens are not accepted.
 */
export async function handler(event){
  if(!issuer||!audience||!table||!jwks)return json(503,{error:'sync_not_configured'});
  const method=event.requestContext?.http?.method;
  const path=event.rawPath;
  if(path!=='/v1/progress'||!['GET','PUT','DELETE'].includes(method))return json(404,{error:'not_found'});
  const auth=event.headers?.authorization||event.headers?.Authorization||'';
  if(!/^Bearer [A-Za-z0-9._~-]+$/.test(auth)||auth.length>8192)return json(401,{error:'authentication_required'});
  let identity;
  try{
    const verified=await jwtVerify(auth.slice(7),jwks,{issuer,audience,algorithms:['RS256']});
    if(verified.payload.token_use!=='id'||typeof verified.payload.sub!=='string')throw Error();
    identity=verified.payload.sub;
  }catch{return json(401,{error:'invalid_identity_token'});}
  const key={pk:'LEARNER#'+identity,sk:'PROGRESS'};
  try{
    if(method==='DELETE'){
      // Account deletion requires a separate authenticated flow; no accidental destructive sync.
      return json(501,{error:'account_deletion_flow_required'});
    }
    if(method==='GET'){
      const r=await client.send(new GetCommand({TableName:table,Key:key,ConsistentRead:true}));
      return json(200,{schema:1,progress:r.Item?.progress||[],revision:r.Item?.revision||0});
    }
    const body=event.body||'';
    if(body.length>262144)return json(413,{error:'payload_too_large'});
    let input;
    try{input=JSON.parse(event.isBase64Encoded?Buffer.from(body,'base64').toString('utf8'):body);validateSnapshot(input);}
    catch{return json(400,{error:'invalid_progress'});}
    // Optimistic concurrency prevents overwriting updates from another device.
    const current=await client.send(new GetCommand({TableName:table,Key:key,ConsistentRead:true}));
    const revision=current.Item?.revision||0;
    const merged=mergeSnapshots({schema:1,progress:current.Item?.progress||[]},input);
    await client.send(new PutCommand({TableName:table,Item:{...key,progress:merged.progress,revision:revision+1,updated_at:Date.now()},ConditionExpression:current.Item?'revision = :revision':'attribute_not_exists(pk)',ExpressionAttributeValues:current.Item?{':revision':revision}:undefined}));
    return json(200,{...merged,revision:revision+1});
  }catch(error){
    if(error?.name==='ConditionalCheckFailedException')return json(409,{error:'sync_conflict_retry'});
    return json(503,{error:'sync_unavailable'});
  }
}
