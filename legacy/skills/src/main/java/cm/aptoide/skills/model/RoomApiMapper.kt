package cm.aptoide.skills.model

import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.RoomStatusCode
import com.google.gson.Gson
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class RoomApiMapper@Inject constructor(private val gson: Gson) {
  private data class Response(
    var detail: Detail
  )

  private data class Detail(
    var code: String
  )

  fun map(roomResponse: Single<RoomResponse.SuccessfulRoomResponse>): Single<RoomResponse> {
    return roomResponse.flatMap { response: RoomResponse ->
        Single.just(response)
      }.onErrorReturn { throwable: Throwable -> mapException(throwable) }
  }

  private fun mapException(throwable: Throwable): RoomResponse {
    var status: RoomStatusCode = RoomStatusCode.GENERIC_ERROR
    if (throwable is HttpException) {
      val errorResponse = throwable.response()
      try {
        if (errorResponse?.errorBody() != null) {
          val gsonResponse = gson.fromJson(
            errorResponse.errorBody()!!.charStream(), Response::class.java
          )
          status = RoomStatusCode.valueOf(gsonResponse.detail.code)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return RoomResponse.FailedRoomResponse(statusCode = status)
  }
}