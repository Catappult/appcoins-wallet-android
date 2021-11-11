package com.asfoundation.wallet.logging.send_logs

data class SendLogsState(val shouldShow: Boolean, val state: SendState)

enum class SendState {
  SENDING, SENT, ERROR, UNINITIALIZED
}
