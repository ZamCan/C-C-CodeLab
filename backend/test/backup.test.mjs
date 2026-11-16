import test from 'node:test';
import assert from 'node:assert/strict';
import {randomBytes} from 'node:crypto';
import {encryptBackup,decryptBackup} from '../tools/backup-lib.mjs';
import {DatabaseSync} from 'node:sqlite';
test('encrypted independent backup restores SQLite and detects tampering and wrong passphrases',()=>{
 const secret=randomBytes(32).toString('base64'),sql=Buffer.from("CREATE TABLE restored(id TEXT PRIMARY KEY); INSERT INTO restored VALUES('test-only');");
 const encrypted=encryptBackup(sql,secret);assert.equal(JSON.stringify(encrypted).includes('CREATE TABLE'),false);
 const restored=decryptBackup(encrypted,secret),db=new DatabaseSync(':memory:');try{db.exec(restored.toString());assert.equal(db.prepare('SELECT id FROM restored').get().id,'test-only');}finally{db.close();}
 assert.throws(()=>decryptBackup(encrypted,randomBytes(32).toString('base64')));
 const corrupt={...encrypted,ciphertext:Buffer.alloc(Buffer.from(encrypted.ciphertext,'base64').length).toString('base64')};assert.throws(()=>decryptBackup(corrupt,secret));
});
