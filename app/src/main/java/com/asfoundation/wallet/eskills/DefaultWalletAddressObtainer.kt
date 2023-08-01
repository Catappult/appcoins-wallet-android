package com.asfoundation.wallet.eskills

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Observable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = WalletAddressObtainer::class)
class DefaultWalletAddressObtainer @Inject constructor(private val walletService: WalletService) :
  WalletAddressObtainer {

  override fun getWalletAddress(): Single<WalletAddress> {
    return walletService.getWalletAddress()
        .map { WalletAddress.fromValue(it) }
  }

  override fun getOrCreateWallet(): Observable<String> {
    return walletService.findWalletOrCreate()
  }
}
