package com.asfoundation.wallet.billing.address

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.R
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_billing_address.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import java.math.BigDecimal
import javax.inject.Inject

class BillingAddressFragment : DaggerFragment(), BillingAddressView {

  companion object {

    private const val SKU_DESCRIPTION = "sku_description"
    private const val TRANSACTION_TYPE_KEY = "type"
    private const val DOMAIN_KEY = "domain"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val BONUS_KEY = "bonus"
    private const val IS_DONATION_KEY = "is_donation"
    private const val FIAT_AMOUNT_KEY = "fiat_amount"
    private const val FIAT_CURRENCY_KEY = "currency_amount"
    private const val BILLING_PAYMENT_MODEL = "billing_payment_model"

    @JvmStatic
    fun newInstance(skuDescription: String, transactionType: String, domain: String,
                    appcAmount: BigDecimal, bonus: String, fiatAmount: BigDecimal, currency: String,
                    isDonation: Boolean,
                    billingPaymentModel: BillingPaymentModel): BillingAddressFragment {
      return BillingAddressFragment().apply {
        arguments = Bundle().apply {
          putString(TRANSACTION_TYPE_KEY, transactionType)
          putString(SKU_DESCRIPTION, skuDescription)
          putString(DOMAIN_KEY, domain)
          putString(BONUS_KEY, bonus)
          putSerializable(APPC_AMOUNT_KEY, appcAmount)
          putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
          putString(FIAT_CURRENCY_KEY, currency)
          putBoolean(IS_DONATION_KEY, isDonation)
          putSerializable(BILLING_PAYMENT_MODEL, billingPaymentModel)
        }
      }
    }
  }

  @Inject
  lateinit var interactor: BillingAddressInteractor

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  private lateinit var iabView: IabView
  private lateinit var disposables: CompositeDisposable
  private lateinit var presenter: BillingAddressPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    disposables = CompositeDisposable()
    presenter = BillingAddressPresenter(this, disposables, AndroidSchedulers.mainThread(),
        Schedulers.io(), interactor, billingPaymentModel, logger)
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
    cancel_button.setText(R.string.back_button)
    handleBuyButtonText()
    setHeaderInformation()
    showBonus()
    setupFieldsListener()
    setupStateAdapter()
  }

  private fun setupFieldsListener() {
    address.addTextChangedListener(BillingAddressTextWatcher(address_layout))
    number.addTextChangedListener(BillingAddressTextWatcher(number_layout))
    city.addTextChangedListener(BillingAddressTextWatcher(city_layout))
    zipcode.addTextChangedListener(BillingAddressTextWatcher(zipcode_layout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(),
        android.R.layout.simple_list_item_1, languages)
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
              true
          )
        }
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
        .plus(" $currency")
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

  private fun handleBuyButtonText() {
    if (transactionType.equals(TransactionData.TransactionType.DONATION.name, ignoreCase = true)) {
      buy_button.setText(R.string.action_donate)
    } else {
      buy_button.setText(R.string.action_buy)
    }
  }

  override fun showMoreMethods() {
    iabView.showPaymentMethodsView()
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

  private val transactionType: String by lazy {
    if (arguments!!.containsKey(TRANSACTION_TYPE_KEY)) {
      arguments!!.getString(TRANSACTION_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("transaction type data not found")
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
      throw IllegalArgumentException("amount data not found")
    }
  }
  private val currency: String by lazy {
    if (arguments!!.containsKey(FIAT_CURRENCY_KEY)) {
      arguments!!.getString(FIAT_CURRENCY_KEY)!!
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (arguments!!.containsKey(IS_DONATION_KEY)) {
      arguments!!.getBoolean(IS_DONATION_KEY)
    } else {
      throw IllegalArgumentException("is donation data not found")
    }
  }

  private val billingPaymentModel: BillingPaymentModel by lazy {
    if (arguments!!.containsKey(BILLING_PAYMENT_MODEL)) {
      arguments!!.getSerializable(BILLING_PAYMENT_MODEL) as BillingPaymentModel
    } else {
      throw IllegalArgumentException("billingPaymentModel not found")
    }
  }
}