package com.appcoins.wallet.feature.promocode.data.repository

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PromoCodeEntity(
  @PrimaryKey @NonNull val code: String,
  val bonus: Double?,
  val validityState: Int?,
  val appName: String?,
  val appPackageName: String?,
  val appIcon: String?
)
