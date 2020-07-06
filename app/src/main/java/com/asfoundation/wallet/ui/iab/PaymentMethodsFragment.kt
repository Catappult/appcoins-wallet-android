package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Pair
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.fragment.app.Fragment
import com.appcoins.wallet.bdsbilling.Billing
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.iab_error_layout.view.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.payment_methods_layout.*
import kotlinx.android.synthetic.main.payment_methods_layout.error_message
import kotlinx.android.synthetic.main.selected_payment_method.*
import kotlinx.android.synthetic.main.support_error_layout.*
import kotlinx.android.synthetic.main.support_error_layout.view.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class PaymentMethodsFragment : DaggerFragment(), PaymentMethodsView {

  companion object {
    private const val IS_BDS = "isBds"
    private const val APP_PACKAGE = "app_package"
    private val TAG = PaymentMethodsFragment::class.java.simpleName
    private const val TRANSACTION = "transaction"
    private const val ITEM_ALREADY_OWNED = "item_already_owned"
    private const val IS_DONATION = "is_donation"

    @JvmStatic
    fun newInstance(transaction: TransactionBuilder?, productName: String?,
                    isBds: Boolean, isDonation: Boolean,
                    developerPayload: String?, uri: String?,
                    transactionData: String?): Fragment {
      val bundle = Bundle()
      bundle.apply {
        putParcelable(TRANSACTION, transaction)
        putSerializable(IabActivity.TRANSACTION_AMOUNT, transaction!!.amount())
        putString(APP_PACKAGE, transaction.domain)
        putString(IabActivity.PRODUCT_NAME, productName)
        putString(IabActivity.DEVELOPER_PAYLOAD, developerPayload)
        putString(IabActivity.URI, uri)
        putBoolean(IS_BDS, isBds)
        putBoolean(IS_DONATION, isDonation)
        putString(IabActivity.TRANSACTION_DATA, transactionData)
      }
      return PaymentMethodsFragment().apply { arguments = bundle }
    }
  }

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var analyticsSetup: RakamAnalytics

  @Inject
  lateinit var bdsPendingTransactionService: BdsPendingTransactionService

  @Inject
  lateinit var billing: Billing

  @Inject
  lateinit var paymentMethodsMapper: PaymentMethodsMapper

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var paymentMethodsInteract: PaymentMethodsInteract
  private lateinit var presenter: PaymentMethodsPresenter
  private lateinit var iabView: IabView
  private lateinit var fiatValue: FiatValue
  private lateinit var compositeDisposable: CompositeDisposable
  private val paymentMethodList: MutableList<PaymentMethod> =
      ArrayList()
  private var setupSubject: PublishSubject<Boolean>? = null
  private var onBackPressedSubject: PublishSubject<Boolean>? = null
  private var preSelectedPaymentMethod: BehaviorSubject<PaymentMethod>? = null
  private var itemAlreadyOwnedError = false
  private var appcEnabled = false
  private var creditsEnabled = false
  private var bonusMessageValue = ""

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Payment Methods Fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    compositeDisposable = CompositeDisposable()
    setupSubject = PublishSubject.create()
    preSelectedPaymentMethod = BehaviorSubject.create()
    onBackPressedSubject = PublishSubject.create()
    itemAlreadyOwnedError = arguments?.getBoolean(ITEM_ALREADY_OWNED, false) ?: false
    presenter = PaymentMethodsPresenter(this, appPackage, AndroidSchedulers.mainThread(),
        Schedulers.io(), CompositeDisposable(), inAppPurchaseInteractor.billingMessagesMapper,
        bdsPendingTransactionService, billing, analytics, analyticsSetup, isBds, developerPayload,
        uri, transactionBuilder!!, paymentMethodsMapper, transactionValue.toDouble(), formatter,
        logger, paymentMethodsInteract)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    buy_button?.isEnabled = false

    setupAppNameAndIcon()

    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.payment_methods_layout, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(ITEM_ALREADY_OWNED, itemAlreadyOwnedError)
  }

  override fun onDestroyView() {
    presenter.stop()
    compositeDisposable.clear()
    super.onDestroyView()
  }

  override fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue,
                                  currency: String, paymentMethodId: String, fiatAmount: String,
                                  appcAmount: String) {
    updateHeaderInfo(fiatValue, isDonation, currency, fiatAmount, appcAmount)
    setupPaymentMethods(paymentMethods, paymentMethodId)

    presenter.sendPaymentMethodsEvents()

    setupSubject!!.onNext(true)
  }

  override fun onResume() {
    if (paymentMethodList.isNotEmpty()) showLoading()
    presenter.onResume()
    super.onResume()
  }

  private fun setupPaymentMethods(paymentMethods: MutableList<PaymentMethod>,
                                  paymentMethodId: String) {
    pre_selected_payment_method_group.visibility = View.GONE
    if (paymentMethods.isNotEmpty()) {
      paymentMethodList.clear()
      payment_methods_radio_group.removeAllViews()
      hideLoading()
    }
    payment_methods_list_group.visibility = View.VISIBLE

    var radioButton: AppCompatRadioButton
    if (isBds) {
      for (index in paymentMethods.indices) {
        val paymentMethod = paymentMethods[index]
        radioButton = createPaymentRadioButton(paymentMethod, index)
        radioButton.isEnabled = paymentMethod.isEnabled
        radioButton.isChecked = paymentMethod.id == paymentMethodId && paymentMethod.isEnabled
        if (paymentMethod is AppCoinsPaymentMethod) {
          appcEnabled = paymentMethod.isAppcEnabled
          creditsEnabled = paymentMethod.isCreditsEnabled
        }
        paymentMethodList.add(paymentMethod)
        payment_methods_radio_group.addView(radioButton)
      }
    } else {
      for (paymentMethod in paymentMethods) {
        if (paymentMethod.id == paymentMethodsMapper.map(SelectedPaymentMethod.APPC)) {
          radioButton = createPaymentRadioButton(paymentMethod, 0)
          radioButton.isEnabled = true
          radioButton.isChecked = true
          paymentMethodList.add(paymentMethod)
          payment_methods_radio_group.addView(radioButton)
        }
      }
    }
  }

  private fun createPaymentRadioButton(paymentMethod: PaymentMethod,
                                       index: Int): AppCompatRadioButton {
    val radioButton = activity!!.layoutInflater.inflate(R.layout.payment_radio_button,
        null) as AppCompatRadioButton
    radioButton.text = paymentMethod.label
    radioButton.id = index
    loadIcons(paymentMethod, radioButton, false)
    return radioButton
  }

  private fun loadIcons(paymentMethod: PaymentMethod, radioButton: RadioButton, showNew: Boolean) {
    val iconSize = resources.getDimensionPixelSize(R.dimen.payment_method_icon_size)
    val context = context!!
    compositeDisposable.add(Observable.fromCallable {
      val bitmap = GlideApp.with(context)
          .asBitmap()
          .load(paymentMethod.iconUrl)
          .submit()
          .get()
      val drawable = BitmapDrawable(context.resources,
          Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true))
      drawable.current
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          val newOptionIcon =
              if (showNew) context.resources.getDrawable(R.drawable.ic_new_option)
              else null
          radioButton.setCompoundDrawablesWithIntrinsicBounds(it, null, newOptionIcon,
              null)
        }
        .subscribe({ }) { it.printStackTrace() })
  }

  private fun updateHeaderInfo(fiatValue: FiatValue, isDonation: Boolean, currency: String,
                               fiatAmount: String, appcAmount: String) {
    this.fiatValue = fiatValue
    val appcPrice = appcAmount + " " + WalletCurrency.APPCOINS.symbol
    val fiatPrice = "$fiatAmount $currency"
    appc_price.text = appcPrice
    fiat_price.text = fiatPrice
    appc_price.visibility = View.VISIBLE
    fiat_price.visibility = View.VISIBLE
    val buyButtonText = if (isDonation) R.string.action_donate else R.string.action_buy
    buy_button.text = resources.getString(buyButtonText)
    if (isDonation) {
      app_sku_description.text = resources.getString(R.string.item_donation)
      app_name.text = resources.getString(R.string.item_donation)
    } else
      if (productName != null) {
        app_sku_description.text = productName
      }
  }

  override fun showPreSelectedPaymentMethod(paymentMethod: PaymentMethod, fiatValue: FiatValue,
                                            isDonation: Boolean, currency: String,
                                            fiatAmount: String, appcAmount: String,
                                            isBonusActive: Boolean) {
    preSelectedPaymentMethod!!.onNext(paymentMethod)
    updateHeaderInfo(fiatValue, isDonation, currency, fiatAmount, appcAmount)

    setupPaymentMethod(paymentMethod, isBonusActive)

    presenter.sendPreSelectedPaymentMethodsEvents()

    setupSubject!!.onNext(true)
  }

  private fun setupPaymentMethod(paymentMethod: PaymentMethod,
                                 isBonusActive: Boolean) {
    pre_selected_payment_method_group.visibility = View.VISIBLE
    payment_methods_list_group.visibility = View.GONE
    bottom_separator?.visibility = View.INVISIBLE
    if (paymentMethod.id == PaymentMethodId.APPC_CREDITS.id) {
      payment_method_description.visibility = View.VISIBLE
      payment_method_description.text = paymentMethod.label
      payment_method_secondary.visibility = View.VISIBLE
      payment_method_description_single.visibility = View.GONE
      if (isBonusActive) hideBonus()
    } else {
      payment_method_description.visibility = View.VISIBLE
      payment_method_description.text = paymentMethod.label
      payment_method_secondary.visibility = View.GONE
      payment_method_description_single.visibility = View.GONE
      if (isBonusActive) showBonus()
    }

    layout_pre_selected.visibility = View.VISIBLE

    loadIcons(paymentMethod, payment_method_ic)
  }

  private fun loadIcons(paymentMethod: PaymentMethod, view: ImageView?) {
    compositeDisposable.add(Observable.fromCallable {
      val context = context
      GlideApp.with(context!!)
          .asBitmap()
          .load(paymentMethod.iconUrl)
          .submit()
          .get()
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { view?.setImageBitmap(it) }
        .subscribe({ }) { it.printStackTrace() })
  }

  override fun showError(message: Int) {
    if (!itemAlreadyOwnedError) {
      loading_view.visibility = View.GONE
      payment_methods.visibility = View.GONE
      payment_method_main_view.visibility = View.GONE
      error_message.error_dismiss.text = getString(R.string.ok)
      error_message.visibility = View.VISIBLE
      error_message.generic_error_layout.error_message.setText(message)
    }
  }

  override fun showItemAlreadyOwnedError() {
    loading_view.visibility = View.GONE
    payment_methods.visibility = View.GONE
    payment_method_main_view.visibility = View.GONE
    itemAlreadyOwnedError = true
    iabView.disableBack()
    val view = view
    if (view != null) {
      view.isFocusableInTouchMode = true
      view.requestFocus()
      view.setOnKeyListener(View.OnKeyListener { view1: View?, keyCode: Int, keyEvent: KeyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          onBackPressedSubject?.onNext(itemAlreadyOwnedError)
        }
        true
      })
    }
    error_dismiss.text = getString(R.string.ok)
    error_message.visibility = View.VISIBLE
    generic_error_layout.error_message.setText(
        R.string.purchase_error_incomplete_transaction_body)
    layout_support_icn.visibility = View.GONE
    layout_support_logo.visibility = View.GONE
    contact_us.visibility = View.GONE
  }

  override fun finish(bundle: Bundle) {
    iabView.finish(bundle)
  }

  override fun showLoading() {
    loading_view.visibility = View.VISIBLE
    payment_methods.visibility = View.INVISIBLE
  }

  override fun hideLoading() {
    loading_view.visibility = View.GONE
    buy_button.isEnabled = true
    if (processing_loading.visibility != View.VISIBLE) {
      payment_methods.visibility = View.VISIBLE
    }
  }

  override fun getCancelClick(): Observable<PaymentMethod> {
    return RxView.clicks(cancel_button)
        .map { getSelectedPaymentMethod() }
  }

  private fun getSelectedPaymentMethod(): PaymentMethod {
    val hasPreSelectedPaymentMethod =
        inAppPurchaseInteractor.hasPreSelectedPaymentMethod()
    val checkedButtonId = payment_methods_radio_group.checkedRadioButtonId
    return if (paymentMethodList.isNotEmpty() && checkedButtonId != -1) {
      paymentMethodList[checkedButtonId]
    } else if (hasPreSelectedPaymentMethod && checkedButtonId == -1) {
      preSelectedPaymentMethod?.value ?: PaymentMethod()
    } else {
      PaymentMethod()
    }
  }

  override fun close(bundle: Bundle) {
    iabView.close(bundle)
  }

  override fun errorDismisses(): Observable<Boolean> {
    return RxView.clicks(error_dismiss)
        .map { itemAlreadyOwnedError }
  }

  override fun getSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun setupUiCompleted() = setupSubject!!

  override fun showProcessingLoadingDialog() {
    payment_methods.visibility = View.INVISIBLE
    loading_view.visibility = View.GONE
    processing_loading.visibility = View.VISIBLE
  }

  override fun getBuyClick(): Observable<PaymentMethod> {
    return RxView.clicks(buy_button)
        .map { getSelectedPaymentMethod() }
  }

  override fun showPaypal(gamificationLevel: Int) {
    iabView.showAdyenPayment(fiatValue.amount, fiatValue.currency, isBds,
        PaymentType.PAYPAL, bonusMessageValue, false, null, gamificationLevel)
  }

  override fun showAdyen(fiatValue: FiatValue, paymentType: PaymentType, iconUrl: String?,
                         gamificationLevel: Int) {
    if (!itemAlreadyOwnedError) {
      iabView.showAdyenPayment(fiatValue.amount, fiatValue.currency, isBds, paymentType,
          bonusMessageValue, true, iconUrl, gamificationLevel)
    }
  }

  override fun showCreditCard(gamificationLevel: Int) {
    iabView.showAdyenPayment(fiatValue.amount, fiatValue.currency, isBds,
        PaymentType.CARD, bonusMessageValue, false, null, gamificationLevel)
  }

  override fun showAppCoins(gamificationLevel: Int) {
    iabView.showOnChain(transactionBuilder!!.amount(), isBds, bonusMessageValue, gamificationLevel)
  }

  override fun showCredits(gamificationLevel: Int) {
    iabView.showAppcoinsCreditsPayment(transactionBuilder!!.amount(), gamificationLevel)
  }

  override fun showShareLink(selectedPaymentMethod: String) {
    val isOneStep: Boolean = transactionBuilder!!.type
        .equals("INAPP_UNMANAGED", ignoreCase = true)
    iabView.showShareLinkPayment(transactionBuilder!!.domain, transactionBuilder!!.skuId,
        if (isOneStep) transactionBuilder!!.originalOneStepValue else null,
        if (isOneStep) transactionBuilder!!.originalOneStepCurrency else null,
        transactionBuilder!!.amount(),
        transactionBuilder!!.type, selectedPaymentMethod)
  }

  override fun getPaymentSelection(): Observable<String> {
    return Observable.merge(
        RxRadioGroup.checkedChanges(payment_methods_radio_group)
            .filter { checkedRadioButtonId -> checkedRadioButtonId >= 0 }
            .map { paymentMethodList[it].id }, preSelectedPaymentMethod!!.map(
        PaymentMethod::id))
  }

  override fun getMorePaymentMethodsClicks(): Observable<PaymentMethod> {
    return RxView.clicks(more_payment_methods)
        .map { getSelectedPaymentMethod() }
  }

  override fun showLocalPayment(selectedPaymentMethod: String, iconUrl: String, label: String,
                                gamificationLevel: Int) {
    val isOneStep: Boolean = transactionBuilder!!.type
        .equals("INAPP_UNMANAGED", ignoreCase = true)
    iabView.showLocalPayment(transactionBuilder!!.domain, transactionBuilder!!.skuId,
        if (isOneStep) transactionBuilder!!.originalOneStepValue else null,
        if (isOneStep) transactionBuilder!!.originalOneStepCurrency else null, bonusMessageValue,
        selectedPaymentMethod, transactionBuilder!!.toAddress(), transactionBuilder!!.type,
        transactionBuilder!!.amount(), transactionBuilder!!.callbackUrl,
        transactionBuilder!!.orderReference, transactionBuilder!!.payload, iconUrl, label,
        gamificationLevel)
  }

  override fun setBonus(bonus: BigDecimal, currency: String) {
    var scaledBonus = bonus.stripTrailingZeros()
        .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currency
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currency"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    bonusMessageValue = newCurrencyString + formattedBonus
    bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonusMessageValue)
  }

  override fun onBackPressed() = onBackPressedSubject!!

  override fun showNext() = buy_button.setText(R.string.action_next)

  override fun showBuy() = setBuyButtonText()

  private fun setBuyButtonText() {
    val buyButtonText = if (isDonation) R.string.action_donate else R.string.action_buy
    buy_button.setText(buyButtonText)
  }

  override fun showMergedAppcoins(gamificationLevel: Int) {
    iabView.showMergedAppcoins(fiatValue.amount, fiatValue.currency, bonusMessageValue,
        productName, appcEnabled, creditsEnabled, isBds, isDonation, gamificationLevel)
  }

  override fun lockRotation() = iabView.lockRotation()

  override fun showEarnAppcoins() {
    iabView.showEarnAppcoins(transactionBuilder!!.domain, transactionBuilder!!.skuId,
        transactionBuilder!!.amount(), transactionBuilder!!.type)
  }

  override fun showBonus() {
    bonus_layout.visibility = View.VISIBLE
    bonus_msg.visibility = View.VISIBLE
    no_bonus_msg?.visibility = View.INVISIBLE
    bottom_separator?.visibility = View.VISIBLE
  }

  override fun removeBonus() {
    bonus_layout.visibility = View.GONE
    bonus_msg.visibility = View.GONE
    no_bonus_msg?.visibility = View.GONE
    bottom_separator?.visibility = View.GONE
  }

  override fun hideBonus() {
    bonus_layout.visibility = View.INVISIBLE
    bonus_msg.visibility = View.INVISIBLE
    bottom_separator?.visibility = View.INVISIBLE
  }

  override fun replaceBonus() {
    bonus_layout.visibility = View.INVISIBLE
    bonus_msg.visibility = View.INVISIBLE
    no_bonus_msg?.visibility = View.VISIBLE
  }

  private fun setupAppNameAndIcon() {
    compositeDisposable.add(Single.defer { Single.just(appPackage) }
        .observeOn(Schedulers.io())
        .map { packageName ->
          Pair(
              getApplicationName(packageName),
              context!!.packageManager
                  .getApplicationIcon(packageName))
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          app_name?.text = it.first
          app_icon?.setImageDrawable(it.second)
        }) { it.printStackTrace() })
  }

  private fun getApplicationName(packageName: String): String {
    val packageManager = context!!.packageManager
    val packageInfo =
        packageManager.getApplicationInfo(packageName, 0)
    return packageManager.getApplicationLabel(packageInfo)
        .toString()
  }

  private val isBds: Boolean by lazy {
    if (arguments!!.containsKey(IS_BDS)) {
      arguments!!.getBoolean(IS_BDS)
    } else {
      throw IllegalArgumentException("isBds data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (arguments!!.containsKey(IS_DONATION)) {
      arguments!!.getBoolean(IS_DONATION)
    } else {
      throw IllegalArgumentException("isDonation data not found")
    }
  }

  private val transactionBuilder: TransactionBuilder? by lazy {
    if (arguments!!.containsKey(TRANSACTION)) {
      arguments!!.getParcelable(TRANSACTION) as TransactionBuilder?
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val transactionValue: BigDecimal by lazy {
    if (arguments!!.containsKey(IabActivity.TRANSACTION_AMOUNT)) {
      arguments!!.getSerializable(IabActivity.TRANSACTION_AMOUNT) as BigDecimal
    } else {
      throw java.lang.IllegalArgumentException("transaction value not found")
    }
  }

  private val productName: String? by lazy {
    if (arguments!!.containsKey(IabActivity.PRODUCT_NAME)) {
      arguments!!.getString(IabActivity.PRODUCT_NAME)
    } else {
      throw IllegalArgumentException("productName data not found")
    }
  }

  private val appPackage: String by lazy {
    if (arguments!!.containsKey(IabActivity.APP_PACKAGE)) {
      arguments!!.getString(IabActivity.APP_PACKAGE, "")
    } else {
      throw IllegalArgumentException("appPackage data not found")
    }
  }

  private val developerPayload: String by lazy {
    if (arguments!!.containsKey(IabActivity.DEVELOPER_PAYLOAD)) {
      arguments!!.getString(IabActivity.DEVELOPER_PAYLOAD, "")
    } else {
      throw IllegalArgumentException("developer payload data not found")
    }
  }

  private val uri: String by lazy {
    if (arguments!!.containsKey(IabActivity.URI)) {
      arguments!!.getString(IabActivity.URI, "")
    } else {
      throw IllegalArgumentException("productName data not found")
    }
  }
}