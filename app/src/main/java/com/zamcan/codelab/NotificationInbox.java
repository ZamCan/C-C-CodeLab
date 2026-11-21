package com.zamcan.codelab;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Private on-device inbox for learning events. Remote delivery is a separate opt-in integration. */
public final class NotificationInbox {
    private static final int MAX_ITEMS=80;
    private NotificationInbox(){}
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("codelab_inbox",Context.MODE_PRIVATE);}
    public static synchronized JSONArray items(Context c){
        try{return new JSONArray(prefs(c).getString("items","[]"));}catch(Exception e){return new JSONArray();}
    }
    public static synchronized void add(Context c,String title,String body){
        try{
            JSONArray old=items(c), next=new JSONArray();
            JSONObject entry=new JSONObject().put("title",title).put("body",body).put("time",System.currentTimeMillis()).put("read",false);
            next.put(entry);
            for(int i=0;i<old.length()&&i<MAX_ITEMS-1;i++)next.put(old.getJSONObject(i));
            prefs(c).edit().putString("items",next.toString()).apply();
        }catch(Exception ignored){}
    }
    public static synchronized void markRead(Context c){
        try{
            JSONArray data=items(c);
            for(int i=0;i<data.length();i++)data.getJSONObject(i).put("read",true);
            prefs(c).edit().putString("items",data.toString()).apply();
        }catch(Exception ignored){}
    }
    public static int unread(Context c){
        JSONArray data=items(c);int n=0;
        for(int i=0;i<data.length();i++)if(data.optJSONObject(i)!=null&&!data.optJSONObject(i).optBoolean("read",false))n++;
        return n;
    }
}
