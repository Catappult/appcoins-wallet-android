package com.asfoundation.wallet.home

import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityTransactionsBinding
import com.asfoundation.wallet.C
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.ErrorEnvelope
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication
import com.asfoundation.wallet.ui.transactions.HeaderController
import com.asfoundation.wallet.ui.transactions.TransactionsController
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.RootUtil
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.convertDpToPx
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.widget.EmptyTransactionsView
import dagger.android.support.AndroidSupportInjection
import io.intercom.android.sdk.Intercom
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class HomeFragment : BasePageViewFragment(),
    SingleStateFragment<HomeState, HomeSideEffect> {

//  @Inject
//  lateinit var transactionsViewModelFactory: TransactionsViewModelFactory

  @Inject
  lateinit var homeViewModelFactory: HomeViewModelFactory

  @Inject
  lateinit var navigator: HomeNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  //  private val viewModelOld: TransactionsViewModel by viewModels { transactionsViewModelFactory }
  private val viewModel: HomeViewModel by viewModels { homeViewModelFactory }
  private val views by viewBinding(ActivityTransactionsBinding::bind)

  private lateinit var disposables: CompositeDisposable
  private lateinit var headerController: HeaderController
  private lateinit var transactionsController: TransactionsController
  private lateinit var tooltip: View
  private lateinit var popup: PopupWindow
  private var emptyTransactionsSubject: PublishSubject<String>? = null
  private var emptyView: EmptyTransactionsView? = null
  private var maxBonus = 0.0

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    AndroidSupportInjection.inject(this)
    return inflater.inflate(R.layout.activity_transactions, null)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    disposables = CompositeDisposable()

    tooltip = layoutInflater.inflate(R.layout.fingerprint_tooltip, null)
    views.emptyClickableView.visibility = View.VISIBLE
    views.balanceSkeleton.visibility = View.VISIBLE
    views.balanceSkeleton.playAnimation()

    emptyTransactionsSubject = PublishSubject.create()
    views.systemView.visibility = View.GONE
    views.actionButtonVip.root.visibility = View.GONE
    views.actionButtonVip.root
        .setOnClickListener { viewModel.goToVipLink() }
    initializeLists()
    views.refreshLayout.setOnRefreshListener { viewModel.updateData() }
    views.actionButtonSupport.setOnClickListener { viewModel.showSupportScreen(false) }
    views.actionButtonSettings.setOnClickListener { viewModel.onSettingsClick() }
    views.sendButton.setOnClickListener { viewModel.onSendClick() }
    views.receiveButton.setOnClickListener { viewModel.onReceiveClick() }
    views.emptyClickableView.setOnClickListener { viewModel.onBalanceClick() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onPause() {
    super.onPause()
    viewModel.stopRefreshingData()
    disposables.dispose()
  }


  override fun onResume() {
    super.onResume()
    val fromSupportNotification =
        requireActivity().intent.getBooleanExtra(
            SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
            false)
    if (!fromSupportNotification) {
      if (disposables.isDisposed) {
        disposables = CompositeDisposable()
      }
      viewModel.updateData()
      checkRoot()
      Intercom.client()
          .handlePushMessage()
    } else {
      requireActivity().finish()
    }
//    sendPageViewEvent()
  }

  override fun onDestroy() {
    views.balanceSkeleton.removeAllAnimatorListeners()
    views.balanceSkeleton.removeAllUpdateListeners()
    views.balanceSkeleton.removeAllLottieOnCompositionLoadedListener()
    emptyTransactionsSubject = null
    emptyView = null
    disposables.dispose()
    super.onDestroy()
  }

  private fun initializeLists() {
    headerController = HeaderController()
    views.headerRecyclerView.setController(headerController)
    headerController.appcoinsAppClickListener =
        { appcoinsApplication, applicationClickAction ->
          onApplicationClick(appcoinsApplication, applicationClickAction)
        }
    headerController.cardNotificationClickListener =
        { cardNotification, cardNotificationAction ->
          onNotificationClick(cardNotification, cardNotificationAction)
        }
    transactionsController = TransactionsController()
    transactionsController.transactionClickListener =
        { transaction: Transaction -> onTransactionClick(transaction) }
    views.transactionsRecyclerView.setController(transactionsController)
    views.systemView.attachRecyclerView(views.transactionsRecyclerView)
    views.systemView.attachSwipeRefreshLayout(views.refreshLayout)
  }


  override fun onStateChanged(state: HomeState) {
    setTransactionsModel(state.transactionsModelAsync)
    setDefaultWalletBalance(state.defaultWalletBalanceAsync)
    showVipBadge(state.showVipBadge)
    updateSupportIcon(state.unreadMessages)
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
    }
  }

  private fun setTransactionsModel(asyncTransactionsModel: Async<TransactionsModel>) {
    when (asyncTransactionsModel) {
      Async.Uninitialized,
      is Async.Loading -> {

      }
      is Async.Fail -> {

      }
      is Async.Success -> {
        setTransactions(asyncTransactionsModel())
      }
    }
  }

  private fun setTransactions(transactionsModel: TransactionsModel) {
    setDefaultWallet()
    setTransactionList(transactionsModel)
  }

  private fun setDefaultWallet() {
    views.transactionsRecyclerView.visibility = View.INVISIBLE
    views.systemView.visibility = View.VISIBLE
    views.systemView.showProgress(true)
    transactionsController = TransactionsController()
    transactionsController.transactionClickListener =
        { transaction: Transaction -> onTransactionClick(transaction) }
    views.transactionsRecyclerView.setController(transactionsController)
  }

  private fun setTransactionList(transactionsModel: TransactionsModel) {
    views.transactionsRecyclerView.visibility = View.VISIBLE
    views.systemView.visibility = View.GONE
    transactionsController.setData(transactionsModel,
        transactionsModel.transactionsWalletModel.wallet,
        transactionsModel.transactionsWalletModel.networkInfo)
    headerController.setData(transactionsModel)
    showList(transactionsModel)
  }

  private fun showList(transactionsModel: TransactionsModel) {
    views.systemView.showProgress(false)
    if (transactionsModel.transactions.isNotEmpty()) {
      showTransactionsScreen()
    } else {
      showNoTransactionsScreen(transactionsModel)
    }
    if (transactionsModel.notifications.isNotEmpty()
        || transactionsModel.applications.isNotEmpty()) {
      showNotificationOrApplications()
    } else {
      dontShowNotificationOrApplications()
    }
  }

  private fun showTransactionsScreen() {
    views.systemView.visibility = View.INVISIBLE
    views.transactionsRecyclerView.visibility = View.VISIBLE
  }

  private fun showNoTransactionsScreen(transactionsModel: TransactionsModel) {
    views.systemView.visibility = View.VISIBLE
    views.transactionsRecyclerView.visibility = View.INVISIBLE
    maxBonus = transactionsModel.maxBonus
    views.systemView.showEmpty(getEmptyView(maxBonus))
  }

  private fun showNotificationOrApplications() {
    views.headerRecyclerView.visibility = View.VISIBLE
    views.spacer.visibility = View.VISIBLE
    views.container.loadLayoutDescription(R.xml.activity_transactions_scene)
  }

  private fun dontShowNotificationOrApplications() {
    if (views.spacer.visibility === View.VISIBLE) {
      views.headerRecyclerView.visibility = View.GONE
      views.spacer.visibility = View.GONE
    }
    views.container.loadLayoutDescription(R.xml.activity_transactions_scene_short)
  }

  private fun setDefaultWalletBalance(asyncDefaultWalletBalance: Async<GlobalBalance>) {
    when (asyncDefaultWalletBalance) {
      Async.Uninitialized,
      is Async.Loading -> {

      }
      is Async.Fail -> {

      }
      is Async.Success -> {
        setWalletBalance(asyncDefaultWalletBalance())
      }
    }
  }

  private fun setWalletBalance(globalBalance: GlobalBalance) {
    if (globalBalance.fiatValue.isNotEmpty() && globalBalance.fiatSymbol.isNotEmpty()) {
      views.balanceSkeleton.visibility = View.GONE
      views.balance.text = globalBalance.fiatSymbol + globalBalance.fiatValue
      setSubtitle(globalBalance)
    }
  }

  private fun setSubtitle(globalBalance: GlobalBalance) {
    val subtitle = buildCurrencyString(globalBalance.appcoinsBalance, globalBalance.creditsBalance,
        globalBalance.etherBalance, globalBalance.showAppcoins,
        globalBalance.showCredits, globalBalance.showEthereum)
    views.balanceSubtitle.text = Html.fromHtml(subtitle)
  }

  private fun buildCurrencyString(appcoinsBalance: Balance, creditsBalance: Balance,
                                  ethereumBalance: Balance, showAppcoins: Boolean,
                                  showCredits: Boolean, showEthereum: Boolean): String {
    val stringBuilder = StringBuilder()
    val bullet = "\u00A0\u00A0\u00A0\u2022\u00A0\u00A0\u00A0"
    if (showCredits) {
      val creditsString = (formatter.formatCurrency(creditsBalance.value, WalletCurrency.CREDITS)
          + " "
          + WalletCurrency.CREDITS.symbol)
      stringBuilder.append(creditsString)
          .append(bullet)
    }
    if (showAppcoins) {
      val appcString = (formatter.formatCurrency(appcoinsBalance.value, WalletCurrency.APPCOINS)
          + " "
          + WalletCurrency.APPCOINS.symbol)
      stringBuilder.append(appcString)
          .append(bullet)
    }
    if (showEthereum) {
      val ethString = (formatter.formatCurrency(ethereumBalance.value, WalletCurrency.ETHEREUM)
          + " "
          + WalletCurrency.ETHEREUM.symbol)
      stringBuilder.append(ethString)
          .append(bullet)
    }
    var subtitle = stringBuilder.toString()
    if (stringBuilder.length > bullet.length) {
      subtitle = stringBuilder.substring(0, stringBuilder.length - bullet.length)
    }
    return subtitle.replace(bullet, "<font color='#ffffff'>$bullet</font>")
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

  //TODO colocar no viewmodel
  private fun sendPageViewEvent() {
    //pageViewAnalytics.sendPageViewEvent(javaClass.simpleName)
  }

  private fun onApplicationClick(appcoinsApplication: AppcoinsApplication,
                                 applicationClickAction: ApplicationClickAction) {
    viewModel.onAppClick(appcoinsApplication, applicationClickAction)
  }

  private fun onTransactionClick(transaction: Transaction) {
    viewModel.onTransactionDetailsClick(transaction)
  }

  private fun onNotificationClick(cardNotification: CardNotification,
                                  cardNotificationAction: CardNotificationAction) {
    viewModel.onNotificationClick(cardNotification, cardNotificationAction)
  }

  private fun onError(errorEnvelope: ErrorEnvelope) {
    if (errorEnvelope.code == C.ErrorCode.EMPTY_COLLECTION) {
      views.systemView.showEmpty(getEmptyView(maxBonus))
    }
  }

  private fun getEmptyView(maxBonus: Double): EmptyTransactionsView {
    var emptyView = this.emptyView
    if (emptyView == null) {
      emptyView =
          EmptyTransactionsView(requireContext(), maxBonus.toString(), emptyTransactionsSubject,
              this,
              disposables)
    }
    return emptyView
  }

  val emptyTransactionsScreenClick: Observable<String>?
    get() = emptyTransactionsSubject

  private fun setFingerprintTooltip() {
    popup = PopupWindow(tooltip)
    popup.height = ViewGroup.LayoutParams.WRAP_CONTENT
    popup.width = ViewGroup.LayoutParams.MATCH_PARENT
    val yOffset = 36.convertDpToPx(resources)
    views.fadedBackground.visibility = View.VISIBLE
    popup.showAsDropDown(views.actionButtonSettings, 0, -yOffset)
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