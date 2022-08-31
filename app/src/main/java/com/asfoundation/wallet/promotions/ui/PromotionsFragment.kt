package com.asfoundation.wallet.promotions.ui

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentPromotionsBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.promotions.model.GamificationItem
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.promotions.ui.list.PromotionsController
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class PromotionsFragment : BasePageViewFragment(),
  SingleStateFragment<PromotionsState, PromotionsSideEffect> {

  @Inject
  lateinit var navigator: PromotionsNavigator

  @Inject
  lateinit var gamificationMapper: GamificationMapper

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  private lateinit var gamificationHeaderBinding: GamificationHeaderBindingAdapter


  val df = DecimalFormat("###.#")

  private lateinit var promotionsController: PromotionsController

  private val viewModel: PromotionsViewModel by viewModels()
  private val views by viewBinding(FragmentPromotionsBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchPromotions()
    viewModel.fetchGamificationStats()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    promotionsController = PromotionsController()
    promotionsController.clickListener = { promotionClick ->
      viewModel.promotionClicked(promotionClick)
    }
    views.rvPromotions.setController(promotionsController)
    views.rvPromotions.addItemDecoration(
      MarginItemDecoration(
        resources.getDimension(R.dimen.promotions_item_margin).toInt()
      )
    )

    views.toolbar.gamificationInfoBtn.setOnClickListener { viewModel.gamificationInfoClicked() }
    views.noNetwork.retryButton.setOnClickListener {
      viewModel.fetchPromotions()
      viewModel.fetchGamificationStats()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: PromotionsState) {
    setPromotionsModel(state.promotionsModelAsync, state.gamificationStatsAsync)
  }

  override fun onSideEffect(sideEffect: PromotionsSideEffect) {
    when (sideEffect) {
      is PromotionsSideEffect.NavigateToGamification -> navigator.navigateToGamification(
        sideEffect.cachedBonus
      )
      is PromotionsSideEffect.NavigateToShare -> navigator.handleShare(sideEffect.url)
      PromotionsSideEffect.NavigateToInfo -> navigator.navigateToInfo()
      PromotionsSideEffect.NavigateToInviteFriends -> navigator.navigateToInviteFriends()
      PromotionsSideEffect.ShowErrorToast -> showErrorToast()
    }
  }

  private fun setPromotionsModel(
    asyncPromotionsModel: Async<PromotionsModel>,
    asyncGamificationStats: Async<GamificationStats>
  ) {
    when (asyncPromotionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (asyncPromotionsModel.value == null) {
          showLoading()
          showPromotionSkeleton()
        } else {
          if (asyncPromotionsModel.value.error == PromotionsModel.Status.NO_NETWORK) {
            showNoNetworkErrorLoading()
          }
        }
      }
      is Async.Fail -> showErrorToast()
      // PromotionsModel has the errors mapped into the object itself, so expected errors will be
      // on success... In the future errors should just be thrown so they're handled with Async.Fail
      is Async.Success -> {
        asyncGamificationStats()?.let {
          setPromotions(
            asyncPromotionsModel(),
            asyncPromotionsModel.previousValue,
            it
          )
        }
      }
    }
  }

  private fun setPromotions(
    promotionsModel: PromotionsModel,
    previousModel: PromotionsModel?,
    gamificationStats: GamificationStats
  ) {
    hideLoading()
    if (promotionsModel.hasError() && !promotionsModel.fromCache) {
      // In the future we should get rid of this previousModel. Here it exists because the offline
      // first flow emits when it shouldn't (e.g. it emits a network error even if local data exists)
      if (previousModel == null || previousModel.hasError()) {
        if (promotionsModel.error == PromotionsModel.Status.NO_NETWORK) {
          showNetworkErrorView()
        } else {
          // In case of error that is not "no network", this screen will be shown. This was already
          // like this. I think a general error screen was implemented with vouchers, so on merge
          // we should check this out.
          showNoPromotionsScreen(promotionsModel, gamificationStats)
        }
      }
    } else if (promotionsModel.walletOrigin == PromotionsModel.WalletOrigin.UNKNOWN) {
      if (!promotionsModel.fromCache) {
        showLockedPromotionsScreen()
      }
    } else {
      if (promotionsModel.perks.isEmpty()) {
        showNoPromotionsScreen(promotionsModel, gamificationStats)
      } else {
        showPromotions(promotionsModel, gamificationStats)
      }
    }
  }

  private fun showPromotions(
    promotionsModel: PromotionsModel,
    gamificationStats: GamificationStats
  ) {
    promotionsController.setData(promotionsModel)
    showPromotionsHeader(promotionsModel, gamificationStats, hasPerksAvailable = true)
    views.rvPromotions.visibility = View.VISIBLE
    views.noNetwork.root.visibility = View.GONE
    views.lockedPromotions.root.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
  }

  private fun showPromotionsHeader(
    promotionsModel: PromotionsModel,
    gamificationStats: GamificationStats,
    hasPerksAvailable: Boolean
  ) {
    views.currentLevelHeader.root.visibility = View.VISIBLE

    val gamificationHeaderItem: GamificationItem =
      (promotionsModel.promotions[0] as GamificationItem)
    val gamificationHeaderLayout = getGamificationHeaderBinding(gamificationHeaderItem)

    gamificationHeaderLayout.type?.root?.visibility = View.VISIBLE
    gamificationHeaderLayout.type?.root?.setOnClickListener {
      viewModel.promotionClicked(PromotionClick(gamificationHeaderItem.id))
    }

    showGamificationHeaderColors(gamificationHeaderLayout, gamificationHeaderItem)
    showGamificationHeaderText(gamificationHeaderLayout, gamificationHeaderItem)
    showGamificationHeaderProgress(gamificationHeaderLayout, gamificationStats)

    if (hasPerksAvailable) {
      showPromotionsTitle()
    } else {
      views.promotionsListTitleLayout.root.visibility =
        View.GONE
    }
  }

  private fun getGamificationHeaderBinding(gamificationHeaderItem: GamificationItem): GamificationHeaderBindingAdapter {
    return when (gamificationHeaderItem.gamificationStatus) {
      GamificationStatus.APPROACHING_VIP -> {
        GamificationHeaderBindingAdapter(
          null,
          views.currentLevelHeader.almostVipLevelHeader,
          null,
          null
        )
      }
      GamificationStatus.VIP -> {
        GamificationHeaderBindingAdapter(
          null,
          null,
          views.currentLevelHeader.vipLevelHeader,
          null
        )
      }
      GamificationStatus.VIP_MAX -> {
        GamificationHeaderBindingAdapter(
          null,
          null,
          null,
          views.currentLevelHeader.vipMaxLevelHeader
        )
      }
      else -> {
        GamificationHeaderBindingAdapter(
          views.currentLevelHeader.regularLevelHeader,
          null,
          null,
          null
        )
      }
    }
  }

  private fun showGamificationHeaderColors(
    gamificationHeaderLayout: GamificationHeaderBindingAdapter,
    gamificationHeaderItem: GamificationItem
  ) {
    when (gamificationHeaderItem.gamificationStatus) {
      GamificationStatus.VIP,
      GamificationStatus.VIP_MAX -> {
        gamificationHeaderLayout.currentLevelProgressBar?.progressTintList =
          ColorStateList.valueOf(
            ContextCompat.getColor(
              requireContext(),
              R.color.only_for_vip_background_color
            )
          )
      }
      else -> {
        gamificationHeaderLayout.currentLevelBonus?.background =
          gamificationMapper.getOvalBackground(gamificationHeaderItem.levelColor)
        gamificationHeaderLayout.currentLevelProgressBar?.progressTintList =
          ColorStateList.valueOf(
            gamificationHeaderItem.levelColor
          )
      }
    }

    gamificationHeaderLayout.currentLevelImage?.setImageDrawable(gamificationHeaderItem.planet)
  }

  private fun showGamificationHeaderText(
    gamificationHeaderLayout: GamificationHeaderBindingAdapter,
    gamificationHeaderItem: GamificationItem
  ) {
    gamificationHeaderLayout.currentLevelBonus?.text =
      getString(R.string.gamif_bonus, df.format(gamificationHeaderItem.bonus))

    when (gamificationHeaderItem.gamificationStatus) {
      GamificationStatus.VIP_MAX -> {
        gamificationHeaderLayout.currentLevelTitle?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
        gamificationHeaderLayout.currentLevelSubtitle?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11F)

        gamificationHeaderLayout.currentLevelTitle?.text =
          getString(R.string.vip_program_max_bonus_title, df.format(gamificationHeaderItem.bonus))
        gamificationHeaderLayout.currentLevelSubtitle?.text =
          getString(R.string.vip_program_max_bonus_body)
      }
      else -> {
        gamificationHeaderLayout.currentLevelTitle?.text = gamificationHeaderItem.title
        if (gamificationHeaderItem.toNextLevelAmount != null) {
          gamificationHeaderLayout.currentLevelSubtitle?.text = getString(
            R.string.gamif_card_body,
            currencyFormatUtils.formatGamificationValues(gamificationHeaderItem.toNextLevelAmount)
          )
        } else {
          gamificationHeaderLayout.currentLevelSubtitle?.visibility = View.INVISIBLE
        }
      }
    }
  }

  private fun showGamificationHeaderProgress(
    gamificationHeaderLayout: GamificationHeaderBindingAdapter,
    gamificationStats: GamificationStats
  ) {
    val progress = gamificationMapper.getProgressPercentage(
      gamificationStats.totalSpend,
      gamificationStats.nextLevelAmount
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      gamificationHeaderLayout.currentLevelProgressBar?.setProgress(progress.toInt(), true)
    } else {
      gamificationHeaderLayout.currentLevelProgressBar?.progress = progress.toInt()
    }
    gamificationHeaderLayout.currentLevelProgressLeft?.text =
      gamificationMapper.validateAndGetProgressString(
        gamificationStats.totalSpend,
        gamificationStats.nextLevelAmount
      )
  }

  private fun showPromotionsTitle() {
    views.promotionsListTitleLayout.root.visibility = View.VISIBLE
    views.promotionsListTitleLayout.promotionsTitle.text = getString(R.string.perks_title)
    views.promotionsListTitleLayout.promotionsSubtitle.text = getString(R.string.perks_body)
  }

  private fun showNoPromotionsScreen(
    promotionsModel: PromotionsModel,
    gamificationStats: GamificationStats
  ) {
    showPromotionsHeader(promotionsModel, gamificationStats, hasPerksAvailable = false)
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noPromotions.root.visibility = View.VISIBLE
    views.lockedPromotions.root.visibility = View.GONE
  }

  private fun showLoading() {
    views.headerLoadingContainer.visibility = View.VISIBLE
    views.currentLevelHeader.root.visibility = View.GONE
    views.lockedPromotions.root.visibility = View.GONE
  }

  private fun showPromotionSkeleton() {
    views.promotionsSkeleton.root.visibility = View.VISIBLE
    views.lockedPromotions.root.visibility = View.GONE
  }

  private fun showNoNetworkErrorLoading() {
    views.noNetwork.retryButton.visibility = View.INVISIBLE
    views.noNetwork.retryAnimation.visibility = View.VISIBLE
  }

  private fun hideLoading() {
    views.headerLoadingContainer.visibility = View.GONE
    views.promotionsSkeleton.root.visibility = View.GONE
  }

  private fun showErrorToast() {
    Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
      .show()
  }

  private fun showNetworkErrorView() {
    views.currentLevelHeader.root.visibility = View.GONE
    views.rvPromotions.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
    views.noNetwork.root.visibility = View.VISIBLE
    views.noNetwork.retryButton.visibility = View.VISIBLE
    views.noNetwork.retryAnimation.visibility = View.GONE
  }

  private fun showLockedPromotionsScreen() {
    views.currentLevelHeader.root.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.noNetwork.retryAnimation.visibility = View.GONE
    views.noPromotions.root.visibility = View.GONE
    views.lockedPromotions.root.visibility = View.VISIBLE
    views.rvPromotions.visibility = View.GONE
  }
}