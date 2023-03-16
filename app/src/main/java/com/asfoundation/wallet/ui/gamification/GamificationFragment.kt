package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.appcoins.wallet.ui.common.MarginItemDecoration
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.bonus_updated_layout.*
import kotlinx.android.synthetic.main.fragment_gamification.*
import kotlinx.android.synthetic.main.gamification_info_bottom_sheet.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GamificationFragment : BasePageViewFragment(), GamificationView {

  @Inject
  lateinit var interactor: GamificationInteractor

  @Inject
  lateinit var analytics: GamificationAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mapper: GamificationMapper
  private lateinit var presenter: GamificationPresenter
  private lateinit var activityView: GamificationActivityView
  private lateinit var levelsAdapter: LevelsAdapter
  private var uiEventListener: PublishSubject<Pair<String, Boolean>>? = null
  private var onBackPressedSubject: PublishSubject<Any>? = null
  private lateinit var detailsBottomSheet: BottomSheetBehavior<View>

  companion object {
    const val SHOW_REACHED_LEVELS_ID = "SHOW_REACHED_LEVELS"
    const val GAMIFICATION_INFO_ID = "GAMIFICATION_INFO"
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is GamificationActivityView) { GamificationFragment::class.java.simpleName + " needs to be attached to a " + GamificationActivityView::class.java.simpleName }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
    onBackPressedSubject = PublishSubject.create()
    presenter =
        GamificationPresenter(this, activityView, interactor, analytics, formatter,
            CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_gamification, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    detailsBottomSheet = BottomSheetBehavior.from(bottom_sheet_fragment_container)
    detailsBottomSheet.addBottomSheetCallback(
        object : BottomSheetBehavior.BottomSheetCallback() {
          override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

          override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 0f) bottomsheet_coordinator_container.visibility = View.GONE
            bottomsheet_coordinator_container.background.alpha = (255 * slideOffset).toInt()
          }
        })
    gamification_recycler_view.visibility = View.INVISIBLE
    levelsAdapter = LevelsAdapter(formatter, mapper, uiEventListener!!)
    gamification_recycler_view.adapter = levelsAdapter
    gamification_recycler_view.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.gamification_card_margin)
            .toInt())
    )
    presenter.present(savedInstanceState)
  }

  override fun displayGamificationInfo(hiddenLevels: List<LevelItem>,
                                       shownLevels: List<LevelItem>,
                                       updateDate: Date?) {
    gamification_recycler_view.visibility = View.VISIBLE
    levelsAdapter.setLevelsContent(hiddenLevels, shownLevels)
    handleBonusUpdatedText(updateDate)
  }

  override fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String) {
    bonus_earned.text = getString(R.string.value_fiat, symbol, bonusEarned)
    total_spend.text = getString(R.string.gamification_how_table_a2, totalSpent)

    bonus_earned_skeleton.visibility = View.INVISIBLE
    total_spend_skeleton.visibility = View.INVISIBLE
    bonus_earned.visibility = View.VISIBLE
    total_spend.visibility = View.VISIBLE
  }

  override fun getUiClick() = uiEventListener!!

  override fun toggleReachedLevels(show: Boolean) {
    levelsAdapter.toggleReachedLevels(show)
    gamification_scroll_view.scrollTo(0, 0)
  }

  private fun handleBonusUpdatedText(updateDate: Date?) {
    if (updateDate != null) {
      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      val date = dateFormat.format(updateDate)
      bonus_update_text.text = getString(R.string.pioneer_bonus_updated_body, date)
      bonus_update.visibility = View.VISIBLE
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun getHomeBackPressed() = activityView.backPressed()

  override fun handleBackPressed() {
    // Currently we only call the hide bottom sheet
    // but maybe later additional stuff needs to be handled
    updateBottomSheetVisibility()
  }

  override fun getBottomSheetButtonClick() = RxView.clicks(got_it_button)

  override fun getBackPressed() = onBackPressedSubject!!

  override fun updateBottomSheetVisibility() {
    if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      detailsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
      disableBackListener(bottomsheet_coordinator_container)
    } else {
      detailsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
      bottomsheet_coordinator_container.visibility = View.VISIBLE
      bottomsheet_coordinator_container.background.alpha = 255
      setBackListener(bottomsheet_coordinator_container)
    }
  }

  override fun getBottomSheetContainerClick() = RxView.clicks(bottomsheet_coordinator_container)

  private fun setBackListener(view: View) {
    activityView.disableBack()
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
            onBackPressedSubject?.onNext("")
        }
        true
      }
    }
  }

  private fun disableBackListener(view: View) {
    activityView.enableBack()
    view.apply {
      isFocusableInTouchMode = false
      setOnKeyListener(null)
    }
  }
}
