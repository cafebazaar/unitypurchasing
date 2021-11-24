package com.farsitel.bazaar;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.unity.purchasing.common.IStoreCallback;
import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.common.InitializationFailureReason;
import com.unity.purchasing.common.ProductDefinition;
import com.unity.purchasing.common.ProductDescription;
import com.unity.purchasing.common.ProductMetadata;
import com.unity.purchasing.common.ProductType;
import com.unity.purchasing.common.PurchaseFailureDescription;
import com.unity.purchasing.common.PurchaseFailureReason;
import com.unity.purchasing.common.UnityPurchasing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CafebazaarPurchasing implements PurchasesResponseListener, BillingClientStateListener,
        SkuDetailsResponseListener, PurchasesUpdatedListener {

    public static String TAG = "FarsiSell";
    public static IStoreCallback unityCallback;
    private static CafebazaarPurchasing instance;

    private final BillingClient billingClient;
    private String pendingJsonProducts = null;
    private Map<String, SkuDetails> skusDetails;
    private HashMap<String, Purchase> purchasesMap;
    private Map<String, ProductDefinition> definedProducts;

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
        pendingJsonProducts = json;
        if (!billingClient.isReady) {
            unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable);
            return;
        }

        // Create defined products
        List<String> skusList = new ArrayList<>();
        definedProducts = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(pendingJsonProducts);
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject value = jsonArray.getJSONObject(i);
                ProductDefinition product = new ProductDefinition(value.getString("storeSpecificId"),
                        ProductType.valueOf(value.getString("type")));
                definedProducts.put(product.id, product);
                skusList.add(product.id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pendingJsonProducts = null;

        // Query SkuDetails
        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skusList).setType(null).build();
        billingClient.querySkuDetailsAsync(params, this);
    }

    @Override
    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skusList) {
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable);
            return;
        }
        skusDetails = new HashMap<>();
        for (SkuDetails skuDetails : skusList) {
            skusDetails.put(skuDetails.getSku(), skuDetails);
        }

        // Query Purchases
        billingClient.queryPurchasesAsync(null, this);
    }

    @Override
    public void onPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable);
            return;
        }

        purchasesMap = new HashMap<>();
        for (Purchase purchase : purchases) {
            purchasesMap.put(purchase.getSkus().get(0), purchase);
        }

        List<ProductDescription> productDescriptions = new ArrayList<>();
        for (Map.Entry<String, SkuDetails> entry : skusDetails.entrySet()) {
            ProductMetadata metadata = new ProductMetadata(
                    entry.getValue().getPrice(),
                    entry.getValue().getTitle(),
                    entry.getValue().getDescription(),
                    "IRR",
                    new BigDecimal(parsePrice(entry.getValue().getPrice()))
            );
            String receipt = "";
            String transactionId = "";
            Purchase purchase = purchasesMap.get(entry.getKey());
            if (purchase != null) {
                receipt = purchase.getPurchaseToken();
                transactionId = purchase.getOrderId();
            }
            productDescriptions.add(new ProductDescription(entry.getKey(), metadata, receipt, transactionId));
            if (entry.getValue().getType().equals(ProductType.Consumable)) {
                unityCallback.OnPurchaseSucceeded(entry.getKey(), purchase.getPurchaseToken(), purchase.getOrderId());
            }
        }

        unityCallback.OnProductsRetrieved(productDescriptions);
    }


    public void Purchase(String productJSON, String developerPayload) {
        log("Purchase " + productJSON);
        ProductDefinition product = getProductFromJson(productJSON);
        if (product == null) {
            PurchaseFailureDescription description = new PurchaseFailureDescription("", PurchaseFailureReason.BillingUnavailable, "Json is invalid.", "");
            CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description);
            return;
        }
        BillingResult billingResult = billingClient.launchBillingFlow(getActivity(),
                BillingFlowParams.newBuilder().setSkuDetails(skusDetails.get(product.id)).build());
        log("onPurchaseStarted: " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        log("onPurchasesUpdated: " + billingResult.getResponseCode() + " " + purchases.toString());
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            unityCallback.OnPurchaseFailed(new PurchaseFailureDescription(billingResult.getDebugMessage(), PurchaseFailureReason.Unknown));
            return;
        }
        for (Purchase purchase : purchases) {
//            if (!Security.verifyPurchase(purchase.getOriginalJson(), purchase.getSignature())) {
//                Log.e(TAG, "Invalid signature on purchase. Check to make " +
//                        "sure your public key is correct.");
//                continue;
//            }
            purchasesMap.put(purchase.getSkus().get(0), purchase);
            unityCallback.OnPurchaseSucceeded(purchase.getSkus().get(0), purchase.getPurchaseToken(), purchase.getOrderId());
        }
    }

    public void FinishTransaction(String productJSON, String transactionID) {
        log("Finishing transaction " + productJSON + " - " + transactionID);
        ProductDefinition product = getProductFromJson(productJSON);
        if (product == null || !product.type.equals(ProductType.Consumable)) {
            return;
        }
        Purchase purchase = purchasesMap.get(product.id);
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.consumeAsync(consumeParams, (consumeResult, outToken) -> {
            log("Consume " + productJSON + " - " + consumeResult);
                });
    }

    private String parsePrice(String price) {
        String[] pre = price.split(" ریال");
        String _price = pre[0]
                .replace(",", "")
                .replace('٠', '0')
                .replace('١', '1')
                .replace('٢', '2')
                .replace('٣', '3')
                .replace('۴', '4')
                .replace('۵', '5')
                .replace('۶', '6')
                .replace('٧', '7')
                .replace('٨', '8')
                .replace('٩', '9');

        return _price.equals("صفر") ? "0" : _price;
    }

    private ProductDefinition getProductFromJson(String productJSON) {
        ProductDefinition product;
        try {
            JSONObject json = new JSONObject(productJSON);
            product = new ProductDefinition(
                    json.getString("storeSpecificId"),
                    ProductType.valueOf(json.getString("type")));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return product;
    }
}
