package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetCanLogUseCase(
    private val sendLogsRepository: SendLogsRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  operator fun invoke(): Single<Boolean> {
    return getCurrentWalletUseCase()
            .subscribeOn(Schedulers.io())
            .flatMap { sendLogsRepository.canLog(it.address) }
  }
}