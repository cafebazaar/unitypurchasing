package com.sample.android.trivialdrivesample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.farsitel.bazaar.BazaarPurchasing;
import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.common.UnityPurchasing;

public class MainActivity extends Activity {

    private BazaarPurchasing purchasing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BazaarPurchasing.testActivity = this;
    }

    public void init(View view){
        purchasing = BazaarPurchasing.instance(new IUnityCallback(){
            @Override
            public void OnSetupFailed(String message) {
                Log.i(BazaarPurchasing.TAG, "OnSetupFailed: " + message);
            }

            @Override
            public void OnProductsRetrieved(String message) {
                Log.i(BazaarPurchasing.TAG, "OnProductsRetrieved: " + message);
            }

            @Override
            public void OnPurchaseSucceeded(String s, String s1, String s2) {
                Log.i(BazaarPurchasing.TAG, "OnPurchaseSucceeded: " + s + ", " + s1);
            }

            @Override
            public void OnPurchaseFailed(String message) {
                Log.i(BazaarPurchasing.TAG, "OnPurchaseFailed: " + message);
            }
        });
    }

    public void retrieveProducts(View view){
        purchasing.RetrieveProducts("[{\"id\":\"gas\",\"storeSpecificId\":\"gas\",\"type\":\"Consumable\",\"enabled\":true,\"payouts\":[]},{\"id\":\"premium\",\"storeSpecificId\":\"premium\",\"type\":\"NonConsumable\",\"enabled\":true,\"payouts\":[]},{\"id\":\"infinite_gas_monthly\",\"storeSpecificId\":\"infinite_gas_monthly\",\"type\":\"Subscription\",\"enabled\":true,\"payouts\":[]}]");
    }
}