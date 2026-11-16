import {PDFDocument,rgb} from 'pdf-lib';
import fontkit from '@pdf-lib/fontkit';
import QRCode from 'qrcode';

/** Vector artwork and QR stay crisp in print. The attached JWS carries the signature. */
export async function certificatePdf(credential,token,state,env){
 const doc=await PDFDocument.create();doc.registerFontkit(fontkit);
 const response=await env.ASSETS.fetch(new Request('https://assets.invalid/fonts/NotoSans-Regular.ttf'));
 if(!response.ok)throw new Error('Certificate font unavailable');
 const font=await doc.embedFont(await response.arrayBuffer(),{subset:true});
 const page=doc.addPage([842,595]);const navy=rgb(.07,.13,.23),gold=rgb(.65,.48,.22),muted=rgb(.32,.37,.43),ivory=rgb(.99,.98,.95);
 page.drawRectangle({x:0,y:0,width:842,height:595,color:ivory});
 page.drawRectangle({x:20,y:20,width:802,height:555,borderWidth:1.2,borderColor:gold});
 page.drawRectangle({x:29,y:29,width:784,height:537,borderWidth:.5,borderColor:gold});
 page.drawRectangle({x:0,y:0,width:14,height:595,color:navy});
 for(const [x,y,dx,dy] of [[43,550,1,-1],[799,550,-1,-1],[43,45,1,1],[799,45,-1,1]]){
   for(let i=0;i<3;i++){page.drawLine({start:{x:x+dx*i*6,y},end:{x:x+dx*i*6,y:y+dy*32},color:gold,thickness:.7});page.drawLine({start:{x,y:y+dy*i*6},end:{x:x+dx*32,y:y+dy*i*6},color:gold,thickness:.7});}
 }
 function centered(text,y,size,color=navy,max=680){let s=size;while(font.widthOfTextAtSize(text,s)>max&&s>12)s-=.5;if(font.widthOfTextAtSize(text,s)>max)throw Error('Certificate text exceeds printable width');page.drawText(text,{x:(842-font.widthOfTextAtSize(text,s))/2,y,size:s,font,color});}
 const sw=credential.name==='Cheti cha ufaulu';
 centered('ZAMCAN ACADEMY',508,18);centered('C/C++ CodeLab',481,10,muted);
 centered(sw?'CHETI CHA UFAULU':'CERTIFICATE OF ACHIEVEMENT',422,26,gold);
 centered(sw?'Cheti hiki kinatolewa kwa':'This certificate is awarded to',379,12,muted);
 centered(credential.credentialSubject.name,331,31);
 page.drawLine({start:{x:200,y:310},end:{x:642,y:310},thickness:.6,color:gold});
 centered(sw?'kwa kufaulu tathmini ya':'for successfully passing the assessment in',279,12,muted);
 centered(credential.credentialSubject.achievement,246,21);
 centered((sw?'Toleo la kozi: ':'Course version: ')+credential.credentialSubject.courseVersion,217,10,muted);
 const id=credential.id.split('/').pop(),url=new URL('/verify/'+id,credential.issuer.id).href;
 const qr=QRCode.create(url,{errorCorrectionLevel:'M'}),n=qr.modules.size,cell=2.1,left=688,base=65;
 page.drawRectangle({x:left-8,y:base-8,width:(n+8)*cell,height:(n+8)*cell,color:rgb(1,1,1)});
 for(let row=0;row<n;row++)for(let col=0;col<n;col++)if(qr.modules.get(row,col))page.drawRectangle({x:left+col*cell,y:base+(n-row-1)*cell,width:cell+.02,height:cell+.02,color:navy});
 page.drawText((sw?'Tarehe: ':'Issued: ')+credential.validFrom.slice(0,10),{x:65,y:151,size:11,font,color:navy});
 page.drawText('ID: '+id,{x:65,y:129,size:9,font,color:muted});
 page.drawText(url,{x:65,y:108,size:8,font,color:muted});
 page.drawText(sw?'Saini ya kidijitali imeambatishwa. Hakiki hali kwa QR.':'Signed credential attached. Scan the QR to check current status.',{x:65,y:74,size:9,font,color:muted});
 if(state==='revoked')centered(sw?'IMEBATILISHWA':'REVOKED',180,24,rgb(.65,.12,.15));
 doc.setTitle('ZamCan Academy — '+credential.name);doc.setAuthor('ZamCan Academy');
 doc.setSubject('Independently verifiable course achievement; no accreditation claim');
 await doc.attach(new TextEncoder().encode(token),'credential.vc.jwt',{mimeType:'application/vc+jwt',description:'Ed25519-signed portable credential. PDF appearance alone does not prove validity.'});
 return doc.save();
}
