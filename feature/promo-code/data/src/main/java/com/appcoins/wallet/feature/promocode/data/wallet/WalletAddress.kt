package com.appcoins.wallet.feature.promocode.data.wallet

import io.reactivex.Single

interface WalletAddress {
  fun getWalletAddresses(): Single<List<String>>
}