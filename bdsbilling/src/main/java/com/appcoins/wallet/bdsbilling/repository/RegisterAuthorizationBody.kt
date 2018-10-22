package com.appcoins.wallet.bdsbilling.repository

import com.google.gson.annotations.SerializedName

data class RegisterAuthorizationBody(@SerializedName("product.name") val productName: String,
                                     @SerializedName("package.name") val packageName: String,
                                     val token: String, @SerializedName("wallets.developer")
                                     val developerWallet: String,
                                     @SerializedName("wallets.store") val storeWallet: String,
                                     @SerializedName("payload") val developerPayload: String?)
