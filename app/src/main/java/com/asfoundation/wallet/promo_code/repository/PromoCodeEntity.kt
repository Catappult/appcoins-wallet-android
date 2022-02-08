package com.asfoundation.wallet.promo_code.repository

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PromoCodeEntity(@PrimaryKey @NonNull val code: String, val bonus: Double?,
                           val expiryDate: String?, val expired: Boolean?, val appName: String?,
                           val appPackageName: String?, val appIcon: String?)
