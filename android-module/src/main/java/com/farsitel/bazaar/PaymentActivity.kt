package com.farsitel.bazaar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.unity.purchasing.common.PurchaseFailureDescription
import com.unity.purchasing.common.PurchaseFailureReason
import ir.cafebazaar.poolakey.exception.DynamicPriceNotSupportedException
import ir.cafebazaar.poolakey.request.PurchaseRequest
import java.security.InvalidParameterException

class PaymentActivity : FragmentActivity() {
    private var productId: String? = null
    private var payload: String? = null
    private var dynamicPriceToken: String? = null
    private var command: Command? = null

    fun log(message: String?) {
        Log.i(CafebazaarPurchasing.TAG, message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArgs()
        when (command) {
            Command.Purchase -> purchaseProduct()
            Command.Subscribe -> subscribeProduct()
            else -> throw InvalidParameterException("Undefined command: $command")
        }
    }

    private fun initArgs() {
        productId = intent.extras?.getString(KEY_PRODUCT_ID)
        payload = intent.extras?.getString(KEY_PAYLOAD)
        dynamicPriceToken = intent.extras?.getString(KEY_DYNAMIC_PRICE_TOKEN)
        command = Command.valueOf(requireNotNull(intent.extras?.getString(KEY_COMMAND)))
    }

    private fun purchaseProduct() {
        CafebazaarPurchasing.poolakey.payment.purchaseProduct(
            this@PaymentActivity,
            PurchaseRequest(productId!!, REQUEST_CODE, payload, dynamicPriceToken)
        ) {
            purchaseFlowBegan {
                log("Bazaar's billing screen has opened successfully")
//                paymentCallback?.onStart()
            }
            failedToBeginFlow { throwable ->
                // Failed to open Bazaar's billing screen
                if (throwable is DynamicPriceNotSupportedException) {
                    dynamicPriceToken = null
                    purchaseProduct()
                } else {
                    val description = PurchaseFailureDescription(productId, PurchaseFailureReason.Unknown, "Purchase failed.", throwable.stackTrace.joinToString("\n"))
                    CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description)                }
                finish()
            }
        }
    }

    private fun subscribeProduct() {
        CafebazaarPurchasing.poolakey.payment.subscribeProduct(
            this@PaymentActivity,
            PurchaseRequest(productId!!, REQUEST_CODE, payload, dynamicPriceToken)
        ) {
            purchaseFlowBegan {
                log("Bazaar's billing screen has opened successfully")
//                paymentCallback?.onStart()
            }
            failedToBeginFlow { throwable ->
                // Failed to open Bazaar's billing screen
                if (throwable is DynamicPriceNotSupportedException) {
                    dynamicPriceToken = null
                    subscribeProduct()
                } else {
                    val description = PurchaseFailureDescription(productId, PurchaseFailureReason.Unknown, "Purchase failed.", throwable.stackTrace.joinToString("\n"))
                    CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        CafebazaarPurchasing.poolakey.payment.onActivityResult(requestCode, resultCode, data) {
            purchaseSucceed { purchaseInfo ->
                // User purchased the product
                CafebazaarPurchasing.onPurchaseSucceeded(purchaseInfo.productId, purchaseInfo.purchaseToken, purchaseInfo.orderId)
                finish()
            }
            purchaseCanceled {
                // User canceled the purchase
                val description = PurchaseFailureDescription(requestCode.toString(), PurchaseFailureReason.UserCancelled, "User canceled the purchase.", resultCode.toString())
                CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description)
                finish()
            }
            purchaseFailed { throwable ->
                val description = PurchaseFailureDescription(requestCode.toString(), PurchaseFailureReason.PaymentDeclined, "Purchase failed.", resultCode.toString())
                CafebazaarPurchasing.unityCallback.OnPurchaseFailed(description)
                finish()
            }
        }
    }

    companion object {

        private const val REQUEST_CODE: Int = 1000
        private const val KEY_PRODUCT_ID = "productId"
        private const val KEY_PAYLOAD = "payload"
        private const val KEY_DYNAMIC_PRICE_TOKEN = "dynamicPriceToken"
        private const val KEY_COMMAND = "command"

        @JvmStatic
        fun start(
            activity: Activity,
            command: Command,
            productId: String,
            payload: String?,
            dynamicPriceToken: String?
        ) {
            val intent = Intent(activity, PaymentActivity::class.java)
            intent.putExtra(KEY_PRODUCT_ID, productId)
            intent.putExtra(KEY_PAYLOAD, payload)
            intent.putExtra(KEY_DYNAMIC_PRICE_TOKEN, dynamicPriceToken)
            intent.putExtra(KEY_COMMAND, command.name)
            activity.startActivity(intent)
        }
    }

    enum class Command {
        Purchase,
        Subscribe
    }
}