package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BonusUser {
  @SerializedName("rank")
  @Expose
  val rank: Long = 0

  @SerializedName("bonus_amount")
  @Expose
  val bonusAmount = 0f

  @SerializedName("user_name")
  @Expose
  val userName: String? = null

  @SerializedName("score")
  @Expose
  val score = 0.0
}