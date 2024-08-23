package com.miczon.cartoonme.Activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.makeramen.roundedimageview.RoundedImageView;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class SaveActivity extends BaseActivity implements BannerAdLoadListener {

    String TAG = "SaveActivity", path = "", from = "", via = "", savedImagePath = "";
    RelativeLayout backLayout, homeLayout, adDisplayLayout;
    TextView tvHeader;
    Button saveImageBtn;
    //    AdjustableImageView imageView;
    RoundedImageView imageView;
    ImageView ivInstagram, ivWhatsapp, ivFacebook, ivMore;
    KonfettiView konfettiView;
    FrameLayout adContainer;
    PrefsManager prefsManager;

    Intent intent;
    Uri shareImageUri;
    Bitmap saveBitmap;

//    AdView adView;
    BannerAdManager bannerAdManager;
    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

    boolean isPhotoSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        imageView = findViewById(R.id.imageView);
        backLayout = findViewById(R.id.rl_back);
        homeLayout = findViewById(R.id.rl_home);
        tvHeader = findViewById(R.id.tv_header);
        saveImageBtn = findViewById(R.id.btn_save);
        ivInstagram = findViewById(R.id.iv_instagram);
        ivWhatsapp = findViewById(R.id.iv_whatsapp);
        ivFacebook = findViewById(R.id.iv_facebook);
        ivMore = findViewById(R.id.iv_more);
        konfettiView = findViewById(R.id.konfettiView);
        adDisplayLayout = findViewById(R.id.rl_adLayout);
        adContainer = findViewById(R.id.fl_adContainer);

        prefsManager = new PrefsManager(SaveActivity.this);
        bannerAdManager = new BannerAdManager(this, this);
        adRequest = new AdRequest.Builder().build();
//        adView = new AdView(SaveActivity.this);
//
//        bannerAdManager.setAdSize(adView);

        if (!Utility.getInstance().isPremiumActive(this)) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
//            adContainer.addView(adView);
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
            Toast.makeText(this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onCreate: Intent is null");

        } else {
            path = intent.getStringExtra("pic");
            from = intent.getStringExtra("from");
            via = intent.getStringExtra("via");

            Log.e(TAG, "onCreate: from: " + from);
            Log.e(TAG, "onCreate: path: " + path);

            tvHeader.setText(getResources().getString(R.string.app_name));

            if (from.equalsIgnoreCase("SavedFileActivity")) {
                saveImageBtn.setVisibility(View.GONE);
                backLayout.setVisibility(View.VISIBLE);
                isPhotoSaved = true;
            }

            Glide.with(SaveActivity.this)
                    .load(path)
                    .load(path)
                    .into(imageView);
        }

        saveImageBtn.setOnClickListener(v -> loadInterstitialAd("saved"));

        ivInstagram.setOnClickListener(v -> DialogHandler.getInstance().shareImagePermissionDialog(this, getString(R.string.share_instagram), true, (status, message, data, alertDialog) -> {
            if (status.equalsIgnoreCase("0")) {
                alertDialog.dismiss();
            } else {
                alertDialog.dismiss();
                if (from.equalsIgnoreCase("ApplyEffectActivity")) {
                    if (savedImagePath.isEmpty()) {
                        new Thread(() -> {
                            saveBitmap = Utility.getInstance().loadImageFromPath(SaveActivity.this, path);
                            runOnUiThread(() -> {
                                if (saveBitmap != null) {
                                    savedImagePath = Utility.getInstance().saveImageToGallery(SaveActivity.this, saveBitmap);
                                    isPhotoSaved = true;
                                    saveImageBtn.setText(R.string.saved_label);
                                    shareImageToSocial(savedImagePath, "com.instagram.android", "instagram");
                                }
                            });
                        }).start();
                    } else {
                        Log.e(TAG, "onCreate: uri: " + savedImagePath);
                        shareImageToSocial(savedImagePath, "com.instagram.android", "instagram");
                    }
                } else if (from.equalsIgnoreCase("SavedFileActivity")) {
                    Uri uri = Utility.getInstance().getImageContentUri(SaveActivity.this, path);
                    Log.e(TAG, "onCreate: uri: " + uri);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setPackage("com.instagram.android"); // Package name for Instagram

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Instagram not installed or no handler for the intent
                        Toast.makeText(SaveActivity.this, R.string.insta_msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }));

        ivWhatsapp.setOnClickListener(v -> DialogHandler.getInstance().shareImagePermissionDialog(this, getString(R.string.share_whatsapp), true, (status, message, data, alertDialog) -> {
            if (status.equalsIgnoreCase("0")) {
                alertDialog.dismiss();
            } else {
                alertDialog.dismiss();
                if (from.equalsIgnoreCase("ApplyEffectActivity")) {
                    if (savedImagePath.isEmpty()) {
                        new Thread(() -> {
                            saveBitmap = Utility.getInstance().loadImageFromPath(SaveActivity.this, path);
                            runOnUiThread(() -> {
                                if (saveBitmap != null) {
                                    savedImagePath = Utility.getInstance().saveImageToGallery(SaveActivity.this, saveBitmap);
                                    isPhotoSaved = true;
                                    saveImageBtn.setText(R.string.saved_label);
                                    if (isWhatsAppInstalled(SaveActivity.this)) {
                                        shareImageToSocial(savedImagePath, "com.whatsapp", "whatsapp");

                                    } else if (isWhatsAppBInstalled(SaveActivity.this)) {
                                        shareImageToSocial(savedImagePath, "com.whatsapp.w4b", "whatsapp");
                                    }
                                }
                            });
                        }).start();


                    } else {
                        Log.e(TAG, "onCreate: uri: " + savedImagePath);
                        if (isWhatsAppInstalled(SaveActivity.this)) {
                            shareImageToSocial(savedImagePath, "com.whatsapp", "whatsapp");

                        } else if (isWhatsAppBInstalled(SaveActivity.this)) {
                            shareImageToSocial(savedImagePath, "com.whatsapp.w4b", "whatsapp");
                        }
                    }
                } else if (from.equalsIgnoreCase("SavedFileActivity")) {
                    Uri uri = Uri.parse(path);
                    Log.e(TAG, "onCreate: uri: " + uri);
                    if (isWhatsAppInstalled(SaveActivity.this)) {
                        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                        whatsappIntent.setType("image/*");
                        whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        whatsappIntent.setPackage("com.whatsapp");
                        startActivity(whatsappIntent);

                    } else if (isWhatsAppBInstalled(SaveActivity.this)) {
                        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                        whatsappIntent.setType("image/*");
                        whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        whatsappIntent.setPackage("com.whatsapp.w4b");
                        startActivity(whatsappIntent);

                    } else {
                        Toast.makeText(SaveActivity.this, R.string.whatsapp_msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }));

        ivFacebook.setOnClickListener(v -> DialogHandler.getInstance().shareImagePermissionDialog(this, getString(R.string.share_facebook), true, (status, message, data, alertDialog) -> {
            if (status.equalsIgnoreCase("0")) {
                alertDialog.dismiss();
            } else {
                alertDialog.dismiss();
                if (from.equalsIgnoreCase("ApplyEffectActivity")) {
                    if (savedImagePath.isEmpty()) {
                        new Thread(() -> {
                            saveBitmap = Utility.getInstance().loadImageFromPath(SaveActivity.this, path);
                            runOnUiThread(() -> {
                                if (saveBitmap != null) {
                                    savedImagePath = Utility.getInstance().saveImageToGallery(SaveActivity.this, saveBitmap);
                                    isPhotoSaved = true;
                                    saveImageBtn.setText(R.string.saved_label);
                                    shareImageToSocial(savedImagePath, "com.facebook.katana", "facebook");
                                }
                            });
                        }).start();

                    } else {
                        Log.e(TAG, "onCreate: uri: " + savedImagePath);
                        shareImageToSocial(savedImagePath, "com.facebook.katana", "facebook");
                    }
                } else if (from.equalsIgnoreCase("SavedFileActivity")) {
                    Uri uri = Utility.mInstance.getImageContentUri(SaveActivity.this, path);
                    if (uri != null) {
                        Intent facebookIntent = new Intent(Intent.ACTION_SEND);
                        facebookIntent.setType("image/*");
                        facebookIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        facebookIntent.setPackage("com.facebook.katana"); // Package name for Facebook

                        try {
                            startActivity(facebookIntent);
                        } catch (ActivityNotFoundException ex) {
                            // Facebook not installed or no handler for the intent
                            Toast.makeText(SaveActivity.this, R.string.facebook_msg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SaveActivity.this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }));

        ivMore.setOnClickListener(v -> {
            if (from.equalsIgnoreCase("ApplyEffectActivity")) {
                if (savedImagePath.isEmpty()) {
                    new Thread(() -> {
                        saveBitmap = Utility.getInstance().loadImageFromPath(SaveActivity.this, path);
                        runOnUiThread(() -> {
                            if (saveBitmap != null) {
                                savedImagePath = Utility.getInstance().saveImageToGallery(SaveActivity.this, saveBitmap);
                                isPhotoSaved = true;
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(savedImagePath));
                                shareIntent.setType("image/*");
                                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    startActivity(shareIntent);

                                } else {
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
                                }
                            } else {
                                Log.e(TAG, "onClick: save bitmap is null");
                            }
                        });
                    }).start();
                } else {
                    Log.e(TAG, "onCreate: uri: " + savedImagePath);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(savedImagePath));
                    shareIntent.setType("image/*");
                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        startActivity(shareIntent);

                    } else {
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
                    }
                }
            } else if (from.equalsIgnoreCase("SavedFileActivity")) {
                shareImage(path);
            }
        });

        homeLayout.setOnClickListener(v -> loadInterstitialAd("home"));

        backLayout.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Method to load and display interstitial add
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
        if (!Utility.getInstance().isPremiumActive(this)) {
            InterstitialAd.load(SaveActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
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
                    mInterstitialAd.show(SaveActivity.this);
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
                                saveImage();
                            } else if (from.equalsIgnoreCase("home")) {
                                navigateToHome();
                            } else if (from.equalsIgnoreCase("continue")) {
                                Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                                intent.putExtra("via", via);
                                intent.putExtra("from", "save");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("saved")) {
                                saveImage();
                            } else if (from.equalsIgnoreCase("home")) {
                                navigateToHome();
                            } else if (from.equalsIgnoreCase("continue")) {
                                Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                                intent.putExtra("via", via);
                                intent.putExtra("from", "save");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        }
                    });
                } else {
                    if (from.equalsIgnoreCase("saved")) {
                        saveImage();
                    } else if (from.equalsIgnoreCase("home")) {
                        navigateToHome();
                    } else if (from.equalsIgnoreCase("continue")) {
                        Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                        intent.putExtra("via", via);
                        intent.putExtra("from", "save");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (from.equalsIgnoreCase("saved")) {
                    saveImage();
                } else if (from.equalsIgnoreCase("home")) {
                    navigateToHome();
                } else if (from.equalsIgnoreCase("continue")) {
                    Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                    intent.putExtra("via", via);
                    intent.putExtra("from", "save");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
            }
        } else {
            Log.e(TAG, "No Premium");
            if (from.equalsIgnoreCase("saved")) {
                saveImage();
            } else if (from.equalsIgnoreCase("home")) {
                navigateToHome();
            } else if (from.equalsIgnoreCase("continue")) {
                Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                intent.putExtra("via", via);
                intent.putExtra("from", "save");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        }
    }

    private void saveImage() {
        if (!isPhotoSaved) {
            new Thread(() -> {
                saveBitmap = Utility.getInstance().loadImageFromPath(SaveActivity.this, path);
                runOnUiThread(() -> {
                    if (saveBitmap != null) {
                        savedImagePath = Utility.getInstance().saveImageToGallery(SaveActivity.this, saveBitmap);
                        if (savedImagePath != null && !savedImagePath.isEmpty()) {
                            isPhotoSaved = true;
                            saveImageBtn.setText(R.string.saved_label);
                            EmitterConfig emitterConfig = new Emitter(1L, TimeUnit.SECONDS).perSecond(150);
                            Party party = new PartyFactory(emitterConfig)
                                    .angle(270)
                                    .spread(90)
                                    .setSpeedBetween(1f, 5f)
                                    .timeToLive(2000L)
                                    .shapes(new Shape.Rectangle(0.2f))
                                    .sizes(new Size(17, 5f, 0.2f))
                                    .position(1.0, 0.0, 0.0, 1.0)  //bottom to top
                                    .build();
                            konfettiView.start(party);
                            DialogHandler.getInstance().showSaveDialog(SaveActivity.this, true, (status, message, data, alertDialog) -> {
                                if (status.equalsIgnoreCase("0")) {
                                    alertDialog.dismiss();
                                    startActivity(new Intent(SaveActivity.this, HomeActivity.class));

                                } else if (status.equalsIgnoreCase("1")) {
                                    alertDialog.dismiss();
                                    loadInterstitialAd("continue");
                                }
                            });
                        } else {
                            Toast.makeText(SaveActivity.this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e(TAG, "onCreate: bitmap is null");
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, R.string.saved_body, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        if (!isPhotoSaved) {
            DialogHandler.getInstance().showBottomSheet(SaveActivity.this, getString(R.string.app_name), getString(R.string.exit_warning_msg),
                    getString(R.string.cancel_label), getString(R.string.ok_label), "", "backPress", true, itemClicked -> {
                        if (itemClicked.equalsIgnoreCase("right")) {
                            Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                            intent.putExtra("path", "");
                            intent.putExtra("from", "save");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        }
                    });
        } else {
            startActivity(new Intent(SaveActivity.this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private void shareImageToSocial(String savedImagePath, String packageName, String from) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(savedImagePath));
        intent.setPackage(packageName);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (from.equalsIgnoreCase("instagram")) {
                Toast.makeText(SaveActivity.this, R.string.insta_msg, Toast.LENGTH_SHORT).show();
            } else if (from.equalsIgnoreCase("whatsapp")) {
                Toast.makeText(SaveActivity.this, R.string.whatsapp_msg, Toast.LENGTH_SHORT).show();
            } else if (from.equalsIgnoreCase("facebook")) {
                Toast.makeText(SaveActivity.this, R.string.facebook_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to share image to other apps
     *
     * @param imagePath: path of image
     */
    private void shareImage(String imagePath) {
        int lastSlashIndex = imagePath.lastIndexOf('/');
        String imageName = imagePath.substring(lastSlashIndex + 1);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Log.e(TAG, "shareImage: path: " + imagePath);

        Log.e(TAG, "shareImage: imageName: " + imageName);

        Bitmap bitmapResult = Utility.getInstance().loadImageFromPath(SaveActivity.this, imagePath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!imagePath.contains("Cartoon Photo Editor")) {
                shareImageUri = Uri.parse(imagePath);
            } else {
                shareImageUri = Utility.getInstance().shareBitmapToUri(SaveActivity.this, bitmapResult, imageName);
            }
        } else {
            shareImageUri = Utility.getInstance().shareBitmapToUri(SaveActivity.this, bitmapResult, imageName);
        }
        Log.e(TAG, "shareImage: bitmap: " + bitmapResult.toString());

        if (shareImageUri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareImageUri);
            shareIntent.setType("image/*");
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startActivity(shareIntent);

            } else {
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
            }
        } else {
            Toast.makeText(this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SaveActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private boolean isWhatsAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isWhatsAppBInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        if (!from.isEmpty() && from.equalsIgnoreCase("SavedFileActivity")) {
            startActivity(new Intent(SaveActivity.this, SavedFileActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else {
            if (!isPhotoSaved) {
                DialogHandler.getInstance().showBottomSheet(SaveActivity.this, getString(R.string.app_name), getString(R.string.exit_warning_msg),
                        getString(R.string.cancel_label), getString(R.string.ok_label), "", "backPress", true, itemClicked -> {
                            if (itemClicked.equalsIgnoreCase("right")) {
                                Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                                intent.putExtra("path", "");
                                intent.putExtra("from", "save");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        });
            } else {
                Intent intent = new Intent(SaveActivity.this, HomeActivity.class);
                intent.putExtra("path", "");
                intent.putExtra("from", "save");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadInterstitialAd("resume");
    }

    @Override
    public void onAdClicked() {

    }

    @Override
    public void onAdLoaded() {

    }

    @Override
    public void onAdFailedToLoad(LoadAdError loadAdError) {

    }
}