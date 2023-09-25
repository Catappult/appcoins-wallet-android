package cm.aptoide.skills.endgame.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cm.aptoide.skills.R
import com.appcoins.wallet.core.network.eskills.model.User

class PlayerRankingAdapter(dataSet: List<User>) :
  RecyclerView.Adapter<PlayerRankingAdapter.ViewHolder>() {
  private var localDataSet: List<User>

  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val userNameTextView: TextView = view.findViewById<View>(R.id.user_name) as TextView // TODO
    private val userScoreTextView: TextView = view.findViewById<View>(R.id.user_score) as TextView  // TODO

    fun bind(user: User) {
      userNameTextView.text = user.userName
      userScoreTextView.text =
        itemView.context.resources.getQuantityString(
          R.plurals.rank_score_details,
          user.score.toInt(),
          user.score.toInt()
      )
    }
  }

  init {
    localDataSet = dataSet
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
    val view: View = LayoutInflater.from(viewGroup.context)
      .inflate(R.layout.player_row_item, viewGroup, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    viewHolder.bind(localDataSet[position])
  }

  override fun getItemCount(): Int {
    return localDataSet.size
  }

  fun updateData(dataSet: List<User>) {
    localDataSet = dataSet
    notifyDataSetChanged()  // TODO
  }
}