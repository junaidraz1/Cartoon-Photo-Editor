package com.miczon.cartoonme.Retrofit;

import android.app.Activity;

import com.miczon.cartoonme.Utils.Constants;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class apiUtils {

    public static apiInterface getAPIService(Activity activity) {
//        try {
//            HomeActivity.getRemoteConfigs(activity);
//
//        } catch (Exception e) {
//            Log.e("Firebase Remote Exp", e.toString());
//        }
        //Log.e("RemoteConfigs", RemoteConfigs.baseurlLive);
        return apiClient.getClient(activity, Constants.baseURL).create(apiInterface.class);
    }
}
