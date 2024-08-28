package com.asfoundation.wallet.iab.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseData(
  val domain: String = ""
) : Parcelable