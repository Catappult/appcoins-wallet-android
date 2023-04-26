package com.asfoundation.wallet.entity

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue

data class TransactionsDetailsModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                    val fiatValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
) {
}