package com.miczon.cartoonme.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.developers.imagezipper.ImageZipper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Manager.ConnectionManager;
import com.miczon.cartoonme.Model.Response.FilterResponse;
import com.miczon.cartoonme.Model.Response.FinalResultResponse;
import com.miczon.cartoonme.Model.Response.UploadImageResponse;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Retrofit.apiUtils;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class ApplyEffectActivity extends BaseActivity implements BannerAdLoadListener {

    String TAG = "ApplyEffectActivity", path = "", from = "", via = "", finalResult = "", uploadedTempUrl = "";
    RelativeLayout backLayout, saveLayout, adDisplayLayout;
    TextView tvHeader;
    ImageView imageView, originalImageView, noImageView;
    FrameLayout adContainer;
    TextView tvLoadingAd;
    AlertDialog alertDialog;

    RecyclerView.LayoutManager layoutManager;

    Bitmap bitmapResult;

    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

    BannerAdManager bannerAdManager;

    public static boolean isFragmentVisible = false;

    ArrayList<String> filterIdList;

    Uri croppedPicture = null;

    Intent intent;
    Bundle bundle;

    File imageZipperFile;
    Call<UploadImageResponse> uploadImageCall;
    Call<FilterResponse> applyFilterCall;
    Call<FinalResultResponse> getFinalImageCall;

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_effect);

        imageView = findViewById(R.id.imageView);
        backLayout = findViewById(R.id.rl_back);
        saveLayout = findViewById(R.id.rl_save);
        tvHeader = findViewById(R.id.tv_header);
        adContainer = findViewById(R.id.fl_adContainer);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        adDisplayLayout = findViewById(R.id.rl_adContainer);
        originalImageView = findViewById(R.id.iv_original);
        noImageView = findViewById(R.id.iv_noImage);

        tvHeader.setText(getResources().getString(R.string.filter_label));

        filterIdList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(ApplyEffectActivity.this);
        adRequest = new AdRequest.Builder().build();
        bannerAdManager = new BannerAdManager(this, this);
        bundle = new Bundle();

        if (!Utility.getInstance().isPremiumActive(this)) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);

        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        /*
         * to get path of selected picture from previous activity
         */
        intent = getIntent();

        if (intent == null) {
            Log.e(TAG, "onCreate: Intent is null");

        } else {
            path = intent.getStringExtra("path");
            from = intent.getStringExtra("from");
            filterIdList = intent.getStringArrayListExtra("ids");
            via = intent.getStringExtra("via");

            Log.e(TAG, "onCreate: from: " + from);
            Log.e(TAG, "onCreate: path: " + path);
            Log.e(TAG, "onCreate: via: " + via);
//            Log.e(TAG, "onCreate: filter ids: " + filterIdList.size());

            if (from != null && path != null && filterIdList != null && filterIdList.size() > 0) {
                Uri picture = Uri.fromFile(new File(path));

                File cacheDir = new File(getCacheDir(), "cropped");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                croppedPicture = Uri.fromFile(new File(cacheDir, "cropped_image.jpg"));
                Log.e(TAG, "onCreate: cropped URI: " + croppedPicture);
                UCrop.of(picture, croppedPicture)
                        .withOptions(Utility.imageCropperStyle(ApplyEffectActivity.this))
                        .start(ApplyEffectActivity.this);

            } else {
                Log.e(TAG, "onCreate: intent data is null"); // navigate back to home

                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            Intent intent = new Intent(ApplyEffectActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.e(TAG, "onActivityResult: from: " + from);
                            if (from.equalsIgnoreCase("camera")) {
                                intent.putExtra("from", "cameraAction");

                            } else if (from.equalsIgnoreCase("home")) {
                                intent.putExtra("from", "filter");
                            }
                            intent.putExtra("via", via);
                            intent.putExtra("filterIds", filterIdList);
//                            Log.e(TAG, "onActivityResult: filter id size: " + filterIdList.size());
                            startActivity(intent);
                            Log.e(TAG, "onActivityResult: outer else working");
                        });
            }
        }

        originalImageView.setOnLongClickListener(v -> {
            if (croppedPicture != null) {
                imageView.setImageURI(croppedPicture);
            } else {
                originalImageView.setVisibility(View.GONE);
                Log.e(TAG, "onLongClick: original bitmap is null");
            }
            return false;
        });

        originalImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (finalResult != null && !finalResult.isEmpty()) {
                    Glide.with(ApplyEffectActivity.this)
                            .load(finalResult)
                            .into(imageView);
                } else {
                    originalImageView.setVisibility(View.GONE);
                    Log.e(TAG, "onLongClick: finalImageBitmap is null");
                }
            }
            return false;
        });

        /*
         * back click listener
         */
        backLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ApplyEffectActivity.this, HomeActivity.class);
            intent.putExtra("from", "exit");
            intent.putExtra("via", via);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            path = "";
            croppedPicture = null;
        });

        /*
         * save button click listener
         */
        saveLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ApplyEffectActivity.this, SaveActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (!finalResult.isEmpty()) {
                intent.putExtra("pic", finalResult);
                intent.putExtra("from", TAG);
                intent.putExtra("via", via);
                startActivity(intent);
            } else {
                Log.e(TAG, "onCreate: save bitmap is null");
            }
        });
    }

    /**
     * Method to apply filter w.r.t filter id and get edited image URL
     *
     * @param imageUrl:     image that needs to be edited
     * @param filterIdList: list that contains filter id's against selected position
     */
    private void applyFilterAndRetrieveImage(String imageUrl, List<String> filterIdList) {
        if (ConnectionManager.getInstance().isNetworkAvailable(ApplyEffectActivity.this)) {
            alertDialog = DialogHandler.getInstance().filterProgress(ApplyEffectActivity.this, getString(R.string.convert_header),
                    getString(R.string.convert_body), "applyFilter", filterIdList.size(), alertDialog);
            if (filterIdList.isEmpty()) {
                return; // No more filters to apply
            }

            String filterId = filterIdList.remove(0); // Get the first filter ID
            Log.e("calls", "applyFilterAndRetrieveImage called ");
            Log.e(TAG, "applyFilterAndRetrieveImage: url: " + imageUrl);

            applyFilterCall = apiUtils.getAPIService(ApplyEffectActivity.this).applyFilter(Constants.AP_K, filterId, imageUrl);
            applyFilterCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<FilterResponse> call, @NonNull Response<FilterResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().status != null && !response.body().status.isEmpty()) {
                            if (response.body().status.equalsIgnoreCase("error")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            }
                            Log.e("FilProcess", "applyFilterAndRetrieveImage: response status: " + response.body().status);

                        } else {
                            Log.e("FilProcess", "applyFilterAndRetrieveImage: response status is: " + response.body().filterProcessResponse.status);
                            Log.e("FilProcess", "applyFilterAndRetrieveImage: req id is: " + response.body().filterProcessResponse.requestId);
                            getEditedImageUrl(response.body().filterProcessResponse.requestId, filterIdList);
                        }
                    } else {
                        Log.e("FilProcess", "applyFilterAndRetrieveImage: response is null");
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FilterResponse> call, @NonNull Throwable t) {
                    Log.e("FilProcess", "applyFilterAndRetrieveImage: response failed: " + t.getLocalizedMessage());
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().equalsIgnoreCase("Canceled")) {
                        DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }
                }
            });
        } else {
            DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.internet_con_msg),
                    "", "", getString(R.string.ok_label), "connectionErr", true, itemClicked -> {
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    });
        }
    }

    /**
     * Method to get final edited image based on request id
     *
     * @param requestId:    id of edited image returned in response from edit image end point
     * @param filterIdList: list of filter ids
     */
    private void getEditedImageUrl(String requestId, List<String> filterIdList) {
        Log.e("calls", "getEditedImageUrl called ");
        getFinalImageCall = apiUtils.getAPIService(ApplyEffectActivity.this).getResult(Constants.AP_K, requestId);
        getFinalImageCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FinalResultResponse> call, @NonNull Response<FinalResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().status != null && !response.body().status.isEmpty()) {
                        Log.e("FilProcess", "getEditedImageUrl: response status: " + response.body().status);
                    } else {
                        Log.e("FilProcess", "getEditedImageUrl: response status is: " + response.body().finalResultDetails.status);
                        Log.e("FilProcess", "getEditedImageUrl: url is: " + response.body().finalResultDetails.resultUrl);
                        if (response.body().finalResultDetails.status.equalsIgnoreCase("InProgress")) {
                            getEditedImageUrl(response.body().finalResultDetails.requestId, filterIdList);

                        } else if (response.body().finalResultDetails.errorCode != null) {
                            noImageView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                            if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("-1000")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", true, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });

                            } else if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("1")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            } else if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("-1020")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            } else if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("-1026")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            } else if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("-5")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            } else if (response.body().finalResultDetails.errorCode.equalsIgnoreCase("-1028")) {
                                DialogHandler.isFirstTime = true;
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), response.body().finalResultDetails.description,
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            } else if (response.body().finalResultDetails.status.equalsIgnoreCase("error") || response.body().finalResultDetails.status.equalsIgnoreCase("Bad Request")) {
                                DialogHandler.isFirstTime = true;
                                DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                        "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                            if (alertDialog != null && alertDialog.isShowing()) {
                                                alertDialog.dismiss();
                                            }
                                            startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        });
                            }

                        } else if (filterIdList.isEmpty()) {
                            noImageView.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }

                            finalResult = response.body().finalResultDetails.resultUrl;
                            saveLayout.setVisibility(View.VISIBLE);

                            Glide.with(ApplyEffectActivity.this)
                                    .load(finalResult)
                                    .centerCrop()
                                    .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(imageView);
//                            loadInterstitialAd("final", finalResult);
                            originalImageView.setVisibility(View.VISIBLE);

                        } else {
                            applyFilterAndRetrieveImage(response.body().finalResultDetails.resultUrl, filterIdList);
                        }
                    }
                } else {
                    Log.e("FilProcess", "getEditedImageUrl: response is null");
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                            "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            });
                }
            }

            @Override
            public void onFailure(@NonNull Call<FinalResultResponse> call, @NonNull Throwable t) {
                DialogHandler.isFirstTime = true;
                Log.e("FilProcess", "onFailure: getEditedImageUrl failed: " + t.getLocalizedMessage());
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().equalsIgnoreCase("Canceled")) {
                    DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                            "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            });
                }
            }
        });
    }

    /**
     * Method to load interstitial ad
     *
     * @param from: from where this method is called e.g. OnResume, OnCreate etc.
     * @param url:  Image URL to display it once ad is displayed (if it exists)
     */
    public void loadInterstitialAd(String from, String url) {
        if (!Utility.getInstance().isPremiumActive(this)) {
            InterstitialAd.load(ApplyEffectActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
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

                if (prefsManager.getAdCount() > 0 && prefsManager.getAdCount() % 3 == 0) {
                    mInterstitialAd.show(ApplyEffectActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Constants.isInterstitialVisible = true;
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.e(TAG, "Ad dismissed fullscreen content.");
                            Constants.isInterstitialVisible = false;
                            mInterstitialAd = null;
                            Log.e(TAG, "onAdDismissedFullScreenContent: from: " + from);
                            Log.e(TAG, "onAdDismissedFullScreenContent: url: " + url);
                            if (from.equalsIgnoreCase("final")) {
                                Glide.with(ApplyEffectActivity.this)
                                        .load(url)
                                        .centerCrop()
                                        .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(imageView);
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            Glide.with(ApplyEffectActivity.this)
                                    .load(url)
                                    .centerCrop()
                                    .timeout(100000)
                                    .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(imageView);
                        }
                    });
                } else {
                    Glide.with(ApplyEffectActivity.this)
                            .load(url)
                            .centerCrop()
                            .timeout(100000)
                            .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (!from.equalsIgnoreCase("resume")) {
                    Glide.with(ApplyEffectActivity.this)
                            .load(url)
                            .centerCrop()
                            .timeout(100000)
                            .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                }
            }
        } else {
            Log.e(TAG, "premium is active");
            Glide.with(ApplyEffectActivity.this)
                    .load(url)
                    .centerCrop()
                    .timeout(100000)
                    .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    /**
     * Interface implementation of Banner Ad helper class
     */
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
    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
        Log.e(TAG, "onAdFailedToLoad: ");
    }

    /**
     * to handle uCrop library intent
     *
     * @param requestCode:
     * @param resultCode:
     * @param data:
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Log.e(TAG, "onActivityResult: outer if working");
            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                Log.e(TAG, "onActivityResult: inner if working");
                final Uri resultUri = UCrop.getOutput(data);

                if (resultUri != null) {
                    Log.e(TAG, "onActivityResult: path: " + resultUri.getPath());

                    File imageFileBc = new File(resultUri.getPath());
                    if (imageFileBc.exists()) {
                        long size = imageFileBc.length() / 1024;
                        Log.e(TAG, "onActivityResult: size before: " + size);

                        try {
                            imageZipperFile = new ImageZipper(ApplyEffectActivity.this)
                                    .setQuality(80)
                                    .setMaxWidth(1024)
                                    .setMaxHeight(1024)
                                    .compressToFile(imageFileBc);
                        } catch (IOException e) {
                            Log.e(TAG, "onActivityResult: compress exception: " + e.getLocalizedMessage());
                        }

                        if (imageZipperFile != null) {
                            size = imageZipperFile.length() / 1024;

                            Log.e(TAG, "onActivityResult: size after: " + size);

                            Glide.with(ApplyEffectActivity.this)
                                    .load("")
                                    .centerCrop()
                                    .placeholder(Utility.getInstance().showLoader(ApplyEffectActivity.this, 10, 70, getResources().getColor(R.color.newpurple)))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(imageView);

                            getImageURL(imageZipperFile.getPath());

                        } else {
                            DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.image_size_msg),
                                    "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                        if (alertDialog != null && alertDialog.isShowing()) {
                                            alertDialog.dismiss();
                                        }
                                        startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    });
                        }

                    } else {
                        DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.image_size_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }

                } else {
                    Log.e(TAG, "onActivityResult: bitmap: " + bitmapResult);
                    Log.e(TAG, "onActivityResult: resultUri: null");
                    DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.image_size_msg),
                            "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            });
                }

            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                Log.e(TAG, "onActivityResult: inner else if crop error: " + cropError);
                Toast.makeText(this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            Constants.isSelectingFile = true;
        } else {
            Intent intent = new Intent(ApplyEffectActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e(TAG, "onActivityResult: from: " + from);
            if (from.equalsIgnoreCase("camera")) {
                intent.putExtra("from", "cameraAction");

            } else if (from.equalsIgnoreCase("home")) {
                intent.putExtra("from", "filter");
            }
            intent.putExtra("via", via);
            intent.putExtra("filterIds", filterIdList);
            Log.e(TAG, "onActivityResult: filter id size: " + filterIdList.size());
            startActivity(intent);
            Log.e(TAG, "onActivityResult: outer else working");
        }
    }

    /**
     * method to upload image from phone to Toon Me Server for further processing
     *
     * @param path: local path of image
     */
    @SuppressWarnings("deprecation")
    private void getImageURL(String path) {
        if (ConnectionManager.getInstance().isNetworkAvailable(ApplyEffectActivity.this)) {
            alertDialog = DialogHandler.getInstance().filterProgress(ApplyEffectActivity.this, getString(R.string.upload_image_header),
                    getString(R.string.upload_image_message), "uploadImage", filterIdList.size(), alertDialog);
            File file = new File(path);

            Log.e(TAG, "getImageURL: file is: " + file);
            Log.e(TAG, "getImageURL: path is: " + path);

            RequestBody requestFile = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), file);
            MultipartBody.Part fileToSend = MultipartBody.Part.createFormData("file1", file.getName(), requestFile);
            uploadImageCall = apiUtils.getAPIService(ApplyEffectActivity.this).uploadPicture(Constants.AP_K, fileToSend);
            uploadImageCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<UploadImageResponse> call, @NonNull Response<UploadImageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e(TAG, "onResponse: status: " + response.body().status);
                        Log.e(TAG, "onResponse: image url: " + response.body().imageUrl);
                        uploadedTempUrl = response.body().imageUrl;
                        applyFilterAndRetrieveImage(uploadedTempUrl, filterIdList);

                    } else {
                        DialogHandler.isFirstTime = true;
                        Log.e(TAG, "onResponse: response is not successful");
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UploadImageResponse> call, @NonNull Throwable t) {
                    DialogHandler.isFirstTime = true;
                    Log.e(TAG, "onFailure: response failed: " + t.getLocalizedMessage());
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().equalsIgnoreCase("Canceled")) {
                        DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }
                }
            });
        } else {
            DialogHandler.getInstance().showBottomSheet(ApplyEffectActivity.this, getString(R.string.app_name), getString(R.string.internet_con_msg),
                    "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        startActivity(new Intent(ApplyEffectActivity.this, HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    });
        }
    }

    /**
     * OnResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: ");

        if (!Utility.getInstance().isPremiumActive(this)) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);

        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        loadInterstitialAd("resume", "");
        Constants.isSelectingFile = false;
    }

    /**
     * OnDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        path = "";
        croppedPicture = null;
        if (uploadImageCall != null && !uploadImageCall.isCanceled()) {
            Log.e(TAG, "onDestroy: uploadImageCall is cancelled");
            uploadImageCall.cancel();

        } else {
            Log.e(TAG, "onDestroy: uploadImageCall obj is null");
        }

        if (applyFilterCall != null && !applyFilterCall.isCanceled()) {
            Log.e(TAG, "onDestroy: applyFilterCall is cancelled");
            applyFilterCall.cancel();

        } else {
            Log.e(TAG, "onDestroy: applyFilterCall obj is null");
        }

        if (getFinalImageCall != null && !getFinalImageCall.isCanceled()) {
            Log.e(TAG, "onDestroy: getFinalImageCall is cancelled");
            getFinalImageCall.cancel();

        } else {
            Log.e(TAG, "onDestroy: getFinalImageCall obj is null");
        }
    }
}