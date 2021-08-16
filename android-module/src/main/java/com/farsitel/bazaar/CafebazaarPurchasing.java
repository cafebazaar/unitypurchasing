package com.farsitel.bazaar;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.unity.purchasing.common.IStoreCallback;
import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.common.UnityPurchasing;
import java.lang.reflect.Field;
public class CafebazaarPurchasing {
    public static String TAG = "FarsiSell";
    public static PoolakeyBridge poolakey;
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
        poolakey = new PoolakeyBridge(context, unityCallback);
        poolakey.connect(context, null);
    }

    public void RetrieveProducts(String json) {
        poolakey.retrieveProducts(json);
    }

    public void Purchase(String productJSON, String developerPayload) {
        log("Purchase " + productJSON);
    }

    static public void onPurchaseSucceeded(String productId, String receipt, String transactionId) {
    }

    public void FinishTransaction(String productJSON, String transactionID) {
        log("Finishing transaction " + transactionID);
    }
}
