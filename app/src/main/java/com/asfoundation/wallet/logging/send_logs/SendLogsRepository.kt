package com.asfoundation.wallet.logging.send_logs
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class SendLogsRepository(private val sendLogsApi: SendLogsApi,
                         private val logsDao: LogsDao) {
  fun saveLog(data: String) {
    logsDao.saveLog(LogEntity(null, data = data))
  }

  fun canLog(address: String): Single<Boolean> {
    return sendLogsApi.getCanSendLogs(address)
            .map { response: CanLogResponse -> response.logging }
  }

  interface SendLogsApi {
    @GET("/transaction/wallet/{address}/info")
    fun getCanSendLogs(@Path("address") address: String): Single<CanLogResponse>
  }
}