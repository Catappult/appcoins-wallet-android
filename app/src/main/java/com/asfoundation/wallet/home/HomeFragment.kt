package com.asfoundation.wallet.home

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RootUtil
import com.appcoins.wallet.core.utils.android_common.WalletCurrency.FIAT
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.BalanceNewCard
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.appcoins.wallet.ui.widgets.ConfirmEmailCard
import com.appcoins.wallet.ui.widgets.GamesBundle
import com.appcoins.wallet.ui.widgets.JoinDiscordCardComposable
import com.appcoins.wallet.ui.widgets.PromotionsCardComposable
import com.appcoins.wallet.ui.widgets.SkeletonLoadingPromotionCards
import com.appcoins.wallet.ui.widgets.SkeletonLoadingTransactionCard
import com.appcoins.wallet.ui.widgets.TransactionCard
import com.appcoins.wallet.ui.widgets.WelcomeEmailCard
import com.appcoins.wallet.ui.widgets.component.BalanceValue
import com.appcoins.wallet.ui.widgets.openGame
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.HomeViewModel.UiState
import com.asfoundation.wallet.home.HomeViewModel.UiState.Success
import com.asfoundation.wallet.main.nav_bar.NavBarViewModel
import com.asfoundation.wallet.promotions.model.DefaultItem
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.transactions.StatusType
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.TransactionsNavigator
import com.asfoundation.wallet.transactions.cardInfoByType
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import javax.inject.Inject
import androidx.core.net.toUri

// Before moving this screen into the :home module, all home dependencies need to be independent
// from the :app module.
@AndroidEntryPoint
class HomeFragment : BasePageViewFragment(), SingleStateFragment<HomeState, HomeSideEffect> {

  @Inject
  lateinit var navigator: HomeNavigator

  @Inject
  lateinit var transactionsNavigator: TransactionsNavigator

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private val viewModel: HomeViewModel by viewModels()
  private val navBarViewModel: NavBarViewModel by activityViewModels()
  private val hasGetSomeValidBalanceResult = mutableStateOf(false)
  private val fragmentName = this::class.java.simpleName
  private var balanceCurrency: String = ""
  private var balanceValue: String = ""

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { HomeScreen() } }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onResume() {
    super.onResume()
    val fromSupportNotification =
      requireActivity()
        .intent
        .getBooleanExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK, false)
    if (!fromSupportNotification) {
      viewModel.updateData()
      checkRoot()
    } else {
      viewModel.showSupportScreen()
    }
    viewModel.fetchPromotions()
    viewModel.isEmailError.value = false
  }

  override fun onStart() {
    super.onStart()
    navBarViewModel.clickedItem.value = Destinations.HOME.ordinal
  }

  override fun onPause() {
    super.onPause()
    viewModel.stopRefreshingData()
  }

  @Composable
  fun HomeScreen(
    modifier: Modifier = Modifier,
  ) {
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
      HomeScreenContent(padding = padding)
    }
  }

  @Composable
  internal fun HomeScreenContent(padding: PaddingValues) {
    getBalanceText(viewModel)
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      BalanceNewCard(
        onClickTopUp = { viewModel.onTopUpClick() },
        isLoading = (viewModel.isLoadingOrIdleBalanceState() && !hasGetSomeValidBalanceResult.value) ||
            !viewModel.isLoadingTransactions.value,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics,
        onClickPromoCode = { navigator.navigateToPromoCode()},
        onClickBackup = { viewModel.onBackupClick() },
        onClickMore = { navigator.navigateToManageBottomSheet(viewModel.canTransfer.value) },
        balance = balanceValue,
        showBackup = viewModel.showBackup.value,
        onClickDetailsBalance = { navigator.navigateToDetailsBalanceBottomSheet(balanceValue, balanceCurrency) },
      )
      /*BalanceCard(
        newWallet = viewModel.newWallet.value,
        showBackup = viewModel.showBackup.value,
        balanceContent = { BalanceContent() },
        onClickTransfer = { viewModel.onTransferClick() },
        onClickBackup = { viewModel.onBackupClick() },
        onClickTopUp = { viewModel.onTopUpClick() },
        onClickMenuOptions = { navigator.navigateToManageBottomSheet() },
        isLoading =
        (viewModel.isLoadingOrIdleBalanceState() && !hasGetSomeValidBalanceResult.value) ||
            !viewModel.isLoadingTransactions.value,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )*/
      if(viewModel.showRebrandingBanner.value) {
        RebrandingBanner()
      }
      if(viewModel.showDiscordBanner.value) {
        DiscordBannerContent()
      }
      PromotionsList()
      TransactionsCard(transactionsState = viewModel.uiState.collectAsState().value)
      UserEmailCard()
      val listState = rememberLazyListState()
      LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
          .filter { it }
          .collectLatest {
            viewModel.getImpression()
          }
      }
      GamesBundle(
        listState,
        viewModel.gamesList.value,
        viewModel.referenceSendPromotionClickEvent()
      ) { viewModel.fetchGamesListing() }
      Spacer(modifier = Modifier.padding(40.dp))
    }
  }

  @Composable
  fun getBalanceText(viewModel: HomeViewModel): String {
    return when (val balanceState = viewModel.uiBalanceState.collectAsState().value) {
      is HomeViewModel.UiBalanceState.Success -> {
        val balance = balanceState.balance.creditsOnlyFiat
        balanceCurrency = balance.currency
        balanceValue = balance.symbol + formatter.formatCurrency(balance.amount, FIAT)
        balanceValue

      }
      else -> ""
    }
  }
  @Composable
  fun DiscordBannerContent() {
    val showDiscordBanner =
      remember { mutableStateOf(viewModel.isShowDiscordBanner()) }
    if(showDiscordBanner.value == true) {
      JoinDiscordCardComposable(
        {
          val intent = Intent(Intent.ACTION_VIEW, "https://discord.com/invite/Byec5eetAG".toUri())
          context?.startActivity(intent)
        },
        {
          viewModel.saveShowDiscordBanner(false)
          showDiscordBanner.value = false
        },
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }
  @Composable
  fun BalanceContent() =
    when (val state = viewModel.uiBalanceState.collectAsState().value) {
      is HomeViewModel.UiBalanceState.Success ->
        with(state.balance.creditsOnlyFiat) {
          BalanceValue(symbol + formatter.formatCurrency(amount, FIAT), currency) {
            viewModel.onBalanceArrowClick(state.balance)
          }
        }

      else -> CircularProgressIndicator()
    }

  @Composable
  fun TransactionsCard(transactionsState: UiState) {
    when (transactionsState) {
      is Success -> {
        if (transactionsState.transactions.isNotEmpty())
          Column(
            modifier = Modifier
              .heightIn(0.dp, 480.dp)
              .padding(horizontal = 16.dp)
          ) {
            Text(
              text = stringResource(R.string.intro_transactions_header),
              modifier = Modifier.padding(start = 8.dp, bottom = 16.dp, top = 16.dp),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = WalletColors.styleguide_dark_grey
            )
            Card(colors = CardDefaults.cardColors(WalletColors.styleguide_dark_secondary)) {
              TransactionsList(transactionsState.transactions)
            }
          }
      }

      else -> {
        Column(
          modifier = Modifier
            .heightIn(0.dp, 480.dp)
            .padding(horizontal = 16.dp)
        ) {
          Text(
            text = stringResource(R.string.intro_transactions_header),
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = WalletColors.styleguide_dark_grey
          )
          SkeletonLoadingTransactionCard()
        }
      }
    }
  }

  @Composable
  fun PromotionsList() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (viewModel.activePromotions.isNotEmpty() && !viewModel.isLoadingOrIdlePromotionState()) {
      Text(
        text = getString(R.string.intro_active_promotions_header),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_dark_grey,
        modifier = Modifier.padding(top = 16.dp, end = 13.dp, start = 24.dp)
      )
    }
    if (viewModel.activePromotions.isEmpty() && viewModel.isLoadingOrIdlePromotionState()) {
      Text(
        text = getString(R.string.intro_active_promotions_header),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_dark_grey,
        modifier = Modifier.padding(top = 16.dp, end = 13.dp, start = 24.dp)
      )
    }
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.Bottom
    ) {
      if (viewModel.activePromotions.isEmpty() && viewModel.isLoadingOrIdlePromotionState()) {
        item { SkeletonLoadingPromotionCards(hasVerticalList = false) }
      } else {
        items(viewModel.activePromotions) { promotion ->
          PromotionsCardComposable(
            cardItem = promotion,
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics,
            modifier = Modifier.fillParentMaxWidth(if (isLandscape) 0.45f else 0.9f)
          )
        }
      }
    }
  }

  @Composable
  fun UserEmailCard() {
    val email = remember { mutableStateOf(viewModel.getWalletEmailPreferencesData()) }
    val hideUserEmailCard =
      remember { mutableStateOf(viewModel.isHideWalletEmailCardPreferencesData()) }
    val hasSavedEmail = remember { viewModel.hasSavedEmail }
    val isEmailError = remember { viewModel.isEmailError }
    if (!hideUserEmailCard.value) {
      if (!hasSavedEmail.value) {
        WelcomeEmailCard(
          email,
          {
            viewModel.postUserEmail(email.value)
          },
          {
            viewModel.saveHideWalletEmailCardPreferencesData(true)
            hideUserEmailCard.value = true
          },
          isEmailError.value,
          if (isEmailError.value) stringResource(id = viewModel.emailErrorText.value) else "",
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      } else {
        ConfirmEmailCard(email = email) {
          viewModel.saveHideWalletEmailCardPreferencesData(true)
          hideUserEmailCard.value = true
        }
      }
    }
  }

  @Composable
  fun LottieAnimationLoop() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.rebranding_new))
    val progress by animateLottieCompositionAsState(
      composition,
      iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
      composition = composition,
      progress = { progress },
      modifier = Modifier.size(24.dp)
    )
  }

  @Composable
  fun RebrandingBanner() {
    val showRebrandBanner =
      remember { mutableStateOf(viewModel.isShowRebrandingBanner()) }

    if(showRebrandBanner.value == true) {
      Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_rebranding_blue),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
              painter = painterResource(id = R.drawable.ic_reb_cross_purple),
              contentDescription = "Close",
              tint = WalletColors.styleguide_rebranding_subtext,
              modifier = Modifier
                .clickable {
                  viewModel.saveShowRebrandingBanner(false)
                  showRebrandBanner.value = false
                }
            )
          }
          Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp, start = 10.dp, end = 10.dp)
          ) {
            LottieAnimationLoop()
            Text(
              text = stringResource(R.string.rebranding_post_title),
              modifier = Modifier
                .padding(start = 4.dp, end = 16.dp),
              fontWeight = FontWeight(600),
              fontSize = 14.sp,
              color = WalletColors.styleguide_light_grey,
            )
          }

          Text(
            text = stringResource(R.string.rebrandinh_post_body),
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 16.sp,
            fontSize = 12.sp,
            fontWeight = FontWeight(400),
            color = WalletColors.styleguide_rebranding_subtext
          )
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun TransactionsList(transactionsGrouped: Map<String, List<TransactionModel>>) {
    Box(contentAlignment = Alignment.TopEnd) {
      LazyColumn(userScrollEnabled = false, modifier = Modifier.padding(vertical = 8.dp)) {
        transactionsGrouped.forEach { (date, transactionsForDate) ->
          stickyHeader {
            Text(
              text = date,
              color = WalletColors.styleguide_dark_grey,
              modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp),
              style = MaterialTheme.typography.bodySmall
            )
          }

          items(transactionsForDate) { transaction ->
            with(transaction.cardInfoByType()) {
              TransactionCard(
                icon = icon,
                appIcon = appIcon,
                title = stringResource(id = title),
                description = if (status == StatusType.PENDING)
                  stringResource(R.string.transaction_status_pending)
                else
                  description ?: app,
                amount = amount,
                subIcon = subIcon,
                onClick = { navigateToTransactionDetails(transaction) },
                textDecoration = textDecoration,
                isPending = status == StatusType.PENDING
              )
            }
          }
        }
      }
      TextButton(
        modifier = Modifier.padding(top = 8.dp, end = 8.dp),
        onClick = { viewModel.onSeeAllTransactionsClick() }) {
        Text(
          text = stringResource(R.string.see_all_button),
          color = WalletColors.styleguide_primary,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }

  @Preview(showBackground = true)
  @Composable
  fun HomeScreenPreview() {
    HomeScreen()
  }

  override fun onStateChanged(state: HomeState) {
    setBalance(state.defaultWalletBalanceAsync)
    setPromotions(state.promotionsModelAsync)
    setBackup(state.hasBackup)
    setRebrandingBanner(state.showRebrandingBanner)
    setDiscordBanner(state.showDiscordBanner)
  }

  override fun onSideEffect(sideEffect: HomeSideEffect) {
    when (sideEffect) {
      is HomeSideEffect.NavigateToBrowser -> navigator.navigateToBrowser(sideEffect.uri)
      is HomeSideEffect.NavigateToRateUs -> navigator.navigateToRateUs(sideEffect.shouldNavigate)
      is HomeSideEffect.NavigateToSettings ->
        navigator.navigateToSettings(navController(), sideEffect.turnOnFingerprint)

      is HomeSideEffect.NavigateToBackup ->
        navigator.navigateToBackup(
          sideEffect.walletAddress, sideEffect.walletName, navController()
        )

      is HomeSideEffect.NavigateToRecover -> navigator.navigateToRecoverWallet()
      is HomeSideEffect.NavigateToIntent -> navigator.openIntent(sideEffect.intent)
      HomeSideEffect.NavigateToTopUp -> navigator.navigateToTopUp()
      HomeSideEffect.NavigateToTransfer -> navigator.navigateToTransfer(navController())
      HomeSideEffect.NavigateToTransactionsList ->
        transactionsNavigator.navigateToTransactionsList(navController())

      is HomeSideEffect.NavigateToBalanceDetails ->
        navigator.navigateToBalanceBottomSheet(sideEffect.balance)
    }
  }

  private fun checkRoot() {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit().putBoolean("should_show_root_warning", false).apply()
      val alertDialog =
        AlertDialog.Builder(context)
          .setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok) { _, _ -> }
          .show()
      alertDialog
        .getButton(AlertDialog.BUTTON_NEGATIVE)
        .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
      alertDialog
        .getButton(AlertDialog.BUTTON_NEGATIVE)
        .setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_primary, null))
    }
  }

  private fun setBalance(balanceAsync: Async<GlobalBalance>) {
    when (balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        viewModel.updateBalance(HomeViewModel.UiBalanceState.Loading)
      }

      is Async.Success ->
        balanceAsync.value.let { globalBalance ->
          if (globalBalance != null) {
            viewModel.updateBalance(
              HomeViewModel.UiBalanceState.Success(globalBalance.walletBalance)
            )
            hasGetSomeValidBalanceResult.value = true
          }
        }

      else -> Unit
    }
  }

  private fun setBackup(hasBackup: Async<Boolean>) {
    when (hasBackup) {
      is Async.Success -> viewModel.showBackup.value = !(hasBackup.value ?: false)
      else -> Unit
    }
  }

  private fun setPromotions(promotionsModel: Async<PromotionsModel>) {
    when (promotionsModel) {
      is Async.Success -> {
        viewModel.activePromotions.clear()
        promotionsModel.value?.perks?.forEach { promotion ->
          if (promotion is DefaultItem) {
            val cardItem =
              CardPromotionItem(
                title = promotion.appName,
                subtitle = promotion.description,
                promotionStartTime = promotion.startDate,
                promotionEndTime = promotion.endDate,
                imageUrl = promotion.icon,
                urlRedirect = promotion.actionUrl,
                packageName = promotion.packageName,
                hasVipPromotion = promotion.gamificationStatus == GamificationStatus.VIP ||
                    promotion.gamificationStatus == GamificationStatus.VIP_MAX,
                hasFuturePromotion = false,
                hasVerticalList = false,
                action = {
                  openGame(
                    promotion.packageName ?: promotion.actionUrl,
                    promotion.actionUrl,
                    requireContext(),
                    viewModel.referenceSendPromotionClickEvent(),
                  )
                })
            viewModel.activePromotions.add(cardItem)
          }
        }

        Intercom.client().handlePushMessage()
      }

      else -> Unit
    }
  }

  private fun setRebrandingBanner(showBanner: Async<Boolean>) {
    when (showBanner) {
      is Async.Success -> viewModel.showRebrandingBanner.value = (showBanner.value ?: false)
      else -> Unit
    }
  }

  private fun setDiscordBanner(showBanner: Async<Boolean>) {
    when (showBanner) {
      is Async.Success -> viewModel.showDiscordBanner.value = (showBanner.value ?: false)
      else -> Unit
    }
  }

  private fun navigateToTransactionDetails(transaction: TransactionModel) =
    transactionsNavigator.navigateToTransactionDetails(navController(), transaction)

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }
}
