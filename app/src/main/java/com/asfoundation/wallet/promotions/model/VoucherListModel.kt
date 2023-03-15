package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.core.utils.common.Error

data class VoucherListModel(val vouchers: List<Voucher>, val error: Error? = null) {
  constructor(error: Error) : this(emptyList(), error)
}

data class Voucher(val packageName: String, val title: String, val icon: String,
                   val hasAppcoins: Boolean)