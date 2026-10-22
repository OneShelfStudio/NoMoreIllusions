package com.oneshelfstudio.nomoreillusions

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BillingManager(private val context: Context, private val onProductPurchased: (String) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val billingClient: BillingClient
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    var onUiPurchaseListener: ((String) -> Unit)? = null

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        connectToBilling()
    }

    private fun notifyPurchase(productId: String) {
        scope.launch {
            onProductPurchased(productId)
            onUiPurchaseListener?.invoke(productId)
        }
    }

    private fun connectToBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun queryProducts() {
        val skus = listOf("points_10000", "full_album_unlock") + (2..MusicLibrary.TOTAL_TRACKS).map { "track_${it.toString().padStart(2, '0')}" }
        val productList = skus.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, queryProductDetailsResult ->
            queryProductDetailsResult.productDetailsList.forEach { details ->
                productDetailsMap[details.productId] = details
            }
        }
    }

    fun purchaseProduct(activity: Activity, productId: String) {
        val details = productDetailsMap[productId]
        if (details != null && billingClient.isReady) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        } else {
            // Demo / Debug fallback when Play Console SKUs are not yet live
            val label = when {
                productId == "full_album_unlock" -> "No More Illusions Full Album Unlocked!"
                productId == "points_10000" -> "+1,000 Pulse Points Added!"
                productId.startsWith("track_") -> "Track ${productId.removePrefix("track_")} Unlocked!"
                else -> "Unlocked!"
            }
            Toast.makeText(activity, label, Toast.LENGTH_SHORT).show()
            notifyPurchase(productId)
        }
    }

    fun purchasePoints(activity: Activity) = purchaseProduct(activity, "points_10000")

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val isConsumable = purchase.products.contains("points_10000")
            if (isConsumable) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchase.products.forEach { productId ->
                            notifyPurchase(productId)
                        }
                    }
                }
            } else if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchase.products.forEach { productId ->
                            notifyPurchase(productId)
                        }
                    }
                }
            } else {
                purchase.products.forEach { productId ->
                    notifyPurchase(productId)
                }
            }
        }
    }

    fun release() {
        onUiPurchaseListener = null
        scope.cancel()
        billingClient.endConnection()
    }
}
