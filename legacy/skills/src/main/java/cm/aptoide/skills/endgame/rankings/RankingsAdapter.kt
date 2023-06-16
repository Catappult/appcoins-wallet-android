package cm.aptoide.skills.endgame.rankings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cm.aptoide.skills.R
import cm.aptoide.skills.endgame.model.RankingsItem
import cm.aptoide.skills.endgame.model.UserRankingsItem
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.RuntimeException

class RankingsAdapter(layoutInflater: LayoutInflater) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private val differ: AsyncListDiffer<RankingsItem> = AsyncListDiffer(this, DIFF_CALLBACK)


  private val layoutInflater: LayoutInflater

  init {
    this.layoutInflater = layoutInflater
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == R.layout.player_rank_layout) {
      val v: View = layoutInflater.inflate(R.layout.player_rank_layout, parent, false)
      PlayerStatsViewHolder(v)
    } else if (viewType == R.layout.rankings_title) {
      val v: View = layoutInflater.inflate(R.layout.rankings_title, parent, false)
      RankingTitleViewHolder(v)
    } else {
      throw RuntimeException("Invalid view type $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val itemViewType = getItemViewType(position)
    if (itemViewType == R.layout.player_rank_layout) {
      val player: UserRankingsItem = differ.currentList[position] as UserRankingsItem
      (holder as PlayerStatsViewHolder).setPlayerStats(player)
    } else {
      throw RuntimeException("Invalid view type $itemViewType")
    }
  }

  fun setRankings(rankingsItems: List<RankingsItem>?) {
    differ.submitList(rankingsItems)
  }

  override fun getItemCount(): Int {
    return differ.currentList
      .size
  }

  override fun getItemViewType(position: Int): Int {
    return differ.currentList[position]
      .itemType
  }

  internal class PlayerStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var username: TextView
    private var rank: TextView
    var score: TextView

    init {
      username = itemView.findViewById(R.id.rankingUsername)
      rank = itemView.findViewById(R.id.rankingRank)
      score = itemView.findViewById(R.id.rankingScore)
    }

    fun setPlayerStats(player: UserRankingsItem) {
      username.text = player.userName
      score.text = player.score.toString()
      when (player.rank) {
        1L -> {
          rank.setBackgroundResource(R.drawable.gold_medal)
        }
        2L -> {
          rank.setBackgroundResource(R.drawable.silver_medal)
        }
        3L -> {
          rank.setBackgroundResource(R.drawable.bronze_medal)
        }
        else -> {
          rank.text = player.rank.toString()
        }
      }
      if (player.isCurrentUser) {
        val color = itemView.resources
          .getColor(R.color.icon_background)
        username.setTextColor(color)
        rank.setTextColor(color)
        score.setTextColor(color)
      }
    }
  }

  internal class RankingTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var rankingTitle: TextView

    init {
      rankingTitle = itemView.findViewById(R.id.rankingTitle)
    }
  }

  companion object {
    private val DIFF_CALLBACK: DiffUtil.ItemCallback<RankingsItem> =
      object : DiffUtil.ItemCallback<RankingsItem>() {
        override fun areItemsTheSame(
          oldProduct: RankingsItem,
          newProduct: RankingsItem
        ): Boolean {
          return oldProduct == newProduct
        }

        override fun areContentsTheSame(
          oldProduct: RankingsItem,
          newProduct: RankingsItem
        ): Boolean {
          return areItemsTheSame(oldProduct, newProduct)
        }
      }
  }
}