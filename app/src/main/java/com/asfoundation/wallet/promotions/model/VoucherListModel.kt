package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.core.utils.jvm_common.Error

data class VoucherListModel(val vouchers: List<Voucher>, val error: com.appcoins.wallet.core.utils.jvm_common.Error? = null) {
  constructor(error: com.appcoins.wallet.core.utils.jvm_common.Error) : this(emptyList(), error)
}

data class Voucher(val packageName: String, val title: String, val icon: String,
                   val hasAppcoins: Boolean)