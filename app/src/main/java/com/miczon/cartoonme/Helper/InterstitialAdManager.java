package com.miczon.cartoonme.Helper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.miczon.cartoonme.Listeners.AdCallBackListener;
import com.miczon.cartoonme.Manager.ConnectionManager;
import com.miczon.cartoonme.Utils.Constants;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class InterstitialAdManager {
    private static final String TAG = "InterstitialAdManager";
    private static InterstitialAdManager mInstance;
    private InterstitialAd mInterstitialAd;
    private final AdRequest adRequest = new AdRequest.Builder().build();
    private final Activity mActivity;
    private final AdCallBackListener mAdCallback;

    /**
     * Constructor of class
     * @param activity: from where it is called
     * @param adCallBackListener: to handle click events
     */
    public InterstitialAdManager(Activity activity, AdCallBackListener adCallBackListener) {
        mActivity = activity;
        mAdCallback = adCallBackListener;
    }

    /**
     * Method to enable singleton instance
     * @param activity: from where it is called
     * @param adCallBackListener: to handle click events
     * @return: object of class
     */
    public static InterstitialAdManager getInstance(Activity activity, AdCallBackListener adCallBackListener) {
        if (mInstance == null) {
            mInstance = new InterstitialAdManager(activity, adCallBackListener);
        }
        return mInstance;
    }

    /**
     * Load and display interstitial ad
     * @param adUnitId: ad id
     * @param from: from where it is called
     */
    public void loadAndDisplayInterstitialAd(String adUnitId, String from) {
        if (ConnectionManager.getInstance().isNetworkAvailable(mActivity)) {
//            if (!Utility.getInstance().isPremiumActive(mActivity)) {
                InterstitialAd.load(mActivity, adUnitId, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                mInterstitialAd = interstitialAd;
                                Log.e(TAG, "onAdLoaded: " + mInterstitialAd);
                                showInterstitialAd(from);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.e(TAG, "Load failed: " + loadAdError);
                                mInterstitialAd = null;
                                startHomeActivity(from);
                            }
                        });
//            } else {
//                Log.e(TAG, "loadAndDisplayInterstitialAd: premium is active");
//                startHomeActivity(from);
//            }
        } else {
            if (!from.equals("resume")) {
                Log.e(TAG, "loadAndDisplayInterstitialAd: no internet access");
                startHomeActivity(from);
            }
        }
    }

    /**
     * Show interstitial ad
     * @param from: from where it is called
     */
    private void showInterstitialAd(String from) {
        Log.e(TAG, "showInterstitialAd: " + mInterstitialAd);
        if (mInterstitialAd != null && !from.equals("resume")) {
            mInterstitialAd.show(mActivity);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                @Override
                public void onAdShowedFullScreenContent() {
                    Constants.isInterstitialVisible = true;
                    mAdCallback.onAdShowedFullScreenContent();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Constants.isInterstitialVisible = false;
                    Log.e(TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                    startHomeActivity(from);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.e(TAG, "Ad failed to show fullscreen content.");
                    mAdCallback.onAdFailedToShowFullScreenContent();
                    mInterstitialAd = null;
                    startHomeActivity(from);
                }
            });
        } else {
            Log.e(TAG, "The interstitial ad wasn't ready yet.");
            if (!from.equalsIgnoreCase("resume")) {
                startHomeActivity(from);
            }
        }
    }

    /**
     * Call attachment to open home activity
     * @param from: from where it is called
     */
    private void startHomeActivity(String from) {
        mAdCallback.onAdDismissedOrFinished(from);
    }
}
