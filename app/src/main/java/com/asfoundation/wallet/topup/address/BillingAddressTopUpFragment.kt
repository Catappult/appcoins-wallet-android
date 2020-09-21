package com.asfoundation.wallet.topup.address

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressInteractor
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.address.BillingAddressTextWatcher
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_billing_address_top_up.*
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import javax.inject.Inject

class BillingAddressTopUpFragment : DaggerFragment(), BillingAddressTopUpView {

  companion object {

    private const val BILLING_PAYMENT_MODEL = "billing_payment_model"
    private const val PAYMENT_DATA = "data"

    @JvmStatic
    fun newInstance(data: TopUpPaymentData,
                    billingPaymentModel: BillingPaymentTopUpModel): BillingAddressTopUpFragment {
      return BillingAddressTopUpFragment().apply {
        arguments = Bundle().apply {
          putSerializable(PAYMENT_DATA, data)
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

  private lateinit var topUpView: TopUpActivityView
  private lateinit var disposables: CompositeDisposable
  private lateinit var presenter: BillingAddressTopUpPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    disposables = CompositeDisposable()
    presenter = BillingAddressTopUpPresenter(this, disposables, AndroidSchedulers.mainThread(),
        Schedulers.io(), interactor, billingPaymentModel, logger)
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
  }

  private fun setupFieldsListener() {
    address.addTextChangedListener(BillingAddressTextWatcher(address_layout))
    number.addTextChangedListener(BillingAddressTextWatcher(number_layout))
    city.addTextChangedListener(BillingAddressTextWatcher(city_layout))
    zipcode.addTextChangedListener(BillingAddressTextWatcher(zipcode_layout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(), R.layout.item_state, languages)
    state.setAdapter(adapter)
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is TopUpActivityView) { "billing address fragment must be attached to TopUp activity" }
    topUpView = context
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
      main_value.setText(billingPaymentModel.priceAmount)
      main_currency_code.text = billingPaymentModel.currency
      converted_value.text = "$formattedValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      main_value.setText(formattedValue)
      main_currency_code.text = WalletCurrency.CREDITS.symbol
      converted_value.text = "${billingPaymentModel.priceAmount} ${billingPaymentModel.currency}"
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val billingPaymentModel: BillingPaymentTopUpModel by lazy {
    if (arguments!!.containsKey(BILLING_PAYMENT_MODEL)) {
      arguments!!.getSerializable(BILLING_PAYMENT_MODEL) as BillingPaymentTopUpModel
    } else {
      throw IllegalArgumentException("billingPaymentModel not found")
    }
  }

  private val data: TopUpPaymentData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA) as TopUpPaymentData
    } else {
      throw IllegalArgumentException("previous payment data not found")
    }
  }

}