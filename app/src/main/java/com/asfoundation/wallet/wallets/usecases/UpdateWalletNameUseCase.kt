package com.asfoundation.wallet.wallets.usecases

import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdateWalletNameUseCase @Inject constructor(private val walletInfoRepository: WalletInfoRepository) {

  operator fun invoke(address: String, name: String?): Completable =
    walletInfoRepository.updateWalletName(address, name)
}