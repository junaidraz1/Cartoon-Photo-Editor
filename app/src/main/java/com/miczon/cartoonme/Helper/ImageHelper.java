package com.miczon.cartoonme.Helper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class ImageHelper {
    public static String TAG = "ImageHelper";

    /**
     * Method to resize bitmap from uri
     * @param imageUri: uri of image to be resized
     * @param contentResolver:
     * @param imageMaxSideLength: max length to set after resize
     * @return: resize bitmap
     */
    public static Bitmap loadSizeLimitedBitmapFromUri(Uri imageUri, ContentResolver contentResolver, int imageMaxSideLength) {
        try {
            InputStream imageInputStream = contentResolver.openInputStream(imageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Rect outPadding = new Rect();
            BitmapFactory.decodeStream(imageInputStream, outPadding, options);

            int maxSideLength = Math.max(options.outWidth, options.outHeight);
            options.inSampleSize = 1;
            options.inSampleSize = calculateSampleSize(maxSideLength, imageMaxSideLength);
            options.inJustDecodeBounds = false;
            imageInputStream.close();

            imageInputStream = contentResolver.openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageInputStream, outPadding, options);

            int orientation = getOrientation(contentResolver, imageUri);
            Log.e(TAG, "loadSizeLimitedBitmapFromUri: orientation: " + orientation);
            bitmap = rotateBitmap(bitmap, orientation);

            maxSideLength = Math.max(bitmap.getWidth(), bitmap.getHeight());
            double ratio = imageMaxSideLength / (double) maxSideLength;
            if (ratio < 1) {
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio), false);
            }

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "loadSizeLimitedBitmapFromUri: exception: " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * To calculate initial size of image
     * @param maxSideLength: max length
     * @param expectedMaxImageSideLength: after resize expected length
     * @return: size
     */
    private static int calculateSampleSize(int maxSideLength, int expectedMaxImageSideLength) {
        int inSampleSize = 1;

        while (maxSideLength > 2 * expectedMaxImageSideLength) {
            maxSideLength /= 2;
            inSampleSize *= 2;
        }

        return inSampleSize;
    }

    /**
     * To get orientation of picture
     * @param contentResolver:
     * @param imageUri: Uri of image
     * @return: orientation
     * @throws: IOException
     */
    private static int getOrientation(ContentResolver contentResolver, Uri imageUri) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(imageUri);
        ExifInterface exifInterface = new ExifInterface(inputStream);
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        inputStream.close();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * To rotate bitmap if it is not in correct orientation by default
     * @param bitmap: bitmap to be rotated
     * @param degrees: degrees to be rotated
     * @return: rotated bitmap
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0) {
            Log.e(TAG, "rotateBitmap: degrees: " + degrees);
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            Log.e(TAG, "rotateBitmap: degrees: " + degrees);
        }
        return bitmap;
    }

}

