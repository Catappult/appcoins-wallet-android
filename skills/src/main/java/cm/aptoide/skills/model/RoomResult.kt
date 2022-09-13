package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class RoomResult(
  @SerializedName("winner")
  var winner: User,

  @SerializedName("winner_amount")
  var winnerAmount: Float
)
