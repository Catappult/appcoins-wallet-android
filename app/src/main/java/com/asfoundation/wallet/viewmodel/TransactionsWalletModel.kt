package com.asfoundation.wallet.viewmodel

import com.asfoundation.wallet.entity.NetworkInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet

data class TransactionsWalletModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                   val isNewWallet: Boolean)