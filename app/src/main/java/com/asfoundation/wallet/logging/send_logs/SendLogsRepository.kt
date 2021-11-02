package com.asfoundation.wallet.logging.send_logs

import android.annotation.SuppressLint
import android.util.Log
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import retrofit2.http.GET
import retrofit2.http.Path

class SendLogsRepository(private val sendLogsApi: SendLogsApi,
                         private val logsDao: LogsDao,
                         private val rxSchedulers: RxSchedulers) {

  private val sendStateBehaviorSubject = BehaviorSubject.createDefault(SendState.UNINITIALIZED)

  fun saveLog(tag: String?, data: String): Completable {
    return Completable.fromAction { logsDao.saveLog(LogEntity(null, tag = tag, data = data)) }
        .subscribeOn(rxSchedulers.io)
  }

  @SuppressLint("CheckResult")
  fun sendLogs(address: String) {
    sendStateBehaviorSubject.onNext(SendState.SENDING)
    Log.e("OLAH", "ADUES")

    logsDao.updateLogs().subscribeOn(rxSchedulers.io)
    val logs = logsDao.getLogs().subscribeOn(rxSchedulers.io)

    sendStateBehaviorSubject.onNext(SendState.SENT)
  }

  private fun canLog(address: String): Single<Boolean> {
    return sendLogsApi.getCanSendLogs(address)
        .map { response: CanLogResponse -> response.logging }
        .subscribeOn(rxSchedulers.io)
  }

  fun getSendLogsState(address: String): Observable<SendLogsState> {
    return Observable.zip(canLog(address).toObservable(), sendStateBehaviorSubject,
        { shouldShow, state -> SendLogsState(shouldShow, state) })
  }

  interface SendLogsApi {
    @GET("/transaction/wallet/{address}/info")
    fun getCanSendLogs(@Path("address") address: String): Single<CanLogResponse>
  }
}