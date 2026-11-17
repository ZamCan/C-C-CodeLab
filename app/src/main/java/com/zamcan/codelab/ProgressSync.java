package com.zamcan.codelab;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Optional authenticated sync. Never runs without a verified identity token and explicit opt-in. */
public final class ProgressSync {
    public interface Callback { void finished(boolean success,String status); }
    private final LearningStore store;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    public ProgressSync(LearningStore store){this.store=store;}

    public void sync(String endpoint,String cognitoIdToken,Callback callback){
        if(callback==null)throw new IllegalArgumentException("callback");
        if(endpoint==null||!endpoint.startsWith("https://")||cognitoIdToken==null||
           cognitoIdToken.length()<30||cognitoIdToken.length()>8192){
            main.post(()->callback.finished(false,"Cloud sign-in is required"));return;
        }
        final URL url;
        try{
            url=new URL(endpoint);
            if(!"https".equals(url.getProtocol())||url.getUserInfo()!=null||
                url.getHost().isEmpty()||url.getPort()!=-1||url.getRef()!=null||
                !"/v1/progress".equals(url.getPath())||url.getQuery()!=null)
                throw new IllegalArgumentException("Invalid sync endpoint");
        }catch(Exception e){main.post(()->callback.finished(false,"Invalid sync endpoint"));return;}
        executor.execute(()->{
            try{
                // Fetch first; merge with device data, then upload union.
                JSONObject remote=request(url,cognitoIdToken,"GET",null);
                store.mergeProgress(remote);
                JSONObject local=store.exportProgress();
                JSONObject updated=request(url,cognitoIdToken,"PUT",local.toString());
                store.mergeProgress(updated);
                main.post(()->callback.finished(true,"Progress synchronized"));
            }catch(Exception e){
                main.post(()->callback.finished(false,"Sync unavailable; local progress is safe"));
            }
        });
    }
    private static JSONObject request(URL url,String token,String method,String body)throws Exception{
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        try{
            // Never follow a redirect with an identity token to another host.
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(12000);conn.setReadTimeout(15000);
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization","Bearer "+token);
            conn.setRequestProperty("Accept","application/json");
            conn.setUseCaches(false);
            if(body!=null){
                byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
                if(bytes.length>262144)throw new IOException("Snapshot too large");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
                conn.setFixedLengthStreamingMode(bytes.length);
                try(OutputStream out=conn.getOutputStream()){out.write(bytes);}
            }
            int status=conn.getResponseCode();
            if(status<200||status>=300)throw new IOException("Sync HTTP "+status);
            try(InputStream input=conn.getInputStream();ByteArrayOutputStream output=new ByteArrayOutputStream()){
                byte[] buffer=new byte[8192];int count;
                while((count=input.read(buffer))!=-1){
                    if(output.size()+count>262144)throw new IOException("Response too large");
                    output.write(buffer,0,count);
                }
                return new JSONObject(output.toString("UTF-8"));
            }
        }finally{conn.disconnect();}
    }
    public void close(){executor.shutdownNow();}
}
