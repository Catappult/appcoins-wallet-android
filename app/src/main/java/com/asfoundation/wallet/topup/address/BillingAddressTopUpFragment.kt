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
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_billing_address_top_up.*
import kotlinx.android.synthetic.main.layout_billing_address.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class BillingAddressTopUpFragment : BasePageViewFragment(), BillingAddressTopUpView {

  companion object {

    internal const val BILLING_ADDRESS_MODEL = "billing_address_model"
    internal const val PAYMENT_DATA = "data"
    internal const val FIAT_AMOUNT_KEY = "fiat_amount"
    internal const val FIAT_CURRENCY_KEY = "fiat_currency"
    internal const val STORE_CARD_KEY = "store_card"
    internal const val IS_STORED_KEY = "is_stored"

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

  @Inject
  lateinit var presenter: BillingAddressTopUpPresenter

  private lateinit var topUpView: TopUpActivityView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_billing_address_top_up, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(data: TopUpPaymentData,
                              fiatAmount: String, fiatCurrency: String,
                              shouldStoreCard: Boolean, isStored: Boolean,
                              savedBillingAddress: BillingAddressModel?) {
    showBonus(data)
    showValues(data, fiatAmount, fiatCurrency)
    savedBillingAddress?.let { setupSavedBillingAddress(savedBillingAddress) }
    setupFieldsListener()
    setupStateAdapter()
    button.setText(getString(R.string.topup_home_button))
  }

  private fun setupSavedBillingAddress(savedBillingAddress: BillingAddressModel) {
    address.setText(savedBillingAddress.address)
    city.setText(savedBillingAddress.city)
    zipcode.setText(savedBillingAddress.zipcode)
    state.setText(savedBillingAddress.state)
    country.setText(savedBillingAddress.country)
    number.setText(savedBillingAddress.number)
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
              false
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
  }

  private fun showBonus(data: TopUpPaymentData) {
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

  private fun showValues(data: TopUpPaymentData, fiatAmount: String, fiatCurrency: String) {
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
}