import fs from 'node:fs';
const config=JSON.parse(fs.readFileSync('wrangler.jsonc','utf8'));
const failures=[];
if(config.d1_databases.some(d=>!d.database_id||d.database_id.startsWith('00000000')))failures.push('Set the owner-created D1 database ID.');
if(config.vars.PUBLIC_ORIGIN.includes('replace-with'))failures.push('Set the actual HTTPS Worker origin.');
if(!config.vars.ISSUER_KEY_ID.startsWith(config.vars.PUBLIC_ORIGIN+'/#keys/'))failures.push('Issuer key identifier must belong to the configured origin.');
if(!fs.existsSync('public/app.js'))failures.push('Build the dashboard first.');
if(failures.length){for(const error of failures)console.error(error);process.exitCode=1;}else console.log('Static deployment configuration passed. Secrets, identity, bank review and live end-to-end checks are still required.');
