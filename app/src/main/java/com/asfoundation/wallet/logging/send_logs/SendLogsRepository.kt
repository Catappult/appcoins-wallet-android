package com.asfoundation.wallet.logging.send_logs

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.*
import java.io.File

class SendLogsRepository(
    private val sendLogsApi: SendLogsApi,
    private val awsUploadFilesApi: AwsUploadFilesApi,
    private val logsDao: LogsDao,
    private val rxSchedulers: RxSchedulers,
    private val cacheDir: File,
) {

  private val sendStateBehaviorSubject = BehaviorSubject.createDefault(SendState.UNINITIALIZED)

  fun saveLog(tag: String?, data: String): Completable {
    return Completable.fromAction { logsDao.saveLog(LogEntity(null, tag = tag, data = data)) }
        .subscribeOn(rxSchedulers.io)
  }

  fun updateLogs(): Single<File> {

    return logsDao.updateLogs()
        .andThen(
            logsDao.getSendingLogs()
                .map { logs ->
                  val logsFile = File.createTempFile("log", null, cacheDir)
                  val logContent = StringBuilder()

                  logs.forEach { logEntity ->
                    logContent.append(logEntity.created.toString())
                    logEntity.tag?.let { logContent.append(" " + logEntity.tag) }
                    logContent.appendLine(": " + logEntity.data)

                    logsFile.appendText(logContent.toString())
                    logContent.clear()
                  }

                  return@map logsFile
                }
        )
        .subscribeOn(rxSchedulers.io)
  }


  fun sendLogs(ewt: String): Completable {

    return Single.zip(sendLogsApi.getSendLogsUrl(ewt), updateLogs(),
        { awsInfo, file -> Pair(awsInfo, file) }
    )
        .flatMapCompletable {
          val filePart = MultipartBody.Part.createFormData("file", it.second.getName(),
              it.second.asRequestBody("multipart/form-data".toMediaTypeOrNull()))

          val body = HashMap<String, RequestBody>()
          body["AWSAccessKeyId"] = it.first.fields.awsAccessKeyId.toRequestBody("multipart/form-data".toMediaTypeOrNull());
          body["signature"] = it.first.fields.signature.toRequestBody("multipart/form-data".toMediaTypeOrNull());
          body["policy"] = it.first.fields.policy.toRequestBody("multipart/form-data".toMediaTypeOrNull());
          body["Key"] = it.first.fields.key.toRequestBody("multipart/form-data".toMediaTypeOrNull());

          awsUploadFilesApi.uploadFile(
              it.first.url,
              filePart,
              body
          )
        }
        .doOnComplete {
          sendStateBehaviorSubject.onNext(SendState.SENT)
        }
        .onErrorComplete {
          sendStateBehaviorSubject.onNext(SendState.ERROR)
          return@onErrorComplete true
        }

        .doOnSubscribe {
          sendStateBehaviorSubject.onNext(SendState.SENDING)
        }
        .subscribeOn(rxSchedulers.io)
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

    @GET("/transaction/wallet/logging/url")
    fun getSendLogsUrl(@Header("authorization") ewt: String): Single<GetSendLogsUrlResponse>
  }

  interface AwsUploadFilesApi {
    @Multipart
    @POST
    fun uploadFile(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @PartMap body: HashMap<String, RequestBody>,
    ): Completable
  }
}