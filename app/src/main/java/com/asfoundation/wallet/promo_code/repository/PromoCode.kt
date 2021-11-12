package com.asfoundation.wallet.promo_code.repository

data class PromoCode(val code: String?,
                     val bonus: Double?,
                     val expiryDate: String?,
                     val expired: Boolean?)