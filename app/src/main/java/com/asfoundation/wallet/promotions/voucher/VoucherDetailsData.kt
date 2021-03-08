package com.asfoundation.wallet.promotions.voucher

data class VoucherDetailsData(val title: String, val featureGraphic: String, val icon: String,
                              val maxBonus: Double, val packageName: String,
                              val hasAppcoins: Boolean)