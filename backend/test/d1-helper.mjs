import {DatabaseSync} from 'node:sqlite';
import {readFileSync,readdirSync} from 'node:fs';
export function database(){
 const db=new DatabaseSync(':memory:');db.exec('PRAGMA foreign_keys=ON');
 for(const file of readdirSync(new URL('../migrations/',import.meta.url)).filter(x=>x.endsWith('.sql')).sort())db.exec(readFileSync(new URL('../migrations/'+file,import.meta.url),'utf8'));
 function prepare(sql,values=[]){return {bind(...args){return prepare(sql,args);},async first(){return db.prepare(sql).get(...values)||null;},async all(){return {results:db.prepare(sql).all(...values)};},_run(){const result=db.prepare(sql).run(...values);return {success:true,meta:{changes:Number(result.changes)}};},async run(){return this._run();}};}
 return {prepare,async batch(statements){db.exec('BEGIN');try{const result=statements.map(s=>s._run());db.exec('COMMIT');return result;}catch(error){db.exec('ROLLBACK');throw error;}},raw:db,close:()=>db.close()};
}
