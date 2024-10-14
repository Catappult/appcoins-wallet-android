package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdateWalletNameUseCase @Inject constructor(private val walletInfoRepository: WalletInfoRepository) {

  operator fun invoke(address: String, name: String?): Completable =
    walletInfoRepository.updateWalletName(address, name)
}