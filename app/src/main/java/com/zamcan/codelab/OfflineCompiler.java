package com.zamcan.codelab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import org.json.JSONObject;

/** Local-only WebAssembly renderer. No JavaScript interface or filesystem/network access. */
public final class OfflineCompiler {
    private static final String ORIGIN="https://compiler.codelab.invalid";
    public interface Listener { void onEvent(JSONObject event); }
    private final WebView view;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WebMessagePort port;
    private boolean ready,closed,busy;
    private final Listener listener;
    private final Runnable watchdog=()->{emit("failure","Compiler exceeded the total time limit; reopen the editor to restart.");close();};

    @SuppressLint("SetJavaScriptEnabled")
    public OfflineCompiler(Context context, Listener listener) {
        this.listener=listener;
        view=new WebView(context);
        WebSettings settings=view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setBlockNetworkLoads(true);settings.setDomStorageEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        view.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest request){
                Uri uri=request.getUrl();String path=uri.getPath();
                if(!"https".equals(uri.getScheme())||!"compiler.codelab.invalid".equals(uri.getHost())||uri.getPort()!=-1||uri.getQuery()!=null||uri.getFragment()!=null||uri.getUserInfo()!=null||!"GET".equals(request.getMethod())||path==null||!path.matches("/[A-Za-z0-9_.-]+"))return denied();
                String name=path.substring(1);
                if(!java.util.Arrays.asList("index.html","controller.js","worker.js","shared.js","engine.js","clang22-noeh","lld22-noeh","sysroot22.tar","memfs","empty.tar").contains(name))return denied();
                try{return new WebResourceResponse(name.endsWith(".html")?"text/html":name.endsWith(".js")?"application/javascript":"application/octet-stream","UTF-8",200,"OK",Collections.singletonMap("Cache-Control","no-store"),openAsset(context,name));}
                catch(IOException error){return denied();}
            }
            @Override public void onPageFinished(WebView v,String url){
                if(closed||port!=null||!url.equals(ORIGIN+"/index.html"))return;
                WebMessagePort[] channels=v.createWebMessageChannel();port=channels[0];
                port.setWebMessageCallback(new WebMessagePort.WebMessageCallback(){
                    @Override public void onMessage(WebMessagePort p,WebMessage message){
                        if(closed||message.getData()==null||message.getData().length()>100000)return;
                        try{
                            JSONObject event=new JSONObject(message.getData());String type=event.optString("type");
                            if(type.equals("ready")){ready=true;handler.removeCallbacks(watchdog);}
                            if(type.equals("result")||type.equals("failure")||type.equals("cancelled")){busy=false;handler.removeCallbacks(watchdog);}
                            listener.onEvent(event);
                        }catch(Exception error){emit("failure","Invalid compiler response");}
                    }
                });
                v.postWebMessage(new WebMessage("connect",new WebMessagePort[]{channels[1]}),Uri.parse(ORIGIN));
            }
            @Override public boolean onRenderProcessGone(WebView v,RenderProcessGoneDetail detail){emit("failure","Compiler renderer stopped. Reopen the editor to restart.");close();return true;}
            @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError error){if(r.isForMainFrame())emit("failure","Offline compiler could not load: "+error.getDescription());}
        });
        handler.postDelayed(watchdog,30000);
        view.loadUrl(ORIGIN+"/index.html");
    }
    private static java.io.InputStream openAsset(Context context,String name) throws IOException {
        if(java.util.Arrays.asList("clang22-noeh","lld22-noeh","sysroot22.tar","memfs").contains(name))return new java.util.zip.GZIPInputStream(context.getAssets().open("compiler/"+name+".gz"));
        return context.getAssets().open("compiler/"+name);
    }
    private static WebResourceResponse denied(){return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));}
    private void emit(String type,String message){try{listener.onEvent(new JSONObject().put("type",type).put("message",message));}catch(Exception ignored){}}
    public boolean isReady(){return ready&&!closed&&!busy;}
    public void run(JSONObject request){if(!isReady()){emit("failure","Compiler is loading or already running");return;}busy=true;handler.postDelayed(watchdog,125000);port.postMessage(new WebMessage(request.toString()));}
    public void cancel(){if(closed||port==null)return;port.postMessage(new WebMessage("{\"cancel\":true}"));}
    public void close(){if(closed)return;closed=true;ready=false;handler.removeCallbacksAndMessages(null);if(port!=null)port.close();view.stopLoading();view.destroy();}
}
