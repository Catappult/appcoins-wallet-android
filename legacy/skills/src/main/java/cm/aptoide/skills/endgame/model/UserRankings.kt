package cm.aptoide.skills.endgame.model

import com.google.gson.annotations.SerializedName

data class UserRankings(
  @SerializedName("username")
  val rankingUsername: String,

  @SerializedName("rank_position")
  val rankPosition: Int,

  @SerializedName("wallet_address")
  val rankingWalletAddress: String,

  @SerializedName("score")
  val rankingScore: Int
)