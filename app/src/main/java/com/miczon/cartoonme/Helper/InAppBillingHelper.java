package com.miczon.cartoonme.Helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.miczon.cartoonme.Listeners.PremiumStatusChangeListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class InAppBillingHelper {

    public static String TAG = "InAppBillingHelper";
    public static InAppBillingHelper mInstance = null;
    private static BillingClient billingClient;
    private static boolean isConEstablished = false;
    public static String PRODUCT_MONTHLY = "monthly_4.99";
    public PrefsManager prefsManager;
    PremiumStatusChangeListener premiumStatusChangeListener;


    public static InAppBillingHelper getInstance() {
        if (mInstance == null) {
            mInstance = new InAppBillingHelper();
        }
        return mInstance;
    }

    public boolean isConnectionEstablished() {
        Log.e(TAG, "inside isConnectionEstablished val is: " + isConEstablished);
        return isConEstablished;
    }

    public void initialiseBillingClient(Context context) {
        Log.e(TAG, "inside initialiseBillingClient");
        prefsManager = new PrefsManager(context);
        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {
                                    Log.e(TAG, "--------------initialiseBillingClient: calling verify sub purchase------------");
                                    verifySubPurchase(purchase, new PremiumStatusChangeListener() {
                                        @Override
                                        public void onPremiumStatusChanged(boolean isPremium) {
                                            if (isPremium) {
                                                prefsManager.setIsPremium(true);
                                            }
                                        }
                                    });
                                }
                            } else {

                            }
                        }
                ).build();
    }

    public void establishConnection() {
        Log.e(TAG, "inside establishConnection");
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    //Use any of function below to get details upon successful connection
                    isConEstablished = true;
                    purchasedSubVerification();
                    Log.e(TAG, "establishConnection: Connection Established");
                } else {

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e(TAG, "establishConnection: Connection NOT Established");
//                establishConnection();
            }
        });
    }

    public void GetSubPurchases(Activity activity) {
        Log.e(TAG, "inside GetSubPurchases");
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
//            if (list.size() > 1) {
                LaunchSubPurchase(list.get(0), activity);
                List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList = list.get(0).getSubscriptionOfferDetails();
                if (subscriptionOfferDetailsList != null) {
                    if (!subscriptionOfferDetailsList.isEmpty()) {
                        Log.e(TAG, "Product Price" + subscriptionOfferDetailsList.get(0).getPricingPhases().
                                getPricingPhaseList().get(0).getFormattedPrice());
                    }
                }
//            }
        });
    }

    private void LaunchSubPurchase(ProductDetails productDetails, Activity activity) {
        Log.e(TAG, "inside LaunchSubPurchase");
        assert productDetails.getSubscriptionOfferDetails() != null;
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        productList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    public void verifySubPurchase(Purchase purchases, PremiumStatusChangeListener callback) {
        Log.e(TAG, "inside verifySubPurchase");
        if (!purchases.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    boolean isPremium = false;
                    for (String pur : purchases.getProducts()) {
                        if (pur.equalsIgnoreCase(PRODUCT_MONTHLY)) {
                            prefsManager.setIsPremium(true);
                            Constants.PURCHASE_VAL = purchases.getPurchaseState();
                            Log.e(TAG, "verifySubPurchase:acknowledged purchase state: " + purchases.getPurchaseState());
                            Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
                            isPremium = true;
                            break;
                        }
                    }
                    callback.onPremiumStatusChanged(isPremium);
                    if (premiumStatusChangeListener != null) {
                        premiumStatusChangeListener.onPremiumStatusChanged(isPremium);
                    }
                } else {
                    prefsManager.setIsPremium(false);
                    Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
                    callback.onPremiumStatusChanged(false);
                }
            });
        } else {
            Log.e(TAG, "verifySubPurchase: purchase state: " + purchases.getPurchaseState());
            Constants.PURCHASE_VAL = purchases.getPurchaseState();
            prefsManager.setIsPremium(true);
            Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
            callback.onPremiumStatusChanged(true);
        }
    }

    public void setPremiumStatusChangeListener(PremiumStatusChangeListener listener) {
        this.premiumStatusChangeListener = listener;
    }


//    public void verifySubPurchase(Purchase purchases) {
//        Log.e(TAG, "inside verifySubPurchase");
//        if (!purchases.isAcknowledged()) {
//            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
//                    .newBuilder()
//                    .setPurchaseToken(purchases.getPurchaseToken())
//                    .build(), billingResult -> {
//
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    for (String pur : purchases.getProducts()) {
//                        if (pur.equalsIgnoreCase(PRODUCT_MONTHLY)) {
//                            prefsManager.setIsPremium(true);
//                            Constants.PURCHASE_VAL = purchases.getPurchaseState();
//                            Log.e(TAG, "verifySubPurchase:acknowledged purchase state: " + purchases.getPurchaseState());
//                            Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
//                        }
//                    }
//                } else {
//                    prefsManager.setIsPremium(false);
//                    Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
//                }
//            });
//        } else {
//            Log.e(TAG, "verifySubPurchase: purchase state: " + purchases.getPurchaseState());
//            Constants.PURCHASE_VAL = purchases.getPurchaseState();
//            prefsManager.setIsPremium(true);
//            Log.e(TAG, "verifySubPurchase: pref val: " + prefsManager.getIsPremium());
//        }
//    }

    /**
     * <p>
     * To get status of subscription i.e. if it is purchased or not
     * </p>
     */
    public void purchasedSubVerification() {
        Log.e(TAG, "inside purchasedSubVerification");
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult, list) -> {

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (list.size() == 0) {
                            prefsManager.setIsPremium(false);
                            Log.e(TAG, "purchasedSubVerification: pref val: " + prefsManager.getIsPremium());
                        }

                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                Log.e(TAG, "--------------purchasedSubVerification: calling verify sub purchase------------");
                                verifySubPurchase(purchase, new PremiumStatusChangeListener() {
                                    @Override
                                    public void onPremiumStatusChanged(boolean isPremium) {
                                        if (isPremium) {
                                            prefsManager.setIsPremium(true);
                                        }
                                    }
                                });

                            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                                prefsManager.setIsPremium(false);
                                Log.e(TAG, "PurchaseState.PENDING: pref val: " + prefsManager.getIsPremium());

                            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                                prefsManager.setIsPremium(false);
                                Log.e(TAG, "PurchaseState.UNSPECIFIED_STATE: pref val: " + prefsManager.getIsPremium());

                            } else {
                                Constants.PURCHASE_VAL = purchase.getPurchaseState();
                                Log.e(TAG, "purchasedSubVerification: constants: " + Constants.PURCHASE_VAL);
                                prefsManager.setIsPremium(true);
                                Log.e(TAG, "purchasedSubVerification: pref val: " + prefsManager.getIsPremium());
                            }
                        }
                    } else {
                        Log.e(TAG, "purchasedSubVerification: billing code in verification: " + billingResult.getResponseCode());
                        Constants.PURCHASE_VAL = billingResult.getResponseCode();
                    }
                }
        );
    }

    //Call this function using PRODUCT_PREMIUM or PRODUCT_MONTHLY as parameters.
//    void GetListsSubDetail(String SKU) {
//        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
//
//        //Set your In App Product ID in setProductId()
//        for (String ids : purchaseItemIDs) {
//            productList.add(
//                    QueryProductDetailsParams.Product.newBuilder()
//                            .setProductId(ids)
//                            .setProductType(BillingClient.ProductType.SUBS)
//                            .build());
//        }
//
//        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
//                .setProductList(productList)
//                .build();

//        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
//            @Override
//            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
////                Log.d(TAG, "Total size is: " + list);
//
//                for (ProductDetails li : list) {
//                    if (li.getProductId().equalsIgnoreCase(SKU) && SKU.equalsIgnoreCase(PRODUCT_MONTHLY)) {
//                        LaunchSubPurchase(li);
//                        Log.e(TAG, "Monthly Price is " + li.getSubscriptionOfferDetails().get(0).getPricingPhases().
//                                getPricingPhaseList().get(0).getFormattedPrice());
//                        return;
//                    }
//
//                }
//                //Do Anything that you want with requested product details
//            }
//        });
//    }

//    void restorePurchases() {
//
//        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {
//        }).build();
//        final BillingClient finalBillingClient = billingClient;
//        billingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingServiceDisconnected() {
//            }
//
//            @Override
//            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
//
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    finalBillingClient.queryPurchasesAsync(
//                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
//                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                                    if (list.size() > 0) {
//                                        for (int i = 0; i < list.size(); i++) {
//                                            if (list.get(i).getProducts().contains(PRODUCT_MONTHLY)) {
//                                                tv_status.setText("Premium Restored");
//                                                Log.d("TAG", "Product id " + PRODUCT_MONTHLY + " will restore here");
//                                            }
//                                        }
//                                    } else {
//                                        tv_status.setText("Nothing found to Restored");
//                                    }
//                                }
//                            });
//                }
//            }
//        });
//    }


}
