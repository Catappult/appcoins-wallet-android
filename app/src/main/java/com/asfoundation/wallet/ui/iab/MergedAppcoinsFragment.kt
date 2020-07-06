package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.appcoins_radio_button.*
import kotlinx.android.synthetic.main.credits_radio_button.*
import kotlinx.android.synthetic.main.credits_radio_button.view.*
import kotlinx.android.synthetic.main.dialog_buy_app_info_header.app_icon
import kotlinx.android.synthetic.main.dialog_buy_app_info_header.app_name
import kotlinx.android.synthetic.main.dialog_buy_app_info_header.app_sku_description
import kotlinx.android.synthetic.main.dialog_buy_buttons.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.merged_appcoins_layout.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.support_error_layout.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import javax.inject.Inject

class MergedAppcoinsFragment : DaggerFragment(), MergedAppcoinsView {

  companion object {
    private const val FIAT_AMOUNT_KEY = "fiat_amount"
    private const val FIAT_CURRENCY_KEY = "currency_amount"
    private const val BONUS_KEY = "bonus"
    private const val APP_NAME_KEY = "app_name"
    private const val PRODUCT_NAME_KEY = "product_name"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val APPC_ENABLED_KEY = "appc_enabled"
    private const val CREDITS_ENABLED_KEY = "credits_enabled"
    private const val IS_BDS_KEY = "is_bds"
    private const val IS_DONATION_KEY = "is_donation"
    private const val SKU_ID = "sku_id"
    private const val TRANSACTION_TYPE = "transaction_type"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    const val APPC = "appcoins"
    const val CREDITS = "credits"

    @JvmStatic
    fun newInstance(fiatAmount: BigDecimal,
                    currency: String, bonus: String, appName: String,
                    productName: String?,
                    appcAmount: BigDecimal, appcEnabled: Boolean,
                    creditsEnabled: Boolean, isBds: Boolean,
                    isDonation: Boolean, skuId: String?, transactionType: String,
                    gamificationLevel: Int): Fragment {
      val fragment = MergedAppcoinsFragment()
      val bundle = Bundle().apply {
        putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
        putString(FIAT_CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putString(APP_NAME_KEY, appName)
        putString(PRODUCT_NAME_KEY, productName)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putBoolean(APPC_ENABLED_KEY, appcEnabled)
        putBoolean(CREDITS_ENABLED_KEY, creditsEnabled)
        putBoolean(IS_BDS_KEY, isBds)
        putBoolean(IS_DONATION_KEY, isDonation)
        putString(SKU_ID, skuId)
        putString(TRANSACTION_TYPE, transactionType)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
      }
      fragment.arguments = bundle
      return fragment
    }
  }

  private lateinit var mergedAppcoinsPresenter: MergedAppcoinsPresenter
  private var paymentSelectionSubject: PublishSubject<String>? = null
  private var onBackPressSubject: PublishSubject<Any>? = null
  private lateinit var iabView: IabView

  @Inject
  lateinit var billingAnalytics: BillingAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mergedAppcoinsInteract: MergedAppcoinsInteract

  private val fiatAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(FIAT_AMOUNT_KEY)) {
      arguments!!.getSerializable(FIAT_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }
  private val currency: String by lazy {
    if (arguments!!.containsKey(FIAT_CURRENCY_KEY)) {
      arguments!!.getString(FIAT_CURRENCY_KEY)
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val bonus: String by lazy {
    if (arguments!!.containsKey(BONUS_KEY)) {
      arguments!!.getString(BONUS_KEY)
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val appName: String by lazy {
    if (arguments!!.containsKey(APP_NAME_KEY)) {
      arguments!!.getString(APP_NAME_KEY)
    } else {
      throw IllegalArgumentException("app name data not found")
    }
  }

  private val productName: String? by lazy {
    if (arguments!!.containsKey(PRODUCT_NAME_KEY)) {
      arguments!!.getString(PRODUCT_NAME_KEY)
    } else {
      throw IllegalArgumentException("product name data not found")
    }
  }

  private val appcAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(APPC_AMOUNT_KEY)) {
      arguments!!.getSerializable(APPC_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("appc data not found")
    }
  }

  private val appcEnabled: Boolean by lazy {
    if (arguments!!.containsKey(APPC_ENABLED_KEY)) {
      arguments!!.getBoolean(APPC_ENABLED_KEY)
    } else {
      throw IllegalArgumentException("appc enable data not found")
    }
  }

  private val creditsEnabled: Boolean by lazy {
    if (arguments!!.containsKey(CREDITS_ENABLED_KEY)) {
      arguments!!.getBoolean(CREDITS_ENABLED_KEY)
    } else {
      throw IllegalArgumentException("credits enable data not found")
    }
  }

  private val isBds: Boolean by lazy {
    if (arguments!!.containsKey(IS_BDS_KEY)) {
      arguments!!.getBoolean(IS_BDS_KEY)
    } else {
      throw IllegalArgumentException("is bds data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (arguments!!.containsKey(IS_DONATION_KEY)) {
      arguments!!.getBoolean(IS_DONATION_KEY)
    } else {
      throw IllegalArgumentException("is donation data not found")
    }
  }

  private val skuId: String? by lazy {
    arguments!!.getString(SKU_ID)
  }

  private val transactionType: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_TYPE)) {
      arguments!!.getString(TRANSACTION_TYPE)
    } else {
      throw IllegalArgumentException("transaction type data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (arguments!!.containsKey(GAMIFICATION_LEVEL)) {
      arguments!!.getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level not found")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val navigator = FragmentNavigator(activity as UriNavigator?, iabView)
    paymentSelectionSubject = PublishSubject.create()
    onBackPressSubject = PublishSubject.create()
    mergedAppcoinsPresenter = MergedAppcoinsPresenter(this, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io(), billingAnalytics,
        formatter, mergedAppcoinsInteract, gamificationLevel, navigator)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Merged Appcoins fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.merged_appcoins_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setHeaderInformation()
    buy_button.text = setBuyButtonText()
    cancel_button.text = getString(R.string.back_button)
    setPaymentInformation()
    setBonus()
    setBackListener(view)
    mergedAppcoinsPresenter.present()
  }

  override fun showLoading() {
    payment_methods?.visibility = INVISIBLE
    loading_view?.visibility = VISIBLE
  }

  override fun hideLoading() {
    loading_view?.visibility = GONE
  }

  override fun showPaymentMethods() {
    payment_methods?.visibility = VISIBLE
  }

  private fun setBuyButtonText(): String {
    return if (isDonation) getString(R.string.action_donate) else getString(R.string.action_buy)
  }

  private fun setBonus() {
    if (bonus.isNotEmpty()) {
      //Build string for both landscape (header) and portrait (radio button) bonus layout
      appcoins_radio?.bonus_value?.text =
          getString(R.string.gamification_purchase_header_part_2, bonus)
      bonus_value?.text = getString(R.string.gamification_purchase_header_part_2, bonus)

      //Set visibility for both landscape (header) and portrait (radio button) bonus layout
      if (appcoins_radio_button.isChecked) {
        bonus_layout?.visibility = VISIBLE
        bonus_msg?.visibility = VISIBLE
      }
      appcoins_bonus_layout?.visibility = VISIBLE
    } else {
      appcoins_bonus_layout?.visibility = GONE
    }
  }

  private fun setHeaderInformation() {
    if (isDonation) {
      app_name.text = getString(R.string.item_donation)
      app_sku_description.text = getString(R.string.item_donation)
    } else {
      app_name.text = getApplicationName(appName)
      app_sku_description.text = productName
    }
    try {
      app_icon.setImageDrawable(context!!.packageManager
          .getApplicationIcon(appName))
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    val appcText = formatter.formatCurrency(appcAmount, WalletCurrency.APPCOINS)
        .plus(" " + WalletCurrency.APPCOINS.symbol)
    val fiatText = formatter.formatCurrency(fiatAmount, WalletCurrency.FIAT)
        .plus(" $currency")
    fiat_price.text = fiatText
    appc_price.text = appcText
    fiat_price_skeleton.visibility = GONE
    appc_price_skeleton.visibility = GONE
    fiat_price.visibility = VISIBLE
    appc_price.visibility = VISIBLE
  }

  private fun setPaymentInformation() {
    if (appcEnabled) {
      appcoins_radio.setOnClickListener { appcoins_radio_button.isChecked = true }
      appcoins_radio_button.setOnCheckedChangeListener { _, checked ->
        if (checked) paymentSelectionSubject?.onNext(APPC)
        credits_radio_button.isChecked = !checked
      }
      appcoins_radio_button.isEnabled = true
    } else {
      appcoins_radio.message.text = getString(R.string.purchase_appcoins_noavailable_body)
      appcoins_radio.title.setTextColor(
          ContextCompat.getColor(context!!, R.color.btn_disable_snd_color))
      appcoins_radio.message.setTextColor(
          ContextCompat.getColor(context!!, R.color.btn_disable_snd_color))
      appcoins_bonus_layout?.setBackgroundResource(R.drawable.disable_bonus_img_background)
      appcoins_radio.message.visibility = VISIBLE
      appc_balances_group.visibility = INVISIBLE
    }
    if (creditsEnabled) {
      credits_radio.setOnClickListener { credits_radio_button.isChecked = true }
      credits_radio_button.setOnCheckedChangeListener { _, checked ->
        if (checked) paymentSelectionSubject?.onNext(CREDITS)
        appcoins_radio_button.isChecked = !checked
      }
      credits_radio_button.isEnabled = true
      credits_radio_button.isChecked = true
    } else {
      appcoins_radio_button.isChecked = true
      credits_radio.message.text = getString(R.string.purchase_appcoins_credits_noavailable_body)
      credits_radio.title.setTextColor(resources.getColor(R.color.btn_disable_snd_color))
      credits_radio.message.setTextColor(resources.getColor(R.color.btn_disable_snd_color))
      credits_radio.message.visibility = VISIBLE
      credits_balances_group.visibility = INVISIBLE
    }
  }

  private fun getApplicationName(appPackage: String): CharSequence {
    val packageManager = context!!.packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun setBackListener(view: View) {
    iabView.disableBack()
    view.isFocusableInTouchMode = true
    view.requestFocus()
    view.setOnKeyListener { _, keyCode, keyEvent ->
      if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
        onBackPressSubject?.onNext("")
      }
      true
    }
  }

  private fun getSelectedPaymentMethod(): String {
    var selectedPaymentMethod = ""
    if (appcoins_radio_button.isChecked) selectedPaymentMethod = APPC
    if (credits_radio_button.isChecked) selectedPaymentMethod = CREDITS
    return selectedPaymentMethod
  }

  override fun buyClick(): Observable<PaymentInfoWrapper> {
    return RxView.clicks(buy_button)
        .map {
          PaymentInfoWrapper(appName, skuId, appcAmount.toString(), getSelectedPaymentMethod(),
              transactionType)
        }
  }

  override fun backClick(): Observable<PaymentInfoWrapper> {
    return RxView.clicks(cancel_button)
        .map {
          PaymentInfoWrapper(appName, skuId, appcAmount.toString(), getSelectedPaymentMethod(),
              transactionType)
        }
  }

  override fun backPressed(): Observable<PaymentInfoWrapper> {
    return onBackPressSubject!!.map {
      PaymentInfoWrapper(appName, skuId, appcAmount.toString(), getSelectedPaymentMethod(),
          transactionType)
    }
  }


  override fun getPaymentSelection(): Observable<String> {
    return paymentSelectionSubject!!
  }

  override fun hideBonus() {
    bonus_layout?.visibility = INVISIBLE
    bonus_msg?.visibility = INVISIBLE
  }

  override fun showBonus() {
    if (bonus.isNotEmpty()) {
      val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation)
      animation.duration = 250
      bonus_layout?.visibility = VISIBLE
      bonus_layout?.startAnimation(animation)
      bonus_msg?.visibility = VISIBLE
    } else {
      bonus_layout?.visibility = GONE
      bonus_msg?.visibility = GONE
    }
  }

  override fun showError(@StringRes errorMessage: Int) {
    payment_method_main_view.visibility = GONE
    error_dismiss.text = getString(R.string.ok)
    error_message.text = getString(errorMessage)
    merged_error_layout.visibility = VISIBLE
  }

  override fun errorDismisses() = RxView.clicks(error_dismiss)

  override fun getSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun navigateToAppcPayment() =
      iabView.showOnChain(fiatAmount, isBds, bonus, gamificationLevel)

  override fun navigateToCreditsPayment() =
      iabView.showAppcoinsCreditsPayment(appcAmount, gamificationLevel)

  override fun navigateToPaymentMethods() = iabView.showPaymentMethodsView()

  override fun updateBalanceValues(appcFiat: String, creditsFiat: String, currency: String) {
    balance_fiat_appc_eth.text =
        getString(R.string.purchase_current_balance_appc_eth_body, "$appcFiat $currency")
    credits_fiat_balance.text =
        getString(R.string.purchase_current_balance_appcc_body, "$creditsFiat $currency")
    payment_methods.visibility = VISIBLE
  }

  override fun onResume() {
    super.onResume()
    mergedAppcoinsPresenter.present()
  }

  override fun onPause() {
    mergedAppcoinsPresenter.handleStop()
    super.onPause()
  }

  override fun onDestroyView() {
    iabView.enableBack()
    appcoins_radio_button.setOnCheckedChangeListener(null)
    appcoins_radio.setOnClickListener(null)
    credits_radio_button.setOnCheckedChangeListener(null)
    credits_radio.setOnClickListener(null)
    super.onDestroyView()
  }

  override fun onDestroy() {
    super.onDestroy()
    paymentSelectionSubject = null
    onBackPressSubject = null
  }
}
