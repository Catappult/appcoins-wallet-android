package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class RoomResponse(
  @SerializedName("room_id")
  var roomId: String,

  @SerializedName("room_result")
  var roomResult: RoomResult,

  @SerializedName("current_user")
  var currentUser: User,

  @SerializedName("package_name")
  var packageName: String,

  @SerializedName("status")
  var status: RoomStatus,

  @SerializedName("users")
  var users: List<User>
)
