package com.asfoundation.wallet.wallet.home

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
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
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RootUtil
import com.appcoins.wallet.core.utils.android_common.WalletCurrency.FIAT
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.*
import com.appcoins.wallet.ui.widgets.component.BalanceValue
import com.asf.wallet.R
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.main.nav_bar.NavBarViewModel
import com.asfoundation.wallet.promotions.model.DefaultItem
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.transactions.Transaction.*
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.TransactionsNavigator
import com.asfoundation.wallet.transactions.cardInfoByType
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.asfoundation.wallet.wallet.home.HomeViewModel.UiState
import com.asfoundation.wallet.wallet.home.HomeViewModel.UiState.Success
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import javax.inject.Inject

// Before moving this screen into the :home module, all home dependencies need to be independent
// from the :app module.
@AndroidEntryPoint
class HomeFragment : BasePageViewFragment(), SingleStateFragment<HomeState, HomeSideEffect> {

  @Inject
  lateinit var navigator: HomeNavigator

  @Inject
  lateinit var transactionsNavigator: TransactionsNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private val viewModel: HomeViewModel by viewModels()
  private val navBarViewModel: NavBarViewModel by activityViewModels()

  private val pushNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

  private var isVip by mutableStateOf(false)

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
    askNotificationsPermission()
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
      Intercom.client().handlePushMessage()
    } else {
      requireActivity().finish()
    }
    viewModel.fetchPromotions()
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
      HomeScreenContent(padding = padding)
    }
  }

  @Composable
  internal fun HomeScreenContent(padding: PaddingValues) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      BalanceCard(
        newWallet = viewModel.newWallet.value,
        showBackup = viewModel.showBackup.value,
        balanceContent = { BalanceContent() },
        onClickTransfer = { viewModel.onTransferClick() },
        onClickBackup = { viewModel.onBackupClick() },
        onClickTopUp = { viewModel.onTopUpClick() },
        onClickMenuOptions = { navigator.navigateToManageBottomSheet() })
      PromotionsList()
      TransactionsCard(transactionsState = viewModel.uiState.collectAsState().value)
      GamesBundle(viewModel.gamesList.value) { viewModel.fetchGamesListing() }
      Spacer(modifier = Modifier.padding(40.dp))
    }
  }

  @Composable
  fun BalanceContent() =
    when (val state = viewModel.uiBalanceState.collectAsState().value) {
      is HomeViewModel.UiBalanceState.Success -> with(state.balance.creditsOnlyFiat) {
        BalanceValue(
          symbol + formatter.formatCurrency(amount, FIAT),
          currency
        ) { viewModel.onBalanceArrowClick(state.balance) }
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
              modifier = Modifier.padding(start = 8.dp, bottom = 16.dp, top = 26.dp),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = WalletColors.styleguide_dark_grey
            )
            Card(colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary)) {
              TransactionsList(transactionsState.transactions)
            }
          }
      }
      else -> {}
    }
  }

  @Composable
  fun PromotionsList() {
    if (!viewModel.activePromotions.isEmpty()) {
      Text(
        text = getString(R.string.intro_active_promotions_header),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_dark_grey,
        modifier = Modifier.padding(top = 27.dp, end = 13.dp, start = 24.dp)
      )
    }
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(viewModel.activePromotions) { promotion ->
        PromotionsCardComposable(cardItem = promotion)
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
                description = description ?: app,
                amount = amount,
                convertedAmount = amountSubtitle,
                subIcon = subIcon,
                onClick = { navigateToTransactionDetails(transaction) },
                textDecoration = textDecoration
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
          color = WalletColors.styleguide_pink,
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
    // TODO refreshing. setRefreshLayout(state.defaultWalletBalanceAsync,
    // state.transactionsModelAsync)
    setBalance(state.defaultWalletBalanceAsync)
    showVipBadge(state.showVipBadge)
    setPromotions(state.promotionsModelAsync)
    // TODO updateSupportIcon(state.unreadMessages)
    setBackup(state.hasBackup)
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

      is HomeSideEffect.NavigateToBalanceDetails -> navigator.navigateToBalanceBottomSheet(
        sideEffect.balance
      )
    }
  }

  private fun askNotificationsPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Android OS manages when to ask for permission. After android 11, the default behavior is
      // to only prompt for permission if needed, and if the user didn't deny it twice before. If
      // the user just dismissed it without denying, then it'll prompt again next time.
      pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  private fun checkRoot() {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit().putBoolean("should_show_root_warning", false).apply()
      val alertDialog =
        android.app.AlertDialog.Builder(context)
          .setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok) { _, _ -> }
          .show()
      alertDialog
        .getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
      alertDialog
        .getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        .setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_pink, null))
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
          }
        }

      else -> Unit
    }
  }

  private fun setBackup(hasBackup: Async<Boolean>) {
    when (hasBackup) {
      Async.Uninitialized,
      is Async.Loading -> {
        // TODO loading
      }
      is Async.Success -> viewModel.showBackup.value = !(hasBackup.value ?: false)
      else -> Unit
    }
  }

  private fun setPromotions(promotionsModel: Async<PromotionsModel>) {
    when (promotionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        // TODO loading
      }
      is Async.Success -> {
        viewModel.activePromotions.clear()
        promotionsModel.value!!.perks.forEach { promotion ->
          if (promotion is DefaultItem) {
            val cardItem =
              CardPromotionItem(
                promotion.appName,
                promotion.description,
                promotion.startDate,
                promotion.endDate,
                promotion.icon,
                promotion.actionUrl,
                promotion.packageName,
                promotion.gamificationStatus == GamificationStatus.VIP ||
                    promotion.gamificationStatus == GamificationStatus.VIP_MAX,
                hasFuturePromotion = false,
                hasVerticalList = false,
                action = {
                  openGame(
                    promotion.packageName ?: promotion.actionUrl,
                    promotion.actionUrl,
                    requireContext()
                  )
                })
            viewModel.activePromotions.add(cardItem)
          }
        }
      }
      else -> Unit
    }
  }

  private fun showVipBadge(shouldShow: Boolean) {
    isVip = shouldShow
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
