package cm.aptoide.skills.endgame.model

import cm.aptoide.skills.R

class UserRankingsItem(
  val userName: String,
  val score: Double,
  val rank: Long,
  val isCurrentUser: Boolean
) : RankingsItem {

  override val itemType: Int
    get() = R.layout.player_rank_layout
}