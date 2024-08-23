package com.miczon.cartoonme.Helper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.miczon.cartoonme.Activities.SplashActivity;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

import java.util.Date;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class AppOpenAdHelper extends Application
        implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity = null;
    private static final String TAG = "AppOpenAdHelper";
    private View loadingLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();

        setupLoadingLayout();
    }

    private void setupLoadingLayout() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            loadingLayout = inflater.inflate(R.layout.layout_loading_ad_fs, null);
        }
    }

    private void showLoadingLayout(boolean show) {
        if (loadingLayout != null && currentActivity != null) {
            ViewGroup rootView = currentActivity.findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.removeView(loadingLayout);
                if (show) {
                    rootView.addView(loadingLayout);
                }
            }
        } else {
            Log.e(TAG, "showLoadingLayout: else working: " + currentActivity);
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);

        Log.e(TAG, "onStart: current activity: " + currentActivity);
        Log.e(TAG, "onStart: isInterstitialVisible: " + Constants.isInterstitialVisible);
        Log.e(TAG, "onStart: isSelectingFile: " + Constants.isSelectingFile);

        if (!(currentActivity instanceof SplashActivity) && !Constants.isInterstitialVisible &&
                !Constants.isSelectingFile && !Utility.getInstance().isPremiumActive(currentActivity)) {
            showLoadingLayout(true);
            appOpenAdManager.showAdIfAvailable(currentActivity, () -> showLoadingLayout(false));

        } else {
            showLoadingLayout(false);
            Log.e(TAG, "onStart: The ad won't be shown.");
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    private static class AppOpenAdManager {

        private static final String LOG_TAG = "AppOpenAdManager";

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        private long loadTime = 0;

        public AppOpenAdManager() {
        }

        private void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(context, Constants.App_Open_Ad_Id, request, new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    appOpenAd = ad;
                    isLoadingAd = false;
                    loadTime = (new Date()).getTime();

                    Log.e(LOG_TAG, "onAdLoaded.");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    isLoadingAd = false;
                    Log.e(LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                }
            });
        }

        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        private boolean isAdAvailable() {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        private void showAdIfAvailable(@NonNull final Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isShowingAd) {
                Log.e(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            if (!isAdAvailable()) {
                Log.e(LOG_TAG, "The app open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            Log.e(LOG_TAG, "Will show ad.");

            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    isShowingAd = false;

                    Log.e(LOG_TAG, "onAdDismissedFullScreenContent.");

                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    appOpenAd = null;
                    isShowingAd = false;

                    Log.e(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
//                                    .show();

                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.e(LOG_TAG, "onAdShowedFullScreenContent.");
                }
            });

            Log.e(TAG, "showAdIfAvailable: current activity: " + activity);
            Log.e(TAG, "showAdIfAvailable: isInterstitialVisible: " + Constants.isInterstitialVisible);
            Log.e(TAG, "showAdIfAvailable: isSelectingFile: " + Constants.isSelectingFile);

            isShowingAd = true;
            Log.e(TAG, "showAdIfAvailable if working: activity: " + activity);
            appOpenAd.show(activity);
        }
    }
}
