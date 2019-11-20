package com.asfoundation.wallet.billing.adyen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.billing.adyen.AdyenPaymentInteractor
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.util.KeyboardUtils
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.adyen_credit_card_layout.*
import kotlinx.android.synthetic.main.adyen_credit_card_layout.adyen_card_form
import kotlinx.android.synthetic.main.adyen_credit_card_layout.fragment_credit_card_authorization_progress_bar
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.bonus_layout
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.bonus_msg
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.payment_methods
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_iab_error.*
import kotlinx.android.synthetic.main.fragment_iab_error.view.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.*
import kotlinx.android.synthetic.main.fragment_top_up.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import org.apache.commons.lang3.StringUtils
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class AdyenPaymentFragment : DaggerFragment(),
    AdyenPaymentView {

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor
  @Inject
  lateinit var billing: Billing
  @Inject
  lateinit var analytics: BillingAnalytics
  @Inject
  lateinit var adyenPaymentInteractor: AdyenPaymentInteractor
  private lateinit var iabView: IabView
  private lateinit var publicKey: String
  private lateinit var generationTime: String
  private lateinit var paymentMethod: PaymentMethod
  private lateinit var presenter: AdyenPaymentPresenter
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var compositeDisposable: CompositeDisposable
  private lateinit var cardComponent: CardComponent
  private var cvcOnly: Boolean = false
  private var backButton: PublishRelay<Boolean>? = null
  private var keyboardBuyRelay: PublishRelay<Boolean>? = null
  private var validationSubject: PublishSubject<Boolean>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    backButton = PublishRelay.create<Boolean>()
    keyboardBuyRelay = PublishRelay.create<Boolean>()
    validationSubject = PublishSubject.create<Boolean>()
    val navigator = FragmentNavigator(activity as UriNavigator?, iabView)
    compositeDisposable = CompositeDisposable()
    presenter = AdyenPaymentPresenter(this, compositeDisposable, AndroidSchedulers.mainThread(),
        Schedulers.io(), analytics, domain, adyenPaymentInteractor, inAppPurchaseInteractor,
        inAppPurchaseInteractor.parseTransaction(transactionData, true), navigator, paymentType,
        amount, currency, isPreSelected)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return if (isPreSelected) {
      inflater.inflate(R.layout.adyen_credit_card_pre_selected, container,
          false)
    } else {
      inflater.inflate(R.layout.adyen_credit_card_layout, container, false)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupTransactionCompleteAnimation()
    // removing additional margin top of the credit card form to help in the layout build
    buy_button.visibility = View.INVISIBLE
    if (transactionType.equals(TransactionData.TransactionType.DONATION.name, ignoreCase = true)) {
      buy_button.setText(R.string.action_donate)
    } else {
      buy_button.setText(R.string.action_buy)
    }

    val cardConfigurationBuilder =
        CardConfiguration.Builder(context!!, BuildConfig.ADYEN_PUBLIC_KEY)

    cardConfiguration = cardConfigurationBuilder.let {
      it.setEnvironment(Environment.TEST)
      it.build()
    }

    if (isPreSelected) {
      showBonus()
      loadIcon()
    } else {
      cancel_button.setText(R.string.back_button)
      setBackListener(view)
    }
    if (StringUtils.isNotBlank(bonus)) {
      lottie_transaction_success.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation()
    } else {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
    showProduct()
    presenter.present(savedInstanceState)
  }

  override fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod) {
    val cardComponent = CardComponent.PROVIDER.get(this, paymentMethod, cardConfiguration)
    adyen_card_form?.attach(cardComponent, this)
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this, androidx.lifecycle.Observer {
      if (it?.isValid == true) {
        Log.d("TAG123", "Valid")
      }
      Log.d("TAG123", "Not Valid")
    })
  }

  override fun handleFinalResponse() {

  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "adyen payment fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun getAnimationDuration(): Long {
    return lottie_transaction_success.duration
  }

  override fun showProduct() {
    val formatter = Formatter()
    try {
      app_icon?.setImageDrawable(context!!.packageManager
          .getApplicationIcon(domain))
      app_name?.text = getApplicationName(domain)
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    app_sku_description?.text = arguments!!.getString(IabActivity.PRODUCT_NAME)
    val appcValue = formatter.format(Locale.getDefault(), "%(,.2f", amount.toDouble())
        .toString() + " APPC"
    appc_price.text = appcValue
  }

  override fun showLoading() {
    if (isPreSelected) {
      fragment_credit_card_authorization_progress_bar.visibility = View.VISIBLE
      payment_methods?.visibility = View.INVISIBLE
    } else {
      fragment_credit_card_authorization_progress_bar.visibility = View.VISIBLE
      //cardForm.setVisibility(View.GONE)
      cc_info_view.visibility = View.INVISIBLE
      buy_button.visibility = View.INVISIBLE
      cancel_button.visibility = View.INVISIBLE
      change_card_button.visibility = View.INVISIBLE
    }
  }

  override fun hideLoading() {
    //buy_button.visibility = cardForm.isValid() ? Visible : INVisible
    //    cardForm.setOnCardFormValidListener(valid -> validationSubject.onNext(valid));
    if (isPreSelected) {
      fragment_credit_card_authorization_progress_bar.visibility = View.GONE
      payment_methods?.visibility = View.VISIBLE
    } else {
      fragment_credit_card_authorization_progress_bar.visibility = View.GONE
      //cardForm.setVisibility(View.VISIBLE)
      cc_info_view.visibility = View.VISIBLE
      cancel_button.visibility = View.VISIBLE
    }
  }

  override fun errorDismisses(): Observable<Any> {
    return RxView.clicks(activity_iab_error_ok_button)
  }

  override fun buyButtonClicked(): Observable<com.adyen.checkout.base.model.paymentmethods.PaymentMethod> {
    return Observable.merge(keyboardBuyRelay,
        RxView.clicks(buy_button))
        .doOnNext { view?.let { KeyboardUtils.hideKeyboard(view) } }
        .map { cardComponent.paymentMethod }
  }

  override fun changeCardMethodDetailsEvent(): Observable<PaymentMethod> {
    return RxView.clicks(change_card_button)
        .map { paymentMethod }
  }

  override fun showNetworkError() {
    main_view?.visibility = View.GONE
    main_view_pre_selected?.visibility = View.GONE
    fragment_iab_error?.visibility = View.VISIBLE
    fragment_iab_error?.activity_iab_error_message?.setText(R.string.notification_no_network_poa)
    fragment_iab_error_pre_selected?.visibility = View.VISIBLE
    fragment_iab_error_pre_selected?.activity_iab_error_message?.setText(
        R.string.notification_no_network_poa)
  }

  override fun backEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
        .mergeWith(backButton)
  }

  override fun showCvcView(amount: Amount, paymentMethod: PaymentMethod) {
    cvcOnly = true
    //cardForm.findViewById(com.braintreepayments.cardform.R.id.bt_card_form_card_number_icon)
    //   .setVisibility(View.GONE)
    this.paymentMethod = paymentMethod
    showProductPrice(amount)
    fragment_credit_card_authorization_pre_authorized_card.visibility = View.VISIBLE
    fragment_credit_card_authorization_pre_authorized_card.text = paymentMethod.label
    if (!isPreSelected) {
      change_card_button.visibility = View.VISIBLE
    }
    //rememberCardCheckBox.setVisibility(View.GONE) Adyen
    /*cardForm.cardRequired(false)
        .expirationRequired(false)
        .cvvRequired(true)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.action_buy))
        .setup(activity)*/
    hideLoading()
    finishSetupView()
  }

  override fun showCreditCardView(paymentMethod: PaymentMethod, amount: Amount,
                                  cvcStatus: Boolean, allowSave: Boolean, publicKey: String,
                                  generationTime: String) {
    this.paymentMethod = paymentMethod
    this.publicKey = publicKey
    this.generationTime = generationTime
    cvcOnly = false
    fragment_credit_card_authorization_pre_authorized_card.setVisibility(View.GONE)
    if (!isPreSelected) {
      change_card_button.visibility = View.GONE
    }
    //rememberCardCheckBox.setVisibility(View.VISIBLE)
    showProductPrice(amount)
    /*cardForm.setCardNumberIcon(0)
    cardForm.cardRequired(true)
        .expirationRequired(true)
        .cvvRequired(cvcStatus)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.action_buy))
        .setup(activity)*/
    hideLoading()
    finishSetupView()
  }

  override fun close(bundle: Bundle?) {
    iabView.close(bundle)
  }

  override fun showSuccess() {
    if (isPreSelected) {
      main_view?.visibility = View.GONE
      main_view_pre_selected?.visibility = View.GONE
      iab_activity_transaction_completed.visibility = View.VISIBLE
    } else {
      fragment_credit_card_authorization_progress_bar.visibility = View.GONE
      credit_card_info.visibility = View.GONE
      lottie_transaction_success.visibility = View.VISIBLE
      fragment_iab_error.visibility = View.GONE
      fragment_iab_error_pre_selected.visibility = View.GONE
    }
  }

  override fun showPaymentRefusedError(
      adyenAuthorization: @NotNull AdyenAuthorization?) {
    main_view?.visibility = View.GONE
    main_view_pre_selected?.visibility = View.GONE
    fragment_iab_error?.visibility = View.VISIBLE
    fragment_iab_error?.activity_iab_error_message?.setText(R.string.notification_payment_refused)
    fragment_iab_error_pre_selected?.visibility = View.VISIBLE
    fragment_iab_error_pre_selected?.activity_iab_error_message?.setText(
        R.string.notification_payment_refused)
  }

  override fun showGenericError() {
    main_view?.visibility = View.GONE
    main_view_pre_selected?.visibility = View.GONE
    fragment_iab_error?.visibility = View.VISIBLE
    fragment_iab_error?.activity_iab_error_message?.setText(R.string.unknown_error)
    fragment_iab_error_pre_selected?.visibility = View.VISIBLE
    fragment_iab_error_pre_selected?.activity_iab_error_message?.setText(
        R.string.unknown_error)
  }

  override fun getMorePaymentMethodsClicks(): @NotNull Observable<Any?>? {
    return RxView.clicks(more_payment_methods)
  }

  override fun showMoreMethods() {
    main_view?.let { KeyboardUtils.hideKeyboard(it) }
    main_view_pre_selected?.let { KeyboardUtils.hideKeyboard(it) }
    iabView.showPaymentMethodsView()
  }

  override fun onValidFieldStateChange(): Observable<Boolean?>? {
    return validationSubject
  }

  override fun updateButton(valid: Boolean) {
    buy_button.visibility = if (valid) View.VISIBLE else View.INVISIBLE
  }

  fun lockRotation() {
    iabView.lockRotation()
  }

  private fun finishSetupView() {
    val paddingTop = if (isPreSelected) 0 else 50
    val paddingLeft = if (isPreSelected) 0 else 24
    /*cardForm.findViewById(R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE)
    //CardEditText card_number
    cardForm.findViewById(R.id.bt_card_form_card_number)
        .setPadding(0, 4, 0, 0)
    //TextInputLayout card_number
    val textInputLayout = cardForm.findViewById(R.id.bt_card_form_card_number)
        .getParent()
        .getParent() as TextInputLayout
    textInputLayout.setPadding(paddingLeft, paddingTop, 0, 0)
    val paramsText =
        textInputLayout.layoutParams as LinearLayout.LayoutParams
    paramsText.setMargins(0, 8, 0, 0)
    textInputLayout.layoutParams = paramsText
    //CardEditText expiration date
    cardForm.findViewById(R.id.bt_card_form_expiration)
        .setPadding(0, 4, 0, 0)
    //LinearLayout expiration date
    (cardForm.findViewById(R.id.bt_card_form_expiration)
        .getParent()
        .getParent()
        .getParent() as LinearLayout).setPadding(paddingLeft, 0, 0, 0)
    //CardEditText expiration date
    cardForm.findViewById(R.id.bt_card_form_cvv)
        .setPadding(0, 4, 0, 0)*/
    presenter.sendPaymentMethodDetailsEvent(BillingAnalytics.PAYMENT_METHOD_CC)
  }

  private fun setBackListener(view: View) {
    iabView.disableBack()
    view.isFocusableInTouchMode = true
    view.requestFocus()
    view.setOnKeyListener { view1: View?, i: Int, keyEvent: KeyEvent ->
      if (keyEvent.action == KeyEvent.ACTION_DOWN
          && keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
        backButton?.accept(true)
      }
      true
    }
  }

  /*private fun getPaymentDetails(publicKey: String,
                                generationTime: String): PaymentDetails? {
    if (cvcOnly) {
      val paymentDetails = PaymentDetails(paymentMethod.getInputDetails())
      paymentDetails.fill("cardDetails.cvc", cardForm.getCvv())
      return paymentDetails
    }
    val creditCardPaymentDetails =
        CreditCardPaymentDetails(paymentMethod.getInputDetails())
    try {
      val sensitiveData = JSONObject()
      sensitiveData.put("holderName", "Checkout Shopper Placeholder")
      sensitiveData.put("number", cardForm.getCardNumber())
      sensitiveData.put("expiryMonth", cardForm.getExpirationMonth())
      sensitiveData.put("expiryYear", cardForm.getExpirationYear())
      sensitiveData.put("generationtime", generationTime)
      sensitiveData.put("cvc", cardForm.getCvv())
      creditCardPaymentDetails.fillCardToken(
          ClientSideEncrypter(publicKey).encrypt(sensitiveData.toString()))
    } catch (e: JSONException) {
      Log.e(com.asfoundation.wallet.ui.iab.AdyenAuthorizationFragment.TAG,
          "JSON Exception occurred while generating token.", e)
    } catch (e: EncrypterException) {
      Log.e(com.asfoundation.wallet.ui.iab.AdyenAuthorizationFragment.TAG,
          "EncrypterException occurred while generating token.", e)
    }
    creditCardPaymentDetails.fillStoreDetails(rememberCardCheckBox.isChecked())
    return creditCardPaymentDetails
  }*/

  private fun showProductPrice(amount: Amount) {
    val amountValue: String = amount.toString()
    // AmountUtil.format(amount, false, StringUtils.getLocale(activity)) TODO
    fiat_price.text = amountValue + ' ' + amount.currency
  }

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence? {
    val packageManager = context!!.packageManager
    val packageInfo =
        packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(lottie_transaction_success)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    lottie_transaction_success.setTextDelegate(textDelegate)
    lottie_transaction_success.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private fun showBonus() {
    bonus_layout.visibility = View.VISIBLE
    bonus_msg.visibility = View.VISIBLE
    bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonus)
  }

  private fun loadIcon() {
    compositeDisposable.add(Observable.fromCallable<Bitmap> {
      try {
        val context = context
        return@fromCallable Picasso.with(context)
            .load(iconUrl)
            .get()
      } catch (e: IOException) {
        Log.w("AdyenPaymentFragment",
            "setupPaymentMethods: Failed to load icons!")
        throw RuntimeException(e)
      }
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { payment_method_ic.setImageBitmap(it) }
        .subscribe({ }
        ) { it.printStackTrace() })
  }

  companion object {

    private const val SKU_ID_KEY = "skuId"
    private const val TRANSACTION_TYPE_KEY = "type"
    private const val ORIGIN_KEY = "origin"
    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val DOMAIN_KEY = "domain"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val PAYLOAD = "PAYLOAD"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val ICON_URL_KEY = "icon_url"

    @JvmStatic
    fun newInstance(skuId: String, transactionType: String, origin: String?,
                    paymentType: PaymentType,
                    domain: String, transactionData: String?, amount: BigDecimal,
                    currency: String?, payload: String?,
                    bonus: String?, isPreSelected: Boolean,
                    iconUrl: String?): AdyenPaymentFragment {
      val fragment = AdyenPaymentFragment()
      val bundle = Bundle()
      bundle.putString(SKU_ID_KEY, skuId)
      bundle.putString(TRANSACTION_TYPE_KEY, transactionType)
      bundle.putString(ORIGIN_KEY, origin)
      bundle.putString(PAYMENT_TYPE_KEY, paymentType.name)
      bundle.putString(DOMAIN_KEY, domain)
      bundle.putString(TRANSACTION_DATA_KEY, transactionData)
      bundle.putSerializable(AMOUNT_KEY, amount)
      bundle.putString(CURRENCY_KEY, currency)
      bundle.putString(PAYLOAD, payload)
      bundle.putString(BONUS_KEY, bonus)
      bundle.putBoolean(PRE_SELECTED_KEY, isPreSelected)
      bundle.putString(ICON_URL_KEY, iconUrl)
      fragment.arguments = bundle
      return fragment
    }
  }

  private val skuId: String by lazy {
    if (arguments!!.containsKey(SKU_ID_KEY)) {
      arguments!!.getString(SKU_ID_KEY)
    } else {
      throw IllegalArgumentException("skuId data not found")
    }
  }

  private val transactionType: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_TYPE_KEY)) {
      arguments!!.getString(TRANSACTION_TYPE_KEY)
    } else {
      throw IllegalArgumentException("transaction type data not found")
    }
  }

  private val origin: String by lazy {
    if (arguments!!.containsKey(ORIGIN_KEY)) {
      arguments!!.getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin data not found")
    }
  }

  private val paymentType: String by lazy {
    if (arguments!!.containsKey(PAYMENT_TYPE_KEY)) {
      arguments!!.getString(PAYMENT_TYPE_KEY)
    } else {
      throw IllegalArgumentException("payment type data not found")
    }
  }

  private val domain: String by lazy {
    if (arguments!!.containsKey(DOMAIN_KEY)) {
      arguments!!.getString(DOMAIN_KEY)
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }

  private val transactionData: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_DATA_KEY)) {
      arguments!!.getString(TRANSACTION_DATA_KEY)
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(AMOUNT_KEY)) {
      arguments!!.getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (arguments!!.containsKey(CURRENCY_KEY)) {
      arguments!!.getString(CURRENCY_KEY)
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val payload: String by lazy {
    if (arguments!!.containsKey(PAYLOAD)) {
      arguments!!.getString(PAYLOAD)
    } else {
      throw IllegalArgumentException("payload data not found")
    }
  }

  private val bonus: String by lazy {
    if (arguments!!.containsKey(BONUS_KEY)) {
      arguments!!.getString(BONUS_KEY)
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val isPreSelected: Boolean by lazy {
    if (arguments!!.containsKey(PRE_SELECTED_KEY)) {
      arguments!!.getBoolean(PRE_SELECTED_KEY)
    } else {
      throw IllegalArgumentException("pre selected data not found")
    }
  }

  private val iconUrl: String by lazy {
    if (arguments!!.containsKey(ICON_URL_KEY)) {
      arguments!!.getString(ICON_URL_KEY)
    } else {
      throw IllegalArgumentException("icon url data not found")
    }
  }
}