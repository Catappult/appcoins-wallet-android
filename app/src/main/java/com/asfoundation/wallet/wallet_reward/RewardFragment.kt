package com.asfoundation.wallet.wallet_reward

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.analytics.analytics.rewards.RewardsAnalytics
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse.App
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.ActiveCardPromoCodeItem
import com.appcoins.wallet.ui.widgets.ActivePromoCodeComposable
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.appcoins.wallet.ui.widgets.GamificationHeader
import com.appcoins.wallet.ui.widgets.GamificationHeaderNoPurchases
import com.appcoins.wallet.ui.widgets.GamificationHeaderPartner
import com.appcoins.wallet.ui.widgets.PromotionsCardComposable
import com.appcoins.wallet.ui.widgets.RewardsActions
import com.appcoins.wallet.ui.widgets.SkeletonLoadingGamificationCard
import com.appcoins.wallet.ui.widgets.SkeletonLoadingPromotionCards
import com.appcoins.wallet.ui.widgets.VipReferralCardComposable
import com.appcoins.wallet.ui.widgets.expanded
import com.appcoins.wallet.ui.widgets.openGame
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.main.nav_bar.NavBarViewModel
import com.asfoundation.wallet.promotions.model.DefaultItem
import com.asfoundation.wallet.promotions.model.FutureItem
import com.asfoundation.wallet.promotions.model.GamificationItem
import com.asfoundation.wallet.promotions.model.PromoCodeItem
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin.APTOIDE
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin.PARTNER
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin.PARTNER_NO_BONUS
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin.UNKNOWN
import com.asfoundation.wallet.promotions.model.VipReferralInfo
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class RewardFragment : BasePageViewFragment(), SingleStateFragment<RewardState, RewardSideEffect> {

  companion object {
    const val EXTRA_GIFT_CARD = "giftCard"
    const val EXTRA_PROMO_CODE = "promoCode"
  }

  @Inject
  lateinit var navigator: RewardNavigator

  private val navBarViewModel: NavBarViewModel by activityViewModels()

  private val viewModel: RewardViewModel by viewModels()

  private val rewardSharedViewModel: RewardSharedViewModel by activityViewModels()

  private val df = DecimalFormat("###.#")

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  @Inject
  lateinit var analytics: RewardsAnalytics

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    analytics.rewardsImpressionEvent()
    return ComposeView(requireContext()).apply { setContent { RewardScreen() } }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    arguments?.getString(EXTRA_GIFT_CARD)?.let {
      navigator.showGiftCardFragment(it)
      arguments?.remove(EXTRA_GIFT_CARD)
    }
    arguments?.getString(EXTRA_PROMO_CODE)?.let {
      navigator.showPromoCodeFragment(it)
      arguments?.remove(EXTRA_PROMO_CODE)
    }
  }

  override fun onResume() {
    super.onResume()
    navBarViewModel.clickedItem.value = Destinations.REWARDS.ordinal
  }

  @Composable
  fun RewardScreen(
    modifier: Modifier = Modifier,
  ) {
    val dialogDismissed by rewardSharedViewModel.dialogDismissed
    LaunchedEffect(key1 = dialogDismissed) {
      viewModel.fetchPromotions()
      viewModel.fetchGamificationStats()
      viewModel.fetchWalletInfo()
      viewModel.getCurrency()
    }
    Scaffold(
      topBar = {
        Surface {
          TopBar(
            isMainBar = true,
            onClickSettings = { viewModel.onSettingsClick() },
            onClickSupport = { viewModel.showSupportScreen() },
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
      },
      containerColor = WalletColors.styleguide_dark,
      modifier = modifier
    ) { padding ->
      RewardScreenContent(padding = padding)
    }
  }

  @Composable
  internal fun RewardScreenContent(padding: PaddingValues) {
    LazyColumn(
      modifier = Modifier.padding(padding),
    ) {
      item {
        with(viewModel.gamificationHeaderModel.value) {
          when {
            this != null && walletOrigin == APTOIDE -> {
              GamificationContentAptoide(this)
            }

            this != null && walletOrigin == PARTNER -> {
              GamificationHeaderPartner(this.partnerPerk?.description ?: "")
            }

            this != null && walletOrigin == PARTNER_NO_BONUS -> {
              // No Gamification header
            }

            this != null && this.uninitialized -> {
              SkeletonLoadingGamificationCard()
            }

            else -> {
              GamificationHeaderNoPurchases()
            }
          }

          val vipRefModel = viewModel.vipReferralModel.value
          if (vipRefModel != null) {
            VipReferralCardComposable(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              vipBonus = vipRefModel.vipBonus,
              endDate = vipRefModel.endDate,
              referralCode = vipRefModel.vipCode,
              numberReferrals = vipRefModel.numberReferrals,
              totalEarned = vipRefModel.totalEarnedConvertedCurrency,
              appName = vipRefModel.app.appName,
              appIcon = vipRefModel.app.appIcon,
              onShare = { code -> },
            )
          }

          RewardsActions(
            onClickPromoCode = {
              analytics.promoCodeClickEvent()
              navigator.showPromoCodeFragment()
            },
            onClickGiftCard = { navigator.showGiftCardFragment() },
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }

        viewModel.activePromoCode.value?.let {
          ActivePromoCodeComposable(
            cardItem = it,
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
      }
      item {
        if (viewModel.promotions.isNotEmpty() && !viewModel.isLoadingOrIdlePromotionState()) {
          Text(
            text = getString(R.string.perks_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WalletColors.styleguide_dark_grey,
            modifier = Modifier.padding(top = 16.dp, start = 24.dp, bottom = 6.dp)
          )
        }
        if (viewModel.promotions.isEmpty() && viewModel.isLoadingOrIdlePromotionState()) {
          Text(
            text = getString(R.string.perks_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WalletColors.styleguide_dark_grey,
            modifier = Modifier.padding(top = 16.dp, start = 24.dp, bottom = 6.dp)
          )
          SkeletonLoadingPromotionCards(hasVerticalList = true)
        }
      }
      items(viewModel.promotions) { promotion ->
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
          PromotionsCardComposable(
            cardItem = promotion,
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
      }

      item { Spacer(modifier = Modifier.padding(40.dp)) }
    }
  }

  @Composable
  fun GamificationContentAptoide(
    gamificationHeader: GamificationHeaderModel
  ) {
    BoxWithConstraints {
      if (expanded()) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
          Column(modifier = Modifier.weight(1f)) {
            GamificationHeaderAptoide(gamificationHeader = gamificationHeader)
          }
        }
      } else {
        Column {
          GamificationHeaderAptoide(gamificationHeader = gamificationHeader)
        }
      }
    }
  }

  @Composable
  fun GamificationHeaderAptoide(gamificationHeader: GamificationHeaderModel) {
    with(gamificationHeader) {
      GamificationHeader(
        onClick = { navigator.navigateToGamification(cachedBonus = this.bonusPercentage) },
        indicatorColor = Color(color),
        valueSpendForNextLevel = spendMoreAmount,
        currencySpend = currency ?: "",
        currentProgress = currentSpent,
        maxProgress = nextLevelSpent ?: 0,
        bonusValue = df.format(bonusPercentage),
        planetDrawable = planetImage,
        isVip = isVip,
        isMaxVip = isMaxVip,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Preview(showBackground = true)
  @Composable
  fun RewardScreenPreview() {
    RewardScreen()
  }

  override fun onStateChanged(state: RewardState) {
    setPromotions(
      state.promotionsModelAsync,
      state.promotionsGamificationStatsAsync,
      state.selectedCurrency
    )
  }

  override fun onSideEffect(sideEffect: RewardSideEffect) {
    when (sideEffect) {
      is RewardSideEffect.NavigateToSettings ->
        navigator.navigateToSettings(navController(), sideEffect.turnOnFingerprint)
    }
  }

  private fun setPromotions(
    promotionsModel: Async<PromotionsModel>,
    promotionsGamificationStats: Async<PromotionsGamificationStats>,
    selectedCurrency: Async<String?>
  ) {
    when (promotionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
      }

      is Async.Success -> {
        viewModel.promotions.clear()
        viewModel.activePromoCode.value = null
        promotionsModel.value?.perks?.forEach { promotion ->
          when (promotion) {
            is DefaultItem -> viewModel.promotions.add(
              promotion.toCardPromotionItem(requireContext()) { _, _ ->
                viewModel.referenceSendPromotionClickEvent()
              }
            )

            is FutureItem -> viewModel.promotions.add(
              promotion.toCardPromotionItem(requireContext()) { _, _ ->
                viewModel.referenceSendPromotionClickEvent()
              }
            )

            is PromoCodeItem -> viewModel.activePromoCode.value =
              promotion.toCardPromotionItem(requireContext()) { _, _ ->
                viewModel.referenceSendPromotionClickEvent()
              }

            else -> {}
          }
        }

        setGamification(promotionsModel, promotionsGamificationStats, selectedCurrency.value)

        promotionsModel.value!!.vipReferralInfo?.let { viewModel.vipReferralModel.value = it }
      }

      else -> Unit
    }
  }

  private fun setGamification(
    promotionsModel: Async<PromotionsModel>,
    promotionsGamificationStats: Async<PromotionsGamificationStats>,
    selectedCurrency: String?
  ) {
    if (promotionsGamificationStats.value != null && promotionsModel.value?.promotions != null) {
      val gamificationItem: GamificationItem? =
        (promotionsModel.value?.promotions?.getOrNull(0) as? GamificationItem)
      val gamificationStatus =
        promotionsGamificationStats.value?.gamificationStatus ?: GamificationStatus.NONE

      viewModel.gamificationHeaderModel.value =
        GamificationHeaderModel(
          color = gamificationItem?.levelColor ?: Color.Transparent.toArgb(),
          planetImage = gamificationItem?.planet,
          spendMoreAmount = gamificationItem?.toNextLevelAmount
            ?.let { currencyFormatUtils.formatGamificationValues(it) }
            ?: "",
          currentSpent = promotionsGamificationStats.value?.totalSpend?.toInt() ?: 0,
          nextLevelSpent = promotionsGamificationStats.value?.nextLevelAmount?.toInt(),
          bonusPercentage = gamificationItem?.bonus ?: 0.0,
          isVip = gamificationStatus == GamificationStatus.VIP,
          isMaxVip = gamificationStatus == GamificationStatus.VIP_MAX,
          walletOrigin = promotionsModel.value?.walletOrigin ?: UNKNOWN,
          uninitialized = false,
          partnerPerk = promotionsModel.value?.partnerPerk,
          currency = selectedCurrency
        )
    } else {
      viewModel.gamificationHeaderModel.value = null
    }
  }

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }

}

private fun DefaultItem.toCardPromotionItem(
  context: Context,
  sendPromotionClickEvent: (String?, String) -> Unit
) =
  CardPromotionItem(
    title = this.appName,
    subtitle = this.description,
    promotionStartTime = this.startDate,
    promotionEndTime = this.endDate,
    imageUrl = this.icon,
    urlRedirect = this.actionUrl,
    packageName = this.packageName,
    hasVipPromotion = this.gamificationStatus == GamificationStatus.VIP || this.gamificationStatus == GamificationStatus.VIP_MAX,
    hasFuturePromotion = false,
    hasVerticalList = true,
    action = {
      openGame(
        gamePackage = this.packageName ?: this.actionUrl,
        actionUrl = this.actionUrl,
        context = context,
        sendPromotionClickEvent = sendPromotionClickEvent
      )
    }
  )

private fun FutureItem.toCardPromotionItem(
  context: Context,
  sendPromotionClickEvent: (String?, String) -> Unit
) =
  CardPromotionItem(
    title = this.appName,
    subtitle = this.description,
    promotionStartTime = this.startDate,
    promotionEndTime = this.endDate,
    imageUrl = this.icon,
    urlRedirect = this.actionUrl,
    packageName = this.packageName,
    hasVipPromotion = this.gamificationStatus == GamificationStatus.VIP || this.gamificationStatus == GamificationStatus.VIP_MAX,
    hasFuturePromotion = true,
    hasVerticalList = true,
    action = {
      openGame(
        gamePackage = this.packageName ?: this.actionUrl,
        actionUrl = this.actionUrl,
        context = context,
        sendPromotionClickEvent = sendPromotionClickEvent
      )
    }
  )

private fun PromoCodeItem.toCardPromotionItem(
  context: Context,
  sendPromotionClickEvent: (String?, String) -> Unit
) =
  ActiveCardPromoCodeItem(
    title = this.appName,
    subtitle = this.description,
    imageUrl = this.icon,
    urlRedirect = this.actionUrl,
    packageName = this.packageName,
    status = true,
    action = {
      openGame(
        gamePackage = this.packageName ?: this.actionUrl,
        actionUrl = this.actionUrl,
        context = context,
        sendPromotionClickEvent = sendPromotionClickEvent
      )
    }
  )