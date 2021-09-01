package com.farsitel.bazaar;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.unity.purchasing.common.IStoreCallback;
import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.common.ProductDefinition;
import com.unity.purchasing.common.ProductType;
import com.unity.purchasing.common.PurchaseFailureDescription;
import com.unity.purchasing.common.PurchaseFailureReason;
import com.unity.purchasing.common.UnityPurchasing;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CafebazaarPurchasing {
    public static String TAG = "FarsiSell";
    public static IStoreCallback unityCallback;
    private static CafebazaarPurchasing instance;

    public static void log(String message) {
        Log.i(TAG, message);
    }

    public static CafebazaarPurchasing instance(IUnityCallback bridge) {
        if (instance == null) {
            instance = new CafebazaarPurchasing(new UnityPurchasing(bridge));
        }
        return instance;
    }

    public Activity getActivity() {
        try {
            // Using reflection to remove reference to Unity library.
            Class<?> mUnityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            Field mUnityPlayerActivityField = mUnityPlayerClass.getField("currentActivity");
            return (Activity) mUnityPlayerActivityField.get(mUnityPlayerClass);
        } catch (ClassNotFoundException e) {
            log("Could not find UnityPlayer class: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            log("Could not find currentActivity field: " + e.getMessage());
        } catch (Exception e) {
            log("Unknown exception occurred finding getActivity: " + e.getMessage());
        }
        return null;
    }

    public CafebazaarPurchasing(IStoreCallback callback) {
        unityCallback = callback;
        Context context = getActivity().getApplicationContext();

        billingClient = BillingClient.newBuilder(getActivity().getApplication()).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(this);
    }

    @Override
    public void onBillingServiceDisconnected() {
        log("onBillingServiceDisconnected");
        unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable);
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        log("onBillingSetupFinished: " + responseCode + " " + debugMessage);

        if (pendingJsonProducts != null)
            RetrieveProducts(pendingJsonProducts);
    }

    public void RetrieveProducts(String json) {

    }

    public void Purchase(String productJSON, String developerPayload) {
        log("Purchase " + productJSON);
        ProductDefinition product = null;
        try {
            JSONObject json = new JSONObject(productJSON);
            product = new ProductDefinition(
                    json.getString("storeSpecificId"),
                    ProductType.valueOf(json.getString("type")));
        } catch (JSONException e) {
            e.printStackTrace();
            PurchaseFailureDescription description = new PurchaseFailureDescription("", PurchaseFailureReason.BillingUnavailable, "Json is invalid.", "");
            CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description);
            return;
        }
    }


    public void FinishTransaction(String productJSON, String transactionID) {
        log("Finishing transaction " + transactionID);
    }
}
