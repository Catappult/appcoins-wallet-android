package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class LoginRequest(
  @SerializedName("room_id")
  var roomId: String,
  @SerializedName("ticket_id")
  var ticketId: String
)