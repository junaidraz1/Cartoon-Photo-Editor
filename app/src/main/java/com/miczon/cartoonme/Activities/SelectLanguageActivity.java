package com.miczon.cartoonme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.RecyclerViewAdapters.CountryAdapter;
import com.miczon.cartoonme.Utils.Utility;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class SelectLanguageActivity extends BaseActivity implements BannerAdLoadListener {

    String TAG = "SelectLanguageActivity";
    Button btnSelectLanguage, btnCancel;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rvCountries;
    FrameLayout adContainer;
    TextView tvLoadingAd;
    RelativeLayout adLayout;

    CountryAdapter countryAdapter;
    PrefsManager prefsManager;

   /* AdView adView;*/
    AdRequest adRequest;

    BannerAdManager bannerAdManager;

    int selectedLanguagePosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);

        rvCountries = findViewById(R.id.rv_countries);
        btnSelectLanguage = findViewById(R.id.btn_accept);
        btnCancel = findViewById(R.id.btn_cancel);
        adContainer = findViewById(R.id.fl_adContainer);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        adLayout = findViewById(R.id.rl_adContainer);

        prefsManager = new PrefsManager(SelectLanguageActivity.this);
        layoutManager = new LinearLayoutManager(SelectLanguageActivity.this);
        bannerAdManager = new BannerAdManager(SelectLanguageActivity.this, this);
        adRequest = new AdRequest.Builder().build();
      /*  adView = new AdView(this);

        bannerAdManager.setAdSize(adView);*/

        if (!Utility.getInstance().isPremiumActive(SelectLanguageActivity.this)) {
            adLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
          /*  adContainer.addView(adView);*/
            bannerAdManager.loadBannerAd(adContainer);
        } else {
            adContainer.setVisibility(View.GONE);
            adLayout.setVisibility(View.GONE);
        }

        countryAdapter = new CountryAdapter(this, Utility.getInstance().countryNames(), Utility.getInstance().countryFlags(), (position, path, action) -> {

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

            startActivity(new Intent(SelectLanguageActivity.this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        btnCancel.setOnClickListener(v -> {
            if (prefsManager.getSelectedLanguage() == null && prefsManager.getSelectedLanguage().isEmpty()) {
                prefsManager.setSelectedLanguage("en");
            }
            startActivity(new Intent(SelectLanguageActivity.this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (adView != null) {
//            adView.resume();
//        }

        if (!Utility.getInstance().isPremiumActive(SelectLanguageActivity.this)) {
            adLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
        } else {
            adContainer.setVisibility(View.GONE);
            adLayout.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onPause() {
      /*  if (adView != null) {
            adView.pause();
        }*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }
        super.onDestroy();
    }

    @Override
    public void onAdClicked() {
        Log.e(TAG, "onAdClicked:");
    }

    @Override
    public void onAdLoaded() {
        Log.e(TAG, "onAdaptiveAdLoaded: ");
        tvLoadingAd.setVisibility(View.GONE);
    }

    @Override
    public void onAdFailedToLoad(LoadAdError loadAdError) {
        Log.e(TAG, "onAdFailedToLoad: ");
    }
}