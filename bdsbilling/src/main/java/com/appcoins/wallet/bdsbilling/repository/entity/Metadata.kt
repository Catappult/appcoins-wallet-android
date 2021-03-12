package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName

data class Metadata(val voucher: Voucher?)

data class Voucher(val code: String, @SerializedName("redeem_url") val redeem: String)