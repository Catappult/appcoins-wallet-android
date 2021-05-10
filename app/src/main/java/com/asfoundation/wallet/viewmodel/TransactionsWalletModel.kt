package com.asfoundation.wallet.viewmodel

import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet

data class TransactionsWalletModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                   val isNewWallet: Boolean)