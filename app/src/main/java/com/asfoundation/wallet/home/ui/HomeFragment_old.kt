package com.asfoundation.wallet.home.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RootUtil
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.ui.common.convertDpToPx
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentHomeBinding
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.ui.list.HomeController
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.home.ui.list.transactions.empty.EmptyTransactionsModel.Companion.CAROUSEL_GAMIFICATION
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.wallet.home.HomeNavigator
import com.asfoundation.wallet.wallet.home.HomeSideEffect
import com.asfoundation.wallet.wallet.home.HomeState
import com.asfoundation.wallet.wallet.home.HomeViewModel
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import java.math.BigDecimal
import javax.inject.Inject

// TODO to be removed
@AndroidEntryPoint
class HomeFragment_old : BasePageViewFragment(),
  SingleStateFragment<HomeState, HomeSideEffect> {

  @Inject
  lateinit var navigator: HomeNavigator
  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private val viewModel: HomeViewModel by viewModels()

  private var _views: FragmentHomeBinding? = null
  private val views get() = _views!!

  private lateinit var homeController: HomeController
  private lateinit var tooltip: View
  private lateinit var popup: PopupWindow

  private val pushNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _views = FragmentHomeBinding.inflate(inflater, container, false)
    return views.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tooltip = layoutInflater.inflate(R.layout.fingerprint_tooltip, null)

    views.toolbar.actionButtonVip.root.visibility = View.GONE
    views.toolbar.actionButtonVip.root
      .setOnClickListener { viewModel.goToVipLink() }
    initializeLists()
    views.refreshLayout.setOnRefreshListener { viewModel.updateData() }
    views.refreshLayout.setProgressViewOffset(false, 0, 68.convertDpToPx(resources))
    views.toolbar.actionButtonSupport.setOnClickListener { viewModel.showSupportScreen(false) }
    views.toolbar.actionButtonSettings.setOnClickListener { viewModel.onSettingsClick() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    askNotificationsPermission()
  }

  override fun onPause() {
    super.onPause()
    viewModel.stopRefreshingData()
  }


  override fun onResume() {
    super.onResume()
    val fromSupportNotification =
      requireActivity().intent.getBooleanExtra(
        SUPPORT_NOTIFICATION_CLICK,
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

  override fun onDestroyView() {
    views.toolbar.balanceSkeleton.removeAllAnimatorListeners()
    views.toolbar.balanceSkeleton.removeAllUpdateListeners()
    views.toolbar.balanceSkeleton.removeAllLottieOnCompositionLoadedListener()
    super.onDestroyView()
    _views = null
  }

  private fun initializeLists() {
    homeController = HomeController()
    homeController.homeClickListener = { homeClick ->
      when (homeClick) {
        HomeListClick.BalanceClick -> viewModel.onBalanceClick()
        HomeListClick.ChangeCurrencyClick -> viewModel.onCurrencySelectorClick()
        is HomeListClick.EmptyStateClick -> {
          if (homeClick.id == CAROUSEL_GAMIFICATION) navigator.navigateToPromotions()
        }
        is HomeListClick.NotificationClick -> {
          viewModel.onNotificationClick(
            homeClick.cardNotification,
            homeClick.cardNotificationAction
          )
        }
        is HomeListClick.TransactionClick -> {
          onTransactionClick(homeClick.transaction)
        }
        is HomeListClick.ChangedBalanceVisibility -> {
          if (homeClick.balanceVisible) {
            views.toolbar.toolbarViewSwitcher.showFirstView()
          } else {
            views.toolbar.toolbarViewSwitcher.showSecondView()
          }
        }
      }
    }
    views.homeRecyclerView.setController(homeController)
  }


  override fun onStateChanged(state: HomeState) {
    homeController.setData(state.transactionsModelAsync, state.defaultWalletBalanceAsync)
    setRefreshLayout(state.defaultWalletBalanceAsync, state.transactionsModelAsync)
    setToolbarBalance(state.defaultWalletBalanceAsync)
    showVipBadge(state.showVipBadge)
    updateSupportIcon(state.unreadMessages)
  }

  @SuppressLint("SetTextI18n")
  private fun setToolbarBalance(balanceAsync: Async<GlobalBalance>) {
    when (balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (balanceAsync() == null) {
          views.toolbar.balance.visibility = View.INVISIBLE
          views.toolbar.balanceSkeleton.visibility = View.VISIBLE
          views.toolbar.balanceSkeleton.playAnimation()
        }
      }
      is Async.Success -> {
        val creditsBalanceFiat = balanceAsync().walletBalance.creditsOnlyFiat
        val creditsFiatAmount =
          formatter.formatCurrency(creditsBalanceFiat.amount, WalletCurrency.FIAT)
        if (creditsBalanceFiat.amount > BigDecimal(
            "-1"
          ) && creditsBalanceFiat.symbol.isNotEmpty()
        ) {
          views.toolbar.balance.visibility = View.VISIBLE
          views.toolbar.balanceSkeleton.visibility = View.INVISIBLE
          views.toolbar.balance.text = creditsBalanceFiat.symbol + creditsFiatAmount
        }
      }
      else -> Unit
    }
  }

  private fun setRefreshLayout(
    defaultWalletBalanceAsync: Async<GlobalBalance>,
    transactionsModelAsync: Async<TransactionsModel>
  ) {
    when (defaultWalletBalanceAsync) {
      is Async.Fail,
      is Async.Success -> {
        when (transactionsModelAsync) {
          is Async.Fail,
          is Async.Success -> views.refreshLayout.isRefreshing = false
          else -> Unit
        }
      }
      else -> Unit
    }
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

  private fun showVipBadge(shouldShow: Boolean) {
    views.toolbar.actionButtonVip.root.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  private fun updateSupportIcon(hasMessages: Boolean) {
    if (hasMessages && !views.toolbar.intercomAnimation.isAnimating) {
      views.toolbar.intercomAnimation.playAnimation()
    } else {
      views.toolbar.intercomAnimation.cancelAnimation()
      views.toolbar.intercomAnimation.progress = 0F
    }
  }

  private fun checkRoot() {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit()
        .putBoolean("should_show_root_warning", false)
        .apply()
      val alertDialog = AlertDialog.Builder(context)
        .setTitle(R.string.root_title)
        .setMessage(R.string.root_body)
        .setNegativeButton(R.string.ok) { dialog, which -> }
        .show()
      alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
      alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        .setTextColor(ResourcesCompat.getColor(resources, R.color.styleguide_pink, null))
    }
  }

  private fun onTransactionClick(transaction: Transaction) {
    viewModel.onTransactionDetailsClick(transaction)
  }

  private fun askNotificationsPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Android OS manages when to ask for permission. After android 11, the default behavior is
      // to only prompt for permission if needed, and if the user didn't deny it twice before. If
      // the user just dismissed it without denying, then it'll prompt again next time.
      pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
  }
}
