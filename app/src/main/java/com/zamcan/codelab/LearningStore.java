package com.zamcan.codelab;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/** Subject-neutral progress records; never trusted as certificate eligibility. */
public final class LearningStore extends SQLiteOpenHelper {
    private final String ownerScope;
    private final boolean guest;
    /** Guest storage is never automatically treated as authenticated account storage. */
    public LearningStore(Context context){this(context,null);}
    /** An authenticated Appwrite user ID must be validated by Account.get() before use here. */
    public LearningStore(Context context,String verifiedAppwriteUserId){
        super(context,databaseName(verifiedAppwriteUserId),null,2);
        guest=verifiedAppwriteUserId==null;
        ownerScope=guest?"guest":verifiedAppwriteUserId;
    }
    private static String databaseName(String id){
        return id==null?"learning.db":LearnerDatabaseNames.forVerifiedUser(id);
    }
    public boolean isGuest(){return guest;}
    public String ownerScope(){return ownerScope;}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE progress(course TEXT NOT NULL, lesson TEXT NOT NULL, completed_at INTEGER NOT NULL, PRIMARY KEY(course,lesson))");
        db.execSQL("CREATE TABLE metadata(name TEXT PRIMARY KEY, value TEXT NOT NULL)");
        db.execSQL("CREATE TABLE sync_queue(course TEXT NOT NULL, lesson TEXT NOT NULL, queued_at INTEGER NOT NULL, PRIMARY KEY(course,lesson))");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        if(oldVersion==1 && newVersion==2){
            db.execSQL("CREATE TABLE sync_queue(course TEXT NOT NULL, lesson TEXT NOT NULL, queued_at INTEGER NOT NULL, PRIMARY KEY(course,lesson))");
            // Existing progress must be synchronized when an authenticated session is established.
            db.execSQL("INSERT OR IGNORE INTO sync_queue SELECT course,lesson,completed_at FROM progress");
            return;
        }
        throw new IllegalStateException("A reviewed migration is required");
    }
    public boolean completed(String course,String lesson){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM progress WHERE course=? AND lesson=?",new String[]{course,lesson})){return c.moveToFirst();}
    }
    public void complete(String course,String lesson){
        ContentValues values=new ContentValues();values.put("course",course);values.put("lesson",lesson);values.put("completed_at",System.currentTimeMillis());
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try {
            db.insertWithOnConflict("progress",null,values,SQLiteDatabase.CONFLICT_IGNORE);
            if(!guest)db.execSQL("INSERT OR IGNORE INTO sync_queue(course,lesson,queued_at) VALUES(?,?,?)",new Object[]{course,lesson,System.currentTimeMillis()});
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    /** Portable, minimal snapshot for a future authenticated sync service. */
    public synchronized JSONObject exportProgress() throws JSONException {
        JSONObject result=new JSONObject();
        result.put("schema",1);
        JSONArray entries=new JSONArray();
        try(Cursor cursor=getReadableDatabase().rawQuery(
                "SELECT course,lesson,completed_at FROM progress ORDER BY course,lesson",null)){
            while(cursor.moveToNext()){
                JSONObject entry=new JSONObject();
                entry.put("course",cursor.getString(0));
                entry.put("lesson",cursor.getString(1));
                entry.put("completed_at",cursor.getLong(2));
                entries.put(entry);
            }
        }
        result.put("progress",entries);
        return result;
    }
    /** Monotonic merge: a cloud snapshot cannot erase completed local lessons. */
    public synchronized void mergeProgress(JSONObject snapshot) throws JSONException {
        if(snapshot.optInt("schema",-1)!=1)throw new JSONException("Unsupported progress schema");
        JSONArray entries=snapshot.optJSONArray("progress");
        if(entries==null || entries.length()>10000)throw new JSONException("Invalid progress count");
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try{
            for(int i=0;i<entries.length();i++){
                JSONObject entry=entries.getJSONObject(i);
                String course=entry.getString("course"),lesson=entry.getString("lesson");
                long completedAt=entry.getLong("completed_at");
                if(course.length()<1||course.length()>100||lesson.length()<1||lesson.length()>100||
                   !course.matches("[A-Za-z0-9_-]+")||!lesson.matches("[A-Za-z0-9_-]+")||
                   completedAt<=0||completedAt>System.currentTimeMillis()+86400000L)
                    throw new JSONException("Invalid progress entry");
                // Android 8 ships SQLite older than 3.24; avoid UPSERT syntax.
                ContentValues values=new ContentValues();
                values.put("course",course);values.put("lesson",lesson);values.put("completed_at",completedAt);
                db.insertWithOnConflict("progress",null,values,SQLiteDatabase.CONFLICT_IGNORE);
                db.execSQL("UPDATE progress SET completed_at=MIN(completed_at,?) WHERE course=? AND lesson=?",
                    new Object[]{completedAt,course,lesson});
            }
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }

    /** Account-only upload queue. Guest entries are never sent to a cloud account. */
    public synchronized JSONArray pendingUploads() throws JSONException {
        JSONArray pending=new JSONArray();
        if(guest)return pending;
        try(Cursor cursor=getReadableDatabase().rawQuery(
                "SELECT p.course,p.lesson,p.completed_at FROM progress p INNER JOIN sync_queue q ON p.course=q.course AND p.lesson=q.lesson ORDER BY q.queued_at",null)){
            while(cursor.moveToNext()){
                JSONObject item=new JSONObject();
                item.put("course",cursor.getString(0));
                item.put("lesson",cursor.getString(1));
                item.put("completed_at",cursor.getLong(2));
                pending.put(item);
            }
        }
        return pending;
    }
    /** Acknowledgment is only valid after a successful remote write for this exact owner. */
    public synchronized void acknowledgeUpload(String verifiedOwner,String course,String lesson,long uploadedCompletedAt) {
        if(guest||!ownerScope.equals(verifiedOwner))
            throw new SecurityException("Cannot acknowledge another learner's sync queue");
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try(Cursor c=db.rawQuery("SELECT p.completed_at FROM progress p INNER JOIN sync_queue q ON p.course=q.course AND p.lesson=q.lesson WHERE p.course=? AND p.lesson=?",new String[]{course,lesson})){
            if(c.moveToFirst() && c.getLong(0)==uploadedCompletedAt)
                db.delete("sync_queue","course=? AND lesson=?",new String[]{course,lesson});
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public void importLegacy(SharedPreferences preferences){
        if(!isGuest())throw new SecurityException("Legacy guest progress must not be imported into an account automatically");
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT 1 FROM metadata WHERE name='legacy_import'",null)){if(c.moveToFirst())return;}
        db.beginTransaction();
        try{
            for(String language:new String[]{"c","cpp"})for(int i=0;i<10;i++){
                if(!preferences.getBoolean(language+"-"+i,false))continue;
                ContentValues values=new ContentValues();
                values.put("course",language+"-foundation");
                values.put("lesson",Integer.toString(i));
                values.put("completed_at",System.currentTimeMillis());
                db.insertWithOnConflict("progress",null,values,SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.execSQL("INSERT INTO metadata VALUES('legacy_import','1')");
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
}
