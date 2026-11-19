package com.zamcan.codelab;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.*;
import org.json.*;
import io.appwrite.Client;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.Row;
import io.appwrite.models.RowList;
import io.appwrite.services.TablesDB;

/** Serial, identity-checked eventual synchronization. Never sync guest data. */
public final class LearningSync {
    private static final String DB="codelab-learning", TABLE="lesson-progress";
    private final TablesDB tables;
    private final CodeLabAccount identity;
    private final Handler main=new Handler(Looper.getMainLooper());
    private boolean running;
    public LearningSync(Context context){
        identity=CodeLabAccount.shared(context);
        tables=new TablesDB(new Client(context.getApplicationContext())
            .setEndpoint("https://cloud.appwrite.io/v1").setProject("codelab-zamcan"));
    }
    public synchronized void sync(LearningStore store) {
        if(running || store==null || store.isGuest())return;
        running=true;
        final String owner=store.ownerScope();
        identity.verify(new CodeLabAccount.Result(){
            @Override public void authenticated(String verified) {
                if(!owner.equals(verified)){stop();return;}
                upload(store,owner,0);
            }
            @Override public void unauthenticated(String reason){stop();}
        });
    }
    private void upload(LearningStore store,String owner,int index){
        final JSONArray pending;
        try{pending=store.pendingUploads();}catch(Exception e){stop();return;}
        if(pending.length()==0){download(store,owner,null);return;}
        final JSONObject item=pending.optJSONObject(0);
        if(item==null){stop();return;}
        final String course=item.optString("course"),lesson=item.optString("lesson");
        final long at=item.optLong("completed_at");
        final String rowId=ProgressRowIds.forLesson(owner,course,lesson);
        Map<String,Object> data=new HashMap<>();
        data.put("course",course);data.put("lesson",lesson);data.put("completed_at",at);data.put("schema_version",1);
        List<String> permissions=Arrays.asList("read(\"user:"+owner+"\")","update(\"user:"+owner+"\")","delete(\"user:"+owner+"\")");
        tables.createRow(DB,TABLE,rowId,data,permissions,new CoroutineCallback<>((row,error)->{
            if(error==null){ack(store,owner,course,lesson,at);return;}
            // A previously uploaded row may already exist after an interrupted acknowledgement.
            // Never update on arbitrary errors: first verify that this owner can read the row.
            tables.getRow(DB,TABLE,rowId,new CoroutineCallback<>((existing,readError)->{
                if(readError!=null || existing==null){stop();return;}
                tables.updateRow(DB,TABLE,rowId,data,null,new CoroutineCallback<>((updated,updateError)->{
                    if(updateError!=null){stop();return;}
                    ack(store,owner,course,lesson,at);
                }));
            }));
        }));
    }
    private void ack(LearningStore store,String owner,String course,String lesson,long at){
        try{store.acknowledgeUpload(owner,course,lesson,at);}catch(Exception e){stop();return;}
        upload(store,owner,0);
    }
    private void download(LearningStore store,String owner,String after){
        List<String> queries=new ArrayList<>();
        queries.add(Query.limit(100));
        if(after!=null)queries.add(Query.cursorAfter(after));
        tables.listRows(DB,TABLE,queries,new CoroutineCallback<>((result,error)->{
            if(error!=null || result==null){stop();return;}
            try{
                RowList<?> page=(RowList<?>)result;
                JSONArray entries=new JSONArray();
                String last=null;
                for(Object value:page.getRows()){
                    Row<?> row=(Row<?>)value;
                    last=row.getId();
                    Map<?,?> data=(Map<?,?>)row.getData();
                    JSONObject entry=new JSONObject();
                    entry.put("course",data.get("course"));
                    entry.put("lesson",data.get("lesson"));
                    entry.put("completed_at",((Number)data.get("completed_at")).longValue());
                    entries.put(entry);
                }
                JSONObject snapshot=new JSONObject();snapshot.put("schema",1);snapshot.put("progress",entries);
                store.mergeProgress(snapshot);
                if(entries.length()==100 && last!=null)download(store,owner,last);
                else stop();
            }catch(Exception e){stop();}
        }));
    }
    private synchronized void stop(){running=false;}
}
