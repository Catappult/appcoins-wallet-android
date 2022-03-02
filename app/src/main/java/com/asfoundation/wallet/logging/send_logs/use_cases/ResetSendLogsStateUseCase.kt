package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import javax.inject.Inject

class ResetSendLogsStateUseCase @Inject constructor(
    private val sendLogsRepository: SendLogsRepository) {

  operator fun invoke() {
    sendLogsRepository.resetSendLogsState()
  }
}