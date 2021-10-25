package com.asfoundation.wallet.logging.send_logs
import com.asfoundation.wallet.entity.Address
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class SendLogsRepository(private val sendLogsApi: SendLogsApi,
                         private val logsDao: LogsDao) {
  fun saveLog(data: String) {
    logsDao.saveLog(LogEntity(null, data = data))
  }

  fun canLog(address: Address): Single<Boolean> {
    return sendLogsApi.getCanSendLogs(address.value)
            .map { response: CanLogResponse -> response.logging }
  }

  interface SendLogsApi {
    @GET("/transaction/wallet/{address}/info")
    fun getCanSendLogs(@Path("address") address: String): Single<CanLogResponse>
  }
}