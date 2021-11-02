package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.logging.send_logs.SendLogsState
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetSendLogsStateUseCase(
    private val sendLogsRepository: SendLogsRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  operator fun invoke(): Single<Observable<SendLogsState>> {
    return getCurrentWalletUseCase()
            .subscribeOn(Schedulers.io())
            .map { sendLogsRepository.getSendLogsState(it.address) }
  }
}