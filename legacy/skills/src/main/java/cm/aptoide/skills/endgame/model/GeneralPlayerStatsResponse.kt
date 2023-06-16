package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GeneralPlayerStatsResponse {
  @SerializedName("current_user")
  @Expose
  val player: UserRankings? = null

  @SerializedName("top_3")
  @Expose
  val top3: Array<UserRankings>

  @SerializedName("above_user")
  @Expose
  val aboveUser: Array<UserRankings>

  @SerializedName("same_rank")
  @Expose
  val sameRank: Array<UserRankings>

  @SerializedName("below_user")
  @Expose
  val belowUser: Array<UserRankings>
}