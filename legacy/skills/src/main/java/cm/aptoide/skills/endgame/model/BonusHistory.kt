package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BonusHistory {
  @SerializedName("date")
  @Expose
  val date: String? = null

  @SerializedName("users")
  @Expose
  val users: List<BonusUser>? = null
}