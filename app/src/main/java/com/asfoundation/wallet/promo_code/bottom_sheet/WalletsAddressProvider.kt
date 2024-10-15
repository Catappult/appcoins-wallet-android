package com.asfoundation.wallet.promo_code.bottom_sheet

import com.appcoins.wallet.feature.promocode.data.wallet.WalletAddress
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single

class WalletsAddressProvider(
  private val walletRepository: WalletRepositoryType,
) : WalletAddress {

  override fun getWalletAddresses(): Single<List<String>> =
    walletRepository.fetchWallets()
      .map { it.map { wallet -> wallet.address } }
}