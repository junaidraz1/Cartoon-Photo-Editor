package com.miczon.cartoonme.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jaredrummler.android.device.DeviceName;
import com.miczon.cartoonme.BuildConfig;
import com.miczon.cartoonme.Fragment.ComicFiltersFragment;
import com.miczon.cartoonme.Fragment.DreamyFiltersFragment;
import com.miczon.cartoonme.Fragment.FreeTrailFragment;
import com.miczon.cartoonme.Fragment.ProfileFiltersFragment;
import com.miczon.cartoonme.Fragment.TrendingFiltersFragment;
import com.miczon.cartoonme.Fragment.VintageFiltersFragment;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Helper.InAppBillingHelper;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Listeners.FragmentClickListener;
import com.miczon.cartoonme.Listeners.PremiumStatusChangeListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.ShowNativeAd;
import com.miczon.cartoonme.Utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class HomeActivity extends BaseActivity implements BannerAdLoadListener, FragmentClickListener, PremiumStatusChangeListener {

    public String TAG = "HomeActivity", model = "", manufacturer = "", osVersion = "", apiLevel = "";

    LinearLayout cameraNdGalleryLayout, allowAccessLayout, navLanguageLayout,
            navSavedFilesLayout, navShareLayout, navRateLayout, navPrivacyLayout, navFeedbackLayout, navMoreAppLayout, navExitLayout,
            filterTilesLayout, trendingFiltersLayout, dreamyFiltersLayout, profileFiltersLayout, comicStylesLayout, vintageStylesLayout,
            navTrendingFiltersLayout, navDreamyFiltersLayout, navProfileFiltersLayout, navComicStylesLayout, navVintageStylesLayout;
    RelativeLayout navDrawerLayout, buyPremiumLayout, navDrawer, adDisplayLayout, savedFilesLayout;
    Button storageAccessBtn;
    DrawerLayout drawerLayout;
    FrameLayout adContainer, fragmentContainer, nativeAdContainer;
    TextView tvAppVersion;
    ImageView navBackLayout;
    LottieAnimationView animationView;

    ArrayList<String> imagePathList;
    ArrayList<String> filterIdsList;

    Uri capturedImageUri;

    TextView tvLoadingAd;

    AdRequest adRequest;
    InterstitialAd mInterstitialAd;
    //    AdView adView;
    NativeAdView nativeAdView;

    BannerAdManager bannerAdManager;

    PrefsManager prefsManager;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    Bundle bundle;

    boolean isFromSettings = false;
    boolean isPremium;
    boolean isStarted = false;
    public static boolean isFragmentVisible = false;

    File camPhotoFile = null;

    public static Fragment existingFragment;

    /**
     * flag to handle first time permission dialog display
     */
    boolean isFirstTime = true;

    Intent globalIntent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cameraNdGalleryLayout = findViewById(R.id.rl_cameraNdGallery);
        allowAccessLayout = findViewById(R.id.ll_accessDenyHint);
        storageAccessBtn = findViewById(R.id.btn_allowAccess);
        buyPremiumLayout = findViewById(R.id.rl_premium);
        navDrawerLayout = findViewById(R.id.rl_drawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        navDrawer = findViewById(R.id.nav_drawer);
        navLanguageLayout = findViewById(R.id.ll_navLanguage);
        navSavedFilesLayout = findViewById(R.id.ll_navSavedFiles);
        navShareLayout = findViewById(R.id.ll_navShare);
        navRateLayout = findViewById(R.id.ll_navRateUs);
        navPrivacyLayout = findViewById(R.id.ll_navPrivacyPolicy);
        navFeedbackLayout = findViewById(R.id.ll_navFeedback);
        navMoreAppLayout = findViewById(R.id.ll_navMoreApps);
        navExitLayout = findViewById(R.id.ll_navExit);
        navBackLayout = findViewById(R.id.iv_back);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        tvAppVersion = findViewById(R.id.tV_appVersion);
        adContainer = findViewById(R.id.fl_adContainer);
        adDisplayLayout = findViewById(R.id.rl_adLayout);
        fragmentContainer = findViewById(R.id.fl_fragmentContainer);
        filterTilesLayout = findViewById(R.id.ll_filterTiles);
        trendingFiltersLayout = findViewById(R.id.ll_trendingFilter);
        dreamyFiltersLayout = findViewById(R.id.ll_dreamyStyles);
        profileFiltersLayout = findViewById(R.id.ll_profileStyles);
        comicStylesLayout = findViewById(R.id.ll_comicStyles);
        vintageStylesLayout = findViewById(R.id.ll_vintageStyles);
        animationView = findViewById(R.id.animation_view);
        navTrendingFiltersLayout = findViewById(R.id.ll_navTrendingFilter);
        navDreamyFiltersLayout = findViewById(R.id.ll_navDreamyFilters);
        navProfileFiltersLayout = findViewById(R.id.ll_navProfileFilters);
        navComicStylesLayout = findViewById(R.id.ll_navComicFilters);
        navVintageStylesLayout = findViewById(R.id.ll_navVintageFilters);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        nativeAdView = findViewById(R.id.ad_view);
        savedFilesLayout = findViewById(R.id.rl_savedFiles);

        imagePathList = new ArrayList<>();
        filterIdsList = new ArrayList<>();

        InAppBillingHelper.getInstance().setPremiumStatusChangeListener(this);

        try {
            checkForAppUpdate();
        } catch (Exception e) {
            Log.e("EXP 2", e.toString());
        }

        adRequest = new AdRequest.Builder().build();
        prefsManager = new PrefsManager(this);
        bannerAdManager = new BannerAdManager(this, this);
        bundle = new Bundle();
        isPremium = Utility.getInstance().isPremiumActive(this);

        if (!isPremium) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);
            nativeAdContainer.setVisibility(View.VISIBLE);
        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
            nativeAdContainer.setVisibility(View.GONE);
        }

        Log.e(TAG, "onCreate: is premium: " + isPremium);

        ShowNativeAd nativeAdHelper = new ShowNativeAd(this);
        nativeAdHelper.showAdMobNativeBannerAd(nativeAdView, nativeAdContainer, Constants.AdMob_Main_Native_Advance_Ad_Id);

        initializeFCM();

        if (prefsManager.getSelectedLanguage() != null) {
            if (prefsManager.getSelectedLanguage().equalsIgnoreCase("ar") ||
                    prefsManager.getSelectedLanguage().equalsIgnoreCase("fa") ||
                    prefsManager.getSelectedLanguage().equalsIgnoreCase("iw")) {
                navExitLayout.setBackgroundResource(R.drawable.nav_exit_rtl_bg);

            } else {
                navExitLayout.setBackgroundResource(R.drawable.nav_exit_btn_bg);
            }
        }

        tvAppVersion.setText(getString(R.string.app_ver_label) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        globalIntent = getIntent();

        if (globalIntent != null) {

            Log.e(TAG, "onCreate: via: " + globalIntent.getStringExtra("via"));
            Log.e(TAG, "onCreate: from: " + globalIntent.getStringExtra("from"));

            if (globalIntent.getStringExtra("via") != null && globalIntent.getStringExtra("from") != null) {

                if (!globalIntent.getStringExtra("from").equalsIgnoreCase("exit") &&
                        !globalIntent.getStringExtra("from").equalsIgnoreCase("save") &&
                        !globalIntent.getStringExtra("from").equalsIgnoreCase("trail")) {
                    Log.e(TAG, "onCreate: via: " + globalIntent.getStringExtra("via"));
                    filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                    Log.e(TAG, "onCreate: filter id size: " + filterIdsList.size());

                    String cameVia = globalIntent.getStringExtra("via");

                    if (cameVia.equalsIgnoreCase("trendingFraghome")) {
                        loadFragment(new TrendingFiltersFragment(), "intentGallery");

                    } else if (cameVia.equalsIgnoreCase("trendingFragcamera")) {
                        loadFragment(new TrendingFiltersFragment(), "intentCamera");
                    }

                    if (cameVia.equalsIgnoreCase("dreamyFraghome")) {
                        loadFragment(new DreamyFiltersFragment(), "intentGallery");

                    } else if (cameVia.equalsIgnoreCase("dreamyFragcamera")) {
                        loadFragment(new DreamyFiltersFragment(), "intentCamera");
                    }

                    if (cameVia.equalsIgnoreCase("profileFraghome")) {
                        loadFragment(new ProfileFiltersFragment(), "intentGallery");

                    } else if (cameVia.equalsIgnoreCase("profileFragcamera")) {
                        loadFragment(new ProfileFiltersFragment(), "intentCamera");
                    }

                    if (cameVia.equalsIgnoreCase("comicFraghome")) {
                        loadFragment(new ComicFiltersFragment(), "intentGallery");

                    } else if (cameVia.equalsIgnoreCase("comicFragcamera")) {
                        loadFragment(new ComicFiltersFragment(), "intentCamera");
                    }

                    if (cameVia.equalsIgnoreCase("vintageFraghome")) {
                        loadFragment(new VintageFiltersFragment(), "intentGallery");

                    } else if (cameVia.equalsIgnoreCase("vintageFragcamera")) {
                        loadFragment(new VintageFiltersFragment(), "intentCamera");
                    }

                } else if (globalIntent.getStringExtra("from").equalsIgnoreCase("exit") ||
                        globalIntent.getStringExtra("from").equalsIgnoreCase("save")) {

                    if (globalIntent.getStringExtra("via").equalsIgnoreCase("trendingFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("trendingFragcamera")) {
                        loadFragment(new TrendingFiltersFragment(), "");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("dreamyFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("dreamyFragcamera")) {
                        loadFragment(new DreamyFiltersFragment(), "");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("profileFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("profileFragcamera")) {
                        loadFragment(new ProfileFiltersFragment(), "");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("comicFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("comicFragcamera")) {
                        loadFragment(new ComicFiltersFragment(), "");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("vintageFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("vintageFragcamera")) {
                        loadFragment(new VintageFiltersFragment(), "");
                    }

                } else if (globalIntent.getStringExtra("from").equalsIgnoreCase("trail")) {

                    if (globalIntent.getStringExtra("via").equalsIgnoreCase("trendingFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("trendingFragcamera")) {
                        filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                        Log.e(TAG, "onCreate: filter ids from trail: " + filterIdsList.size());
                        loadFragment(new TrendingFiltersFragment(), "trail");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("dreamyFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("dreamyFragcamera")) {
                        filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                        loadFragment(new DreamyFiltersFragment(), "trail");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("profileFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("profileFragcamera")) {
                        filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                        loadFragment(new ProfileFiltersFragment(), "trail");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("comicFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("comicFragcamera")) {
                        filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                        loadFragment(new ComicFiltersFragment(), "trail");

                    } else if (globalIntent.getStringExtra("via").equalsIgnoreCase("vintageFraghome") ||
                            globalIntent.getStringExtra("via").equalsIgnoreCase("vintageFragcamera")) {
                        filterIdsList = globalIntent.getStringArrayListExtra("filterIds");
                        loadFragment(new VintageFiltersFragment(), "trail");

                    }
                }
            }
        } else {
            Log.e(TAG, "onCreate: intent is null");
        }

        /*
         * Check for storage and camera permission
         */
        askStoragePermission("start");

        /*
         * Click listener to allow storage access when revoke by user
         */
        storageAccessBtn.setOnClickListener(v -> askStoragePermission("settings"));

        /*
         * Click listener to load Free Trail Fragment
         */
        buyPremiumLayout.setOnClickListener(v -> loadFragment(new FreeTrailFragment(), "click"));

        navDrawerLayout.setOnClickListener(v -> openDrawer());

        navLanguageLayout.setOnClickListener(v -> {
            closeDrawer();
            startActivity(new Intent(HomeActivity.this, SelectLanguageActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        navSavedFilesLayout.setOnClickListener(v -> {
            closeDrawer();
            loadInterstitialAd("saved");
        });

        savedFilesLayout.setOnClickListener(v -> loadInterstitialAd("saved"));

        navShareLayout.setOnClickListener(v -> {
            closeDrawer();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body) + "\n"
                    + "https://play.google.com/store/apps/details?id=com.cartoonpic.aiphotoeditor");

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
        });

        navRateLayout.setOnClickListener(v -> {
            closeDrawer();
            DialogHandler.getInstance().rateUsDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
                if (status != null && data != null) {
                    float ratingVal = Float.parseFloat(data);

                    if (status.equals("2")) {
                        alertDialog.dismiss();

                    } else if (ratingVal < 4) {
                        getDeviceInfo();
                        alertDialog.dismiss();

                    } else {
                        alertDialog.dismiss();
                        Intent appPlayStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.cartoonpic.aiphotoeditor"));
                        appPlayStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(appPlayStoreIntent);
                    }
                }
            });
        });

        navPrivacyLayout.setOnClickListener(v -> {
            closeDrawer();
            DialogHandler.getInstance().privacyDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
                if (status != null) {
                    switch (status) {
                        case "1":
                        case "3":
                            alertDialog.dismiss();
                            break;
                        case "2":
                            Uri webpage = Uri.parse("https://airportflightsstatus.com/");
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            alertDialog.dismiss();
                            startActivity(browserIntent);
                            break;
                    }
                }
            });
        });

        navFeedbackLayout.setOnClickListener(v -> {
            closeDrawer();
            getDeviceInfo();
        });

        navMoreAppLayout.setOnClickListener(v -> {
            closeDrawer();
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Airport%20Flights%20Status™"));
            playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(playStoreIntent);
        });

        navExitLayout.setOnClickListener(v -> {
            closeDrawer();
            showExitDialog();
        });

        navBackLayout.setOnClickListener(v -> closeDrawer());

        trendingFiltersLayout.setOnClickListener(v -> loadInterstitialAd("trending"));

        dreamyFiltersLayout.setOnClickListener(v -> loadInterstitialAd("dreamy"));

        profileFiltersLayout.setOnClickListener(v -> loadInterstitialAd("profile"));

        comicStylesLayout.setOnClickListener(v -> loadInterstitialAd("comic"));

        vintageStylesLayout.setOnClickListener(v -> loadInterstitialAd("vintage"));

        navTrendingFiltersLayout.setOnClickListener(v -> loadInterstitialAd("trending"));

        navDreamyFiltersLayout.setOnClickListener(v -> loadInterstitialAd("dreamy"));

        navProfileFiltersLayout.setOnClickListener(v -> loadInterstitialAd("profile"));

        navComicStylesLayout.setOnClickListener(v -> loadInterstitialAd("comic"));

        navVintageStylesLayout.setOnClickListener(v -> loadInterstitialAd("vintage"));
    }

    /**
     * Method to load Fragment
     */
    public void loadFragment(Fragment fragmentToLoad, String from) {
        if (drawerLayout.isOpen()) {
            closeDrawer();
        }

        FragmentManager manager = getSupportFragmentManager();
        String fragmentTag = fragmentToLoad.getClass().getName();

        boolean fragmentPopped = manager.popBackStackImmediate(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        existingFragment = fragmentToLoad;

        if (from.equalsIgnoreCase("intentGallery") || from.equalsIgnoreCase("intentCamera") && !(existingFragment instanceof FreeTrailFragment)) {
            bundle.putString("from", from);
            bundle.putStringArrayList("filterIds", filterIdsList);
            Log.e(TAG, "loadFragment: filter id size: " + filterIdsList.size());

        } else if (from.equalsIgnoreCase("trail")) {
            bundle.putString("from", "trail");
            bundle.putStringArrayList("filterIds", filterIdsList);

        } else {
            bundle.putString("from", "home");
        }

        existingFragment.setArguments(bundle);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_fragmentContainer, existingFragment, fragmentTag);

        if (!fragmentPopped) {
            Log.e(TAG, "loadFragment: working");
            transaction.addToBackStack(fragmentTag);
        }

        transaction.commit();
        isFragmentVisible = true;
    }

    /**
     * Method to get device info
     */
    private void getDeviceInfo() {
        model = android.os.Build.MODEL;
        manufacturer = android.os.Build.MANUFACTURER;
        osVersion = Build.VERSION.RELEASE + " " + Build.DISPLAY + " ";
        apiLevel = String.valueOf(Build.VERSION.SDK_INT);

        DeviceName.with(this).request((info, error) -> {
            if (info.model.equalsIgnoreCase(info.marketName)) {
                model = info.marketName;
            } else {
                model = info.marketName + " " + info.model;
            }
        });

        if (Constants.F_MAIL == null || Constants.F_MAIL.isEmpty()) {
            Constants.F_MAIL = "info@airportflightsstatus.com";
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.F_MAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.device_info_label) + "\n" + getString(R.string.os_ver_label) +
                osVersion + "\n" + getString(R.string.api_level_label) + apiLevel + "\n" + getString(R.string.dev_label) + manufacturer + "\n" + getString(R.string.model_label) + model + "(" + manufacturer + ")" + "\n\n" +
                getString(R.string.feedback_msg) + "\n");

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "getDeviceInfo: exception: " + e.getLocalizedMessage());
            Toast.makeText(this, R.string.no_email_txt, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to load and display interstitial add
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
        if (!isPremium) {
            InterstitialAd.load(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            Log.e(TAG, "mInterstitialAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, loadAdError.toString());
                            mInterstitialAd = null;
                        }
                    });
            if (mInterstitialAd != null && !from.equalsIgnoreCase("resume")) {
                SplashActivity.adCounter++;
                prefsManager.setAdCount(SplashActivity.adCounter);

                Log.e(TAG, "loadInterstitialAd: ad count: " + prefsManager.getAdCount());

                if (prefsManager.getAdCount() > 0 && prefsManager.getAdCount() % 3 == 0) {
                    mInterstitialAd.show(HomeActivity.this);
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
                            if (from.equalsIgnoreCase("saved")) {
                                savedFilesClickAction();
                            } else if (from.equalsIgnoreCase("trending")) {
                                loadFragment(new TrendingFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("dreamy")) {
                                loadFragment(new DreamyFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("profile")) {
                                loadFragment(new ProfileFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("comic")) {
                                loadFragment(new ComicFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("vintage")) {
                                loadFragment(new VintageFiltersFragment(), "click");
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("saved")) {
                                savedFilesClickAction();
                            } else if (from.equalsIgnoreCase("trending")) {
                                loadFragment(new TrendingFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("dreamy")) {
                                loadFragment(new DreamyFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("profile")) {
                                loadFragment(new ProfileFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("comic")) {
                                loadFragment(new ComicFiltersFragment(), "click");

                            } else if (from.equalsIgnoreCase("vintage")) {
                                loadFragment(new VintageFiltersFragment(), "click");
                            }
                        }
                    });
                } else {
                    if (from.equalsIgnoreCase("saved")) {
                        savedFilesClickAction();
                    } else if (from.equalsIgnoreCase("trending")) {
                        loadFragment(new TrendingFiltersFragment(), "click");

                    } else if (from.equalsIgnoreCase("dreamy")) {
                        loadFragment(new DreamyFiltersFragment(), "click");

                    } else if (from.equalsIgnoreCase("profile")) {
                        loadFragment(new ProfileFiltersFragment(), "click");

                    } else if (from.equalsIgnoreCase("comic")) {
                        loadFragment(new ComicFiltersFragment(), "click");

                    } else if (from.equalsIgnoreCase("vintage")) {
                        loadFragment(new VintageFiltersFragment(), "click");
                    }
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (from.equalsIgnoreCase("saved")) {
                    savedFilesClickAction();
                } else if (from.equalsIgnoreCase("trending")) {
                    loadFragment(new TrendingFiltersFragment(), "click");

                } else if (from.equalsIgnoreCase("dreamy")) {
                    loadFragment(new DreamyFiltersFragment(), "click");

                } else if (from.equalsIgnoreCase("profile")) {
                    loadFragment(new ProfileFiltersFragment(), "click");

                } else if (from.equalsIgnoreCase("comic")) {
                    loadFragment(new ComicFiltersFragment(), "click");

                } else if (from.equalsIgnoreCase("vintage")) {
                    loadFragment(new VintageFiltersFragment(), "click");
                }
            }
        } else {
            Log.e(TAG, "Premium");
            if (from.equalsIgnoreCase("saved")) {
                savedFilesClickAction();
            } else if (from.equalsIgnoreCase("trending")) {
                loadFragment(new TrendingFiltersFragment(), "click");

            } else if (from.equalsIgnoreCase("dreamy")) {
                loadFragment(new DreamyFiltersFragment(), "click");

            } else if (from.equalsIgnoreCase("profile")) {
                loadFragment(new ProfileFiltersFragment(), "click");

            } else if (from.equalsIgnoreCase("comic")) {
                loadFragment(new ComicFiltersFragment(), "click");

            } else if (from.equalsIgnoreCase("vintage")) {
                loadFragment(new VintageFiltersFragment(), "click");
            }
        }
    }

    /**
     * Method to navigate user to saved files activity
     */
    private void savedFilesClickAction() {
        startActivity(new Intent(HomeActivity.this, SavedFileActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * Helper method to open the drawer programmatically
     */
    private void openDrawer() {
        drawerLayout.openDrawer(navDrawer);
    }

    /**
     * Helper method to close the drawer programmatically
     */
    private void closeDrawer() {
        drawerLayout.closeDrawer(navDrawer);
    }

    /**
     * Method to initialize FCM push notification service
     */
    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    }
                });
    }

    /**
     * Method to handle camera and media access permissions
     *
     * @param from : To check if this method is called via on create or on click on camera or gallery layout
     */
    private void askStoragePermission(String from) {
        boolean storagePermFlag = false;
        String permission;

        /*
         * to handle permission when it called from on create or gallery or settings layout click
         */
        if (from.equalsIgnoreCase("start") || from.equalsIgnoreCase("gallery") || from.equalsIgnoreCase("settings")) {
            Log.e(TAG, "askStoragePermission: inside if from: " + from);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            }
            if (ContextCompat.checkSelfPermission(HomeActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "askStoragePermission: inside second if from: " + from);
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permission)) {
                    Log.e(TAG, "askStoragePermission: inside 3rd if from: " + from);
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                            Constants.REQUEST_GALLERY_ACCESS);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permission)) {
                    if (from.equalsIgnoreCase("start")) {
                        Log.e(TAG, "askStoragePermission: inside 3rd if's else if from: " + from);
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                                Constants.REQUEST_GALLERY_ACCESS);
                    } else {
                        Log.e(TAG, "askStoragePermission: inside 3rd if's else from: " + from);
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                                Constants.PERMISSION_REQUEST_SETTINGS);
                    }
                } else {
                    Log.e(TAG, "askStoragePermission: inside 3rd if's else from: " + from);
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                            Constants.REQUEST_GALLERY_ACCESS);
                }
            } else {
                storagePermFlag = true;
            }
        }
        /*
         * to handle permission when it is called from camera layout click
         */
        else if (from.equalsIgnoreCase("camera")) {
            Log.e(TAG, "askStoragePermission: inside if");
            if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "askStoragePermission: inside second if");
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third else if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

                } else {
                    Log.e(TAG, "askStoragePermission: inside third else");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);
                }
            } else {
                Log.e(TAG, "askStoragePermission: inside else of first if");
                storagePermFlag = true;
                startCameraIntent();
            }
        }
    }

    /**
     * Method to handle permission requests
     *
     * @param requestCode:  result code
     * @param permissions:  permissions asked
     * @param grantResults: request status e.g. granted/denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean showStorageAccessSettings = false;
        boolean isGranted = false;

        Log.e(TAG, "onRequestPermissionsResult: called");
        /*
         * When Permission to gallery access is requested
         */
        if (requestCode == Constants.REQUEST_GALLERY_ACCESS) {
            Log.e(TAG, "onRequestPermissionsResult: inside gallery access");
            for (int result : grantResults) {
                Log.e(TAG, "onRequestPermissionsResult: inside for");
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: inside if");
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                        Log.e(TAG, "onRequestPermissionsResult: inside nested if");
                        storageAccessBtn.setText(R.string.open_settings);
                    }
                    allowAccessLayout.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                    filterTilesLayout.setVisibility(View.GONE);
                    adDisplayLayout.setVisibility(View.GONE);

                } else {
                    Log.e(TAG, "onRequestPermissionsResult: inside else");
                    allowAccessLayout.setVisibility(View.GONE);
                    getNotificationPermission();
                }
            }
        }
        /*
         * When permission to camera access is requested
         */
        else if (requestCode == Constants.REQUEST_CAMERA_ACCESS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {

                        if (!isFirstTime) {
                            showStorageAccessSettings = true;

                        } else {
                            isFirstTime = false;
                        }

                    } else {
                        Toast.makeText(this, getString(R.string.cam_denied), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    isGranted = true;
                }
            }
            if (isGranted) {
                startCameraIntent();
            }

            if (showStorageAccessSettings) {
                openSettings();
            }
        }
        /*
         * When permission to open settings is requested
         */
        else if (requestCode == Constants.PERMISSION_REQUEST_SETTINGS) {
            Log.e(TAG, "onRequestPermissionsResult: PERMISSION_REQUEST_SETTINGS working");
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            !ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        storageAccessBtn.setText(R.string.open_settings);
                        showStorageAccessSettings = true;
                    }
                    allowAccessLayout.setVisibility(View.VISIBLE);
                    filterTilesLayout.setVisibility(View.GONE);
                    adDisplayLayout.setVisibility(View.GONE);

                } else {
                    allowAccessLayout.setVisibility(View.GONE);
                }
            }
            if (showStorageAccessSettings) {
                openSettings();
            }
        } else if (requestCode == Constants.REQUEST_NOTIFICATION_ACCESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onRequestPermissionsResult: permission granted");

            } else {
                Toast.makeText(this, R.string.noti_msg_body, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onRequestPermissionsResult: permission not granted");
            }
        }
    }

    /**
     * Method to ask for notifications permission (Devices Greater or equal to android 13)
     */
    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        Constants.REQUEST_NOTIFICATION_ACCESS);
            } else {
                Log.e(TAG, "getNotificationPermission: no permission needed");
            }
        } catch (Exception e) {
            Log.e(TAG, "getNotificationPermission: exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * Method to open camera when camera access is granted
     */
    @SuppressWarnings("deprecation")
    private void startCameraIntent() {
        Constants.isSelectingFile = true;
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Log.e(TAG, "startCameraIntent: inside if");

        try {
            camPhotoFile = Utility.getInstance().createCamFile(HomeActivity.this);
        } catch (IOException ex) {
            Log.e(TAG, "startCameraIntent: exception: " + ex.getLocalizedMessage());
        }

        if (camPhotoFile != null) {
            try {
                Log.e(TAG, "startCameraIntent: file: " + camPhotoFile.getAbsolutePath());
                capturedImageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        camPhotoFile);

                Log.e(TAG, "startCameraIntent: captured uri: " + capturedImageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(takePictureIntent, Constants.CAMERA_PIC_REQUEST);

            } catch (Exception e) {
                Log.e(TAG, "startCameraIntent: camPhotoFile exception: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Method to open settings to allow permission when "don't ask again" is selected by user
     */
    private void openSettings() {
        Log.e(TAG, "openSettings: working");
        Constants.isSelectingFile = true;
        isFromSettings = true;
        Intent settingsIntent = new Intent();
        settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        settingsIntent.setData(uri);
        startActivity(settingsIntent);
    }

    /**
     * To handle camera intent and gallery intent requests
     *
     * @param requestCode: to identify if gallery intent is invoked or camera intent
     * @param resultCode:  to identify if intent result is successful
     * @param data:        data received via intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Constants.isSelectingFile = true;
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == RESULT_OK) {
            assert data != null;
            Uri path = data.getData();
            handleIntentAction(path, "home");

        } else if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(camPhotoFile.getAbsolutePath());

                // Rotate the bitmap based on Exif orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, camPhotoFile.getAbsolutePath());

                Uri path = Utility.getInstance().bitmapToUri(this, rotatedBitmap, "");
                Log.e(TAG, "onActivityResult: path: " + path.getPath());
                handleIntentAction(path, "camera");

            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: error: " + e.getLocalizedMessage());
            }
        } else if (requestCode == Constants.REQ_CODE_VERSION_UPDATE) {
            if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
                // Log.e("Update flow failed!", " Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
                unregisterInstallStateUpdListener();
            }
        } else {
            Log.e(TAG, "Data is Null");
        }
    }

    /**
     * Method to rotate image to it's default state when captured by camera
     *
     * @param bitmap:    bitmap of captured image
     * @param imagePath: local path of image in cache
     * @return: Bitmap
     */
    private Bitmap rotateBitmap(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Log.e(TAG, "rotateBitmap: orientation: " + orientation);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            Log.e(TAG, "rotateBitmap: exception: " + e.getLocalizedMessage());
            return bitmap;
        }
    }

    /**
     * to open Apply Effect Activity and pass
     * gallery selected image or camera captured image
     *
     * @param path: path of gallery selected image or camera captured image
     */
    private void handleIntentAction(Uri path, String from) {
        String selectedImagePath = Utility.getInstance().getPath(this, path);
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            Intent intent = new Intent(HomeActivity.this, ApplyEffectActivity.class);
            intent.putExtra("path", selectedImagePath);
            intent.putExtra("from", from);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void showExitDialog() {
        DialogHandler.getInstance().exitDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
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

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
//        if (adView != null) {
//            adView.resume();
//        }

        loadInterstitialAd("resume");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onResume: inside perm if");
            animationView.cancelAnimation();
            allowAccessLayout.setVisibility(View.GONE);
            filterTilesLayout.setVisibility(View.VISIBLE);
            adDisplayLayout.setVisibility(View.VISIBLE);
        } else {
            adDisplayLayout.setVisibility(View.GONE);
            Log.e(TAG, "onResume: inside perm else: ");
        }

        if (isPremium) {
            adDisplayLayout.setVisibility(View.GONE);
            nativeAdContainer.setVisibility(View.GONE);
        } else {
            adDisplayLayout.setVisibility(View.VISIBLE);
            nativeAdContainer.setVisibility(View.VISIBLE);
        }

        if (!isFromSettings) {
            Constants.isSelectingFile = false;
        }

        isFromSettings = false;
        Log.e(TAG, "onResume: isSelectingFile: " + Constants.isSelectingFile);
    }

    /**
     * Method To handle back press
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isOpen()) {
            closeDrawer();
        }

        Log.e(TAG, "onBackPressed: fragment visible bit: " + isFragmentVisible);
        if (isFragmentVisible) {
            Log.e(TAG, "onBackPressed: existing frag: " + existingFragment);
            Log.e(TAG, "onBackPressed: fragment count: " + getSupportFragmentManager().getBackStackEntryCount());

            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                Log.e(TAG, "onBackPressed: if working");
                getSupportFragmentManager().popBackStack();

            } else {
                Log.e(TAG, "onBackPressed: else working");
                getSupportFragmentManager().popBackStack(existingFragment.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Log.e(TAG, "onBackPressed: count in else: " + getSupportFragmentManager().getBackStackEntryCount());
                if (existingFragment != null) {
                    Log.e(TAG, "onBackPressed: inside existing fragment not null");
                    getSupportFragmentManager().beginTransaction().remove(existingFragment).commit();
                    isFragmentVisible = false;
                } else {
                    Log.e(TAG, "onBackPressed: existing fragment is null");
                }
            }
        } else {
            showExitDialog();
        }
    }

    /**
     * Interface implementation of Banner Ad helper class
     */
    @Override
    public void onAdClicked() {
        Log.e(TAG, "onAdClicked: ");
    }

    /**
     * Interface implementation of Banner Ad helper class
     */
    @Override
    public void onAdLoaded() {
        Log.e(TAG, "onAdaptiveAdLoaded: ");
        tvLoadingAd.setVisibility(View.GONE);
    }

    /**
     * Interface implementation of Banner Ad helper class
     */
    @Override
    public void onAdFailedToLoad(LoadAdError loadAdError) {
        Log.e(TAG, "onAdFailedToLoad: " + loadAdError);
    }

    @Override
    public void itemClicked() {

    }

    @Override
    public void onPremiumStatusChanged(boolean isPremium) {
        Log.e(TAG, "onPremiumStatusChanged: is premium: " + isPremium);
        if (isPremium) {
            adDisplayLayout.setVisibility(View.GONE);
            nativeAdContainer.setVisibility(View.GONE);

            if (!isStarted) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("from", globalIntent.getStringExtra("from"));
                intent.putExtra("via", globalIntent.getStringExtra("via"));
                intent.putExtra("filterIds", globalIntent.getStringArrayListExtra("filterIds"));
                startActivity(intent);
                isStarted = true;
            }

        } else {
            adDisplayLayout.setVisibility(View.VISIBLE);
            nativeAdContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * checkForAppUpdate method
     */
    private void checkForAppUpdate() {
        //Log.e("checkForAppUpdate", "Here");
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(HomeActivity.this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = installState -> {
            // Show module progress, log state, or install the update.
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                //Log.e("APPUpdate", "Downloaded");
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popupSnackbarForCompleteUpdateAndUnregister();

            } else if (installState.installStatus() == InstallStatus.INSTALLED) {
                //Log.e("APPUpdate", "Installed");
                if (appUpdateManager != null) {
                    appUpdateManager.unregisterListener(installStateUpdatedListener);
                }

            } else if (installState.installStatus() == InstallStatus.PENDING) {
                //Log.e("APPUpdate", "Pending");

            } else if (installState.installStatus() == InstallStatus.DOWNLOADING) {
                // Log.e("APPUpdate", "Downloading");

            } else if (installState.installStatus() == InstallStatus.INSTALLING) {
                // Log.e("APPUpdate", "Installing");

            } else if (installState.installStatus() == InstallStatus.FAILED) {
                // Log.e("APPUpdate", "Failed");

            } else {
                // Log.e("appUpdateManager", "InstallStateUpdatedListener: state: " + installState.installStatus());
            }
        };

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            //  Log.e("V1", "" + appUpdateInfo.updateAvailability());
            //   Log.e("V2", "" + UpdateAvailability.UPDATE_AVAILABLE);
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    appUpdateManager.registerListener(installStateUpdatedListener);
                    // Start an update.
                    //  Log.e("UpdateType", "Immediate");
                    startAppUpdateImmediate(appUpdateInfo);

                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    appUpdateManager.registerListener(installStateUpdatedListener);
                    // Log.e("UpdateType", "Flexible");
                    // Start an update.
                    startAppUpdateFlexible(appUpdateInfo);

                } else {
                    // Log.e("UpdateType", "Nothing");
                }

            } else {
                // Log.e("UpdateType", "Not Available");
            }
        });

        appUpdateInfoTask.addOnFailureListener(e -> {
            //Log.e("appUpdateInfoTask Fail", e.toString())
        });
    }

    /**
     * startAppUpdateImmediate method
     *
     * @param appUpdateInfo
     */
    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    /**
     * startAppUpdateFlexible method
     *
     * @param appUpdateInfo
     */
    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {
        //Log.e("APPUpdate", "Snackbar");

        try {
            appUpdateManager.completeUpdate();
            //unregisterInstallStateUpdListener();

        } catch (Exception e) {
            Log.e("complete exception", e.toString());
        }
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            //FLEXIBLE:
                            // If the update is downloaded but not installed,
                            // notify the user to complete the update.
                            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                // Log.e("AppUpdate", "Was Downloaded");
                                popupSnackbarForCompleteUpdateAndUnregister();
                            }

                            //IMMEDIATE:
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                //Log.e("AppUpdate", "Here");
                                startAppUpdateImmediate(appUpdateInfo);
                            } else {
                                // Log.e("AppUpdate", "Here 1");
                            }
                        });

    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        // Log.e("Unregister", "1");
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }
}