package com.asfoundation.wallet.wallet_reward

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_orange
import com.appcoins.wallet.ui.widgets.*
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.DefaultItem
import com.asfoundation.wallet.promotions.model.FutureItem
import com.asfoundation.wallet.promotions.model.GamificationItem
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class RewardFragment : BasePageViewFragment(), SingleStateFragment<RewardState, RewardSideEffect> {

  @Inject
  lateinit var navigator: RewardNavigator
  private val viewModel: RewardViewModel by viewModels()

  private var isVip by mutableStateOf(false)

  private val df = DecimalFormat("###.#")

  @Inject
  lateinit var currencyFormatUtils: CurrencyFormatUtils

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        RewardScreen()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onResume() {
    super.onResume()
//    viewModel.fetchPromotions()
    viewModel.fetchGamificationStats()
  }


  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun RewardScreen(
    modifier: Modifier = Modifier,
  ) {
    Scaffold(
      topBar = {
        Surface(shadowElevation = 4.dp) {
          TopBar(
            isMainBar = true,
            isVip = isVip,
            onClickNotifications = { Log.d("TestHomeFragment", "Notifications") },
            onClickSettings = { viewModel.onSettingsClick() },
            onClickSupport = { viewModel.showSupportScreen(false) },
          )
        }
      },
      containerColor = WalletColors.styleguide_blue,
      modifier = modifier
    ) { padding ->
      RewardScreenContent(
        padding = padding
      )
    }
  }

  @Composable
  internal fun RewardScreenContent(
    padding: PaddingValues
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      if(
        viewModel.gamificationHeaderModel.value != null &&
        viewModel.gamificationHeaderModel.value?.bonusPercentage != null &&
        viewModel.gamificationHeaderModel.value?.bonusPercentage!! >= 10.0
      ) {
        GamificationHeader(
          onClick = {
            navigator.navigateToGamification(
              cachedBonus = viewModel.gamificationHeaderModel.value!!.bonusPercentage
            )
          },
          indicatorColor = Color(
            viewModel.gamificationHeaderModel.value!!.color
          ),
          valueSpendForNextLevel = viewModel.gamificationHeaderModel.value!!.spendMoreAmount,
          currencySpend = " AppCoins Credits",
          currentProgress = viewModel.gamificationHeaderModel.value!!.currentSpent,
          maxProgress = viewModel.gamificationHeaderModel.value!!.nextLevelSpent ?: 0,
          bonusValue = df.format(viewModel.gamificationHeaderModel.value!!.bonusPercentage),
          planetDrawable = viewModel.gamificationHeaderModel.value!!.planetImage
        )
        if (viewModel.vipReferralModel.value != null) {
          VipReferralCard (
            {
              navigator.navigateToVipReferral(
                bonus = viewModel.vipReferralModel.value!!.vipBonus,
                code = viewModel.vipReferralModel.value!!.vipCode,
                totalEarned = viewModel.vipReferralModel.value!!.totalEarned,
                numberReferrals = viewModel.vipReferralModel.value!!.numberReferrals,
                mainNavController = navController()
              )
            },
            viewModel.vipReferralModel.value!!.vipBonus
          )
        }

      } else if (
        viewModel.gamificationHeaderModel.value != null &&
        viewModel.gamificationHeaderModel.value?.bonusPercentage!! > 0.0 &&
        viewModel.gamificationHeaderModel.value?.bonusPercentage!! < 10.0
      ) {
        GamificationHeaderPartner(
          df.format(viewModel.gamificationHeaderModel.value?.bonusPercentage)
        )
      } else {
        GamificationHeaderNoPurchases()
      }

      RewardsActions(
        { navigator.navigateToWithdrawScreen() },
        { navigator.showPromoCodeFragment() },
        { navigator.showGiftCardFragment() }
      )
      PromotionsList()
      Spacer(modifier = Modifier.padding(32.dp))
    }
  }


  @Composable
  private fun PromotionsList() {
    LazyColumn(
      modifier = Modifier
        .padding(
          start = 16.dp,
          end = 16.dp
        )
        .heightIn(min = 0.dp, max = 1000.dp),
      userScrollEnabled = false,
    ) {
      item {
        Text(
          text = getString(R.string.perks_title),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_dark_grey,
          modifier = Modifier.padding(top = 16.dp, end = 2.dp, start = 6.dp)
        )
      }
      items(viewModel.promotions) { promotion ->
        PromotionsCardComposable(cardItem = promotion)
      }
    }

  }

  @Composable
  fun DummyCard() {
    Card(
      modifier = Modifier
        .padding(
          start = 16.dp,
          end = 16.dp,
          top = 16.dp
        )
        .fillMaxWidth()
        .height(200.dp),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Reward Screen",
          style = MaterialTheme.typography.titleMedium,
          color = WalletColors.styleguide_white
        )
      }
    }
  }

  @Preview(showBackground = true)
  @Composable
  fun RewardScreenPreview() {
    RewardScreen()
  }

  override fun onStateChanged(state: RewardState) {
    showVipBadge(state.showVipBadge)
    setPromotions(state.promotionsModelAsync, state.promotionsGamificationStatsAsync)
  }

  override fun onSideEffect(sideEffect: RewardSideEffect) {
    when (sideEffect) {
      is RewardSideEffect.NavigateToSettings -> navigator.navigateToSettings(
        sideEffect.turnOnFingerprint
      )
    }
  }

  private fun setPromotions(
    promotionsModel: Async<PromotionsModel>,
    promotionsGamificationStats: Async<PromotionsGamificationStats>
  ) {
    when (promotionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
      }
      is Async.Success -> {
        viewModel.promotions.clear()
        promotionsModel.value!!.perks.forEach { promotion ->
          if (promotion is DefaultItem) {
            val cardItem = CardPromotionItem(
              promotion.appName,
              promotion.description,
              promotion.startDate,
              promotion.endDate,
              promotion.icon,
              promotion.detailsLink,
              promotion.gamificationStatus == GamificationStatus.VIP || promotion.gamificationStatus == GamificationStatus.VIP_MAX,
              false,
              true,
              action = { promotion.detailsLink?.let { openGame(it, requireContext()) } }
            )
            viewModel.promotions.add(cardItem)
          } else if (promotion is FutureItem) {
            val cardItem = CardPromotionItem(
              promotion.appName,
              promotion.description,
              promotion.startDate,
              promotion.endDate,
              promotion.icon,
              promotion.detailsLink,
              promotion.gamificationStatus == GamificationStatus.VIP || promotion.gamificationStatus == GamificationStatus.VIP_MAX,
              true,
              true,
              action = { promotion.detailsLink?.let { openGame(it, requireContext()) } }
            )
            viewModel.promotions.add(cardItem)
          }
        }

        setGamification(promotionsModel, promotionsGamificationStats)

        promotionsModel.value!!.vipReferralInfo?.let {
          viewModel.vipReferralModel.value = it
        }

      }
      else -> Unit
    }
  }

  private fun setGamification(
    promotionsModel: Async<PromotionsModel>,
    promotionsGamificationStats: Async<PromotionsGamificationStats>
  ) {

    if (
      promotionsGamificationStats.value != null &&
      promotionsModel.value?.promotions != null
    ) {
      val gamificationItem: GamificationItem? =
        (promotionsModel.value?.promotions?.get(0) as? GamificationItem)


      gamificationItem?.let {gamificationItem ->
        viewModel.gamificationHeaderModel.value =
          GamificationHeaderModel(
            color = gamificationItem.levelColor,
            planetImage = gamificationItem.planet,
            spendMoreAmount = if (gamificationItem.toNextLevelAmount != null)
                currencyFormatUtils.formatGamificationValues(gamificationItem.toNextLevelAmount)
              else
                "" ,
            currentSpent = promotionsGamificationStats.value!!.totalSpend.toInt(),
            nextLevelSpent = if (promotionsGamificationStats.value!!.nextLevelAmount != null)
                promotionsGamificationStats.value!!.nextLevelAmount!!.toInt()
              else
                null ,
            bonusPercentage = gamificationItem.bonus,
            //TODO isVipMax = gamificationItem?.gamificationStatus == GamificationStatus.VIP_MAX
          )
      }

    }

  }

  private fun showVipBadge(shouldShow: Boolean) {
    isVip = shouldShow
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

}
