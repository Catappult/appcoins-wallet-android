package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.Balance
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.util.scaleToString
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
import kotlinx.android.synthetic.main.dialog_credit_card_authorization.*
import kotlinx.android.synthetic.main.fragment_iab_error.*
import kotlinx.android.synthetic.main.merged_appcoins_layout.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
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
    const val APPC = "appcoins"
    const val CREDITS = "credits"

    @JvmStatic
    fun newInstance(fiatAmount: BigDecimal,
                    currency: String, bonus: String, appName: String,
                    productName: String?,
                    appcAmount: BigDecimal, appcEnabled: Boolean,
                    creditsEnabled: Boolean, isBds: Boolean,
                    isDonation: Boolean): Fragment {
      val fragment = MergedAppcoinsFragment()
      val bundle = Bundle()
      bundle.putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
      bundle.putString(FIAT_CURRENCY_KEY, currency)
      bundle.putString(BONUS_KEY, bonus)
      bundle.putString(APP_NAME_KEY, appName)
      bundle.putString(PRODUCT_NAME_KEY, productName)
      bundle.putSerializable(APPC_AMOUNT_KEY, appcAmount)
      bundle.putBoolean(APPC_ENABLED_KEY, appcEnabled)
      bundle.putBoolean(CREDITS_ENABLED_KEY, creditsEnabled)
      bundle.putBoolean(IS_BDS_KEY, isBds)
      bundle.putBoolean(IS_DONATION_KEY, isDonation)
      fragment.arguments = bundle
      return fragment
    }
  }

  private lateinit var mergedAppcoinsPresenter: MergedAppcoinsPresenter
  private var paymentSelectionSubject: PublishSubject<String>? = null
  private var onBackPressSubject: PublishSubject<Any>? = null
  private lateinit var iabView: IabView
  @Inject
  lateinit var balanceInteract: BalanceInteract

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentSelectionSubject = PublishSubject.create()
    onBackPressSubject = PublishSubject.create()
    mergedAppcoinsPresenter = MergedAppcoinsPresenter(this, CompositeDisposable(), balanceInteract,
        AndroidSchedulers.mainThread(), Schedulers.io())
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
    setBalanceInformation()
    buy_button.text = setBuyButtonText()
    cancel_button.text = getString(R.string.back_button)
    setPaymentInformation()
    setBonus()
    setBackListener(view)
  }

  private fun setBalanceInformation() {
    val balanceText = getString(R.string.balance_title) + ":"
    appcoins_balance.text = balanceText
    credits_balance.text = balanceText
    balance_eth.text = getString(R.string.p2p_send_currency_eth) + ":"
  }

  private fun setBuyButtonText(): String {
    return if (isDonation) getString(R.string.action_donate) else getString(R.string.action_buy)
  }

  private fun setBonus() {
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
    val formatter = Formatter()
    val decimalFormat = DecimalFormat("0.00")
    val appcText = formatter.format(Locale.getDefault(), "%(,.2f", appcAmount)
        .toString() + " APPC"
    val fiatText = decimalFormat.format(fiatAmount) + ' ' + currency
    fiat_price.text = fiatText
    appc_price.text = appcText
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

  override fun buyClick(): Observable<String> {
    return RxView.clicks(buy_button)
        .map { getSelectedPaymentMethod() }
  }

  override fun backClick(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun backPressed(): Observable<Any> {
    return onBackPressSubject!!
  }

  override fun getPaymentSelection(): Observable<String> {
    return paymentSelectionSubject!!
  }

  override fun hideBonus() {
    bonus_layout?.visibility = INVISIBLE
    bonus_msg?.visibility = INVISIBLE
  }

  override fun showBonus() {
    bonus_layout?.visibility = VISIBLE
    bonus_msg?.visibility = VISIBLE
  }

  override fun showError(@StringRes errorMessage: Int) {
    payment_method_main_view.visibility = GONE
    activity_iab_error_message.text = getString(errorMessage)
    activity_iab_error_view.visibility = VISIBLE
  }

  override fun navigateToAppcPayment() {
    iabView.showOnChain(fiatAmount, isBds, bonus)
  }

  override fun navigateToCreditsPayment() {
    iabView.showAppcoinsCreditsPayment(appcAmount)
  }

  override fun navigateToPaymentMethods(
      preSelectedMethod: PaymentMethodsView.SelectedPaymentMethod) {
    iabView.showPaymentMethodsView(preSelectedMethod)
  }

  override fun updateBalanceValues(appcBalance: Balance, creditsBalance: Balance,
                                   ethBalance: Balance) {
    appc_balance.text = appcBalance.token.amount.scaleToString(2) + " APPC"
    credits_balance_value.text = creditsBalance.token.amount.scaleToString(2) + " APPC-C"
    appc_fiat_balance.text =
        appcBalance.fiat.amount.scaleToString(2) + " " + appcBalance.fiat.currency
    credits_fiat_balance.text =
        creditsBalance.fiat.amount.scaleToString(2) + " " + creditsBalance.fiat.currency
    eth_fiat_balance.text = ethBalance.fiat.amount.scaleToString(2) + " " + ethBalance.fiat.currency
    eth_balance.text = ethBalance.token.amount.scaleToString(2) + " ETH"
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
