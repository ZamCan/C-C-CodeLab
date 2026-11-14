package com.zamcan.codelab;
public final class EditorRules {
    private EditorRules(){}
    public static String indentAfter(String text,int newline){
        if(newline<0||newline>=text.length()||text.charAt(newline)!='\n')return "";
        int start=text.lastIndexOf('\n',newline-1)+1,end=start;
        while(end<newline&&(text.charAt(end)==' '||text.charAt(end)=='\t'))end++;
        String prefix=text.substring(start,end).replace("\t","    ");
        String line=text.substring(start,newline).trim();
        return prefix+(line.endsWith("{")?"    ":"");
    }
}
