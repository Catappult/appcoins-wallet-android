package com.asfoundation.wallet.skills

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import com.appcoins.wallet.bdsbilling.WalletService
import io.reactivex.Single

class DefaultWalletAddressObtainer(private val walletService: WalletService) :
    WalletAddressObtainer {

  override fun getWalletAddress(): Single<String> {
    return walletService.getWalletAddress()
  }
}