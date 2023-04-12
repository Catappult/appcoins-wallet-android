package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.gamification.GamificationAnalytics
import com.asf.wallet.R
import com.appcoins.wallet.ui.common.MarginItemDecoration
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.FragmentGamificationBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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

  private val binding by viewBinding(FragmentGamificationBinding::bind)

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
    detailsBottomSheet = BottomSheetBehavior.from(binding.bottomSheetFragmentContainer.root)
    detailsBottomSheet.addBottomSheetCallback(
        object : BottomSheetBehavior.BottomSheetCallback() {
          override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

          override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 0f) binding.bottomsheetCoordinatorContainer.visibility = View.GONE
            binding.bottomsheetCoordinatorContainer.background.alpha = (255 * slideOffset).toInt()
          }
        })
    binding.gamificationRecyclerView.visibility = View.INVISIBLE
    levelsAdapter = LevelsAdapter(formatter, mapper, uiEventListener!!)
    binding.gamificationRecyclerView.adapter = levelsAdapter
    binding.gamificationRecyclerView.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.gamification_card_margin)
            .toInt())
    )
    presenter.present(savedInstanceState)
  }

  override fun displayGamificationInfo(hiddenLevels: List<LevelItem>,
                                       shownLevels: List<LevelItem>,
                                       updateDate: Date?) {
    binding.gamificationRecyclerView.visibility = View.VISIBLE
    levelsAdapter.setLevelsContent(hiddenLevels, shownLevels)
    handleBonusUpdatedText(updateDate)
  }

  override fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String) {
    binding.bonusEarned.text = getString(R.string.value_fiat, symbol, bonusEarned)
    binding.totalSpend.text = getString(R.string.gamification_how_table_a2, totalSpent)

    binding.bonusEarnedSkeleton.visibility = View.INVISIBLE
    binding.totalSpendSkeleton.visibility = View.INVISIBLE
    binding.bonusEarned.visibility = View.VISIBLE
    binding.totalSpend.visibility = View.VISIBLE
  }

  override fun getUiClick() = uiEventListener!!

  override fun toggleReachedLevels(show: Boolean) {
    levelsAdapter.toggleReachedLevels(show)
    binding.gamificationScrollView.scrollTo(0, 0)
  }

  private fun handleBonusUpdatedText(updateDate: Date?) {
    if (updateDate != null) {
      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      val date = dateFormat.format(updateDate)
      binding.bonusUpdate.bonusUpdateText.text = getString(R.string.pioneer_bonus_updated_body, date)
      binding.bonusUpdate.root.visibility = View.VISIBLE
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

  override fun getBottomSheetButtonClick() = RxView.clicks(binding.bottomSheetFragmentContainer.gotItButton)

  override fun getBackPressed() = onBackPressedSubject!!

  override fun updateBottomSheetVisibility() {
    if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      detailsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
      disableBackListener(binding.bottomsheetCoordinatorContainer)
    } else {
      detailsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
      binding.bottomsheetCoordinatorContainer.visibility = View.VISIBLE
      binding.bottomsheetCoordinatorContainer.background.alpha = 255
      setBackListener(binding.bottomsheetCoordinatorContainer)
    }
  }

  override fun getBottomSheetContainerClick() = RxView.clicks(binding.bottomsheetCoordinatorContainer)

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
