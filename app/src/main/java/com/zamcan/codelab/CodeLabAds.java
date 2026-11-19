package com.zamcan.codelab;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** Test-only banners. Consent, user opt-in and screen eligibility all fail closed. */
public final class CodeLabAds {
    private static final String TEST_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private final Activity activity;
    private final SharedPreferences preferences;
    private AdView banner;
    private FrameLayout container;
    private boolean consentChecked;
    private boolean initialized;
    private boolean destroyed;
    private boolean requestStarted;
    private int generation;
    private boolean debugBuild() {
        return (activity.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public CodeLabAds(Activity activity, SharedPreferences preferences) {
        this.activity=activity;
        this.preferences=preferences;
    }
    public void attach(LinearLayout parent, String screen) {
        detach();
        if (destroyed || !debugBuild() || !"home".equals(screen) || !preferences.getBoolean("test_ads_opt_in",false)) return;
        container=new FrameLayout(activity);
        parent.addView(container,new LinearLayout.LayoutParams(-1,-2));
        refreshConsent();
    }
    public void refreshConsent() {
        if (destroyed || container==null || consentChecked) return;
        consentChecked=true;
        final int ticket=generation;
        ConsentInformation info=UserMessagingPlatform.getConsentInformation(activity);
        ConsentRequestParameters parameters=new ConsentRequestParameters.Builder().build();
        info.requestConsentInfoUpdate(activity,parameters,()->{
            if(destroyed || ticket!=generation || container==null)return;
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,error->{
                if(destroyed || ticket!=generation || container==null)return;
                if(info.canRequestAds()) startBanner();
            });
        },error->{
            // No ad request without a confirmed current or previously valid consent state.
            if(!destroyed && ticket==generation && container!=null && info.canRequestAds())startBanner();
        });
    }
    private void startBanner() {
        if(requestStarted || destroyed || !debugBuild() || container==null || !preferences.getBoolean("test_ads_opt_in",false))return;
        requestStarted=true;
        if(!initialized) {
            initialized=true;
            final int ticket=generation;
            MobileAds.initialize(activity,status->activity.runOnUiThread(()->{if(ticket==generation)loadBanner();}));
        } else loadBanner();
    }
    private void loadBanner() {
        if(destroyed || !debugBuild() || container==null || !preferences.getBoolean("test_ads_opt_in",false))return;
        int width=(int)(activity.getResources().getDisplayMetrics().widthPixels/activity.getResources().getDisplayMetrics().density);
        if(width<=0)return;
        final int ticket=generation;
        final AdView view=new AdView(activity);
        banner=view;
        view.setAdUnitId(TEST_BANNER);
        view.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity,width));
        view.setVisibility(View.GONE);
        view.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
                if(!destroyed && ticket==generation && banner==view) view.setVisibility(View.VISIBLE);
            }
            @Override public void onAdFailedToLoad(LoadAdError error) {
                if(ticket==generation && banner==view) view.setVisibility(View.GONE);
            }
        });
        container.addView(view);
        view.loadAd(new AdRequest.Builder().build());
    }
    public void detach() {
        generation++;
        requestStarted=false;
        consentChecked=false;
        if(banner!=null){banner.destroy();banner=null;}
        if(container!=null){container.removeAllViews();if(container.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)container.getParent()).removeView(container);container=null;}
    }
    public void destroy(){destroyed=true;detach();}
}
