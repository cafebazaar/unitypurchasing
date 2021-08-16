package com.farsitel.bazaar

import android.content.Context
import android.util.Log
import com.unity.purchasing.common.*
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import java.util.*

class PoolakeyBridge(context: Context, private val unityCallback: IStoreCallback) {

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

}