package com.asfoundation.wallet.logging.send_logs
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
class SendLogsRepository(private val sendLogsApi: SendLogsApi,
                         private val logsDao: LogsDao,
                         private val rxSchedulers: RxSchedulers) {
  fun saveLog(tag: String?, data: String): Completable {
    return Completable.fromAction { logsDao.saveLog(LogEntity(null, tag = tag, data = data)) }
        .subscribeOn(rxSchedulers.io)
  }

  fun canLog(address: String): Single<Boolean> {
    return sendLogsApi.getCanSendLogs(address)
            .map { response: CanLogResponse -> response.logging }
            .subscribeOn(rxSchedulers.io)
  }

  fun sendLogs(address: String) {
  }

  fun getSendLogsStatus(): Observable<SendLogsStatus> {
    return Observable.just(SendLogsStatus.SENDING)
  }

  interface SendLogsApi {
    @GET("/transaction/wallet/{address}/info")
    fun getCanSendLogs(@Path("address") address: String): Single<CanLogResponse>
  }
}