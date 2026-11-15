package com.zamcan.codelab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

/** Measured native reading pages. Text never shrinks to fit, and no ScrollView is used. */
public final class ReadingView extends LinearLayout {
    public interface Position { void save(int offset); }
    private final TextView body, rail, heading;
    private final LinearLayout segments;
    private final Button back, next;
    private final String source;
    private final Runnable finish;
    private final Position position;
    private final List<Integer> starts=new ArrayList<>();
    private int offset, measuredWidth, measuredHeight;
    private boolean layingOut;

    public ReadingView(Context context, boolean light, String title, String text,
            boolean code, int savedOffset, Position position, Runnable close,
            Runnable finish, Runnable extra, String extraLabel) {
        super(context);this.source=text;this.offset=Math.max(0,Math.min(savedOffset,Math.max(0,text.length()-1)));
        this.position=position;this.finish=finish;
        setOrientation(VERTICAL);setBackgroundColor(light?0xfffcfcff:0xff0b1025);
        setPadding(dp(20),dp(12),dp(20),dp(12));
        setOnApplyWindowInsetsListener((view,insets)->{setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(12)+insets.getSystemWindowInsetBottom());return insets;});
        int ink=light?0xff202536:0xfff6f7ff;
        segments=new LinearLayout(context);addView(segments,new LayoutParams(-1,dp(4)));
        LinearLayout top=new LinearLayout(context);top.setGravity(Gravity.CENTER_VERTICAL);addView(top);
        Button exit=button("Close",false,light);exit.setOnClickListener(v->close.run());top.addView(exit,new LayoutParams(-2,dp(48)));
        rail=new TextView(context);rail.setTextColor(light?0xff425ff5:0xff91b7ff);rail.setTextSize(14);rail.setGravity(Gravity.END);
        top.addView(rail,new LayoutParams(0,-2,1));
        heading=new TextView(context);heading.setText(title);heading.setTextColor(ink);heading.setTextSize(22);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.setPadding(0,dp(12),0,dp(12));addView(heading);
        body=new TextView(context);body.setTextColor(ink);body.setTextSize(16);body.setTextIsSelectable(true);body.setGravity(Gravity.TOP);body.setIncludeFontPadding(false);body.setLineSpacing(dp(4),1);
        if(code){body.setTypeface(Typeface.MONOSPACE);body.setTextDirection(TEXT_DIRECTION_LTR);}
        addView(body,new LayoutParams(-1,0,1));
        if(extra!=null){Button more=button(extraLabel,false,light);more.setOnClickListener(v->extra.run());addView(more,new LayoutParams(-1,dp(48)));}
        LinearLayout controls=new LinearLayout(context);controls.setPadding(0,dp(12),0,0);addView(controls);
        back=button("Back",false,light);next=button("Next",true,light);
        LayoutParams previousParams=new LayoutParams(0,dp(52),1);previousParams.setMarginEnd(dp(8));controls.addView(back,previousParams);controls.addView(next,new LayoutParams(0,dp(52),2));
        back.setOnClickListener(v->{int page=current();if(page>0){offset=starts.get(page-1);showPage();}});
        next.setOnClickListener(v->{int page=current();if(page+1<starts.size()){offset=starts.get(page+1);showPage();}else finish.run();});
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private Button button(String title,boolean primary,boolean light){
        Button button=new Button(getContext());button.setText(title);button.setTextSize(16);button.setAllCaps(false);
        button.setTextColor(primary?Color.WHITE:light?0xff425ff5:0xff91b7ff);
        GradientDrawable shape=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?new int[]{0xff425ff5,0xff7851db}:new int[]{Color.TRANSPARENT,Color.TRANSPARENT});shape.setCornerRadius(dp(16));
        button.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x337851db),shape,null));return button;
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){
        super.onLayout(changed,left,top,right,bottom);
        int width=body.getWidth(),height=body.getHeight();
        if(layingOut||width<=0||height<=0||(width==measuredWidth&&height==measuredHeight))return;
        layingOut=true;measuredWidth=width;measuredHeight=height;paginate(width,height);showPage();layingOut=false;
    }
    private void paginate(int width,int height){
        starts.clear();starts.add(0);int start=0;
        while(start<source.length()){
            String remaining=source.substring(start);
            StaticLayout layout=StaticLayout.Builder.obtain(remaining,0,remaining.length(),body.getPaint(),width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false).setLineSpacing(dp(4),1).build();
            if(layout.getHeight()<=height)break;
            int line=0;while(line+1<layout.getLineCount()&&layout.getLineBottom(line+1)<=height)line++;
            int end=start+layout.getLineEnd(line);
            if(end<=start)end=Math.min(source.length(),start+1);
            // Prefer complete authored paragraphs, then sentence boundaries, then measured lines.
            int paragraph=source.lastIndexOf("\n\n",end-1);
            int sentence=source.lastIndexOf(". ",end-1);
            int preferred=paragraph>start?paragraph+2:sentence>start?sentence+2:end;
            if(preferred-start>=(end-start)/3)end=preferred;
            if(end>=source.length())break;
            starts.add(end);start=end;
        }
    }
    private int current(){int page=0;while(page+1<starts.size()&&starts.get(page+1)<=offset)page++;return page;}
    private void showPage(){
        if(starts.isEmpty())return;
        int page=current();offset=starts.get(page);int end=page+1<starts.size()?starts.get(page+1):source.length();
        segments.removeAllViews();for(int i=0;i<starts.size();i++){View segment=new View(getContext());GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(2));bg.setColor(i<=page?0xff6670ed:0xffdce0ed);segment.setBackground(bg);LayoutParams lp=new LayoutParams(0,-1,1);lp.setMargins(dp(1),0,dp(1),0);segments.addView(segment,lp);}
        body.setText(source.substring(offset,end));rail.setText("Page "+(page+1)+" / "+starts.size());
        back.setEnabled(page>0);back.setAlpha(page>0?1f:.4f);next.setText(page+1==starts.size()?"Continue":"Next");
        position.save(offset);body.announceForAccessibility("Page "+(page+1)+" of "+starts.size());
    }
}
