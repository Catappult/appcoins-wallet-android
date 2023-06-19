package cm.aptoide.skills.endgame.model

import cm.aptoide.skills.R


class BonusRankingsItem(
  val userName: String,
  val score: Double,
  val rank: Long,
  val bonusAmount: Float,
  val isCurrentUser: Boolean
) : RankingsItem {

  override val itemType: Int
    get() = R.layout.player_bonus_layout
}