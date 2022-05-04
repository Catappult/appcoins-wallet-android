package com.asfoundation.wallet.logging.send_logs

import android.util.Log
import com.appcoins.wallet.commons.LogReceiver

class SendLogsReceiver(private var sendLogsRepository: SendLogsRepository) : LogReceiver {
  override fun log(tag: String?, throwable: Throwable?) {
    sendLogsRepository.saveLog(tag, map(throwable = throwable))
        .subscribe()
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) = Unit

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    sendLogsRepository.saveLog(tag, map(message = message, throwable = throwable))
        .subscribe()
  }

  private fun map(message: String? = null, throwable: Throwable?): String {
    val data = StringBuilder()
    message?.let { data.appendLine(message) }
    throwable?.let { data.append(Log.getStackTraceString(throwable)) }

    return data.toString()
  }
}