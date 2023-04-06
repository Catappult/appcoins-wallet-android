package com.asfoundation.wallet.wallet_v3.home

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RootUtil
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import javax.inject.Inject

// Before moving this screen into the :home module, all home dependencies need to be independent
// from the :app module.
@AndroidEntryPoint
class HomeFragment_new: BasePageViewFragment(), SingleStateFragment<HomeState, HomeSideEffect> {

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

  @Composable
  fun HomeScreen(
    modifier: Modifier = Modifier,
  ) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
      scaffoldState = scaffoldState,
      topBar = {
        Surface(elevation = 4.dp) {
          TopBar(
            isMainBar = true,
            isVip = isVip,
            onClickNotifications = { Log.d("TestHomeFragment", "Notifications")},
            onClickSettings = { viewModel.onSettingsClick() },
            onClickSupport = { viewModel.showSupportScreen(false) },
          )
        }
      },
      backgroundColor = WalletColors.styleguide_blue,
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
      //TODO replace with home composables
      DummyCard()
      DummyCard()
      DummyCard()
      DummyCard()
      DummyCard()
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
      backgroundColor =  WalletColors.styleguide_blue_secondary,
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Home Screen",
          style = MaterialTheme.typography.h5,
          color =  WalletColors.styleguide_white
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
    // TODO set transaction list elements. setData(state.transactionsModelAsync, state.defaultWalletBalanceAsync)
    // TODO refreshing. setRefreshLayout(state.defaultWalletBalanceAsync, state.transactionsModelAsync)
    // TODO setBalance(state.defaultWalletBalanceAsync)
    showVipBadge(state.showVipBadge)
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

  private fun showVipBadge(shouldShow: Boolean) {
    isVip = shouldShow
  }

}
