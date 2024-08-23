package com.miczon.cartoonme.Manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class ConnectionManager {

    public static ConnectionManager mInstance = null;

    /**
     * Singleton instance method
     *
     * @return instance as object
     */
    public static ConnectionManager getInstance() {
        if (mInstance == null) {
            mInstance = new ConnectionManager();
        }
        return mInstance;
    }

    /**
     * Method to check if internet connection is available or not
     * @param context: from where it is called
     * @return: flag to check if internet is available or not
     */
    public boolean isNetworkAvailable(Context context) {
        boolean isAvailable = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                // Connected to a network
                isAvailable = true;
            }
        }
        return isAvailable;
    }
}
