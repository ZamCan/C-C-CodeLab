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
    private static final int INK=0xfff1f5ff, MUTED=0xffbac6df, MINT=0xff8cf2d5, LILAC=0xffbcadff;
    private LinearLayout content;
    private ProjectStore store;
    private WorkspaceRepository workspaces;
    private String project="Practice";
    private LearningStore progress;
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
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }
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
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,accent ? new int[]{0xdd254752,0xdd252440} : new int[]{0xc92b354f,0xc91a2239});
        d.setCornerRadius(dp(24)); d.setStroke(dp(1),accent ? 0x668cf2d5 : 0x36d0dcff); return d;
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
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(primary?0xff102e2a:INK); b.setMinHeight(dp(50)); b.setPadding(dp(14),dp(10),dp(14),dp(10));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?new int[]{MINT,0xffb1ebf2}:new int[]{0x40b1a4ef,0x252b4767});
        background.setCornerRadius(dp(15)); background.setStroke(dp(1),primary?0x55ffffff:0x44ced9ff);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x40ffffff),background,null));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(10); b.setLayoutParams(lp);
        b.setOnClickListener(v->run.run()); parent.addView(b);
    }
    private void shell(String name,String eyebrow,String heading,String subtitle) {
        handler.removeCallbacks(autosave); editor=null; dirty=false; screen=name;
        FrameLayout root=new FrameLayout(this); root.addView(new Aurora(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); root.addView(stack,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); stack.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(22),dp(24),dp(22),dp(14)); scroll.addView(content);
        content.addView(label(eyebrow,12,MINT)); title(content,heading,32); body(content,subtitle);
        View gap=new View(this); content.addView(gap,new LinearLayout.LayoutParams(1,dp(20)));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(16),0,dp(16),dp(12)); nav.setBackground(glass(false)); stack.addView(nav);
        for(String tab:new String[]{"⌘  Studio","◈  Academy"}) {
            Button b=new Button(this); b.setText(tab); b.setAllCaps(false); b.setTextColor(INK); b.setBackgroundColor(Color.TRANSPARENT); b.setMinHeight(dp(56));
            nav.addView(b,new LinearLayout.LayoutParams(0,-2,1)); b.setOnClickListener(v->{if(leaveEditor()){if(tab.contains("Studio")) studio(); else academy();}});
        }
        setContentView(root);
    }
    private void home() {
        shell("home","ZAMCAN ACADEMY","Small steps.\nReal programs.","Your C & C++ learning space. Built for focused, offline practice.");
        LinearLayout studio=panel(false); studio.addView(label("01  /  CREATE",12,LILAC)); title(studio,getString(R.string.studio),23); body(studio,"A quiet workspace for your C and C++ source files. Save, edit, and export on your phone."); action(studio,"Open workspace",false,this::studio);
        LinearLayout academy=panel(false); academy.addView(label("02  /  UNDERSTAND",12,MINT)); title(academy,getString(R.string.academy),23); body(academy,"20 foundation lessons · offline quizzes\nTwo languages. One clear starting point."); action(academy,"Explore the academy",false,this::academy);
        body(content,"LOCAL FIRST  •  No account required\nLearning and source files stay on this device.");
        action(content,"Build status & privacy",false,this::status);
    }
    private void status() {
        shell("status","ABOUT THIS BUILD","Honest by design.","Development build 0.1.0");
        LinearLayout p=panel(false); title(p,"Available offline",22); body(p,"Lessons, practice quizzes, saved progress, source editing, multiple source files, and source export work without an account. Uninstalling clears local data; export important source files first.");
        LinearLayout s=panel(false); title(s,"Release gates still open",22); body(s,"An Android-compatible C/C++ compiler and isolated execution runtime are not bundled. Compile and Run are unavailable. No program output is simulated.\n\nOnline certificate exams, signed credentials, public verification, and AdMob are not integrated. Practice completion is not certification.\n\nThis build requests no network or storage permissions and includes no analytics or advertising SDK.");
        action(content,"Back home",true,this::home);
    }
    private void studio() {
        shell("studio","CODE STUDIO  /  LOCAL WORKSPACE","Make something.","Your source files, always close at hand.");
        LinearLayout p=panel(true); title(p,project,23); body(p,"C, C++, and header files • UTF-8\nEdits save automatically on this device."); action(p,"+  Create a source file",true,this::newFile); action(p,"Switch or create project",false,this::chooseProject);
        for(String file:store.list()) { LinearLayout f=panel(false); title(f,file,20); body(f,file.endsWith(".cpp")?"C++ source":"Source file"); action(f,"Open editor  →",false,()->openEditor(file)); }
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
        editor=new CodeEditor(this); editor.setTextColor(INK); editor.setTextSize(preferences.getInt("font",15)); editor.setTypeface(Typeface.MONOSPACE); editor.setGravity(Gravity.TOP|Gravity.START); editor.setPadding(dp(44),dp(12),dp(8),dp(12)); editor.setBackgroundColor(0x66101729); editor.setMinLines(12); editor.setHorizontallyScrolling(true); editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ProjectStore.MAX_BYTES)}); editor.setContentDescription("Source code editor for "+file); editor.setText(source); p.addView(editor,new LinearLayout.LayoutParams(-1,-2));
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

    private void academy() {
        shell("academy","LEARNING ACADEMY  /  YOUR PATH","Understand.\nThen build.","Small lessons. Useful ideas. Progress at your pace.");
        LinearLayout p=panel(true); title(p,cpp?"C++ foundations":"C foundations",25);body(p,completed()+" of 10 lessons completed");
        ProgressBar courseProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);courseProgress.setMax(10);courseProgress.setProgress(completed());courseProgress.setProgressTintList(ColorStateList.valueOf(MINT));p.addView(courseProgress,new LinearLayout.LayoutParams(-1,dp(8)));
        action(p,cpp?"Switch to C":"Switch to C++",false,()->{cpp=!cpp;preferences.edit().putBoolean("cpp",cpp).apply();academy();});
        Lesson[] lessons=Curriculum.lessons(cpp);for(int i=0;i<lessons.length;i++){final int index=i;LinearLayout row=panel(false);row.addView(label(String.format(Locale.ROOT,"%02d  /  %s",i+1,progress.completed(cpp?"cpp-foundation":"c-foundation",Integer.toString(i))?"COMPLETED":"FOUNDATION"),12,MINT));title(row,lessons[i].title,21);action(row,"Open lesson  →",false,()->lesson(index));}
        action(content,"Worked math problems",false,this::mathProblems);action(content,"Guided projects",true,this::projects);action(content,"Your achievements",false,this::achievements);
    }
    private void lesson(int index) {
        Lesson lesson=Curriculum.lessons(cpp)[index];shell("lesson",(cpp?"C++":"C")+" FOUNDATIONS  /  "+(index+1)+" OF 10",lesson.title,"Read, experiment, and check your understanding.");
        LinearLayout explanation=panel(false);body(explanation,lesson.explanation);
        if(index==0)action(explanation,"Explore the syntax, token by token",true,()->readGuide("First program: syntax anatomy","lessons/first-program.txt"));
        LinearLayout example=panel(false);example.addView(label("WORKED EXAMPLE  /  SOURCE",12,MINT));TextView code=label(lesson.code,14,INK);code.setTypeface(Typeface.MONOSPACE);code.setTextIsSelectable(true);example.addView(code);action(example,"Open a copy in Studio",true,()->copyLesson(index));
        LinearLayout challenge=panel(true);title(challenge,"Your turn",22);body(challenge,lesson.challenge);
        LinearLayout quiz=panel(false);quiz.addView(label("PRACTICE CHECK  /  OFFLINE",12,LILAC));title(quiz,lesson.question,19);RadioGroup choices=new RadioGroup(this);int[] order=index%2==0?new int[]{0,1}:new int[]{1,0};for(int option:order){RadioButton button=new RadioButton(this);button.setId(View.generateViewId());button.setTag(option);button.setText(lesson.options[option]);button.setTextColor(INK);button.setTextSize(16);button.setMinHeight(dp(52));choices.addView(button);}quiz.addView(choices);TextView feedback=label("Choose an answer to check your understanding.",14,MUTED);quiz.addView(feedback);
        action(quiz,"Check answer",true,()->{if(choices.getCheckedRadioButtonId()==-1){feedback.setText(R.string.select_answer);return;}RadioButton selected=choices.findViewById(choices.getCheckedRadioButtonId());boolean right=((Integer)selected.getTag())==lesson.answer;feedback.setText(getString(right?R.string.correct_feedback:R.string.retry_feedback,lesson.feedback));feedback.setTextColor(right?MINT:0xffffd5a4);if(right)progress.complete(cpp?"cpp-foundation":"c-foundation",Integer.toString(index));feedback.announceForAccessibility(feedback.getText());});
        if(index<9)action(content,"Next lesson  →",false,()->lesson(index+1));action(content,"Back to course",false,this::academy);
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
        Aurora(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
            if(w<=0||h<=0)return;
            top=new RadialGradient(w*.95f,h*.08f,w*.95f,new int[]{0xff303465,0x00303465},null,Shader.TileMode.CLAMP);
            bottom=new RadialGradient(w*.02f,h*.58f,w*.8f,new int[]{0xff103e48,0x00103e48},null,Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();canvas.drawColor(0xff0b1020);paint.setShader(top);canvas.drawRect(0,0,w,h,paint);paint.setShader(bottom);canvas.drawRect(0,0,w,h,paint);paint.setShader(null);paint.setColor(0x12b6caff);paint.setStrokeWidth(1);for(int x=0;x<w;x+=64)canvas.drawLine(x,0,x,h,paint);for(int y=0;y<h;y+=64)canvas.drawLine(0,y,w,y,paint);}

    }
}
