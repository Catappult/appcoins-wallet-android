package cm.aptoide.skills.model

import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.RoomStatusCode
import com.google.gson.Gson
import io.reactivex.Single

class RoomApiMapper(private val gson: Gson) {
  private inner class Response {
    var detail: Detail? = null
  }

  private class Detail {
    var code: String? = null
  }

  fun map(roomResponse: Single<RoomResponse>): Single<RoomResponse> {
    return roomResponse
      .flatMap { response: RoomResponse ->
        response.statusCode = RoomStatusCode.SUCCESSFUL_RESPONSE
        Single.just(response)
      }
    //.onErrorReturn { throwable: Throwable -> mapException(throwable) }
  }

  /*private fun mapException(throwable: Throwable): RoomResponse {
    var status: RoomStatusCode = RoomStatusCode.GENERIC_ERROR
    if (throwable is HttpException) {
      val errorResponse = throwable.response()
      try {
        if (errorResponse != null && errorResponse.errorBody() != null) {
          val gsonResponse = gson.fromJson(
            errorResponse.errorBody()!!.charStream(), Response::class.java
          )
          status = RoomStatusCode.valueOf(gsonResponse.detail!!.code!!)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    val roomResponse = RoomResponse()
    roomResponse.statusCode = status
    return roomResponse
  }*/
}