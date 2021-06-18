package com.asfoundation.wallet.entity

import com.asfoundation.wallet.ui.iab.FiatValue

data class TransactionsDetailsModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                    val fiatValue: FiatValue) {
}