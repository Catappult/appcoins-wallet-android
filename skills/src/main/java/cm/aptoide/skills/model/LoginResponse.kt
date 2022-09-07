package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class LoginResponse(
  @SerializedName("token")
  var token: String
)