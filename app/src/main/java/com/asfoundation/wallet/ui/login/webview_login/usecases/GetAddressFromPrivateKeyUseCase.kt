package com.asfoundation.wallet.ui.login.webview_login.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService
import io.reactivex.Single
import javax.inject.Inject

class GetAddressFromPrivateKeyUseCase @Inject constructor(
  private val accountWalletService: AccountWalletService
) {

  operator fun invoke(privateKey: String): Single<String> {
    return accountWalletService.getAddressFromPrivateKey(privateKey)
  }
}