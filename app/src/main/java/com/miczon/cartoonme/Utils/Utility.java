package com.miczon.cartoonme.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.miczon.cartoonme.Activities.SplashActivity;
import com.miczon.cartoonme.Helper.InAppBillingHelper;
import com.miczon.cartoonme.Listeners.RemoteConfigListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class Utility {

    String TAG = "Utility";
    public static Utility mInstance = null;
    boolean isFetched = false;
    Bitmap urlToBitmap;

    /**
     * Singleton instance method
     *
     * @return instance as object
     */
    public static Utility getInstance() {
        if (mInstance == null) {
            mInstance = new Utility();
        }
        return mInstance;
    }

    /**
     * to modify crop library toolbar's color
     *
     * @param context: from where it is invoked
     * @return options object to apply it on library object
     */
    public static UCrop.Options imageCropperStyle(Context context) {
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle(context.getString(R.string.crop_image_label));
        options.setToolbarColor(context.getResources().getColor(R.color.newBgColor));
        options.setToolbarWidgetColor(context.getResources().getColor(R.color.white));
        options.setStatusBarColor(context.getResources().getColor(R.color.newBgColor));
        options.setToolbarCancelDrawable(R.drawable.ic_nav_back);
        options.setToolbarCropDrawable(R.drawable.ic_golden_tick);
        return options;
    }

    /**
     * to convert bitmap to uri
     *
     * @param context: from where it is invoked
     * @param bitmap:  image in bitmap
     */
    public Uri bitmapToUri(Context context, Bitmap bitmap, String imgName) {
        Log.e(TAG, "bitmapToUri: inside ");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "", mimeType;

        if (!imgName.isEmpty()) {
            imageName = imgName;
            mimeType = "";
        } else {
            imageName = "Image_" + timeStamp + ".jpg";
            mimeType = "image/jpg";
        }

        // Create a subdirectory within the "Pictures" directory for your app
        File appPicturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Cartoon Photo Editor");
        if (!appPicturesDir.exists()) {
            appPicturesDir.mkdirs();
        }

        // Create the image file within your app's subdirectory
        File imageFile = new File(appPicturesDir, imageName);

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        try {
            OutputStream outputStream = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUri;
    }

    /**
     * To get URI of image from Bitmap
     *
     * @param context: from where it is called
     * @param bitmap:  bitmap of image to be converted to URI
     * @param imgName: name of same image
     * @return: URI of image
     */
    public Uri shareBitmapToUri(Context context, Bitmap bitmap, String imgName) {
        if (bitmap == null) {
            // Handle the case where the bitmap is null, e.g., show an error message.
            return null;
        }

        File imageFile = new File(context.getExternalFilesDir(null), imgName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
    }

    /**
     * To get path of image when it is selected via gallery by user
     *
     * @param uri: uri of picture whose path is to be retrieved
     */
    public String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }

        if (result == null && "content".equalsIgnoreCase(uri.getScheme())) {
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                File file = createTempFile(context);
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                result = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * to create temporary file to hold path of uri requested
     *
     * @param context: from where it is called
     * @return temporary file path
     */
    public File createTempFile(Context context) throws IOException {
        Log.e(TAG, "createTempFile: called");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        return File.createTempFile("IMG_", ".jpg", storageDir);
    }

    /**
     * To create temp file when captured from camera
     *
     * @param context: from where it is called
     * @return: file
     * @throws: IOException
     */
    public File createCamFile(Context context) throws IOException {
        Log.e(TAG, "createTempFile: called");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        Log.e(TAG, "createCamFile: file name: " + fileName);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    /**
     * to convert images from url and local path into bitmap
     *
     * @param imagePath: image path/url
     */
    public Bitmap loadImageFromPath(Context context, String imagePath) {
        Log.e(TAG, "loadImageFromPath: working");
        File imageFile = new File(imagePath);
        Log.e(TAG, "run: path: " + imagePath);
        Log.e(TAG, "run: file: " + imageFile);

        if (imageFile.exists()) {
            Log.e(TAG, "loadImageFromPath: inside if");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(imagePath, options);
        } else {
            if (urlToBitmap == null) {
                final CountDownLatch latch = new CountDownLatch(1);

                HandlerThread handlerThread = new HandlerThread("ImageLoaderThread");
                handlerThread.start();

                Handler handler = new Handler(handlerThread.getLooper());
                handler.post(() -> {
                    try {
                        Log.e(TAG, "loadImageFromPath: inside try path: " + imagePath);
                        if (imagePath.contains("http") || imagePath.contains("https")) {
                            InputStream inputStream = new URL(imagePath).openStream();
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        } else {
                            ContentResolver contentResolver = context.getContentResolver();
                            InputStream inputStream = contentResolver.openInputStream(Uri.parse(imagePath));
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        }
                        Log.e(TAG, "run: bitmap" + urlToBitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "loadImageFromPath: exception: " + e);
                    } finally {
                        latch.countDown(); // Signal the completion of the thread
                    }
                });

                try {
                    latch.await(); // Wait until the latch count reaches zero
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handlerThread.quitSafely(); // Quit the handler thread when done
            } else {
                Log.e(TAG, "loadImageFromPath: else working" + urlToBitmap);
            }

            // Retrieve the bitmap and reset the variable to null
            Bitmap bitmap = urlToBitmap;
            urlToBitmap = null;
            return bitmap;
        }
    }

/*
    public Bitmap loadImageFromPath(Context context, String imagePath) {
        Log.e(TAG, "loadImageFromPath: working");
        File imageFile = new File(imagePath);
        Log.e(TAG, "run: path: " + imagePath);
        Log.e(TAG, "run: file: " + imageFile);
        if (imageFile.exists()) {
            Log.e(TAG, "loadImageFromPath: inside if");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(imagePath, options);
        } else {
            if (urlToBitmap == null) {
                final CountDownLatch latch = new CountDownLatch(1);
                new Thread(() -> {
                    try {
                        Log.e(TAG, "loadImageFromPath: inside try path: " + imagePath);
                        if (imagePath.contains("http") || imagePath.contains("https")) {
                            InputStream inputStream = new URL(imagePath).openStream();
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        } else {
                            ContentResolver contentResolver = context.getContentResolver();
                            InputStream inputStream = contentResolver.openInputStream(Uri.parse(imagePath));
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        }
                        Log.e(TAG, "run: bitmap" + urlToBitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "loadImageFromPath: exception: " + e);
                    } finally {
                        latch.countDown();// Signal the completion of the thread
                    }
                }).start();

                try {
                    latch.await(); // Wait until the latch count reaches zero
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e(TAG, "loadImageFromPath: else working" + urlToBitmap);
            }

            // Retrieve the bitmap and reset the variable to null
            Bitmap bitmap = urlToBitmap;
            urlToBitmap = null;
            return bitmap;
        }
    }
*/

    /**
     * Method to save image to gallery
     *
     * @param context:     from where it is called
     * @param imageBitmap: bitmap of image to be saved
     * @return: path of image that is saved
     */
    public String saveImageToGallery(Context context, Bitmap imageBitmap) {
        String path = "";

        String galleryFolderName = "Cartoon Photo Editor";
        String imageFileName = "Image_" + System.currentTimeMillis() + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and higher, use MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "Saved from Cartoon Photo Editor app");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + galleryFolderName);

            ContentResolver contentResolver = context.getContentResolver();
            Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try {
                    OutputStream outputStream = contentResolver.openOutputStream(imageUri);
                    if (outputStream != null) {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
//                        Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                        path = imageUri.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // For Android 9 (API level 28) and below, use the old approach
            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            File galleryDir = new File(externalStorageDir, galleryFolderName);
            galleryDir.mkdirs();

            File imageFile = new File(galleryDir, imageFileName);

            Log.e(TAG, "saveImageToGallery: path is: " + imageFile);

            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
//                Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                // Skip the media scan for the image file
                MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, null);

                path = imageFile.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return path;
    }

    /**
     * To display loader
     *
     * @param context:     the activity that it is called from
     * @param strokeWidth: the width of circle's stroke
     * @param radius:      the round radius of circle
     * @param color:       color of circle
     */
    public CircularProgressDrawable showLoader(Context context, int strokeWidth, int radius, int color) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(strokeWidth);
        circularProgressDrawable.setCenterRadius(radius);
        circularProgressDrawable.setColorSchemeColors(color);
        circularProgressDrawable.start();

        return circularProgressDrawable;
    }

    /**
     * method to convert bitmap image into base64 for network call
     *
     * @param bitmap: bitmap image
     * @return string
     */
    public String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String base64String = Base64.encodeToString(byteArray, Base64.URL_SAFE | Base64.NO_WRAP);
        base64String = base64String.replace("-", "+").replace("_", "/").replace("=", "");

        return base64String;
    }

    public void addSplashAnimation(View view1, View view2, View view3, boolean shouldStart) {
        TranslateAnimation topToBottom1 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -400);

        topToBottom1.setDuration(10000);
        topToBottom1.setFillAfter(true);
        topToBottom1.setRepeatCount(Animation.INFINITE);
        topToBottom1.setRepeatMode(Animation.REVERSE);
        topToBottom1.setInterpolator(new LinearInterpolator());

        TranslateAnimation topToBottom2 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -300);

        topToBottom2.setDuration(10000);
        topToBottom2.setFillAfter(true);
        topToBottom2.setRepeatCount(Animation.INFINITE);
        topToBottom2.setRepeatMode(Animation.REVERSE);
        topToBottom2.setInterpolator(new LinearInterpolator());

        TranslateAnimation topToBottom3 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -200);

        topToBottom3.setDuration(10000);
        topToBottom3.setFillAfter(true);
        topToBottom3.setRepeatCount(Animation.INFINITE);
        topToBottom3.setRepeatMode(Animation.REVERSE);
        topToBottom3.setInterpolator(new LinearInterpolator());

        if (shouldStart) {
            view1.startAnimation(topToBottom1);
            view2.startAnimation(topToBottom3);
            view3.startAnimation(topToBottom2);
        } else {
            view1.getAnimation().cancel();
            view2.getAnimation().cancel();
            view3.getAnimation().cancel();
        }

    }


    /**
     * To get URI of image from its path
     *
     * @param context:   from where it is called
     * @param imagePath: path of image
     * @return: uri of image
     */
    @SuppressLint("Range")
    public Uri getImageContentUri(Context context, String imagePath) {
        if (imagePath == null) {
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{imagePath};
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                return Uri.withAppendedPath(contentUri, "" + imageId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the original content URI if the image's content URI is not found
        return Uri.parse("file://" + imagePath);
    }

    /**
     * To check if premium sub is bought or not
     *
     * @param activity: from where it is called
     * @return: flag that contains state of premium i.e bought or not
     */
    public boolean isPremiumActive(Activity activity) {
        PrefsManager prefsManager = new PrefsManager(activity.getApplicationContext());
        InAppBillingHelper.getInstance().initialiseBillingClient(activity);
        InAppBillingHelper.getInstance().establishConnection();
        InAppBillingHelper.getInstance().purchasedSubVerification();
        if (prefsManager.getIsPremium()) {

            Log.e(TAG, "isPremiumActive: is subscribed: " + prefsManager.getIsPremium());
            return true;

        } else {
            Log.e(TAG, "isPremiumActive: not subscribed: " + prefsManager.getIsPremium());
            return false;
        }
    }

    /**
     * Method to verify whether if app is downloaded from playstore or not
     *
     * @return: boolean flag
     */
    public boolean verifyInstallerId(Context context) {
        // A list with valid installers package names
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        //A list with invalid installers package names
        List<String> invalidInstallers = new ArrayList<>(Arrays.asList("com.lenovo.anyshare.gps", "com.dewmobile.kuaiya.play"));
        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        if (installer != null) {
            if (validInstallers.contains(installer))
                return true;
            else if (invalidInstallers.contains(installer))
                return false;
            else
                return false;
        } else
            return false;
    }

    /**
     * Method to get remote config
     *
     * @param activity: current activity
     */
    public void getRemoteConfigs(Activity activity, RemoteConfigListener remoteConfigListener) {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(900)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "getRemoteConfigs: remote fetched: ");
                        Constants.AP_K = remoteConfig.getString(SplashActivity.apK);
                        Constants.F_MAIL = remoteConfig.getString(SplashActivity.fMail);

                        Constants.App_Open_Ad_Id = "ca-app-pub-3940256099942544/3419835294";

                        Constants.AdMob_Splash_Adaptive_Banner_Ad_Id = "ca-app-pub-3940256099942544/9214589741";
                        Constants.AdMob_Splash_Interstitial_Ad_Id = "ca-app-pub-3940256099942544/1033173712";

                        Constants.AdMob_Main_Adaptive_Banner_Ad_Id = "ca-app-pub-3940256099942544/9214589741";
                        Constants.AdMob_Main_Native_Advance_Ad_Id = "ca-app-pub-3940256099942544/2247696110";
                        Constants.AdMob_Main_Interstitial_Ad_Id = "ca-app-pub-3940256099942544/1033173712";
                        Constants.AdMob_Main_Rewarded_Interstitial_Ad_Id = "ca-app-pub-3940256099942544/5354046379";

                    /*    Constants.App_Open_Ad_Id = remoteConfig.getString("App_Open_Ad_Id");

                        Constants.AdMob_Splash_Adaptive_Banner_Ad_Id = remoteConfig.getString("AdMob_Splash_Adaptive_Banner_Ad_Id");
                        Constants.AdMob_Splash_Interstitial_Ad_Id = remoteConfig.getString("AdMob_Splash_Interstitial_Ad_Id");

                        Constants.AdMob_Main_Adaptive_Banner_Ad_Id = remoteConfig.getString("AdMob_Main_Adaptive_Banner_Ad_Id");
                        Constants.AdMob_Main_Native_Advance_Ad_Id = remoteConfig.getString("AdMob_Main_Native_Advance_Ad_Id");
                        Constants.AdMob_Main_Interstitial_Ad_Id = remoteConfig.getString("AdMob_Main_Interstitial_Ad_Id");
                        Constants.AdMob_Main_Rewarded_Interstitial_Ad_Id = remoteConfig.getString("AdMob_Main_Rewarded_Interstitial_Ad_Id");*/

                        Constants.Admob_App_Id = remoteConfig.getString("Admob_App_Id");
                        /*   String TAG = "appID";*/
                        try {
                            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
                            Bundle bundle = ai.metaData;
                            String myApiKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
                            Log.e(TAG, "Name Found: " + myApiKey);
                            ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", Constants.Admob_App_Id);
                            String ApiKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
                            Log.e(TAG, "ReNamed Found: " + ApiKey);
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
                        } catch (NullPointerException e) {
                            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
                        }

                      /*  Log.e(TAG, "getRemoteConfigs: AP_K: " + Constants.AP_K);
                        Log.e(TAG, "getRemoteConfigs: F_MAIL: " + Constants.F_MAIL);*/

                        remoteConfigListener.onRemoteConfigFetched();
                        Log.e(TAG, "getRemoteConfigs: splash adaptive id: " + Constants.AdMob_Splash_Adaptive_Banner_Ad_Id);

                    } else {
                        Constants.AP_K = "b96f748720msh11690bb20f87f2ap16c0b3jsn054286d97085";
                        Log.e(TAG, "onComplete: failed to fetch remote config params");
                        remoteConfigListener.onRemoteConfigFetchFailed();
                    }
                });
        /* return isFetched;*/
    }

    /**
     * Method to pass country names to country adapter, to display it in select language
     *
     * @return: array list of names
     */
    public ArrayList<String> countryNames() {
        ArrayList<String> countryName = new ArrayList<>();
        countryName.add("English " + "-" + " English");
        countryName.add("Afrikaans " + "-" + " Afrikaans");
        countryName.add("Arabic " + "-" + "العربية ");
        countryName.add("Chinese " + "-" + " 中文");
        countryName.add("Czech " + "-" + " Čeština");
        countryName.add("Dutch " + "-" + " Niederländisch");
        countryName.add("French " + "-" + " Français");
        countryName.add("German " + "-" + " Deutsch");
        countryName.add("Greek " + "-" + " Ελληνικά");
        countryName.add("Hindi " + "-" + " हिन्दी");
        countryName.add("Indonesian " + "-" + " Bahasa Indonesia");
        countryName.add("Italian " + "-" + " Italiano");
        countryName.add("Japanese " + "-" + " 日本語");
        countryName.add("Korean " + "-" + " 한국어");
        countryName.add("Malay " + "-" + " Melayu");
        countryName.add("Norwegian " + "-" + " Norsk");
        countryName.add("Persian " + "-" + " فارسی");
        countryName.add("Portuguese " + "-" + " Português");
        countryName.add("Russian " + "-" + " Pусский");
        countryName.add("Spanish " + "-" + " Español");
        countryName.add("Thai " + "-" + " ไทย");
        countryName.add("Turkish " + "-" + " Türkçe");
        countryName.add("Vietnamese " + "-" + " Tiếng Việt");

        return countryName;
    }

    /**
     * Method to pass country images to country adapter, to display it in select language
     *
     * @return: array list of country flag id's stored in drawable
     */
    public ArrayList<Integer> countryFlags() {
        ArrayList<Integer> countryFlags = new ArrayList<>();
        countryFlags.add(R.drawable.ic_english);
        countryFlags.add(R.drawable.ic_afrikaans);
        countryFlags.add(R.drawable.ic_arabic);
        countryFlags.add(R.drawable.ic_china);
        countryFlags.add(R.drawable.ic_czech);
        countryFlags.add(R.drawable.ic_netherlands);
        countryFlags.add(R.drawable.ic_france);
        countryFlags.add(R.drawable.ic_germany);
        countryFlags.add(R.drawable.ic_greece);
        countryFlags.add(R.drawable.ic_india);
        countryFlags.add(R.drawable.ic_indonesia);
        countryFlags.add(R.drawable.ic_italy);
        countryFlags.add(R.drawable.ic_japan);
        countryFlags.add(R.drawable.ic_korean);
        countryFlags.add(R.drawable.ic_malay);
        countryFlags.add(R.drawable.ic_norway);
        countryFlags.add(R.drawable.ic_perisan);
        countryFlags.add(R.drawable.ic_portugal);
        countryFlags.add(R.drawable.ic_russia);
        countryFlags.add(R.drawable.ic_spain);
        countryFlags.add(R.drawable.ic_thailand);
        countryFlags.add(R.drawable.ic_turkey);
        countryFlags.add(R.drawable.ic_vietnam);

        return countryFlags;
    }

    /**
     * Method to pass filter preview images to filter adapter inside apply effect activity
     *
     * @return: array list of filter preview picture ids stored in drawable
     */
    public ArrayList<Integer> trendingFilterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
        filterInfo.add(R.drawable.ic_tf_1);
        filterInfo.add(R.drawable.ic_tf_2);
        filterInfo.add(R.drawable.ic_tf_3);
        filterInfo.add(R.drawable.ic_tf_4);
        filterInfo.add(R.drawable.ic_tf_14);
        filterInfo.add(R.drawable.ic_tf_6);
        filterInfo.add(R.drawable.ic_tf_7);
        filterInfo.add(R.drawable.ic_tf_8);
        filterInfo.add(R.drawable.ic_tf_9);
        filterInfo.add(R.drawable.ic_tf_10);
        filterInfo.add(R.drawable.ic_tf_11);
        filterInfo.add(R.drawable.ic_tf_12);
        filterInfo.add(R.drawable.ic_tf_16);
        filterInfo.add(R.drawable.ic_tf_13);
        filterInfo.add(R.drawable.ic_tf_5);
        filterInfo.add(R.drawable.ic_tf_15);

//        filterInfo.add(R.drawable.ic_tf_1);
//        filterInfo.add(R.drawable.ic_tf_2);
//        filterInfo.add(R.drawable.ic_tf_3);
//        filterInfo.add(R.drawable.ic_tf_4);
//        filterInfo.add(R.drawable.ic_tf_5);
//        filterInfo.add(R.drawable.ic_tf_6);
//        filterInfo.add(R.drawable.ic_tf_7);
//        filterInfo.add(R.drawable.ic_tf_8);
//        filterInfo.add(R.drawable.ic_tf_9);
//        filterInfo.add(R.drawable.ic_tf_10);
//        filterInfo.add(R.drawable.ic_tf_11);
//        filterInfo.add(R.drawable.ic_tf_12);
//        filterInfo.add(R.drawable.ic_tf_16);
//        filterInfo.add(R.drawable.ic_tf_13);
//        filterInfo.add(R.drawable.ic_tf_14);
//        filterInfo.add(R.drawable.ic_tf_15);

        return filterInfo;
    }

    public ArrayList<Integer> dreamyFilterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
        filterInfo.add(R.drawable.ic_dr_1);
        filterInfo.add(R.drawable.ic_dr_2);
        filterInfo.add(R.drawable.ic_dr_3);
        filterInfo.add(R.drawable.ic_dr_16); // 3
        filterInfo.add(R.drawable.ic_dr_5);
        filterInfo.add(R.drawable.ic_dr_6);
        filterInfo.add(R.drawable.ic_dr_7);
        filterInfo.add(R.drawable.ic_dr_8);
        filterInfo.add(R.drawable.ic_dr_9);
        filterInfo.add(R.drawable.ic_dr_10);
        filterInfo.add(R.drawable.ic_dr_15); // 10
        filterInfo.add(R.drawable.ic_dr_12);
        filterInfo.add(R.drawable.ic_dr_13);
        filterInfo.add(R.drawable.ic_dr_14);
        filterInfo.add(R.drawable.ic_dr_11); // 14
        filterInfo.add(R.drawable.ic_dr_4); // 15

//        filterInfo.add(R.drawable.ic_dr_1);
//        filterInfo.add(R.drawable.ic_dr_2);
//        filterInfo.add(R.drawable.ic_dr_3);
//        filterInfo.add(R.drawable.ic_dr_4);
//        filterInfo.add(R.drawable.ic_dr_5);
//        filterInfo.add(R.drawable.ic_dr_6);
//        filterInfo.add(R.drawable.ic_dr_7);
//        filterInfo.add(R.drawable.ic_dr_8);
//        filterInfo.add(R.drawable.ic_dr_9);
//        filterInfo.add(R.drawable.ic_dr_10);
//        filterInfo.add(R.drawable.ic_dr_11);
//        filterInfo.add(R.drawable.ic_dr_12);
//        filterInfo.add(R.drawable.ic_dr_13);
//        filterInfo.add(R.drawable.ic_dr_14);
//        filterInfo.add(R.drawable.ic_dr_15);
//        filterInfo.add(R.drawable.ic_dr_16);
        return filterInfo;
    }

    public ArrayList<Integer> profileFilterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
        filterInfo.add(R.drawable.ic_pf_1);
        filterInfo.add(R.drawable.ic_pf_2);
        filterInfo.add(R.drawable.ic_pf_3);
        filterInfo.add(R.drawable.ic_pf_4);
        filterInfo.add(R.drawable.ic_pf_5);
        filterInfo.add(R.drawable.ic_pf_6);
        filterInfo.add(R.drawable.ic_pf_7);
        filterInfo.add(R.drawable.ic_pf_8);
        filterInfo.add(R.drawable.ic_pf_9);
        filterInfo.add(R.drawable.ic_pf_10);
        filterInfo.add(R.drawable.ic_pf_11);
        filterInfo.add(R.drawable.ic_pf_12);
        filterInfo.add(R.drawable.ic_pf_13);
        filterInfo.add(R.drawable.ic_pf_17);
        filterInfo.add(R.drawable.ic_pf_14);
        filterInfo.add(R.drawable.ic_pf_15);
        filterInfo.add(R.drawable.ic_pf_16);

        return filterInfo;
    }

    public ArrayList<Integer> comicFilterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
        filterInfo.add(R.drawable.ic_com_1);
        filterInfo.add(R.drawable.ic_com_2);
        filterInfo.add(R.drawable.ic_com_3);
        filterInfo.add(R.drawable.ic_com_4);
        filterInfo.add(R.drawable.ic_com_19);
        filterInfo.add(R.drawable.ic_com_6);
        filterInfo.add(R.drawable.ic_com_7);
        filterInfo.add(R.drawable.ic_com_8);
        filterInfo.add(R.drawable.ic_com_18); // 8
        filterInfo.add(R.drawable.ic_com_10);
        filterInfo.add(R.drawable.ic_com_11);
        filterInfo.add(R.drawable.ic_com_12);
        filterInfo.add(R.drawable.ic_com_13);
        filterInfo.add(R.drawable.ic_com_14);
        filterInfo.add(R.drawable.ic_com_15);
        filterInfo.add(R.drawable.ic_com_16);
        filterInfo.add(R.drawable.ic_com_17);
        filterInfo.add(R.drawable.ic_com_9); // 17
        filterInfo.add(R.drawable.ic_com_5);

//        filterInfo.add(R.drawable.ic_com_1);
//        filterInfo.add(R.drawable.ic_com_2);
//        filterInfo.add(R.drawable.ic_com_3);
//        filterInfo.add(R.drawable.ic_com_4);
//        filterInfo.add(R.drawable.ic_com_5);
//        filterInfo.add(R.drawable.ic_com_6);
//        filterInfo.add(R.drawable.ic_com_7);
//        filterInfo.add(R.drawable.ic_com_8);
//        filterInfo.add(R.drawable.ic_com_9);
//        filterInfo.add(R.drawable.ic_com_10);
//        filterInfo.add(R.drawable.ic_com_11);
//        filterInfo.add(R.drawable.ic_com_12);
//        filterInfo.add(R.drawable.ic_com_13);
//        filterInfo.add(R.drawable.ic_com_14);
//        filterInfo.add(R.drawable.ic_com_15);
//        filterInfo.add(R.drawable.ic_com_16);
//        filterInfo.add(R.drawable.ic_com_17);
//        filterInfo.add(R.drawable.ic_com_18);
//        filterInfo.add(R.drawable.ic_com_19);

        return filterInfo;
    }

    public ArrayList<Integer> vintageFilterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
        filterInfo.add(R.drawable.ic_vin_1);
        filterInfo.add(R.drawable.ic_vin_13);
        filterInfo.add(R.drawable.ic_vin_3);
        filterInfo.add(R.drawable.ic_vin_4);
        filterInfo.add(R.drawable.ic_vin_5);
        filterInfo.add(R.drawable.ic_vin_6);
        filterInfo.add(R.drawable.ic_vin_7);
        filterInfo.add(R.drawable.ic_vin_8);
        filterInfo.add(R.drawable.ic_vin_9);
        filterInfo.add(R.drawable.ic_vin_10);
        filterInfo.add(R.drawable.ic_vin_11);
        filterInfo.add(R.drawable.ic_vin_12);
        filterInfo.add(R.drawable.ic_vin_2);
        filterInfo.add(R.drawable.ic_vin_14);
        filterInfo.add(R.drawable.ic_vin_15);
        filterInfo.add(R.drawable.ic_vin_16);

//        filterInfo.add(R.drawable.ic_vin_1);
//        filterInfo.add(R.drawable.ic_vin_2);
//        filterInfo.add(R.drawable.ic_vin_3);
//        filterInfo.add(R.drawable.ic_vin_4);
//        filterInfo.add(R.drawable.ic_vin_5);
//        filterInfo.add(R.drawable.ic_vin_6);
//        filterInfo.add(R.drawable.ic_vin_7);
//        filterInfo.add(R.drawable.ic_vin_8);
//        filterInfo.add(R.drawable.ic_vin_9);
//        filterInfo.add(R.drawable.ic_vin_10);
//        filterInfo.add(R.drawable.ic_vin_11);
//        filterInfo.add(R.drawable.ic_vin_12);
//        filterInfo.add(R.drawable.ic_vin_13);
//        filterInfo.add(R.drawable.ic_vin_14);
//        filterInfo.add(R.drawable.ic_vin_15);
//        filterInfo.add(R.drawable.ic_vin_16);

        return filterInfo;
    }

    /**
     * To get only file name part from whole path
     *
     * @param filePath: file path from where name is to be extracted
     * @return: file name
     */
    public static String extractFileName(String filePath) {
        int lastIndex = filePath.lastIndexOf('/');
        if (lastIndex == -1) {
            return filePath;
        }

        String lastPart = filePath.substring(lastIndex + 1);

        int dotIndex = lastPart.lastIndexOf('.');
        if (dotIndex == -1) {
            return lastPart;
        }

        return lastPart.substring(0, dotIndex);
    }

    /**
     * To convert integer set to string in order to store it in prefs as string set
     *
     * @param intSet: integer set
     * @return: string converted set
     */
    public Set<String> convertSetToStringSet(Set<Integer> intSet) {
        Set<String> stringSet = new HashSet<>();
        for (Integer i : intSet) {
            stringSet.add(String.valueOf(i));
        }
        return stringSet;
    }

    /**
     * To convert string to int set to extract values from it
     *
     * @param stringSet: string set
     * @return: int converted set
     */
    public Set<Integer> convertStringSetToSet(Set<String> stringSet) {
        Set<Integer> intSet = new HashSet<>();
        for (String s : stringSet) {
            try {
                intSet.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // Handle parsing errors, if any
            }
        }
        return intSet;
    }

}
