package com.zamcan.codelab;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import androidx.appcompat.widget.AppCompatEditText;

/** Editable source text with a fixed line gutter, indentation and brace-aware entry. */
public final class CodeEditor extends AppCompatEditText {
    private final Paint gutter=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gutterBackground=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint currentLine=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean changing, cpp;
    private final LinearLayout assistance, words;
    private final HorizontalScrollView suggestions;
    private final TextView advisory;
    private final int ink;
    private int insertedAt = -1;
    private final Runnable update = this::refreshAssistance;
    private final int gutterWidth;

    public CodeEditor(Context context,boolean light) {
        // AppCompat widgets require an AppCompat theme. Scope that theme to the
        // editor so the rest of the native activity keeps its existing styling.
        super(new ContextThemeWrapper(context,light?androidx.appcompat.R.style.Theme_AppCompat_Light:androidx.appcompat.R.style.Theme_AppCompat));
        setTextDirection(TEXT_DIRECTION_LTR);
        gutterWidth=Math.round(48*getResources().getDisplayMetrics().density);
        gutter.setColor(light?0xff6f7f99:0xff91a6c4);
        gutter.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);
        gutter.setTextAlign(Paint.Align.RIGHT);
        gutter.setTypeface(Typeface.MONOSPACE);
        gutterBackground.setColor(light?0xffeef3fc:0xff0d172b);
        currentLine.setColor(light?0x142f5bd0:0x18a9c8ff);
        ink=light?0xff394466:0xffdfdcf4;
        assistance=new LinearLayout(context);
        assistance.setOrientation(LinearLayout.VERTICAL);
        assistance.setVisibility(GONE);
        suggestions=new HorizontalScrollView(context);
        suggestions.setHorizontalScrollBarEnabled(false);
        words=new LinearLayout(context);
        suggestions.addView(words);
        assistance.addView(suggestions);
        advisory=new TextView(context);
        advisory.setTextSize(13);
        advisory.setTextColor(light?0xff883d12:0xffffc48e);
        advisory.setGravity(Gravity.CENTER_VERTICAL);
        advisory.setPadding(dp(6),dp(4),dp(6),dp(4));
        advisory.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        assistance.addView(advisory);
        addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence text,int start,int count,int after){}
            public void onTextChanged(CharSequence text,int start,int before,int count){
                insertedAt=!changing && isFocused() && before==0 && count==1?start:-1;
            }
            public void afterTextChanged(Editable text){
                if(changing)return;
                int offset=insertedAt;
                insertedAt=-1;
                if(offset>=0 && offset<text.length()){
                    changing=true;
                    try {
                        char typed=text.charAt(offset);
                        if(typed=='\n'){
                            String indent=EditorRules.indentAfter(text.toString(),offset);
                            if(!indent.isEmpty())text.insert(offset+1,indent);
                        }else if(typed=='}'){
                            int remove=EditorAssist.dedentStart(text,offset);
                            if(remove<offset)text.delete(remove,offset);
                        }
                    }finally{changing=false;}
                }
                scheduleAssistance();
            }
        });
    }

    public void setSourceName(String name) {
        cpp=name.endsWith(".cpp") || name.endsWith(".hpp");
        scheduleAssistance();
    }

    public View assistanceBar() {return assistance;}

    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}

    private void scheduleAssistance(){
        if(assistance==null)return;
        removeCallbacks(update);
        postDelayed(update,300);
    }

    @Override protected void onSelectionChanged(int start,int end){
        super.onSelectionChanged(start,end);
        scheduleAssistance();
    }

    @Override protected void onFocusChanged(boolean focused,int direction,Rect previous){
        super.onFocusChanged(focused,direction,previous);
        scheduleAssistance();
    }

    @Override protected void onDetachedFromWindow(){
        removeCallbacks(update);
        super.onDetachedFromWindow();
    }

    private void refreshAssistance(){
        Editable source=getText();
        if(source==null)return;
        words.removeAllViews();
        int cursor=getSelectionStart();
        if(isFocused() && cursor>=0 && cursor==getSelectionEnd()){
            List<String> options=EditorAssist.suggestions(source,cursor,cpp);
            for(String option:options){
                Button button=new Button(getContext(),null,android.R.attr.borderlessButtonStyle);
                button.setAllCaps(false);
                button.setText(option);
                button.setTextSize(13);
                button.setTextColor(ink);
                button.setContentDescription("Insert "+option);
                button.setTypeface(Typeface.MONOSPACE);
                button.setMinWidth(dp(48));
                button.setOnClickListener(v->insertCompletion(option));
                words.addView(button,new LinearLayout.LayoutParams(-2,dp(48)));
            }
        }
        boolean hasWords=words.getChildCount()>0;
        suggestions.setVisibility(hasWords?VISIBLE:GONE);
        EditorAssist.Hint hint=EditorAssist.delimiterHint(source);
        advisory.setVisibility(hint==null?GONE:VISIBLE);
        if(hint!=null){
            int line=1;
            for(int i=0;i<hint.offset;i++)if(source.charAt(i)=='\n')line++;
            String message="Line "+line+" · "+hint.message;
            if(!message.contentEquals(advisory.getText()))advisory.setText(message);
            advisory.setOnClickListener(v->{
                requestFocus();
                if(getText()!=null && hint.offset<getText().length())setSelection(hint.offset,hint.offset+1);
            });
        }
        assistance.setVisibility(hasWords || hint!=null?VISIBLE:GONE);
    }

    private void insertCompletion(String word){
        Editable source=getText();
        int cursor=getSelectionStart();
        if(source==null || cursor<0 || cursor!=getSelectionEnd())return;
        int start=EditorAssist.prefixStart(source,cursor);
        String prefix=source.subSequence(start,cursor).toString();
        if(prefix.isEmpty() || !word.startsWith(prefix))return;
        int end=cursor;
        while(end<source.length() && Character.isJavaIdentifierPart(source.charAt(end)))end++;
        if(word.endsWith(".h") && end+2<=source.length() && ".h".contentEquals(source.subSequence(end,end+2)))end+=2;
        source.replace(start,end,word);
        setSelection(Math.min(start+word.length(),source.length()));
        requestFocus();
    }

    public void insertKeyText(String text){
        requestFocus();
        Editable source=getText();
        if(source==null)return;
        int anchor=getSelectionStart(),caret=getSelectionEnd();
        int start=Math.max(0,Math.min(anchor,caret));
        int end=Math.max(start,Math.max(anchor,caret));
        beginBatchEdit();
        try {
            android.view.inputmethod.BaseInputConnection.removeComposingSpans(source);
            Selection.setSelection(source,end);
            source.replace(start,end,text);
        }finally{
            endBatchEdit();
        }
    }

    public void moveCaret(int keyCode){
        requestFocus();
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,keyCode));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,keyCode));
    }

    @Override public boolean onKeyDown(int code,KeyEvent event){
        if(code==KeyEvent.KEYCODE_TAB){
            insertKeyText("    ");
            return true;
        }
        return super.onKeyDown(code,event);
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);Layout layout=getLayout();if(layout==null)return;
        float scroll=getScrollX();
        canvas.save();
        canvas.clipRect(scroll,0,scroll+gutterWidth,getHeight());
        canvas.drawRect(scroll,0,scroll+gutterWidth,getHeight(),gutterBackground);
        canvas.restore();
        int first=layout.getLineForVertical(getScrollY()),last=layout.getLineForVertical(getScrollY()+getHeight());
        for(int line=first;line<=last;line++)canvas.drawText(Integer.toString(line+1),scroll+gutterWidth-8*getResources().getDisplayMetrics().density,layout.getLineBaseline(line)+getTotalPaddingTop(),gutter);
        int cursor=getSelectionStart();
        if(cursor>=0&&cursor<=layout.getText().length()){
            int line=layout.getLineForOffset(cursor);
            float top=layout.getLineTop(line)+getTotalPaddingTop();
            float bottom=layout.getLineBottom(line)+getTotalPaddingTop();
            canvas.drawRect(scroll+gutterWidth,top,scroll+getWidth(),bottom,currentLine);
        }
    }
}
