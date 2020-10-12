package com.asfoundation.wallet.billing.address

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.asf.wallet.R
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_CANCEL_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_billing_address.*
import kotlinx.android.synthetic.main.layout_billing_address.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import java.math.BigDecimal
import javax.inject.Inject

class BillingAddressFragment : DaggerFragment(), BillingAddressView {

  companion object {

    const val BILLING_ADDRESS_MODEL = "billing_address_model"
    private const val SKU_DESCRIPTION = "sku_description"
    private const val DOMAIN_KEY = "domain"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val BONUS_KEY = "bonus"
    private const val IS_DONATION_KEY = "is_donation"
    private const val FIAT_AMOUNT_KEY = "fiat_amount"
    private const val FIAT_CURRENCY_KEY = "fiat_currency"
    private const val STORE_CARD_KEY = "store_card"
    private const val PRE_SELECTED_KEY = "pre_selected"

    @JvmStatic
    fun newInstance(skuDescription: String, domain: String, appcAmount: BigDecimal, bonus: String,
                    fiatAmount: BigDecimal, fiatCurrency: String, isDonation: Boolean,
                    shouldStoreCard: Boolean, preSelected: Boolean): BillingAddressFragment {
      return BillingAddressFragment().apply {
        arguments = Bundle().apply {
          putString(SKU_DESCRIPTION, skuDescription)
          putString(DOMAIN_KEY, domain)
          putString(BONUS_KEY, bonus)
          putSerializable(APPC_AMOUNT_KEY, appcAmount)
          putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
          putString(FIAT_CURRENCY_KEY, fiatCurrency)
          putBoolean(IS_DONATION_KEY, isDonation)
          putBoolean(STORE_CARD_KEY, shouldStoreCard)
          putBoolean(PRE_SELECTED_KEY, preSelected)
        }
      }
    }
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  private lateinit var iabView: IabView
  private lateinit var presenter: BillingAddressPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BillingAddressPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_billing_address, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  private fun setupUi() {
    iabView.unlockRotation()
    showButtons()
    setHeaderInformation()
    showBonus()
    setupFieldsListener()
    setupStateAdapter()
    if (preSelected) remember.visibility = GONE
    else {
      remember.visibility = VISIBLE
      remember.isChecked = shouldStoreCard
    }
  }

  private fun showButtons() {
    cancel_button.setText(R.string.back_button)

    if (isDonation) buy_button.setText(R.string.action_donate)
    else buy_button.setText(R.string.action_buy)

    buy_button.isEnabled = true
    buy_button.visibility = VISIBLE
    cancel_button.visibility = VISIBLE
  }

  private fun setupFieldsListener() {
    address.addTextChangedListener(BillingAddressTextWatcher(address_layout))
    number.addTextChangedListener(BillingAddressTextWatcher(number_layout))
    city.addTextChangedListener(BillingAddressTextWatcher(city_layout))
    zipcode.addTextChangedListener(BillingAddressTextWatcher(zipcode_layout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(), R.layout.item_billing_address_state, languages)
    state.setAdapter(adapter)
  }

  override fun submitClicks(): Observable<BillingAddressModel> {
    return RxView.clicks(buy_button)
        .filter { validateFields() }
        .map {
          BillingAddressModel(
              address.text.toString(),
              city.text.toString(),
              zipcode.text.toString(),
              state.text.toString(),
              country.text.toString(),
              number.text.toString(),
              remember.isChecked
          )
        }
  }

  override fun finishSuccess(billingAddressModel: BillingAddressModel) {
    val intent = Intent().apply {
      putExtra(BILLING_ADDRESS_MODEL, billingAddressModel)
    }
    targetFragment?.onActivityResult(BILLING_ADDRESS_REQUEST_CODE, BILLING_ADDRESS_SUCCESS_CODE,
        intent)
    iabView.navigateBack()
  }

  override fun cancel() {
    targetFragment?.onActivityResult(BILLING_ADDRESS_REQUEST_CODE, BILLING_ADDRESS_CANCEL_CODE,
        null)
    iabView.navigateBack()
  }

  private fun validateFields(): Boolean {
    var valid = true
    if (address.text.isNullOrEmpty()) {
      valid = false
      address_layout.error = getString(R.string.error_field_required)
    }

    if (number.text.isNullOrEmpty()) {
      valid = false
      number_layout.error = getString(R.string.error_field_required)
    }

    if (city.text.isNullOrEmpty()) {
      valid = false
      city_layout.error = getString(R.string.error_field_required)
    }

    if (zipcode.text.isNullOrEmpty()) {
      valid = false
      zipcode_layout.error = getString(R.string.error_field_required)
    }

    if (state.text.isNullOrEmpty()) {
      valid = false
      state_layout.error = getString(R.string.error_field_required)
    }

    if (country.text.isNullOrEmpty()) {
      valid = false
      country_layout.error = getString(R.string.error_field_required)
    }

    return valid
  }

  override fun backClicks() = RxView.clicks(cancel_button)

  private fun setHeaderInformation() {
    if (isDonation) {
      app_name.text = getString(R.string.item_donation)
      app_sku_description.text = getString(R.string.item_donation)
    } else {
      app_name.text = getApplicationName(domain)
      app_sku_description.text = skuDescription
    }
    try {
      app_icon.setImageDrawable(context!!.packageManager
          .getApplicationIcon(domain))
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    val appcText = formatter.formatCurrency(appcAmount, WalletCurrency.APPCOINS)
        .plus(" " + WalletCurrency.APPCOINS.symbol)
    val fiatText = formatter.formatCurrency(fiatAmount, WalletCurrency.FIAT)
        .plus(" $fiatCurrency")
    fiat_price.text = fiatText
    appc_price.text = appcText
    fiat_price_skeleton.visibility = GONE
    appc_price_skeleton.visibility = GONE
    fiat_price.visibility = VISIBLE
    appc_price.visibility = VISIBLE
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "billing address fragment must be attached to IAB activity" }
    iabView = context
  }

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence? {
    val packageManager = context!!.packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun showBonus() {
    if (bonus.isNotEmpty()) {
      bonus_layout?.visibility = VISIBLE
      bonus_msg?.visibility = VISIBLE
      bonus_value?.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      bonus_layout?.visibility = GONE
      bonus_msg?.visibility = GONE
    }
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  private val skuDescription: String by lazy {
    if (arguments!!.containsKey(SKU_DESCRIPTION)) {
      arguments!!.getString(SKU_DESCRIPTION, "")
    } else {
      throw IllegalArgumentException("sku description data not found")
    }
  }

  private val domain: String by lazy {
    if (arguments!!.containsKey(DOMAIN_KEY)) {
      arguments!!.getString(DOMAIN_KEY, "")
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }

  private val appcAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(APPC_AMOUNT_KEY)) {
      arguments!!.getSerializable(APPC_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("appc amount data not found")
    }
  }

  private val bonus: String by lazy {
    if (arguments!!.containsKey(BONUS_KEY)) {
      arguments!!.getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val fiatAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(FIAT_AMOUNT_KEY)) {
      arguments!!.getSerializable(FIAT_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("fiat amount data not found")
    }
  }

  private val fiatCurrency: String by lazy {
    if (arguments!!.containsKey(FIAT_CURRENCY_KEY)) {
      arguments!!.getString(FIAT_CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("fiat currency data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (arguments!!.containsKey(IS_DONATION_KEY)) {
      arguments!!.getBoolean(IS_DONATION_KEY)
    } else {
      throw IllegalArgumentException("is donation data not found")
    }
  }

  private val shouldStoreCard: Boolean by lazy {
    if (arguments!!.containsKey(STORE_CARD_KEY)) {
      arguments!!.getBoolean(STORE_CARD_KEY)
    } else {
      throw IllegalArgumentException("should store card data not found")
    }
  }

  private val preSelected: Boolean by lazy {
    if (arguments!!.containsKey(PRE_SELECTED_KEY)) {
      arguments!!.getBoolean(PRE_SELECTED_KEY)
    } else {
      throw IllegalArgumentException("pre selected data not found")
    }
  }

}