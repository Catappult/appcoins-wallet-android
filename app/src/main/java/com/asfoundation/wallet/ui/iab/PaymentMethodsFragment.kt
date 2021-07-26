package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.analytics.TaskTimer
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
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
import kotlinx.android.synthetic.main.overlay_fragment.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.payment_methods_layout.*
import kotlinx.android.synthetic.main.payment_methods_layout.error_message
import kotlinx.android.synthetic.main.selected_payment_method.*
import kotlinx.android.synthetic.main.support_error_layout.*
import kotlinx.android.synthetic.main.support_error_layout.view.error_message
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class PaymentMethodsFragment : DaggerFragment(), PaymentMethodsView {

  companion object {
    private const val IS_BDS = "isBds"
    private const val APP_PACKAGE = "app_package"
    private const val TRANSACTION = "transaction"
    private const val ITEM_ALREADY_OWNED = "item_already_owned"
    private const val IS_DONATION = "is_donation"

    @JvmStatic
    fun newInstance(
      transaction: TransactionBuilder?, productName: String?,
      isBds: Boolean, isDonation: Boolean,
      developerPayload: String?, uri: String?,
      transactionData: String?
    ): Fragment {
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
  lateinit var taskTimer: TaskTimer

  @Inject
  lateinit var paymentMethodsAnalytics: PaymentMethodsAnalytics

  @Inject
  lateinit var paymentMethodsMapper: PaymentMethodsMapper

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var paymentMethodsInteractor: PaymentMethodsInteractor

  private lateinit var presenter: PaymentMethodsPresenter
  private lateinit var iabView: IabView
  private lateinit var compositeDisposable: CompositeDisposable
  private lateinit var paymentMethodClick: PublishRelay<Int>
  private lateinit var paymentMethodsAdapter: PaymentMethodsAdapter
  private val paymentMethodList: MutableList<PaymentMethod> = ArrayList()
  private var setupSubject: PublishSubject<Boolean>? = null
  private var preSelectedPaymentMethod: BehaviorSubject<PaymentMethod>? = null
  private var isPreSelected = false
  private var itemAlreadyOwnedError = false
  private var bonusMessageValue = ""
  private var bonusValue: BigDecimal? = null

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
    paymentMethodClick = PublishRelay.create()
    itemAlreadyOwnedError = arguments?.getBoolean(ITEM_ALREADY_OWNED, false) ?: false
    val paymentMethodsData = PaymentMethodsData(
      appPackage, isBds, getDeveloperPayload(), getUri(),
      transactionBuilder!!.skuId
    )
    presenter = PaymentMethodsPresenter(
      this, AndroidSchedulers.mainThread(),
      Schedulers.io(), CompositeDisposable(), paymentMethodsAnalytics, transactionBuilder!!,
      paymentMethodsMapper, formatter, logger, paymentMethodsInteractor, paymentMethodsData,
      taskTimer
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    buy_button?.isEnabled = false

    setupAppNameAndIcon()

    setBuyButtonText()
    presenter.present(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.payment_methods_layout, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(ITEM_ALREADY_OWNED, itemAlreadyOwnedError)
    presenter.onSavedInstance(outState)
  }

  override fun onDestroyView() {
    presenter.stop()
    compositeDisposable.clear()
    super.onDestroyView()
  }

  override fun showPaymentMethods(
    paymentMethods: MutableList<PaymentMethod>, currency: String,
    paymentMethodId: String, fiatAmount: String, appcAmount: String,
    appcEnabled: Boolean, creditsEnabled: Boolean
  ) {
    updateHeaderInfo(currency, fiatAmount, appcAmount)
    setupPaymentMethods(paymentMethods, paymentMethodId)

    setupSubject!!.onNext(true)
  }

  override fun onResume() {
    val firstRun = paymentMethodList.isEmpty() && !isPreSelected
    presenter.onResume(firstRun)
    super.onResume()
  }

  private fun setupPaymentMethods(
    paymentMethods: MutableList<PaymentMethod>,
    paymentMethodId: String
  ) {
    isPreSelected = false
    pre_selected_payment_method_group.visibility = View.GONE
    mid_separator?.visibility = View.VISIBLE
    if (paymentMethods.isNotEmpty()) {
      paymentMethodsAdapter =
        PaymentMethodsAdapter(paymentMethods, paymentMethodId, paymentMethodClick)
      payment_methods_radio_list.adapter = paymentMethodsAdapter
      paymentMethodList.clear()
      paymentMethodList.addAll(paymentMethods)
      paymentMethodClick.accept(paymentMethodsAdapter.getSelectedItem())
    }
  }

  private fun updateHeaderInfo(currency: String, fiatAmount: String, appcAmount: String) {
    val appcPrice = appcAmount + " " + WalletCurrency.APPCOINS.symbol
    val fiatPrice = "$fiatAmount $currency"
    appc_price.text = appcPrice
    fiat_price.text = fiatPrice
    fiat_price_skeleton.visibility = View.GONE
    appc_price_skeleton.visibility = View.GONE
    appc_price.visibility = View.VISIBLE
    fiat_price.visibility = View.VISIBLE
  }

  private fun getPaymentMethodLabel(paymentMethod: PaymentMethod): String {
    return TranslatablePaymentMethods.values()
      .firstOrNull { it.paymentMethod == paymentMethod.id }
      ?.let { getString(it.stringId) } ?: paymentMethod.label
  }

  override fun showPreSelectedPaymentMethod(
    paymentMethod: PaymentMethod, currency: String,
    fiatAmount: String, appcAmount: String,
    isBonusActive: Boolean
  ) {
    preSelectedPaymentMethod!!.onNext(paymentMethod)
    updateHeaderInfo(currency, fiatAmount, appcAmount)

    setupPaymentMethod(paymentMethod, isBonusActive)

    setupSubject!!.onNext(true)
  }

  private fun setupPaymentMethod(
    paymentMethod: PaymentMethod,
    isBonusActive: Boolean
  ) {
    isPreSelected = true
    mid_separator?.visibility = View.INVISIBLE
    payment_method_description.visibility = View.VISIBLE
    payment_method_description.text = getPaymentMethodLabel(paymentMethod)
    payment_method_description_single.visibility = View.GONE
    if (paymentMethod.id == PaymentMethodId.APPC_CREDITS.id) {
      payment_method_secondary.visibility = View.VISIBLE
      if (isBonusActive) hideBonus()
    } else {
      payment_method_secondary.visibility = View.GONE
      if (isBonusActive) showBonus()
    }
    setupFee(paymentMethod.fee)
    loadIcons(paymentMethod, payment_method_ic)
  }

  private fun setupFee(fee: PaymentMethodFee?) {
    if (fee?.isValidFee() == true) {
      payment_method_fee.visibility = View.VISIBLE
      val formattedValue = formatter.formatPaymentCurrency(fee.amount!!, WalletCurrency.FIAT)
      payment_method_fee_value.text = "$formattedValue ${fee.currency}"

      payment_method_fee_value.apply {
        this.setTextColor(ContextCompat.getColor(requireContext(), R.color.appc_pink))
        this.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
      }
    } else {
      payment_method_fee.visibility = View.GONE
    }
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
      payment_method_main_view.visibility = View.GONE
      error_message.error_dismiss.text = getString(R.string.ok)
      error_message.visibility = View.VISIBLE
      error_message.generic_error_layout.error_message.setText(message)
    }
  }

  override fun showItemAlreadyOwnedError() {
    payment_method_main_view.visibility = View.GONE
    itemAlreadyOwnedError = true
    iabView.disableBack()
    error_dismiss.text = getString(R.string.ok)
    error_message.visibility = View.VISIBLE
    generic_error_layout.error_message.setText(
      R.string.purchase_error_incomplete_transaction_body
    )
    layout_support_icn.visibility = View.GONE
    layout_support_logo.visibility = View.GONE
    contact_us.visibility = View.GONE
  }

  override fun finish(bundle: Bundle) {
    iabView.finish(bundle)
  }

  override fun showPaymentsSkeletonLoading() {
    buy_button.isEnabled = false
    pre_selected_payment_method_group.visibility = View.GONE
    payment_methods_list_group.visibility = View.INVISIBLE
    mid_separator?.visibility = View.VISIBLE
    payments_skeleton.visibility = View.VISIBLE
  }

  override fun showSkeletonLoading() {
    showPaymentsSkeletonLoading()
    bonus_view.visibility = View.VISIBLE
    bonus_view.showSkeleton()
  }

  override fun showProgressBarLoading() {
    payment_methods.visibility = View.INVISIBLE
    loading_view.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    if (processing_loading.visibility != View.VISIBLE) {
      payment_methods.visibility = View.VISIBLE
      removeSkeletons()
      buy_button.isEnabled = true
      if (isPreSelected) {
        pre_selected_payment_method_group.visibility = View.VISIBLE
        payment_methods_list_group.visibility = View.GONE
        bottom_separator?.visibility = View.INVISIBLE
        layout_pre_selected.visibility = View.VISIBLE
      } else {
        payment_methods_list_group.visibility = View.VISIBLE
        pre_selected_payment_method_group.visibility = View.GONE
      }
      loading_view.visibility = View.GONE
    }
  }

  override fun getCancelClick(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun getSelectedPaymentMethod(hasPreSelectedPaymentMethod: Boolean): PaymentMethod {
    if (!isPreSelected && ::paymentMethodsAdapter.isInitialized.not()) return PaymentMethod()
    val checkedButtonId =
      if (::paymentMethodsAdapter.isInitialized) paymentMethodsAdapter.getSelectedItem() else -1
    return if (paymentMethodList.isNotEmpty() && !isPreSelected && checkedButtonId != -1) {
      paymentMethodList[checkedButtonId]
    } else if (hasPreSelectedPaymentMethod && checkedButtonId == -1) {
      preSelectedPaymentMethod?.value ?: PaymentMethod()
    } else {
      PaymentMethod()
    }
  }

  override fun updateProductName() {
    app_sku_description.text = transactionBuilder?.productName
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

  override fun showAuthenticationActivity() = iabView.showAuthenticationActivity()

  override fun setupUiCompleted() = setupSubject!!

  override fun showProcessingLoadingDialog() {
    payment_methods.visibility = View.INVISIBLE
    processing_loading.visibility = View.VISIBLE
  }

  override fun getBuyClick(): Observable<Any> {
    return RxView.clicks(buy_button)
  }

  override fun showCarrierBilling(fiatValue: FiatValue, isPreselected: Boolean) {
    iabView.showCarrierBilling(fiatValue.currency, fiatValue.amount, bonusValue, isPreselected)
  }

  override fun showPaypal(gamificationLevel: Int, fiatValue: FiatValue) {
    iabView.showAdyenPayment(
      fiatValue.amount, fiatValue.currency, isBds,
      PaymentType.PAYPAL, bonusMessageValue, false, null, gamificationLevel
    )
  }


  override fun showAdyen(
    fiatAmount: BigDecimal, fiatCurrency: String, paymentType: PaymentType,
    iconUrl: String?,
    gamificationLevel: Int
  ) {
    if (!itemAlreadyOwnedError) {
      iabView.showAdyenPayment(
        fiatAmount, fiatCurrency, isBds, paymentType, bonusMessageValue,
        true, iconUrl, gamificationLevel
      )
    }
  }

  override fun showCreditCard(gamificationLevel: Int, fiatValue: FiatValue) {
    iabView.showAdyenPayment(
      fiatValue.amount, fiatValue.currency, isBds,
      PaymentType.CARD, bonusMessageValue, false, null, gamificationLevel
    )
  }

  override fun showAppCoins(gamificationLevel: Int) {
    iabView.showOnChain(
      transactionBuilder!!.amount(), isBds, bonusMessageValue,
      gamificationLevel
    )
  }

  override fun showCredits(gamificationLevel: Int) {
    iabView.showAppcoinsCreditsPayment(transactionBuilder!!.amount(), gamificationLevel)
  }

  override fun showShareLink(selectedPaymentMethod: String) {
    val isOneStep: Boolean = transactionBuilder!!.type
      .equals("INAPP_UNMANAGED", ignoreCase = true)
    iabView.showShareLinkPayment(
      transactionBuilder!!.domain, transactionBuilder!!.skuId,
      if (isOneStep) transactionBuilder!!.originalOneStepValue else null,
      if (isOneStep) transactionBuilder!!.originalOneStepCurrency else null,
      transactionBuilder!!.amount(),
      transactionBuilder!!.type, selectedPaymentMethod
    )
  }

  override fun getPaymentSelection(): Observable<String> {
    return Observable.merge(paymentMethodClick
      .filter { checkedRadioButtonId -> checkedRadioButtonId >= 0 }
      .map { paymentMethodList[it].id }, preSelectedPaymentMethod!!.map(
      PaymentMethod::id
    )
    )
  }

  override fun getMorePaymentMethodsClicks(): Observable<Any> {
    return RxView.clicks(more_payment_methods)
  }

  override fun showLocalPayment(
    selectedPaymentMethod: String, iconUrl: String, label: String,
    async: Boolean, fiatAmount: String, fiatCurrency: String,
    gamificationLevel: Int
  ) {
    iabView.showLocalPayment(
      transactionBuilder!!.domain, transactionBuilder!!.skuId,
      fiatAmount, fiatCurrency, bonusMessageValue, selectedPaymentMethod,
      transactionBuilder!!.toAddress(), transactionBuilder!!.type, transactionBuilder!!.amount(),
      transactionBuilder!!.callbackUrl, transactionBuilder!!.orderReference,
      transactionBuilder!!.payload, transactionBuilder!!.origin, iconUrl, label, async,
      transactionBuilder!!.referrerUrl, gamificationLevel
    )
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
    bonusValue = bonus
    bonus_view.setPurchaseBonusHeaderValue(bonus, currency)
  }

  override fun onBackPressed(): Observable<Boolean> {
    return iabView.backButtonPress()
      .map { itemAlreadyOwnedError }
  }

  override fun showNext() = buy_button.setText(R.string.action_next)

  override fun showBuy() = setBuyButtonText()

  private fun setBuyButtonText() {
    val buyButtonText = if (isDonation) R.string.action_donate else R.string.action_buy
    buy_button.setText(buyButtonText)
  }

  override fun showMergedAppcoins(gamificationLevel: Int, fiatValue: FiatValue) {
    iabView.showMergedAppcoins(
      fiatValue.amount, fiatValue.currency, bonusMessageValue,
      isBds, isDonation, gamificationLevel
    )
  }

  override fun lockRotation() = iabView.lockRotation()

  override fun showEarnAppcoins() {
    iabView.showEarnAppcoins(
      transactionBuilder!!.domain, transactionBuilder!!.skuId,
      transactionBuilder!!.amount(), transactionBuilder!!.type
    )
  }

  override fun showBonus() {
    bonus_view.visibility = View.VISIBLE
    bonus_view.setPurchaseBonusDescription(getString(R.string.gamification_purchase_body))
    bonus_view.showPurchaseBonusHeader()
    bottom_separator?.visibility = View.VISIBLE
    bonus_view.hideSkeleton()
  }

  override fun removeBonus() {
    bonusMessageValue = ""
    bonusValue = null
    bonus_view.visibility = View.GONE
    bottom_separator?.visibility = View.GONE
    bonus_view.hideSkeleton()
  }

  override fun hideBonus() {
    bonus_view.visibility = View.INVISIBLE
    bottom_separator?.visibility = View.INVISIBLE
    bonus_view.hideSkeleton()
  }

  override fun replaceBonus() {
    bonus_view.visibility = View.VISIBLE
    bottom_separator?.visibility = View.VISIBLE
    bonus_view.setPurchaseBonusDescription(getString(R.string.purchase_poa_body))
    bonus_view.hidePurchaseBonusHeader()
    bonus_view.hideSkeleton()
  }

  override fun onAuthenticationResult(): Observable<Boolean> {
    return iabView.onAuthenticationResult()
  }

  private fun setupAppNameAndIcon() {
    if (isDonation) {
      app_sku_description.text = resources.getString(R.string.item_donation)
      app_name.text = resources.getString(R.string.item_donation)
    } else {
      compositeDisposable.add(Single.defer { Single.just(appPackage) }
        .observeOn(Schedulers.io())
        .map { packageName ->
          Pair(
            getApplicationName(packageName),
            context!!.packageManager.getApplicationIcon(packageName)
          )
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ setHeaderInfo(it.first, it.second) }) { it.printStackTrace() })
    }
  }

  private fun setHeaderInfo(appName: String, appIcon: Drawable) {
    app_name?.text = appName
    app_icon?.setImageDrawable(appIcon)
    app_sku_description.text = transactionBuilder?.productName
  }

  private fun getApplicationName(packageName: String): String {
    val packageManager = context!!.packageManager
    val packageInfo = packageManager.getApplicationInfo(packageName, 0)
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

  private val appPackage: String by lazy {
    if (arguments!!.containsKey(IabActivity.APP_PACKAGE)) {
      arguments!!.getString(IabActivity.APP_PACKAGE, "")
    } else {
      throw IllegalArgumentException("appPackage data not found")
    }
  }

  private fun getDeveloperPayload(): String? {
    return if (arguments!!.containsKey(IabActivity.DEVELOPER_PAYLOAD)) {
      arguments!!.getString(IabActivity.DEVELOPER_PAYLOAD, "")
    } else {
      throw IllegalArgumentException("developer payload data not found")
    }
  }

  private fun getUri(): String? {
    return if (arguments!!.containsKey(IabActivity.URI)) {
      arguments!!.getString(IabActivity.URI, "")
    } else {
      throw IllegalArgumentException("uri data not found")
    }
  }

  private fun getTransactionValue(): BigDecimal {
    return if (arguments!!.containsKey(IabActivity.TRANSACTION_AMOUNT)) {
      arguments!!.getSerializable(IabActivity.TRANSACTION_AMOUNT) as BigDecimal
    } else {
      throw java.lang.IllegalArgumentException("transaction value not found")
    }
  }

  private fun removeSkeletons() {
    fiat_price_skeleton.visibility = View.GONE
    appc_price_skeleton.visibility = View.GONE
    payments_skeleton.visibility = View.GONE
  }
}