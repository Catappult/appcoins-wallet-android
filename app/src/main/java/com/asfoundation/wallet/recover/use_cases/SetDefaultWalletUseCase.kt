package com.asfoundation.wallet.recover.use_cases

import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SetDefaultWalletUseCase @Inject constructor(
  private val setActiveWalletUseCase: SetActiveWalletUseCase
) {
  operator fun invoke(address: String): Completable =
    setActiveWalletUseCase(address)
}