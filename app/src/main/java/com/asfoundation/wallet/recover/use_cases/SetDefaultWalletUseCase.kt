package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import io.reactivex.Completable
import javax.inject.Inject

class SetDefaultWalletUseCase @Inject constructor(private val setDefaultWalletInteractor: SetDefaultWalletInteractor) {
  operator fun invoke(address: String): Completable = setDefaultWalletInteractor.set(address)
}