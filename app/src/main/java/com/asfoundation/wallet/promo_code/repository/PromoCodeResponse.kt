package com.asfoundation.wallet.promo_code.repository

data class PromoCodeResponse(val code: String, val expiry: String?, val expired: Boolean? = null)