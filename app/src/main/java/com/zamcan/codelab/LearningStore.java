package com.zamcan.codelab;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

/** Subject-neutral progress records; never trusted as certificate eligibility. */
public final class LearningStore extends SQLiteOpenHelper {
    public LearningStore(Context context){super(context,"learning.db",null,1);}
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
    public void importLegacy(SharedPreferences preferences){
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.rawQuery("SELECT 1 FROM metadata WHERE name='legacy_import'",null)){if(c.moveToFirst())return;}
        db.beginTransaction();
        try{for(String language:new String[]{"c","cpp"})for(int i=0;i<10;i++)if(preferences.getBoolean(language+"-"+i,false))complete(language+"-foundation",Integer.toString(i));db.execSQL("INSERT INTO metadata VALUES('legacy_import','1')");db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
}
