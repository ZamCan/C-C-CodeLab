package com.zamcan.codelab;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

final class AppAppearance {
    static final String[] MODES={"Dark","Light","System"};
    static final String[] COLORS={"Violet","Rose","Blue","Green","Amber"};
    private static final int[] DAY={0xff694594,0xffa52758,0xff2358aa,0xff166944,0xff805700};
    private static final int[] NIGHT={0xffc7afff,0xffffa9c6,0xffa5c7ff,0xff8ce2ba,0xffffcc70};
    private AppAppearance() {}

    static String mode(SharedPreferences prefs){
        return prefs.getString("appearance_mode",prefs.getBoolean("light_theme",false)?"Light":"Dark");
    }

    static boolean isLight(Context context){
        SharedPreferences prefs=context.getSharedPreferences("academy",Context.MODE_PRIVATE);
        String mode=mode(prefs);
        if("System".equals(mode)){
            int flags=context.getResources().getConfiguration().uiMode;
            return (flags & Configuration.UI_MODE_NIGHT_MASK)!=Configuration.UI_MODE_NIGHT_YES;
        }
        return "Light".equals(mode);
    }

    static int choice(SharedPreferences prefs){
        int selected=prefs.getInt("appearance_accent",0);
        return selected>=0 && selected<COLORS.length?selected:0;
    }

    static int color(int choice,boolean light){
        return (light?DAY:NIGHT)[choice];
    }
}
