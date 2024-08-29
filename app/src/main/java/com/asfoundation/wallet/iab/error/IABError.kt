package com.asfoundation.wallet.iab.error

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IABError(
  val errorMessage: String
) : Parcelable
