package com.appcoins.wallet.feature.changecurrency.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FiatCurrencyEntity(
  @field:PrimaryKey @ColumnInfo(name = "currency") val currency: String,
  @ColumnInfo(name = "flag") val flag: String?,
  @ColumnInfo(name = "label") val label: String?,
  @ColumnInfo(name = "sign") val sign: String?
)
