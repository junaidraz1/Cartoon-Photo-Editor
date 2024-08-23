package com.miczon.cartoonme.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.miczon.cartoonme.BuildConfig;
import com.miczon.cartoonme.Fragment.FreeTrailFragment;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Helper.InAppBillingHelper;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Listeners.FragmentClickListener;
import com.miczon.cartoonme.Listeners.PremiumStatusChangeListener;
import com.miczon.cartoonme.Listeners.RemoteConfigListener;
import com.miczon.cartoonme.Manager.ConnectionManager;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.RecyclerViewAdapters.CountryAdapter;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity implements BannerAdLoadListener, FragmentClickListener, PremiumStatusChangeListener {
    public static String TAG = "SplashActivity", apK = "AP_K", fMail = "F_M";

    Button continueBtn;
    PrefsManager prefsManager;
    ProgressBar progressBar;
    RelativeLayout adLayout;
    TextView tvLoadingAd, tvPrivacyPolicy;
    FrameLayout fragmentContainer, adContainer;
    ImageView ivTile1, ivTile2, ivTile3;

    AdRequest adRequest;
    InterstitialAd mInterstitialAd;
    private BannerAdManager bannerAdManager;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    Handler handler;

    Intent intent;

    int selectedLanguagePosition = 0;
    public static int adCounter;
    boolean showAgreementDialog = false;
    boolean isAgreementWebOpened = false;
    boolean isFragmentVisible = false;
    boolean isPremium = false;
    boolean isFirstTime = true;

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        continueBtn = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.pb_progressBar);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        tvPrivacyPolicy = findViewById(R.id.tv_privacyPolicy);
        adLayout = findViewById(R.id.rl_ads);
        fragmentContainer = findViewById(R.id.fl_fragmentContainer);
        adContainer = findViewById(R.id.fl_adContainer);
        ivTile1 = findViewById(R.id.iv1);
        ivTile2 = findViewById(R.id.iv2);
        ivTile3 = findViewById(R.id.iv3);

        InAppBillingHelper.getInstance().setPremiumStatusChangeListener(this);

        prefsManager = new PrefsManager(SplashActivity.this);
        handler = new Handler();
        adRequest = new AdRequest.Builder().build();
        bannerAdManager = new BannerAdManager(this, this);
        bundle = new Bundle();

        prefsManager.clearAdCount();

        if (!Utility.getInstance().verifyInstallerId(this) && !BuildConfig.DEBUG) {
            progressBar.setVisibility(View.GONE);
            DialogHandler.getInstance().showPiracyCheckerDialog(this, (position, path, action) -> {
                if (position == 0) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (RuntimeException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }

                } else if (position == 1) {
                    finishAffinity();

                }
            });

        } else {

            /*
             * Check for app update
             */
            try {
                Log.d("updateCheck", "inside app update try ");
                AppUpdater appUpdater = new AppUpdater(this)
                        .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                        .setDisplay(Display.DIALOG)
                        .setTitleOnUpdateAvailable("Update available")
                        .setContentOnUpdateAvailable("Please update your app to the latest available version!")
                        .setButtonUpdate("Update")
                        .setButtonUpdateClickListener((dialog, which) -> {
                            Log.d("updateCheck", "inside app update try click ");
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            Log.e(TAG, "onClick: Package name: " + appPackageName);
                            try {
                                Log.d("updateCheck", "inside app update click try ");
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (ActivityNotFoundException anfe) {
                                Log.d("updateCheck", "inside app update click catch ");
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        })
                        .setButtonDismiss(null)
                        .setButtonDoNotShowAgain(null)
                        .setCancelable(true);
                appUpdater.start();

            } catch (Exception e) {
                Log.e(TAG, "AppUpdater Exception: " + e.toString());
            }

            isPremium = Utility.getInstance().isPremiumActive(this);

            intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "onCreate: Intent is null");

            } else {
                if (intent.getStringExtra("from") != null) {
                    Log.e(TAG, "onCreate: language: " + prefsManager.getSelectedLanguage());
                    if (intent.getStringExtra("from").equalsIgnoreCase("self") && prefsManager.getPrivacyBit()) {
                        progressBar.setVisibility(View.GONE);
                        continueBtn.setVisibility(View.VISIBLE);

                    } else {
                        Log.e(TAG, "onCreate: calling agreement dialog");
                        progressBar.setVisibility(View.GONE);
                        showAgreementDialog();
                    }
                } else {
                    Log.e(TAG, "onCreate: from is null");
                    handler.postDelayed(() -> {
                        if (prefsManager.getSelectedLanguage() == null || prefsManager.getSelectedLanguage().isEmpty()) {
                            displaySelectLanguageDialog();

                        } else if (prefsManager.getPrivacyBit()) {
                            continueBtn.setVisibility(View.VISIBLE);

                        } else if (!prefsManager.getSelectedLanguage().isEmpty()) {
                            showAgreementDialog();
                        }
                    }, Constants.SPLASH_TIMER);
                }
            }

            Log.e(TAG, "onCreate: premium: " + Utility.getInstance().isPremiumActive(this));
            if (isPremium) {
                adLayout.setVisibility(View.GONE);

            } else {
                adLayout.setVisibility(View.VISIBLE);
                adCounter = prefsManager.getAdCount();
            }

            continueBtn.setOnClickListener(v -> {
                if (!isPremium) {
                    isFragmentVisible = true;
                    loadTrailFragment("continue");
                } else {
                    startHomeActivity();
                }
            });

            tvPrivacyPolicy.setOnClickListener(v -> {
                Uri webpage = Uri.parse("https://airportflightsstatus.com/");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            });

        }
    }

    /**
     * Method to load and display interstitial ad
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
        if (ConnectionManager.getInstance().isNetworkAvailable(SplashActivity.this)) {
            Log.e(TAG, "loadInterstitialAd: id: " + Constants.AdMob_Splash_Interstitial_Ad_Id);
            if (!isPremium) {
                InterstitialAd.load(SplashActivity.this, Constants.AdMob_Splash_Interstitial_Ad_Id, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                mInterstitialAd = interstitialAd;
                                Log.e(TAG, "onAdLoaded");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.e(TAG, "interstitial failed to load: " + loadAdError);
                                mInterstitialAd = null;
                            }
                        });
                Log.e(TAG, "loadInterstitialAd: interstitial val: " + mInterstitialAd);
                if (mInterstitialAd != null && !from.equalsIgnoreCase("resume")) {
                    mInterstitialAd.show(SplashActivity.this);
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
                            Log.e(TAG, "interstitial Ad failed to show fullscreen content." + adError);
                            mInterstitialAd = null;
                            startHomeActivity();
                        }
                    });
                } else {
                    Log.e(TAG, "The interstitial ad wasn't ready yet.");
                    if (from.equalsIgnoreCase("continue")) {
                        startHomeActivity();
                    }
                }
            } else {
                Log.e(TAG, "loadInterstitialAd: not premium");
                if (from.equalsIgnoreCase("continue")) {
                    startHomeActivity();
                }
            }
        } else if (!from.equalsIgnoreCase("resume")) {
            Log.e(TAG, "loadInterstitialAd: no internet access");
            startHomeActivity();
        }
    }

    /**
     * Method to start home activity
     */
    public void startHomeActivity() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * Method to display language selection full screen dialog
     */
    public void displaySelectLanguageDialog() {
        Dialog languageDialog = new Dialog(SplashActivity.this, android.R.style.Theme_Light);
        languageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        languageDialog.setContentView(R.layout.activity_select_language);

        languageDialog.setCancelable(false);

        Button btnSelectLanguage, btnCancel;
        RecyclerView.LayoutManager layoutManager;
        RecyclerView rvCountries;
        RelativeLayout adLayout;
        FrameLayout adContainer;

        CountryAdapter countryAdapter;
        PrefsManager prefsManager;

        rvCountries = languageDialog.findViewById(R.id.rv_countries);
        btnSelectLanguage = languageDialog.findViewById(R.id.btn_accept);
        btnCancel = languageDialog.findViewById(R.id.btn_cancel);
        adContainer = languageDialog.findViewById(R.id.fl_adContainer);
        adLayout = languageDialog.findViewById(R.id.rl_adContainer);

        prefsManager = new PrefsManager(SplashActivity.this);
        layoutManager = new LinearLayoutManager(SplashActivity.this);

        if (!isPremium) {
            adLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            Log.e("BannerAdCall", "displaySelectLanguageDialog: working");
            bannerAdManager.loadBannerAd(adContainer);
        } else {
            adLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        countryAdapter = new CountryAdapter(this, Utility.getInstance().countryNames(),
                Utility.getInstance().countryFlags(), (position, path, action) -> {

            selectedLanguagePosition = position;
            Log.e(TAG, "itemClick: selected language position: " + position + " " + "name is: " + action);
        });

        rvCountries.setAdapter(countryAdapter);
        rvCountries.setLayoutManager(layoutManager);

        btnSelectLanguage.setOnClickListener(v -> {
            switch (selectedLanguagePosition) {
                case 0:
                    prefsManager.setSelectedLanguage("en");
                    prefsManager.setLangPos(0);
                    break;
                case 1:
                    prefsManager.setSelectedLanguage("af");
                    prefsManager.setLangPos(1);
                    break;
                case 2:
                    prefsManager.setSelectedLanguage("ar");
                    prefsManager.setLangPos(2);
                    break;
                case 3:
                    prefsManager.setSelectedLanguage("zh");
                    prefsManager.setLangPos(3);
                    break;
                case 4:
                    prefsManager.setSelectedLanguage("cs");
                    prefsManager.setLangPos(4);
                    break;
                case 5:
                    prefsManager.setSelectedLanguage("nl");
                    prefsManager.setLangPos(5);
                    break;
                case 6:
                    prefsManager.setSelectedLanguage("fr");
                    prefsManager.setLangPos(6);
                    break;
                case 7:
                    prefsManager.setSelectedLanguage("de");
                    prefsManager.setLangPos(7);
                    break;
                case 8:
                    prefsManager.setSelectedLanguage("el");
                    prefsManager.setLangPos(8);
                    break;
                case 9:
                    prefsManager.setSelectedLanguage("hi");
                    prefsManager.setLangPos(9);
                    break;
                case 10:
                    prefsManager.setSelectedLanguage("in");
                    prefsManager.setLangPos(10);
                    break;
                case 11:
                    prefsManager.setSelectedLanguage("it");
                    prefsManager.setLangPos(11);
                    break;
                case 12:
                    prefsManager.setSelectedLanguage("ja");
                    prefsManager.setLangPos(12);
                    break;
                case 13:
                    prefsManager.setSelectedLanguage("ko");
                    prefsManager.setLangPos(13);
                    break;
                case 14:
                    prefsManager.setSelectedLanguage("ms");
                    prefsManager.setLangPos(14);
                    break;
                case 15:
                    prefsManager.setSelectedLanguage("no");
                    prefsManager.setLangPos(15);
                    break;
                case 16:
                    prefsManager.setSelectedLanguage("fa");
                    prefsManager.setLangPos(16);
                    break;
                case 17:
                    prefsManager.setSelectedLanguage("pt");
                    prefsManager.setLangPos(17);
                    break;
                case 18:
                    prefsManager.setSelectedLanguage("ru");
                    prefsManager.setLangPos(18);
                    break;
                case 19:
                    prefsManager.setSelectedLanguage("es");
                    prefsManager.setLangPos(19);
                    break;
                case 20:
                    prefsManager.setSelectedLanguage("th");
                    prefsManager.setLangPos(20);
                    break;
                case 21:
                    prefsManager.setSelectedLanguage("tr");
                    prefsManager.setLangPos(21);
                    break;
                case 22:
                    prefsManager.setSelectedLanguage("vi");
                    prefsManager.setLangPos(22);
                    break;
                default:
                    break;
            }

            if (isPremium) {
                adLayout.setVisibility(View.GONE);
            }
            languageDialog.dismiss();

            Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "self");
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> {
            if (prefsManager.getSelectedLanguage() == null || prefsManager.getSelectedLanguage().isEmpty()) {
                prefsManager.setSelectedLanguage("en");
            }
            if (languageDialog.isShowing()) {
                Log.e(TAG, "displaySelectLanguageDialog: if working");
                languageDialog.dismiss();
            } else {
                Log.e(TAG, "displaySelectLanguageDialog: else working");
            }
            showAgreementDialog();
        });

        languageDialog.show();
    }

    /**
     * Method to display agreement dialog
     */
    private void showAgreementDialog() {
        Log.e(TAG, "showAgreementDialog: ");
        progressBar.setVisibility(View.GONE);

        if (isPremium) {
            adLayout.setVisibility(View.GONE);
        }

        DialogHandler.getInstance().showAgreementDialog(SplashActivity.this, false, (status, message, data, alertDialog) -> {
            if (status != null) {
                if (status.equals("0")) {
                    alertDialog.dismiss();
                    Uri webpage = Uri.parse("https://airportflightsstatus.com/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    startActivity(intent);
                    showAgreementDialog = true;
                    isAgreementWebOpened = true;
                } else {
                    alertDialog.dismiss();
                    prefsManager.setPrivacyBit(true);
                    if (!Utility.mInstance.isPremiumActive(SplashActivity.this)) {
                        isFragmentVisible = true;
                        loadTrailFragment("continue");

                    } else {
                        loadInterstitialAd("continue");
                    }
                }
            }
        });
    }

    /**
     * Method to display In App Purchase fragment
     */
    public void loadTrailFragment(String from) {
        Log.e(TAG, "loadTrailFragment: working");
        /*  if (from.equalsIgnoreCase("resume") && isFirstTime) {*/
        FragmentManager manager = getSupportFragmentManager();
        String fragmentTag = "TrailFrag";
        Fragment existingFragment = new FreeTrailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("from", "splash");
        existingFragment.setArguments(bundle);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_fragmentContainer, existingFragment, fragmentTag).commit();
//            fragmentContainer.setVisibility(View.GONE);
//            isFirstTime = false;
//        } else {
        fragmentContainer.setVisibility(View.VISIBLE);
//        }
    }

    public void addAnimation() {
        TranslateAnimation topToBottom = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -400);

        topToBottom.setDuration(10000);
        topToBottom.setFillAfter(true);
        topToBottom.setRepeatCount(Animation.INFINITE);
        topToBottom.setRepeatMode(Animation.REVERSE);
        topToBottom.setInterpolator(new LinearInterpolator());

        TranslateAnimation bottomToTop = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 400);

        bottomToTop.setDuration(10000);
        bottomToTop.setFillAfter(true);
        bottomToTop.setInterpolator(new LinearInterpolator());
        bottomToTop.setRepeatCount(Animation.INFINITE);
        bottomToTop.setRepeatMode(Animation.REVERSE);

        runOnUiThread(() -> {
            ivTile1.startAnimation(topToBottom);
            ivTile3.startAnimation(topToBottom);
            ivTile2.startAnimation(bottomToTop);
        });

    }

    /**
     * onPause
     */
    @Override
    protected void onPause() {
        /*if (adView != null) {
            adView.pause();
        }*/

        loadInterstitialAd("resume");

        Log.e(TAG, "onPause:");
        super.onPause();
    }

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        new Thread(this::addAnimation).start();

        isPremium = Utility.getInstance().isPremiumActive(this);

        Log.e(TAG, "onResume: prem: " + isPremium);

        Utility.getInstance().getRemoteConfigs(this, new RemoteConfigListener() {
            @Override
            public void onRemoteConfigFetched() {
                if (!isPremium) {
                    loadInterstitialAd("resume");
                    bannerAdManager.loadBannerAd(adContainer);
                }
            }

            @Override
            public void onRemoteConfigFetchFailed() {

            }
        });

        Log.e(TAG, "onResume: called: ");

        if (isAgreementWebOpened && !prefsManager.getPrivacyBit()) {
            showAgreementDialog();
        }

        /*if (!isPremium) {
            loadTrailFragment("resume");
        }*/

    }

    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
        /*if (adView != null) {
            adView.destroy();
        }*/
        super.onDestroy();
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed: frag val: " + isFragmentVisible);
        if (isFragmentVisible) {
            Log.e(TAG, "onBackPressed: inside if");
            if (prefsManager.getBoardingBit()) {
                fragmentContainer.setVisibility(View.GONE);
//                loadInterstitialAd("continue");
            } else {
                startActivity(new Intent(SplashActivity.this, OnBoardingActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                prefsManager.setBoardingBit(true);
            }
            isFragmentVisible = false;

        } else {
            Log.e(TAG, "onBackPressed: inside else ");
            finishAffinity();
        }
    }

    /**
     * Interface method implementation of Banner Ad helper class
     */
    @Override
    public void onAdClicked() {
        Log.e(TAG, "onAdClicked: ");

    }

    /**
     * Interface method implementation of Banner Ad helper class
     */
    @Override
    public void onAdLoaded() {
        Log.e(TAG, "onAdLoaded: ");
        tvLoadingAd.setVisibility(View.GONE);
    }

    /**
     * Interface method implementation of Banner Ad helper class
     */
    @Override
    public void onAdFailedToLoad(LoadAdError loadAdError) {
        Log.e(TAG, "onAdFailedToLoad: " + loadAdError);

    }

    /**
     * Interface click listener implementation of IAP Fragment
     */
    @Override
    public void itemClicked() {
        Log.e(TAG, "itemClicked: boarding bit: " + prefsManager.getBoardingBit());
        if (!prefsManager.getBoardingBit()) {
            startActivity(new Intent(SplashActivity.this, OnBoardingActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            prefsManager.setBoardingBit(true);
        } else {
            loadInterstitialAd("continue");
        }
    }

    @Override
    public void onPremiumStatusChanged(boolean isPremium) {
        if (isPremium) {
            adLayout.setVisibility(View.GONE);

        } else {
            Log.e("BannerAdCall", "onPremiumStatusChanged: working");
            bannerAdManager.loadBannerAd(adContainer);
            adLayout.setVisibility(View.VISIBLE);
        }
    }
}
