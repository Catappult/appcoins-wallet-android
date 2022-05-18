package com.asfoundation.wallet.promo_code.repository

import java.io.Serializable

data class PromoCode(val code: String?, val bonus: Double?, val expiryDate: String?,
                     val expired: Boolean?, val appName: String?) : Serializable