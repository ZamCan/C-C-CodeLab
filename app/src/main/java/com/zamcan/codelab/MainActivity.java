package com.zamcan.codelab;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Offline academy and source workspace. No fabricated compiler or certificate results. */
public final class MainActivity extends Activity {
    private static final int INK=0xff202536, MUTED=0xff596277, MINT=0xff425ff5, LILAC=0xff8256d9;
    private LinearLayout content;
    private ProjectStore store;
    private WorkspaceRepository workspaces;
    private String project="Practice";
    private LearningStore progress;
    private RewardedLessonGate rewardedGate;
    private CompilerEngine compilerEngine;
    private final java.util.concurrent.ExecutorService compilerWorker=java.util.concurrent.Executors.newSingleThreadExecutor();
    private android.content.SharedPreferences preferences;
    private boolean cpp, dirty, highlighting;
    private String screen="home", filename="main.c", exportSource="";
    private EditText editor;
    private TextView saveStatus;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable autosave=() -> saveSource(false);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences=getSharedPreferences("academy", MODE_PRIVATE);
        progress=new LearningStore(this);
        rewardedGate=new RewardedLessonGate(this);
        compilerEngine=OfflineCompilerBootstrap.create(this);
        progress.importLegacy(preferences);
        cpp=preferences.getBoolean("cpp", false);
        filename=preferences.getString("file", "main.c");
        try {
            workspaces=new WorkspaceRepository(getFilesDir());
            project=preferences.getString("project","Practice");
            store=workspaces.open(project);
            if (!store.exists("main.c")) store.write("main.c", Curriculum.C[0].code);
            if (!store.exists("main.cpp")) store.write("main.cpp", Curriculum.CPP[0].code);
        } catch(IOException e) { fatal(e); return; }
        if(state!=null && "editor".equals(state.getString("screen"))) openEditor(filename); else home();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        saveSource(false); out.putString("screen",screen); super.onSaveInstanceState(out);
    }
    @Override protected void onPause() { saveSource(false); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); compilerWorker.shutdownNow(); super.onDestroy(); }
    @Override public void onBackPressed() {
        if("home".equals(screen)) super.onBackPressed();
        else if(leaveEditor()) home();
    }
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void fatal(Exception e) { new AlertDialog.Builder(this).setTitle("Storage unavailable").setMessage(e.getMessage()).setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show(); }
    private void error(Exception e) { new AlertDialog.Builder(this).setTitle("Could not complete action").setMessage(e.getMessage()).setPositiveButton("OK",null).show(); }
    private void message(String title,String body) { new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Got it",null).show(); }
    private boolean leaveEditor() { return editor==null || !dirty || saveSource(true); }
    private GradientDrawable glass(boolean accent) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,accent ? new int[]{0xfff0f3ff,0xfffafaff} : new int[]{0xffffffff,0xfffcfcff});
        d.setCornerRadius(dp(24)); d.setStroke(dp(1),accent ? 0x665d79e8 : 0x1d596277); return d;
    }
    private TextView label(String text,int size,int color) {
        TextView v=new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        v.setLineSpacing(dp(3),1); v.setPadding(0,dp(5),0,dp(5)); return v;
    }
    private void title(LinearLayout parent,String text,int size) { TextView t=label(text,size,INK); t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); parent.addView(t); }
    private void body(LinearLayout parent,String text) { parent.addView(label(text,15,MUTED)); }
    private LinearLayout panel(boolean accent) {
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(20),dp(18),dp(20),dp(18)); p.setBackground(glass(accent));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(16); p.setLayoutParams(lp); content.addView(p); return p;
    }
    private void action(LinearLayout parent,String text,boolean primary,Runnable run) {
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(primary?Color.WHITE:INK); b.setMinHeight(dp(50)); b.setPadding(dp(14),dp(10),dp(14),dp(10));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?new int[]{MINT,0xff7851db}:new int[]{0xfff2f4fa,0xfff2f4fa});
        background.setCornerRadius(dp(15)); background.setStroke(dp(1),primary?0x00ffffff:0x33596277);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x40ffffff),background,null));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(10); b.setLayoutParams(lp);
        b.setOnClickListener(v->run.run()); parent.addView(b);
    }
    private void shell(String name,String eyebrow,String heading,String subtitle) {
        handler.removeCallbacks(autosave); editor=null; dirty=false; screen=name;
        FrameLayout root=new FrameLayout(this); root.setBackgroundColor(Color.WHITE);
        LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); root.addView(stack,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); stack.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(22),dp(24),dp(22),dp(14)); scroll.addView(content);
        if (!eyebrow.isEmpty()) content.addView(label(eyebrow,12,MINT)); title(content,heading,32); if (!subtitle.isEmpty()) body(content,subtitle);
        View gap=new View(this); content.addView(gap,new LinearLayout.LayoutParams(1,dp(20)));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(16),0,dp(16),dp(12)); nav.setBackgroundColor(0xfff7f8fc); stack.addView(nav);
        for(String tab:new String[]{"Compiler","Learn C/C++"}) {
            Button b=new Button(this); b.setText(tab); b.setAllCaps(false); b.setTextColor(INK); b.setBackgroundColor(Color.TRANSPARENT); b.setMinHeight(dp(56));
            nav.addView(b,new LinearLayout.LayoutParams(0,-2,1)); b.setOnClickListener(v->{if(leaveEditor()){if(tab.equals("Compiler")) studio(); else academy();}});
        }
        setContentView(root);
    }
    private void home() {
        shell("home","","C/C++ CodeLab","");
        LinearLayout studio=panel(false); studio.addView(label("01  /  CREATE",12,LILAC)); title(studio,"Compiler",23); body(studio,"Your source workspace · offline"); action(studio,"Open compiler",true,this::studio);
        LinearLayout academy=panel(false); academy.addView(label("02  /  LEARN",12,MINT)); title(academy,"Learn C/C++",23); body(academy,"Two separate courses · real progress"); action(academy,"Explore courses",false,this::academy);
        if(preferences.getBoolean("intro_completed",false)) {
            LinearLayout resume=panel(true); title(resume,"Continue learning",20);
            body(resume,(cpp?"C++":"C")+" · "+completed()+" of 10 lessons completed");
            action(resume,"Resume course",true,this::academy);
        }
        body(content,"LOCAL FIRST  •  No account required\nLearning and source files stay on this device.");
        
    }
    private void status() {
        shell("status","ABOUT THIS BUILD","Honest by design.","Development build 0.1.0");
        LinearLayout p=panel(false); title(p,"Available offline",22); body(p,"Lessons, practice quizzes, saved progress, source editing, multiple source files, and source export work without an account. Uninstalling clears local data; export important source files first.");
        LinearLayout s=panel(false); title(s,"Release gates still open",22); body(s,"An Android-compatible C/C++ compiler and isolated execution runtime are not bundled. Compile and Run are unavailable. No program output is simulated.\n\nOnline certificate exams, signed credentials, and public verification are not integrated. Practice completion is not certification.\n\nRewarded advertisements use Google's test ad unit in this development build. No production ad revenue is generated.");
        action(content,"Back home",true,this::home);
    }
    private void studio() {
        shell("studio","COMPILER","Compiler","Write C and C++.");
        LinearLayout p=panel(true); title(p,project,23); body(p,"C / C++ · Autosaved"); action(p,"Files  ⋮",false,this::fileMenu);
        for(String file:store.list()) { LinearLayout f=panel(false); title(f,file,20); action(f,"Open",false,()->openEditor(file)); }
        body(content,"Compiler setup is pending. You can edit and export sources; this build cannot execute them.");
    }
    private void fileMenu() {
        new AlertDialog.Builder(this).setTitle("Files").setItems(
            new String[]{"New file","Projects"},(dialog,which)->{if(which==0)newFile();else chooseProject();}
        ).show();
    }
    private void chooseProject(){
        java.util.List<String> projects=workspaces.list();projects.add("+ Create project");
        new AlertDialog.Builder(this).setTitle("Your projects").setItems(projects.toArray(new String[0]),(dialog,index)->{
            if(index<projects.size()-1){switchProject(projects.get(index));return;}
            EditText input=new EditText(this);input.setSingleLine(true);input.setHint("MyProject");
            AlertDialog create=new AlertDialog.Builder(this).setTitle("New project").setView(input).setPositiveButton("Create",null).setNegativeButton("Cancel",null).create();
            create.setOnShowListener(d->create.getButton(-1).setOnClickListener(v->{try{String name=input.getText().toString().trim();workspaces.create(name);create.dismiss();switchProject(name);}catch(IOException e){input.setError(e.getMessage());}}));create.show();
        }).show();
    }
    private void switchProject(String name){
        try{store=workspaces.open(name);project=name;preferences.edit().putString("project",name).apply();studio();}catch(IOException e){error(e);}
    }
    private void newFile() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint("example.cpp"); input.setPadding(dp(24),dp(12),dp(24),dp(12));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("New source file").setMessage("Use .c, .cpp, .h, or .hpp. Existing files will not be replaced.").setView(input).setPositiveButton("Create",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{String name=input.getText().toString().trim();try {if(store.exists(name)) {input.setError("That file already exists");return;}store.write(name,name.endsWith(".cpp")?Curriculum.CPP[0].code:name.endsWith(".c")?Curriculum.C[0].code:"// Header declarations\n");dialog.dismiss();openEditor(name);}catch(IOException e){input.setError(e.getMessage());}}));dialog.show();
    }
    private void openEditor(String file) {
        if(!leaveEditor()) return;
        String source; try {source=store.read(file);}catch(IOException e){error(e);return;}
        filename=file; preferences.edit().putString("file",file).apply();
        shell("editor","COMPILER",file,"Source editor");
        LinearLayout p=panel(false); saveStatus=label("Saved on this device",12,MINT); p.addView(saveStatus);
        editor=new CodeEditor(this); editor.setTextColor(INK); editor.setTextSize(preferences.getInt("font",15)); editor.setTypeface(Typeface.MONOSPACE); editor.setGravity(Gravity.TOP|Gravity.START); editor.setPadding(dp(44),dp(12),dp(8),dp(12)); editor.setBackgroundColor(0x66101729); editor.setMinLines(12); editor.setHorizontallyScrolling(true); editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ProjectStore.MAX_BYTES)}); editor.setContentDescription("Source code editor for "+file); editor.setText(source); p.addView(editor,new LinearLayout.LayoutParams(-1,-2));
        highlight(editor.getText());
        editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){}public void afterTextChanged(Editable s){if(highlighting)return;dirty=true;saveStatus.setText(R.string.saving);handler.removeCallbacks(autosave);handler.postDelayed(autosave,700);highlight(s);}});
        action(p,"Save",true,()->saveSource(true)); action(p,"Export",false,()->{if(saveSource(true)){exportSource=editor.getText().toString();Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,filename);try{startActivityForResult(intent,41);}catch(ActivityNotFoundException e){error(e);}}});
        action(p,"Text size",false,()->new AlertDialog.Builder(this).setTitle("Text size").setItems(new String[]{"Compact · 13 sp","Comfortable · 16 sp","Large · 20 sp"},(d,w)->{int size=new int[]{13,16,20}[w];preferences.edit().putInt("font",size).apply();editor.setTextSize(size);}).show());
        LinearLayout terminal=panel(false);
        terminal.addView(label("TERMINAL  /  OFFLINE",12,LILAC));
        TextView output=label(compilerEngine.available()?"Ready to compile C17/C++17":"Offline toolchain not installed",14,MUTED);
        output.setTextIsSelectable(true);
        terminal.addView(output);
        EditText stdin=new EditText(this);
        stdin.setHint("Program input (stdin)");
        stdin.setMinLines(2);
        stdin.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        terminal.addView(stdin,new LinearLayout.LayoutParams(-1,-2));
        action(terminal,"Compile & Run",true,()->{
            if(!compilerEngine.available()){
                message("Offline compiler unavailable","This APK does not yet contain a verified Android Clang/WASI toolchain and isolated WAMR runtime.");
                return;
            }
            if(!saveSource(true))return;
            final String compileSource=editor.getText().toString();
            final String input=stdin.getText().toString();
            final String compileFilename=filename;
            output.setText("Compiling...");
            compilerWorker.execute(()->{
                String result;
                try {
                    CompilerEngine.Request request=new CompilerEngine.Request(compileFilename,compileSource,CompilerEngine.languageFor(compileFilename));
                    CompilerEngine.CompileResult compile=compilerEngine.compile(request);
                    if(!compile.success)result="Compilation failed (exit "+compile.exitCode+")\n"+compile.stderr+"\n"+compile.stdout;
                    else {
                        CompilerEngine.RunResult run=compilerEngine.run(compile.artifactId,input,10000);
                        result="Exit: "+run.exitCode+(run.timedOut?" (timed out)":"")+"\n"+run.stdout+"\n"+run.stderr;
                    }
                }catch(Exception ex){result="Compiler error: "+ex.getMessage();}
                final String shown=result;
                runOnUiThread(()->{if(!isFinishing() && output.getParent()!=null)output.setText(shown);});
            });
        });
    }
    private void highlight(Editable s) {
        if(s.length()>24000) return;
        highlighting=true;
        for(ForegroundColorSpan span:s.getSpans(0,s.length(),ForegroundColorSpan.class))s.removeSpan(span);
        Pattern pattern=Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\])*\"|\\b(?:int|double|char|void|return|if|else|for|while|const|class|struct|public|private|auto|bool|namespace|include)\\b");
        Matcher matcher=pattern.matcher(s);while(matcher.find()){String token=matcher.group();int color=token.startsWith("/")?0xffa0b1c7:token.startsWith("\"")?MINT:LILAC;s.setSpan(new ForegroundColorSpan(color),matcher.start(),matcher.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}highlighting=false;
    }
    private boolean saveSource(boolean notify) {
        handler.removeCallbacks(autosave); if(editor==null)return true;
        try{store.write(filename,editor.getText().toString());dirty=false;saveStatus.setText(R.string.saved);if(notify)Toast.makeText(this,"Source saved",Toast.LENGTH_SHORT).show();return true;}catch(IOException e){dirty=true;saveStatus.setText(R.string.save_failed);if(notify)error(e);return false;}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data); if(request!=41||result!=RESULT_OK||data==null||data.getData()==null)return;
        try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IOException("Cannot open export destination");String source=editor==null?exportSource:editor.getText().toString();out.write(source.getBytes(StandardCharsets.UTF_8));Toast.makeText(this,"Source exported",Toast.LENGTH_SHORT).show();}catch(IOException|SecurityException e){error(e);}
    }
    private int completed() {int count=0;for(int i=0;i<10;i++)if(progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(i)))count++;return count;}

    /** A lesson is a sequence of short, full-screen reading pages. No scrolling. */
    private String[] storyPages(String text) {
        java.util.ArrayList<String> pages=new java.util.ArrayList<>();
        StringBuilder page=new StringBuilder();
        for(String paragraph:text.split("\\n\\s*\\n|\\n(?=[A-Z][A-Z0-9 ,—/+()?-]{7,}\\n)")) {
            String part=paragraph.trim();
            if(part.isEmpty())continue;
            while(part.length()>380) {
                int cut=part.lastIndexOf(' ',350);
                if(cut<180)cut=350;
                if(page.length()>0){pages.add(page.toString());page.setLength(0);}
                pages.add(part.substring(0,cut).trim());
                part=part.substring(cut).trim();
            }
            if(page.length()>0 && page.length()+part.length()>380){pages.add(page.toString());page.setLength(0);}
            if(page.length()>0)page.append("\\n\\n");
            page.append(part);
        }
        if(page.length()>0)pages.add(page.toString());
        return pages.isEmpty()?new String[]{"No lesson content available."}:pages.toArray(new String[0]);
    }
    private void storyShell(String eyebrow,String heading,int current,int total,String paragraph,Runnable previous,Runnable next,String nextLabel) {
        handler.removeCallbacks(autosave);editor=null;dirty=false;screen="story";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(22),dp(18),dp(22),dp(20));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(22)+insets.getSystemWindowInsetLeft(),dp(18)+insets.getSystemWindowInsetTop(),dp(22)+insets.getSystemWindowInsetRight(),dp(20)+insets.getSystemWindowInsetBottom());return insets;});
        LinearLayout progressRow=new LinearLayout(this);progressRow.setOrientation(LinearLayout.HORIZONTAL);root.addView(progressRow);
        for(int i=0;i<total;i++){View segment=new View(this);GradientDrawable bg=new GradientDrawable();bg.setColor(i<=current?MINT:0xffe9edf6);bg.setCornerRadius(dp(5));segment.setBackground(bg);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(5),1);lp.setMargins(dp(2),0,dp(2),0);progressRow.addView(segment,lp);}
        TextView close=label("×   "+eyebrow,13,MUTED);close.setPadding(0,dp(16),0,dp(8));close.setOnClickListener(v->academy());root.addView(close);
        LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.VERTICAL);center.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(center,new LinearLayout.LayoutParams(-1,0,1));
        TextView headingView=label(heading,25,INK);headingView.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));center.addView(headingView);
        TextView paragraphView=label(paragraph,16,INK);paragraphView.setLineSpacing(dp(4),1.12f);paragraphView.setTextIsSelectable(true);center.addView(paragraphView);
        TextView count=label((current+1)+" / "+total,13,MUTED);count.setGravity(Gravity.CENTER);root.addView(count);
        if(previous!=null)action(root,"Back",false,previous);
        action(root,nextLabel,true,next);
        setContentView(root);
    }
    private void readStory(String eyebrow,String heading,String text,int page,Runnable finish) {
        String[] pages=storyPages(text);
        int safe=Math.max(0,Math.min(page,pages.length-1));
        storyShell(eyebrow,heading,safe,pages.length,pages[safe],
            safe==0?null:()->readStory(eyebrow,heading,text,safe-1,finish),
            safe+1<pages.length?()->readStory(eyebrow,heading,text,safe+1,finish):finish,
            safe+1<pages.length?"Next":"Continue");
    }
    private void foundation(int index) {
        String[] titles={"What is an algorithm?","Algorithms and computers","Representing an algorithm","Decision tables","Pseudocode and the programming bridge"};
        String[] files={"algorithm","computer","representations","decision-table","pseudocode"};
        if(index>=titles.length){programmingIntroduction();return;}
        try{readStory("OPTIONAL FOUNDATION · Skip via Academy",titles[index],asset("foundation/"+files[index]+".txt"),0,()->foundation(index+1));}
        catch(IOException e){error(e);}
    }
    private void programmingIntroduction() {
        try{readStory("REQUIRED INTRODUCTION","From algorithms to programs",asset("foundation/programming-languages.txt"),0,this::languageHistory);}
        catch(IOException e){error(e);}
    }
    private void languageHistory() {
        try{readStory("REQUIRED INTRODUCTION","Meet C and C++",asset("foundation/language-history.txt"),0,()->{preferences.edit().putBoolean("intro_completed",true).apply();academy();});}
        catch(IOException e){error(e);}
    }
    private void academy() {
        shell("academy","LEARN C/C++","Learn C/C++","Courses");
        LinearLayout intro=panel(false);title(intro,"Start with problem solving",22);body(intro,"Optional: algorithms, computers, five ways to express solutions, decision tables and pseudocode.");action(intro,"Explore",false,()->foundation(0));
        if(!preferences.getBoolean("intro_completed",false)){LinearLayout required=panel(true);title(required,"C & C++ introduction",22);body(required,"Required: what programming languages are, the history of C and C++, and their relationship.");action(required,"Continue",true,this::programmingIntroduction);return;}
        LinearLayout p=panel(true); title(p,cpp?"C++ Programming":"C Programming",25);body(p,completed()+" of 10 lessons completed · "+(completed()*10)+"%");
        ProgressBar courseProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);courseProgress.setMax(10);courseProgress.setProgress(completed());courseProgress.setProgressTintList(ColorStateList.valueOf(MINT));p.addView(courseProgress,new LinearLayout.LayoutParams(-1,dp(8)));
        action(p,cpp?"Switch to C":"Switch to C++",false,()->{cpp=!cpp;preferences.edit().putBoolean("cpp",cpp).apply();academy();});
        Lesson[] lessons=Curriculum.lessons(cpp);
        for(int i=0;i<lessons.length;i++){
            final int index=i;
            boolean done=progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(i));
            boolean ready=i==0||progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(i-1));
            LinearLayout row=panel(false);
            row.addView(label(String.format(Locale.ROOT,"%02d  /  %s",i+1,done?"COMPLETED":LessonAccessPolicy.isAdvanced(i)?"ADVANCED COURSE":"FOUNDATIONS"),12,MINT));
            title(row,lessons[i].title,21);
            if(LessonAccessPolicy.needsReward(i))body(row,"Rewarded-ad unlock · "+(LessonAccessPolicy.isAdvanced(i)?"Advanced course":"Foundation lesson")+" · No premium membership");
            action(row,done?"Review lesson":ready?"Continue":"Complete previous lesson first",false,()->requestLesson(index));
        }
        action(content,"Worked math problems",false,this::mathProblems);action(content,"Guided projects",true,this::projects);action(content,"Your achievements",false,this::achievements);
    }
    /**
     * A rewarded-ad unlock must only be recorded by a verified onUserEarnedReward
     * callback from a real ads SDK. This build does not bundle that SDK.
     * Never fake an impression or an unlock, and never show an inert ad spinner.
     */
    private void requestLesson(int index) {
        boolean previous=index==0||progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(index-1));
        if(!previous) {
            message("Complete the previous lesson","Finish the previous lesson checkpoint before continuing.");
            return;
        }
        boolean earned=preferences.getBoolean("reward-"+LessonAccessPolicy.key(cpp,index),false);
        boolean alreadyCompleted=progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(index));
        if(!LessonAccessPolicy.needsReward(index)||earned||alreadyCompleted) {
            lesson(index);
            return;
        }
        final boolean targetCpp=cpp;
        new AlertDialog.Builder(this)
            .setTitle("Unlock "+(targetCpp?"C++":"C")+" lesson "+(index+1))
            .setMessage("Watch one short advertisement to unlock this lesson permanently on this device. No premium membership is required.")
            .setPositiveButton("Watch ad", (dialog,which)->{
                rewardedGate.show(this,new RewardedLessonGate.Callback(){
                    @Override public void earned(){
                        String key="reward-"+LessonAccessPolicy.key(targetCpp,index);
                        if(!preferences.edit().putBoolean(key,true).commit()){
                            message("Unlock could not be saved","Please retry when local storage is available.");
                            return;
                        }
                        if(cpp==targetCpp)lesson(index);else academy();
                    }
                    @Override public void unavailable(String reason){
                        message("Advertisement unavailable","No reward was earned. Please check your connection and try again. "+reason);
                    }
                });
            })
            .setNegativeButton("Not now",null)
            .show();
    }
    private void lesson(int index) { lessonPage(index,0); }
    private void lessonPage(int index,int page) {
        Lesson lesson=Curriculum.lessons(cpp)[index];
        String[] explanation=storyPages(lesson.explanation);
        int examplePage=explanation.length,challengePage=examplePage+1,quizPage=examplePage+2,total=quizPage+1;
        if(page<examplePage) {
            storyShell((cpp?"C++":"C")+" · LESSON "+(index+1),lesson.title,page,total,explanation[page],
                page==0?null:()->lessonPage(index,page-1),()->lessonPage(index,page+1),"Next");
        }else if(page==examplePage) {
            storyShell("WORKED SOURCE",lesson.title,page,total,lesson.code,
                ()->lessonPage(index,page-1),()->lessonPage(index,page+1),"Next");
        }else if(page==challengePage) {
            storyShell("YOUR TURN · INDEPENDENT PRACTICE",lesson.title,page,total,lesson.challenge,
                ()->lessonPage(index,page-1),()->lessonPage(index,page+1),"Next");
        }else {
            questionPage(index);
        }
    }
    private void questionPage(int index) {
        Lesson lesson=Curriculum.lessons(cpp)[index];
        handler.removeCallbacks(autosave);editor=null;dirty=false;screen="quiz";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(22),dp(22),dp(22));root.setBackgroundColor(Color.WHITE);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(22)+insets.getSystemWindowInsetLeft(),dp(22)+insets.getSystemWindowInsetTop(),dp(22)+insets.getSystemWindowInsetRight(),dp(22)+insets.getSystemWindowInsetBottom());return insets;});
        TextView eyebrow=label("CHECK YOUR UNDERSTANDING  ·  "+(index+1)+"/10",13,MINT);root.addView(eyebrow);
        LinearLayout middle=new LinearLayout(this);middle.setOrientation(LinearLayout.VERTICAL);middle.setGravity(Gravity.CENTER_VERTICAL);root.addView(middle,new LinearLayout.LayoutParams(-1,0,1));
        title(middle,lesson.question,23);
        RadioGroup choices=new RadioGroup(this);middle.addView(choices);
        int[] order=index%2==0?new int[]{0,1}:new int[]{1,0};
        for(int option:order){RadioButton button=new RadioButton(this);button.setId(View.generateViewId());button.setTag(option);button.setText(lesson.options[option]);button.setTextColor(INK);button.setTextSize(17);button.setPadding(dp(8),dp(12),dp(8),dp(12));button.setMinHeight(dp(64));choices.addView(button);}
        TextView feedback=label("Choose one answer, then check.",15,MUTED);middle.addView(feedback);
        action(root,"Back",false,()->lessonPage(index,storyPages(lesson.explanation).length+1));
        Button check=new Button(this);check.setText("Check");check.setAllCaps(false);check.setTextColor(Color.WHITE);check.setTextSize(16);check.setMinHeight(dp(54));check.setEnabled(false);check.setBackgroundTintList(ColorStateList.valueOf(MINT));root.addView(check,new LinearLayout.LayoutParams(-1,dp(56)));choices.setOnCheckedChangeListener((group,id)->check.setEnabled(id!=-1));check.setOnClickListener(v->{
            if(choices.getCheckedRadioButtonId()==-1){feedback.setText("Select an answer first.");return;}
            RadioButton selected=choices.findViewById(choices.getCheckedRadioButtonId());
            boolean correct=((Integer)selected.getTag())==lesson.answer;
            if(correct)progress.complete(cpp?"cpp-foundation":"c-foundation",Integer.toString(index));
            new AlertDialog.Builder(this).setTitle(correct?"Correct answer":"Not quite — review and retry")
                .setMessage(lesson.feedback)
                .setPositiveButton(correct?"Next":"Try again",(dialog,which)->{if(correct){if(index+1<Curriculum.lessons(cpp).length)requestLesson(index+1);else academy();}})
                .show();
        });
        setContentView(root);
    }
    private void copyLesson(int index) {
        String base=(cpp?"cpp":"c")+"-lesson-"+(index+1),extension=cpp?".cpp":".c",name=base+extension;int n=2;
        try{while(store.exists(name))name=base+"-"+(n++)+extension;store.write(name,Curriculum.lessons(cpp)[index].code);openEditor(name);}catch(IOException e){error(e);}
    }
    private void achievements() {
        shell("achievements","ACADEMY  /  ACHIEVEMENTS","See your progress.","Every concept understood is a step forward.");LinearLayout p=panel(true);title(p,completed()+" / 10",40);body(p,(cpp?"C++":"C")+" practice checks completed on this device.");
        title(p,completed()==10?"Foundation finisher":"Building your foundation",22);body(p,"Practice achievements record local learning progress. They are not accredited qualifications or signed certificates.");
        LinearLayout exams=panel(false);title(exams,"Certificate exams",22);body(exams,"Not available in this build. Certificate exams require a server-authorized timed attempt, trusted assessment, and protected credential signing. No payment or certificate claim is offered here.");action(content,"Continue learning",true,this::academy);
    }
    private void projects() {
        shell("projects","ACADEMY  /  GUIDED PROJECTS","Put it together.","Five practical exercises after your foundation course.");
        if(completed()<10){LinearLayout p=panel(true);title(p,"Build your foundation first",23);body(p,"Complete the 10 practice checks in this language to unlock project briefs. No purchase or certificate is required.");action(p,"Return to lessons",true,this::academy);return;}
        String[] names={"Calculator","Educational login","Student results","ATM simulator","Library management"};
        String[] ids={"calculator","login","grades","atm","library"};
        for(int i=0;i<names.length;i++){final String id=ids[i],name=names[i];LinearLayout p=panel(false);title(p,name,22);body(p,"Specification · algorithm · milestones · reference · tests");action(p,"Open project guide",true,()->projectGuide(id,name));}
    }
    private String asset(String path) throws IOException {
        try(InputStream input=getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] block=new byte[4096];int length;while((length=input.read(block))!=-1)out.write(block,0,length);
            return out.toString("UTF-8");
        }
    }
    private void projectGuide(String id,String name){
        shell("guide","PROJECT LAB  /  "+(cpp?"C++":"C"),name,"Work through the milestones, then compare your solution.");
        try{LinearLayout p=panel(false);body(p,asset("projects/"+id+".txt"));}catch(IOException e){error(e);}
        action(content,"Open starter",true,()->copyAsset(id,true));
        action(content,"Open reference solution",false,()->copyAsset(id,false));
        action(content,"Back to projects",false,this::projects);
    }
    private void copyAsset(String id,boolean starter){
        String extension=cpp?".cpp":".c",base=id+(starter?"-starter":"-reference"),name=base+extension;int suffix=2;
        try{while(store.exists(name))name=base+"-"+(suffix++)+extension;store.write(name,asset("projects/"+id+(starter?"-starter":"")+extension));openEditor(name);}catch(IOException e){error(e);}
    }
    private void readGuide(String title,String path){
        shell("reading","ACADEMY  /  FIELD NOTES",title,"Original teaching material · available offline");
        try{LinearLayout p=panel(false);body(p,asset(path));}catch(IOException e){error(e);}
        action(content,"Back to academy",false,this::academy);
    }
    private void mathProblems(){readGuide("Math into programs","lessons/math.txt");}

    private static final class Aurora extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private Shader top, bottom;
        Aurora(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
            if(w<=0||h<=0)return;
            top=new RadialGradient(w*.95f,h*.08f,w*.95f,new int[]{0xff303465,0x00303465},null,Shader.TileMode.CLAMP);
            bottom=new RadialGradient(w*.02f,h*.58f,w*.8f,new int[]{0xff103e48,0x00103e48},null,Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();canvas.drawColor(0xff0b1020);paint.setShader(top);canvas.drawRect(0,0,w,h,paint);paint.setShader(bottom);canvas.drawRect(0,0,w,h,paint);paint.setShader(null);paint.setColor(0x12b6caff);paint.setStrokeWidth(1);for(int x=0;x<w;x+=64)canvas.drawLine(x,0,x,h,paint);for(int y=0;y<h;y+=64)canvas.drawLine(0,y,w,y,paint);}

    }
}
