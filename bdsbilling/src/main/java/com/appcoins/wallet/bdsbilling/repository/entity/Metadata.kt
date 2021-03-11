package com.appcoins.wallet.bdsbilling.repository.entity

data class Metadata(val voucher: Voucher?)

data class Voucher(val code: String, val redeem: String)