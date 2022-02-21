package com.asfoundation.wallet.logging.send_logs

import android.content.Context
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.logging.send_logs.db.LogEntity
import com.asfoundation.wallet.logging.send_logs.db.LogsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.*
import java.io.File
import javax.inject.Inject

class SendLogsRepository @Inject constructor(
    private val sendLogsApi: SendLogsApi,
    private val awsUploadFilesApi: AwsUploadFilesApi,
    private val logsDao: LogsDao,
    private val rxSchedulers: RxSchedulers,
    @ApplicationContext private val context: Context) {

  private val sendStateBehaviorSubject = BehaviorSubject.createDefault(SendState.UNINITIALIZED)

  fun saveLog(tag: String?, data: String): Completable {
    return Completable.fromAction { logsDao.saveLog(LogEntity(null, tag = tag, data = data)) }
        .subscribeOn(rxSchedulers.io)
  }

  fun updateLogs(): Maybe<File> {

    return logsDao.setLogsToSent()
        .andThen(
            logsDao.getSendingLogs()
                .flatMapMaybe { logs ->
                  if (logs.isEmpty())
                    return@flatMapMaybe Maybe.empty()

                  val logsFile = File.createTempFile("log", null, context.cacheDir)
                  val logContent = StringBuilder()

                  logs.forEach { logEntity ->
                    logContent.append(logEntity.created.toString())
                    logEntity.tag?.let { logContent.append(" " + logEntity.tag) }
                    logContent.appendLine(": " + logEntity.data)

                    logsFile.appendText(logContent.toString())
                    logContent.clear()
                  }

                  return@flatMapMaybe Maybe.just(logsFile)
                }
        )
        .subscribeOn(rxSchedulers.io)
  }


  fun sendLogs(ewt: String): Completable {

    return Single.zip(sendLogsApi.getSendLogsUrl(ewt), updateLogs().materialize(),
        { awsInfo, materializedFile -> Pair(awsInfo, materializedFile) }
    )
        .flatMapCompletable {
          val file = it.second.value ?: return@flatMapCompletable Completable.complete()

          val filePart = MultipartBody.Part.createFormData("file", file.name,
              file.asRequestBody("text/*".toMediaType()))

          awsUploadFilesApi.uploadFile(
              it.first.url,
              it.first.fields.awsAccessKeyId.toRequestBody("text/plain".toMediaType()),
              it.first.fields.signature.toRequestBody("text/plain".toMediaType()),
              it.first.fields.policy.toRequestBody("text/plain".toMediaType()),
              it.first.fields.key.toRequestBody("text/plain".toMediaType()),
              it.first.fields.token?.toRequestBody("text/plain".toMediaType()),
              filePart,
          )
        }
        .andThen(
            logsDao.deleteSentLogs()
        )
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

  private fun getSendLogsVisibility(address: String): Single<Boolean> {
    return sendLogsApi.getCanSendLogs(address)
        .map { response: CanLogResponse -> response.logging }
        .onErrorReturnItem(false)
        .subscribeOn(rxSchedulers.io)
  }

  fun observeSendLogsState(address: String): Observable<SendLogsState> {
    return Observable.combineLatest(getSendLogsVisibility(address).toObservable(),
        sendStateBehaviorSubject,
        { shouldShow, state -> SendLogsState(shouldShow, state) })
  }

  fun resetSendLogsState() {
    if (sendStateBehaviorSubject.value == SendState.SENT)
      sendStateBehaviorSubject.onNext(SendState.UNINITIALIZED)
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
        @Part("AWSAccessKeyId") awsAccessKeyId: RequestBody,
        @Part("signature") signature: RequestBody,
        @Part("policy") policy: RequestBody,
        @Part("Key") key: RequestBody,
        @Part("x-amz-security-token") token: RequestBody?,
        @Part file: MultipartBody.Part,
    ): Completable
  }
}