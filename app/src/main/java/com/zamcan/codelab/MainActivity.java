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
    private int INK=0xfff6f7ff, MUTED=0xffb5c5e8, MINT=0xff67c8ff, LILAC=0xffc2a5ff;
    private boolean light;
    private LinearLayout content;
    private ProjectStore store;
    private WorkspaceRepository workspaces;
    private String project="Practice";
    private LearningStore progress;
    private android.content.SharedPreferences preferences;
    private boolean cpp, dirty, highlighting;
    private int activeLesson, activeStage;
    private String screen="home", filename="main.c", exportSource="";
    private EditText editor;
    private TextView saveStatus;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable autosave=() -> saveSource(false);

    @Override public void onCreate(Bundle state) {
        light=getSharedPreferences("academy",MODE_PRIVATE).getBoolean("light_theme",false);
        if(light) {setTheme(R.style.AppThemeLight);INK=0xff172440;MUTED=0xff43516b;MINT=0xff125ba6;LILAC=0xff6240a6;}
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(light?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR:0);
        preferences=getSharedPreferences("academy", MODE_PRIVATE);
        progress=new LearningStore(this);
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
        if(state!=null && "editor".equals(state.getString("screen"))) openEditor(filename);
        else if(state!=null && "lesson".equals(state.getString("screen"))) lesson(state.getInt("lesson"),state.getInt("stage"));
        else home();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        saveSource(false); out.putString("screen",screen); out.putInt("lesson",activeLesson); out.putInt("stage",activeStage); super.onSaveInstanceState(out);
    }
    @Override protected void onPause() { saveSource(false); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }
    @Override public void onBackPressed() {
        if("home".equals(screen)) super.onBackPressed();
        else if("lesson".equals(screen)) { if(activeStage>0) lesson(activeLesson,activeStage-1); else courseMap(); }
        else if("course".equals(screen))pathways();
        else if("pathways".equals(screen))academy();
        else if(leaveEditor()) home();
    }
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void fatal(Exception e) { new AlertDialog.Builder(this).setTitle("Storage unavailable").setMessage(e.getMessage()).setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show(); }
    private void error(Exception e) { new AlertDialog.Builder(this).setTitle("Could not complete action").setMessage(e.getMessage()).setPositiveButton("OK",null).show(); }
    private void message(String title,String body) { new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Got it",null).show(); }
    private boolean leaveEditor() { return editor==null || !dirty || saveSource(true); }
    private GradientDrawable glass(boolean accent) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,light ? (accent ? new int[]{0xf0eef2ff,0xe8f1eaff} : new int[]{0xfaffffff,0xe8f0f4ff}) : (accent ? new int[]{0xdd172c59,0xdd29204c} : new int[]{0xc9202c4f,0xc9111833}));
        d.setCornerRadius(dp(20)); d.setStroke(dp(1),light ? 0x66818eb2 : (accent ? 0x6682aaff : 0x36d0dcff)); return d;
    }
    private TextView label(String text,int size,int color) {
        TextView v=new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        v.setLineSpacing(dp(3),1); v.setPadding(0,dp(5),0,dp(5)); return v;
    }
    private void title(LinearLayout parent,String text,int size) { TextView t=label(text,size,INK); t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); parent.addView(t); }
    private void body(LinearLayout parent,String text) { parent.addView(label(text,16,MUTED)); }
    private LinearLayout panel(boolean accent) {
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(20),dp(18),dp(20),dp(18)); p.setBackground(glass(accent));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(16); p.setLayoutParams(lp); content.addView(p); return p;
    }
    private void action(LinearLayout parent,String text,boolean primary,Runnable run) {
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(primary?(light?Color.WHITE:0xff102e2a):INK); b.setMinHeight(dp(50)); b.setPadding(dp(14),dp(10),dp(14),dp(10));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?(light?new int[]{0xff125ba6,0xff553b9f}:new int[]{MINT,0xffb1ebf2}):(light?new int[]{0xffe9e5fa,0xffe4ecf9}:new int[]{0x40b1a4ef,0x252b4767}));
        background.setCornerRadius(dp(15)); background.setStroke(dp(1),primary?0x55ffffff:0x44ced9ff);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x40ffffff),background,null));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(10); b.setLayoutParams(lp);
        b.setOnClickListener(v->run.run()); parent.addView(b);
    }
    private void shell(String name,String eyebrow,String heading,String subtitle) {
        handler.removeCallbacks(autosave); editor=null; dirty=false; screen=name;
        FrameLayout root=new FrameLayout(this); root.addView(new Aurora(this,light),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); root.addView(stack,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); stack.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(20),dp(20),dp(20),dp(20)); scroll.addView(content);
        content.addView(label(eyebrow,12,MINT)); title(content,heading,28); body(content,subtitle);
        View gap=new View(this); content.addView(gap,new LinearLayout.LayoutParams(1,dp(20)));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(16),0,dp(16),dp(12)); nav.setBackground(glass(false)); stack.addView(nav);
        String[] tabs={"Home","Studio","Academy"};
        int selected="home".equals(name)?0:("studio".equals(name)||"editor".equals(name)?1:2);
        for(int i=0;i<tabs.length;i++) {
            final int destination=i;Button b=new Button(this);b.setText(tabs[i]);b.setAllCaps(false);b.setTextSize(14);
            b.setTextColor(i==selected?MINT:MUTED);b.setMinHeight(dp(56));b.setSelected(i==selected);
            GradientDrawable surface=new GradientDrawable();surface.setCornerRadius(dp(16));surface.setColor(i==selected?(light?0xffe4ecfa:0xff202d50):Color.TRANSPARENT);
            b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22758cdb),surface,null));
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);params.setMargins(dp(3),dp(6),dp(3),0);nav.addView(b,params);
            b.setOnClickListener(v->{if(leaveEditor()){if(destination==0)home();else if(destination==1)studio();else academy();}});
        }
        setContentView(root);
    }
    private void artwork(LinearLayout parent) {
        ImageView art=new ImageView(this);BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;
        art.setImageBitmap(BitmapFactory.decodeResource(getResources(),light?R.drawable.academy_art_light:R.drawable.academy_art_dark,options));
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(-1,dp(144));layout.bottomMargin=dp(12);parent.addView(art,layout);
    }
    private void section(String text) {
        TextView heading=label(text,14,MUTED);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setPadding(0,dp(18),0,dp(10));content.addView(heading);
    }
    private void row(LinearLayout parent,String number,String name,String detail,boolean enabled,Runnable open) {
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),dp(12),dp(12),dp(12));row.setMinimumHeight(dp(76));
        TextView marker=label(number,16,enabled?MINT:MUTED);marker.setGravity(Gravity.CENTER);marker.setTypeface(Typeface.MONOSPACE);
        row.addView(marker,new LinearLayout.LayoutParams(dp(42),-2));
        LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(dp(8),0,dp(8),0);
        TextView title=label(name,17,INK);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);text.addView(title);text.addView(label(detail,14,MUTED));
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView arrow=label("›",24,enabled?MINT:MUTED);row.addView(arrow,new LinearLayout.LayoutParams(dp(20),-2));
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33758cdb),null,new android.graphics.drawable.ColorDrawable(Color.WHITE)));
        row.setFocusable(true);row.setContentDescription(name+". "+detail);row.setOnClickListener(v->open.run());parent.addView(row);
        View line=new View(this);line.setBackgroundColor(light?0x197080a0:0x19d0dcff);parent.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    private void home() {
        shell("home","BY ZAMCAN ACADEMY","C/C++ CodeLab","Your space to learn programming and build with code.");
        LinearLayout create=panel(true);artwork(create);create.addView(label("CODE STUDIO",14,MINT));title(create,"From idea to source.",26);
        body(create,"C and C++ projects, a focused editor, and files that stay on your phone.");action(create,"Open Code Studio  →",true,this::studio);
        LinearLayout learn=panel(false);learn.addView(label("LEARNING ACADEMY",14,LILAC));title(learn,"A path through programming.",26);
        body(learn,"Start with the concepts. Explore a worked example. Practice, then check your understanding.");action(learn,"Continue learning  →",true,this::academy);
        section("YOUR SPACE");
        row(content,"◐","Appearance",light?"Light glass theme":"Midnight glass theme",true,this::appearance);
        row(content,"i","About this preview","Capabilities, privacy and build status",true,this::status);
    }
    private void appearance() {
        shell("appearance","SETTINGS / APPEARANCE","Your learning space.","Choose a theme. Text follows your device font scaling.");
        LinearLayout p=panel(false);title(p,light?"Light glass":"Midnight glass",24);
        body(p,"A full-screen gradient, translucent surfaces and clear controls. Theme choice is saved on this device.");
        action(p,light?"Use midnight theme":"Use light theme",true,()->{preferences.edit().putBoolean("light_theme",!light).apply();recreate();});
        action(content,"Back home",false,this::home);
    }
    private void status() {
        shell("status","ABOUT THIS BUILD","Honest by design.","Development build 0.3.0");
        LinearLayout p=panel(false); title(p,"Available offline",22); body(p,"Lessons, practice quizzes, saved progress, source editing, multiple source files, and source export work without an account. Uninstalling clears local data; export important source files first.");
        LinearLayout s=panel(false); title(s,"Release gates still open",22); body(s,"An Android-compatible C/C++ compiler and isolated execution runtime are not bundled. Compile and Run are unavailable. No program output is simulated.\n\nOnline certificate exams, signed credentials, public verification, and AdMob are not integrated. Practice completion is not certification.\n\nThis build requests no network or storage permissions and includes no analytics or advertising SDK.");
        action(content,"Back home",true,this::home);
    }
    private void studio() {
        shell("studio","CODE STUDIO  /  LOCAL WORKSPACE","Your workspace","Projects and source files, stored on this device.");
        LinearLayout p=panel(true); title(p,project,23); body(p,"C, C++, and header files • UTF-8\nEdits save automatically on this device."); action(p,"+  Create a source file",true,this::newFile); action(p,"Switch or create project",false,this::chooseProject);
        section("PROJECT FILES");LinearLayout files=panel(false);for(String file:store.list())row(files,file.endsWith(".cpp")?"C++":"C",file,"Saved locally · tap to edit",true,()->openEditor(file));
        body(content,"Compiler setup is pending. You can edit and export sources; this build cannot execute them.");
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
        shell("editor","CODE STUDIO  /  EDITOR",file,"A little focus goes a long way.");
        LinearLayout p=panel(false); saveStatus=label("Saved on this device",12,MINT); p.addView(saveStatus);
        editor=new CodeEditor(this); editor.setTextColor(INK); editor.setTextSize(preferences.getInt("font",15)); editor.setTypeface(Typeface.MONOSPACE); editor.setGravity(Gravity.TOP|Gravity.START); editor.setPadding(dp(44),dp(12),dp(8),dp(12)); editor.setBackgroundColor(light?0xfff6f8ff:0x66101729); editor.setMinLines(12); editor.setHorizontallyScrolling(true); editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ProjectStore.MAX_BYTES)}); editor.setContentDescription("Source code editor for "+file); editor.setText(source); p.addView(editor,new LinearLayout.LayoutParams(-1,-2));
        highlight(editor.getText());
        editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){}public void afterTextChanged(Editable s){if(highlighting)return;dirty=true;saveStatus.setText(R.string.saving);handler.removeCallbacks(autosave);handler.postDelayed(autosave,700);highlight(s);}});
        action(p,"Save source",true,()->saveSource(true)); action(p,"Export source…",false,()->{if(saveSource(true)){exportSource=editor.getText().toString();Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,filename);try{startActivityForResult(intent,41);}catch(ActivityNotFoundException e){error(e);}}});
        action(p,"Editor text size",false,()->new AlertDialog.Builder(this).setTitle("Text size").setItems(new String[]{"Compact · 13 sp","Comfortable · 16 sp","Large · 20 sp"},(d,w)->{int size=new int[]{13,16,20}[w];preferences.edit().putInt("font",size).apply();editor.setTextSize(size);}).show());
        LinearLayout terminal=panel(false); terminal.addView(label("TERMINAL  /  NOT CONNECTED",12,LILAC)); body(terminal,"Native compiler not installed in this build. No code has been compiled or executed."); action(terminal,"View execution requirements",false,()->message("Native execution is pending","Release requires a packaged Android toolchain, linker and runtime, verified input/output, time/memory/output limits, and termination on a real device. Host compiler tests do not establish Android support."));
    }
    private void highlight(Editable s) {
        if(s.length()>24000) return;
        highlighting=true;
        for(ForegroundColorSpan span:s.getSpans(0,s.length(),ForegroundColorSpan.class))s.removeSpan(span);
        Pattern pattern=Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\])*\"|\\b(?:int|double|char|void|return|if|else|for|while|const|class|struct|public|private|auto|bool|namespace|include)\\b");
        Matcher matcher=pattern.matcher(s);while(matcher.find()){String token=matcher.group();int color=token.startsWith("/")?(light?0xff52617a:0xffa0b1c7):token.startsWith("\"")?MINT:LILAC;s.setSpan(new ForegroundColorSpan(color),matcher.start(),matcher.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}highlighting=false;
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

    /** Optional algorithmic-thinking foundation; does not award C/C++ course mastery. */
    private void foundation(int index) {
        String[] titles={"What is an algorithm?","Algorithms and computers","Representing an algorithm","Decision tables","Pseudocode and the programming bridge"};
        String[] files={"algorithm","computer","representations","decision-table","pseudocode"};
        if(index<0||index>=titles.length){programmingIntroduction();return;}
        shell("foundation","OPTIONAL FOUNDATION  /  "+(index+1)+" OF "+titles.length,titles[index],"Think clearly before writing code. You may skip this foundation.");
        try{LinearLayout lesson=panel(false);body(lesson,asset("foundation/"+files[index]+".txt"));}catch(IOException e){error(e);}
        action(content,index==titles.length-1?"Continue to programming languages":"Next foundation topic  →",true,()->foundation(index+1));
        action(content,"Skip foundation — introduce C & C++",false,this::programmingIntroduction);
        action(content,"Back to academy",false,this::academy);
    }
    /** Required shared introduction before entering either formal programming pathway. */
    private void programmingIntroduction() {
        shell("programming-intro","REQUIRED INTRODUCTION  /  C & C++","From algorithms to programs.","Discover the languages, their history, and how source becomes executable.");
        try{LinearLayout lesson=panel(false);body(lesson,asset("foundation/programming-languages.txt"));}catch(IOException e){error(e);}
        action(content,"Continue to C and C++ history  →",true,this::languageHistory);
        action(content,"Back to academy",false,this::academy);
    }
    private void languageHistory() {
        shell("language-history","REQUIRED INTRODUCTION  /  C & C++","Meet C and C++.","Two related languages with different strengths and rules.");
        try{LinearLayout lesson=panel(false);body(lesson,asset("foundation/language-history.txt"));}catch(IOException e){error(e);}
        action(content,"Check your understanding  →",true,this::introductionCheckpoint);
        action(content,"Previous: programming languages",false,this::programmingIntroduction);
    }
    private RadioGroup practiceChoices(LinearLayout parent,String question,String... options) {
        title(parent,question,19);
        RadioGroup group=new RadioGroup(this);
        for(int i=0;i<options.length;i++) {
            RadioButton button=new RadioButton(this);button.setId(View.generateViewId());button.setTag(i);
            button.setText(options[i]);button.setTextSize(16);button.setTextColor(INK);button.setMinHeight(dp(52));group.addView(button);
        }
        parent.addView(group);return group;
    }
    private int selected(RadioGroup group) {
        RadioButton choice=group.findViewById(group.getCheckedRadioButtonId());
        return choice==null?-1:(Integer)choice.getTag();
    }
    private EditText practiceInput(LinearLayout parent,String hint) {
        EditText input=new EditText(this);input.setTextColor(INK);input.setHintTextColor(MUTED);input.setHint(hint);
        input.setContentDescription(hint);input.setSingleLine(true);input.setMinHeight(dp(52));parent.addView(input);return input;
    }
    private void introductionCheckpoint() {
        shell("intro-checkpoint","REQUIRED INTRODUCTION / PRACTICE","Ready for the pathway?","Four short checks. You can retry freely; this is not a certificate exam.");
        LinearLayout p=panel(false);
        RadioGroup language=practiceChoices(p,"1. Which statement describes C and C++?","C++ accepts every C program unchanged.","C and C++ are related languages with different rules.","C and C++ are names for the same language.");
        RadioGroup compatibility=practiceChoices(p,"2. True or false: indentation alone defines a C block.","False — braces define a compound statement.","True — adding spaces changes block boundaries.");
        body(p,"3. Fill the blank: a ______ translates C/C++ source into object code in a typical separate-compilation workflow.");
        EditText compiler=practiceInput(p,"Name the translation tool");
        body(p,"4. Arrange these steps by entering their numbers: 3 = link object files and libraries; 1 = write source; 2 = compile source to object code.");
        EditText pipeline=practiceInput(p,"Order, for example 3, 2, 1");
        TextView feedback=label("Answer all four checks, then review the feedback.",16,MUTED);p.addView(feedback);
        action(p,"Check introduction",true,()->{
            if(LearningPath.introductionPassed(selected(language),selected(compatibility),compiler.getText().toString(),pipeline.getText().toString())) {
                preferences.edit().putBoolean("intro_checkpoint_v1",true).apply();
                feedback.setText("Checkpoint passed. The C and C++ pathways are now available.");
                action(content,"Choose your pathway",true,this::academy);
            } else {
                feedback.setText("Review and retry: C and C++ have different rules. Braces, not indentation, group C statements. A compiler translates source; a linker combines object files and libraries. Write source, compile, then link. Your existing course progress is preserved.");
            }
            feedback.announceForAccessibility(feedback.getText());
        });
        action(content,"Review introduction",false,this::programmingIntroduction);
        action(content,"Back to academy",false,this::academy);
    }
    private String courseKey(){return cpp?"cpp-foundation":"c-foundation";}
    private String lessonKey(int index){return courseKey()+":"+index;}
    private void academy() {
        shell("academy","LEARNING ACADEMY","Your learning journey","Offline lessons. Clear steps. Progress you can return to.");
        boolean introduced=preferences.getBoolean("intro_checkpoint_v1",false);
        LinearLayout next=panel(true);artwork(next);next.addView(label(introduced?"CONTINUE YOUR PATH":"START HERE",14,MINT));
        title(next,introduced?(cpp?"C++ foundations":"C foundations"):"Understand the languages",24);
        body(next,introduced?completed()+" of "+Curriculum.lessons(cpp).length+" preview checkpoints complete":"Explore how programs work, meet C and C++, then check your understanding.");
        action(next,introduced?"Resume lesson  →":"Begin introduction  →",true,()->{if(introduced)lesson(preferences.getInt(courseKey()+":resume",0));else programmingIntroduction();});
        section("THE JOURNEY");LinearLayout journey=panel(false);
        row(journey,"01","Think like a programmer","Optional · algorithms and problem solving",true,()->foundation(0));
        row(journey,"02","Meet C and C++",introduced?"Checkpoint passed · review anytime":"Required · languages, history and toolchain",true,this::programmingIntroduction);
        row(journey,"03","Choose your pathway","C or C++ · preview lessons and checkpoints",introduced,()->{if(introduced)pathways();else message("Start with the introduction","Complete the introduction checkpoint to begin either pathway.");});
        row(journey,"04","Build guided projects","Five projects · unlock through course practice",introduced,()->{if(introduced)projects();else message("Your project journey","The projects follow the course. Start with the introduction.");});
        section("YOUR LEARNING TOOLS");
        row(content,"✓","Progress & achievements","Your completed practice checks",true,this::achievements);
        row(content,"ƒ","Math into code","Worked problems and reasoning",true,this::mathProblems);
    }
    private void pathways() {
        shell("pathways","ACADEMY / PATHWAYS","Choose your language","Both pathways remain available. Your progress is saved separately.");
        for(boolean language:new boolean[]{false,true}) {
            LinearLayout p=panel(language);p.addView(label(language?"C++ / ABSTRACTION & STRUCTURE":"C / PROGRAMMING FOUNDATIONS",14,language?LILAC:MINT));
            title(p,language?"Build with modern C++":"Understand the foundations of C",24);
            body(p,language?"Values, functions, containers and objects. Learn how C++ expresses reusable programs.":"Types, decisions, functions and memory. Learn how source code describes precise operations.");
            action(p,"View "+(language?"C++":"C")+" course",true,()->{cpp=language;preferences.edit().putBoolean("cpp",cpp).apply();courseMap();});
        }
        body(content,"Preview scope: ten introductory lessons per language. The complete sixteen-module pathways are still being developed.");
        action(content,"Back to journey",false,this::academy);
    }
    private void courseMap() {
        shell("course","ACADEMY / "+(cpp?"C++":"C"),cpp?"C++ foundations":"C foundations","Work through the checkpoints in order. Revisit completed lessons anytime.");
        Lesson[] lessons=Curriculum.lessons(cpp);LinearLayout summary=panel(true);
        title(summary,completed()+" / "+lessons.length+" checkpoints",24);
        ProgressBar meter=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);meter.setMax(lessons.length);meter.setProgress(completed());meter.setProgressTintList(ColorStateList.valueOf(MINT));summary.addView(meter,new LinearLayout.LayoutParams(-1,dp(6)));
        action(summary,"Resume learning",true,()->lesson(preferences.getInt(courseKey()+":resume",0)));
        LinearLayout group=null;
        for(int i=0;i<lessons.length;i++) {
            if(i%4==0){section("LESSONS "+(i+1)+"–"+Math.min(i+4,lessons.length));group=panel(false);}
            final int index=i;boolean complete=progress.completed(courseKey(),Integer.toString(i));
            boolean available=LearningPath.canOpen(preferences.getBoolean("intro_checkpoint_v1",false),i,lessons.length,i>0&&progress.completed(courseKey(),Integer.toString(i-1)));
            String detail=complete?"Checkpoint passed":available?"Ready to learn":"Complete the previous checkpoint";
            if(preferences.getBoolean(lessonKey(i)+":bookmark",false))detail+=" · Bookmarked";
            row(group,String.format(Locale.ROOT,"%02d",i+1),lessons[i].title,detail,available,()->lesson(index));
        }
        action(content,"Change pathway",false,this::pathways);
        action(content,"Back to journey",false,this::academy);
    }
    private void lesson(int index) {
        lesson(index,preferences.getInt(lessonKey(index)+":stage",0));
    }
    private void lesson(int index,int requestedStage) {
        Lesson[] lessons=Curriculum.lessons(cpp);
        boolean previous=index>0 && progress.completed(courseKey(),Integer.toString(index-1));
        if(!LearningPath.canOpen(preferences.getBoolean("intro_checkpoint_v1",false),index,lessons.length,previous)) {
            academy();message("Continue your learning path","Pass the introduction and preceding lesson checkpoint first. Completed lessons remain available for review.");return;
        }
        int stage=LearningPath.stage(requestedStage);activeLesson=index;activeStage=stage;
        preferences.edit().putInt(courseKey()+":resume",index).putInt(lessonKey(index)+":stage",stage).apply();
        Lesson lesson=lessons[index];
        shell("lesson",(cpp?"C++":"C")+" FOUNDATIONS / "+(index+1)+" OF "+lessons.length,lesson.title,"Stage "+(stage+1)+" of 4 · "+LearningPath.STAGES[stage]);
        LinearLayout stages=new LinearLayout(this);stages.setPadding(0,0,0,dp(16));content.addView(stages);
        for(int i=0;i<LearningPath.STAGES.length;i++) {
            final int target=i;Button step=new Button(this);step.setText(Integer.toString(i+1));step.setAllCaps(false);
            step.setTextColor(i==stage?MINT:MUTED);step.setContentDescription("Stage "+(i+1)+": "+LearningPath.STAGES[i]);step.setMinHeight(dp(48));step.setBackground(glass(i==stage));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),0,dp(3),0);stages.addView(step,lp);step.setOnClickListener(v->lesson(index,target));
        }
        LinearLayout page=panel(false);
        if(stage==0) {
            title(page,"Understand the idea",22);body(page,lesson.explanation);
            if(index==0)action(page,"Explore syntax, token by token",false,()->readGuide("First program: syntax anatomy","lessons/first-program.txt"));
        } else if(stage==1) {
            page.addView(label("WORKED EXAMPLE / SOURCE, NOT LIVE OUTPUT",12,MINT));
            TextView code=label(lesson.code,16,INK);code.setTypeface(Typeface.MONOSPACE);code.setTextIsSelectable(true);page.addView(code);
            body(page,"Read each statement in order. Predict what changes in memory and what is printed before comparing with the source.");
            action(page,"Open a copy in Studio",true,()->copyLesson(index));
        } else if(stage==2) {
            title(page,"Your turn",22);body(page,lesson.challenge);
            body(page,"Write your prediction or solution notes below. These stay on this device; saving a note does not award mastery.");
            EditText notes=new EditText(this);notes.setTextColor(INK);notes.setTextSize(16);notes.setGravity(Gravity.TOP);
            notes.setMinLines(4);notes.setHint("My prediction and reasoning");notes.setHintTextColor(MUTED);
            notes.setContentDescription("Private practice notes");notes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8000)});
            String key=lessonKey(index)+":notes";notes.setText(preferences.getString(key,""));page.addView(notes);
            notes.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence t,int a,int c,int f){}public void onTextChanged(CharSequence t,int a,int b,int c){preferences.edit().putString(key,t.toString()).apply();}public void afterTextChanged(Editable t){}});
        } else {
            RadioGroup choices=practiceChoices(page,lesson.question,lesson.options);
            TextView feedback=label("Choose an answer. You may retry and review the earlier stages.",16,MUTED);page.addView(feedback);
            action(page,"Check answer",true,()->{
                int answer=selected(choices);if(answer<0){feedback.setText(R.string.select_answer);return;}
                boolean right=answer==lesson.answer;
                feedback.setText(getString(right?R.string.correct_feedback:R.string.retry_feedback,lesson.feedback));
                if(right) progress.complete(courseKey(),Integer.toString(index));
                feedback.announceForAccessibility(feedback.getText());
            });
        }
        if(stage<3)action(content,"Continue to "+LearningPath.STAGES[stage+1],true,()->lesson(index,stage+1));
        else action(content,index+1<lessons.length?"Continue to next lesson":"Return to course",true,()->{
            if(!progress.completed(courseKey(),Integer.toString(index))) {message("Checkpoint needed","Answer this lesson's practice check correctly before continuing.");return;}
            if(index+1<lessons.length)lesson(index+1,0);else courseMap();
        });
        if(stage>0)action(content,"Previous stage",false,()->lesson(index,stage-1));
        String bookmark=lessonKey(index)+":bookmark";
        action(content,preferences.getBoolean(bookmark,false)?"Remove bookmark":"Bookmark lesson",false,()->{
            preferences.edit().putBoolean(bookmark,!preferences.getBoolean(bookmark,false)).apply();lesson(index,stage);
        });
        action(content,"Back to course",false,this::courseMap);
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
        action(content,"Open starter in Studio",true,()->copyAsset(id,true));
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
        private final boolean light;
        Aurora(Context context,boolean light){super(context);this.light=light;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
            if(w<=0||h<=0)return;
            top=new RadialGradient(w*.95f,h*.08f,w*.95f,light?new int[]{0xffd7d2f5,0x00d7d2f5}:new int[]{0xff303465,0x00303465},null,Shader.TileMode.CLAMP);
            bottom=new RadialGradient(w*.02f,h*.58f,w*.8f,light?new int[]{0xffc8e6f3,0x00c8e6f3}:new int[]{0xff103e48,0x00103e48},null,Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();canvas.drawColor(light?0xfff5f7ff:0xff0b1025);paint.setShader(top);canvas.drawRect(0,0,w,h,paint);paint.setShader(bottom);canvas.drawRect(0,0,w,h,paint);paint.setShader(null);paint.setColor(light?0x0c344b80:0x12b6caff);paint.setStrokeWidth(1);for(int x=0;x<w;x+=64)canvas.drawLine(x,0,x,h,paint);for(int y=0;y<h;y+=64)canvas.drawLine(0,y,w,y,paint);}

    }
}
