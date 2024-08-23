package com.miczon.cartoonme.Activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.miczon.cartoonme.BuildConfig;
import com.miczon.cartoonme.Manager.PrefsManager;

import java.util.Locale;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class BaseActivity extends AppCompatActivity {

    String TAG = "BaseLocaleActivity";
    PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(BaseActivity.this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        initializeAdMob();
        prefsManager = new PrefsManager(BaseActivity.this);
        setAppLanguage(prefsManager.getSelectedLanguage());

        Log.e(TAG, "onCreate: locale: " + getLocale());

    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedLanguage = prefsManager.getSelectedLanguage();
        if (!getLocale().getLanguage().equals(selectedLanguage)) {
            setAppLanguage(selectedLanguage);
        }

        Log.e(TAG, "onResume: locale: " + getLocale());
    }

    /**
     * Method to initialize Ad Mob
     */
    public void initializeAdMob() {
        MobileAds.initialize(this, initializationStatus -> {
         /*   List<String> testDeviceIds = List.of("A72AB4C10FD06596B4F313B2A34BA6B6", "D8258FEBB3EE361B3D889A4064C01567", "43AD809CB29C8D32D764A97FD383CD36");
            RequestConfiguration configuration = new RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds) // Add your test device ID here
                    .build();
            MobileAds.setRequestConfiguration(configuration);*/
        });
    }

    /**
     * To change app's language
     * @param languageCode: language code of language that is to be changed
     */
    protected void setAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /**
     * get current selected language of app
     * @return: Locale
     */
    protected Locale getLocale() {
        Configuration configuration = getResources().getConfiguration();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        } else {
            //noinspection deprecation
            return configuration.locale;
        }
    }
}