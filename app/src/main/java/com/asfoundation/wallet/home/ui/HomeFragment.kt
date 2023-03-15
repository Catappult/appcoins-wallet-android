package com.asfoundation.wallet.home.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentHomeBinding
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.ui.list.HomeController
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.home.ui.list.transactions.empty.EmptyTransactionsModel.Companion.CAROUSEL_GAMIFICATION
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RootUtil
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.convertDpToPx
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.Intercom
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BasePageViewFragment(),
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
  }

  override fun onPause() {
    super.onPause()
    viewModel.stopRefreshingData()
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
  private fun setToolbarBalance(balanceAsync: com.appcoins.wallet.ui.arch.Async<GlobalBalance>) {
    when (balanceAsync) {
      com.appcoins.wallet.ui.arch.Async.Uninitialized,
      is com.appcoins.wallet.ui.arch.Async.Loading -> {
        if (balanceAsync() == null) {
          views.toolbar.balance.visibility = View.INVISIBLE
          views.toolbar.balanceSkeleton.visibility = View.VISIBLE
          views.toolbar.balanceSkeleton.playAnimation()
        }
      }
      is com.appcoins.wallet.ui.arch.Async.Success -> {
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
    defaultWalletBalanceAsync: com.appcoins.wallet.ui.arch.Async<GlobalBalance>,
    transactionsModelAsync: com.appcoins.wallet.ui.arch.Async<TransactionsModel>
  ) {
    when (defaultWalletBalanceAsync) {
      is com.appcoins.wallet.ui.arch.Async.Fail,
      is com.appcoins.wallet.ui.arch.Async.Success -> {
        when (transactionsModelAsync) {
          is com.appcoins.wallet.ui.arch.Async.Fail,
          is com.appcoins.wallet.ui.arch.Async.Success -> views.refreshLayout.isRefreshing = false
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
}