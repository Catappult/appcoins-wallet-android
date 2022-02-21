package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SendLogsUseCase @Inject constructor(
    private val sendLogsRepository: SendLogsRepository,
    private val ewtObtainer: EwtAuthenticatorService) {

  operator fun invoke(): Completable {
    return ewtObtainer.getEwtAuthentication()
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { sendLogsRepository.sendLogs(it) }
  }
}