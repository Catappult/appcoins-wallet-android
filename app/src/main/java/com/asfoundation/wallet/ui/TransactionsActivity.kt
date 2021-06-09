package com.asfoundation.wallet.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.app.ShareCompat
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityTransactionsBinding
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.ErrorEnvelope
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
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
import com.asfoundation.wallet.viewmodel.TransactionsViewModel
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import com.asfoundation.wallet.widget.EmptyTransactionsView
import dagger.android.AndroidInjection
import io.intercom.android.sdk.Intercom
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class TransactionsActivity : BaseActivity(), View.OnClickListener {
//  @Inject
//  lateinit var transactionsViewModelFactory: TransactionsViewModelFactory

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private var viewModel: TransactionsViewModel? = null
  private var disposables: CompositeDisposable? = null
  private var views: ActivityTransactionsBinding? = null
  private var headerController: HeaderController? = null
  private var transactionsController: TransactionsController? = null
  private var emptyTransactionsSubject: PublishSubject<String>? = null
  private var badge: View? = null
  private var tooltip: View? = null
  private var popup: PopupWindow? = null
  private var emptyView: EmptyTransactionsView? = null
  private var maxBonus = 0.0
  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    views = ActivityTransactionsBinding.inflate(layoutInflater)
    setContentView(views!!.root)
    disposables = CompositeDisposable()
    tooltip = layoutInflater.inflate(R.layout.fingerprint_tooltip, null)
    views!!.emptyClickableView.visibility = View.VISIBLE
    views!!.balanceSkeleton.visibility = View.VISIBLE
    views!!.balanceSkeleton.playAnimation()
    //initBottomNavigation()
    disableDisplayHomeAsUp()
    //prepareNotificationIcon()
    emptyTransactionsSubject = PublishSubject.create()
    views!!.systemView.visibility = View.GONE
    views!!.actionButtonVip.root.visibility = View.GONE
    views!!.actionButtonVip.root
        .setOnClickListener { viewModel!!.goToVipLink(this) }
    initializeLists()
    initializeViewModel()
//    views!!.transactionsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//        if (newState == RecyclerView.SCROLL_STATE_IDLE && !views!!.topUpBtn.isExtended
//            && recyclerView.computeVerticalScrollOffset() == 0) {
//          views!!.topUpBtn.extend()
//        }
//        super.onScrollStateChanged(recyclerView, newState)
//      }
//
//      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//        if (dy != 0 && recyclerView.computeVerticalScrollOffset() > 0 && views!!.topUpBtn.isExtended) {
//          views!!.topUpBtn.shrink()
//        }
//        super.onScrolled(recyclerView, dx, dy)
//      }
//    })
//    views!!.topUpBtn.extend()
    views!!.refreshLayout.setOnRefreshListener { viewModel!!.updateData() }
    views!!.actionButtonSupport.setOnClickListener { viewModel!!.showSupportScreen(false) }
    views!!.actionButtonSettings.setOnClickListener { viewModel!!.showSettings(this) }
    views!!.sendButton.setOnClickListener { viewModel!!.showSend(this) }
    views!!.receiveButton.setOnClickListener { viewModel!!.showMyAddress(this) }
    if (savedInstanceState == null) {
      val fromAppOpening = intent.getBooleanExtra(FROM_APP_OPENING_FLAG, false)
      if (fromAppOpening) viewModel!!.increaseTimesInHome()
      val supportNotificationClick = intent.getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false)
      if (supportNotificationClick) {
        overridePendingTransition(0, 0)
        viewModel!!.showSupportScreen(true)
      }
    }
  }

  private fun initializeLists() {
    headerController = HeaderController()
    views!!.headerRecyclerView.setController(headerController!!)
    headerController!!.appcoinsAppClickListener =
        { appcoinsApplication: AppcoinsApplication, applicationClickAction: ApplicationClickAction ->
          onApplicationClick(appcoinsApplication, applicationClickAction)
        }
    headerController!!.cardNotificationClickListener =
        { cardNotification: CardNotification, cardNotificationAction: CardNotificationAction ->
          onNotificationClick(cardNotification, cardNotificationAction)
        }
    transactionsController = TransactionsController()
    transactionsController!!.transactionClickListener =
        { transaction: Transaction -> onTransactionClick(transaction) }
    views!!.transactionsRecyclerView.setController(transactionsController!!)
    views!!.systemView.attachRecyclerView(views!!.transactionsRecyclerView)
    views!!.systemView.attachSwipeRefreshLayout(views!!.refreshLayout)
  }

  private fun initializeViewModel() {
//    viewModel =
//        ViewModelProviders.of(this, transactionsViewModelFactory)[TransactionsViewModel::class.java]
    viewModel!!.progress()
        .observe(this, views!!.systemView::showProgress)
    viewModel!!.error()
        .observe(this, { errorEnvelope: ErrorEnvelope ->
//          onError(errorEnvelope)
        })
    viewModel!!.defaultWalletBalance
        .observe(this, { globalBalance: GlobalBalance ->
          onBalanceChanged(globalBalance)
        })
    viewModel!!.defaultWalletModel()
        .observe(this,
            { walletModel: TransactionsWalletModel ->
              onDefaultWallet(walletModel)
            })
    viewModel!!.transactionsModel()
        .observe(this,
            { result: Pair<TransactionsModel, TransactionsWalletModel> ->
              onTransactionsModel(result)
            })
    viewModel!!.shouldShowPromotionsNotification()
        .observe(this, { shouldShow: Boolean ->
//          onPromotionsNotification(shouldShow)
        })
    viewModel!!.unreadMessages
        .observe(this, { hasMessages: Boolean ->
          updateSupportIcon(hasMessages)
        })
    viewModel!!.shareApp()
        .observe(this, { url: String? ->
          shareApp(url)
        })
    viewModel!!.shouldShowRateUsDialog()
        .observe(this, { shouldNavigate: Boolean ->
          navigateToRateUs(shouldNavigate)
        })
    viewModel!!.shouldShowFingerprintTooltip()
        .observe(this, { shouldShow: Boolean ->
          showFingerprintTooltip(shouldShow)
        })
    viewModel!!.shouldShowVipBadge()
        .observe(this, { shouldShow: Boolean ->
          showVipBadge(shouldShow)
        })
  }

  private fun navigateToRateUs(shouldNavigate: Boolean) {
    if (shouldNavigate) {
      val intent = RatingActivity.newIntent(this)
      this.startActivityForResult(intent, 0)
    }
  }


  // TODO: ddd
  //@Override public boolean onOptionsItemSelected(MenuItem item) {
  //  if (item.getItemId() == R.id.action_settings) {
  //    viewModel.showSettings(this);
  //  }
  //  if (item.getItemId() == R.id.action_vip_badge) {
  //    viewModel.goToVipLink(this);
  //  }
  //  return super.onOptionsItemSelected(item);
  //}
  override fun onBackPressed() {
    if (popup != null && popup!!.isShowing) {
      dismissPopup()
    } else {
      super.onBackPressed()
    }
  }

  private fun shareApp(url: String?) {
    if (url != null) {
      viewModel!!.clearShareApp()
      ShareCompat.IntentBuilder.from(this)
          .setText(url)
          .setType("text/plain")
          .setChooserTitle(R.string.share_via)
          .startChooser()
    }
  }

//  private fun prepareNotificationIcon() {
//    val bottomNavigationMenuView = (findViewById<View>(
//        R.id.bottom_navigation) as BottomNavigationView).getChildAt(0) as BottomNavigationMenuView
//    val promotionsIcon =
//        bottomNavigationMenuView.getChildAt(BottomNavigationItem.PROMOTIONS.position)
//    val itemView = promotionsIcon as BottomNavigationItemView
//    badge = LayoutInflater.from(this)
//        .inflate(R.layout.notification_badge, bottomNavigationMenuView, false)
//    badge!!.visibility = View.INVISIBLE
//    itemView.addView(badge)
//  }

//  private fun onPromotionsNotification(shouldShow: Boolean) {
//    if (shouldShow) {
//      badge!!.visibility = View.VISIBLE
//    } else {
//      badge!!.visibility = View.INVISIBLE
//    }
//  }

  private fun updateSupportIcon(hasMessages: Boolean) {
    if (hasMessages && !views!!.intercomAnimation.isAnimating) {
      views!!.intercomAnimation.playAnimation()
    } else {
      views!!.intercomAnimation.cancelAnimation()
      views!!.intercomAnimation.progress = 0F
    }
  }

  private fun onApplicationClick(appcoinsApplication: AppcoinsApplication,
                                 applicationClickAction: ApplicationClickAction) {
    viewModel!!.onAppClick(appcoinsApplication, applicationClickAction, this)
  }

  private fun onTransactionClick(transaction: Transaction) {
    viewModel!!.showDetails(this, transaction)
  }

  private fun onNotificationClick(cardNotification: CardNotification,
                                  cardNotificationAction: CardNotificationAction) {
    viewModel!!.onNotificationClick(cardNotification, cardNotificationAction, this)
  }

  override fun onPause() {
    super.onPause()
    viewModel!!.stopRefreshingData()
    disposables!!.dispose()
  }

  override fun onResume() {
    super.onResume()
    val supportNotificationClick = intent.getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false)
    if (!supportNotificationClick) {
      if (disposables!!.isDisposed) {
        disposables = CompositeDisposable()
      }
      viewModel!!.updateData()
      checkRoot()
      Intercom.client()
          .handlePushMessage()
    } else {
      finish()
    }
    sendPageViewEvent()
  }

  override fun onClick(view: View) {
    val id = view.id
    if (view.id == R.id.try_again) {
      viewModel!!.updateData()
    } else if (id == R.id.empty_clickable_view) {
      viewModel!!.showTokens(this)
    }
  }

  private fun onTransactionsModel(result: Pair<TransactionsModel, TransactionsWalletModel>) {
    views!!.transactionsRecyclerView.visibility = View.VISIBLE
    views!!.systemView.visibility = View.GONE
    transactionsController!!.setData(result.first, result.second.wallet,
        result.second.networkInfo)
    headerController!!.setData(result.first)
    showList(result.first)
  }

  private fun showList(transactionsModel: TransactionsModel) {
    views!!.systemView.showProgress(false)
    if (transactionsModel.transactions.isNotEmpty()) {
      views!!.systemView.visibility = View.INVISIBLE
      views!!.transactionsRecyclerView.visibility = View.VISIBLE
    } else {
      views!!.systemView.visibility = View.VISIBLE
      views!!.transactionsRecyclerView.visibility = View.INVISIBLE
      maxBonus = transactionsModel.maxBonus
//      views!!.systemView.showEmpty(getEmptyView(maxBonus))
    }
    if (transactionsModel.notifications.isNotEmpty()
        || transactionsModel.applications.isNotEmpty()) {
      views!!.headerRecyclerView.visibility = View.VISIBLE
      views!!.spacer.visibility = View.VISIBLE
      views!!.container.loadLayoutDescription(R.xml.activity_transactions_scene)
    } else {
      if (views!!.spacer.visibility === View.VISIBLE) {
        views!!.headerRecyclerView.visibility = View.GONE
        views!!.spacer.visibility = View.GONE
      }
      views!!.container.loadLayoutDescription(R.xml.activity_transactions_scene_short)
    }
  }
//
//  private fun getEmptyView(maxBonus: Double): EmptyTransactionsView {
//    if (emptyView == null) {
//      emptyView = EmptyTransactionsView(this, maxBonus.toString(), emptyTransactionsSubject, this,
//          disposables)
//    }
//    return emptyView as EmptyTransactionsView
//  }

  private fun onDefaultWallet(walletModel: TransactionsWalletModel) {
    views!!.transactionsRecyclerView.visibility = View.INVISIBLE
    views!!.systemView.visibility = View.VISIBLE
    views!!.systemView.showProgress(true)
    transactionsController = TransactionsController()
    transactionsController!!.transactionClickListener =
        { transaction: Transaction -> onTransactionClick(transaction) }
    views!!.transactionsRecyclerView.setController(transactionsController!!)
  }

//  private fun onError(errorEnvelope: ErrorEnvelope) {
//    if (errorEnvelope.code == C.ErrorCode.EMPTY_COLLECTION) {
//      views!!.systemView.showEmpty(getEmptyView(maxBonus))
//    }
//  }

  private fun checkRoot() {
    val pref = PreferenceManager.getDefaultSharedPreferences(this)
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit()
          .putBoolean("should_show_root_warning", false)
          .apply()
      val alertDialog = AlertDialog.Builder(this)
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

  override fun onDestroy() {
    views!!.balanceSkeleton.removeAllAnimatorListeners()
    views!!.balanceSkeleton.removeAllUpdateListeners()
    views!!.balanceSkeleton.removeAllLottieOnCompositionLoadedListener()
    emptyTransactionsSubject = null
    emptyView = null
    disposables!!.dispose()
    super.onDestroy()
  }

  private fun setTooltip() {
    popup = PopupWindow(tooltip)
    popup!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
    popup!!.width = ViewGroup.LayoutParams.MATCH_PARENT
    val yOffset = 36.convertDpToPx(resources)
    views!!.fadedBackground.visibility = View.VISIBLE
    popup!!.showAsDropDown(views!!.actionButtonSettings, 0, -yOffset)
    setTooltipListeners()
    viewModel!!.onFingerprintTooltipShown()
  }

  private fun setTooltipListeners() {
    tooltip!!.findViewById<View>(R.id.tooltip_later_button)
        .setOnClickListener { dismissPopup() }
    val context: Context = this
    tooltip!!.findViewById<View>(R.id.tooltip_turn_on_button)
        .setOnClickListener {
          dismissPopup()
          viewModel!!.onTurnFingerprintOnClick(context)
        }
  }

  private fun dismissPopup() {
    viewModel!!.onFingerprintDismissed()
    views!!.fadedBackground.visibility = View.GONE
    popup!!.dismiss()
  }

  private fun onBalanceChanged(globalBalance: GlobalBalance) {
    if (globalBalance.fiatValue.isNotEmpty() && globalBalance.fiatSymbol.isNotEmpty()) {
      views!!.balanceSkeleton.visibility = View.GONE
      views!!.balance.text = globalBalance.fiatSymbol + globalBalance.fiatValue
      setCollapsingTitle(globalBalance.fiatSymbol + globalBalance.fiatValue)
      setSubtitle(globalBalance)
    }
  }

  private fun setSubtitle(globalBalance: GlobalBalance) {
    val subtitle = buildCurrencyString(globalBalance.appcoinsBalance, globalBalance.creditsBalance,
        globalBalance.etherBalance, globalBalance.showAppcoins,
        globalBalance.showCredits, globalBalance.showEthereum)
    views!!.balanceSubtitle.text = Html.fromHtml(subtitle)
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

  val emptyTransactionsScreenClick: Observable<String>?
    get() = emptyTransactionsSubject

  fun navigateToTopApps() {
    viewModel!!.showTopApps(this)
  }

  fun navigateToPromotions(clearStack: Boolean) {
    if (clearStack) {
      supportFragmentManager.popBackStack()
    }
    viewModel!!.navigateToPromotions(this)
  }

  private fun showFingerprintTooltip(shouldShow: Boolean) {
    if (shouldShow) {
      setTooltip()
    }
  }

  private fun showVipBadge(shouldShow: Boolean) {
    views!!.actionButtonVip.root.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  companion object {
    private const val FROM_APP_OPENING_FLAG = "app_opening_flag"
    fun newIntent(context: Context?): Intent {
      return Intent(context, TransactionsActivity::class.java)
    }

    fun newIntent(context: Context?, supportNotificationClicked: Boolean,
                  fromAppOpening: Boolean): Intent {
      val intent = Intent(context, TransactionsActivity::class.java)
      intent.putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      intent.putExtra(FROM_APP_OPENING_FLAG, fromAppOpening)
      return intent
    }

    fun newInstance() = TransactionsActivity()
  }
}