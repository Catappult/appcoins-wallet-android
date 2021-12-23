package com.asfoundation.wallet.home.ui

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
import com.asfoundation.wallet.C
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.entity.ErrorEnvelope
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
import io.intercom.android.sdk.Intercom
import java.math.BigDecimal
import javax.inject.Inject

class HomeFragment : BasePageViewFragment(),
    SingleStateFragment<HomeState, HomeSideEffect> {

  @Inject
  lateinit var homeViewModelFactory: HomeViewModelFactory

  @Inject
  lateinit var navigator: HomeNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val viewModel: HomeViewModel by viewModels { homeViewModelFactory }

  private var _views: FragmentHomeBinding? = null
  private val views get() = _views!!

  private lateinit var homeController: HomeController
  private lateinit var tooltip: View
  private lateinit var popup: PopupWindow

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _views = FragmentHomeBinding.inflate(inflater, container, false)
    return views.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tooltip = layoutInflater.inflate(R.layout.fingerprint_tooltip, null)

    views.actionButtonVip.root.visibility = View.GONE
    views.actionButtonVip.root
        .setOnClickListener { viewModel.goToVipLink() }
    initializeLists()
    views.refreshLayout.setOnRefreshListener { viewModel.updateData() }
    views.refreshLayout.setProgressViewOffset(false, 0, 68.convertDpToPx(resources))
    views.actionButtonSupport.setOnClickListener { viewModel.showSupportScreen(false) }
    views.actionButtonSettings.setOnClickListener { viewModel.onSettingsClick() }
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
            false)
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
    views.balanceSkeleton.removeAllAnimatorListeners()
    views.balanceSkeleton.removeAllUpdateListeners()
    views.balanceSkeleton.removeAllLottieOnCompositionLoadedListener()
    super.onDestroyView()
    _views = null
  }

  private fun initializeLists() {
    homeController = HomeController()
    homeController.homeClickListener = { homeClick ->
      when (homeClick) {
        HomeListClick.BalanceClick -> viewModel.onBalanceClick()
        HomeListClick.ChangeCurrencyClick -> viewModel.onCurrencySelectorClick()
        HomeListClick.ReceiveButtonClick -> viewModel.onReceiveClick()
        HomeListClick.SendButtonClick -> viewModel.onSendClick()
        is HomeListClick.EmptyStateClick -> {
          if (homeClick.id == CAROUSEL_GAMIFICATION) navigator.navigateToPromotions()
        }
        is HomeListClick.NotificationClick -> {
          viewModel.onNotificationClick(homeClick.cardNotification,
              homeClick.cardNotificationAction)
        }
        is HomeListClick.TransactionClick -> {
          onTransactionClick(homeClick.transaction)
        }
        is HomeListClick.ChangedBalanceVisibility -> {
          if (homeClick.balanceVisible) {
            views.toolbarViewSwitcher.showFirstView()
          } else {
            views.toolbarViewSwitcher.showSecondView()
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

  private fun setToolbarBalance(balanceAsync: Async<GlobalBalance>) {
    when (balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (balanceAsync() == null) {
          views.balance.visibility = View.INVISIBLE
          views.balanceSkeleton.visibility = View.VISIBLE
          views.balanceSkeleton.playAnimation()
        }
      }
      is Async.Success -> {
        val overallBalanceFiat = balanceAsync().walletBalance.overallFiat
        val overallAmount = formatter.formatCurrency(overallBalanceFiat.amount, WalletCurrency.FIAT)
        if (overallBalanceFiat.amount > BigDecimal(
                "-1") && overallBalanceFiat.symbol.isNotEmpty()) {
          views.balance.visibility = View.VISIBLE
          views.balanceSkeleton.visibility = View.INVISIBLE
          views.balance.text = overallBalanceFiat.symbol + overallAmount
        }
      }
      else -> Unit
    }
  }

  private fun setRefreshLayout(defaultWalletBalanceAsync: Async<GlobalBalance>,
                               transactionsModelAsync: Async<TransactionsModel>) {
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
      is HomeSideEffect.NavigateToReceive -> navigator.navigateToReceive(sideEffect.wallet)
      HomeSideEffect.NavigateToSend -> navigator.navigateToSend()
      is HomeSideEffect.NavigateToSettings -> navigator.navigateToSettings(
          sideEffect.turnOnFingerprint)
      is HomeSideEffect.NavigateToShare -> navigator.handleShare(sideEffect.url)
      is HomeSideEffect.NavigateToDetails -> navigator.navigateToTransactionDetails(
          sideEffect.transaction, sideEffect.balanceCurrency)
      is HomeSideEffect.NavigateToBackup -> navigator.navigateToBackup(sideEffect.walletAddress)
      is HomeSideEffect.NavigateToIntent -> navigator.openIntent(sideEffect.intent)
      HomeSideEffect.ShowFingerprintTooltip -> setFingerprintTooltip()
      HomeSideEffect.NavigateToChangeCurrency -> navigator.navigateToCurrencySelector()
    }
  }

//  private fun setTransactionsModel(asyncTransactionsModel: Async<TransactionsModel>) {
//    when (asyncTransactionsModel) {
//      Async.Uninitialized,
//      is Async.Loading -> {
//        if (asyncTransactionsModel() == null) {
//          showLoading()
//        }
//      }
//      is Async.Fail -> {
//        onError(ErrorEnvelope(C.ErrorCode.UNKNOWN, null, asyncTransactionsModel.error.throwable))
//      }
//      is Async.Success -> {
//        //setTransactions(asyncTransactionsModel())
//      }
//    }
//  }

  private fun setDefaultWalletBalance(asyncDefaultWalletBalance: Async<GlobalBalance>) {
    when (asyncDefaultWalletBalance) {
      is Async.Fail -> {
        onError(ErrorEnvelope(C.ErrorCode.UNKNOWN, null, asyncDefaultWalletBalance.error.throwable))
      }
      else -> Unit
    }
  }

  private fun showVipBadge(shouldShow: Boolean) {
    views.actionButtonVip.root.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  private fun updateSupportIcon(hasMessages: Boolean) {
    if (hasMessages && !views.intercomAnimation.isAnimating) {
      views.intercomAnimation.playAnimation()
    } else {
      views.intercomAnimation.cancelAnimation()
      views.intercomAnimation.progress = 0F
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
          .setTextColor(ResourcesCompat.getColor(resources, R.color.text_button_color, null))
    }
  }

  private fun onTransactionClick(transaction: Transaction) {
    viewModel.onTransactionDetailsClick(transaction)
  }

  private fun onError(errorEnvelope: ErrorEnvelope) {
    if (errorEnvelope.code == C.ErrorCode.EMPTY_COLLECTION) {
//      views.systemView.showEmpty(showEmptyView(maxBonus))
    }
  }

  private fun setFingerprintTooltip() {
    popup = PopupWindow(tooltip)
    popup.height = ViewGroup.LayoutParams.WRAP_CONTENT
    popup.width = ViewGroup.LayoutParams.MATCH_PARENT
    val yOffset = 36.convertDpToPx(resources)
    views.fadedBackground.visibility = View.VISIBLE
    views.actionButtonSettings.post {
      popup.showAsDropDown(views.actionButtonSettings, 0, -yOffset)
    }
    setTooltipListeners()
  }

  private fun setTooltipListeners() {
    tooltip.findViewById<View>(R.id.tooltip_later_button)
        .setOnClickListener { dismissPopup() }
    tooltip.findViewById<View>(R.id.tooltip_turn_on_button)
        .setOnClickListener {
          dismissPopup()
          viewModel.onTurnFingerprintOnClick()
        }
  }

  private fun dismissPopup() {
    viewModel.onFingerprintDismissed()
    views.fadedBackground.visibility = View.GONE
    popup.dismiss()
  }

  companion object {
    fun newInstance() = HomeFragment()
  }

}