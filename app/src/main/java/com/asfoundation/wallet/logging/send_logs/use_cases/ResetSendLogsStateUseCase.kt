package com.asfoundation.wallet.logging.send_logs.use_cases

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository

class ResetSendLogsStateUseCase(
    private val sendLogsRepository: SendLogsRepository) {

  operator fun invoke() {
    sendLogsRepository.resetSendLogsState()
  }
}