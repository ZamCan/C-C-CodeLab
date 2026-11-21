package com.zamcan.codelab;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import androidx.appcompat.widget.AppCompatEditText;

/** Editable source text with a fixed line gutter, indentation and brace-aware entry. */
public final class CodeEditor extends AppCompatEditText {
    private final Paint gutter=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gutterBackground=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint currentLine=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean changing, dedenting;
    private int newline=-1;
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
        addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence text,int start,int count,int after){
                dedenting=!changing&&count==1&&after==0&&start<text.length()&&text.charAt(start)=='}';
            }
            public void onTextChanged(CharSequence text,int start,int before,int count){
                newline=!changing&&before==0&&count==1&&start<text.length()&&text.charAt(start)=='\n'?start:-1;
            }
            public void afterTextChanged(Editable text){
                if(changing)return;
                if(dedenting){
                    dedenting=false;
                    int cursor=getSelectionStart();
                    if(cursor>0&&cursor<=text.length()){
                        int lineStart=text.toString().lastIndexOf('\n',cursor-1)+1;
                        int spaces=0;while(lineStart+spaces<cursor&&text.charAt(lineStart+spaces)==' ')spaces++;
                        if(spaces>=4&&lineStart+spaces==cursor-1){
                            changing=true;text.delete(cursor-5,cursor-1);setSelection(cursor-4);changing=false;
                        }
                    }
                }
                if(newline<0)return;
                int end=newline;newline=-1;
                String indent=EditorRules.indentAfter(text.toString(),end);
                if(!indent.isEmpty()){changing=true;text.insert(end+1,indent);changing=false;}
            }
        });
    }

    @Override public boolean onKeyDown(int code,KeyEvent event){
        if(code==KeyEvent.KEYCODE_TAB){int start=Math.max(0,getSelectionStart()),end=Math.max(start,getSelectionEnd());getText().replace(start,end,"    ");return true;}
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
