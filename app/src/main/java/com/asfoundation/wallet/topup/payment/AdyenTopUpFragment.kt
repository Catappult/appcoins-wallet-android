package com.asfoundation.wallet.topup.payment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.ui.view.RoundCornerImageView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.bdsbilling.Billing
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.*
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.view.rx.RxAlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.fragment_adyen_error.layout_support_icn
import kotlinx.android.synthetic.main.fragment_adyen_error.layout_support_logo
import kotlinx.android.synthetic.main.fragment_adyen_error.view.*
import kotlinx.android.synthetic.main.fragment_adyen_error_top_up.*
import kotlinx.android.synthetic.main.fragment_top_up.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class AdyenTopUpFragment : DaggerFragment(), AdyenTopUpView {
  @Inject
  internal lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  internal lateinit var defaultWalletInteract: FindDefaultWalletInteract

  @Inject
  internal lateinit var billing: Billing

  @Inject
  lateinit var adyenPaymentInteractor: AdyenPaymentInteractor

  @Inject
  lateinit var adyenEnvironment: Environment

  private lateinit var topUpView: TopUpActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectComponent: RedirectComponent
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<RedirectComponentModel>? = null
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private var adyenCardImageLayout: RoundCornerImageView? = null
  private var adyenSaveDetailsSwitch: SwitchCompat? = null

  private lateinit var navigator: PaymentFragmentNavigator
  private lateinit var errorDialog: RxAlertDialog
  private lateinit var networkErrorDialog: RxAlertDialog
  private lateinit var paymentRefusedDialog: RxAlertDialog
  private lateinit var presenter: AdyenTopUpPresenter

  private var keyboardTopUpRelay: PublishRelay<Boolean>? = null
  private var validationSubject: PublishSubject<Boolean>? = null
  private var isStored = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    keyboardTopUpRelay = PublishRelay.create()
    validationSubject = PublishSubject.create()
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()

    presenter =
        AdyenTopUpPresenter(this, appPackage, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable(), RedirectComponent.getReturnUrl(context!!), paymentType,
            transactionType, data.currency.fiatValue, data.currency.fiatCurrencyCode, data.currency,
            data.selectedCurrency, navigator, inAppPurchaseInteractor.billingMessagesMapper,
            adyenPaymentInteractor, bonusValue, AdyenErrorCodeMapper(), gamificationLevel)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is TopUpActivityView) { "Payment Auth fragment must be attached to TopUp activity" }
    topUpView = context
    navigator = PaymentFragmentNavigator((activity as UriNavigator?)!!, topUpView)

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }


  override fun showValues(value: BigDecimal, currency: String) {
    main_value.visibility = VISIBLE
    val fiatPrice = Formatter().format(Locale.getDefault(), "%(,.2f", value.toDouble())
    if (currentCurrency == FIAT_CURRENCY) {
      main_value.setText(fiatPrice.toString())
      main_currency_code.text = currency
      converted_value.text = "${data.currency.appcValue} ${data.currency.appcSymbol}"
    } else {
      main_value.setText(data.currency.appcValue)
      main_currency_code.text = data.currency.appcCode
      converted_value.text = "$fiatPrice $currency"
    }
  }

  override fun showLoading() {
    loading.visibility = VISIBLE
    credit_card_info_container.visibility = INVISIBLE
    button.isEnabled = false
    change_card_button.visibility = INVISIBLE
  }

  override fun showFinishingLoading() {
    topUpView.lockOrientation()
    showLoading()
  }

  override fun hideLoading() {
    loading.visibility = GONE
    button.isEnabled = false
    credit_card_info_container.visibility = VISIBLE
  }


  override fun showNetworkError() {
    if (!networkErrorDialog.isShowing) {
      topUpView.lockOrientation()
      networkErrorDialog.show()
      credit_card_info_container.visibility = INVISIBLE
    }
  }

  override fun showGenericError() {
    if (!errorDialog.isShowing) {
      topUpView.lockOrientation()
      errorDialog.show()
      credit_card_info_container.visibility = INVISIBLE
    }
  }

  override fun hideSpecificError() {
    main_currency_code.visibility = VISIBLE
    main_value.visibility = VISIBLE
    swap_value_button.visibility = VISIBLE
    swap_value_label.visibility = VISIBLE
    top_separator_topup.visibility = VISIBLE
    bot_separator.visibility = VISIBLE
    converted_value.visibility = VISIBLE
    button.visibility = VISIBLE

    if (isStored) {
      change_card_button.visibility = VISIBLE
    } else {
      change_card_button.visibility = INVISIBLE
    }

    payment_container.visibility = VISIBLE
    credit_card_info_container.visibility = VISIBLE
    fragment_adyen_error?.visibility = GONE

    topUpView.unlockRotation()
  }

  override fun showSpecificError(@StringRes stringRes: Int) {
    topUpView.lockOrientation()
    loading.visibility = GONE
    if (isStored) {
      change_card_button.visibility = VISIBLE
    } else {
      change_card_button.visibility = INVISIBLE
    }
    payment_container.visibility = INVISIBLE

    //Header
    main_currency_code.visibility = INVISIBLE
    main_value.visibility = INVISIBLE
    swap_value_button.visibility = INVISIBLE
    swap_value_label.visibility = INVISIBLE
    top_separator_topup.visibility = INVISIBLE
    bot_separator.visibility = INVISIBLE
    converted_value.visibility = INVISIBLE
    button.visibility = GONE


    val message = getString(stringRes)
    fragment_adyen_error?.error_message?.text = message
    fragment_adyen_error?.visibility = VISIBLE

  }

  override fun showCvvError() {
    topUpView.lockOrientation()
    loading.visibility = GONE
    button.isEnabled = false
    if (isStored) {
      change_card_button.visibility = VISIBLE
    } else {
      change_card_button.visibility = INVISIBLE
    }
    credit_card_info_container.visibility = VISIBLE

    adyenSecurityCodeLayout.error = getString(R.string.purchase_card_error_CVV)
  }

  override fun getTryAgainClicks() = RxView.clicks(try_again)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))
  }

  override fun topUpButtonClicked() = RxView.clicks(button)

  override fun errorDismisses(): Observable<Any> {
    return Observable.merge(networkErrorDialog.dismisses(),
            paymentRefusedDialog.dismisses(), errorDialog.dismisses())
        .doOnNext { topUpView.unlockRotation() }
        .map { Any() }
  }

  override fun errorCancels(): Observable<Any> {
    return Observable.merge(networkErrorDialog.cancels(),
            paymentRefusedDialog.cancels(), errorDialog.cancels())
        .doOnNext { topUpView.unlockRotation() }
        .map { Any() }
  }

  override fun errorPositiveClicks(): Observable<Any> {
    return Observable.merge(networkErrorDialog.positiveClicks(),
            paymentRefusedDialog.positiveClicks(), errorDialog.positiveClicks())
        .doOnNext { topUpView.unlockRotation() }
        .map { Any() }
  }

  override fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean, forget: Boolean, savedInstanceState: Bundle?) {
    this.isStored = isStored
    val color = ResourcesCompat.getColor(resources, R.color.btn_end_gradient_color, null)
    adyenCardNumberLayout.boxStrokeColor = color
    adyenExpiryDateLayout.boxStrokeColor = color
    adyenSecurityCodeLayout.boxStrokeColor = color
    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentMethod, forget, savedInstanceState)
    setStoredPaymentInformation(isStored)
  }

  private fun prepareCardComponent(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod, forget: Boolean,
      savedInstanceState: Bundle?) {
    if (forget) viewModelStore.clear()
    val cardComponent =
        CardComponent.PROVIDER.get(this, paymentMethod, cardConfiguration)
    if (forget) clearFields()
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this, androidx.lifecycle.Observer {
      if (it != null && it.isValid) {
        button.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          paymentDataSubject?.onNext(
              AdyenCardWrapper(paymentMethod, adyenSaveDetailsSwitch?.isChecked ?: false))
        }
      } else {
        button.isEnabled = false
      }
    })
    if (!forget) {
      getFieldValues(savedInstanceState)
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    if (isStored) {
      adyenCardNumberLayout.visibility = GONE
      adyenExpiryDateLayout.visibility = GONE
      adyenCardImageLayout?.visibility = GONE
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    } else {
      adyenCardNumberLayout.visibility = VISIBLE
      adyenExpiryDateLayout.visibility = VISIBLE
      adyenCardImageLayout?.visibility = VISIBLE
      change_card_button?.visibility = GONE
      change_card_button_pre_selected?.visibility = GONE
    }
  }

  override fun setRedirectComponent(uid: String, action: Action) {
    redirectComponent = RedirectComponent.PROVIDER.get(this)
    redirectComponent.observe(this, Observer {
      paymentDetailsSubject?.onNext(RedirectComponentModel(uid, it.details!!, it.paymentData))
    })
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun getPaymentDetails() = paymentDetailsSubject!!

  override fun forgetCardClick(): Observable<Any> {
    return if (change_card_button != null) RxView.clicks(change_card_button)
    else RxView.clicks(change_card_button_pre_selected)
  }

  override fun submitUriResult(uri: Uri) = redirectComponent.handleRedirectResponse(uri)

  override fun updateTopUpButton(valid: Boolean) {
    button.isEnabled = valid
  }

  override fun cancelPayment() = topUpView.cancelPayment()

  override fun setFinishingPurchase() = topUpView.setFinishingPurchase()

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text =
          adyenCardNumberLayout.editText?.text
      adyen_card_form_pre_selected_number?.visibility = VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardImageLayout?.drawable)
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyen_card_form_pre_selected_number?.visibility = GONE
      payment_method_ic?.visibility = GONE
    }
  }

  private fun getFieldValues(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      adyenCardNumberLayout.editText?.setText(it.getString(CARD_NUMBER_KEY, ""))
      adyenExpiryDateLayout.editText?.setText(it.getString(EXPIRY_DATE_KEY, ""))
      adyenSecurityCodeLayout.editText?.setText(it.getString(CVV_KEY, ""))
      adyenSaveDetailsSwitch?.isChecked = it.getBoolean(SAVE_DETAILS_KEY, false)
      it.clear()
    }
  }

  private fun clearFields() {
    adyenCardNumberLayout.editText?.text = null
    adyenCardNumberLayout.editText?.isEnabled = true
    adyenExpiryDateLayout.editText?.text = null
    adyenExpiryDateLayout.editText?.isEnabled = true
    adyenSecurityCodeLayout.editText?.text = null
    adyenCardNumberLayout.requestFocus()
    adyenSecurityCodeLayout.error = null
  }

  private fun setupUi() {
    credit_card_info_container.visibility = INVISIBLE
    button.isEnabled = false
    button.setText(R.string.topup_home_button)
    setupAdyenLayouts()

    if (paymentType == PaymentType.CARD.name) setupCardConfiguration()

    setupDialogs()
    topUpView.showToolbar()
    main_value.visibility = INVISIBLE
  }

  private fun setupCardConfiguration() {
    val cardConfigurationBuilder =
        CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)

    cardConfiguration = cardConfigurationBuilder.let {
      it.setEnvironment(adyenEnvironment)
      it.build()
    }
  }

  private fun setupAdyenLayouts() {
    adyenCardNumberLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_cardNumber)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_cardNumber)
    adyenExpiryDateLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_expiryDate)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_expiryDate)
    adyenSecurityCodeLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_securityCode)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_securityCode)
    adyenCardImageLayout = adyen_card_form_pre_selected?.findViewById(R.id.cardBrandLogo_imageView)
        ?: adyen_card_form?.findViewById(R.id.cardBrandLogo_imageView)
    adyenSaveDetailsSwitch =
        adyen_card_form_pre_selected?.findViewById(R.id.switch_storePaymentMethod)
            ?: adyen_card_form?.findViewById(R.id.switch_storePaymentMethod)

    adyenSaveDetailsSwitch?.run {

      val params: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
      params.topMargin = 8

      layoutParams = params
      isChecked = true
      textSize = 15f
      text = getString(R.string.dialog_credit_card_remember)

    }

    val height = resources.getDimensionPixelSize(R.dimen.adyen_text_input_layout_height)

    adyenCardNumberLayout.minimumHeight = height
    adyenExpiryDateLayout.minimumHeight = height
    adyenSecurityCodeLayout.minimumHeight = height
  }

  private fun setupDialogs() {
    errorDialog = RxAlertDialog.Builder(context)
        .setMessage(R.string.unknown_error)
        .setPositiveButton(R.string.ok)
        .build()

    networkErrorDialog =
        RxAlertDialog.Builder(context)
            .setMessage(R.string.notification_no_network_poa)
            .setPositiveButton(R.string.ok)
            .build()

    paymentRefusedDialog =
        RxAlertDialog.Builder(context)
            .setMessage(R.string.notification_payment_refused)
            .setPositiveButton(R.string.ok)
            .build()
  }

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(it) }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    validationSubject = null
    keyboardTopUpRelay = null
    paymentDataSubject = null
    paymentDetailsSubject = null
    super.onDestroy()
  }

  private val appPackage: String by lazy {
    if (activity != null) {
      activity!!.packageName
    } else {
      throw IllegalArgumentException("previous app package name not found")
    }
  }

  private val data: TopUpData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA) as TopUpData
    } else {
      throw IllegalArgumentException("previous payment data not found")
    }
  }

  private val paymentType: String by lazy {
    if (arguments!!.containsKey(PAYMENT_TYPE)) {
      arguments!!.getString(PAYMENT_TYPE)
    } else {
      throw IllegalArgumentException("Payment Type not found")
    }
  }

  private val transactionType: String by lazy {
    if (arguments!!.containsKey(PAYMENT_TRANSACTION_TYPE)) {
      arguments!!.getString(PAYMENT_TRANSACTION_TYPE)
    } else {
      throw IllegalArgumentException("Transaction type not found")
    }
  }

  private val currentCurrency: String by lazy {
    if (arguments!!.containsKey(PAYMENT_CURRENT_CURRENCY)) {
      arguments!!.getString(PAYMENT_CURRENT_CURRENCY)
    } else {
      throw IllegalArgumentException("Payment main currency not found")
    }
  }

  private val bonusValue: String by lazy {
    if (arguments!!.containsKey(BONUS)) {
      arguments!!.getString(BONUS)
    } else {
      throw IllegalArgumentException("Bonus not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (arguments!!.containsKey(GAMIFICATION_LEVEL)) {
      arguments!!.getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }

  companion object {

    private const val PAYMENT_TYPE = "paymentType"
    private const val PAYMENT_TRANSACTION_TYPE = "transactionType"
    private const val PAYMENT_DATA = "data"
    private const val PAYMENT_CURRENT_CURRENCY = "currentCurrency"
    private const val BONUS = "bonus"
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"
    private const val GAMIFICATION_LEVEL = "gamification_level"

    fun newInstance(paymentType: PaymentType, data: TopUpData, currentCurrency: String,
                    transactionType: String, bonusValue: String,
                    gamificationLevel: Int): AdyenTopUpFragment {
      val bundle = Bundle()
      val fragment = AdyenTopUpFragment()
      bundle.apply {
        putString(PAYMENT_TYPE, paymentType.name)
        putString(PAYMENT_TRANSACTION_TYPE, transactionType)
        putSerializable(PAYMENT_DATA, data)
        putString(PAYMENT_CURRENT_CURRENCY, currentCurrency)
        putString(BONUS, bonusValue)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        fragment.arguments = this
      }
      return fragment
    }
  }
}