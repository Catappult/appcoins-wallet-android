package com.asfoundation.wallet.main.use_cases

import com.asfoundation.wallet.onboarding.BackupModel
import com.asfoundation.wallet.repository.CachedGuestWalletRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCachedGuestWalletUseCase @Inject constructor(
  private val cachedGuestWalletRepository: CachedGuestWalletRepository
) {

  operator fun invoke(): Single<BackupModel?> {
    return cachedGuestWalletRepository.getCachedGuestWallet()
  }

}
