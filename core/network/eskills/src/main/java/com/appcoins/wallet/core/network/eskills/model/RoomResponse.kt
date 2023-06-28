package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName


sealed class RoomResponse {
  abstract val status: RoomStatus
  abstract val statusCode: RoomStatusCode

  data class SuccessfulRoomResponse(
    @SerializedName("room_id")
    var roomId: String,

    @SerializedName("room_result")
    var roomResult: RoomResult,

    @SerializedName("current_user")
    var currentUser: User,

    @SerializedName("package_name")
    var packageName: String,

    @SerializedName("status")
    override var status: RoomStatus,

    @SerializedName("users")
    var users: List<User>,

    override var statusCode: RoomStatusCode = RoomStatusCode.SUCCESSFUL_RESPONSE
  ) : RoomResponse()

  data class FailedRoomResponse(
    override var status: RoomStatus = RoomStatus.COMPLETED,
    override var statusCode: RoomStatusCode = RoomStatusCode.GENERIC_ERROR
  ) : RoomResponse()
}

