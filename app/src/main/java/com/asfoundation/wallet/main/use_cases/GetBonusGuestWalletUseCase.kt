package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.wallet.AccountWalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetBonusGuestWalletUseCase
@Inject
constructor(
    private val walletInfoRepository: WalletInfoRepository,
    private val accountWalletService: AccountWalletService
) {

  operator fun invoke(key: String): Single<FiatValue> {
    return accountWalletService.getAddressFromPrivateKey(key).flatMap { address ->
      walletInfoRepository.getLatestWalletInfo(address).map { it.walletBalance.overallFiat }
    }
  }
}
