package com.asfoundation.wallet.home.usecases

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class GetEthBalanceUseCase(private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
                           private val balanceRepository: BalanceRepository) {

  operator fun invoke(): Observable<Pair<Balance, FiatValue>> {
    return getCurrentWalletUseCase()
        .subscribeOn(Schedulers.io())
        .flatMapObservable { balanceRepository.getEthBalance(it.address) }
  }
}