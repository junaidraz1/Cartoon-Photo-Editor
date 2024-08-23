package com.miczon.cartoonme.Helper;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.miczon.cartoonme.Activities.SplashActivity;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Utils.Constants;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class BannerAdManager {

    public String TAG = "BannerAdManager";

    private final Activity mActivity;
    private final BannerAdLoadListener mAdLoadListener;
    private final AdRequest adRequest = new AdRequest.Builder().build();

    /**
     * Constructor for Banner Ad Manager class
     *
     * @param activity:       from where it is called
     * @param adLoadListener: listener to handle call backs
     */
    public BannerAdManager(Activity activity, BannerAdLoadListener adLoadListener) {
        mActivity = activity;
        mAdLoadListener = adLoadListener;
    }

    public void loadBannerAd(FrameLayout adContainer) {
        AdView adView = new AdView(mActivity);
        Log.e(TAG, "loadBannerAd: if working");
        if (mActivity instanceof SplashActivity) {
            adView.setAdUnitId(Constants.AdMob_Splash_Adaptive_Banner_Ad_Id);
            Log.e(TAG, "loadBannerAd: id in if: " + Constants.AdMob_Splash_Adaptive_Banner_Ad_Id);
        } else {
            adView.setAdUnitId(Constants.AdMob_Main_Adaptive_Banner_Ad_Id);
            Log.e(TAG, "loadBannerAd: id in else: " + Constants.AdMob_Main_Adaptive_Banner_Ad_Id);
        }
        adView.setAdSize(getAdSize(mActivity));
        adContainer.addView(adView);
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                mAdLoadListener.onAdClicked();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdLoadListener.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mAdLoadListener.onAdFailedToLoad(loadAdError);
            }
        });
    }

    public static AdSize getAdSize(Activity mActivity) {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);
    }
}
