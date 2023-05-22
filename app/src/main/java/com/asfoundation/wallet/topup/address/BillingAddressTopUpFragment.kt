package com.asfoundation.wallet.topup.address

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
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
import com.asf.wallet.databinding.FragmentBillingAddressTopUpBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class BillingAddressTopUpFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), BillingAddressTopUpView {

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

  private val binding by viewBinding(FragmentBillingAddressTopUpBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentBillingAddressTopUpBinding.inflate(inflater).root

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
    binding.button.setText(getString(R.string.topup_home_button))
  }

  private fun setupSavedBillingAddress(savedBillingAddress: BillingAddressModel) {
    binding.billingInfoContainer.address.setText(savedBillingAddress.address)
    binding.billingInfoContainer.city.setText(savedBillingAddress.city)
    binding.billingInfoContainer.zipcode.setText(savedBillingAddress.zipcode)
    binding.billingInfoContainer.state.setText(savedBillingAddress.state)
    binding.billingInfoContainer.country.setText(savedBillingAddress.country)
    binding.billingInfoContainer.number.setText(savedBillingAddress.number)
  }

  private fun setupFieldsListener() {
    binding.billingInfoContainer.address.addTextChangedListener(BillingAddressTextWatcher(binding.billingInfoContainer.addressLayout))
    binding.billingInfoContainer.number.addTextChangedListener(BillingAddressTextWatcher(binding.billingInfoContainer.numberLayout))
    binding.billingInfoContainer.city.addTextChangedListener(BillingAddressTextWatcher(binding.billingInfoContainer.cityLayout))
    binding.billingInfoContainer.zipcode.addTextChangedListener(BillingAddressTextWatcher(binding.billingInfoContainer.zipcodeLayout))
    binding.billingInfoContainer.state.addTextChangedListener(BillingAddressTextWatcher(binding.billingInfoContainer.stateLayout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(), R.layout.item_billing_address_state, languages)
    binding.billingInfoContainer.state.setAdapter(adapter)
  }

  override fun finishSuccess(billingAddressModel: BillingAddressModel) {
    val intent = Intent().apply { putExtra(BILLING_ADDRESS_MODEL, billingAddressModel) }
    targetFragment?.onActivityResult(BILLING_ADDRESS_REQUEST_CODE, BILLING_ADDRESS_SUCCESS_CODE,
        intent)
  }

  override fun submitClicks(): Observable<BillingAddressModel> {
    return RxView.clicks(binding.button)
        .filter { validateFields() }
        .map {
          BillingAddressModel(
            binding.billingInfoContainer.address.text.toString(),
            binding.billingInfoContainer.city.text.toString(),
            binding.billingInfoContainer.zipcode.text.toString(),
            binding.billingInfoContainer.state.text.toString(),
            binding.billingInfoContainer.country.text.toString(),
            binding.billingInfoContainer.number.text.toString(),
              false
          )
        }
  }

  private fun validateFields(): Boolean {
    var valid = true
    if (binding.billingInfoContainer.address.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.addressLayout.error = getString(R.string.error_field_required)
    }

    if (binding.billingInfoContainer.number.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.numberLayout.error = getString(R.string.error_field_required)
    }

    if (binding.billingInfoContainer.city.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.cityLayout.error = getString(R.string.error_field_required)
    }

    if (binding.billingInfoContainer.zipcode.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.zipcodeLayout.error = getString(R.string.error_field_required)
    }

    if (binding.billingInfoContainer.state.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.stateLayout.error = getString(R.string.error_field_required)
    }

    if (binding.billingInfoContainer.country.text.isNullOrEmpty()) {
      valid = false
      binding.billingInfoContainer.countryLayout.error = getString(R.string.error_field_required)
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
      binding.bonusLayout.root.visibility = VISIBLE
      binding.bonusMsg.visibility = VISIBLE
      val scaledBonus = data.bonusValue.max(BigDecimal("0.01"))
      val currency = "~${data.fiatCurrencySymbol}".takeIf { data.bonusValue < BigDecimal("0.01") }
          ?: data.fiatCurrencySymbol
      binding.bonusLayout.bonusHeader1.text = getString(R.string.topup_bonus_header_part_1)
      binding.bonusLayout.bonusValue.text = getString(R.string.topup_bonus_header_part_2,
          currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT))
    }
  }

  private fun showValues(data: TopUpPaymentData, fiatAmount: String, fiatCurrency: String) {
    binding.mainValue.visibility = VISIBLE
    val formattedValue = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      binding.mainValue.setText(fiatAmount)
      binding.mainCurrencyCode.text = fiatCurrency
      binding.convertedValue.text = "$formattedValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      binding.mainValue.setText(formattedValue)
      binding.mainCurrencyCode.text = WalletCurrency.CREDITS.symbol
      binding.convertedValue.text = "$fiatAmount $fiatCurrency"
    }
  }

  override fun showLoading() {
    topUpView.lockOrientation()
    binding.loading.visibility = VISIBLE
    binding.billingInfoContainer.root.visibility = View.INVISIBLE
    binding.title.visibility = View.INVISIBLE
    binding.button.isEnabled = false
  }

  override fun hideLoading() {
    topUpView.unlockRotation()
    binding.button.visibility = VISIBLE
    binding.loading.visibility = View.GONE
    binding.button.isEnabled = true
    binding.title.visibility = VISIBLE
    binding.billingInfoContainer.root.visibility = VISIBLE
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}