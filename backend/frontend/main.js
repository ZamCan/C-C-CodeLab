import {startRegistration,startAuthentication} from '@simplewebauthn/browser';
import {verifyCredential} from '../src/credentials.mjs';
let language=localStorage.getItem('zamcan-language')==='sw'?'sw':'en',user=null,timer=null;
const translations={
 dashboard:['My learning record','Rekodi yangu'],verify:['Verify a certificate','Hakiki cheti'],signin:['Sign in','Ingia'],signout:['Sign out','Ondoka'],eyebrow:['KNOWLEDGE, MADE YOURS','ELIMU, MALI YAKO'],hero:['Your achievement. Your lasting proof.','Mafanikio yako. Ushahidi wa kudumu.'],intro:['Learn with CodeLab. Take your assessment here. Keep a signed record of what you have earned.','Jifunze na CodeLab. Fanya tathmini hapa. Hifadhi rekodi yenye saini ya mafanikio yako.'],start:['Open my learning record','Fungua rekodi yangu'],offline:['Learning stays free and offline.','Masomo ni bure na yanapatikana bila intaneti.'],certTitle:['Certificate of achievement','Cheti cha ufaulu'],earned:['A record earned through understanding.','Rekodi inayotokana na uelewa.'],illustration:['Design illustration · Not an issued credential','Mfano wa muundo · Si cheti kilichotolewa'],privateTitle:['Your identity, protected','Utambulisho wako unalindwa'],private:['Sign in with a passkey. Your private key stays with your authenticator.','Ingia kwa passkey. Ufunguo wako wa siri unabaki kwenye kifaa chako cha uthibitisho.'],proofTitle:['Proof beyond appearance','Ushahidi zaidi ya mwonekano'],proof:['Each certificate includes a signed digital record and a QR link to its current status.','Kila cheti kina rekodi ya kidijitali yenye saini na QR ya kuhakiki hali yake.'],ownerTitle:['A record you can keep','Rekodi unayoweza kuhifadhi'],owner:['Download your printable PDF and portable signed credential. Verification is always separate from course progress.','Pakua PDF ya kuchapisha na cheti cha kidijitali chenye saini. Uhakiki ni tofauti na maendeleo ya masomo.'],privacy:['Privacy & verification','Faragha na uhakiki'],issuerNotice:['Issued by ZamCan Academy. No third-party accreditation is implied.','Hutolewa na ZamCan Academy. Hakuna ithibati ya taasisi nyingine inayodaiwa.'],welcome:['Welcome to your record','Karibu kwenye rekodi yako'],passkey:['Use your phone lock, fingerprint or security key. No password is sent to the academy.','Tumia kufuli la simu, alama ya kidole au ufunguo wa usalama. Hakuna nenosiri linalotumwa chuoni.'],name:['Full name for a new account','Jina kamili la akaunti mpya'],passkeySignin:['Sign in with a passkey','Ingia kwa passkey'],create:['Create a new account','Fungua akaunti mpya'],identityNote:['Your name must be verified before a certificate can be issued. Use a synced passkey or retain access to your authenticator.','Jina lako lazima lihakikiwe kabla ya cheti kutolewa. Tumia passkey iliyosawazishwa au hifadhi ufikiaji wa kifaa chako cha uthibitisho.'],courses:['Examinations','Mitihani'],certificates:['Your certificates','Vyeti vyako'],attempts:['Assessment history','Historia ya tathmini'],emptyCourses:['No examinations have been published yet.','Hakuna mitihani iliyochapishwa bado.'],emptyCertificates:['Your issued certificates will appear here after verified eligibility and a successful assessment.','Vyeti vyako vitaonekana hapa baada ya kustahiki na kufaulu tathmini.'],emptyAttempts:['No assessment attempts yet.','Bado hujafanya tathmini.'],pendingIdentity:['Identity review pending. Certificate issuance requires a verified name.','Uhakiki wa utambulisho unasubiri. Cheti kinahitaji jina lililohakikiwa.'],eligible:['Eligible','Unastahiki'],pending:['Eligibility review required','Uhakiki wa kustahiki unahitajika'],begin:['Begin assessment','Anza tathmini'],resume:['Resume','Endelea'],download:['Download PDF','Pakua PDF'],signed:['Signed credential','Cheti chenye saini'],active:['Active','Halali'],revoked:['Revoked','Kimebatilishwa'],invalid:['Invalid','Si halali'],unknown:['Status unavailable','Hali haipatikani'],credentialId:['Certificate ID','Namba ya cheti'],check:['Verify','Hakiki'],checkFile:['Verify a signed credential file','Hakiki faili ya cheti chenye saini'],next:['Next','Mbele'],back:['Back','Nyuma'],submit:['Submit assessment','Wasilisha tathmini'],saved:['Answer saved','Jibu limehifadhiwa'],saving:['Saving…','Inahifadhi…'],requestCertificate:['Request certificate','Omba cheti'],connect:['Connect CodeLab on this phone','Unganisha CodeLab kwenye simu hii'],connectNote:['Allow the Android app to synchronize your online results and certificates. Offline course progress does not authorize certificate issuance.','Ruhusu programu ya Android kusawazisha matokeo na vyeti. Maendeleo ya masomo bila intaneti hayatoi ruhusa ya cheti.'],approve:['Connect this installation','Unganisha programu hii'],error:['The request could not be completed. Please try again.','Ombi halikukamilika. Tafadhali jaribu tena.'],unconfigured:['This service is being prepared. Sign-in or issuance has not been enabled yet.','Huduma inaandaliwa. Kuingia au utoaji wa vyeti bado haujawezeshwa.'],offlineError:['You are offline. Online exams and certificate status require a connection. Offline lessons remain available in CodeLab.','Huna intaneti. Mitihani na uhakiki wa vyeti vinahitaji intaneti. Masomo ya CodeLab bado yanapatikana bila intaneti.'],admin:['Academic review','Uhakiki wa kitaaluma'],student:['Student ID','Namba ya mwanafunzi'],course:['Course ID','Namba ya kozi'],evidence:['Verified evidence reference','Rejea ya ushahidi uliohakikiwa'],identity:['Approve identity','Thibitisha utambulisho'],eligibility:['Approve eligibility','Thibitisha kustahiki'],audit:['View audit log','Angalia kumbukumbu za ukaguzi'],privacyText:['Passkeys protect sign-in. The service stores your account name, assessment records, issued credentials and security audit events. Public verification shows certificate status and course, not your account details. A valid signature proves integrity and issuer control; it does not imply outside accreditation. Contact the academy for verified identity correction or recovery.','Passkey hulinda kuingia. Huduma huhifadhi jina, rekodi za tathmini, vyeti na ukaguzi wa usalama. Uhakiki wa umma huonyesha hali na kozi, si taarifa za akaunti. Saini halali huthibitisha uadilifu na mtoaji; si ithibati ya taasisi nyingine. Wasiliana na chuo kwa marekebisho au urejeshaji wa utambulisho.']
};
const t=key=>translations[key]?.[language==='sw'?1:0]||key;
const $=id=>document.getElementById(id),workspace=$('workspace');
function node(tag,text,className){const n=document.createElement(tag);if(text!==undefined)n.textContent=text;if(className)n.className=className;return n;}
function button(text,run,primary=false){const b=node('button',text,primary?'primary':'');b.type='button';b.addEventListener('click',()=>Promise.resolve().then(run).catch(showError));return b;}
function reset(){clearInterval(timer);workspace.replaceChildren();}
function notice(text,error=false){$('notice').hidden=!text;$('notice').textContent=text;$('notice').className='notice'+(error?' error':'');}
function showError(error){const code=error?.code;notice(!navigator.onLine?t('offlineError'):code?.includes('not_enabled')||code?.includes('not_configured')?t('unconfigured'):code==='rate_limited'?(language==='sw'?'Subiri kidogo kisha ujaribu tena.':'Please wait briefly before trying again.'):error?.name==='NotAllowedError'?(language==='sw'?'Uthibitisho umeghairiwa au muda umeisha.':'Passkey verification was cancelled or timed out.'):t('error'),true);}
async function api(path,method='GET',body){const response=await fetch(path,{method,credentials:'same-origin',headers:{...(body?{'Content-Type':'application/json'}:{}),...(user?.csrf?{'X-CSRF-Token':user.csrf}:{})},body:body?JSON.stringify(body):undefined});const data=await response.json();if(!response.ok)throw Object.assign(Error(data.error),{code:data.error,status:response.status});return data;}
function translate(){document.documentElement.lang=language;for(const e of document.querySelectorAll('[data-i18n]'))e.textContent=t(e.dataset.i18n);$('language').textContent=language==='en'?'Kiswahili':'English';$('account').textContent=user?t('signout'):t('signin');}
function signIn(){notice('');$('auth-error').textContent='';$('auth-dialog').showModal();}
async function authenticate(register){const buttons=$('auth-form').querySelectorAll('button');buttons.forEach(b=>b.disabled=true);try{
 const options=await api('/v1/auth/'+(register?'register':'login')+'/options','POST',register?{name:$('name').value,locale:language}:{});
 const response=register?await startRegistration({optionsJSON:options.options}):await startAuthentication({optionsJSON:options.options});
 await api('/v1/auth/'+(register?'register':'login')+'/verify','POST',{flow_id:options.flow_id,response});user=await api('/v1/auth/me');$('auth-dialog').close();translate();await route();
 }catch(error){$('auth-error').textContent=error?.code?.includes('not_enabled')?t('unconfigured'):t('error');}finally{buttons.forEach(b=>b.disabled=false);}}
function heading(title,description){const h=node('div',undefined,'section-heading'),block=node('div');block.append(node('h2',title));if(description)block.append(node('p',description));h.append(block);workspace.append(h);return h;}
function gridSection(title,items,render,empty){workspace.append(node('h2',title));const grid=node('div',undefined,'grid');if(!items.length)grid.append(node('p',empty,'empty'));for(const item of items)grid.append(render(item));workspace.append(grid);}
async function dashboard(){reset();if(!user){heading(t('welcome'),t('passkey'));workspace.append(button(t('signin'),signIn,true));return;}heading(user.name,user.identity_verified?user.id:t('pendingIdentity'));const data=await api('/v1/dashboard');
 gridSection(t('courses'),data.courses,course=>{const c=node('article',undefined,'card');c.append(node('span',course.eligible?t('eligible'):t('pending'),'badge'),node('h3',language==='sw'?course.title_sw:course.title_en),node('p',course.version));const b=button(t('begin'),()=>nameForm(false,async (names,turnstile_token)=>openAttempt(await api('/v1/exams/attempts','POST',{course_id:course.id,official_names:names,name_attested:true,turnstile_token}))),true);b.disabled=!course.eligible||!course.duration_seconds||!data.exams_enabled;c.append(b);return c;},t('emptyCourses'));
 gridSection(t('certificates'),data.certificates,cert=>{const c=node('article',undefined,'card');c.append(node('span',t(cert.status),'badge '+cert.status),node('h3',cert.course_id),node('p',cert.id));for(const [label,suffix] of [['download','pdf'],['signed','download']])c.append(button(t(label),()=>{location.href='/v1/credentials/'+encodeURIComponent(cert.id)+'/'+suffix;}));c.append(button(t('verify'),()=>verify(cert.id)));return c;},t('emptyCertificates'));
 gridSection(t('attempts'),data.attempts,attempt=>{const c=node('article',undefined,'card');c.append(node('h3',attempt.course_id),node('p',attempt.status+(attempt.score!==null?' · '+attempt.score+'%':'')));if(attempt.status==='active')c.append(button(t('resume'),async()=>openAttempt(await api('/v1/exams/attempts/'+attempt.id)),true));if(attempt.passed)c.append(button(t('requestCertificate'),()=>nameForm(true,async names=>{await api('/v1/credentials/issue','POST',{attempt_id:attempt.id,official_names:names,name_confirmed:true});await dashboard();})));return c;},t('emptyAttempts'));
 if(['admin','examiner'].includes(user.role))workspace.append(button(t('admin'),admin));
}
async function openAttempt(attempt){
 reset();heading(t('courses'),attempt.course_id);
 if(attempt.status!=='active'){
   workspace.append(node('p',attempt.status+(attempt.score!==null?' · '+attempt.score+'%':'')),button(t('dashboard'),dashboard));return;
 }
 let index=0,pending=Promise.resolve(),failedSaves=new Set(),busy=false;
 const arrived=performance.now(),seconds=attempt.deadline_at-attempt.server_time;
 const shell=node('section',undefined,'exam-shell'),head=node('div',undefined,'exam-top'),
       clock=node('p','','timer'),progress=node('p','','quiet'),box=node('article',undefined,'card question'),
       save=node('p','','quiet'),palette=node('div',undefined,'exam-palette');
 const intro=node('p',language==='sw'?'Majibu yanahifadhiwa moja kwa moja. Ukiondoka, muda unaendelea.':'Answers save automatically. Leaving does not pause the deadline.','quiet');
 head.append(progress,clock);shell.append(head,intro,palette,box,save);workspace.append(shell);
 const timer=setInterval(tick,1000);
 function tick(){
   const remaining=Math.max(0,Math.ceil(seconds-(performance.now()-arrived)/1000));
   clock.textContent=Math.floor(remaining/60)+':'+String(remaining%60).padStart(2,'0');
   clock.setAttribute('aria-label',remaining+' seconds remaining');
   if(!remaining){clearInterval(timer);box.querySelectorAll('input,button').forEach(e=>e.disabled=true);notice(language==='sw'?'Muda wa tathmini umeisha.':'Assessment time has ended.');}
 }
 function render(){
   box.replaceChildren();palette.replaceChildren();
   const answered=Object.keys(attempt.answers||{}).filter(k=>Number.isInteger(attempt.answers[k])).length;
   progress.textContent=(language==='sw'?'Yaliyojibiwa ':'Answered ')+answered+' / '+attempt.questions.length;
   attempt.questions.forEach((q,i)=>{
     const label=(i+1)+(Number.isInteger(attempt.answers[q.id])?' ✓':'');
     const jump=button(label,()=>{index=i;render();},i===index);
     jump.setAttribute('aria-label','Question '+(i+1)+(Number.isInteger(attempt.answers[q.id])?', answered':''));
     jump.setAttribute('aria-current',i===index?'step':'false');
     palette.append(jump);
   });
   const q=attempt.questions[index];
   box.append(node('p',(index+1)+' / '+attempt.questions.length+' · '+q.style.replaceAll('_',' '),'eyebrow'),node('h3',q.prompt));
   const choices=node('div',undefined,'choices');
   q.choices.forEach((choice,i)=>{
     const label=node('label'),radio=node('input');radio.type='radio';radio.name='answer';radio.checked=attempt.answers[q.id]===i;
     radio.addEventListener('change',()=>{
       attempt.answers[q.id]=i;save.textContent=t('saving');
       pending=pending.catch(()=>{}).then(()=>api('/v1/exams/attempts/'+attempt.id+'/answers/'+q.id,'PUT',{answer:i}))
         .then(()=>{failedSaves.delete(q.id);save.textContent=t('saved');render();})
         .catch(error=>{failedSaves.add(q.id);save.textContent=t('error');showError(error);});
     });
     label.append(radio,node('span',choice));choices.append(label);
   });
   box.append(choices);
   const nav=node('div',undefined,'exam-nav');
   const back=button(t('back'),()=>{index--;render();});back.disabled=index===0;nav.append(back);
   if(index+1<attempt.questions.length)nav.append(button(t('next'),()=>{index++;render();},true));
   const submitButton=button(t('submit'),async()=>{
     if(busy)return;
     const missing=attempt.questions.length-Object.keys(attempt.answers).filter(k=>Number.isInteger(attempt.answers[k])).length;
     const warning=missing?(language==='sw'?missing+' maswali hayajajibiwa.':'You have '+missing+' unanswered questions.'):'';
     if(!confirm((warning?warning+'\\n':'')+(language==='sw'?'Wasilisha mtihani sasa?':'Submit this attempt now?')))return;
     busy=true;submitButton.disabled=true;
     try{await pending;if(failedSaves.size)throw Error('Some answers were not saved. Retry before submitting.');
       const result=await api('/v1/exams/attempts/'+attempt.id+'/submit','POST',{});clearInterval(timer);await openAttempt(result);
     }catch(error){busy=false;submitButton.disabled=false;showError(error);}
   },index===attempt.questions.length-1);
   nav.append(submitButton);box.append(nav);tick();
 }
 tick();render();
}
async function verify(id=''){reset();heading(t('verify'));const form=node('form',undefined,'card'),label=node('label',t('credentialId')),input=node('input');input.value=id;input.required=true;input.maxLength=36;label.append(input);const go=node('button',t('check'),'primary');go.type='submit';const result=node('div');form.append(label,go,result);workspace.append(form);form.addEventListener('submit',async event=>{event.preventDefault();result.replaceChildren();try{const value=await api('/v1/credentials/'+encodeURIComponent(input.value.trim())+'/status');result.append(node('p',t(value.state),'verify-state '+value.state));if(value.course)result.append(node('p',value.course),node('p',value.issued_at));}catch(error){result.append(node('p',error.status===404?t('invalid'):t('unknown'),'verify-state invalid'));}});if(id)form.requestSubmit();
 const fileLabel=node('label',t('checkFile')),file=node('input');file.type='file';file.accept='.jwt,.json,application/vc+jwt';fileLabel.append(file);form.append(fileLabel);file.addEventListener('change',async()=>{try{if(!file.files[0]||file.files[0].size>32768)throw Error('Invalid file');const keys=await api('/.well-known/zamcan-issuer-keys.json'),token=(await file.files[0].text()).trim();const checked=await verifyCredential(token,async kid=>{const key=keys.keys.find(k=>k.kid===kid);return key?{status:key.status,jwk:key}:null;},location.origin);input.value=checked.payload.id.split('/').pop();result.replaceChildren(node('p',language==='sw'?'Saini imethibitishwa. Inahakiki hali…':'Signature verified. Checking current status…'));form.requestSubmit();}catch{result.replaceChildren(node('p',t('invalid'),'verify-state invalid'));}});
}
async function admin(){reset();heading(t('admin'));const form=node('div',undefined,'card'),fields={};for(const [name,label] of [['learner_id','student'],['course_id','course'],['evidence_ref','evidence']]){const l=node('label',t(label)),input=node('input');input.maxLength=name==='evidence_ref'?300:100;fields[name]=input;l.append(input);form.append(l);}const data=()=>Object.fromEntries(Object.entries(fields).map(([k,v])=>[k,v.value.trim()]));form.append(button(t('eligibility'),async()=>{await api('/v1/admin/eligibility','POST',data());notice(t('eligible'));},true));if(user.role==='admin'){form.append(button(t('identity'),async()=>{await api('/v1/admin/identity','POST',data());notice(t('identity'));}));form.append(button(t('audit'),async()=>{const events=await api('/v1/admin/audit');const log=node('div');for(const e of events.events)log.append(node('p',new Date(e.created_at*1000).toISOString()+' · '+e.event_type+' · '+e.object_ref));form.append(log);}));}workspace.append(form);}
async function route(){if(location.pathname.startsWith('/verify/'))return verify(location.pathname.split('/').pop());if(location.pathname==='/connect'){reset();heading(t('connect'),t('connectNote'));if(!user){workspace.append(button(t('signin'),signIn,true));return;}const params=new URLSearchParams(location.search),challenge=params.get('challenge'),state=params.get('state');if(!/^[A-Za-z0-9_-]{43}$/.test(challenge||'')||!/^[A-Za-z0-9_-]{43}$/.test(state||'')){notice(t('error'),true);return;}workspace.append(button(t('approve'),async()=>{const result=await api('/v1/mobile/authorize','POST',{challenge});location.href='codelab://certification?code='+encodeURIComponent(result.code)+'&state='+encodeURIComponent(state);},true));return;}return dashboard();}
$('login').onclick=()=>authenticate(false);$('auth-form').onsubmit=event=>{event.preventDefault();authenticate(true);};$('auth-close').onclick=()=>$('auth-dialog').close();$('account').onclick=async()=>{if(!user)return signIn();try{await api('/v1/auth/logout','POST',{});user=null;translate();await dashboard();}catch(error){showError(error);}};$('language').onclick=()=>{language=language==='en'?'sw':'en';localStorage.setItem('zamcan-language',language);translate();route().catch(showError);};$('dashboard').onclick=()=>dashboard().catch(showError);$('start').onclick=()=>user?dashboard().catch(showError):signIn();$('verify-nav').onclick=()=>verify().catch(showError);$('privacy').onclick=()=>{reset();heading(t('privacy'));workspace.append(node('p',t('privacyText'),'card'));};window.addEventListener('offline',()=>notice(t('offlineError'),true));
translate();api('/v1/auth/me').then(value=>{user=value;translate();}).catch(()=>{}).finally(()=>route().catch(showError));

function nameForm(confirm,proceed){
 reset();heading(language==='sw'?'Majina rasmi matatu':'Three official names',language==='sw'?'Andika majina kama yalivyo kwenye vyeti vyako rasmi. Alama za kufaulu ni 70% au zaidi.':'Enter your three names exactly as they appear on your official certificates. The pass mark is 70%.');
 const form=node('form',undefined,'card'),inputs=[];
 for(const labelText of (language==='sw'?['Jina la kwanza','Jina la pili','Jina la tatu']:['First name','Second name','Third name'])){const label=node('label',labelText),input=node('input');input.required=true;input.maxLength=60;input.autocomplete='off';label.append(input);form.append(label);inputs.push(input);}
 const label=node('label'),check=node('input');check.type='checkbox';check.required=true;label.append(check,node('span',language==='sw'?'Ninathibitisha majina haya ni sahihi.':'I confirm these official names are correct.'));form.append(label);
 if(confirm)form.append(node('p',language==='sw'?'Majina haya yatachapishwa kwenye cheti chako.':'These names will be printed on your certificate. Corrections require an identity review.'));
 let challengeToken='',challengeWidget=null,challengeReady=confirm;
 if(!confirm){
   const challenge=node('div',undefined,'exam-challenge');
   challenge.append(node('p',language==='sw'?'Thibitisha kuwa wewe si roboti kabla ya kuanza.':'Complete the security check before the exam starts.','quiet'));
   form.append(challenge);
   (async()=>{
     try{
       const config=await api('/v1/exams/challenge-config');
       if(!config.ready||!config.sitekey)throw Error('Exam security check is not configured.');
       if(!window.turnstile){
         await new Promise((resolve,reject)=>{
           const script=document.createElement('script');
           script.src='https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit';
           script.async=true;script.defer=true;script.onload=resolve;script.onerror=reject;document.head.append(script);
         });
       }
       challengeWidget=window.turnstile.render(challenge,{
         sitekey:config.sitekey,theme:'auto',
         callback:token=>{challengeToken=token;challengeReady=true;},
         'expired-callback':()=>{challengeToken='';challengeReady=false;},
         'error-callback':()=>{challengeToken='';challengeReady=false;}
       });
     }catch(error){challenge.append(node('p',error.message||'Security check unavailable.','quiet'));}
   })();
 }
 const submit=node('button',confirm?t('requestCertificate'):t('begin'),'primary');submit.type='submit';form.append(submit,button(t('back'),dashboard));form.onsubmit=async event=>{event.preventDefault();submit.disabled=true;try{if(!challengeReady)throw Error('Complete the security check first.');await proceed(inputs.map(i=>i.value.trim()),challengeToken);}catch(error){showError(error);if(!confirm&&challengeWidget!==null&&window.turnstile){challengeToken='';challengeReady=false;window.turnstile.reset(challengeWidget);}}finally{submit.disabled=false;}};workspace.append(form);
}
