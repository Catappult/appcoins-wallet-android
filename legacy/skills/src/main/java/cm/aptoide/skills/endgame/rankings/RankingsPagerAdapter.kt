package cm.aptoide.skills.endgame.rankings

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cm.aptoide.skills.R
import cm.aptoide.skills.model.TimeFrame
import java.security.InvalidParameterException

internal class RankingsPagerAdapter(
  fragment: Fragment,
  private val walletAddress: String,
  private val packageName: String,
  private val sku: String
) : FragmentStateAdapter(fragment) {
  override fun getItemCount(): Int = 3

  override fun createFragment(position: Int): Fragment {
    return RankingsContentFragment.newInstance(
      walletAddress, packageName, sku,
      getTimeFrame(position)
    )
  }

  private fun getTimeFrame(position: Int): TimeFrame {
    return when (position) {
      0 -> TimeFrame.TODAY
      1 -> TimeFrame.WEEK
      2 -> TimeFrame.ALL_TIME
      else -> throw InvalidParameterException("Invalid position $position")
    }
  }

  fun getFragmentTitle(position: Int): Int {
    return when (getTimeFrame(position)) {
      TimeFrame.TODAY -> R.string.rankings_today
      TimeFrame.WEEK -> R.string.rankings_week
      TimeFrame.ALL_TIME -> R.string.rankings_all_time
    }
  }
}