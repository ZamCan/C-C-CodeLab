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
        super(context,databaseName(verifiedAppwriteUserId),null,1);
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
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new IllegalStateException("A reviewed migration is required");}
    public boolean completed(String course,String lesson){
        try(Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM progress WHERE course=? AND lesson=?",new String[]{course,lesson})){return c.moveToFirst();}
    }
    public void complete(String course,String lesson){
        ContentValues values=new ContentValues();values.put("course",course);values.put("lesson",lesson);values.put("completed_at",System.currentTimeMillis());
        getWritableDatabase().insertWithOnConflict("progress",null,values,SQLiteDatabase.CONFLICT_IGNORE);
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
    public void importLegacy(SharedPreferences preferences){
        if(!isGuest())throw new SecurityException("Legacy guest progress must not be imported into an account automatically");
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT 1 FROM metadata WHERE name='legacy_import'",null)){if(c.moveToFirst())return;}
        db.beginTransaction();
        try{for(String language:new String[]{"c","cpp"})for(int i=0;i<10;i++)if(preferences.getBoolean(language+"-"+i,false))complete(language+"-foundation",Integer.toString(i));db.execSQL("INSERT INTO metadata VALUES('legacy_import','1')");db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
}
