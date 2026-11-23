package com.zamcan.codelab;

import android.app.AlertDialog;
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

/** Native reader for authored sections and measured text pages. */
public final class ReadingView extends LinearLayout {
    public interface Position { void save(int offset); }
    private final TextView body, rail, heading;
    private final LinearLayout segments;
    private final Button back, next;
    private final String source;
    private final boolean code,light;
    private final Runnable finish,close;
    private final Position position;
    private final List<Integer> starts=new ArrayList<>();
    private final ScrollView sectionScroll;
    private final LinearLayout sectionContent;
    private final int illustration;
    private int offset, measuredWidth, measuredHeight;
    private boolean layingOut;

    public ReadingView(Context context, boolean light, String title, String text,
            boolean code, int savedOffset, Position position, Runnable close,
            Runnable finish, Runnable extra, String extraLabel, int illustration) {
        super(context);this.source=text;this.code=code;this.light=light;this.close=close;this.offset=Math.max(0,Math.min(savedOffset,Math.max(0,text.length()-1)));
        this.illustration=illustration;this.position=position;this.finish=finish;
        setOrientation(VERTICAL);setBackgroundColor(light?0xfffcfcff:0xff0b1025);
        setPadding(dp(20),dp(12),dp(20),dp(12));
        setOnApplyWindowInsetsListener((view,insets)->{setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(12)+insets.getSystemWindowInsetBottom());return insets;});
        int ink=light?0xff202536:0xfff6f7ff;
        segments=new LinearLayout(context);addView(segments,new LayoutParams(-1,dp(4)));
        LinearLayout top=new LinearLayout(context);top.setGravity(Gravity.CENTER_VERTICAL);addView(top);
        Button exit=button("‹  Contents",false,light);exit.setOnClickListener(v->close.run());top.addView(exit,new LayoutParams(-2,dp(48)));
        rail=new TextView(context);rail.setTextColor(light?0xff425ff5:0xff91b7ff);rail.setTextSize(14);rail.setGravity(Gravity.END);
        top.addView(rail,new LayoutParams(0,-2,1));
        heading=new TextView(context);heading.setText(title);heading.setTextColor(ink);heading.setTextSize(22);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.setPadding(0,dp(12),0,dp(12));addView(heading);
        if(code){
            LinearLayout codeBar=new LinearLayout(context);codeBar.setGravity(Gravity.CENTER_VERTICAL);codeBar.setPadding(dp(12),0,0,0);
            TextView label=new TextView(context);label.setText("CODE EXAMPLE");label.setTextSize(11);label.setLetterSpacing(.12f);label.setTextColor(light?0xff087a68:0xff8ee7c8);codeBar.addView(label,new LayoutParams(0,-2,1));
            Button expand=button("Expand",false,light);expand.setOnClickListener(v->showCode());codeBar.addView(expand,new LayoutParams(dp(104),dp(40)));addView(codeBar);
        }
        body=new TextView(context);body.setTextColor(ink);body.setTextSize(16);body.setTextIsSelectable(true);body.setGravity(Gravity.TOP);body.setIncludeFontPadding(false);body.setLineSpacing(dp(4),1);
        body.setPadding(dp(20),dp(22),dp(20),dp(22));
        GradientDrawable paper=new GradientDrawable();paper.setCornerRadius(dp(18));paper.setColor(light?0xffffffff:0xff13203a);paper.setStroke(dp(1),light?0x1a1d3a6b:0x2e9ab4e8);body.setBackground(paper);
        if(code){body.setTypeface(Typeface.MONOSPACE);body.setTextDirection(TEXT_DIRECTION_LTR);}
        if(text.indexOf('\f')>=0){
            sectionScroll=new ScrollView(context);
            sectionContent=new LinearLayout(context);
            sectionContent.setOrientation(VERTICAL);
            sectionScroll.addView(sectionContent);
            addView(sectionScroll,new LayoutParams(-1,0,1));
            starts.add(0);
            for(int i=0;i<text.length()-1;i++)if(text.charAt(i)=='\f')starts.add(i+1);
        }else{
            sectionScroll=null;
            sectionContent=null;
            addView(body,new LayoutParams(-1,0,1));
        }
        if(extra!=null){Button more=button(extraLabel,false,light);more.setOnClickListener(v->extra.run());addView(more,new LayoutParams(-1,dp(48)));}
        LinearLayout controls=new LinearLayout(context);controls.setPadding(0,dp(12),0,0);addView(controls);
        back=button("Back",false,light);next=button("Next",true,light);
        LayoutParams previousParams=new LayoutParams(0,dp(52),1);previousParams.setMarginEnd(dp(8));controls.addView(back,previousParams);controls.addView(next,new LayoutParams(0,dp(52),2));
        back.setOnClickListener(v->{int page=current();if(page>0){offset=starts.get(page-1);showPage();}else close.run();});
        next.setOnClickListener(v->{int page=current();if(page+1<starts.size()){offset=starts.get(page+1);showPage();}else finish.run();});
        if(sectionScroll!=null)showPage();
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
        if(sectionScroll!=null)return;
        int width=Math.max(1,body.getWidth()-body.getPaddingLeft()-body.getPaddingRight());
        int height=Math.max(1,body.getHeight()-body.getPaddingTop()-body.getPaddingBottom());
        if(layingOut||width<=0||height<=0||(width==measuredWidth&&height==measuredHeight))return;
        layingOut=true;measuredWidth=width;measuredHeight=height;paginate(width,height);showPage();layingOut=false;
    }
    private void paginate(int width,int height){
        starts.clear();starts.add(0);int start=0;
        while(start<source.length()){
            CharSequence remaining=RichText.format(source.substring(start),light,code);
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
        segments.removeAllViews();for(int i=0;i<starts.size();i++){View segment=new View(getContext());GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(2));bg.setColor(i<page?0xff29b982:i==page?0xff6670ed:light?0xffdce0ed:0xff36405b);segment.setBackground(bg);LayoutParams lp=new LayoutParams(0,-1,1);lp.setMargins(dp(1),0,dp(1),0);segments.addView(segment,lp);}
        String section=source.substring(offset,end);
        if(sectionScroll==null)body.setText(RichText.format(section,light,code));
        else renderSection(section.trim(),page);
        rail.setText((page+1)+" / "+starts.size());
        back.setVisibility(page==0?View.GONE:View.VISIBLE);next.setText(page+1<starts.size()?"Continue ›":"Finish ✓");
        position.save(offset);body.announceForAccessibility("Scene "+(page+1)+" of "+starts.size());
    }
    private void renderSection(String section,int page){
        sectionContent.removeAllViews();
        String[] paragraphs=section.split("\\n\\s*\\n");
        for(int i=0;i<paragraphs.length;i++){
            String paragraph=paragraphs[i];
            int codeStart=paragraph.indexOf("\n    ");
            if(codeStart>=0 && !paragraph.startsWith("    ")){
                addParagraph(paragraph.substring(0,codeStart),false,false);
                paragraph=paragraph.substring(codeStart+1);
            }
            boolean snippet=paragraph.startsWith("    ");
            addParagraph(snippet?paragraph.replaceAll("(?m)^    ",""):paragraph,i==0,snippet);
            if(i==0 && page==0 && illustration!=0){
                ImageView picture=new ImageView(getContext());
                picture.setImageResource(illustration);
                picture.setAdjustViewBounds(true);
                picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picture.setContentDescription("Illustration of Dennis Ritchie and Bjarne Stroustrup, with functions on the C side and classes on the C++ side.");
                sectionContent.addView(picture,new LayoutParams(-1,-2));
            }
        }
        sectionScroll.scrollTo(0,0);
    }
    private void addParagraph(String value,boolean title,boolean snippet){
        TextView part=new TextView(getContext());
        part.setTextColor(light?0xff202536:0xfff6f7ff);
        part.setTextSize(title?22:snippet?15:17);
        part.setTextIsSelectable(true);
        part.setLineSpacing(dp(3),1f);
        part.setPadding(snippet?dp(12):0,dp(8),snippet?dp(12):0,dp(8));
        if(title)part.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        if(snippet){
            part.setTypeface(Typeface.MONOSPACE);
            part.setTextDirection(TEXT_DIRECTION_LTR);
            part.setBackgroundColor(light?0xffedf2f7:0xff172739);
            part.setText(RichText.format(value,light,true));
        }else part.setText(value);
        sectionContent.addView(part,new LayoutParams(-1,-2));
    }
    private void showCode(){
        TextView view=new TextView(getContext());view.setText(RichText.format(source,light,true));view.setTextSize(14);view.setTypeface(Typeface.MONOSPACE);view.setTextIsSelectable(true);
        view.setTextColor(light?0xff172440:0xfff6f7ff);view.setPadding(dp(16),dp(12),dp(16),dp(12));
        ScrollView scroll=new ScrollView(getContext());scroll.addView(view);
        new AlertDialog.Builder(getContext()).setTitle("Code example").setView(scroll).setPositiveButton("Close",null).show();
    }
}
