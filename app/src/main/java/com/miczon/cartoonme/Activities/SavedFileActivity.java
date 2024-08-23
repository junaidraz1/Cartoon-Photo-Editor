package com.miczon.cartoonme.Activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.miczon.cartoonme.Helper.BannerAdManager;
import com.miczon.cartoonme.Helper.DialogHandler;
import com.miczon.cartoonme.Listeners.BannerAdLoadListener;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.RecyclerViewAdapters.GalleryAdapter;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

import java.io.File;
import java.util.ArrayList;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class SavedFileActivity extends BaseActivity implements BannerAdLoadListener {

    String TAG = "SavedFileActivity", path = "", newName = "";

    RelativeLayout backLayout, noSavedImageLayout, adContainerHolderLayout;
    TextView tvHeader;
    RecyclerView rvSavedFiles;
    FrameLayout adContainer, fragmentContainer;
    TextView tvLoadingAd;

    RecyclerView.LayoutManager layoutManager;
    ArrayList<String> imagePathList, imageNameList;
    GalleryAdapter galleryAdapter;

//    AdView adView;
    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

    BannerAdManager bannerAdManager;

    boolean isForcedOverwrite = false, isFromRename = false, isPremium = false;

    ActivityResultLauncher<IntentSenderRequest> deleteLauncher, renameLauncher;

    Uri shareImageUri;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_file);

        backLayout = findViewById(R.id.rl_back);
        tvHeader = findViewById(R.id.tv_header);
        noSavedImageLayout = findViewById(R.id.rl_noSavedImage);
        adContainerHolderLayout = findViewById(R.id.rl_adContainer);
        rvSavedFiles = findViewById(R.id.rv_savedFile);
        adContainer = findViewById(R.id.fl_adContainer);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        fragmentContainer = findViewById(R.id.fl_fragmentContainer);

        tvHeader.setText(R.string.saved_files_header);

        layoutManager = new LinearLayoutManager(SavedFileActivity.this);
        imagePathList = new ArrayList<>();
        imageNameList = new ArrayList<>();
        adRequest = new AdRequest.Builder().build();
        bannerAdManager = new BannerAdManager(this, this);
        bundle = new Bundle();

        isPremium = Utility.getInstance().isPremiumActive(this);

        Log.e(TAG, "onCreate: is premium: " + Utility.getInstance().isPremiumActive(this));
//
//        adView = new AdView(this);
//        bannerAdManager.setAdSize(adView);

        if (!Utility.getInstance().isPremiumActive(this)) {
            adContainerHolderLayout.setVisibility(View.VISIBLE);
            tvLoadingAd.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
//            adContainer.addView(adView);
            bannerAdManager.loadBannerAd(adContainer);
        } else {
            adContainerHolderLayout.setVisibility(View.GONE);
            tvLoadingAd.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        displayGallery();

        handleDeleteLauncherIntent();
        handleRenameLauncherIntent();

        backLayout.setOnClickListener(v -> onBackPressed());

    }

    /**
     * Method to handle picture delete request in android version greater than 10
     */
    private void handleDeleteLauncherIntent() {
        deleteLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            Log.e(TAG, "handleLauncherIntent: result code: " + result.getResultCode());
            // Handle the result of the IntentSender request here
            if (result.getResultCode() == Activity.RESULT_OK) {
                displayGallery();
                if (!isFromRename) {
                    DialogHandler.getInstance().showDeleteBottomSheet(SavedFileActivity.this, "delSuccess", true, itemClicked -> {
                    });
                }
            } else {
                Log.e(TAG, "onAdDismissedFullScreenContent: not deleted");
            }
        });
    }

    /**
     * Method to handle picture rename request in android version greater than 10
     */
    private void handleRenameLauncherIntent() {
        renameLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            Log.e(TAG, "handleLauncherIntent: result code: " + result.getResultCode());
            // Handle the result of the IntentSender request here
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (path != null && !path.isEmpty() && newName != null && !newName.isEmpty()) {
                    renameImage(SavedFileActivity.this, path, newName);
                } else {
                    Log.e(TAG, "onActivityResult: path: " + path);
                    Log.e(TAG, "onActivityResult: new name: " + newName);
                    Toast.makeText(SavedFileActivity.this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "onAdDismissedFullScreenContent: not deleted");
            }
        });
    }

    /**
     * Method to get and display edited pictures from gallery
     */
    private void displayGallery() {
        Log.e(TAG, "displayGallery: called");
        imagePathList.clear();
        imageNameList.clear();

        String[] projection = {MediaStore.Images.Media.DATA};
        Uri galleryUri;
        String[] selectionArgs;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and above, use MediaStore
            galleryUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            selectionArgs = new String[]{"image/jpeg", "%" + "/Pictures/Cartoon Photo Editor/%"};
        } else {
            galleryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            selectionArgs = new String[]{"image/jpeg", "%" + "/DCIM/Cartoon Photo Editor/%"};
        }

        String selection = MediaStore.Images.Media.MIME_TYPE + "=? AND " + MediaStore.Images.Media.DATA + " LIKE ?";
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(galleryUri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String imagePath = cursor.getString(columnIndexData);

                    String updatedImageName = getImageNameFromMediaStore(imagePath);

                    if (!imagePath.isEmpty() && !updatedImageName.isEmpty()) {
                        imagePathList.add(imagePath);
                        imageNameList.add(updatedImageName);

                        Log.e(TAG, "displayGallery: image path: " + imagePath);
                    } else {
                        noSavedImageLayout.setVisibility(View.VISIBLE);
                        rvSavedFiles.setVisibility(View.GONE);
                        Log.e(TAG, "displayGallery: data is null");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imagePathList != null && imagePathList.size() > 0) {
            adContainerHolderLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            setGalleryAdapter(imagePathList, imageNameList);
        } else {
            noSavedImageLayout.setVisibility(View.VISIBLE);
            adContainerHolderLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
            rvSavedFiles.setVisibility(View.GONE);
        }
    }

    /**
     * Method to get image name
     *
     * @param imagePath: image path
     * @return: name of image
     */
    private String getImageNameFromMediaStore(String imagePath) {
        String imageName = "";

        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {imagePath};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            imageName = cursor.getString(columnIndex);
            cursor.close();
        }

        return imageName;
    }

    /**
     * Method to set recycler view adapter to display gallery items
     *
     * @param imagePathList: Contains path of images loaded from gallery
     */
    @SuppressLint("NotifyDataSetChanged")
    private void setGalleryAdapter
    (ArrayList<String> imagePathList, ArrayList<String> imageNameList) {
        Log.e(TAG, "setGalleryAdapter: inside method size is: " + imagePathList.size());
        layoutManager = new GridLayoutManager(this, Constants.SPAN_COUNT);
        galleryAdapter = new GalleryAdapter(this, imagePathList, imageNameList, (position, path, action) -> {

            if (action.equalsIgnoreCase("delete")) {
                DialogHandler.getInstance().showDeleteBottomSheet(SavedFileActivity.this, "delImage", true, itemClicked -> {
                    if (itemClicked.equalsIgnoreCase("1")) {
                        Log.e(TAG, "sheetClick: path is: " + path + " " + "\nposition is: " + position);
                        loadInterstitialAd("delete", path, "");
                    }
                });

            } else if (action.equalsIgnoreCase("share")) {
                shareImage(path);

            } else if (action.equalsIgnoreCase("rename")) {
                Log.e(TAG, "setGalleryAdapter: path: " + path);
                showRenameDialog(path, false, "");
            } else if (action.equalsIgnoreCase("view")) {
                loadInterstitialAd("view", path, "");
            }
        });
        rvSavedFiles.setAdapter(galleryAdapter);
        rvSavedFiles.setLayoutManager(layoutManager);
        galleryAdapter.notifyDataSetChanged();
    }

    /**
     * Method to display rename dialog
     *
     * @param imagePath:   path of image
     * @param showErrMsg:  error message flag
     * @param changedName: updated name of image (if exists)
     */
    private void showRenameDialog(String imagePath, boolean showErrMsg, String changedName) {
        DialogHandler.getInstance().showRenameDialog(SavedFileActivity.this, true, imagePath, changedName, showErrMsg, (status, message, data, alertDialog) -> {
            if (status != null) {
                switch (status) {
                    case "0":
                    case "2":
                        alertDialog.dismiss();
                        break;
                    case "1":
                        isForcedOverwrite = false;
                        loadInterstitialAd("rename", message, data);
                        alertDialog.dismiss();
                        break;
                    case "3":
                        isForcedOverwrite = true;
                        loadInterstitialAd("rename", imagePath, changedName);
                        alertDialog.dismiss();
                        break;
                }
            }
        });
    }

    /**
     * Method to load and display interstitial ad
     *
     * @param from:    from where it is called e.g. onResume, onCreate etc
     * @param imgPath: path of image
     * @param imgName: name of image
     */
    public void loadInterstitialAd(String from, String imgPath, String imgName) {
        if (!Utility.getInstance().isPremiumActive(this)) {
            InterstitialAd.load(SavedFileActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, adRequest,
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
                    mInterstitialAd.show(SavedFileActivity.this);
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
                            if (from.equalsIgnoreCase("view")) {
                                viewImageAction(imgPath);
                            } else if (from.equalsIgnoreCase("delete")) {
                                if (imgPath != null && !imgPath.isEmpty()) {
                                    deleteImageFromFolder(imgPath);
                                } else {
                                    Log.e(TAG, "onAdDismissedFullScreenContent: path is empty");
                                }
                            } else if (from.equalsIgnoreCase("rename")) {
                                if (imgPath != null && imgName != null) {
                                    if (!imgPath.isEmpty() && !imgName.isEmpty()) {
                                        Log.e(TAG, "onAdDismissedFullScreenContent: image name: " + imgName);
                                        renameImage(SavedFileActivity.this, imgPath, imgName);
                                    } else {
                                        Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                                    }
                                } else {
                                    Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("view")) {
                                viewImageAction(imgPath);
                            } else if (from.equalsIgnoreCase("delete")) {
                                if (imgPath != null && !imgPath.isEmpty()) {
                                    deleteImageFromFolder(imgPath);
                                } else {
                                    Log.e(TAG, "onAdDismissedFullScreenContent: path is empty");
                                }
                            } else if (from.equalsIgnoreCase("rename")) {
                                if (imgPath != null && imgName != null) {
                                    if (!imgPath.isEmpty() && !imgName.isEmpty()) {
                                        renameImage(SavedFileActivity.this, imgPath, imgName);
                                    } else {
                                        Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                                    }
                                } else {
                                    Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                                }
                            }
                        }
                    });
                } else {
                    if (from.equalsIgnoreCase("view")) {
                        viewImageAction(imgPath);
                    } else if (from.equalsIgnoreCase("delete")) {
                        if (imgPath != null && !imgPath.isEmpty()) {
                            deleteImageFromFolder(imgPath);
                        } else {
                            Log.e(TAG, "onAdDismissedFullScreenContent: path is empty");
                        }
                    } else if (from.equalsIgnoreCase("rename")) {
                        if (imgPath != null && imgName != null) {
                            if (!imgPath.isEmpty() && !imgName.isEmpty()) {
                                renameImage(SavedFileActivity.this, imgPath, imgName);
                            } else {
                                Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                            }
                        } else {
                            Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                        }
                    }
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (from.equalsIgnoreCase("view")) {
                    viewImageAction(imgPath);
                } else if (from.equalsIgnoreCase("delete")) {
                    if (imgPath != null && !imgPath.isEmpty()) {
                        deleteImageFromFolder(imgPath);
                    } else {
                        Log.e(TAG, "onAdDismissedFullScreenContent: path is empty");
                    }
                } else if (from.equalsIgnoreCase("rename")) {
                    if (imgPath != null && imgName != null) {
                        if (!imgPath.isEmpty() && !imgName.isEmpty()) {
                            renameImage(SavedFileActivity.this, imgPath, imgName);
                        } else {
                            Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                        }
                    } else {
                        Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                    }
                }
            }
        } else {
            Log.e(TAG, "The interstitial ad wasn't ready yet.");
            if (from.equalsIgnoreCase("view")) {
                viewImageAction(imgPath);
            } else if (from.equalsIgnoreCase("delete")) {
                if (imgPath != null && !imgPath.isEmpty()) {
                    deleteImageFromFolder(imgPath);
                } else {
                    Log.e(TAG, "onAdDismissedFullScreenContent: path is empty");
                }
            } else if (from.equalsIgnoreCase("rename")) {
                if (imgPath != null && imgName != null) {
                    if (!imgPath.isEmpty() && !imgName.isEmpty()) {
                        renameImage(SavedFileActivity.this, imgPath, imgName);
                    } else {
                        Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                    }
                } else {
                    Log.e(TAG, "onAdDismissedFullScreenContent: path or name is empty");
                }
            }
        }
    }

    /**
     * Method to pass image path to view it in full screen mode
     *
     * @param imgPath: path of image
     */
    public void viewImageAction(String imgPath) {
        Intent intent = new Intent(SavedFileActivity.this, SaveActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("pic", imgPath);
        intent.putExtra("from", TAG);
        startActivity(intent);
    }

    /**
     * Method to share image with other apps
     *
     * @param imagePath: path of image
     */
    public void shareImage(String imagePath) {
        Log.e(TAG, "shareImage: path: " + imagePath);
        int lastSlashIndex = imagePath.lastIndexOf('/');
        String imageName = imagePath.substring(lastSlashIndex + 1);

        Log.e(TAG, "shareImage: imageName: " + imageName);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        new Thread(() -> {
            Bitmap bitmapResult = Utility.getInstance().loadImageFromPath(SavedFileActivity.this, imagePath);
            runOnUiThread(() -> {
                Log.e(TAG, "shareImage: bitmap: " + bitmapResult.toString());
                shareImageUri = Utility.getInstance().shareBitmapToUri(this, bitmapResult, imageName);
                Log.e(TAG, "shareImage: share image Uri: " + shareImageUri);

                shareIntent.putExtra(Intent.EXTRA_STREAM, shareImageUri);
                shareIntent.setType("image/*");
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(shareIntent);

                } else {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
                }
            });
        }).start();
    }

    /**
     * Method to delete images
     *
     * @param imagePath: path of image
     */
    private void deleteImageFromFolder(String imagePath) {
        ContentResolver contentResolver = getContentResolver();
        Uri imgUri = Utility.mInstance.getImageContentUri(SavedFileActivity.this, imagePath);

        Log.e(TAG, "deleteImageFromFolder: img uri: " + imgUri);

        try {
            //delete object using resolver
            contentResolver.delete(imgUri, null, null);
            displayGallery();
            if (!isFromRename) {
                DialogHandler.getInstance().showDeleteBottomSheet(SavedFileActivity.this, "delSuccess", true, itemClicked -> {
                });
            }
            Log.e(TAG, "delete: inside try");

        } catch (SecurityException e) {
            Log.e(TAG, "delete: inside catch");
            PendingIntent pendingIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(imgUri);
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();

                }
            } else {
                Log.e(TAG, "deleteImageFromFolder: last case");
                if (imagePath.contains("/DCIM/Cartoon Photo Editor/") || imagePath.contains("/Pictures/")) {
                    File imageFile = new File(imagePath);

                    if (imageFile.exists()) {
                        if (imageFile.delete()) {
                            DialogHandler.getInstance().showDeleteBottomSheet(SavedFileActivity.this, "delSuccess", true, itemClicked -> {
                            });

                            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            scanIntent.setData(Uri.fromFile(imageFile));
                            sendBroadcast(scanIntent);
                            MediaScannerConnection.scanFile(SavedFileActivity.this, new String[]{imageFile.getAbsolutePath()},
                                    null, (path, uri) -> runOnUiThread(this::displayGallery));
                        } else {
                            // Failed to delete the image file
                            DialogHandler.getInstance().showBottomSheet(SavedFileActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                    "", "", getString(R.string.ok_label), "connectionErr", true, itemClicked -> {

                                    });
                            Log.e(TAG, "Failed to delete the image file: " + imagePath);
                        }
                    }
                } else {
                    // Image is not from the "Cartoon Photo Editor" folder
                    DialogHandler.getInstance().showBottomSheet(SavedFileActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                            "", "", getString(R.string.ok_label), "connectionErr", true, itemClicked -> {

                            });
                    Log.e(TAG, "Image is not from the Cartoon Photo Editor folder: " + imagePath);
                }
            }

            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                deleteLauncher.launch(request);
            }
        }
    }

    /**
     * Method to rename image
     *
     * @param context:          context
     * @param originalFilePath: original path
     * @param newFileName:      new image name
     */
    private void renameImage(Context context, String originalFilePath, String newFileName) {
        File imageFile = new File(originalFilePath);
        String fileExtension = originalFilePath.substring(originalFilePath.lastIndexOf("."));
        String newFilePath = imageFile.getParent() + File.separator + newFileName + fileExtension;
        File newImageFile = new File(newFilePath);
        newName = newFileName;
        Uri originalFileUri;
        PendingIntent pendingIntent = null;

        if (newImageFile.exists() && !isForcedOverwrite) {
            showRenameDialog(originalFilePath, true, newName);

        } else {
            if (newImageFile.exists() && isForcedOverwrite) {
                Log.e(TAG, "renameImage: image file: " + imageFile);
                for (int i = 0; i < imagePathList.size(); i++) {
                    if (newImageFile.getAbsolutePath().equals(imagePathList.get(i))) {
                        Log.e(TAG, "renameImage: file found");
                        File fileToDelete = new File(imagePathList.get(i));
                        if (fileToDelete.exists()) {
                            isFromRename = true;
                            deleteImageFromFolder(imagePathList.get(i));
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                originalFileUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(getImageId(context, originalFilePath)))
                        .build();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFileName);

                try {
                    context.getContentResolver().update(originalFileUri, values, null, null);
                    MediaScannerConnection.scanFile(
                            context,
                            new String[]{originalFilePath, newFilePath},
                            null,
                            (path, uri) -> {
                                if (path.equals(originalFilePath)) {
                                    Log.e(TAG, "Old image file scanned: " + path);
                                } else {
                                    runOnUiThread(() -> {
                                        displayGallery();
                                        DialogHandler.getInstance().renameSuccessDialog(SavedFileActivity.this, true);
//                                        Toast.makeText(context, getString(R.string.name_update), Toast.LENGTH_SHORT).show();
                                    });
                                    Log.e(TAG, "New image file scanned: " + path);
                                }
                            }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "delete: inside catch");
                    Log.e(TAG, "renameImage: exception: " + e.getLocalizedMessage());

                    //if exception is recoverable then again send delete request using intent
                    if (e instanceof RecoverableSecurityException) {
                        RecoverableSecurityException exception = (RecoverableSecurityException) e;
                        pendingIntent = exception.getUserAction().getActionIntent();

                    }

                    if (pendingIntent != null) {
                        path = originalFilePath;
                        IntentSender sender = pendingIntent.getIntentSender();
                        IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                        renameLauncher.launch(request);
                    }
                }
            } else {
                // For devices running on Android versions prior to 10, use the old method
                if (imageFile.renameTo(newImageFile)) {
                    if (imageFile.exists() && imageFile.delete()) {
                        Log.e(TAG, "Old image file deleted: " + originalFilePath);
                    } else {
                        Log.e(TAG, "Failed to delete old image file: " + originalFilePath);
                    }

                    MediaScannerConnection.scanFile(context, new String[]{originalFilePath, newImageFile.getAbsolutePath()}, null, (path, uri) -> {
                        if (path.equals(originalFilePath)) {
                            Log.e(TAG, "Old image file scanned: " + path);
                        } else {
                            runOnUiThread(() -> {
                                displayGallery();
                                DialogHandler.getInstance().renameSuccessDialog(SavedFileActivity.this, true);
//                                Toast.makeText(context, getString(R.string.name_update), Toast.LENGTH_SHORT).show();
                            });
                            Log.e(TAG, "New image file scanned: " + path);
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to update the file name: " + originalFilePath);
                    Toast.makeText(context, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Method to get image id from media store
     *
     * @param context:   context
     * @param imagePath: path of image
     * @return: id of image
     */
    private long getImageId(Context context, String imagePath) {
        long imageId = -1;
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {imagePath};

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                imageId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageId;
    }

    /**
     * onResume
     */
    @Override
    protected void onResume() {
//        if (adView != null) {
//            adView.resume();
//        }
        super.onResume();

//        isPremium = Utility.getInstance().isPremiumActive(this);
        Log.e(TAG, "onResume: is premium: " + Utility.getInstance().isPremiumActive(this));

        if (Utility.getInstance().isPremiumActive(this) || imagePathList.size() == 0) {
            adContainerHolderLayout.setVisibility(View.GONE);
            tvLoadingAd.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);

        } else {
            adContainerHolderLayout.setVisibility(View.VISIBLE);
            tvLoadingAd.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
        }

        loadInterstitialAd("resume", "", "");
    }

    /**
     * onPause
     */
    @Override
    protected void onPause() {
//        if (adView != null) {
//            adView.pause();
//        }
        super.onPause();
    }

    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }
        super.onDestroy();
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
        Log.e(TAG, "onAdaptiveAdLoaded: ");
        tvLoadingAd.setVisibility(View.GONE);
    }

    /**
     * Interface method implementation of Banner Ad helper class
     */
    @Override
    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
        Log.e(TAG, "onAdFailedToLoad: ");
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed: else called: ");
        startActivity(new Intent(SavedFileActivity.this, HomeActivity.class)
                .setFlags(FLAG_ACTIVITY_CLEAR_TOP));
    }
}