package com.zamcan.codelab;

import android.content.Context;
import java.util.*;
import org.json.*;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.Row;
import io.appwrite.models.RowList;
import io.appwrite.services.TablesDB;

/** Private, merge-only progress sync. Guest progress is never uploaded. */
public final class LearningSync {
    private static final String DB="codelab-learning", TABLE="lesson-progress";
    private final TablesDB tables;
    private final CodeLabAccount identity;
    private long generation;
    private boolean running;
    private String activeOwner;
    public LearningSync(Context context) {
        identity=CodeLabAccount.shared(context);
        tables=new TablesDB(identity.client());
    }
    /** Invalidates all callbacks from an earlier account or an abandoned sync. */
    public synchronized void cancel() {
        generation++;
        running=false;
        activeOwner=null;
    }
    private synchronized boolean current(long token,String owner) {
        return running && generation==token && owner.equals(activeOwner);
    }
    private synchronized void finish(long token,String owner) {
        if(current(token,owner)){running=false;activeOwner=null;}
    }
    public void sync(LearningStore store) {
        if(store==null || store.isGuest())return;
        final long token;
        final String owner=store.ownerScope();
        synchronized(this) {
            if(running)return;
            running=true;
            activeOwner=owner;
            token=++generation;
        }
        identity.verify(new CodeLabAccount.Result(){
            @Override public void authenticated(String verified) {
                if(!current(token,owner))return;
                if(!owner.equals(verified)){finish(token,owner);return;}
                upload(store,owner,token);
            }
            @Override public void unauthenticated(String reason){finish(token,owner);}
            @Override public void unavailable(String reason){finish(token,owner);}
        });
    }
    private void upload(LearningStore store,String owner,long token) {
        if(!current(token,owner))return;
        final JSONArray pending;
        try {pending=store.pendingUploads();}catch(Exception e){finish(token,owner);return;}
        if(pending.length()==0){download(store,owner,token,null);return;}
        JSONObject item=pending.optJSONObject(0);
        if(item==null){finish(token,owner);return;}
        String course=item.optString("course"),lesson=item.optString("lesson");
        long at=item.optLong("completed_at");
        final String rowId;
        try{rowId=ProgressRowIds.forLesson(owner,course,lesson);}
        catch(Exception e){finish(token,owner);return;}
        Map<String,Object> data=payload(course,lesson,at);
        List<String> permissions=Arrays.asList(
            "read(\"user:"+owner+"\")",
            "update(\"user:"+owner+"\")",
            "delete(\"user:"+owner+"\")");
        tables.createRow(DB,TABLE,rowId,data,permissions,new CoroutineCallback<>((created,error)->{
            if(!current(token,owner))return;
            if(error==null){ack(store,owner,token,course,lesson,at);return;}
            // A duplicate may be the result of an interrupted upload. Never
            // overwrite it blindly: read it under the current user's ACL.
            tables.getRow(DB,TABLE,rowId,new CoroutineCallback<>((existing,readError)->{
                if(!current(token,owner))return;
                if(readError!=null || existing==null){finish(token,owner);return;}
                try {
                    Row<?> row=(Row<?>)existing;
                    Map<?,?> remote=(Map<?,?>)row.getData();
                    if(!course.equals(remote.get("course")) || !lesson.equals(remote.get("lesson")))
                        throw new SecurityException("Cloud row key mismatch");
                    long remoteAt=((Number)remote.get("completed_at")).longValue();
                    if(remoteAt<=0)throw new IllegalStateException("Invalid remote timestamp");
                    if(remoteAt<=at){ack(store,owner,token,course,lesson,at);return;}
                    tables.updateRow(DB,TABLE,rowId,data,null,new CoroutineCallback<>((updated,updateError)->{
                        if(!current(token,owner))return;
                        if(updateError!=null){finish(token,owner);return;}
                        ack(store,owner,token,course,lesson,at);
                    }));
                }catch(Exception e){finish(token,owner);}
            }));
        }));
    }
    private static Map<String,Object> payload(String course,String lesson,long at) {
        Map<String,Object> data=new HashMap<>();
        data.put("course",course);
        data.put("lesson",lesson);
        data.put("completed_at",at);
        data.put("schema_version",1);
        return data;
    }
    private void ack(LearningStore store,String owner,long token,String course,String lesson,long at) {
        if(!current(token,owner))return;
        try {store.acknowledgeUpload(owner,course,lesson,at);}
        catch(Exception e){finish(token,owner);return;}
        upload(store,owner,token);
    }
    private void download(LearningStore store,String owner,long token,String after) {
        if(!current(token,owner))return;
        List<String> queries=new ArrayList<>();
        queries.add(Query.Companion.limit(100));
        if(after!=null)queries.add(Query.Companion.cursorAfter(after));
        tables.listRows(DB,TABLE,queries,new CoroutineCallback<>((result,error)->{
            if(!current(token,owner))return;
            if(error!=null || result==null){finish(token,owner);return;}
            try {
                RowList<?> page=(RowList<?>)result;
                JSONArray entries=new JSONArray();
                String last=null;
                for(Object value:page.getRows()) {
                    Row<?> row=(Row<?>)value;
                    Map<?,?> data=(Map<?,?>)row.getData();
                    String course=(String)data.get("course");
                    String lesson=(String)data.get("lesson");
                    if(!row.getId().equals(ProgressRowIds.forLesson(owner,course,lesson)))
                        throw new SecurityException("Progress row ownership mismatch");
                    JSONObject entry=new JSONObject();
                    entry.put("course",course);
                    entry.put("lesson",lesson);
                    entry.put("completed_at",((Number)data.get("completed_at")).longValue());
                    entries.put(entry);
                    last=row.getId();
                }
                JSONObject snapshot=new JSONObject();
                snapshot.put("schema",1);
                snapshot.put("progress",entries);
                if(!current(token,owner))return;
                store.mergeProgress(snapshot);
                if(entries.length()==100 && last!=null)download(store,owner,token,last);
                else finish(token,owner);
            }catch(Exception e){finish(token,owner);}
        }));
    }
}
