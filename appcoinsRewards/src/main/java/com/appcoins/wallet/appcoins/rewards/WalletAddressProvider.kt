package com.appcoins.wallet.appcoins.rewards

import io.reactivex.Single

interface WalletAddressProvider {
  fun getWallet(): Single<String>
}
