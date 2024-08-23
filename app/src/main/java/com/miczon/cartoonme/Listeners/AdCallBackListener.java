package com.miczon.cartoonme.Listeners;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

/**
 * To Handle call backs of Interstitial Ad
 */
public interface AdCallBackListener {

    void onAdDismissedOrFinished(String from);

    void onAdShowedFullScreenContent();

    void onAdFailedToShowFullScreenContent();

}
