package com.zamcan.codelab;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.view.KeyEvent;
import android.widget.EditText;

/** Native editable text with a line gutter and conservative four-space indentation. */
public final class CodeEditor extends EditText {
    private final Paint gutter=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean changing;
    private int newline=-1;
    public CodeEditor(Context context) {
        super(context);
        setTextDirection(TEXT_DIRECTION_LTR);
        gutter.setColor(0xff90a0bc);gutter.setTypeface(Typeface.MONOSPACE);
        addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence text,int start,int count,int after){}
            public void onTextChanged(CharSequence text,int start,int before,int count){
                newline=!changing&&before==0&&count==1&&text.charAt(start)=='\n'?start:-1;
            }
            public void afterTextChanged(Editable text){
                if(changing||newline<0)return;
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
        gutter.setTextSize(getTextSize()*.75f);
        int first=layout.getLineForVertical(getScrollY()),last=layout.getLineForVertical(getScrollY()+getHeight());
        for(int line=first;line<=last;line++)canvas.drawText(Integer.toString(line+1),getScrollX()+6*getResources().getDisplayMetrics().density,layout.getLineBaseline(line)+getTotalPaddingTop(),gutter);
    }
}
