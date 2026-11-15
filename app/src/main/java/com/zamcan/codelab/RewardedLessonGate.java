package com.zamcan.codelab;

import android.app.Activity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/** One-shot rewarded ad gate. Test ad unit; replace before release. */
public final class RewardedLessonGate {
    public interface Callback {
        void earned();
        void unavailable(String reason);
    }
    // Official Google rewarded test ad unit; NEVER use for production revenue.
    private static final String TEST_UNIT="ca-app-pub-3940256099942544/5224354917";
    private boolean pending;
    public RewardedLessonGate(Activity activity) {
        MobileAds.initialize(activity, status -> {});
    }
    public void show(Activity activity, Callback callback) {
        if (pending) { callback.unavailable("An advertisement is already loading."); return; }
        pending=true;
        RewardedAd.load(activity, TEST_UNIT, new AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    pending=false;
                    callback.unavailable(error.getMessage());
                }
                @Override public void onAdLoaded(RewardedAd ad) {
                    final boolean[] earned={false};
                    ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                            pending=false;
                            callback.unavailable(error.getMessage());
                        }
                        @Override public void onAdDismissedFullScreenContent() {
                            pending=false;
                            if (!earned[0]) callback.unavailable("Advertisement closed before reward.");
                        }
                    });
                    ad.show(activity, reward -> {
                        if (earned[0]) return;
                        earned[0]=true;
                        pending=false;
                        callback.earned();
                    });
                }
            });
    }
}
