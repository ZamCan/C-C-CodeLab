package com.zamcan.codelab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.webkit.WebView;
import android.widget.*;
import android.graphics.Typeface;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import org.json.JSONObject;

/** Runs the shipped engine on the phone; exports observations rather than prewritten claims. */
public final class CompilerDiagnosticsActivity extends Activity {
    private OfflineCompiler compiler;
    private TextView output;
    private Button export;
    private int index,passed;
    private long started;
    private String stdout="",stderr="",diagnostics="";
    private final StringBuilder report=new StringBuilder();
    private final String[][] cases={
        {"C input / output","main.c","#include <stdio.h>\nint main(void){int n;if(scanf(\"%d\",&n)!=1)return 2;printf(\"%d\",n*2);}","21\n","42","", "0"},
        {"C++ containers","main.cpp","#include <iostream>\n#include <vector>\n#include <algorithm>\nint main(){std::vector<int> v{3,1,2};std::sort(v.begin(),v.end());std::cout<<v.front();}","","1","","0"},
        {"Separate stderr","main.c","#include <stdio.h>\nint main(void){fputs(\"out\",stdout);fputs(\"err\",stderr);}","","out","err","0"},
        {"Exit status","main.c","int main(void){return 7;}","","","","7"},
        {"Syntax rejection","main.c","int main(void){return missing_symbol;}","","","","compile-error"},
        {"Loop timeout","main.c","int main(void){for(;;){}}","","","","timeout"}
    };
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,32,24,24);root.setFitsSystemWindows(true);
        TextView title=new TextView(this);title.setText("Compiler self-test");title.setTextSize(24);root.addView(title);
        ScrollView scroll=new ScrollView(this);output=new TextView(this);output.setTextSize(14);output.setTypeface(Typeface.MONOSPACE);output.setTextIsSelectable(true);scroll.addView(output);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        export=new Button(this);export.setText("Export report");export.setEnabled(false);root.addView(export);export.setOnClickListener(v->startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,"CodeLab-device-compiler-test.txt"),42));
        Button back=new Button(this);back.setText("Close");back.setOnClickListener(v->finish());root.addView(back);setContentView(root);
        log("Device: "+Build.MANUFACTURER+" "+Build.MODEL+" / API "+Build.VERSION.SDK_INT+" / "+String.join(",",Build.SUPPORTED_ABIS));
        android.content.pm.PackageInfo webview=WebView.getCurrentWebViewPackage();log("WebView: "+(webview==null?"unavailable":webview.packageName+" "+webview.versionName));
        log("Clang 22.1.8 / C17 / C++17 / WASI. Loading bundled engine…");
        compiler=new OfflineCompiler(this,event->{
            String type=event.optString("type");
            if(type.equals("ready")){next();return;}
            if(type.equals("output")){String stream=event.optString("stream"),text=event.optString("text");if(stream.equals("stdout"))stdout+=text;else if(stream.equals("stderr"))stderr+=text;else diagnostics+=text;return;}
            if(type.equals("result")||type.equals("failure")){
                if(index>=cases.length)return;
                String[] test=cases[index];String expect=test[6];
                boolean ok=expect.equals("compile-error")?type.equals("failure")&&diagnostics.contains("missing_symbol"):
                    expect.equals("timeout")?type.equals("failure")&&event.optString("message").contains("Time limit"):
                    type.equals("result")&&event.isNull("trap")&&expect.equals(event.optString("exitCode"))&&stdout.equals(test[4])&&stderr.equals(test[5]);
                if(ok)passed++;
                log((ok?"PASS ":"FAIL ")+test[0]+" ("+(android.os.SystemClock.elapsedRealtime()-started)+" ms)");
                log("stdout="+JSONObject.quote(stdout)+" stderr="+JSONObject.quote(stderr)+"\n"+event.toString()+"\n"+diagnostics);
                index++;
                if(!compiler.isReady()){log("Engine unavailable; remaining checks not run.");finishReport();}
                else next();
            }
        });
    }
    private void next(){
        if(index>=cases.length){finishReport();return;}
        stdout="";stderr="";diagnostics="";String[] test=cases[index];started=android.os.SystemClock.elapsedRealtime();log("RUN "+test[0]);
        try{compiler.run(new JSONObject().put("files",new JSONObject().put(test[1],test[2])).put("stdin",test[3]));}catch(Exception e){log("FAIL "+e.getMessage());finishReport();}
    }
    private void finishReport(){log("Observed passes: "+passed+" / "+cases.length+". This report covers compiler checks only.");export.setEnabled(true);}
    private void log(String text){report.append(text).append('\n');output.setText(report.toString());}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=42||result!=RESULT_OK||data==null||data.getData()==null)return;try(OutputStream stream=getContentResolver().openOutputStream(data.getData())){if(stream==null)throw new java.io.IOException("Cannot write report");stream.write(report.toString().getBytes(StandardCharsets.UTF_8));Toast.makeText(this,"Report saved",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
    @Override protected void onDestroy(){if(compiler!=null)compiler.close();super.onDestroy();}
}
