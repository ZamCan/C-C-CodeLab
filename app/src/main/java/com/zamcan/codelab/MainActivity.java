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
    /** CodeLab currently provides an English-only interface, independent of device language. */
    @Override protected void attachBaseContext(Context base) {
        android.content.res.Configuration config = new android.content.res.Configuration(base.getResources().getConfiguration());
        config.setLocale(Locale.ENGLISH);
        config.setLocales(new android.os.LocaleList(Locale.ENGLISH));
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    private int INK=0xfff9fbff, MUTED=0xffd4e3f6, MINT=0xff7ee7f5, LILAC=0xffd5b7ff;
    private boolean light;
    private LinearLayout content;
    private ProjectStore store;
    private WorkspaceRepository workspaces;
    private String project="Practice";
    private LearningStore progress;
    private CodeLabAccount account;
    private LearningSync learningSync;
    private String verifiedAccountId;
    private android.content.SharedPreferences preferences;
    private boolean cpp, dirty, highlighting;
    private int activeLesson, activeStage;
    private Runnable readingBack;
    private String screen="home", filename="main.c", exportSource="";
    private EditText editor;
    private TextView saveStatus;
    private OfflineCompiler compiler;
    private CodeLabAds ads;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable autosave=() -> saveSource(false);

    @Override public void onCreate(Bundle state) {
        light=getSharedPreferences("academy",MODE_PRIVATE).getBoolean("light_theme",true);
        if(light) {setTheme(R.style.AppThemeLight);INK=0xff172440;MUTED=0xff52617a;MINT=0xff126bb7;LILAC=0xff7250b5;}
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(light?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR:0);
        preferences=getSharedPreferences("academy", MODE_PRIVATE);
        ads=new CodeLabAds(this,preferences);
        progress=new LearningStore(this);
        progress.importLegacy(preferences);
        account=CodeLabAccount.shared(this);
        learningSync=new LearningSync(this);
        restoreVerifiedAccount();
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
        else if(state!=null && ("lesson".equals(state.getString("screen"))||"story".equals(state.getString("screen"))) && preferences.getString("reading_key","").startsWith(courseKey()+":")) lesson(state.getInt("lesson"),state.getInt("stage"));
        else home();
    }
    /** Open a private database only after Appwrite confirms the active session. */
    private void restoreVerifiedAccount() {
        account.verify(new CodeLabAccount.Result() {
            @Override public void authenticated(String id) {
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed())return;
                    if(id.equals(verifiedAccountId)){
                        if(learningSync!=null)learningSync.sync(progress);
                        return;
                    }
                    if(progress!=null)progress.close();
                    verifiedAccountId=id;
                    preferences=getSharedPreferences("academy_user_"+id,MODE_PRIVATE);
                    progress=new LearningStore(MainActivity.this,id);
                    learningSync.sync(progress);
                    if("home".equals(screen))home();
                });
            }
            @Override public void unauthenticated(String reason) {
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed())return;
                    if(verifiedAccountId!=null){
                        if(progress!=null)progress.close();
                        progress=new LearningStore(MainActivity.this);
                        preferences=getSharedPreferences("academy",MODE_PRIVATE);
                        verifiedAccountId=null;
                        if("home".equals(screen))home();
                    }
                });
            }
        });
    }
    @Override protected void onResume() {
        super.onResume();
        if(account!=null)restoreVerifiedAccount();
    }
    @Override public void setContentView(View view) {
        if(compiler!=null){compiler.close();compiler=null;}
        if(ads!=null)ads.detach();
        super.setContentView(view);
        LearningFeedback.configureClicks(view,preferences==null||preferences.getBoolean("click_sounds",true));
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        saveSource(false); out.putString("screen",screen); out.putInt("lesson",activeLesson); out.putInt("stage",activeStage); super.onSaveInstanceState(out);
    }
    @Override protected void onPause() { saveSource(false); super.onPause(); }
    @Override protected void onDestroy() { if(compiler!=null)compiler.close(); handler.removeCallbacksAndMessages(null); if(ads!=null)ads.destroy(); if(progress!=null)progress.close(); super.onDestroy(); }
    @Override public void onBackPressed() {
        if("home".equals(screen)) super.onBackPressed();
        else if("story".equals(screen) && readingBack!=null)readingBack.run();
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
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,light ? (accent ? new int[]{0xfaffffff,0xeef0fbff} : new int[]{0xfaffffff,0xf0edf8ff}) : (accent ? new int[]{0xee315c8d,0xe8484b86} : new int[]{0xe43b6287,0xe42d476e}));
        d.setCornerRadius(dp(20)); d.setStroke(dp(1),light ? 0x669bbfe7 : (accent ? 0x99b6e9ff : 0x66d0e9ff)); return d;
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
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(primary?Color.WHITE:INK); b.setMinHeight(dp(50)); b.setPadding(dp(14),dp(10),dp(14),dp(10));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?(new int[]{0xff425ff5,0xff7851db}):(light?new int[]{0xffe9e5fa,0xffe4ecf9}:new int[]{0x40b1a4ef,0x252b4767}));
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
        if(!eyebrow.isEmpty())content.addView(label(eyebrow,14,MINT)); title(content,heading,28); if(!subtitle.isEmpty())body(content,subtitle);
        View gap=new View(this); content.addView(gap,new LinearLayout.LayoutParams(1,dp(20)));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(16),0,dp(16),dp(12)); nav.setBackground(glass(false)); stack.addView(nav);
        String[] tabs={"Home","Compiler","Learn C/C++"};
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
        if(ads!=null)ads.attach(stack,name);
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
        shell("home","", "Compile • Learn C/C++","Offline C/C++ Compiler & Learning Academy");
        artwork(content);
        googleProgressRow();
        action(content,"Compiler",true,this::studio);
        action(content,"Learn C/C++",true,this::academy);
        row(content,"§","Academic references","Standards, trusted reading and attribution",true,this::academicReferences);
        row(content,"⋮","Settings & about","Appearance, privacy and build status",true,()->new AlertDialog.Builder(this).setTitle("Settings").setItems(new String[]{"Appearance","Sound & motion","Test advertising","About"},(d,w)->{if(w==0)appearance();else if(w==1)feedbackSettings();else if(w==2)advertisingSettings();else status();}).show());
    }
    /** Appwrite-provided Google mark; this is an entry point, not a completed OAuth login. */
    private void googleProgressRow() {
        LinearLayout line=new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(dp(14),dp(12),dp(14),dp(12));
        line.setMinimumHeight(dp(76));
        line.setBackground(glass(false));
        ImageView google=new ImageView(this);
        google.setImageResource(R.drawable.appwrite_google_g);
        google.setContentDescription("Google");
        line.addView(google,new LinearLayout.LayoutParams(dp(32),dp(32)));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14),0,0,0);
        TextView title=label("Learning progress",17,INK);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        copy.addView(title);
        copy.addView(label(verifiedAccountId==null?"Guest • local progress":"Verified account • private offline progress",13,MUTED));
        line.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        line.setClickable(true);
        line.setFocusable(true);
        line.setContentDescription("Learning progress and account status.");
        line.setOnClickListener(v->progressSettings());
        content.addView(line,new LinearLayout.LayoutParams(-1,-2));
    }
    /** Educational references, not sponsorships or claims of textbook reproduction. */
    private void academicReferences() {
        shell("references","ABOUT THE LEARNING MATERIAL","Academic references","Official standards and independent reading");
        LinearLayout intro=panel(false);
        title(intro,"How CodeLab uses references",20);
        body(intro,"Lessons and examples are written for CodeLab. These links are independent resources for checking language rules and reading further. Listing a publisher or project does not mean it endorses or sponsors this app.");
        reference("ISO C language (WG14)","Official working group for the C language standard.","https://www.open-std.org/jtc1/sc22/wg14/");
        reference("ISO C++ language (WG21)","Official C++ standards committee and public papers.","https://isocpp.org/std/the-standard");
        reference("cppreference","Independent reference for C and C++ library and language features.","https://en.cppreference.com/w/");
        reference("The C Programming Language","Brian W. Kernighan and Dennis M. Ritchie; classic C textbook. Obtain from an authorized source.","https://www.pearson.com/");
        reference("Programming: Principles and Practice Using C++","Bjarne Stroustrup; textbook for learning programming with C++. Obtain from an authorized source.","https://www.stroustrup.com/programming.html");
        reference("Learn C++","Free online C++ learning resource; independent of CodeLab.","https://www.learncpp.com/");
        LinearLayout technology=panel(false);
        title(technology,"Technology acknowledgements",20);
        body(technology,"Appwrite: Google OAuth provider and private progress table configured; Android login and sync pending. Cloudflare: certification Worker deployed; certificate issuance disabled pending verification. Neither organization is a sponsor or endorser of CodeLab. Integration and deployment status must be verified separately.");
        action(content,"Back home",false,this::home);
    }
    private void reference(String name,String detail,String url) {
        row(content,"↗",name,detail,true,()->{
            try {
                Intent intent=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                message("No browser available",url);
            }
        });
    }
    private void progressSettings() {
        if(verifiedAccountId==null) {
            new AlertDialog.Builder(this).setTitle("Learning account")
                .setMessage("Guest progress stays on this device. Google sign-in uses Appwrite and will keep the authenticated account's learning data separate.")
                .setPositiveButton("Sign in with Google",(d,w)->startActivity(new Intent(this,GoogleSignInActivity.class)))
                .setNeutralButton("Study reminders",(d,w)->reminderSettings())
                .setNegativeButton("Continue as guest",null).show();
        } else {
            new AlertDialog.Builder(this).setTitle("Learning account")
                .setMessage("Signed in. Account-specific offline learning data is stored separately from guest data.")
                .setPositiveButton("OK",null)
                .setNeutralButton("Switch Google account",(d,w)->{
                    account.service().deleteSession("current",new io.appwrite.coroutines.CoroutineCallback<>((result,error)->runOnUiThread(()->{
                        if(error!=null){message("Could not sign out",error.getMessage());return;}
                        restoreVerifiedAccount();
                        startActivity(new Intent(this,GoogleSignInActivity.class));
                    })));
                })
                .setNegativeButton("Study reminders",(d,w)->reminderSettings()).show();
        }
    }
    private void reminderSettings() {
        boolean enabled=preferences.getBoolean(StudyReminders.PREF,false);
        new AlertDialog.Builder(this).setTitle("Offline study reminders")
            .setMessage("Receive a daily learning reminder around 6 PM. Reminders are generated on your device and do not upload guest data.")
            .setPositiveButton(enabled?"Turn off":"Turn on",(dialog,which)->{
                if(!enabled && android.os.Build.VERSION.SDK_INT>=33 &&
                    checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},81);
                StudyReminders.configure(this,!enabled);
            }).setNegativeButton("Cancel",null).show();
    }
    private void advertisingSettings() {
        boolean enabled=preferences.getBoolean("test_ads_opt_in",false);
        new AlertDialog.Builder(this).setTitle("AdMob test banners")
            .setMessage("Optional Google test ads appear only on Home. No real ad units are configured. Consent may be requested. Turning this off disables ad requests on subsequent screens.")
            .setPositiveButton(enabled?"Disable test ads":"Enable test ads",(dialog,which)->{
                preferences.edit().putBoolean("test_ads_opt_in",!enabled).apply();
                home();
            }).setNegativeButton("Cancel",null).show();
    }
    private void feedbackSettings() {
        String[] keys={"click_sounds","success_sound","success_motion"};
        boolean[] enabled={preferences.getBoolean(keys[0],true),preferences.getBoolean(keys[1],true),preferences.getBoolean(keys[2],true)};
        new AlertDialog.Builder(this).setTitle("Sound & motion")
            .setMultiChoiceItems(new String[]{"Button click sounds","Checkpoint success tone","Checkpoint animation"},enabled,(dialog,index,checked)->{
                preferences.edit().putBoolean(keys[index],checked).apply();
                LearningFeedback.configureClicks(findViewById(android.R.id.content),preferences.getBoolean("click_sounds",true));
            }).setPositiveButton("Done",null).show();
    }
    private void appearance() {
        shell("appearance","SETTINGS / APPEARANCE","Your learning space.","Choose a theme. Text follows your device font scaling.");
        LinearLayout p=panel(false);title(p,light?"Light glass":"Midnight glass",24);
        body(p,"A full-screen gradient, translucent surfaces and clear controls. Theme choice is saved on this device.");
        action(p,light?"Use midnight theme":"Use light theme",true,()->{preferences.edit().putBoolean("light_theme",!light).apply();recreate();});
        action(content,"Back home",false,this::home);
    }
    private void status() {
        shell("status","ABOUT THIS BUILD","Honest by design.","Development build 0.4.0");
        LinearLayout p=panel(false); title(p,"Available offline",22); body(p,"Lessons, practice quizzes, saved progress, source editing, multiple source files, and source export work without an account. Uninstalling clears local data; export important source files first.");
        LinearLayout s=panel(false); title(s,"Release gates still open",22); body(s,"An offline Clang/LLD WebAssembly compiler is bundled. Device execution, performance and compatibility remain under verification. See Runtime details in Compiler for supported features.\n\nOnline certificate exams, signed credentials, public verification, and production AdMob are not integrated. Practice completion is not certification.\n\nThe bundled compiler runs offline. Network permissions and Google test-ad SDK scaffolding are present for upcoming online features; optional consent-gated test banners are available from Settings; production ads remain disabled.");
        action(content,"Back home",true,this::home);
    }
    private void studio() { try { if(store.list().isEmpty())store.write("main.c",Curriculum.C[0].code);openEditor(store.exists(filename)?filename:store.list().get(0)); } catch(IOException e){error(e);} }
    private void compilerMenu() {
        new AlertDialog.Builder(this).setTitle(project).setItems(new String[]{"Files","New file","Projects","Export","Text size","Compiler self-test"},(dialog,item)->{
            if(item==0){String[] files=store.list().toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Project files").setItems(files,(d,w)->openEditor(files[w])).show();}
            else if(item==1){if(leaveEditor())newFile();}
            else if(item==2){if(leaveEditor())chooseProject();}
            else if(item==3 && saveSource(true)){exportSource=editor.getText().toString();try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,filename),41);}catch(ActivityNotFoundException e){error(e);}}
            else if(item==5){if(saveSource(true))startActivity(new Intent(this,CompilerDiagnosticsActivity.class));}
            else if(item==4)new AlertDialog.Builder(this).setTitle("Text size").setItems(new String[]{"16 sp","20 sp","24 sp"},(d,w)->{int size=new int[]{16,20,24}[w];preferences.edit().putInt("font",size).apply();editor.setTextSize(size);}).show();
        }).show();
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
        shell("editor","COMPILER",file,project);action(content,"⋮  Files & tools",false,this::compilerMenu);
        LinearLayout p=panel(false); saveStatus=label("Saved on this device",12,MINT); p.addView(saveStatus);
        editor=new CodeEditor(this,light); editor.setTextColor(INK); editor.setTextSize(preferences.getInt("font",15)); editor.setTypeface(Typeface.MONOSPACE); editor.setGravity(Gravity.TOP|Gravity.START); editor.setPadding(dp(44),dp(12),dp(8),dp(12)); editor.setBackgroundColor(light?0xfff6f8ff:0x66101729); editor.setMinLines(12); editor.setHorizontallyScrolling(true); editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ProjectStore.MAX_BYTES)}); editor.setContentDescription("Source code editor for "+file); editor.setText(source); p.addView(editor,new LinearLayout.LayoutParams(-1,-2));
        highlight(editor.getText());
        editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){}public void afterTextChanged(Editable s){if(highlighting)return;dirty=true;saveStatus.setText(R.string.saving);handler.removeCallbacks(autosave);handler.postDelayed(autosave,700);highlight(s);}});
        action(p,"Save",true,()->saveSource(true));
        LinearLayout terminal=panel(false);
        terminal.addView(label(file.endsWith(".cpp")?"C++17 · Clang 22 / WASI":"C17 · Clang 22 / WASI",12,LILAC));
        EditText stdin=new EditText(this);stdin.setTextColor(INK);stdin.setHintTextColor(MUTED);stdin.setHint("Program input (stdin)");stdin.setContentDescription("Program standard input");stdin.setMinLines(2);stdin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(65536)});terminal.addView(stdin);
        CheckBox multi=new CheckBox(this);multi.setText("Build all source files in this project");multi.setTextColor(INK);terminal.addView(multi);
        TextView state=label("Loading offline compiler…",14,MINT);state.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);terminal.addView(state);
        TextView diagnostics=label("",13,MUTED),stdout=label("",14,INK),stderr=label("",14,LILAC);
        for(TextView output:new TextView[]{diagnostics,stdout,stderr}){output.setTypeface(Typeface.MONOSPACE);output.setTextIsSelectable(true);}
        terminal.addView(label("Diagnostics",12,MUTED));terminal.addView(diagnostics);
        terminal.addView(label("stdout",12,MUTED));terminal.addView(stdout);
        terminal.addView(label("stderr",12,MUTED));terminal.addView(stderr);
        Button run=new Button(this);run.setText("Compile & run");run.setAllCaps(false);run.setEnabled(false);terminal.addView(run);
        compiler=new OfflineCompiler(this,event->{
            String type=event.optString("type");
            if(type.equals("ready")){state.setText("Ready · offline toolchain");run.setEnabled(true);}
            else if(type.equals("phase"))state.setText(event.optString("phase")+"…");
            else if(type.equals("output")){String stream=event.optString("stream");TextView target=stream.equals("stdout")?stdout:stream.equals("stderr")?stderr:diagnostics;if(target.length()<70000)target.append(event.optString("text"));}
            else if(type.equals("result")){state.setText(event.isNull("trap")?"Exit status: "+event.optString("exitCode"):"Runtime error: "+event.optString("trap"));run.setEnabled(true);}
            else if(type.equals("failure")||type.equals("cancelled")){state.setText(type.equals("cancelled")?"Cancelled":event.optString("message"));run.setEnabled(compiler!=null&&compiler.isReady());}
        });
        run.setOnClickListener(v->{
            if(!saveSource(true)||compiler==null)return;
            try{
                org.json.JSONObject files=new org.json.JSONObject();
                int sourceBytes=0;
                for(String name:store.list())if(name.equals(filename)||name.endsWith(".h")||name.endsWith(".hpp")||multi.isChecked()){
                    if(files.length()>=32)throw new IOException("Maximum 32 project files per build");
                    String text=store.read(name);sourceBytes+=text.getBytes(StandardCharsets.UTF_8).length;
                    if(sourceBytes>1048576)throw new IOException("Build sources exceed 1 MiB");
                    files.put(name,text);
                }
                diagnostics.setText("");stdout.setText("");stderr.setText("");run.setEnabled(false);
                compiler.run(new org.json.JSONObject().put("files",files).put("stdin",stdin.getText().toString()));
            }catch(Exception e){run.setEnabled(true);error(e);}
        });
        action(terminal,"Stop",false,()->{if(compiler!=null)compiler.cancel();});
        action(terminal,"Go to error",false,()->{
            for(CompilerEngine.Diagnostic diagnostic:ClangDiagnostics.parse(diagnostics.getText().toString()))if(diagnostic.filename.equals(filename)){
                String text=editor.getText().toString();int offset=0;
                for(int line=1;line<diagnostic.line&&offset<text.length();line++){int next=text.indexOf('\n',offset);offset=next<0?text.length():next+1;}
                editor.requestFocus();editor.setSelection(Math.min(text.length(),offset+Math.max(0,diagnostic.column-1)));return;
            }
            message("Diagnostics","No diagnostic location for the current source file. Open the reported project file using Files.");
        });
        action(terminal,"Runtime details",false,()->message("Offline C/C++ runtime","Clang 22.1.8, LLD, C17/C++17 headers, libc, libc++ and compiler builtins. Programs run as WebAssembly, not Android native binaries. Input is supplied before running; EOF follows. Run limit: 3 seconds, 64 MiB program memory, 64 KiB output. Virtual files are temporary. Threads, sockets, native GUI, dynamic libraries and C++ exceptions are not supported. Device execution is still under verification."));
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

    private void readStory(String key,String heading,String text,boolean code,Runnable finish,Runnable extra,String extraLabel) {
        handler.removeCallbacks(autosave);editor=null;dirty=false;screen="story";readingBack=this::academy;
        preferences.edit().putString("reading_key",key).apply();
        setContentView(new ReadingView(this,light,heading,text,code,preferences.getInt(key+":offset",0),
            offset->preferences.edit().putInt(key+":offset",offset).apply(),readingBack,finish,extra,extraLabel));
    }
    private void foundation(int index) {
        String[] titles={"What is an algorithm?","Algorithms and computers","Representing an algorithm","Decision tables","Pseudocode"};
        String[] files={"algorithm","computer","representations","decision-table","pseudocode"};
        if(index<0||index>=titles.length){programmingIntroduction();return;}
        preferences.edit().putInt("foundation_index",index).apply();
        try{readStory("foundation:"+index,titles[index],asset("foundation/"+files[index]+".txt"),false,()->foundation(index+1),this::programmingIntroduction,"Skip");}catch(IOException e){error(e);}
    }
    private void programmingIntroduction() {
        try{readStory("intro:languages","Programming languages",asset("foundation/programming-languages.txt"),false,this::languageHistory,null,"");}catch(IOException e){error(e);}
    }
    private void languageHistory() {
        try{readStory("intro:history","Meet C and C++",asset("foundation/language-history.txt"),false,this::introductionCheckpoint,null,"");}catch(IOException e){error(e);}
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
    private void introductionCheckpoint() { introductionQuestion(Math.min(3,preferences.getInt("intro_question",0))); }
    private void introductionQuestion(int index) {
        String[] questions={"Which statement describes C and C++?","Indentation alone defines a C block. True or false?","A ____ translates source into object code.","Arrange: 1 write source, 2 compile, 3 link."};
        String[][] options={{"They are identical languages.","They are related languages with different rules.","C++ accepts every C program unchanged.","C is another name for machine code."},{"False — braces group statements.","True — indentation groups statements."},{"compiler","linker","editor","loader"},{"1 → 2 → 3","1 → 3 → 2","2 → 1 → 3","3 → 2 → 1"}};
        int[] correct={1,0,0,0};
        String[] reasons={"C and C++ are related, but have distinct rules; valid C is not always valid C++.","Braces delimit a compound statement. Indentation communicates structure to readers.","The compiler produces object code in this workflow; the linker combines objects and libraries.","First write the source, compile it to objects, then link the objects and libraries."};
        preferences.edit().putInt("intro_question",index).apply();
        quizScreen("Introduction · "+(index+1)+" / 4",questions[index],options[index],correct[index],reasons[index],"intro:"+index,()->{
            if(index<3)introductionQuestion(index+1);else{preferences.edit().putBoolean("intro_checkpoint_v1",true).apply();academy();}
        },this::programmingIntroduction);
    }
    private void quizScreen(String title,String question,String[] options,int answer,String reason,String key,Runnable passed,Runnable review) {
        handler.removeCallbacks(autosave);editor=null;dirty=false;screen="quiz";
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(light?0xfffcfcff:0xff203d62);
        root.setPadding(dp(20),dp(12),dp(20),dp(12));root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(12)+insets.getSystemWindowInsetBottom());return insets;});
        TextView close=label("Close",16,MINT);close.setMinHeight(dp(48));close.setGravity(Gravity.CENTER_VERTICAL);close.setOnClickListener(v->academy());root.addView(close);
        root.addView(label(title,14,MUTED));
        RadioGroup choices=practiceChoices(root,question,options);
        String selection=key+":selection";int saved=preferences.getInt(selection,-1);
        if(saved>=0&&saved<choices.getChildCount())choices.check(choices.getChildAt(saved).getId());
        root.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
        action(root,"Check",true,()->{
            int choice=selected(choices);if(choice<0)return;
            boolean correct=choice==answer;
            boolean firstSuccess=correct&&!preferences.getBoolean(key+":celebrated",false);
            if(firstSuccess)preferences.edit().putBoolean(key+":celebrated",true).apply();
            if(correct && key.equals(lessonKey(activeLesson))){
                progress.complete(courseKey(),Integer.toString(activeLesson));
                if(learningSync!=null)learningSync.sync(progress);
            }
            preferences.edit().putInt(key+":attempts",preferences.getInt(key+":attempts",0)+1).apply();
            String feedback=(correct?"Correct. ":"Review and retry. ")+reason;
            readStory(key+":feedback",correct?"Well understood":"Keep exploring",feedback,false,correct?passed:()->quizScreen(title,question,options,answer,reason,key,passed,review),review,"Review");
            if(firstSuccess)LearningFeedback.success(this,findViewById(android.R.id.content),preferences.getBoolean("success_sound",true),preferences.getBoolean("success_motion",true));
        });
        Button check=(Button)root.getChildAt(root.getChildCount()-1);check.setEnabled(saved>=0);check.setAlpha(saved>=0?1f:.4f);
        choices.setOnCheckedChangeListener((group,id)->{int chosen=selected(group);preferences.edit().putInt(selection,chosen).apply();check.setEnabled(chosen>=0);check.setAlpha(chosen>=0?1f:.4f);});
        setContentView(root);
    }
    private String courseKey(){return cpp?"cpp-foundation":"c-foundation";}
    private String lessonKey(int index){return courseKey()+":"+index;}
    private void academy() {
        shell("academy","","Learn C/C++","Courses and practice");
        boolean introduced=preferences.getBoolean("intro_checkpoint_v1",false);
        LinearLayout next=panel(true);artwork(next);next.addView(label(introduced?"CONTINUE YOUR PATH":"START HERE",14,MINT));
        title(next,introduced?(cpp?"C++ foundations":"C foundations"):"C/C++ introduction",24);
        body(next,introduced?completed()+" of "+Curriculum.lessons(cpp).length+" preview checkpoints complete":"Explore how programs work, meet C and C++, then check your understanding.");
        action(next,introduced?"Continue":"Start",true,()->{if(introduced)lesson(preferences.getInt(courseKey()+":resume",0));else programmingIntroduction();});
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
        shell("pathways","COURSES","Choose your language","Both pathways remain available. Your progress is saved separately.");
        for(boolean language:new boolean[]{false,true}) {
            LinearLayout p=panel(language);p.addView(label(language?"C++ / ABSTRACTION & STRUCTURE":"C / PROGRAMMING FOUNDATIONS",14,language?LILAC:MINT));
            title(p,language?"Build with modern C++":"Understand the foundations of C",24);
            body(p,language?"Values, functions, containers and objects. Learn how C++ expresses reusable programs.":"Types, decisions, functions and memory. Learn how source code describes precise operations.");
            action(p,"Open",true,()->{cpp=language;preferences.edit().putBoolean("cpp",cpp).apply();courseMap();});
        }
        body(content,"Preview scope: ten introductory lessons per language. The complete sixteen-module pathways are still being developed.");
        action(content,"Back to journey",false,this::academy);
    }
    private void courseMap() {
        shell("course","COURSE / "+(cpp?"C++":"C"),cpp?"C++ foundations":"C foundations","Work through the checkpoints in order. Revisit completed lessons anytime.");
        Lesson[] lessons=Curriculum.lessons(cpp);LinearLayout summary=panel(true);
        title(summary,completed()+" / "+lessons.length+" checkpoints",24);
        ProgressBar meter=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);meter.setMax(lessons.length);meter.setProgress(completed());meter.setProgressTintList(ColorStateList.valueOf(MINT));summary.addView(meter,new LinearLayout.LayoutParams(-1,dp(6)));
        action(summary,"Continue",true,()->lesson(preferences.getInt(courseKey()+":resume",0)));
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
        if(stage<3) {
            String text=stage==0?lesson.explanation:stage==1?lesson.code:lesson.challenge;
            Runnable extra=stage==1?()->copyLesson(index):stage==2?()->lessonTools(index):index==0?()->readGuide("First program: syntax anatomy","lessons/first-program.txt"):()->lessonTools(index);
            readStory(lessonKey(index)+":stage:"+stage,lesson.title,text,stage==1,()->lesson(index,stage+1),extra,stage==1?"Try":stage==2?"Notes":"Explore");
            readingBack=this::courseMap;
        } else {
            quizScreen(lesson.title,lesson.question,lesson.options,lesson.answer,lesson.feedback,lessonKey(index),()->{
                progress.complete(courseKey(),Integer.toString(index));
                if(index+1<lessons.length)lesson(index+1,0);else readStory(courseKey()+":completed","Foundations completed",completed()+" of "+lessons.length+" preview checkpoints passed. Review the concepts or explore the guided projects. These are local learning achievements, not certificates.",false,this::courseMap,this::projects,"Projects");
            },()->lesson(index,0));
        }
    }
    private void lessonTools(int index) {
        EditText notes=new EditText(this);notes.setTextSize(16);notes.setMinLines(4);notes.setMaxLines(8);notes.setHint("My prediction and reasoning");notes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8000)});
        String key=lessonKey(index);notes.setText(preferences.getString(key+":notes",""));
        new AlertDialog.Builder(this).setTitle("Practice notes").setView(notes).setPositiveButton("Save",(d,w)->preferences.edit().putString(key+":notes",notes.getText().toString()).apply()).setNeutralButton(preferences.getBoolean(key+":bookmark",false)?"Unbookmark":"Bookmark",(d,w)->preferences.edit().putBoolean(key+":bookmark",!preferences.getBoolean(key+":bookmark",false)).putString(key+":notes",notes.getText().toString()).apply()).setNegativeButton("Cancel",null).show();
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
        action(content,"Try starter",true,()->copyAsset(id,true));
        action(content,"Reference",false,()->copyAsset(id,false));
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
            top=new RadialGradient(w*.95f,h*.08f,w*.95f,light?new int[]{0xffe5e6ff,0x00e5e6ff}:new int[]{0xff557cbb,0x00557cbb},null,Shader.TileMode.CLAMP);
            bottom=new RadialGradient(w*.02f,h*.58f,w*.8f,light?new int[]{0xffd8f5ff,0x00d8f5ff}:new int[]{0xff326f91,0x00326f91},null,Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();canvas.drawColor(light?0xfffcfdff:0xff203d62);paint.setShader(top);canvas.drawRect(0,0,w,h,paint);paint.setShader(bottom);canvas.drawRect(0,0,w,h,paint);paint.setShader(null);paint.setColor(light?0x0b344b80:0x12d9efff);paint.setStrokeWidth(1);for(int x=0;x<w;x+=64)canvas.drawLine(x,0,x,h,paint);for(int y=0;y<h;y+=64)canvas.drawLine(0,y,w,y,paint);}

    }
}
