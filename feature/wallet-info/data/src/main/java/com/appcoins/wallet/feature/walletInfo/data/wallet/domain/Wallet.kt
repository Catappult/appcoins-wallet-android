package com.appcoins.wallet.feature.walletInfo.data.wallet.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Wallet(val address: String) : Parcelable {

  fun hasSameAddress(address: String): Boolean = this.address.equals(address, ignoreCase = true)
}