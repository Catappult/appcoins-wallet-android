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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.*
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.model.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RewardFragment : BasePageViewFragment(), SingleStateFragment<RewardState, RewardSideEffect> {

  @Inject
  lateinit var navigator: RewardNavigator
  private val viewModel: RewardViewModel by viewModels()

  private var isVip by mutableStateOf(false)

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
      DummyCard()
      RewardsActions(
        { navigator.navigateToWithdrawScreen() },
        { navigator.showPromoCodeFragment() },
        { navigator.showGiftCardFragment() }
      )
      viewModel.activePromoCode.value?.let { ActivePromoCodeComposable(cardItem = it) }
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
    setPromotions(state.promotionsModelAsync)
  }

  override fun onSideEffect(sideEffect: RewardSideEffect) {
    when (sideEffect) {
      is RewardSideEffect.NavigateToSettings -> navigator.navigateToSettings(
        sideEffect.turnOnFingerprint
      )
    }
  }

  private fun setPromotions(promotionsModel: Async<PromotionsModel>) {
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
          } else if (promotion is PromoCodeItem) {
            val cardItem = ActiveCardPromoCodeItem(
              promotion.appName,
              promotion.description,
              promotion.icon,
              promotion.detailsLink,
              true,
              action = { promotion.detailsLink?.let { openGame(it, requireContext()) } }
            )
            viewModel.activePromoCode.value = cardItem
          }
        }
      }
      else -> Unit
    }
  }

  private fun showVipBadge(shouldShow: Boolean) {
    isVip = shouldShow
  }

}
