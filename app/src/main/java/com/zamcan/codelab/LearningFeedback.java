package com.zamcan.codelab;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

/** Restrained platform feedback; never bypasses the device sound/motion preferences. */
public final class LearningFeedback {
    private LearningFeedback() {}

    public static void configureClicks(View view, boolean enabled) {
        view.setSoundEffectsEnabled(enabled);
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)configureClicks(group.getChildAt(i),enabled);
        }
    }

    public static void success(Context context, View feedback, boolean sound, boolean motion) {
        if(motion && ValueAnimator.areAnimatorsEnabled()) {
            feedback.setAlpha(.35f);feedback.setScaleX(.98f);feedback.setScaleY(.98f);
            feedback.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260).start();
        }
        if(!sound)return;
        AudioManager audio=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(audio==null || audio.getRingerMode()!=AudioManager.RINGER_MODE_NORMAL
                || audio.getStreamVolume(AudioManager.STREAM_SYSTEM)==0)return;
        try {
            ToneGenerator tone=new ToneGenerator(AudioManager.STREAM_SYSTEM,35);
            tone.startTone(ToneGenerator.TONE_PROP_ACK,130);
            new Handler(Looper.getMainLooper()).postDelayed(tone::release,250);
        } catch(RuntimeException unavailable) {
            // Audio feedback is optional; never interrupt a completed learning action.
        }
    }
}
