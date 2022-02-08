package com.asfoundation.wallet.eskills

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.bdsbilling.WalletService
import io.reactivex.Observable
import io.reactivex.Single

class DefaultWalletAddressObtainer(private val walletService: WalletService) :
    WalletAddressObtainer {

  override fun getWalletAddress(): Single<WalletAddress> {
    return walletService.getWalletAddress()
        .map { WalletAddress.fromValue(it) }
  }

  override fun getOrCreateWallet(): Observable<String> {
    return walletService.findWalletOrCreate()
  }
}
