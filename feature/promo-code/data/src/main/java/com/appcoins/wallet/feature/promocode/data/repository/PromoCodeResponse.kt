package com.appcoins.wallet.feature.promocode.data.repository

data class PromoCodeResponse(val code: String, val expiry: String?, val expired: Boolean)