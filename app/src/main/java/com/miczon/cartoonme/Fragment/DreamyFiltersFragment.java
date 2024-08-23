package com.miczon.cartoonme.Fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.miczon.cartoonme.Activities.ApplyEffectActivity;
import com.miczon.cartoonme.Activities.HomeActivity;
import com.miczon.cartoonme.Activities.SplashActivity;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.Listeners.DialogClickListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.Model.FilterData;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.RecyclerViewAdapters.DreamyFiltersAdapter;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class DreamyFiltersFragment extends Fragment implements BannerAdLoadListener, OnUserEarnedRewardListener {

    String TAG = "DreamyFiltersFragment", from = "";

    TextView tvHeader;
    RelativeLayout backLayout, buyPremiumLayout, adDisplayLayout;
    RecyclerView rvDreamyFilter;
    FrameLayout adContainer;
    PrefsManager prefsManager;

    StaggeredGridLayoutManager staggeredGridLayoutManager;
    DreamyFiltersAdapter dreamyFiltersAdapter;

    ArrayList<String> selectedFilterIds;

    File camPhotoFile = null;
    Uri capturedImageUri;

    boolean isFromSettings = false;
    boolean isFirstTime = true;
    boolean isRewardedAdShown = false;
    boolean isInAppShown = false;

    BannerAdManager bannerAdManager;
    AdRequest adRequest;
    InterstitialAd mInterstitialAd;
    RewardedInterstitialAd rewardedInterstitialAd;

    Bundle bundle;
    FilterData filterData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending_filters, container, false);

        tvHeader = view.findViewById(R.id.tv_header);
        backLayout = view.findViewById(R.id.rl_back);
        buyPremiumLayout = view.findViewById(R.id.rl_premium);
        rvDreamyFilter = view.findViewById(R.id.rV_trendingFilters);
        adDisplayLayout = view.findViewById(R.id.rl_adLayout);
        adContainer = view.findViewById(R.id.fl_adContainer);

        prefsManager = new PrefsManager(requireActivity());
        adRequest = new AdRequest.Builder().build();
        bannerAdManager = new BannerAdManager(requireActivity(), this);
        filterData = FilterData.getInstance();

        Log.e(TAG, "onCreateView: is premium: " + Utility.getInstance().isPremiumActive(requireActivity()));

        if (!Utility.getInstance().isPremiumActive(requireActivity())) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);
        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        selectedFilterIds = new ArrayList<>();

        tvHeader.setText(R.string.dreamy_act_title);

        bundle = getArguments();
        if (bundle != null) {
            from = bundle.getString("from");

            if (from.equalsIgnoreCase("intentGallery")) {
                selectedFilterIds = bundle.getStringArrayList("filterIds");
                Log.e(TAG, "onCreateView: selected filter id size: " + selectedFilterIds.size());
                galleryClickAction();

            } else if (from.equalsIgnoreCase("intentCamera")) {
                selectedFilterIds = bundle.getStringArrayList("filterIds");
                Log.e(TAG, "onCreateView: selected filter id size: " + selectedFilterIds.size());
                checkCameraPerm();
            } else if (from.equalsIgnoreCase("trail") && Utility.getInstance().isPremiumActive(requireActivity())) {
                selectedFilterIds = bundle.getStringArrayList("filterIds");
                Log.e(TAG, "onCreateView: selected ids from trail: " + selectedFilterIds.size());
                choosePictureDialog();
            }
        }

        dreamyFiltersAdapter = new DreamyFiltersAdapter(requireActivity(), Utility.getInstance().dreamyFilterPreview(), (position, path, action) -> {
            selectedFilterIds = checkPosition(position);
            filterData.setFilterIds(selectedFilterIds);

            if (!Utility.getInstance().isPremiumActive(requireActivity())) {
                //For rewarded
                if (position == 2 || position == 15 || position == 6 || position == 8 || position == 11 || position == 12) {
                    DialogHandler.getInstance().displayPreRewardAdDialog(requireActivity(), true, new DialogClickListener() {
                        @Override
                        public void onButtonClick(String status, String message, String data, AlertDialog alertDialog) {
                            if (status.equalsIgnoreCase("0")) {
                                alertDialog.dismiss();
                                loadRewardedInterstitialAd("filters");

                            } else if (status.equalsIgnoreCase("1")) {
                                alertDialog.dismiss();
                                loadTrailFragment("rewarded");
                            } else if (status.equalsIgnoreCase("2")) {
                                alertDialog.dismiss();
                            }
                        }
                    });
                }
                //For in app
                else if (position == 1 || position == 4 || position == 5 ||
                        position == 7 || position == 9 || position == 10 || position == 13 ||
                        position == 14 || position == 3) {
                    loadInterstitialAd("purchase");
                }
                //For free
                else {
                    loadInterstitialAd("choose");
                }
            } else {
                loadInterstitialAd("choose");
            }
        });

        rvDreamyFilter.setLayoutManager(staggeredGridLayoutManager);
        rvDreamyFilter.setAdapter(dreamyFiltersAdapter);

        backLayout.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            HomeActivity.isFragmentVisible = false;
        });

        buyPremiumLayout.setOnClickListener(v -> {
            loadTrailFragment("buyPrem");
        });

        return view;
    }

    private void choosePictureDialog() {
        DialogHandler.getInstance().choosePictureDialog(requireActivity(), true, (status, message, data, alertDialog) -> {
            if (status.equalsIgnoreCase("0")) {
                alertDialog.dismiss();
                checkCameraPerm();
            } else if (status.equalsIgnoreCase("1")) {
                alertDialog.dismiss();
                galleryClickAction();
            }
        });
    }

    /**
     * Method to load and display interstitial add
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
        if (!Utility.getInstance().isPremiumActive(requireActivity())) {
            InterstitialAd.load(requireActivity(), Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
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
                    mInterstitialAd.show(requireActivity());
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
                            if (from.equalsIgnoreCase("choose")) {
                                choosePictureDialog();
                            } else if (from.equalsIgnoreCase("camera")) {
                                checkCameraPerm();
                            } else if (from.equalsIgnoreCase("gallery")) {
                                galleryClickAction();
                            } else if (from.equalsIgnoreCase("purchase")) {
                                loadTrailFragment(from);
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("choose")) {
                                choosePictureDialog();
                            } else if (from.equalsIgnoreCase("camera")) {
                                checkCameraPerm();
                            } else if (from.equalsIgnoreCase("gallery")) {
                                galleryClickAction();
                            } else if (from.equalsIgnoreCase("purchase")) {
                                loadTrailFragment(from);
                            }
                        }
                    });
                } else {
                    if (from.equalsIgnoreCase("choose")) {
                        choosePictureDialog();
                    } else if (from.equalsIgnoreCase("camera")) {
                        checkCameraPerm();
                    } else if (from.equalsIgnoreCase("gallery")) {
                        galleryClickAction();
                    } else if (from.equalsIgnoreCase("purchase")) {
                        loadTrailFragment(from);
                    }
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (from.equalsIgnoreCase("choose")) {
                    choosePictureDialog();
                } else if (from.equalsIgnoreCase("camera")) {
                    checkCameraPerm();
                } else if (from.equalsIgnoreCase("gallery")) {
                    galleryClickAction();
                } else if (from.equalsIgnoreCase("purchase")) {
                    loadTrailFragment(from);
                }
            }
        } else {
            Log.e(TAG, "No Premium");
            if (from.equalsIgnoreCase("choose")) {
                choosePictureDialog();
            } else if (from.equalsIgnoreCase("camera")) {
                checkCameraPerm();
            } else if (from.equalsIgnoreCase("gallery")) {
                galleryClickAction();
            }
        }
    }

    /**
     * Method to load rewarded interstitial ad
     *
     * @param from: where this method is called e.g. OnResume, OnCreate etc.
     */
    public void loadRewardedInterstitialAd(String from) {
        if (!Utility.getInstance().isPremiumActive(requireActivity())) {
            RewardedInterstitialAd.load(requireActivity(), Constants.AdMob_Main_Rewarded_Interstitial_Ad_Id,
                    new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                            rewardedInterstitialAd = ad;
                            Log.e(TAG, "rewardedInterstitialAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, "RewardedOnAdFailedToLoad: " + loadAdError);
                            rewardedInterstitialAd = null;
                        }
                    });
            if (rewardedInterstitialAd != null && !from.equalsIgnoreCase("resume")) {
                rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Constants.isInterstitialVisible = true;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Constants.isInterstitialVisible = false;
                        Log.e(TAG, "rewardedInterstitialAd dismissed fullscreen content.");
                        rewardedInterstitialAd = null;
                        isRewardedAdShown = true;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "rewardedInterstitialAd failed to show fullscreen content.");
                        rewardedInterstitialAd = null;
                        if (from.equalsIgnoreCase("filters")) {
                            Toast.makeText(requireActivity(), getString(R.string.no_ad_msg), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                rewardedInterstitialAd.show(requireActivity(), this);

            } else {
                Log.e(TAG, "The rewardedInterstitialAd wasn't ready yet.");
                if (from.equalsIgnoreCase("filters")) {
                    Toast.makeText(requireActivity(), getString(R.string.no_ad_msg), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Log.e(TAG, "loadRewardedInterstitialAd: premium is active:");
            if (!from.equalsIgnoreCase("resume")) {
                choosePictureDialog();
            }
        }
    }

    private void loadTrailFragment(String from) {
        HomeActivity.existingFragment = new FreeTrailFragment();

        Bundle bundle = new Bundle();

        if (from.equalsIgnoreCase("purchase") || from.equalsIgnoreCase("rewarded")) {
            bundle.putString("from", "filter");
            bundle.putStringArrayList("filterIds", selectedFilterIds);

        } else if (from.equalsIgnoreCase("buyPrem")) {
            bundle.putString("from", "exit");
        }
        bundle.putString("via", "dreamyFraghome");
        HomeActivity.existingFragment.setArguments(bundle);

        String fragmentTag = HomeActivity.existingFragment.getClass().getName();

        boolean fragmentPopped = requireActivity().getSupportFragmentManager()
                .popBackStackImmediate(fragmentTag, 0);

        if (!fragmentPopped && requireActivity().getSupportFragmentManager().findFragmentByTag(fragmentTag) == null) {
            Log.e(TAG, "loadTrailFragment: if working");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragmentContainer, HomeActivity.existingFragment)
                    .addToBackStack(fragmentTag)
                    .commit();
        } else {
            Log.e(TAG, "loadTrailFragment: else working");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragmentContainer, HomeActivity.existingFragment)
                    .addToBackStack(null)
                    .commit();
        }

        if (from.equalsIgnoreCase("purchase")) {
            isInAppShown = true;
        }
    }

    private void displayRewardedAd() {
        DialogHandler.getInstance().displayPreRewardAdDialog(requireActivity(), true, new DialogClickListener() {
            @Override
            public void onButtonClick(String status, String message, String data, AlertDialog alertDialog) {
                if (status.equalsIgnoreCase("0")) {
                    alertDialog.dismiss();
                    loadRewardedInterstitialAd("filters");

                } else if (status.equalsIgnoreCase("1")) {
                    alertDialog.dismiss();
                    loadTrailFragment("rewarded");
                } else if (status.equalsIgnoreCase("2")) {
                    alertDialog.dismiss();
                }
            }
        });

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
            camPhotoFile = Utility.getInstance().createCamFile(requireActivity());
        } catch (IOException ex) {
            Log.e(TAG, "startCameraIntent: exception: " + ex.getLocalizedMessage());
        }

        if (camPhotoFile != null) {
            try {
                Log.e(TAG, "startCameraIntent: file: " + camPhotoFile.getAbsolutePath());
                capturedImageUri = FileProvider.getUriForFile(requireActivity(),
                        requireActivity().getPackageName() + ".fileprovider",
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
     * Method to handle click event when gallery button is clicked
     */
    @SuppressWarnings("deprecation")
    private void galleryClickAction() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY);
    }

    /**
     * To handle camera intent and gallery intent requests
     *
     * @param requestCode: to identify if gallery intent is invoked or camera intent
     * @param resultCode:  to identify if intent result is successful
     * @param data:        data received via intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Constants.isSelectingFile = true;
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == RESULT_OK) {
            assert data != null;
            Uri path = data.getData();
            if (from != null && from.equalsIgnoreCase("intent")) {
                handleIntentAction(path, "intent");

            } else {
                handleIntentAction(path, "home");
            }

        } else if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(camPhotoFile.getAbsolutePath());

                // Rotate the bitmap based on Exif orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, camPhotoFile.getAbsolutePath());

                Uri path = Utility.getInstance().bitmapToUri(requireActivity(), rotatedBitmap, "");
                Log.e(TAG, "onActivityResult: path: " + path.getPath());
                handleIntentAction(path, "camera");

            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: error: " + e.getLocalizedMessage());
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
        String selectedImagePath = Utility.getInstance().getPath(requireActivity(), path);
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            Intent intent = new Intent(requireActivity(), ApplyEffectActivity.class);
            intent.putExtra("path", selectedImagePath);
            intent.putExtra("from", from);
            intent.putExtra("ids", selectedFilterIds);
            intent.putExtra("via", "dreamyFrag" + from);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e(TAG, "handleIntentAction: image path: " + selectedImagePath);
            Log.e(TAG, "handleIntentAction: from: " + from);
            for (int i = 0; i < selectedFilterIds.size(); i++) {
                Log.e(TAG, "handleIntentAction: filter ids: " + selectedFilterIds.get(i));
            }
            startActivity(intent);
        } else {
            Toast.makeText(requireActivity(), getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPerm() {
        boolean storagePermFlag = false;
//        if (from.equalsIgnoreCase("camera")) {
        Log.e(TAG, "askStoragePermission: inside if");
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "askStoragePermission: inside second if");
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third if");
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third else if");
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else {
                Log.e(TAG, "askStoragePermission: inside third else");
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);
            }
        } else {
            Log.e(TAG, "askStoragePermission: inside else of first if");
            storagePermFlag = true;
            startCameraIntent();
        }
//        }
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
         * When permission to camera access is requested
         */
        if (requestCode == Constants.REQUEST_CAMERA_ACCESS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {

                        if (!isFirstTime) {
                            showStorageAccessSettings = true;

                        } else {
                            isFirstTime = false;
                        }

                    } else {
                        Toast.makeText(requireActivity(), getString(R.string.cam_denied), Toast.LENGTH_SHORT).show();
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
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        settingsIntent.setData(uri);
        startActivity(settingsIntent);
    }

    public ArrayList<String> checkPosition(int pos) {
        Log.e(TAG, "checkPosition: position: " + pos);
        ArrayList<String> filterIdList = new ArrayList<>();
        if (pos == 0) {
            filterIdList.add("1066");
            filterIdList.add("1829");
        } else if (pos == 1) {
            filterIdList.add("1862");
            filterIdList.add("1850");
            filterIdList.add("5686");
            filterIdList.add("2351");
        } else if (pos == 2) {
            filterIdList.add("2105");
            filterIdList.add("7088");
            filterIdList.add("1067");
            filterIdList.add("2860");
            filterIdList.add("2341");
        } else if (pos == 3) {
            filterIdList.add("7088");
            filterIdList.add("3797");
        } else if (pos == 4) {
            filterIdList.add("3663");
            filterIdList.add("3810");
        } else if (pos == 5) {
            filterIdList.add("3735");
            filterIdList.add("2116");
            filterIdList.add("3769");
        } else if (pos == 6) {
            filterIdList.add("6632");
            filterIdList.add("2230");
            filterIdList.add("2343");
            filterIdList.add("1067");
        } else if (pos == 7) {
            filterIdList.add("6632");
            filterIdList.add("2570");
            filterIdList.add("2351");
        } else if (pos == 8) {
            filterIdList.add("6632");
        } else if (pos == 9) {
            filterIdList.add("6977");
        } else if (pos == 10) {
            filterIdList.add("7088");
            filterIdList.add("3762");
        } else if (pos == 11) {
            filterIdList.add("7088");
            filterIdList.add("2568");
            filterIdList.add("2277");
        } else if (pos == 12) {
            filterIdList.add("7088");
            filterIdList.add("3018");
            filterIdList.add("1067");
            filterIdList.add("950");
        } else if (pos == 13) {
            filterIdList.add("7088");
            filterIdList.add("3694");
            filterIdList.add("2349");
        } else if (pos == 14) {
            filterIdList.add("7088");
            filterIdList.add("2349");
        } else if (pos == 15) {
            filterIdList.add("2122");
            filterIdList.add("7088");
        }
        return filterIdList;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (from.equalsIgnoreCase("trail") && Utility.getInstance().isPremiumActive(requireActivity())) {
            if (bundle != null) {
                selectedFilterIds = bundle.getStringArrayList("filterIds");
                choosePictureDialog();
            } else {
                Log.e(TAG, "onResume: bundle is null");
            }
        } else {
            Log.e(TAG, "onResume: else working");
            if (isInAppShown) {
                selectedFilterIds = filterData.getFilterIds();
                displayRewardedAd();
                isInAppShown = false;
            }
        }

        HomeActivity.existingFragment = this;
        loadInterstitialAd("resume");
        loadRewardedInterstitialAd("resume");

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

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        choosePictureDialog();
    }
}