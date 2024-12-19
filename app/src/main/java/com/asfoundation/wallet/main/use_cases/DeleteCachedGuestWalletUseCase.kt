package com.asfoundation.wallet.main.use_cases

import com.asfoundation.wallet.repository.CachedGuestWalletRepository
import io.reactivex.Completable
import javax.inject.Inject

class DeleteCachedGuestWalletUseCase @Inject constructor(
  private val cachedGuestWalletRepository: CachedGuestWalletRepository,
) {

  operator fun invoke(): Completable =
    cachedGuestWalletRepository.deleteCachedGuestWallet()

}
