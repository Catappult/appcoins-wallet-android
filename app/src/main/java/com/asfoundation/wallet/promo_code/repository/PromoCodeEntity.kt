package com.asfoundation.wallet.promo_code.repository

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PromoCodeEntity(@field:PrimaryKey @ColumnInfo(name = "code") @NonNull val code: String,
                           @ColumnInfo(name = "bonus") val bonus: Double?,
                           @ColumnInfo(name = "expiryDate") val expiryDate: String?,
                           @ColumnInfo(name = "expired") val expired: Boolean?)
