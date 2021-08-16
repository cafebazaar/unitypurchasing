package com.farsitel.bazaar

import android.content.Context
import android.util.Log
import com.unity.purchasing.common.*
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.entity.SkuDetails
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap

class PoolakeyBridge(context: Context, private val unityCallback: IStoreCallback) {

    lateinit var definedProducts: MutableList<ProductDefinition>
    lateinit var payment: Payment
    lateinit var connection: Connection
    var isConnected: Boolean = false
    var pendingJsonProducts: String? = null
    fun log(message: String?) {
        Log.i(CafebazaarPurchasing.TAG, message)
    }

    fun connect(context: Context, rsaPublicKey: String?) {
        val securityCheck = if (rsaPublicKey != null) {
            SecurityCheck.Enable(rsaPublicKey)
        } else {
            SecurityCheck.Disable
        }
        val paymentConfig = PaymentConfiguration(localSecurityCheck = securityCheck)
        payment = Payment(context = context, config = paymentConfig)
        connection = payment.connect {
            connectionFailed {
                isConnected = false
                unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable)
            }
            connectionSucceed {
                isConnected = true
                log("connectionSucceed")
                pendingJsonProducts?.let { retrieveProducts(it) }
            }
            disconnected {
                isConnected = false
                connectionFailed { unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable) }
            }
        }
    }

    fun disconnect() {
        connection.disconnect();
    }

    fun retrieveProducts(json: String) {
        log("retrieveProducts $json")
        pendingJsonProducts = json
        if (!isConnected) {
            return
        }

        // Create defined products
        definedProducts = ArrayList()
        var products = ArrayList<String>()
        var subscriptions = ArrayList<String>()
        val jsonArray = JSONArray(pendingJsonProducts)
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray[i] as JSONObject
            var product = ProductDefinition(
                value.getString("storeSpecificId"),
                ProductType.valueOf(value.getString("type"))
            )
            definedProducts.add(product)
            if (product.type.equals(ProductType.Subscription))
                subscriptions.add(product.id);
            else
                products.add(product.id);
        }
        pendingJsonProducts = null

        // 1 ---> Get purchases products
        var purchasesMap = HashMap<String, PurchaseInfo>()
        payment.getPurchasedProducts {
            querySucceed { purchasedProducts ->
                addToPurchases(purchasedProducts, purchasesMap)

                // 2 ---> Get purchases subscriptions
                payment.getSubscribedProducts {
                    querySucceed { subscribedProducts ->
                        addToPurchases(subscribedProducts, purchasesMap)

                        // 3 ---> SKU details of products
                        val productsDescriptions = ArrayList<ProductDescription>()
                        payment.getInAppSkuDetails(skuIds = products) {
                            getSkuDetailsSucceed { productDetails ->
                                addToDescriptions(
                                    productDetails,
                                    productsDescriptions,
                                    purchasesMap
                                )

                                // 4 ---> SKU details of subscriptions
                                payment.getSubscriptionSkuDetails(skuIds = subscriptions) {
                                    getSkuDetailsSucceed { subscriptionSkuDetails ->
                                        addToDescriptions(
                                            subscriptionSkuDetails,
                                            productsDescriptions,
                                            purchasesMap
                                        )
                                        unityCallback.OnProductsRetrieved(productsDescriptions)
                                    }
                                    getSkuDetailsFailed {
                                        unityCallback.OnSetupFailed(
                                            InitializationFailureReason.NoProductsAvailable
                                        )
                                    }
                                }
                            }
                            getSkuDetailsFailed {
                                unityCallback.OnSetupFailed(
                                    InitializationFailureReason.NoProductsAvailable
                                )
                            }
                        }
                    }
                    queryFailed { unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable) }
                }
            }
            queryFailed { unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable) }
        }
    }

    private fun addToPurchases(
        purchases: List<PurchaseInfo>,
        purchasesMap: HashMap<String, PurchaseInfo>
    ) {
        for (purchase in purchases) {
            purchasesMap[purchase.productId] = purchase
        }
    }

    private fun addToDescriptions(
        skuDetails: List<SkuDetails>,
        productDescriptions: ArrayList<ProductDescription>,
        purchasesMap: HashMap<String, PurchaseInfo>
    ) {
        for (sku in skuDetails) {
            val metadata = ProductMetadata(
                sku.price,
                sku.title,
                sku.description,
                "IRR",
                BigDecimal(parsePrice(sku.price))
            )
            var receipt = ""
            var transactionId = ""
            val purchase = purchasesMap[sku.sku]
            if (purchase != null) {
                receipt = purchase.purchaseToken
                transactionId = purchase.orderId
            }
            productDescriptions.add(ProductDescription(sku.sku, metadata, receipt, transactionId))
        }
    }

    private fun parsePrice(price: String): String? {
        return "110"
    }

    fun consume(productId: String, receipt: String, transactionId: String) {
        log("consume $productId")
        if (connection.getState() != ConnectionState.Connected) {
            val description = PurchaseFailureDescription(
                productId,
                PurchaseFailureReason.SignatureInvalid,
                "Connection not found.",
                ""
            )
            unityCallback.OnPurchaseFailed(description)
            log(
                "Connection not found. In order to consumption, connect to Poolakey!"
            )
            return
        }

        payment.consumeProduct(receipt) {
            consumeSucceed {
                unityCallback.OnPurchaseSucceeded(
                    productId,
                    receipt,
                    transactionId
                )
            }
            consumeFailed { throwable ->
                val description = PurchaseFailureDescription(
                    productId,
                    PurchaseFailureReason.BillingUnavailable,
                    throwable.message,
                    ""
                )
                unityCallback.OnPurchaseFailed(description)
            }
        }
    }

    fun getDefinedProduct(id: String): ProductDefinition? {
        return definedProducts.find { p -> p.id == id }
    }
}