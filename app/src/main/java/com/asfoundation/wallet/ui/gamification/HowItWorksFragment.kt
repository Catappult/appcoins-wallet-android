package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannedString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.appcoins.wallet.gamification.repository.UserStats
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.ui.iab.FiatValue
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_how_it_works.*
import java.math.RoundingMode
import javax.inject.Inject

class HowItWorksFragment : DaggerFragment(), HowItWorksView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor
  @Inject
  lateinit var analytics: GamificationAnalytics

  private lateinit var presenter: HowItWorksPresenter
  private lateinit var gamificationView: GamificationView


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = HowItWorksPresenter(this, gamificationView, gamificationInteractor, analytics,
        Schedulers.io(), AndroidSchedulers.mainThread())
  }


  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is GamificationView) { HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName }
    gamificationView = context
  }

  override fun showLevels(levels: List<ViewLevel>, currentLevel: Int) {
    fragment_gamification_how_it_works_loading.visibility = View.INVISIBLE
    var view: View?

    for (level in levels) {
      view = layoutInflater.inflate(R.layout.fragment_gamification_how_it_works_level,
          fragment_gamification_how_it_works_levels_layout, false)
      val levelTextView = view.findViewById<TextView>(R.id.level)
      val spendTextView = view.findViewById<TextView>(R.id.message)
      val bonusTextView = view.findViewById<TextView>(R.id.bonus)
      levelTextView.text = (level.level + 1).toString()
      spendTextView.text =
          getString(R.string.gamification_how_table_a2, formatLevelInfo(level.amount.toDouble()))
      bonusTextView.text =
          getString(R.string.gamification_how_table_b2, formatLevelInfo(level.bonus))
      view.findViewById<ImageView>(R.id.ic_level)
          .setImageResource(LevelResourcesMapper.mapDarkIcons(level))
      (fragment_gamification_how_it_works_levels_layout as LinearLayout).addView(view)
      if (level.level == currentLevel) {
        highlightCurrentLevel(levelTextView, spendTextView, bonusTextView)
      }
    }
  }

  override fun showPeekInformation(userStats: UserStats, bonusEarnedFiat: FiatValue) {
    val totalSpendRounded = userStats.totalSpend.setScale(2, RoundingMode.DOWN)
    val bonusEarnedRounded = bonusEarnedFiat.amount.setScale(2, RoundingMode.DOWN)
    bonus_earned.text =
        getString(R.string.value_fiat, bonusEarnedFiat.symbol, bonusEarnedRounded)
    total_spend.text = getString(R.string.gamification_how_table_a2, totalSpendRounded)

    bonus_earned_skeleton.visibility = View.INVISIBLE
    total_spend_skeleton.visibility = View.INVISIBLE
    bonus_earned.visibility = View.VISIBLE
    total_spend.visibility = View.VISIBLE
  }

  override fun showNextLevelFooter(userStatus: UserRewardsStatus) {
    if (userStatus.level == MAX_LEVEL) {
      next_level_footer.text = getString(R.string.gamification_how_max_level_body)
    } else {
      val nextLevel = (userStatus.level + 2).toString()
      next_level_footer.text =
          formatNextLevelFooter(R.string.gamification_how_to_next_level_body,
              formatLevelInfo(userStatus.toNextLevelAmount.toDouble()), nextLevel)
    }
  }

  private fun formatNextLevelFooter(id: Int, nextLevelAmount: String,
                                    nextLevel: String): CharSequence {
    return HtmlCompat.fromHtml(String.format(
        HtmlCompat.toHtml(SpannedString(getText(id)), HtmlCompat.FROM_HTML_MODE_LEGACY),
        nextLevelAmount, nextLevel), HtmlCompat.FROM_HTML_MODE_LEGACY)
  }

  private fun formatLevelInfo(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return if (splitValue[1] != "0") {
      value.toString()
    } else {
      removeDecimalPlaces(value)
    }
  }

  private fun removeDecimalPlaces(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return splitValue[0]
  }

  private fun highlightCurrentLevel(levelTextView: TextView, messageTextView: TextView,
                                    bonusTextView: TextView) {
    levelTextView.typeface = Typeface.DEFAULT_BOLD
    levelTextView.setTextColor(
        ContextCompat.getColor(context!!, R.color.rewards_level_main_color))
    messageTextView.typeface = Typeface.DEFAULT_BOLD
    messageTextView.setTextColor(
        ContextCompat.getColor(context!!, R.color.rewards_level_main_color))
    bonusTextView.typeface = Typeface.DEFAULT_BOLD
    bonusTextView.setTextColor(
        ContextCompat.getColor(context!!, R.color.rewards_level_main_color))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_gamification_how_it_works, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {
    private val TAG = HowItWorksFragment::class.java.simpleName
    const val MAX_LEVEL = 4
    @JvmStatic
    fun newInstance(): HowItWorksFragment {
      return HowItWorksFragment()
    }
  }
}