package com.asfoundation.wallet.topup.address

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.address.BillingAddressTextWatcher
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_billing_address_top_up.*
import kotlinx.android.synthetic.main.layout_billing_address.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import javax.inject.Inject

class BillingAddressTopUpFragment : DaggerFragment(), BillingAddressTopUpView {

  companion object {

    const val BILLING_ADDRESS_MODEL = "billing_address_model"
    private const val PAYMENT_DATA = "data"
    private const val FIAT_AMOUNT_KEY = "fiat_amount"
    private const val FIAT_CURRENCY_KEY = "fiat_currency"
    private const val STORE_CARD_KEY = "store_card"
    private const val IS_STORED_KEY = "is_stored"

    @JvmStatic
    fun newInstance(data: TopUpPaymentData, fiatAmount: String, fiatCurrency: String,
                    shouldStoreCard: Boolean, isStored: Boolean): BillingAddressTopUpFragment {
      return BillingAddressTopUpFragment().apply {
        arguments = Bundle().apply {
          putSerializable(PAYMENT_DATA, data)
          putString(FIAT_AMOUNT_KEY, fiatAmount)
          putString(FIAT_CURRENCY_KEY, fiatCurrency)
          putBoolean(STORE_CARD_KEY, shouldStoreCard)
          putBoolean(IS_STORED_KEY, isStored)
        }
      }
    }
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private lateinit var topUpView: TopUpActivityView
  private lateinit var navigator: Navigator
  private lateinit var presenter: BillingAddressTopUpPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        BillingAddressTopUpPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread(),
            navigator)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_billing_address_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  private fun setupUi() {
    showBonus()
    showValues()
    setupFieldsListener()
    setupStateAdapter()
    button.setText(R.string.topup_home_button)
    if (isStored) remember.visibility = View.GONE
    else {
      remember.visibility = VISIBLE
      remember.isChecked = shouldStoreCard
    }
  }

  private fun setupFieldsListener() {
    address.addTextChangedListener(BillingAddressTextWatcher(address_layout))
    number.addTextChangedListener(BillingAddressTextWatcher(number_layout))
    city.addTextChangedListener(BillingAddressTextWatcher(city_layout))
    zipcode.addTextChangedListener(BillingAddressTextWatcher(zipcode_layout))
    state.addTextChangedListener(BillingAddressTextWatcher(state_layout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(), R.layout.item_billing_address_state, languages)
    state.setAdapter(adapter)
  }

  override fun finishSuccess(billingAddressModel: BillingAddressModel) {
    val intent = Intent().apply { putExtra(BILLING_ADDRESS_MODEL, billingAddressModel) }
    targetFragment?.onActivityResult(BILLING_ADDRESS_REQUEST_CODE, BILLING_ADDRESS_SUCCESS_CODE,
        intent)
  }

  override fun submitClicks(): Observable<BillingAddressModel> {
    return RxView.clicks(button)
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is TopUpActivityView) { "billing address fragment must be attached to TopUp activity" }
    topUpView = context
    navigator = TopUpNavigator(requireFragmentManager(), (activity as UriNavigator?)!!, topUpView)
  }

  private fun showBonus() {
    if (data.bonusValue.compareTo(BigDecimal.ZERO) != 0) {
      bonus_layout?.visibility = VISIBLE
      bonus_msg?.visibility = VISIBLE
      val scaledBonus = data.bonusValue.max(BigDecimal("0.01"))
      val currency = "~${data.fiatCurrencySymbol}".takeIf { data.bonusValue < BigDecimal("0.01") }
          ?: data.fiatCurrencySymbol
      bonus_layout?.bonus_header_1?.text = getString(R.string.topup_bonus_header_part_1)
      bonus_layout?.bonus_value?.text = getString(R.string.topup_bonus_header_part_2,
          currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT))
    }
  }

  private fun showValues() {
    main_value.visibility = VISIBLE
    val formattedValue = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      main_value.setText(fiatAmount)
      main_currency_code.text = fiatCurrency
      converted_value.text = "$formattedValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      main_value.setText(formattedValue)
      main_currency_code.text = WalletCurrency.CREDITS.symbol
      converted_value.text = "$fiatAmount $fiatCurrency"
    }
  }

  override fun showLoading() {
    topUpView.lockOrientation()
    loading.visibility = VISIBLE
    billing_info_container.visibility = View.INVISIBLE
    title.visibility = View.INVISIBLE
    button.isEnabled = false
  }

  override fun hideLoading() {
    topUpView.unlockRotation()
    button.visibility = VISIBLE
    loading.visibility = View.GONE
    button.isEnabled = true
    title.visibility = VISIBLE
    billing_info_container.visibility = VISIBLE
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val fiatAmount: String by lazy {
    if (arguments!!.containsKey(FIAT_AMOUNT_KEY)) {
      arguments!!.getString(FIAT_AMOUNT_KEY, "")
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

  private val data: TopUpPaymentData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA) as TopUpPaymentData
    } else {
      throw IllegalArgumentException("previous payment data not found")
    }
  }

  private val shouldStoreCard: Boolean by lazy {
    if (arguments!!.containsKey(STORE_CARD_KEY)) {
      arguments!!.getBoolean(STORE_CARD_KEY)
    } else {
      throw IllegalArgumentException("should store card data not found")
    }
  }

  private val isStored: Boolean by lazy {
    if (arguments!!.containsKey(IS_STORED_KEY)) {
      arguments!!.getBoolean(IS_STORED_KEY)
    } else {
      throw IllegalArgumentException("is stored data not found")
    }
  }

}