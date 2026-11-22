package com.zamcan.codelab;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    /** Last server-verified Appwrite user ID. It selects a local database, nothing more. */
    private String verifiedAccountId;
    /** True only while Appwrite has confirmed the session in this process. */
    private boolean onlineVerified;
    private static final String LAST_VERIFIED_USER = "last_verified_account";
    private long accountVerificationGeneration;
    private android.content.SharedPreferences preferences;
    private boolean cpp, dirty, highlighting;
    private int activeLesson, activeStage;
    private Runnable readingBack, quizBack;
    private String screen="home", filename="main.c", exportSource="";
    private EditText editor;
    private TextView saveStatus;
    private OfflineCompiler compiler;
    private CodeLabAds ads;
    private boolean scratch;
    private String scratchFile="main.c", scratchSource="";
    private LinearLayout editorPane, outputPane;
    private FrameLayout workspace;
    private boolean outputVisible;
    private EditText stdinInput;
    private CheckBox buildAll;
    private TextView outputState, editorState, diagnosticsView, stdoutView, stderrView;
    private Button runButton, runAgainButton;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable autosave=() -> saveSource(false);

    @Override public void onCreate(Bundle state) {
        light=getSharedPreferences("academy",MODE_PRIVATE).getBoolean("light_theme",false);
        if(light) {setTheme(R.style.AppThemeLight);INK=0xff172440;MUTED=0xff52617a;MINT=0xff126bb7;LILAC=0xff7250b5;}
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(light?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR:0);
        preferences=getSharedPreferences("academy", MODE_PRIVATE);
        ads=new CodeLabAds(this,settings());
        account=CodeLabAccount.shared(this);
        learningSync=new LearningSync(this);
        openRememberedLearner();
        restoreVerifiedAccount();
        cpp=preferences.getBoolean("cpp", false);
        filename=preferences.getString("file", "main.c");
        if(!openWorkspaces())return;
        if(state!=null && "editor".equals(state.getString("screen"))) {
            if(state.getBoolean("scratch",false)) openScratchWith(state.getString("scratch_source",settings().getString("scratch_source","")),state.getString("scratch_file","main.c"));
            else openEditor(state.getString("file",filename));
        }
        else if(state!=null && ("lesson".equals(state.getString("screen"))||"story".equals(state.getString("screen"))) && preferences.getString("reading_key","").startsWith(courseKey()+":")) lesson(state.getInt("lesson"),state.getInt("stage"));
        else home();
    }
    /**
     * Opens the learner storage used in the previous session. A remembered ID only chooses a
     * private local database; it never authorizes upload or download. Cloud work waits for
     * Appwrite to confirm the session.
     */
    private void openRememberedLearner() {
        String remembered=settings().getString(LAST_VERIFIED_USER,null);
        if(remembered!=null && remembered.matches("[A-Za-z0-9_-]{1,36}")){
            verifiedAccountId=remembered;
            onlineVerified=false;
            preferences=getSharedPreferences("academy_user_"+remembered,MODE_PRIVATE);
            progress=new LearningStore(this,remembered);
            return;
        }
        verifiedAccountId=null;
        onlineVerified=false;
        progress=new LearningStore(this);
        progress.importLegacy(preferences);
    }
    /** Verifies the server session; account storage is opened only for a confirmed identity. */
    private void restoreVerifiedAccount() {
        if(account==null)return;
        final long request=++accountVerificationGeneration;
        account.verify(new CodeLabAccount.Result() {
            @Override public void authenticated(String id) {
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed() || request!=accountVerificationGeneration)return;
                    if(id.equals(verifiedAccountId)){
                        boolean newlyVerified=!onlineVerified;
                        onlineVerified=true;
                        if(learningSync!=null)learningSync.sync(progress);
                        if(newlyVerified && "home".equals(screen))home();
                        return;
                    }
                    switchToAccount(id);
                });
            }
            @Override public void unavailable(String reason) {
                // Offline: the last verified learner keeps working locally. No cloud call is
                // made until Appwrite verifies the session again, and nothing is deleted.
                runOnUiThread(()->{
                    if(request!=accountVerificationGeneration)return;
                    boolean wasVerified=onlineVerified;
                    onlineVerified=false;
                    if(wasVerified && verifiedAccountId!=null && "home".equals(screen))home();
                });
            }
            @Override public void unauthenticated(String reason) {
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed() || request!=accountVerificationGeneration)return;
                    if(verifiedAccountId==null)return;
                    // Expired or revoked session: close the account database and return to
                    // guest storage. Account data stays on disk for a later sign-in.
                    closeLearnerStorage();
                    settings().edit().remove(LAST_VERIFIED_USER).apply();
                    preferences=settings();
                    verifiedAccountId=null;
                    onlineVerified=false;
                    progress=new LearningStore(MainActivity.this);
                    progress.importLegacy(preferences);
                    if(!reopenLearnerWorkspace())return;
                    if("home".equals(screen))home();
                });
            }
        });
    }
    /** Closes the active learner database without deleting any stored progress. */
    private void closeLearnerStorage() {
        if(learningSync!=null)learningSync.cancel();
        if(progress!=null){progress.close();progress=null;}
    }
    /** Activates a newly verified account; invalidates every callback from the previous one. */
    private void switchToAccount(String id) {
        if(learningSync!=null)learningSync.cancel();
        if(progress!=null)progress.close();
        verifiedAccountId=id;
        onlineVerified=true;
        settings().edit().putString(LAST_VERIFIED_USER,id).apply();
        preferences=getSharedPreferences("academy_user_"+id,MODE_PRIVATE);
        progress=new LearningStore(MainActivity.this,id);
        if(learningSync!=null)learningSync.sync(progress);
        if(!reopenLearnerWorkspace())return;
        if("home".equals(screen))home();
    }
    /**
     * Ends the Appwrite session. The learner database is closed only after the server confirms
     * the sign-out, so a failed sign-out never strands account data behind a guest store.
     */
    private void signOut(Runnable after) {
        if(learningSync!=null)learningSync.cancel();
        final long request=++accountVerificationGeneration;
        account.service().deleteSession("current",new io.appwrite.coroutines.CoroutineCallback<>((result,error)->runOnUiThread(()->{
            if(isFinishing() || isDestroyed() || request!=accountVerificationGeneration)return;
            if(error!=null){
                restoreVerifiedAccount();
                message("Could not sign out","The session could not be closed. Check your connection and try again.");
                return;
            }
            if(progress!=null)progress.close();
            settings().edit().remove(LAST_VERIFIED_USER).apply();
            verifiedAccountId=null;
            onlineVerified=false;
            preferences=settings();
            progress=new LearningStore(MainActivity.this);
            progress.importLegacy(preferences);
            if(!reopenLearnerWorkspace())return;
            if(after!=null)after.run();else home();
        })));
    }
    /** Merges guest progress only after an explicit confirmation from the learner. */
    private void offerGuestTransfer() {
        new AlertDialog.Builder(this).setTitle("Copy guest progress?")
            .setMessage("Completed guest checkpoints will be merged into this account and queued for upload. Guest progress is not uploaded and is not deleted.")
            .setPositiveButton("Copy",(d,w)->transferGuestProgress())
            .setNegativeButton("Cancel",null).show();
    }
    private void transferGuestProgress() {
        final String owner=verifiedAccountId;
        final LearningStore target=progress;
        if(owner==null || target==null || target.isGuest())return;
        new Thread(()->{
            String status;
            try(LearningStore guest=new LearningStore(getApplicationContext())){
                target.mergeProgress(guest.exportProgress());
                target.enqueueAllForUpload(owner);
                status="Guest progress copied to this account";
            }catch(Exception e){status="Guest progress could not be copied";}
            final String report=status;
            runOnUiThread(()->{
                if(isFinishing() || isDestroyed())return;
                Toast.makeText(this,report,Toast.LENGTH_LONG).show();
                if(learningSync!=null)learningSync.sync(target);
            });
        },"guest-progress-copy").start();
    }
    /** Device-wide settings; learner content stays in account-scoped preferences. */
    private android.content.SharedPreferences settings() {
        return getSharedPreferences("academy",MODE_PRIVATE);
    }
    /**
     * Source workspaces are account-scoped, so one learner's saved code is not listed for a
     * different identity on the same device. Guest keeps the legacy shared workspace.
     */
    private File learnerFilesDir() {
        if(verifiedAccountId==null)return getFilesDir();
        String scope=LearnerDatabaseNames.forVerifiedUser(verifiedAccountId).replace(".db","");
        return new File(getFilesDir(),"learners/"+scope);
    }
    private boolean openWorkspaces() {
        try {
            workspaces=new WorkspaceRepository(learnerFilesDir());
            project=preferences.getString("project","Practice");
            store=workspaces.open(project);
            if(!store.exists("main.c"))store.write("main.c",Curriculum.C[0].code);
            if(!store.exists("main.cpp"))store.write("main.cpp",Curriculum.CPP[0].code);
            return true;
        } catch(IOException e){fatal(e);return false;}
    }
    /** Reopens the active learner's workspace after an identity change. */
    private boolean reopenLearnerWorkspace() {
        handler.removeCallbacks(autosave);
        editor=null;dirty=false;
        return openWorkspaces();
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
        saveSource(false); out.putString("screen",screen); out.putInt("lesson",activeLesson); out.putInt("stage",activeStage);
        out.putBoolean("scratch",scratch); out.putString("file",filename);
        if(scratch) out.putString("scratch_source",editor==null?scratchSource:String.valueOf(editor.getText()));
        super.onSaveInstanceState(out);
    }
    @Override protected void onPause() { saveSource(false); super.onPause(); }
    @Override protected void onDestroy() { if(compiler!=null)compiler.close(); if(learningSync!=null)learningSync.cancel(); handler.removeCallbacksAndMessages(null); if(ads!=null)ads.destroy(); if(progress!=null)progress.close(); super.onDestroy(); }
    @Override public void onBackPressed() {
        if("editor".equals(screen)&&outputVisible){showOutput(false);return;}
        if("home".equals(screen)) super.onBackPressed();
        else if("story".equals(screen) && readingBack!=null)readingBack.run();
        else if("quiz".equals(screen) && quizBack!=null)quizBack.run();
        else if("lesson".equals(screen)) { if(activeStage>0) lesson(activeLesson,activeStage-1); else courseMap(); }
        else if("course".equals(screen))pathways();
        else if("pathways".equals(screen))academy();
        else if("labs".equals(screen)||"history".equals(screen)||"achievements".equals(screen)||"projects".equals(screen))academy();
        else if("guide".equals(screen))projects();
        else if(leaveEditor()) home();
    }
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void fatal(Exception e) { new AlertDialog.Builder(this).setTitle("Storage unavailable").setMessage(e.getMessage()).setPositiveButton("Close",(d,w)->finish()).setCancelable(false).show(); }
    private void error(Exception e) { new AlertDialog.Builder(this).setTitle("Could not complete action").setMessage(e.getMessage()).setPositiveButton("OK",null).show(); }
    private void message(String title,String body) { new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Got it",null).show(); }
    private boolean leaveEditor() { if(editor==null)return true; if(scratch){saveSource(false);return true;} return !dirty || saveSource(true); }
    /** Native elevated surface: quiet fill, hairline border and a generous corner radius. */
    private GradientDrawable glass(boolean accent) {
        GradientDrawable d=new GradientDrawable();
        d.setCornerRadius(dp(22));
        d.setColor(light?(accent?0xfff6f9ff:0xffffffff):(accent?0xff25375f:0xff1c2c4e));
        d.setStroke(dp(1),light?(accent?0x332f5fe8:0x1a1d3a6b):(accent?0x4d8fb6ff:0x2e9ab4e8));
        return d;
    }
    private TextView label(String text,int size,int color) {
        TextView v=new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        v.setLineSpacing(dp(3),1); v.setPadding(0,dp(5),0,dp(5)); return v;
    }
    private void title(LinearLayout parent,String text,int size) { TextView t=label(text,size,INK); t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); parent.addView(t); }
    private void body(LinearLayout parent,String text) { parent.addView(label(text,16,MUTED)); }
    private LinearLayout panel(boolean accent) {
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(20),dp(18),dp(20),dp(18)); p.setBackground(glass(accent));
        p.setElevation(dp(2));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(14); p.setLayoutParams(lp); content.addView(p); return p;
    }
    private void action(LinearLayout parent,String text,boolean primary,Runnable run) {
        Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(16);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); b.setLetterSpacing(0.01f);
        b.setTextColor(primary?Color.WHITE:INK); b.setMinHeight(dp(52));
        b.setPadding(dp(16),dp(12),dp(16),dp(12));
        GradientDrawable background;
        if(primary){
            background=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff168e7a,0xff35c6a2});
            background.setCornerRadius(dp(16));
        } else {
            background=new GradientDrawable();
            background.setCornerRadius(dp(16));
            background.setColor(light?0xffffffff:0xff223351);
            background.setStroke(dp(1),light?0x33223a6b:0x4d9ab4e8);
        }
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(primary?0x40ffffff:0x33758cdb),background,null));
        b.setElevation(dp(primary?2:0));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(10); b.setLayoutParams(lp);
        b.setOnClickListener(v->run.run()); parent.addView(b);
    }
    private void shell(String name,String eyebrow,String heading,String subtitle) {
        handler.removeCallbacks(autosave); editor=null; dirty=false; screen=name;
        FrameLayout root=new FrameLayout(this); root.addView(new Aurora(this,light),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); root.addView(stack,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        stack.addView(topBar(name),new LinearLayout.LayoutParams(-1,dp(58)));
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); stack.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(20),dp(6),dp(20),dp(20)); scroll.addView(content);
        if(!eyebrow.isEmpty())content.addView(label(eyebrow,12,MINT)); if(!heading.isEmpty())title(content,heading,28); if(!subtitle.isEmpty())body(content,subtitle);
        if(!eyebrow.isEmpty()||!heading.isEmpty()||!subtitle.isEmpty()){View gap=new View(this);content.addView(gap,new LinearLayout.LayoutParams(1,dp(14)));}
        stack.addView(bottomNav(name),new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
        if(ads!=null)ads.attach(stack,name);
    }
    /** Compact app bar: brand or back on the left, settings overflow on the right. */
    private View topBar(String name) {
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(10),0,dp(8),0);
        if(!"home".equals(name)){
            Button back=compactButton("‹",this::onBackPressed);back.setTextSize(28);back.setContentDescription("Back");
            bar.addView(back,new LinearLayout.LayoutParams(dp(46),dp(46)));
        } else {
            TextView mark=label("</>",17,MINT);mark.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);mark.setGravity(Gravity.CENTER);
            mark.setBackground(iconBadge());bar.addView(mark,new LinearLayout.LayoutParams(dp(40),dp(40)));
        }
        String barTitle=headingFor(name);
        TextView heading=label(barTitle,17,INK);heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        heading.setPadding(dp(10),0,0,0);bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        ImageButton more=new ImageButton(this);more.setImageResource(R.drawable.ic_more_vert);
        more.setColorFilter(MINT);more.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22758cdb),null,null));
        more.setContentDescription("Settings and more");more.setPadding(dp(12),dp(12),dp(12),dp(12));
        more.setOnClickListener(this::showOverflow);bar.addView(more,new LinearLayout.LayoutParams(dp(48),dp(48)));
        return bar;
    }
    private String headingFor(String name){
        if("home".equals(name))return "Compile•Learn";
        if("editor".equals(name))return "Compiler";
        if("academy".equals(name))return "Learn C/C++";
        if("story".equals(name))return "Reading";
        if("quiz".equals(name))return "Checkpoint";
        if("labs".equals(name))return "Labs";
        if("history".equals(name))return "History";
        if("course".equals(name))return "Course";
        if("pathways".equals(name))return "Courses";
        if("projects".equals(name))return "Projects";
        if("guide".equals(name))return "Project lab";
        if("achievements".equals(name))return "Achievements";
        if("references".equals(name))return "References";
        if("appearance".equals(name))return "Appearance";
        if("status".equals(name))return "About";
        if("inbox".equals(name))return "Notifications";
        return "CodeLab";
    }
    private void showOverflow(View anchor) {
        if(editor!=null && !leaveEditor())return;
        String[] items={"Learning account","Notifications","Study reminders","Theme & appearance","Sound & motion","Test advertising","Academic references","About this build"};
        new AlertDialog.Builder(this).setTitle("CodeLab").setItems(items,(dialog,item)->{
            if(item==0)progressSettings();
            else if(item==1)notificationInbox();
            else if(item==2)reminderSettings();
            else if(item==3)appearance();
            else if(item==4)feedbackSettings();
            else if(item==5)advertisingSettings();
            else if(item==6)academicReferences();
            else if(item==7)status();
        }).show();
    }
    /** Persistent bottom navigation shared by every reading and workspace surface. */
    private View bottomNav(String name) {
        LinearLayout nav=new LinearLayout(this); nav.setPadding(dp(16),0,dp(16),dp(12)); nav.setBackground(glass(false));
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
        return nav;
    }
    private void artwork(LinearLayout parent) {
        ImageView art=new ImageView(this);BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;
        art.setImageBitmap(BitmapFactory.decodeResource(getResources(),light?R.drawable.academy_art_light:R.drawable.academy_art_dark,options));
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(-1,dp(144));layout.bottomMargin=dp(12);parent.addView(art,layout);
    }
    private void section(String text) {
        TextView heading=label(text,12,MUTED);
        heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        heading.setLetterSpacing(0.09f);
        heading.setPadding(dp(4),dp(20),0,dp(10));
        content.addView(heading);
    }
    private void row(LinearLayout parent,String number,String name,String detail,boolean enabled,Runnable open) {
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),dp(14),dp(14),dp(14));row.setMinimumHeight(dp(68));
        TextView marker=label(number,15,enabled?MINT:MUTED);marker.setGravity(Gravity.CENTER);marker.setTypeface(Typeface.MONOSPACE);
        row.addView(marker,new LinearLayout.LayoutParams(dp(40),-2));
        LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(dp(10),0,dp(10),0);
        TextView title=label(name,17,INK);title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));text.addView(title);
        text.addView(label(detail,14,enabled?MUTED:light?0xff8a97ad:0xff8fa1bd));
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView arrow=label("›",22,enabled?MINT:light?0xffb4bfd0:0xff63769a);row.addView(arrow,new LinearLayout.LayoutParams(dp(18),-2));
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(light?0x1a2f5fe8:0x33a8c6ff),null,null));
        row.setFocusable(true);row.setContentDescription(name+". "+detail);row.setOnClickListener(v->open.run());parent.addView(row);
        View line=new View(this);line.setBackgroundColor(light?0x141d3a6b:0x1a9ab4e8);parent.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    /** Offline-first inbox. Cloud push delivery requires an authenticated target and provider. */
    private void notificationInbox() {
        shell("inbox","LEARNING","Notifications","Learning activity saved on this device.");
        LinearLayout controls=panel(false);
        action(controls,"Notification preferences",false,this::reminderSettings);
        action(controls,"Mark all as read",false,()->{NotificationInbox.markRead(this);notificationInbox();});
        org.json.JSONArray entries=NotificationInbox.items(this);
        if(entries.length()==0){
            LinearLayout empty=panel(false);
            body(empty,"No notifications yet. Your study reminders and future course announcements will appear here.");
        }
        for(int i=0;i<entries.length();i++){
            org.json.JSONObject event=entries.optJSONObject(i);
            if(event==null)continue;
            LinearLayout item=panel(false);
            String heading=event.optString("title","Learning update");
            title(item,(event.optBoolean("read",false)?"":"●  ")+heading,18);
            body(item,event.optString("body",""));
            long time=event.optLong("time",0);
            if(time>0)body(item,android.text.format.DateFormat.getMediumDateFormat(this).format(new java.util.Date(time)));
        }
    }

    private void home() {
        shell("home","","","");
        content.addView(new BrandHero(this,light),new LinearLayout.LayoutParams(-1,dp(178)));
        TextView welcome=label("WELCOME",12,MINT);welcome.setLetterSpacing(.12f);welcome.setPadding(0,dp(14),0,0);content.addView(welcome);
        title(content,"Your C/C++ studio.",28);
        body(content,"Write immediately in a scratch buffer, or follow guided academic notes. The compiler and lessons work without an account.");
        googleProgressRow();
        destination("Compiler","Scratch buffer, project files and a real editor","▶",this::studio);
        destination("Learn C/C++","Guided notes, checkpoints and worked labs","◈",this::academy);
        boolean introduced=preferences.getBoolean("intro_checkpoint_v1",false);
        if(introduced){
            LinearLayout resume=panel(true);
            TextView eyebrow=label("CONTINUE",11,MINT);eyebrow.setLetterSpacing(.12f);resume.addView(eyebrow);
            title(resume,(cpp?"C++":"C")+" foundations",22);
            body(resume,"Pick up where you stopped. Your notes and checkpoint answers are saved on this device.");
            action(resume,"Resume pathway",true,()->lesson(preferences.getInt(courseKey()+":resume",0)));
        } else {
            LinearLayout start=panel(true);
            TextView eyebrow=label("NEW HERE",11,MINT);eyebrow.setLetterSpacing(.12f);start.addView(eyebrow);
            title(start,"Start with the introduction",22);
            body(start,"Learn how programs work, meet Dennis Ritchie's C and Bjarne Stroustrup's C++, then choose a pathway.");
            action(start,"Begin guided introduction",true,this::programmingIntroduction);
        }
        row(content,"◉","Notifications"+(NotificationInbox.unread(this)>0?" ("+NotificationInbox.unread(this)+")":""),"Your learning updates and reminders",true,this::notificationInbox);
        row(content,"§","Academic references","Standards, trusted reading and attribution",true,this::academicReferences);
    }
    /** One of the two primary home destinations: a large, quiet, native surface. */
    private void destination(String heading,String subtitle,String glyph,Runnable open) {
        LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.HORIZONTAL);tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(18),dp(16),dp(16),dp(16));tile.setMinimumHeight(dp(92));
        tile.setElevation(dp(3));tile.setClickable(true);tile.setFocusable(true);
        tile.setContentDescription(heading+". "+subtitle);
        LinearLayout badge=new LinearLayout(this);badge.setGravity(Gravity.CENTER);badge.setBackground(iconBadge());
        TextView icon=label(glyph,20,MINT);icon.setGravity(Gravity.CENTER);icon.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        badge.addView(icon,new LinearLayout.LayoutParams(-2,-2));
        tile.addView(badge,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(dp(16),0,dp(8),0);
        TextView title=label(heading,19,INK);title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));copy.addView(title);
        copy.addView(label(subtitle,14,MUTED));
        tile.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        TextView arrow=label("›",26,MINT);tile.addView(arrow,new LinearLayout.LayoutParams(dp(20),-2));
        tile.setBackground(new RippleDrawable(ColorStateList.valueOf(light?0x1a2f5fe8:0x33a8c6ff),glass(true),null));
        tile.setOnClickListener(v->open.run());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=dp(14);content.addView(tile,params);
    }
    private GradientDrawable iconBadge() {
        GradientDrawable badge=new GradientDrawable();badge.setShape(GradientDrawable.OVAL);
        badge.setColor(light?0xffeaf1ff:0xff2c3f6a);
        badge.setStroke(dp(1),light?0x332f5fe8:0x4d8fb6ff);
        return badge;
    }
    /** Appwrite-provided Google mark; this is an entry point, not a completed OAuth login. */
    private void googleProgressRow() {
        LinearLayout line=new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(dp(14),dp(12),dp(14),dp(12));
        line.setMinimumHeight(dp(76));
        line.setBackground(glass(false));
        line.setElevation(dp(1));
        ImageView google=new ImageView(this);
        google.setImageResource(R.drawable.appwrite_google_g);
        google.setContentDescription("Google");
        line.addView(google,new LinearLayout.LayoutParams(dp(32),dp(32)));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14),0,0,0);
        TextView title=label(verifiedAccountId==null?"Continue with Google":"Google account connected",17,INK);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        copy.addView(title);
        copy.addView(label(verifiedAccountId==null?"Optional sign-in • guest progress stays on this device"
            :onlineVerified?"Verified • private progress can sync"
            :"Progress is saved on this device",13,MUTED));
        line.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        line.setClickable(true);
        line.setFocusable(true);
        line.setContentDescription(verifiedAccountId==null?"Continue with Google. Optional. Guest learning stays on this device.":"Google account status.");
        line.setOnClickListener(v->{if(verifiedAccountId==null)startActivity(new Intent(this,GoogleSignInActivity.class));else progressSettings();});
        content.addView(line,new LinearLayout.LayoutParams(-1,-2));
        TextView advice=label("Google sign-in is optional. Every lesson, lab and scratch run works as a guest; guest learning progress is not uploaded to an account.",11,MUTED);
        advice.setPadding(dp(6),dp(2),dp(6),dp(12));content.addView(advice);
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
        body(technology,"Appwrite: Google sign-in and account-scoped progress sync are implemented in this build; live verification against the configured project is still pending. Cloudflare: the certification Worker integration is not enabled and certificate issuance remains disabled pending verification. Neither organization is a sponsor or endorser of CodeLab.");
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
                .setMessage("Guest progress stays on this device. Google sign-in uses Appwrite and keeps each account's learning data separate.")
                .setPositiveButton("Sign in with Google",(d,w)->startActivity(new Intent(this,GoogleSignInActivity.class)))
                .setNeutralButton("Study reminders",(d,w)->reminderSettings())
                .setNegativeButton("Continue as guest",null).show();
            return;
        }
        String[] actions={"Copy guest progress here","Study reminders","Switch Google account","Sign out"};
        new AlertDialog.Builder(this).setTitle("Learning account")
            .setMessage(onlineVerified
                ?"Google account verified. This account keeps its own private progress, bookmarks and upload queue."
                :"Your last verified session is ready. New progress stays on this device until your account is verified.")
            .setItems(actions,(dialog,which)->{
                if(which==0)offerGuestTransfer();
                else if(which==1)reminderSettings();
                else if(which==2)signOut(()->startActivity(new Intent(this,GoogleSignInActivity.class)));
                else signOut(null);
            })
            .setNegativeButton("Close",null).show();
    }
    private void reminderSettings() {
        boolean enabled=settings().getBoolean(StudyReminders.PREF,false);
        new AlertDialog.Builder(this).setTitle("Study reminders")
            .setMessage("Daily local reminders at your chosen time, outside quiet hours (10 PM–7 AM). No guest data is uploaded.")
            .setPositiveButton(enabled?"Turn off":"Turn on",(dialog,which)->{
                if(!enabled && android.os.Build.VERSION.SDK_INT>=33 &&
                    checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},81);
                StudyReminders.configure(this,!enabled);
            }).setNeutralButton("Change time",(dialog,which)->{
                int hour=settings().getInt("study_reminder_hour",18);
                int minute=settings().getInt("study_reminder_minute",0);
                new TimePickerDialog(this,(picker,h,m)->{
                    if(h<7 || h>=22){message("Quiet hours","Choose a time between 7:00 AM and 9:59 PM.");return;}
                    getSharedPreferences("academy",MODE_PRIVATE).edit()
                        .putInt("study_reminder_hour",h).putInt("study_reminder_minute",m).apply();
                    if(getSharedPreferences("academy",MODE_PRIVATE).getBoolean(StudyReminders.PREF,false))
                        StudyReminders.configure(this,true);
                },hour,minute,true).show();
            }).setNegativeButton("Cancel",null).show();
    }
    private void advertisingSettings() {
        boolean enabled=settings().getBoolean("test_ads_opt_in",false);
        new AlertDialog.Builder(this).setTitle("AdMob test banners")
            .setMessage("Optional Google test ads appear only on Home. No real ad units are configured. Consent may be requested. Turning this off disables ad requests on subsequent screens.")
            .setPositiveButton(enabled?"Disable test ads":"Enable test ads",(dialog,which)->{
                settings().edit().putBoolean("test_ads_opt_in",!enabled).apply();
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
        action(p,light?"Use midnight theme":"Use light theme",true,()->{settings().edit().putBoolean("light_theme",!light).apply();recreate();});
        action(content,"Back home",false,this::home);
    }
    private void status() {
        shell("status","ABOUT THIS BUILD","Honest by design.","Development build 0.5.1");
        LinearLayout p=panel(false); title(p,"Your learning space",22); body(p,"Lessons, practice, saved progress, source editing, multiple files, and source export are ready without an account. Export important source files before uninstalling.");
        LinearLayout s=panel(false); title(s,"Release gates still open",22); body(s,"An offline Clang/LLD WebAssembly compiler is bundled. Device execution, performance and compatibility remain under verification. See Runtime details in Compiler for supported features.\n\nOnline certificate exams, signed credentials, public verification, and production AdMob are not integrated. Practice completion is not certification.\n\nThe bundled compiler runs offline. Network permissions and Google test-ad SDK scaffolding are present for upcoming online features; optional consent-gated test banners are available from Settings; production ads remain disabled.");
        action(content,"Back home",true,this::home);
    }
    private void studio() {
        openScratchWith(preferences.getString("scratch_source",""),preferences.getString("scratch_file","main.c"));
    }
    private String safeSourceName(String file) {
        return file!=null&&file.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,47}\\.(c|cpp|h|hpp)")?file:"main.c";
    }
    private void openScratchWith(String source,String file) {
        scratch=true;filename=safeSourceName(file);scratchFile=filename;cpp=filename.endsWith(".cpp")||filename.endsWith(".hpp");
        scratchSource=source==null?"":source;
        preferences.edit().putString("scratch_file",filename).apply();
        openCompilerScreen(scratchSource);
    }
    private void openEditor(String file) {
        if(!leaveEditor()) return;
        String source; try {source=store.read(file);}catch(IOException e){error(e);return;}
        scratch=false;filename=file;cpp=file.endsWith(".cpp")||file.endsWith(".hpp");
        preferences.edit().putString("file",file).apply();
        openCompilerScreen(source);
    }
    /** A dedicated editor surface. The scratch buffer is complete without a file or save step. */
    private void openCompilerScreen(String source) {
        handler.removeCallbacks(autosave);editor=null;dirty=false;screen="editor";outputVisible=false;
        FrameLayout root=new FrameLayout(this);root.addView(new Aurora(this,light),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout stack=new LinearLayout(this);stack.setOrientation(LinearLayout.VERTICAL);root.addView(stack,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        stack.addView(topBar("editor"),new LinearLayout.LayoutParams(-1,dp(58)));
        workspace=new FrameLayout(this);stack.addView(workspace,new LinearLayout.LayoutParams(-1,0,1));

        editorPane=new LinearLayout(this);editorPane.setOrientation(LinearLayout.VERTICAL);editorPane.setPadding(dp(14),dp(2),dp(14),dp(8));
        workspace.addView(editorPane,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout fileRow=new LinearLayout(this);fileRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView fileName=label(filename,17,INK);fileName.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));fileRow.addView(fileName,new LinearLayout.LayoutParams(0,-2,1));
        final Button[] languageRef=new Button[1];
        languageRef[0]=compactButton(cpp?"C++17":"C17",()->{
            if(!scratch){message("Language follows the file","Create or open a .c or .cpp file to choose the language. The scratch buffer can switch freely.");return;}
            filename=filename.endsWith(".cpp")?"main.c":"main.cpp";scratchFile=filename;cpp=filename.endsWith(".cpp");fileName.setText(filename);languageRef[0].setText(cpp?"C++17":"C17");
            preferences.edit().putString("scratch_file",filename).apply();
        });fileRow.addView(languageRef[0],new LinearLayout.LayoutParams(dp(76),dp(38)));editorPane.addView(fileRow);
        LinearLayout meta=new LinearLayout(this);meta.setGravity(Gravity.CENTER_VERTICAL);
        meta.addView(label(scratch?"SCRATCH  ·  FREE BUFFER":"PROJECT  ·  "+project,11,MINT),new LinearLayout.LayoutParams(0,-2,1));
        saveStatus=label(scratch?"Not saved as a file":"Saved on this device",11,MUTED);meta.addView(saveStatus);editorPane.addView(meta);
        editorState=label("Preparing the compiler…",12,MINT);editorState.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);editorPane.addView(editorState);

        HorizontalScrollView tools=new HorizontalScrollView(this);tools.setHorizontalScrollBarEnabled(false);
        LinearLayout toolRow=new LinearLayout(this);toolRow.setPadding(0,dp(4),0,dp(4));tools.addView(toolRow);
        toolRow.addView(compactButton("Cut",()->editSelection("Cut")),new LinearLayout.LayoutParams(-2,dp(40)));
        toolRow.addView(compactButton("Copy",()->editSelection("Copy")),new LinearLayout.LayoutParams(-2,dp(40)));
        toolRow.addView(compactButton("Paste",()->editSelection("Paste")),new LinearLayout.LayoutParams(-2,dp(40)));
        toolRow.addView(compactButton(scratch?"Save as":"Save",()->{if(scratch)saveScratchAsFile();else saveSource(true);}),new LinearLayout.LayoutParams(-2,dp(40)));
        toolRow.addView(compactButton("Text size",this::textSizeDialog),new LinearLayout.LayoutParams(-2,dp(40)));
        editorPane.addView(tools);

        LinearLayout editorCard=new LinearLayout(this);editorCard.setOrientation(LinearLayout.VERTICAL);editorCard.setPadding(dp(2),dp(2),dp(2),dp(2));
        GradientDrawable editorBackground=new GradientDrawable();editorBackground.setColor(light?0xfff7f9ff:0xff0d172b);editorBackground.setCornerRadius(dp(16));editorBackground.setStroke(dp(1),light?0x225b78a8:0x336f9cdd);editorCard.setBackground(editorBackground);
        editor=new CodeEditor(this,light);editor.setTextColor(INK);editor.setTextSize(preferences.getInt("font",15));editor.setTypeface(Typeface.MONOSPACE);editor.setGravity(Gravity.TOP|Gravity.START);editor.setPadding(dp(56),dp(12),dp(10),dp(12));editor.setBackgroundColor(Color.TRANSPARENT);editor.setMinLines(6);editor.setHorizontallyScrolling(true);editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ProjectStore.MAX_BYTES)});editor.setContentDescription("Source code editor for "+filename);editor.setHint("Start typing C or C++…");editor.setHintTextColor(light?0x667080a0:0x66a9bdd8);editor.setText(source);editor.setLineSpacing(dp(2),1f);
        editorCard.addView(editor,new LinearLayout.LayoutParams(-1,0,1));editorPane.addView(editorCard,new LinearLayout.LayoutParams(-1,0,1));
        highlight(editor.getText());
        editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){}public void afterTextChanged(Editable s){if(highlighting)return;dirty=true;saveStatus.setText(scratch?"Scratch · saving locally":getString(R.string.saving));handler.removeCallbacks(autosave);handler.postDelayed(autosave,700);highlight(s);}});

        stdinInput=new EditText(this);stdinInput.setTextColor(INK);stdinInput.setHintTextColor(MUTED);stdinInput.setHint("Program input (stdin, optional)");stdinInput.setContentDescription("Program standard input");stdinInput.setMinLines(1);stdinInput.setMaxLines(3);stdinInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(65536)});stdinInput.setBackground(glass(false));stdinInput.setPadding(dp(12),dp(6),dp(12),dp(6));
        editorPane.addView(stdinInput,new LinearLayout.LayoutParams(-1,dp(46)));
        if(!scratch){buildAll=new CheckBox(this);buildAll.setText("Build all project files");buildAll.setTextColor(MUTED);buildAll.setTextSize(12);editorPane.addView(buildAll);}else buildAll=null;
        LinearLayout runRow=new LinearLayout(this);runRow.setPadding(0,dp(4),0,0);
        Button files=compactButton("Files / Project",this::filesMenu);runRow.addView(files,new LinearLayout.LayoutParams(0,dp(50),1));
        runButton=new Button(this);runButton.setText("Run");runButton.setAllCaps(false);runButton.setTextSize(16);runButton.setTextColor(Color.WHITE);runButton.setEnabled(false);
        GradientDrawable runBackground=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff168e7a,0xff35c6a2});runBackground.setCornerRadius(dp(15));runButton.setBackground(new RippleDrawable(ColorStateList.valueOf(0x40ffffff),runBackground,null));
        LinearLayout.LayoutParams runParams=new LinearLayout.LayoutParams(0,dp(50),2);runParams.setMarginStart(dp(8));runRow.addView(runButton,runParams);editorPane.addView(runRow);

        outputPane=new LinearLayout(this);outputPane.setOrientation(LinearLayout.VERTICAL);outputPane.setPadding(dp(14),dp(2),dp(14),dp(10));outputPane.setVisibility(View.GONE);
        GradientDrawable outputBackground=new GradientDrawable();outputBackground.setColor(light?0xccf7f9ff:0xcc0d172b);outputBackground.setCornerRadius(dp(16));outputBackground.setStroke(dp(1),light?0x225b78a8:0x336f9cdd);outputPane.setBackground(outputBackground);
        LinearLayout outputHeader=new LinearLayout(this);outputHeader.setGravity(Gravity.CENTER_VERTICAL);
        Button backToEditor=compactButton("‹ Editor",()->showOutput(false));outputHeader.addView(backToEditor,new LinearLayout.LayoutParams(-2,dp(42)));
        TextView outputTitle=label("OUTPUT",12,MINT);outputTitle.setLetterSpacing(.12f);outputTitle.setGravity(Gravity.END);outputHeader.addView(outputTitle,new LinearLayout.LayoutParams(0,-2,1));outputPane.addView(outputHeader);
        outputState=label("Preparing the compiler…",12,MINT);outputState.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);outputPane.addView(outputState);
        ScrollView outputScroll=new ScrollView(this);LinearLayout outputContent=new LinearLayout(this);outputContent.setOrientation(LinearLayout.VERTICAL);outputScroll.addView(outputContent);outputPane.addView(outputScroll,new LinearLayout.LayoutParams(-1,0,1));
        diagnosticsView=outputText();stdoutView=outputText();stderrView=outputText();
        outputContent.addView(label("Diagnostics",11,MUTED));outputContent.addView(diagnosticsView);
        outputContent.addView(label("stdout",11,MUTED));outputContent.addView(stdoutView);
        outputContent.addView(label("stderr",11,MUTED));outputContent.addView(stderrView);
        LinearLayout outputActions=new LinearLayout(this);outputActions.setPadding(0,dp(4),0,0);
        outputActions.addView(compactButton("Runtime details",()->message("Offline C/C++ runtime","Clang 22.1.8, LLD, C17/C++17 headers, libc, libc++ and compiler builtins. Programs run as WebAssembly, not Android native binaries. Input is supplied before running; EOF follows. Run limit: 3 seconds, 64 MiB program memory, 64 KiB output. Virtual files are temporary. Threads, sockets, native GUI, dynamic libraries and C++ exceptions are not supported. Device execution is still under verification.")),new LinearLayout.LayoutParams(0,dp(46),1));
        runAgainButton=compactButton("Run again",this::runCurrent);outputActions.addView(runAgainButton,new LinearLayout.LayoutParams(0,dp(46),1));outputPane.addView(outputActions);
        workspace.addView(outputPane,new FrameLayout.LayoutParams(-1,-1));

        stack.addView(bottomNav("editor"),new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
        runButton.setOnClickListener(v->runCurrent());
        compiler=new OfflineCompiler(this,event->{
            String type=event.optString("type");
            if(type.equals("ready"))setCompilerState("Ready · compiler available",true);
            else if(type.equals("phase"))setCompilerState(event.optString("phase")+"…",false);
            else if(type.equals("output")){String stream=event.optString("stream");TextView target=stream.equals("stdout")?stdoutView:stream.equals("stderr")?stderrView:diagnosticsView;if(target!=null&&target.length()<70000)target.append(event.optString("text"));}
            else if(type.equals("result"))setCompilerState(event.isNull("trap")?"Exit status: "+event.optString("exitCode"):"Runtime error: "+event.optString("trap"),compiler!=null&&compiler.isReady());
            else if(type.equals("failure")||type.equals("cancelled"))setCompilerState(type.equals("cancelled")?"Cancelled":event.optString("message"),compiler!=null&&compiler.isReady());
        });
    }
    private Button compactButton(String text,Runnable action) {
        Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(light?0xff0e6e62:0xffb9f4e8);b.setMinHeight(dp(40));b.setPadding(dp(10),0,dp(10),0);
        GradientDrawable background=new GradientDrawable();background.setCornerRadius(dp(12));background.setColor(light?0xffeaf7f4:0xff1a2f4a);background.setStroke(dp(1),light?0x33326f91:0x557ee7f5);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x337ee7f5),background,null));b.setOnClickListener(v->action.run());return b;
    }
    private TextView outputText(){TextView t=label("",13,INK);t.setTypeface(Typeface.MONOSPACE);t.setTextIsSelectable(true);t.setPadding(0,dp(2),0,dp(8));return t;}
    private void setCompilerState(String text,boolean ready) {
        if(editorState!=null)editorState.setText(text);if(outputState!=null)outputState.setText(text);
        if(runButton!=null)runButton.setEnabled(ready);if(runAgainButton!=null)runAgainButton.setEnabled(ready);
    }
    private void textSizeDialog() {
        new AlertDialog.Builder(this).setTitle("Editor text size").setItems(new String[]{"14 sp","16 sp","18 sp","20 sp","24 sp"},(d,w)->{int size=new int[]{14,16,18,20,24}[w];preferences.edit().putInt("font",size).apply();if(editor!=null)editor.setTextSize(size);}).show();
    }
    private void exportCurrent() {
        exportSource=editor==null?scratchSource:String.valueOf(editor.getText());
        try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE,filename),41);}catch(ActivityNotFoundException e){error(e);}
    }
    private void filesMenu() {
        String[] items=scratch?new String[]{"Open file","New file","Projects","Save scratch as file","Export source","Text size","Compiler self-test"}
                :new String[]{"Open file","New file","Projects","Rename file","Delete file","Save file","Export source","Text size","Compiler self-test"};
        new AlertDialog.Builder(this).setTitle("Files & projects").setItems(items,(dialog,item)->{
            if(item==0)openFileDialog();
            else if(item==1){if(leaveEditor())newFile();}
            else if(item==2){if(leaveEditor())chooseProject();}
            else if(scratch){
                if(item==3)saveScratchAsFile();
                else if(item==4)exportCurrent();
                else if(item==5)textSizeDialog();
                else if(item==6)startActivity(new Intent(this,CompilerDiagnosticsActivity.class));
            }else{
                if(item==3)renameCurrentFile();
                else if(item==4)deleteCurrentFile();
                else if(item==5)saveSource(true);
                else if(item==6)exportCurrent();
                else if(item==7)textSizeDialog();
                else if(item==8)startActivity(new Intent(this,CompilerDiagnosticsActivity.class));
            }
        }).show();
    }
    private void openFileDialog() {
        java.util.List<String> files=store.list();
        if(files.isEmpty()){message("No project files","Create a file from Files & projects. The scratch buffer never requires a file.");return;}
        new AlertDialog.Builder(this).setTitle("Project files · "+project).setItems(files.toArray(new String[0]),(d,w)->{if(leaveEditor())openEditor(files.get(w));}).show();
    }
    private void saveScratchAsFile() {
        EditText input=new EditText(this);input.setSingleLine(true);input.setHint("main.c");input.setText(filename);input.setPadding(dp(24),dp(12),dp(24),dp(12));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Save scratch as file").setMessage("The scratch buffer becomes a project file. Nothing is saved until you confirm.").setView(input).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{String name=input.getText().toString().trim();try{if(store.exists(name)){input.setError("That file already exists");return;}store.write(name,editor==null?scratchSource:String.valueOf(editor.getText()));dialog.dismiss();openEditor(name);}catch(IOException e){input.setError(e.getMessage());}}));dialog.show();
    }
    private void runCurrent() {
        if(editor==null||compiler==null)return;
        String source=String.valueOf(editor.getText());
        if(scratch){scratchSource=source;preferences.edit().putString("scratch_source",source).putString("scratch_file",filename).apply();}
        else if(!saveSource(false))return;
        try{
            org.json.JSONObject files=new org.json.JSONObject();
            int sourceBytes=source.getBytes(StandardCharsets.UTF_8).length;
            if(sourceBytes>1048576)throw new IOException("Build sources exceed 1 MiB");
            files.put(filename,source);
            if(!scratch){
                for(String name:store.list())if(!name.equals(filename)&&(name.endsWith(".h")||name.endsWith(".hpp")||(buildAll!=null&&buildAll.isChecked()))){
                    if(files.length()>=32)throw new IOException("Maximum 32 project files per build");
                    String text=store.read(name);sourceBytes+=text.getBytes(StandardCharsets.UTF_8).length;
                    if(sourceBytes>1048576)throw new IOException("Build sources exceed 1 MiB");
                    files.put(name,text);
                }
            }
            diagnosticsView.setText("");stdoutView.setText("");stderrView.setText("");
            setCompilerState("Compiling…",false);showOutput(true);
            compiler.run(new org.json.JSONObject().put("files",files).put("stdin",stdinInput==null?"":stdinInput.getText().toString()));
        }catch(Exception e){setCompilerState("Could not start",compiler!=null&&compiler.isReady());error(e);}
    }
    private void showOutput(boolean show) {
        if(workspace==null||editorPane==null||outputPane==null||show==outputVisible)return;
        outputVisible=show;int width=workspace.getWidth();
        if(width<=0){editorPane.setVisibility(show?View.GONE:View.VISIBLE);outputPane.setVisibility(show?View.VISIBLE:View.GONE);return;}
        if(show){
            outputPane.setVisibility(View.VISIBLE);outputPane.setTranslationX(width);outputPane.setAlpha(0f);
            editorPane.animate().translationX(-width*.35f).alpha(0f).setDuration(220).withEndAction(()->editorPane.setVisibility(View.GONE)).start();
            outputPane.animate().translationX(0f).alpha(1f).setDuration(260).start();
        }else{
            editorPane.setVisibility(View.VISIBLE);editorPane.setTranslationX(-width*.35f);editorPane.setAlpha(0f);
            editorPane.animate().translationX(0f).alpha(1f).setDuration(220).start();
            outputPane.animate().translationX(width).alpha(0f).setDuration(220).withEndAction(()->outputPane.setVisibility(View.GONE)).start();
        }
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
        if(!leaveEditor())return;
        try{store=workspaces.open(name);project=name;preferences.edit().putString("project",name).apply();editor=null;dirty=false;java.util.List<String> files=store.list();if(files.isEmpty())openScratchWith("","main.c");else openEditor(files.get(0));}catch(IOException e){error(e);}
    }
    private void newFile() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint("example.cpp"); input.setPadding(dp(24),dp(12),dp(24),dp(12));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("New source file").setMessage("Use .c, .cpp, .h, or .hpp. Existing files will not be replaced.").setView(input).setPositiveButton("Create",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{String name=input.getText().toString().trim();try {if(store.exists(name)) {input.setError("That file already exists");return;}store.write(name,"");dialog.dismiss();openEditor(name);}catch(IOException e){input.setError(e.getMessage());}}));dialog.show();
    }
    private void editSelection(String operation) {
        if(editor==null)return;
        Editable value=editor.getText();
        int a=Math.min(editor.getSelectionStart(),editor.getSelectionEnd());
        int b=Math.max(editor.getSelectionStart(),editor.getSelectionEnd());
        if(a<0||b<0)return;
        android.content.ClipboardManager clip=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(clip==null)return;
        if("Paste".equals(operation)){
            if(clip.hasPrimaryClip()&&clip.getPrimaryClip()!=null&&clip.getPrimaryClip().getItemCount()>0){
                CharSequence pasted=clip.getPrimaryClip().getItemAt(0).coerceToText(this);
                if(pasted!=null){value.replace(a,b,pasted);editor.setSelection(a+pasted.length());}
            }
        }else if(b>a){
            clip.setPrimaryClip(ClipData.newPlainText("CodeLab source",value.subSequence(a,b)));
            if("Cut".equals(operation))value.delete(a,b);
        }else message(operation,"Select some source text first.");
    }
    private void renameCurrentFile(){
        if(scratch){saveScratchAsFile();return;}
        if(editor==null||!saveSource(false))return;
        EditText input=new EditText(this);input.setSingleLine(true);input.setText(filename);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Rename file")
            .setView(input).setPositiveButton("Rename",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            try{
                String target=input.getText().toString().trim();
                store.rename(filename,target);
                dialog.dismiss();
                openEditor(target);
            }catch(IOException e){input.setError(e.getMessage());}
        }));
        dialog.show();
    }
    private void deleteCurrentFile(){
        if(scratch){message("Scratch buffer","The scratch buffer is not a project file. Clear the text or save it as a file first.");return;}
        if(editor==null)return;
        new AlertDialog.Builder(this).setTitle("Delete "+filename+"?")
            .setMessage("Delete this saved file from the current project? This cannot be undone.")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                try{
                    handler.removeCallbacks(autosave);
                    store.delete(filename);
                    editor=null;dirty=false;
                    java.util.List<String> files=store.list();
                    if(files.isEmpty())openScratchWith("","main.c");else openEditor(files.get(0));
                }catch(IOException e){error(e);}
            }).show();
    }
    private void highlight(Editable s) {
        if(s.length()>24000) return;
        highlighting=true;
        SyntaxHighlighter.apply(s,light);
        highlighting=false;
    }
    private boolean saveSource(boolean notify) {
        handler.removeCallbacks(autosave); if(editor==null)return true;
        String source=String.valueOf(editor.getText());
        if(scratch){
            scratchSource=source;preferences.edit().putString("scratch_source",source).putString("scratch_file",filename).apply();dirty=false;
            if(saveStatus!=null)saveStatus.setText("Scratch · saved locally");if(notify)Toast.makeText(this,"Scratch saved on this device",Toast.LENGTH_SHORT).show();return true;
        }
        try{store.write(filename,source);dirty=false;if(saveStatus!=null)saveStatus.setText(R.string.saved);if(notify)Toast.makeText(this,"Source saved",Toast.LENGTH_SHORT).show();return true;}catch(IOException e){dirty=true;if(saveStatus!=null)saveStatus.setText(R.string.save_failed);if(notify)error(e);return false;}
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
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=label("‹  Back",16,MINT);back.setMinHeight(dp(48));back.setGravity(Gravity.CENTER_VERTICAL);
        quizBack=()->{if(key.startsWith("intro:"))programmingIntroduction();else if(activeLesson>=0)lesson(activeLesson,2);else courseMap();};
        back.setOnClickListener(v->quizBack.run());
        bar.addView(back,new LinearLayout.LayoutParams(0,-2,1));
        TextView kind=label("CHECKPOINT",11,MUTED);kind.setLetterSpacing(.12f);bar.addView(kind);root.addView(bar);
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
            readingBack=()->quizScreen(title,question,options,answer,reason,key,passed,review);
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
        LinearLayout next=panel(true);next.addView(label(introduced?"YOUR PATH":"START HERE",11,MINT));
        title(next,introduced?(cpp?"C++ foundations":"C foundations"):"C/C++ introduction",24);
        body(next,introduced?completed()+" of "+Curriculum.lessons(cpp).length+" preview checkpoints complete":"Explore how programs work, meet C and C++, then check your understanding.");
        action(next,introduced?"Resume pathway":"Start introduction",true,()->{if(introduced)lesson(preferences.getInt(courseKey()+":resume",0));else programmingIntroduction();});
        section("THE JOURNEY");LinearLayout journey=panel(false);
        row(journey,"01","Think like a programmer","Optional · algorithms and problem solving",true,()->foundation(0));
        row(journey,"02","Meet C and C++",introduced?"Checkpoint passed · review anytime":"Required · languages, history and toolchain",true,this::programmingIntroduction);
        row(journey,"03","Choose your pathway","C or C++ · preview lessons and checkpoints",introduced,()->{if(introduced)pathways();else message("Start with the introduction","Complete the introduction checkpoint to begin either pathway.");});
        row(journey,"04","Build guided projects","Five projects · unlock through course practice",introduced,()->{if(introduced)projects();else message("Your project journey","The projects follow the course. Start with the introduction.");});
        section("LABS & PRACTICE");
        row(content,"ƒ","Math into code","Nine worked problems you can open and run",true,this::mathLab);
        row(content,"{}","Code labs","Ten small programs with teaching notes",true,this::codeLabs);
        row(content,"1972","C/C++ history","Dennis Ritchie, Bell Labs and the Unix years",true,this::historyTimeline);
        row(content,"✓","Progress & achievements","Your completed practice checks",true,this::achievements);
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
        action(summary,"Resume pathway",true,()->lesson(preferences.getInt(courseKey()+":resume",0)));
        LinearLayout group=null;
        for(int i=0;i<lessons.length;i++) {
            if(i%4==0){section("LESSONS "+(i+1)+"–"+Math.min(i+4,lessons.length));group=panel(false);}
            final int index=i;boolean complete=progress.completed(courseKey(),Integer.toString(i));
            boolean available=LearningPath.canOpen(preferences.getBoolean("intro_checkpoint_v1",false),i,lessons.length,i>0&&progress.completed(courseKey(),Integer.toString(i-1)));
            String detail=complete?"Checkpoint passed":available?"Ready to learn":"Complete the previous checkpoint";
            if(preferences.getBoolean(lessonKey(i)+":bookmark",false))detail+=" · Bookmarked";
            row(group,complete?"✓":String.format(Locale.ROOT,"%02d",i+1),lessons[i].title,detail,available,()->lesson(index));
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
        // Device-wide hint so an offline reminder never names another learner's content.
        settings().edit().putString("resume_hint",lesson.title).apply();
        if(stage<3) {
            String text=stage==0?lesson.explanation:stage==1?lesson.code:lesson.challenge;
            Runnable extra=stage==1?()->copyLesson(index):stage==2?()->lessonTools(index):index==0?()->readGuide("First program: syntax anatomy","lessons/first-program.txt"):()->lessonTools(index);
            readStory(lessonKey(index)+":stage:"+stage,lesson.title,text,stage==1,()->lesson(index,stage+1),extra,stage==1?"Try":stage==2?"Notes":"Explore");
            readingBack=this::courseMap;
        } else {
            quizScreen(lesson.title,lesson.question,lesson.options,lesson.answer,lesson.feedback,lessonKey(index),()->{
                progress.complete(courseKey(),Integer.toString(index));
                if((index+1)%4==0 || index+1==lessons.length)
                    celebrateCheckpoint(index+1,lessons.length,()->advanceAfterCheckpoint(index,lessons.length));
                else advanceAfterCheckpoint(index,lessons.length);
            },()->lesson(index,0));
        }
    }
    private void advanceAfterCheckpoint(int index,int total){
        if(index+1<total)lesson(index+1,0);
        else readStory(courseKey()+":completed","Foundations completed",
            completed()+" of "+total+" checkpoints passed. Review concepts or explore guided projects. These are learning achievements, not certificates.",
            false,this::courseMap,this::projects,"Projects");
    }
    private void celebrateCheckpoint(int passed,int total,Runnable next){
        final int green=0xff36d399;
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);layout.setPadding(dp(24),dp(24),dp(24),dp(20));
        TextView emblem=new TextView(this);emblem.setText("✓");emblem.setTextSize(56);
        emblem.setTextColor(green);emblem.setGravity(Gravity.CENTER);
        layout.addView(emblem,new LinearLayout.LayoutParams(-1,dp(88)));
        TextView heading=label(passed==total?"Course completed!":"Congratulations!",24,INK);
        heading.setGravity(Gravity.CENTER);layout.addView(heading);
        TextView description=label(passed+" of "+total+" checkpoints passed. Keep building your skills.",16,MUTED);
        description.setGravity(Gravity.CENTER);description.setPadding(0,dp(12),0,dp(12));layout.addView(description);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(layout)
            .setPositiveButton(passed==total?"View progress":"Next lesson",(d,w)->next.run())
            .setNegativeButton("Course contents",(d,w)->courseMap()).create();
        dialog.setOnShowListener(d->{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(green);
            if(preferences.getBoolean("success_motion",true)&&android.animation.ValueAnimator.areAnimatorsEnabled()){
                emblem.setScaleX(.4f);emblem.setScaleY(.4f);emblem.setAlpha(0f);
                emblem.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(480).start();
            }
            LearningFeedback.success(this,emblem,preferences.getBoolean("success_sound",true),false);
        });
        dialog.show();
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
        LinearLayout exams=panel(false);title(exams,"Certificate exams",22);body(exams,"Not available in this build. Certificate exams require a server-authorized timed attempt, trusted assessment, and protected credential signing. No payment or certificate claim is offered here.");action(content,"Return to pathway",true,this::academy);
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
    private void mathLab() {
        shell("labs","LABS  /  MATHEMATICS","Math into code","Nine worked problems. Read the reasoning, then open the program in the real compiler.");
        LinearLayout intro=panel(false);
        body(intro,"These examples connect a mathematical rule to C or C++ syntax. Predict the output before running, then change one value and explain the new result.");
        for(LabCatalog.Lab lab:LabCatalog.math())labCard(lab);
        action(content,"Back to academy",false,this::academy);
    }
    private void codeLabs() {
        shell("labs","LABS  /  CODE","Small programs, clear ideas","Ten complete examples covering functions, pointers, structures, algorithms and C++ classes.");
        LinearLayout intro=panel(false);
        body(intro,"Each lab opens in a scratch buffer. Nothing is saved as a project file unless you choose Save as. Run the program, then modify one line and predict the effect.");
        for(LabCatalog.Lab lab:LabCatalog.code())labCard(lab);
        action(content,"Back to academy",false,this::academy);
    }
    private void labCard(LabCatalog.Lab lab) {
        LinearLayout card=panel(false);
        TextView kind=label(lab.cpp?"C++ LAB":"C LAB",11,lab.cpp?LILAC:MINT);kind.setLetterSpacing(.12f);card.addView(kind);
        title(card,lab.title,21);body(card,lab.summary);
        TextView code=label("",13,INK);code.setTypeface(Typeface.MONOSPACE);code.setTextSize(12);code.setTextIsSelectable(true);
        code.setText(RichText.format(lab.source,light,true));code.setPadding(dp(12),dp(10),dp(12),dp(10));code.setLineSpacing(dp(2),1);
        GradientDrawable surface=new GradientDrawable();surface.setCornerRadius(dp(12));surface.setColor(light?0xfff4f8ff:0xff0b1526);surface.setStroke(dp(1),light?0x1a1d3a6b:0x2e9ab4e8);code.setBackground(surface);
        card.addView(code);
        action(card,"Open in compiler",true,()->{if(leaveEditor())openScratchWith(lab.source,lab.cpp?"main.cpp":"main.c");});
    }
    private void historyTimeline() {
        shell("history","ACADEMY  /  HISTORY","C, C++ and the Unix years","A concise timeline of the languages behind the compiler.");
        LinearLayout intro=panel(false);
        body(intro,"C grew from earlier Bell Labs languages and became the implementation language of Unix. C++ began as an extension of C, then developed its own standards and library. These are historical notes, not a claim that one language replaces the other.");
        timeline("1969–1972","Bell Labs and the birth of C","Dennis Ritchie developed C at Bell Labs, building on B and BCPL. It was designed for systems programming and closely tied to the development of Unix.");
        timeline("1973","Unix rewritten in C","Rewriting the Unix kernel in C demonstrated that a high-level language could express portable systems software. This helped C spread beyond one machine.");
        timeline("1978","The K&R book","Brian Kernighan and Dennis Ritchie published The C Programming Language. Its concise style and examples shaped how a generation learned C.");
        timeline("1979–1983","C with Classes becomes C++","Bjarne Stroustrup began work on C with Classes at Bell Labs in 1979. The language was named C++ in 1983 and added stronger abstraction while retaining C-style performance.");
        timeline("1989–1990","C standardized","ANSI C appeared in 1989 and became an ISO standard in 1990. Standardization made portability and conformance testable across implementations.");
        timeline("1998–2020","Modern C++ standards","C++98 was the first ISO C++ standard. Later revisions added libraries and language features while preserving a large C-compatible foundation. C and C++ remain distinct languages with different rules.");
        action(content,"Read the full history",true,this::readHistory);
        action(content,"Back to academy",false,this::academy);
    }
    private void timeline(String year,String heading,String text) {
        LinearLayout card=panel(false);
        TextView marker=label(year,12,MINT);marker.setLetterSpacing(.1f);card.addView(marker);
        title(card,heading,20);body(card,text);
    }
    private void readHistory() {
        try {
            readStory("history:standalone","C and C++: history and purpose",asset("foundation/language-history.txt"),false,this::historyTimeline,null,"");
            readingBack=this::historyTimeline;
        } catch(IOException e){error(e);}
    }
    private void readGuide(String title,String path){
        try{
            readStory("guide:"+path,title,asset(path),false,()->lesson(activeLesson,0),null,"");
            readingBack=()->lesson(activeLesson,0);
        }catch(IOException e){error(e);}
    }
    private void mathProblems(){mathLab();}

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
