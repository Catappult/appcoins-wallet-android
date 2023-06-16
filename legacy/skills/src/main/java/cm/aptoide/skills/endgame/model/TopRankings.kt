package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TopRankings {
  @SerializedName("username")
  @Expose
  val username: String? = null

  @SerializedName("rank_position")
  @Expose
  val rankPosition = 0

  @SerializedName("wallet_address")
  @Expose
  val walletAddress: String? = null

  @SerializedName("score")
  @Expose
  val score = 0
}