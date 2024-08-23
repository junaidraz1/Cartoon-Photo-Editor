package com.miczon.cartoonme.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.miczon.cartoonme.Listeners.ActionSheetClickListener;
import com.miczon.cartoonme.Listeners.DialogClickListener;
import com.miczon.cartoonme.Listeners.RecyclerViewClickListener;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.ShowNativeAd;
import com.miczon.cartoonme.Utils.Utility;
import com.sasank.roundedhorizontalprogress.RoundedHorizontalProgressBar;
import com.willy.ratingbar.ScaleRatingBar;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class DialogHandler {

    String TAG = "DialogHandler";
    public static DialogHandler mInstance = null;
    AlertDialog alertDialog;
    public static boolean isFirstTime = true;
    public boolean isFirstApply = true;
    int call, dialogWidth;
    ;

    /**
     * Method for singleton object implementation
     *
     * @return: object of class DialogHandler
     */
    public static DialogHandler getInstance() {
        if (mInstance == null) {
            mInstance = new DialogHandler();
        }
        return mInstance;
    }

    /**
     * Method to display bottom sheet
     *
     * @param activity:                 from where it is called
     * @param title:                    title to display on bottom sheet's top
     * @param messageBody:              message to display in bottom sheet
     * @param btnLeftTxt:               left button text
     * @param btnRightTxt:              right button text
     * @param btnSingleTxt:             button text (in case of single button)
     * @param isFrom:                   from where sheet is called
     * @param isCancelable:             is it cancelable or not
     * @param actionSheetClickListener: click listener to handle click events
     */
    public void showBottomSheet(Activity activity, String title, String messageBody, String btnLeftTxt, String btnRightTxt, String btnSingleTxt,
                                String isFrom, boolean isCancelable, ActionSheetClickListener actionSheetClickListener) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.layout_bottomsheet_dialog);
        bottomSheetDialog.setCancelable(isCancelable);
        LinearLayout rightLayout = bottomSheetDialog.findViewById(R.id.ll_save);
        LinearLayout leftLayout = bottomSheetDialog.findViewById(R.id.ll_getFilters);
        LinearLayout buttonLayout = bottomSheetDialog.findViewById(R.id.ll_actionButtons);
        LinearLayout singleButtonLayout = bottomSheetDialog.findViewById(R.id.ll_singleBtn);
//        View horizontalView = bottomSheetDialog.findViewById(R.id.v_horizontalLine);
//        LinearLayout unifiedAdLayout = bottomSheetDialog.findViewById(R.id.ll_unified_ad_layout);
        TextView tvTitle = bottomSheetDialog.findViewById(R.id.tv_title);
        TextView tvMessage = bottomSheetDialog.findViewById(R.id.tv_message);
        TextView tvLeftBtn = bottomSheetDialog.findViewById(R.id.tv_btnLeft);
        TextView tvRightBtn = bottomSheetDialog.findViewById(R.id.tv_btnRight);
        TextView tvSingleBtn = bottomSheetDialog.findViewById(R.id.tv_btnSingle);
        ProgressBar progressBar = bottomSheetDialog.findViewById(R.id.pb_progressBar);
        ProgressBar progressBar2 = bottomSheetDialog.findViewById(R.id.pb_progressBar2);
        NativeAdView nativeAdView = bottomSheetDialog.findViewById(R.id.ad_view);
        RelativeLayout adaptiveLayout = bottomSheetDialog.findViewById(R.id.rl_adContainer);
        FrameLayout adContainer = bottomSheetDialog.findViewById(R.id.fl_adContainer);
        FrameLayout nativeAdContainer = bottomSheetDialog.findViewById(R.id.native_ad_container);
        TextView tvLoadingAd = bottomSheetDialog.findViewById(R.id.tv_loadingAd);
        ShowNativeAd nativeAdHelper = new ShowNativeAd(activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView;

        if (rightLayout != null && leftLayout != null && buttonLayout != null && singleButtonLayout != null &&
                tvTitle != null && tvMessage != null && tvLeftBtn != null && tvSingleBtn != null && tvRightBtn != null &&
                progressBar != null && progressBar2 != null && nativeAdView != null && nativeAdContainer != null && adaptiveLayout != null &&
                adContainer != null && tvLoadingAd != null) {

            tvTitle.setText(title);
            tvMessage.setText(messageBody);

            if (isFrom.equalsIgnoreCase("delete")) {
                buttonLayout.setVisibility(View.GONE);
                singleButtonLayout.setVisibility(View.VISIBLE);
                tvSingleBtn.setText(btnSingleTxt);
                adaptiveLayout.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
                adView = new AdView(activity.getApplicationContext());
                adView.setAdUnitId(Constants.AdMob_Main_Adaptive_Banner_Ad_Id);
                adView.setAdSize(AdSize.BANNER);

                if (!Utility.mInstance.isPremiumActive(activity)) {
                    adContainer.addView(adView);
                    adView.loadAd(adRequest);

                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            Log.e(TAG, "onAdClicked: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdOpened();
                            Log.e(TAG, "onAdaptiveAdLoaded: ");
                            tvLoadingAd.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);

                        }
                    });
                } else {
                    tvLoadingAd.setVisibility(View.GONE);
                    adaptiveLayout.setVisibility(View.GONE);
                    Log.e(TAG, "showBottomSheet: premium is active:");
                }

            } else if (isFrom.equalsIgnoreCase("loadingFilters")) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar2.setVisibility(View.GONE);
                buttonLayout.setVisibility(View.GONE);

            } else if (isFrom.equalsIgnoreCase("connectionErr")) {
                progressBar2.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    progressBar2.setVisibility(View.GONE);
                    singleButtonLayout.setVisibility(View.VISIBLE);
                    tvSingleBtn.setText(btnSingleTxt);
                }, Constants.FILTER_PROGRESS_TIMER);
                buttonLayout.setVisibility(View.GONE);

            } else if (isFrom.equalsIgnoreCase("backpress")) {
                buttonLayout.setVisibility(View.VISIBLE);
                adaptiveLayout.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
                adView = new AdView(activity.getApplicationContext());
                adView.setAdUnitId(Constants.AdMob_Main_Adaptive_Banner_Ad_Id);
                adView.setAdSize(BannerAdManager.getAdSize(activity));

                if (!Utility.mInstance.isPremiumActive(activity)) {
                    adContainer.addView(adView);
                    adView.loadAd(adRequest);

                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            Log.e(TAG, "onAdClicked: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdOpened();
                            Log.e(TAG, "onAdaptiveAdLoaded: ");
                            tvLoadingAd.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);

                        }
                    });
                } else {
                    tvLoadingAd.setVisibility(View.GONE);
                    adaptiveLayout.setVisibility(View.GONE);
                    Log.e(TAG, "showBottomSheet: premium is active:");
                }
            } else if (isFrom.equalsIgnoreCase("deletecon")) {
                buttonLayout.setVisibility(View.VISIBLE);
                adaptiveLayout.setVisibility(View.VISIBLE);
                nativeAdContainer.setVisibility(View.GONE);
                adView = new AdView(activity.getApplicationContext());
                adView.setAdUnitId(Constants.AdMob_Main_Adaptive_Banner_Ad_Id);
                adView.setAdSize(AdSize.BANNER);

                if (!Utility.mInstance.isPremiumActive(activity)) {
                    adContainer.addView(adView);
                    adView.loadAd(adRequest);

                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            Log.e(TAG, "onAdClicked: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdOpened();
                            Log.e(TAG, "onAdaptiveAdLoaded: ");
                            tvLoadingAd.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);

                        }
                    });
                } else {
                    tvLoadingAd.setVisibility(View.GONE);
                    adaptiveLayout.setVisibility(View.GONE);
                    Log.e(TAG, "showBottomSheet: premium is active:");
                }
            } else {
                adaptiveLayout.setVisibility(View.GONE);
                nativeAdContainer.setVisibility(View.VISIBLE);
                nativeAdHelper.showAdMobNativeBannerAd(nativeAdView, nativeAdContainer, Constants.AdMob_Main_Native_Advance_Ad_Id);
            }

            tvLeftBtn.setText(btnLeftTxt);
            tvRightBtn.setText(btnRightTxt);

            rightLayout.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                actionSheetClickListener.sheetClick("right");
            });

            leftLayout.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                actionSheetClickListener.sheetClick("left");
            });

            singleButtonLayout.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                actionSheetClickListener.sheetClick("single");
            });

        }
        bottomSheetDialog.show();
    }

    public void showDeleteBottomSheet(Activity activity, String isFrom, boolean isCancelable, ActionSheetClickListener actionSheetClickListener) {
        BottomSheetDialog bottomSheetDialog;
        bottomSheetDialog = new BottomSheetDialog(activity, R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet_delete_image);
        bottomSheetDialog.setCancelable(isCancelable);

        CardView delConfirmationLayout = bottomSheetDialog.findViewById(R.id.ll_imageDelConfirmation);
        CardView delSuccessLayout = bottomSheetDialog.findViewById(R.id.ll_imageDelSuccess);
        LinearLayout deleteImageLayout = bottomSheetDialog.findViewById(R.id.ll_delete);
        LinearLayout cancelLayout = bottomSheetDialog.findViewById(R.id.ll_cancel);
        LinearLayout dismissSheetLayout = bottomSheetDialog.findViewById(R.id.ll_done);

        if (delConfirmationLayout != null && delSuccessLayout != null) {
            if (isFrom.equalsIgnoreCase("delImage")) {
                delConfirmationLayout.setVisibility(View.VISIBLE);
                delSuccessLayout.setVisibility(View.GONE);

            } else if (isFrom.equalsIgnoreCase("delSuccess")) {
                delConfirmationLayout.setVisibility(View.GONE);
                delSuccessLayout.setVisibility(View.VISIBLE);
            }
        }

        if (deleteImageLayout != null && cancelLayout != null && dismissSheetLayout != null) {

            cancelLayout.setOnClickListener(v -> bottomSheetDialog.dismiss());

            deleteImageLayout.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                actionSheetClickListener.sheetClick("1");
            });

            dismissSheetLayout.setOnClickListener(v -> bottomSheetDialog.dismiss());

        }


        bottomSheetDialog.show();
    }

    /**
     * Method to display dialog when user tries to exit the app
     *
     * @param activity:            from where this is callled
     * @param isCancelable:        is it cancelable
     * @param dialogClickListener: to handle click events
     */
    public void exitDialog(Activity activity, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater factory = LayoutInflater.from(activity);

        final View dialogView = factory.inflate(R.layout.layout_exit_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout cancelLayout = dialogView.findViewById(R.id.ll_getFilters);
        LinearLayout visitLayout = dialogView.findViewById(R.id.ll_okay);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);

        cancelLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        visitLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));

        ivClose.setOnClickListener(v -> dialogClickListener.onButtonClick("2", "", "", alertDialog));

        try {
            alertDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    public void shareImagePermissionDialog(Activity activity, String bodyText, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater factory = LayoutInflater.from(activity);

        final View dialogView = factory.inflate(R.layout.layout_dialog_sharing_permission, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout cancelLayout = dialogView.findViewById(R.id.ll_cancel);
        LinearLayout shareLayout = dialogView.findViewById(R.id.ll_share);
        TextView tvBodyText = dialogView.findViewById(R.id.tv_msg);

        tvBodyText.setText(bodyText);

        cancelLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        shareLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));


        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    public void choosePictureDialog(Context context, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_dialog_choose_picture, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout cameraLayout = dialogView.findViewById(R.id.ll_cameraLayout);
        LinearLayout galleryLayout = dialogView.findViewById(R.id.ll_galleryLayout);
        LottieAnimationView animationView = dialogView.findViewById(R.id.animation_view);

        animationView.playAnimation();

        cameraLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        galleryLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    public AlertDialog filterProgress(Context context, String title, String body, String from, int size, AlertDialog existingDialog) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_dialog_filter_progress, null);

        Log.e(TAG, "filterProgress: From: " + from);

        if (existingDialog != null && existingDialog.isShowing() && !isFirstApply) {
            Log.e(TAG, "filterProgress: start if");
            alertDialog = existingDialog;

        } else if (from.equalsIgnoreCase("uploadImage")) {
            Log.e(TAG, "filterProgress: start else if 1");
            alertDialog = dialog.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setView(dialogView);

            if (!isFirstApply) {
                isFirstApply = true;
            }

        } else if (from.equalsIgnoreCase("applyFilter") && isFirstApply && alertDialog.isShowing()) {
            Log.e(TAG, "filterProgress: start else if 2");
            alertDialog.dismiss();
            alertDialog = dialog.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setView(dialogView);
            isFirstApply = false;
        }

        alertDialog.setCancelable(false);

        ImageView ivAnimate1 = dialogView.findViewById(R.id.iv1);
        ImageView ivAnimate2 = dialogView.findViewById(R.id.iv2);
        ImageView ivAnimate3 = dialogView.findViewById(R.id.iv3);
        TextView tvHeader = dialogView.findViewById(R.id.tv_title);
        TextView tvBody = dialogView.findViewById(R.id.tv_body);
        RoundedHorizontalProgressBar progressBar = dialogView.findViewById(R.id.progress_bar_1);

        Log.e(TAG, "filterProgress: title: " + title);
        Log.e(TAG, "filterProgress: body: " + body);

        tvHeader.setText(title);
        tvBody.setText(body);
        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(Animation.INFINITE)
                .playOn(tvBody);

        TranslateAnimation topToBottom = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -400);

        topToBottom.setDuration(10000);
        topToBottom.setFillAfter(true);
        topToBottom.setInterpolator(new LinearInterpolator());
        topToBottom.setRepeatCount(Animation.INFINITE);
        topToBottom.setRepeatMode(Animation.REVERSE);
        ivAnimate1.startAnimation(topToBottom);
        ivAnimate3.startAnimation(topToBottom);

        TranslateAnimation bottomToTop = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 400);

        bottomToTop.setDuration(10000);
        bottomToTop.setFillAfter(true);
        bottomToTop.setInterpolator(new LinearInterpolator());
        bottomToTop.setRepeatCount(Animation.INFINITE);
        bottomToTop.setRepeatMode(Animation.REVERSE);
        ivAnimate2.startAnimation(bottomToTop);

        if (from.equalsIgnoreCase("uploadImage")) {
            progressBar.setVisibility(View.GONE);

        } else if (from.equalsIgnoreCase("applyFilter")) {
            Log.e(TAG, "filterProgress: applyFilter else if working");
            progressBar.setVisibility(View.VISIBLE);
            Log.e(TAG, "showBottomSheeet: size: " + size);
            for (int i = 0; i < size; i++) {
                call = i;
            }

            if (isFirstTime) {
                Log.e(TAG, "showBottomSheeet: first time if");
                progressBar.animateProgress(15000, 0, 150);
                isFirstTime = false;
            }


            Log.e(TAG, "showBottomSheeet: call: " + call);
            Log.e(TAG, "showBottomSheeet: size: " + size);
            if (call == 0) {
                Log.e(TAG, "showBottomSheeet: call if working");
                isFirstTime = true;
            }

        }

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
        return alertDialog;
    }

    /**
     * Method to display Pirated Version Warning
     */
    public void showPiracyCheckerDialog(Context context, RecyclerViewClickListener recyclerViewClickListener) {
        Dialog alert = new Dialog(context);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.appcheckerrdialog);
        alert.setCancelable(false);
        Button accept = alert.findViewById(R.id.accept);
        ImageView close = alert.findViewById(R.id.close);

        accept.setOnClickListener(v -> recyclerViewClickListener.itemClick(0, "", ""));

        close.setOnClickListener(v -> recyclerViewClickListener.itemClick(1, "", ""));
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        alert.show();
    }

    public void showSaveDialog(Context context, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_save_image_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout exitLayout = dialogView.findViewById(R.id.ll_exit);
        LinearLayout continueLayout = dialogView.findViewById(R.id.ll_continue);

        exitLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        continueLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    /**
     * Method to display rate us dialog
     *
     * @param context:             from where it is called
     * @param isCancelable:        is it cancelable
     * @param dialogClickListener: to handle click events
     */
    public void rateUsDialog(Context context, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_rateus_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout visitLayout = dialogView.findViewById(R.id.ll_okay);
        ScaleRatingBar ratingBar = dialogView.findViewById(R.id.rb_RatingBar);
        TextView tvSingleButton = dialogView.findViewById(R.id.tv_btnRight);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);

        ratingBar.setOnRatingChangeListener((ratingBar1, rating, fromUser) -> {
            if (rating < 4) {
                visitLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkYellow)));
                tvSingleButton.setText(context.getString(R.string.feedback_label));
            } else {
                visitLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.newpurple)));
                tvSingleButton.setText(context.getString(R.string.rate_us_label));
            }
        });

        visitLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", String.valueOf(ratingBar.getRating()), alertDialog));

        ivClose.setOnClickListener(v -> dialogClickListener.onButtonClick("2", "", String.valueOf(ratingBar.getRating()), alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    /**
     * Method to display dialog before displaying rewarded interstitial ad
     *
     * @param context:             from where it is called
     * @param isCancelable:        is it cancelable
     * @param dialogClickListener: to handle click events
     */
    public void displayPreRewardAdDialog(Context context, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_rewarded_info_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout watchAdLayout = dialogView.findViewById(R.id.ll_okay);
        LinearLayout getFiltersLayout = dialogView.findViewById(R.id.ll_getFilters);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);

        watchAdLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        getFiltersLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));

        ivClose.setOnClickListener(v -> dialogClickListener.onButtonClick("2", "", "", alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "exitDialog EXP: " + e);
        }
    }

    /**
     * To display rename dialog when user tries to rename images
     *
     * @param context:             from where it is called
     * @param isCancelable:        is it cancelable
     * @param imagePath:           path of image to be renamed
     * @param imageName:           new image name
     * @param errMsg:              flag to display error message if image with same name already exists
     * @param dialogClickListener: to handle click events
     */
    @SuppressLint("SetTextI18n")
    public void showRenameDialog(Context context, boolean isCancelable, String imagePath, String imageName, boolean errMsg, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_rename_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvBtnLeft = dialogView.findViewById(R.id.tv_btnLeft);
        TextView tvBtnRight = dialogView.findViewById(R.id.tv_btnRight);
        TextView tvErrorMsg = dialogView.findViewById(R.id.tv_errMsg);
        EditText etBody = dialogView.findViewById(R.id.et_message);
        LinearLayout cancelLayout = dialogView.findViewById(R.id.ll_getFilters);
        LinearLayout saveLayout = dialogView.findViewById(R.id.ll_save);
        LinearLayout retryLayout = dialogView.findViewById(R.id.ll_retry);
        LinearLayout overwriteLayout = dialogView.findViewById(R.id.ll_overwrite);
        LinearLayout actionBtnLayout = dialogView.findViewById(R.id.ll_actionButtons);
        LinearLayout errActionLayout = dialogView.findViewById(R.id.ll_errActionButtons);
        RelativeLayout errorLayout = dialogView.findViewById(R.id.rl_errMsg);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);

        tvTitle.setText(R.string.rename_file_label);
        etBody.setText(Utility.extractFileName(imagePath));

        Log.e(TAG, "showRenameDialog: text: " + etBody.getText().toString().trim());

        if (errMsg) {
            etBody.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errActionLayout.setVisibility(View.VISIBLE);
            actionBtnLayout.setVisibility(View.GONE);
            tvBtnLeft.setText(context.getString(R.string.retry_label));
            tvBtnRight.setText(context.getString(R.string.overwrite_label));
            tvErrorMsg.setText(context.getString(R.string.overwrite_body1) + " '" + imageName + "' " + context.getString(R.string.overwrite_body2));

        } else {
            etBody.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            errActionLayout.setVisibility(View.GONE);
            actionBtnLayout.setVisibility(View.VISIBLE);
            tvBtnLeft.setText(context.getString(R.string.cancel_label));
            tvBtnRight.setText(context.getString(R.string.save_label));
        }

        cancelLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        saveLayout.setOnClickListener(v -> {
            if (etBody.getText().toString().isEmpty()) {
                etBody.setError(context.getString(R.string.rename_err_msg));

            } else {
                dialogClickListener.onButtonClick("1", imagePath, etBody.getText().toString().trim(), alertDialog);
                Log.e(TAG, "showRenameDialog: text: " + etBody.getText().toString().trim());
            }
        });

        ivClose.setOnClickListener(v -> dialogClickListener.onButtonClick("2", "", "", alertDialog));

        retryLayout.setOnClickListener(v -> {
            etBody.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            errActionLayout.setVisibility(View.GONE);
            actionBtnLayout.setVisibility(View.VISIBLE);
            tvBtnLeft.setText(context.getString(R.string.cancel_label));
            tvBtnRight.setText(context.getString(R.string.save_label));
        });

        overwriteLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("3", imagePath, etBody.getText().toString().trim(), alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "showRenameDialog EXP: " + e);
        }
    }

    public void renameSuccessDialog(Context context, boolean isCancelable) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_rename_success, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout dismissLayout = dialogView.findViewById(R.id.ll_okay);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);

        dismissLayout.setOnClickListener(v -> alertDialog.dismiss());

        ivClose.setOnClickListener(v -> alertDialog.dismiss());

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "showRenameDialog EXP: " + e);
        }
    }

    /**
     * Method to display agreement dialog
     *
     * @param context:             from where it is called
     * @param isCancelable:        is it cancelable
     * @param dialogClickListener: to handle click events
     */
    public void showAgreementDialog(Context context, boolean isCancelable, DialogClickListener dialogClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View dialogView = factory.inflate(R.layout.layout_useragreement_dialog, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        Button btnAccept = dialogView.findViewById(R.id.btn_accept);
        TextView tvTermsOfService = dialogView.findViewById(R.id.tv_termsOfService);

        tvTermsOfService.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        btnAccept.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));


        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "showRenameDialog EXP: " + e);
        }
    }

    /**
     * Method to display privacy policy dialog
     *
     * @param activity:            from where it is called
     * @param isCancelable:        is it cancelable
     * @param dialogClickListener: to handle click events
     */
    public void privacyDialog(Activity activity, boolean isCancelable, DialogClickListener dialogClickListener) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater factory = LayoutInflater.from(activity);

        final View dialogView = factory.inflate(R.layout.layout_dialog_privacy, null);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setView(dialogView);
        alertDialog.setCancelable(isCancelable);

        LinearLayout cancelLayout = dialogView.findViewById(R.id.ll_getFilters);
        LinearLayout visitLayout = dialogView.findViewById(R.id.ll_okay);
        LinearLayout unifiedAdLayout = dialogView.findViewById(R.id.ll_unified_ad_layout);
        TextView tvPrivacyVisit = dialogView.findViewById(R.id.tv_privacyPolicy);
        ImageView ivClose = dialogView.findViewById(R.id.iv_close);
        NativeAdView nativeAdView = dialogView.findViewById(R.id.ad_view);

        boolean isPremium = Utility.getInstance().isPremiumActive(activity);

        ShowNativeAd nativeAdHelper = new ShowNativeAd(activity);

        nativeAdHelper.showAdMobNativeAd(nativeAdView, unifiedAdLayout, Constants.AdMob_Main_Native_Advance_Ad_Id);

        if (!isPremium) {
            unifiedAdLayout.setVisibility(View.VISIBLE);
        } else {
            unifiedAdLayout.setVisibility(View.GONE);
        }

        cancelLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("0", "", "", alertDialog));

        visitLayout.setOnClickListener(v -> dialogClickListener.onButtonClick("1", "", "", alertDialog));

        tvPrivacyVisit.setOnClickListener(v -> dialogClickListener.onButtonClick("2", "", "", alertDialog));

        ivClose.setOnClickListener(v -> dialogClickListener.onButtonClick("3", "", "", alertDialog));

        try {
            alertDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "privacyDialog EXP: " + e);
        }
    }
}
