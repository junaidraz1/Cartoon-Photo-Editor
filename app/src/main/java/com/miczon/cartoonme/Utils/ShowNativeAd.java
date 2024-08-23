package com.miczon.cartoonme.Utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.miczon.cartoonme.R;

import org.jetbrains.annotations.NotNull;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class ShowNativeAd {

    Activity activity;
    public String TAG = "ShowNativeAd";

    /**
     * Constructor of class
     *
     * @param activity: from where it is called
     */
    public ShowNativeAd(Activity activity) {
        this.activity = activity;
    }

    /**
     * Method to show native ad
     *
     * @param adView:          native ad view to display ad in it
     * @param adUnifiedLayout: another ad view for native ad
     * @param adUnitID:        ad id
     */
    public void showAdMobNativeAd(final NativeAdView adView, final LinearLayout adUnifiedLayout, String adUnitID) {
        LinearLayout loadingLayout = adUnifiedLayout.findViewById(R.id.ll_loadingText);
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        AdLoader.Builder builder = new AdLoader.Builder(activity.getApplicationContext(), adUnitID);

        if (!Utility.getInstance().isPremiumActive(activity)) {
            AdLoader adLoader = builder.forNativeAd(nativeAd -> {
                adView.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
                populateNativeAdView(nativeAd, adView);
                Log.d("NativeAd", "AdMob Native Advanced Successfully Load!");
            }).withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d("NativeAd", "AdMob Native Advanced Failed to Load!");
                }
            }).build();
            adLoader.loadAds(new AdRequest.Builder().build(), 1);
        } else {
            loadingLayout.setVisibility(View.GONE);
            adUnifiedLayout.setVisibility(View.GONE);
            Log.e(TAG, "showAdMobNativeAd: premiums is active");
        }
    }

    public void showAdMobNativeBannerAd(final NativeAdView adView, final FrameLayout nativeAdContainer, String adUnitID) {
        LinearLayout loadingLayout = nativeAdContainer.findViewById(R.id.ll_loadingText);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        AdLoader.Builder builder = new AdLoader.Builder(activity, adUnitID);

        if (!Utility.getInstance().isPremiumActive(activity)) {
            AdLoader adLoader = builder.forNativeAd(nativeAd -> {
                nativeAdContainer.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
                populateNativeAdView(nativeAd, adView);
                Log.v("NativeAd", "AdMob Native Advanced Successfully Load!");
            }).withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("NativeAd", "AdMob Native Advanced Failed to Load! " + loadAdError);
                }
            }).build();
            adLoader.loadAds(new AdRequest.Builder().build(), 1);
        } else {
            loadingLayout.setVisibility(View.GONE);
            nativeAdContainer.setVisibility(View.GONE);
            Log.e(TAG, "showAdMobNativeBannerAd: premiums is active");
        }
    }

    /**
     * Method to set data in native ad i.e. title, headline, description etc
     *
     * @param nativeAd: object of native ad class
     * @param adView:   ad view in which it is to be displayed
     */
    private void populateNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        try {
            if (adView.getHeadlineView() != null)
                ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
            if (adView.getBodyView() != null)
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            if (adView.getCallToActionView() != null)
                ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        com.google.android.gms.ads.nativead.NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            if (adView.getIconView() != null)
                adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getIconView() != null) {
                ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        }

        if (nativeAd.getPrice() == null) {
            if (adView.getPriceView() != null)
                adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getPriceView() != null) {
                adView.getPriceView().setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        }

        if (nativeAd.getStore() == null) {
            if (adView.getStoreView() != null)
                adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getStoreView() != null) {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        }

        if (nativeAd.getStarRating() == null) {
            if (adView.getStarRatingView() != null)
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getStarRatingView() != null) {
                ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        }

        if (nativeAd.getAdvertiser() == null) {
            if (adView.getAdvertiserView() != null)
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getAdvertiserView() != null) {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}