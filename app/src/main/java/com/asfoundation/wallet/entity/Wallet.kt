package com.asfoundation.wallet.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Wallet(val address: String) : Parcelable {

  fun hasSameAddress(address: String): Boolean = this.address == address
}