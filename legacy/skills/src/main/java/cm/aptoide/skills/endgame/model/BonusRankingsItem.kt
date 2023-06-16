package cm.aptoide.skills.endgame.model

import com.appcoins.eskills2048.R

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