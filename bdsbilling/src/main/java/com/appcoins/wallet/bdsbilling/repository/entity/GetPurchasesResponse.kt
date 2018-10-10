package com.appcoins.wallet.billing.repository.entity

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase

data class GetPurchasesResponse(val items: List<Purchase>)
