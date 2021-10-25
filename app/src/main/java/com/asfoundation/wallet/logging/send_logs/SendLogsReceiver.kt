package com.asfoundation.wallet.logging.send_logs

import android.util.Log
import com.asfoundation.wallet.logging.LogReceiver

class SendLogsReceiver(private var sendLogsRepository: SendLogsRepository): LogReceiver {
  override fun log(tag: String?, throwable: Throwable?) {
    sendLogsRepository.saveLog(map(tag = tag, throwable = throwable))
  }

  override fun log(tag: String?, message: String?) = Unit

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    sendLogsRepository.saveLog(map(tag = tag, message = message, throwable = throwable))
  }

  private fun map(tag: String?, message: String? = null, throwable: Throwable?): String {
    val data = StringBuilder()
    tag?.let{data.append(tag)}
    message?.let{data.append(message)}
    throwable?.let{data.append(Log.getStackTraceString(throwable))}

    return data.toString()
  }
}