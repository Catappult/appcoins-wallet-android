package com.asfoundation.wallet.billing.adyen

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.ui.view.RoundCornerImageView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressFragment.Companion.BILLING_ADDRESS_MODEL
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.TransactionPaymentData
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.adyen_credit_card_layout.*
import kotlinx.android.synthetic.main.adyen_credit_card_layout.fragment_credit_card_authorization_progress_bar
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_adyen_error.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import kotlinx.android.synthetic.main.support_error_layout.*
import kotlinx.android.synthetic.main.support_error_layout.view.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import javax.inject.Inject

class AdyenPaymentFragment : DaggerFragment(), AdyenPaymentView {

  @Inject
  lateinit var adyenEnvironment: Environment

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: AdyenPaymentPresenter

  private lateinit var iabView: IabView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<AdyenComponentResponseModel>? = null
  private var adyen3DSErrorSubject: PublishSubject<String>? = null
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private var adyenCardImageLayout: RoundCornerImageView? = null
  private var adyenSaveDetailsSwitch: SwitchCompat? = null
  private var isStored = false
  private var billingAddressInput: PublishSubject<Boolean>? = null
  private var billingAddressModel: BillingAddressModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    billingAddressInput = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = if (isPreselected()) {
      inflater.inflate(R.layout.adyen_credit_card_pre_selected, container, false)
    } else {
      inflater.inflate(R.layout.adyen_credit_card_layout, container, false)
    }
    if (arguments?.getBoolean(USE_BOTTOM_SHEET_KEY) == true) {
      val scrollView = view.findViewById<ScrollView>(R.id.cc_info_view)
      scrollView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
    }
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun setup3DSComponent() {
    adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(this)
    adyen3DS2Component.observe(this, Observer {
      paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
    })
    adyen3DS2Component.observeErrors(this, Observer {
      adyen3DSErrorSubject?.onNext(it.errorMessage)
    })
  }

  override fun setupUi(data: AdyenPaymentData) {
    setupAdyenLayouts()
    setupTransactionCompleteAnimation(data.bonus)
    handleBuyButtonText(data.paymentData.type)
    if (data.paymentType == PaymentType.CARD.name) setupCardConfiguration()

    handlePreSelectedView(data.isPreselected, data.bonus)
    handleBonusAnimation(data.bonus)

    showProduct(data.paymentData.domain, data.paymentData.skuDescription,
        data.paymentData.appcAmount)
  }

  override fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean, forget: Boolean, savedInstance: Bundle?) {
    this.isStored = isStored
    buy_button.visibility = VISIBLE
    cancel_button.visibility = VISIBLE

    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentMethod, forget, savedInstance)
    setStoredPaymentInformation(isStored)
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.apply {
      putString(CARD_NUMBER_KEY, adyenCardNumberLayout.editText?.text.toString())
      putString(EXPIRY_DATE_KEY, adyenExpiryDateLayout.editText?.text.toString())
      putString(CVV_KEY, adyenSecurityCodeLayout.editText?.text.toString())
      putBoolean(SAVE_DETAILS_KEY, adyenSaveDetailsSwitch?.isChecked ?: false)
    }
    presenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "adyen payment fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == BILLING_ADDRESS_REQUEST_CODE && resultCode == BILLING_ADDRESS_SUCCESS_CODE) {
      main_view_pre_selected?.visibility = VISIBLE
      main_view?.visibility = VISIBLE
      val billingAddressModel =
          data!!.getSerializableExtra(BILLING_ADDRESS_MODEL) as BillingAddressModel
      this.billingAddressModel = billingAddressModel
      billingAddressInput?.onNext(true)
    } else {
      showMoreMethods()
    }
  }

  override fun billingAddressInput(): Observable<Boolean> {
    return billingAddressInput!!
  }

  override fun retrieveBillingAddressData() = billingAddressModel

  override fun getAnimationDuration() = lottie_transaction_success.duration

  private fun showProduct(domain: String, skuDescription: String, appcAmount: BigDecimal) {
    try {
      app_icon?.setImageDrawable(context!!.packageManager
          .getApplicationIcon(domain))
      app_name?.text = getApplicationName(domain)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    app_sku_description?.text = skuDescription
    val appcValue = formatter.formatCurrency(appcAmount, WalletCurrency.APPCOINS)
    appc_price.text = appcValue.plus(" " + WalletCurrency.APPCOINS.symbol)
  }

  override fun showLoading(isPreSelected: Boolean, bonus: String) {
    fragment_credit_card_authorization_progress_bar.visibility = VISIBLE
    if (isPreSelected) {
      payment_methods?.visibility = INVISIBLE
    } else {
      if (bonus.isNotEmpty()) {
        bonus_layout.visibility = INVISIBLE
        bonus_msg.visibility = INVISIBLE
      }
      adyen_card_form.visibility = INVISIBLE
      change_card_button.visibility = INVISIBLE
      cancel_button.visibility = INVISIBLE
      buy_button.visibility = INVISIBLE
      fiat_price_skeleton.visibility = GONE
      appc_price_skeleton.visibility = GONE
    }
  }

  override fun hideLoadingAndShowView(isPreSelected: Boolean, bonus: String) {
    fragment_credit_card_authorization_progress_bar?.visibility = INVISIBLE
    if (isPreSelected) {
      payment_methods?.visibility = VISIBLE
    } else {
      showBonus(bonus)
      adyen_card_form.visibility = VISIBLE
      cancel_button.visibility = VISIBLE
    }
  }

  override fun showNetworkError() {
    showSpecificError(R.string.notification_no_network_poa)
  }

  override fun backEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
        .mergeWith(iabView.backButtonPress())
  }

  override fun showSuccess(isPreSelected: Boolean) {
    fragment_credit_card_authorization_progress_bar?.visibility = GONE
    if (isPreSelected) {
      main_view?.visibility = GONE
      main_view_pre_selected?.visibility = GONE
    } else {
      credit_card_info.visibility = GONE
      fragment_adyen_error?.visibility = GONE
      fragment_adyen_error_pre_selected?.visibility = GONE
    }
    iab_activity_transaction_completed.visibility = VISIBLE
  }

  override fun showGenericError() {
    showSpecificError(R.string.unknown_error)
  }

  override fun showVerification() = iabView.showVerification()

  override fun showBillingAddress(value: BigDecimal, currency: String, bonus: String,
                                  appcAmount: BigDecimal) {
    main_view?.visibility = GONE
    main_view_pre_selected?.visibility = GONE
    iabView.showBillingAddress(value, currency, bonus, appcAmount, this,
        adyenSaveDetailsSwitch?.isChecked ?: true, isStored)
  }

  override fun showSpecificError(@StringRes stringRes: Int) {
    fragment_credit_card_authorization_progress_bar?.visibility = INVISIBLE
    cancel_button?.visibility = GONE
    buy_button?.visibility = GONE
    payment_methods?.visibility = VISIBLE
    bonus_layout_pre_selected?.visibility = GONE
    bonus_msg_pre_selected?.visibility = GONE
    bonus_layout?.visibility = GONE
    bonus_msg?.visibility = GONE
    more_payment_methods?.visibility = GONE
    adyen_card_form?.visibility = GONE
    layout_pre_selected?.visibility = GONE
    change_card_button?.visibility = GONE
    change_card_button_pre_selected?.visibility = GONE

    error_buttons?.visibility = VISIBLE
    dialog_buy_buttons_error?.visibility = VISIBLE

    val message = getString(stringRes)

    fragment_adyen_error?.error_message?.text = message
    fragment_adyen_error_pre_selected?.error_message?.text = message
    fragment_adyen_error?.visibility = VISIBLE
    fragment_adyen_error_pre_selected?.visibility = VISIBLE
  }

  override fun showCvvError(isPreSelected: Boolean, bonus: String) {
    iabView.unlockRotation()
    hideLoadingAndShowView(isPreSelected, bonus)
    if (isStored) {
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    }
    buy_button?.visibility = VISIBLE
    buy_button?.isEnabled = false
    adyenSecurityCodeLayout.error = getString(R.string.purchase_card_error_CVV)
  }

  override fun getMorePaymentMethodsClicks() = RxView.clicks(more_payment_methods)

  override fun showMoreMethods() {
    main_view?.let { KeyboardUtils.hideKeyboard(it) }
    main_view_pre_selected?.let { KeyboardUtils.hideKeyboard(it) }
    iabView.unlockRotation()
    iabView.showPaymentMethodsView()
  }

  override fun setupRedirectComponent() {
    redirectComponent = RedirectComponent.PROVIDER.get(this)
    redirectComponent.observe(this, Observer {
      paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
    })
  }


  override fun handle3DSAction(action: Action) {
    adyen3DS2Component.handleAction(activity!!, action)
  }

  override fun onAdyen3DSError(): Observable<String> = adyen3DSErrorSubject!!

  override fun forgetCardClick(): Observable<Any> {
    return if (change_card_button != null) RxView.clicks(change_card_button)
    else RxView.clicks(change_card_button_pre_selected)
  }

  override fun showProductPrice(amount: String, currencyCode: String) {
    fiat_price.text = "$amount $currencyCode"
    fiat_price_skeleton.visibility = GONE
    appc_price_skeleton.visibility = GONE
    fiat_price.visibility = VISIBLE
    appc_price.visibility = VISIBLE
  }

  override fun adyenErrorBackClicks() = RxView.clicks(error_back)

  override fun adyenErrorCancelClicks() = RxView.clicks(error_cancel)

  override fun errorDismisses() = RxView.clicks(error_dismiss)

  override fun buyButtonClicked() = RxView.clicks(buy_button)

  override fun close(bundle: Bundle?) = iabView.close(bundle)

  override fun submitUriResult(uri: Uri) = redirectComponent.handleRedirectResponse(uri)

  override fun getPaymentDetails(): Observable<AdyenComponentResponseModel> =
      paymentDetailsSubject!!

  override fun getAdyenSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getAdyenSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun lockRotation() = iabView.lockRotation()

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
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

    adyenCardNumberLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenExpiryDateLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenSecurityCodeLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

    adyenSaveDetailsSwitch?.run {

      val params: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
      params.topMargin = 2

      layoutParams = params
      isChecked = true
      textSize = 14f
      text = getString(R.string.dialog_credit_card_remember)
    }

    val height = resources.getDimensionPixelSize(R.dimen.adyen_text_input_layout_height)

    adyenCardNumberLayout.minimumHeight = height
    adyenCardNumberLayout.errorIconDrawable = null
    adyenExpiryDateLayout.minimumHeight = height
    adyenSecurityCodeLayout.minimumHeight = height
  }

  private fun setupCardConfiguration() {
    val cardConfigurationBuilder =
        CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)

    cardConfiguration = cardConfigurationBuilder.let {
      it.setEnvironment(adyenEnvironment)
      it.build()
    }
  }

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence? {
    val packageManager = context!!.packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun setupTransactionCompleteAnimation(bonus: String) {
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

  private fun showBonus(bonus: String) {
    if (bonus.isNotEmpty()) {
      bonus_layout?.visibility = VISIBLE
      bonus_layout_pre_selected?.visibility = VISIBLE
      bonus_msg?.visibility = VISIBLE
      bonus_msg_pre_selected?.visibility = VISIBLE
      bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      bonus_layout?.visibility = GONE
      bonus_layout_pre_selected?.visibility = GONE
      bonus_msg?.visibility = GONE
      bonus_msg_pre_selected?.visibility = GONE
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    if (isStored) {
      adyenCardNumberLayout.visibility = GONE
      adyenExpiryDateLayout.visibility = GONE
      adyenCardImageLayout?.visibility = GONE
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyenCardNumberLayout.visibility = VISIBLE
      adyenExpiryDateLayout.visibility = VISIBLE
      adyenCardImageLayout?.visibility = VISIBLE
      change_card_button?.visibility = GONE
      change_card_button_pre_selected?.visibility = GONE
    }

  }

  private fun prepareCardComponent(
      paymentMethodEntity: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      forget: Boolean,
      savedInstanceState: Bundle?) {
    if (forget) viewModelStore.clear()
    val cardComponent = CardComponent.PROVIDER.get(this, paymentMethodEntity, cardConfiguration)
    if (forget) clearFields()
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this, Observer {
      adyenSecurityCodeLayout.error = null
      if (it != null && it.isValid) {
        buy_button?.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          val supportedShopperInteractions =
              if (paymentMethodEntity is StoredPaymentMethod) paymentMethodEntity.supportedShopperInteractions else emptyList()
          paymentDataSubject?.onNext(
              AdyenCardWrapper(paymentMethod, adyenSaveDetailsSwitch?.isChecked ?: false, hasCvc,
                  supportedShopperInteractions))
        }
      } else {
        buy_button?.isEnabled = false
      }
    })
    if (!forget) {
      getFieldValues(savedInstanceState)
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

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text = adyenCardNumberLayout.editText?.text
      adyen_card_form_pre_selected_number?.visibility = VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardImageLayout?.drawable)
    } else {
      adyen_card_form_pre_selected_number?.visibility = GONE
      payment_method_ic?.visibility = GONE
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

  private fun handleBonusAnimation(bonus: String) {
    if (StringUtils.isNotBlank(bonus)) {
      lottie_transaction_success.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation(bonus)
    } else {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
  }

  private fun handlePreSelectedView(isPreSelected: Boolean, bonus: String) {
    if (!isPreSelected) {
      cancel_button.setText(R.string.back_button)
      iabView.disableBack()
    }
    showBonus(bonus)
  }

  private fun handleBuyButtonText(type: String) {
    if (type.equals(TransactionData.TransactionType.DONATION.name, ignoreCase = true)) {
      buy_button.setText(R.string.action_donate)
    } else {
      buy_button.setText(R.string.action_buy)
    }
  }

  private fun isPreselected() = arguments!!.getBoolean(PRE_SELECTED_KEY, false)

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    paymentDataSubject = null
    paymentDetailsSubject = null
    adyen3DSErrorSubject = null
    billingAddressInput = null
    super.onDestroy()
  }

  companion object {

    const val PAYMENT_TYPE_KEY = "payment_type"
    const val BONUS_KEY = "bonus"
    const val PRE_SELECTED_KEY = "pre_selected"
    const val GAMIFICATION_LEVEL = "gamification_level"
    const val TRANSACTION_PAYMENT_DATA = "transaction_payment_data"
    private const val USE_BOTTOM_SHEET_KEY = "use_bottom_sheet"
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"

    @JvmStatic
    fun newInstance(paymentType: PaymentType, bonus: String?, isPreSelected: Boolean,
                    gamificationLevel: Int, transactionData: TransactionPaymentData,
                    shouldUseBottomSheet: Boolean = false): AdyenPaymentFragment {
      val fragment = AdyenPaymentFragment()
      fragment.arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(BONUS_KEY, bonus)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putSerializable(TRANSACTION_PAYMENT_DATA, transactionData)
        putBoolean(USE_BOTTOM_SHEET_KEY, shouldUseBottomSheet)
      }
      return fragment
    }
  }
}