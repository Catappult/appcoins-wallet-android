package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.SerializedName

class UserRankings {
  @SerializedName("username")
  val rankingUsername: String? = null
    get() = field ?: rankingWalletAddress

  @SerializedName("rank_position")
  val rankPosition = 0

  @SerializedName("wallet_address")
  val rankingWalletAddress: String? = null

  @SerializedName("score")
  val rankingScore = 0
}