package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.logging.send_logs.SendLogsState
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ObserveSendLogsStateUseCase(
    private val sendLogsRepository: SendLogsRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  operator fun invoke(): Observable<SendLogsState> {
    return getCurrentWalletUseCase()
            .subscribeOn(Schedulers.io())
            .flatMapObservable { sendLogsRepository.observeSendLogsState(it.address) }
  }
}