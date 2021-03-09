package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.util.Error

data class VoucherSkuModelList(val list: List<VoucherSkuItem>,
                               val error: Error = Error()) {

  constructor(error: Error) : this(emptyList(), error)
}
