package com.asfoundation.wallet.entity

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet

data class TransactionsDetailsModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                    val fiatValue: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
) {
}