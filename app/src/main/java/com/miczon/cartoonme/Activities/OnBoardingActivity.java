package com.miczon.cartoonme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Manager.ConnectionManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.ViewPagerAdapter.OnBoardingAdapter;

public class OnBoardingActivity extends AppCompatActivity {

    String TAG = "OnBoardingActivity";

    ViewPager mSlideViewPager;
    LinearLayout mDotLayout;
    TextView tvSkip;
    RelativeLayout nextLayout;

    TextView[] dots;
    OnBoardingAdapter onBoardingAdapter;

    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

    boolean isPremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        tvSkip = findViewById(R.id.tv_skip);
        mDotLayout = findViewById(R.id.ll_indicator);
        nextLayout = findViewById(R.id.rl_continue);
        mSlideViewPager = findViewById(R.id.slider);

        adRequest = new AdRequest.Builder().build();
        onBoardingAdapter = new OnBoardingAdapter(this);
        mSlideViewPager.setAdapter(onBoardingAdapter);
        setUpIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        nextLayout.setOnClickListener(v -> loadInterstitialAd("next"));

        tvSkip.setOnClickListener(v -> loadInterstitialAd("skip"));
    }

    /**
     * Method to load and display interstitial ad
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
        if (ConnectionManager.getInstance().isNetworkAvailable(OnBoardingActivity.this)) {
            if (!isPremium) {
                InterstitialAd.load(OnBoardingActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                mInterstitialAd = interstitialAd;
                                Log.e(TAG, "onAdLoaded");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.e(TAG, loadAdError.toString());
                                mInterstitialAd = null;
                            }
                        });
                Log.e(TAG, "loadInterstitialAd: interstitial val: " + mInterstitialAd);
                if (mInterstitialAd != null && !from.equalsIgnoreCase("resume")) {
                    mInterstitialAd.show(OnBoardingActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Constants.isInterstitialVisible = true;

                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Constants.isInterstitialVisible = false;
                            Log.e(TAG, "Ad dismissed fullscreen content.");
                            mInterstitialAd = null;
                            startHomeActivity();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            startHomeActivity();
                        }
                    });
                } else {
                    Log.e(TAG, "The interstitial ad wasn't ready yet.");
                    if (from.equalsIgnoreCase("next") || from.equalsIgnoreCase("skip")) {
                        startHomeActivity();
                    }
                }
            } else {
                Log.e(TAG, "loadInterstitialAd: not premium");
                if (from.equalsIgnoreCase("next") || from.equalsIgnoreCase("skip")) {
                    startHomeActivity();
                }
            }
        } else if (!from.equalsIgnoreCase("resume")) {
            Log.e(TAG, "loadInterstitialAd: no internet access");
            startHomeActivity();
        }
    }

    public void startHomeActivity() {
        startActivity(new Intent(OnBoardingActivity.this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void showExitDialog() {
        DialogHandler.getInstance().exitDialog(OnBoardingActivity.this, true, (status, message, data, alertDialog) -> {
            if (status != null) {
                switch (status) {
                    case "0":
                    case "2":
                        alertDialog.dismiss();
                        break;
                    case "1":
                        alertDialog.dismiss();
                        finishAffinity();
                        break;
                }
            }
        });
    }

    public void setUpIndicator(int position) {
        dots = new TextView[3];
        mDotLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(getResources().getColor(R.color.onBoardColor));
            mDotLayout.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.newpurple));
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.e(TAG, "onPageScrolled: working");
            onBoardingAdapter.startAnimation(position);
        }

        @Override
        public void onPageSelected(int position) {
            setUpIndicator(position);

            if (position > 1) {
                nextLayout.setVisibility(View.VISIBLE);
                tvSkip.setVisibility(View.INVISIBLE);

            } else {
                nextLayout.setVisibility(View.INVISIBLE);
                tvSkip.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loadInterstitialAd("resume");
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}