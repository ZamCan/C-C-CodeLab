import {readFileSync,writeFileSync} from 'node:fs';
import {encryptBackup,decryptBackup} from './backup-lib.mjs';
const [mode,input,output]=process.argv.slice(2),passphrase=process.env.ZAMCAN_BACKUP_PASSPHRASE;
if(!['encrypt','decrypt'].includes(mode)||!input||!output||!passphrase)throw Error('Usage: ZAMCAN_BACKUP_PASSPHRASE supplied securely; node tools/backup.mjs encrypt|decrypt input output');
const data=mode==='encrypt'?JSON.stringify(encryptBackup(readFileSync(input),passphrase)):decryptBackup(JSON.parse(readFileSync(input,'utf8')),passphrase);
writeFileSync(output,data,{mode:0o600,flag:'wx'});console.log('Backup operation completed. Keep recovery passphrases separately from encrypted backups.');
