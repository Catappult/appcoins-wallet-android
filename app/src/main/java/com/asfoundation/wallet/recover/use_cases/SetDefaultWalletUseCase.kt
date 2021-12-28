package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import io.reactivex.Completable

class SetDefaultWalletUseCase(private val setDefaultWalletInteractor: SetDefaultWalletInteractor) {
  operator fun invoke(address: String): Completable = setDefaultWalletInteractor.set(address)
}