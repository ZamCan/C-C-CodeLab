package com.zamcan.codelab;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One idea per step. Long paragraphs and source listings also break at measured lines. */
public final class ReadingView extends LinearLayout {
    public interface Position { void save(int offset); }
    public interface Responses {
        boolean answered(int offset);
        void record(int offset,String response);
    }
    private static final Pattern BREAK = Pattern.compile("\\n[ \\t]*\\n+|\\f+");
    private final ArrayList<Page> pages=new ArrayList<>();
    private final TextView body,rail;
    private final Button back,next,skip;
    private final ProgressBar meter;
    private final ImageView picture;
    private final String source;
    private final boolean code,light,challenge;
    private final int illustration;
    private final Position position;
    private final Responses responses;
    private final Runnable finish;
    private int current,offset,width,height;

    private static final class Page {
        final int start,end,question;
        final boolean snippet;
        Page(int start,int end,int question,boolean snippet){
            this.start=start;this.end=end;this.question=question;this.snippet=snippet;
        }
    }

    public ReadingView(Context context, boolean light, String title, String text, boolean code,
            int savedOffset, Position position, Runnable close, Runnable finish, Runnable extra,
            String extraLabel, int illustration, boolean challenge, Responses responses) {
        super(context);
        this.source=text;this.code=code;this.light=light;this.offset=savedOffset;
        this.position=position;this.finish=finish;this.illustration=illustration;
        this.challenge=challenge;this.responses=responses;
        setOrientation(VERTICAL);
        setBackgroundColor(light?0xfffaf9ff:0xff100b1a);
        setPadding(dp(18),dp(6),dp(18),dp(6));
        setOnApplyWindowInsetsListener((v,i)->{setPadding(dp(18)+i.getSystemWindowInsetLeft(),
            dp(6)+i.getSystemWindowInsetTop(),dp(18)+i.getSystemWindowInsetRight(),
            dp(6)+i.getSystemWindowInsetBottom());return i;});

        LinearLayout bar=new LinearLayout(context);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        addView(bar);
        Button contents=button("Contents",false);contents.setOnClickListener(v->close.run());
        bar.addView(contents);
        rail=new TextView(context);rail.setGravity(Gravity.CENTER);rail.setTextColor(ink());
        bar.addView(rail,new LayoutParams(0,-2,1));
        skip=button("Skip reading",false);skip.setOnClickListener(v->skipReading());bar.addView(skip);
        meter=new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);
        meter.setProgressTintList(android.content.res.ColorStateList.valueOf(light?0xff6953ad:0xffbaa7ff));
        addView(meter,new LayoutParams(-1,dp(4)));
        TextView heading=new TextView(context);heading.setText(title);heading.setTextSize(22);
        heading.setTextColor(ink());heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setPadding(0,dp(10),0,dp(8));addView(heading);

        FrameLayout viewport=new FrameLayout(context);
        addView(viewport,new LayoutParams(-1,0,1));
        body=new TextView(context);body.setTextSize(17);body.setTextColor(ink());body.setTextIsSelectable(true);
        body.setIncludeFontPadding(false);body.setLineSpacing(dp(4),1f);
        body.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_SIMPLE);
        body.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NONE);
        body.setPadding(0,dp(6),0,dp(6));viewport.addView(body,new FrameLayout.LayoutParams(-1,-1));
        picture=new ImageView(context);picture.setVisibility(GONE);
        if(illustration!=0){
            picture.setImageResource(illustration);
            picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
            picture.setContentDescription("Dennis Ritchie and Bjarne Stroustrup with C functions and C++ classes.");
            viewport.addView(picture,new FrameLayout.LayoutParams(-1,-1));
        }
        LinearLayout controls=new LinearLayout(context);controls.setGravity(Gravity.CENTER_VERTICAL);
        addView(controls);
        back=button("Back",false);back.setOnClickListener(v->{if(current>0)show(current-1);});
        controls.addView(back,new LayoutParams(0,-2,1));
        if(extra!=null){
            Button more=button(extraLabel,false);more.setOnClickListener(v->extra.run());
            controls.addView(more,new LayoutParams(0,-2,1));
        }
        next=button("Next",true);next.setOnClickListener(v->advance());
        controls.addView(next,new LayoutParams(0,-2,2));
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int ink(){return light?0xff211a32:0xfff7f2ff;}
    private Button button(String title,boolean primary){
        Button b=new Button(getContext());b.setText(title);b.setAllCaps(false);b.setMinHeight(dp(48));
        b.setMinWidth(0);b.setPadding(dp(8),0,dp(8),0);b.setTextColor(primary?0xff201400:ink());
        GradientDrawable bg=new GradientDrawable();bg.setColor(primary?0xffffbc38:0x00000000);bg.setCornerRadius(dp(12));
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x308371ab),bg,null));
        return b;
    }

    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        super.onLayout(changed,l,t,r,b);
        int w=body.getWidth()-body.getPaddingLeft()-body.getPaddingRight();
        int h=body.getHeight()-body.getPaddingTop()-body.getPaddingBottom();
        if(w<=0||h<=0||(w==width&&h==height))return;
        width=w;height=h;pages.clear();
        if(illustration!=0)pages.add(new Page(-1,-1,-1,false));
        if(code)addBlock(0,source.length());
        else{
            Matcher breaks=BREAK.matcher(source);
            int start=0;
            while(breaks.find()){
                String block=source.substring(start,breaks.start()).trim();
                boolean title=!block.contains("\n")&&block.length()<65
                    &&!source.startsWith("    ",start)&&!block.matches("(?s).*?[?.!;}]$");
                boolean predictionWithCode=source.startsWith("    ",breaks.end())
                    &&ReadingPause.required(block,false,false);
                if((title||predictionWithCode)&&breaks.end()<source.length()
                        &&breaks.group().indexOf('\f')<0)continue;
                addBlock(start,breaks.start());start=breaks.end();
            }
            addBlock(start,source.length());
        }
        if(pages.isEmpty())pages.add(new Page(0,0,-1,false));
        int saved=0;
        while(saved+1<pages.size()&&pages.get(saved+1).start<=offset)saved++;
        for(int i=0;i<saved;i++)if(pending(pages.get(i))){saved=i;break;}
        show(saved);
    }
    private void addBlock(int start,int end){
        if(start>=end)return;
        String raw=source.substring(start,end);
        if(raw.trim().isEmpty())return;
        boolean snippet=code||raw.startsWith("    ")||raw.trim().startsWith("#include");
        boolean question=ReadingPause.required(raw,snippet,challenge);
        // Show the author's answer only after the learner has recorded a prediction.
        int boundary=raw.indexOf("? ");
        if(question&&boundary>=0&&!challenge&&!raw.contains("\n    ")){
            fit(start,start+boundary+1,start,snippet);
            fit(start+boundary+2,end,-1,snippet);
        }else fit(start,end,question?start:-1,snippet);
    }
    private CharSequence styled(String value,boolean snippet){
        if(snippet)return RichText.format(value,light,true);
        SpannableStringBuilder result=new SpannableStringBuilder();
        for(String line:value.split("(?<=\\n)",-1)){
            int start=result.length();
            if(line.startsWith("    ")){
                result.append(RichText.format(line,light,true));
                result.setSpan(new TypefaceSpan("monospace"),start,result.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else result.append(line);
        }
        return result;
    }
    private void fit(int start,int end,int question,boolean snippet){
        if(start>=end)return;
        body.setTypeface(snippet?Typeface.MONOSPACE:Typeface.DEFAULT);
        CharSequence text=styled(source.substring(start,end),snippet);
        StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),body.getPaint(),width)
            .setIncludePad(false).setLineSpacing(dp(4),1f).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE).build();
        int first=0;
        while(first<layout.getLineCount()){
            int last=first;
            while(last+1<layout.getLineCount()&&
                (!snippet||last-first<8)&&layout.getLineBottom(last+1)-layout.getLineTop(first)<=height)last++;
            int a=start+layout.getLineStart(first),b=start+layout.getLineEnd(last);
            if(b>a)pages.add(new Page(a,b,b==end?question:-1,snippet));
            first=last+1;
        }
    }
    private boolean pending(Page p){return p.question>=0&&!responses.answered(p.question);}
    private void show(int index){
        current=index;Page p=pages.get(index);offset=p.start;position.save(offset);
        boolean art=illustration!=0&&index==0;
        body.setVisibility(art?INVISIBLE:VISIBLE);picture.setVisibility(art?VISIBLE:GONE);
        body.setTypeface(p.snippet?Typeface.MONOSPACE:Typeface.DEFAULT);
        if(!art)body.setText(styled(source.substring(p.start,p.end),p.snippet));

        rail.setText((index+1)+" / "+pages.size());
        meter.setMax(pages.size());meter.setProgress(index+1);
        back.setEnabled(index>0);back.setAlpha(index>0?1f:.35f);
        boolean inQuestion=false;
        for(int i=index;i<pages.size();i++)if(pending(pages.get(i))){
            inQuestion=p.start>=pages.get(i).question;
            break;
        }
        skip.setVisibility(inQuestion||index+1==pages.size()?INVISIBLE:VISIBLE);
        next.setText(pending(p)?"Respond":index+1==pages.size()?"Continue":"Next");
    }
    private void skipReading(){
        for(int i=current;i<pages.size();i++)if(pending(pages.get(i))){
            int j=i;
            while(j>current&&pages.get(j-1).start>=pages.get(i).question)j--;
            show(j);return;
        }
        finish.run();
    }
    private void advance(){
        if(pages.isEmpty())return;
        Page p=pages.get(current);
        if(!pending(p)){nextPage();return;}
        EditText input=new EditText(getContext());
        input.setHint("Your prediction or reasoning");
        input.setMinLines(2);input.setMaxLines(4);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2000)});
        AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle("Think it through")
            .setView(input).setPositiveButton("Save response",null).setNegativeButton("Back",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            String value=input.getText().toString().trim();
            if(value.isEmpty()){input.setError("Enter your response first");return;}
            responses.record(p.question,value);dialog.dismiss();nextPage();
        }));
        dialog.show();
    }
    private void nextPage(){if(current+1<pages.size())show(current+1);else finish.run();}
}
