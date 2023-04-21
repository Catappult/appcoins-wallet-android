package com.asfoundation.wallet.wallet.home

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils
import com.appcoins.wallet.core.utils.android_common.RootUtil
import com.appcoins.wallet.core.utils.android_common.WalletCurrency.FIAT
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.BalanceCard
import com.appcoins.wallet.ui.widgets.NftCard
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionCard
import com.asf.wallet.R
import com.asfoundation.wallet.C.ETHER_DECIMALS
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.Transaction.*
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.transactions.TransactionDetails
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import java.math.BigDecimal
import javax.inject.Inject

// Before moving this screen into the :home module, all home dependencies need to be independent
// from the :app module.
// TODO rename class after completed
@AndroidEntryPoint
class HomeFragment: BasePageViewFragment(), SingleStateFragment<HomeState, HomeSideEffect> {

  @Inject
  lateinit var navigator: HomeNavigator
  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private val viewModel: HomeViewModel by viewModels()

  private val pushNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

  private var isVip by mutableStateOf(false)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        HomeScreen()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // TODO transactions list
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    askNotificationsPermission()
  }

  override fun onResume() {
    super.onResume()
    val fromSupportNotification =
      requireActivity().intent.getBooleanExtra(
        SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
        false
      )
    if (!fromSupportNotification) {
      viewModel.updateData()
      checkRoot()
      Intercom.client()
        .handlePushMessage()
    } else {
      requireActivity().finish()
    }
  }

  override fun onPause() {
    super.onPause()
    viewModel.stopRefreshingData()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun HomeScreen(
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
      HomeScreenContent(
        padding = padding
      )
    }
  }

  @Composable
  internal fun HomeScreenContent(
    padding: PaddingValues
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
        with(viewModel.balance.value) {
          BalanceCard(
            newWallet = viewModel.newWallet.value,
            showBackup = viewModel.state.showBackup,
            balance = symbol + formatter.formatCurrency(amount, FIAT),
            currencyCode = currency,
            onClickCurrencies = { viewModel.onCurrencySelectorClick() },
            onClickTransfer = { viewModel.onTransferClick() },
            onClickBackup = { viewModel.onBackupClick() },
            onClickTopUp = { viewModel.onTopUpClick() },
            onClickMenuOptions = {
              Toast.makeText(context, "In progress", Toast.LENGTH_SHORT).show()
            } // TODO create bottom sheet
          )
        }
      //TODO replace with home composables
      TransactionsList(transactionsGrouped = viewModel.transactionsGrouped.value)
      NftCard(
        onClick = { navigateToNft() }
      )
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun TransactionsList(
    transactionsGrouped: Map<String, List<Transaction>>
  ) {
    Column(
      modifier = Modifier
        .heightIn(0.dp, 400.dp)
        .padding(horizontal = 16.dp)
    ) {
      Text(
        text = stringResource(R.string.intro_transactions_header),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = WalletColors.styleguide_dark_grey
      )
      Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary)
      )
      {
        Box(
          contentAlignment = Alignment.TopEnd
        ) {
          LazyColumn(userScrollEnabled = false, modifier = Modifier.padding(vertical = 8.dp)) {
            transactionsGrouped.forEach { (date, transactionsForDate) ->
              stickyHeader {
                Text(
                  text = date,
                  color = WalletColors.styleguide_medium_grey,
                  modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp),
                  style = MaterialTheme.typography.bodySmall
                )
              }
              items(transactionsForDate) { transaction ->
                TransactionCard(
                  icon = painterResource(id = transaction.cardInfoByType().icon),
                  title = stringResource(id = transaction.cardInfoByType().title),
                  description = transaction.cardInfoByType().description,
                  amount = transaction.cardInfoByType().amount,
                  currency = transaction.cardInfoByType().currency,
                  subIcon = transaction.cardInfoByType().subIcon
                )
              }
            }
          }
          TextButton(onClick = {}) {
            Text(
              text = stringResource(R.string.see_all_button),
              color = WalletColors.styleguide_pink,
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }

  @Preview(showBackground = true)
  @Composable
  fun HomeScreenPreview() {
    HomeScreen()
  }

  @Preview
  @Composable
  fun PreviewTransactionList() {
    TransactionsList(
      mapOf(
        "Apr, 20 2023" to listOf<Transaction>(
          Transaction(
            "",
            BONUS,
            null,
            Method.APPC,
            "Title",
            "Subtitle",
            Perk.PACKAGE_PERK,
            "",
            123456,
            12345,
            TransactionStatus.SUCCESS,
            "APPC",
            "",
            "",
            TransactionDetails(
              "",
              TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, ""),
              ""
            ),
            "APP-C",
            listOf(),
            listOf(),
            "12.0",
            "€",
            ""
          ),
          Transaction(
            "",
            BONUS,
            null,
            Method.APPC,
            "Title",
            "Subtitle",
            Perk.PACKAGE_PERK,
            "",
            123456,
            12345,
            TransactionStatus.SUCCESS,
            "APPC",
            "",
            "",
            TransactionDetails(
              "",
              TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, ""),
              ""
            ),
            "APP-C",
            listOf(),
            listOf(),
            "12.0",
            "APP_CC",
            ""
          )
        ),
        "Apr, 19 2023" to listOf<Transaction>(
          Transaction(
            "",
            BONUS,
            null,
            Method.APPC,
            "Title",
            "Subtitle",
            Perk.PACKAGE_PERK,
            "",
            123456,
            12345,
            TransactionStatus.SUCCESS,
            "APPC",
            "",
            "",
            TransactionDetails(
              "",
              TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, ""),
              ""
            ),
            "APP-C",
            listOf(),
            listOf(),
            "12.0",
            "APP_CC",
            ""
          ),
          Transaction(
            "",
            BONUS,
            null,
            Method.APPC,
            "Title",
            "Subtitle",
            Perk.PACKAGE_PERK,
            "",
            123456,
            12345,
            TransactionStatus.SUCCESS,
            "APPC",
            "",
            "",
            TransactionDetails(
              "",
              TransactionDetails.Icon(TransactionDetails.Icon.Type.FILE, ""),
              ""
            ),
            "APP-C",
            listOf(),
            listOf(),
            "12.0",
            "APP_CC",
            ""
          )
        )
      )
    )
  }

  override fun onStateChanged(state: HomeState) {
    // TODO set transaction list elements. setData(state.transactionsModelAsync, state.defaultWalletBalanceAsync)
    // TODO refreshing. setRefreshLayout(state.defaultWalletBalanceAsync, state.transactionsModelAsync)
    setBalance(state.defaultWalletBalanceAsync)
    showVipBadge(state.showVipBadge)
    setTransactions(state.transactionsModelAsync)
    // TODO updateSupportIcon(state.unreadMessages)
  }

  override fun onSideEffect(sideEffect: HomeSideEffect) {
    when (sideEffect) {
      is HomeSideEffect.NavigateToBrowser -> navigator.navigateToBrowser(sideEffect.uri)
      is HomeSideEffect.NavigateToRateUs -> navigator.navigateToRateUs(sideEffect.shouldNavigate)
      HomeSideEffect.NavigateToMyWallets -> navigator.navigateToMyWallets()
      is HomeSideEffect.NavigateToSettings -> navigator.navigateToSettings(
        sideEffect.turnOnFingerprint
      )
      is HomeSideEffect.NavigateToShare -> navigator.handleShare(sideEffect.url)
      is HomeSideEffect.NavigateToDetails -> navigator.navigateToTransactionDetails(
        sideEffect.transaction, sideEffect.balanceCurrency
      )
      is HomeSideEffect.NavigateToBackup -> navigator.navigateToBackup(
        sideEffect.walletAddress
      )
      is HomeSideEffect.NavigateToIntent -> navigator.openIntent(sideEffect.intent)
      is HomeSideEffect.ShowBackupTrigger -> navigator.navigateToBackupTrigger(
        sideEffect.walletAddress,
        sideEffect.triggerSource
      )
      HomeSideEffect.NavigateToChangeCurrency -> navigator.navigateToCurrencySelector()
      HomeSideEffect.NavigateToTopUp -> navigator.navigateToTopUp()
      HomeSideEffect.NavigateToTransfer -> navigator.navigateToTransfer()
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
      pref.edit()
        .putBoolean("should_show_root_warning", false)
        .apply()
      val alertDialog = android.app.AlertDialog.Builder(context)
        .setTitle(R.string.root_title)
        .setMessage(R.string.root_body)
        .setNegativeButton(R.string.ok) { dialog, which -> }
        .show()
      alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
      alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        .setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_pink, null))
    }
  }

  private fun setBalance(balanceAsync: Async<GlobalBalance>) {
    when (balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO loading
      }
      is Async.Success ->
        with(balanceAsync().walletBalance.creditsOnlyFiat) {
          if (amount >= BigDecimal.ZERO && symbol.isNotEmpty()) viewModel.balance.value = this
        }
      else -> Unit
    }
  }

  private fun setTransactions(transactionsModel: Async<TransactionsModel>) {
    when (transactionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO loading
      }

      is Async.Success -> {
        viewModel.newWallet.value = transactionsModel().transactions.isEmpty()
        viewModel.transactionsGrouped.value =
          transactionsModel().transactions.groupBy { DateFormatterUtils.getDate(it.timeStamp) }
        println(transactionsModel().transactions.distinctBy { it.transactionId }.size.toString() + " " + viewModel.transactionsGrouped.value)
      }

      else -> Unit
    }
  }

  private fun showVipBadge(shouldShow: Boolean) {
    isVip = shouldShow
  }

  private fun navigateToNft() {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    val navController = navHostFragment.navController

    navigator.navigateToNfts(navController)
  }

  private fun Transaction.cardInfoByType() = when (this.type) {
    STANDARD -> TODO()
    IAP -> TODO()
    ADS -> TODO()
    IAP_OFFCHAIN -> TODO()
    ADS_OFFCHAIN -> TODO()
    BONUS -> TransactionCardInfo(
      icon = R.drawable.ic_transaction_reward,
      title = R.string.transaction_type_bonus,
      amount = formatter.getScaledValue(value, ETHER_DECIMALS.toLong(), "", false),
      currency = currency
    )

    TOP_UP -> TransactionCardInfo(
      icon = R.drawable.ic_transaction_topup,
      title = R.string.topup_title,
      amount = formatter.getScaledValue(paidAmount!!, 0, "", false),
      currency = "${
        formatter.getScaledValue(
          value,
          ETHER_DECIMALS.toLong(),
          currency ?: "",
          false
        )
      } $currency"
    )

    TRANSFER_OFF_CHAIN -> TODO()
    BONUS_REVERT -> TODO()
    TOP_UP_REVERT -> TODO()
    IAP_REVERT -> TODO()
    SUBS_OFFCHAIN -> TODO()
    ESKILLS_REWARD -> TODO()
    ESKILLS -> TODO()
    TRANSFER -> TODO()
    ETHER_TRANSFER -> TODO()
  }


}
