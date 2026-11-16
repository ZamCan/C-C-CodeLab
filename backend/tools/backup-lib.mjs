import {randomBytes,scryptSync,createCipheriv,createDecipheriv} from 'node:crypto';
export function encryptBackup(bytes,passphrase){
 if(typeof passphrase!=='string'||passphrase.length<16)throw Error('A strong recovery passphrase of at least 16 characters is required');
 const salt=randomBytes(16),iv=randomBytes(12),key=scryptSync(passphrase,salt,32,{N:131072,r:8,p:1,maxmem:256*1024*1024});
 try{const cipher=createCipheriv('aes-256-gcm',key,iv);cipher.setAAD(Buffer.from('ZamCan encrypted backup v1'));const data=Buffer.concat([cipher.update(bytes),cipher.final()]);return {version:1,kdf:'scrypt-N131072-r8-p1',salt:salt.toString('base64'),iv:iv.toString('base64'),tag:cipher.getAuthTag().toString('base64'),ciphertext:data.toString('base64')};}finally{key.fill(0);}
}
export function decryptBackup(envelope,passphrase){
 if(envelope.version!==1||envelope.kdf!=='scrypt-N131072-r8-p1')throw Error('Unsupported backup format');
 const salt=Buffer.from(envelope.salt,'base64'),iv=Buffer.from(envelope.iv,'base64'),tag=Buffer.from(envelope.tag,'base64');if(salt.length!==16||iv.length!==12||tag.length!==16)throw Error('Invalid backup parameters');
 const key=scryptSync(passphrase,salt,32,{N:131072,r:8,p:1,maxmem:256*1024*1024});
 try{const cipher=createDecipheriv('aes-256-gcm',key,iv);cipher.setAAD(Buffer.from('ZamCan encrypted backup v1'));cipher.setAuthTag(tag);return Buffer.concat([cipher.update(Buffer.from(envelope.ciphertext,'base64')),cipher.final()]);}finally{key.fill(0);}
}
