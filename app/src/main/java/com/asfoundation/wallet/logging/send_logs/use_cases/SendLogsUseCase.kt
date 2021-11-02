package com.asfoundation.wallet.logging.send_logs.use_cases
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class SendLogsUseCase(
    private val sendLogsRepository: SendLogsRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  operator fun invoke(): Completable {
    return Completable.fromAction {
      getCurrentWalletUseCase()
          .subscribeOn(Schedulers.io())
          .map { sendLogsRepository.sendLogs(it.address) }
    }
  }
}