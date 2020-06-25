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
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_how_it_works.*
import java.math.BigDecimal
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HowItWorksFragment : DaggerFragment(), HowItWorksView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor

  @Inject
  lateinit var analytics: GamificationAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private lateinit var presenter: HowItWorksPresenter
  private lateinit var gamificationView: GamificationView


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = HowItWorksPresenter(this, gamificationView, gamificationInteractor, analytics,
        CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread(), formatter)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is GamificationView) { HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName }
    gamificationView = context
  }

  override fun showLevels(
      levels: List<ViewLevel>,
      currentLevel: Int, updateDate: Date?) {
    fragment_gamification_how_it_works_loading.visibility = View.INVISIBLE
    val newList: MutableList<ViewLevel> = levels as MutableList<ViewLevel>
    newList.add(ViewLevel(6, BigDecimal(20000), 27.5, false))
    newList.add(ViewLevel(7, BigDecimal(25000), 30.0, false))
    newList.add(ViewLevel(8, BigDecimal(30000), 32.5, false))
    newList.add(ViewLevel(9, BigDecimal(35000), 35.0, false))
    newList.add(ViewLevel(10, BigDecimal(40000), 37.5, false))
    for (level in newList) {
      val view = layoutInflater.inflate(R.layout.fragment_gamification_how_it_works_level,
          fragment_gamification_how_it_works_levels_layout, false)
      val levelTextView = view.findViewById<TextView>(R.id.level)
      val spendTextView = view.findViewById<TextView>(R.id.message)
      val bonusTextView = view.findViewById<TextView>(R.id.bonus)
      levelTextView.text = (level.level + 1).toString()
      spendTextView.text = getString(R.string.gamification_how_table_a2,
          formatter.formatGamificationValues(level.amount))
      bonusTextView.text =
          getString(R.string.gamification_how_table_b2, formatLevelInfo(level.bonus))
      view.findViewById<ImageView>(R.id.ic_level)
          .setImageResource(LevelResourcesMapper.mapDarkIcons(level))
      (fragment_gamification_how_it_works_levels_layout as LinearLayout).addView(view)
      if (level.level == currentLevel) {
        highlightCurrentLevel(levelTextView, spendTextView, bonusTextView)
      }
    }

    showBonusUpdatedDate(updateDate)
  }

  private fun showBonusUpdatedDate(updateDate: Date?) {
    if (updateDate == null) {
      bonus_update_icon.visibility = View.GONE
      bonus_update_info.visibility = View.GONE
    } else {
      val df: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      val date = df.format(updateDate)
      bonus_update_info.text = getString(R.string.pioneer_bonus_updated_body, date)
    }
  }

  override fun showPeekInformation(totalSpend: String, bonusEarned: String,
                                   currencySymbol: String) {
    bonus_earned.text =
        getString(R.string.value_fiat, currencySymbol, bonusEarned)
    total_spend.text = getString(R.string.gamification_how_table_a2, totalSpend)

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

  override fun bottomSheetHeaderClick() = RxView.clicks(bottom_sheet_header)

  override fun changeBottomSheetState() {
    val parentFragment = provideParentFragment()
    parentFragment?.changeBottomSheetState()
  }

  private fun formatNextLevelFooter(id: Int, nextLevelAmount: String,
                                    nextLevel: String): CharSequence {
    return HtmlCompat.fromHtml(String.format(
        HtmlCompat.toHtml(SpannedString(getText(id)),
            HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE),
        nextLevelAmount, nextLevel), HtmlCompat.FROM_HTML_MODE_LEGACY)
        .trim()
  }

  private fun formatLevelInfo(value: Double): String {
    val formatter: NumberFormat = DecimalFormat("##.##")
    return formatter.format(value)
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

  private fun provideParentFragment(): MyLevelFragment? {
    if (parentFragment !is MyLevelFragment) {
      return null
    }
    return parentFragment as MyLevelFragment
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
    const val MAX_LEVEL = 4

    @JvmStatic
    fun newInstance() = HowItWorksFragment()
  }
}