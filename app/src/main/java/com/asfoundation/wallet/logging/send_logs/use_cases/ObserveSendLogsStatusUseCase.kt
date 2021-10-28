package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.logging.send_logs.SendLogsStatus
import io.reactivex.Observable

class ObserveSendLogsStatusUseCase(
        private val sendLogsRepository: SendLogsRepository) {

    operator fun invoke(): Observable<SendLogsStatus> {
        return sendLogsRepository.getSendLogsStatus()
    }
}