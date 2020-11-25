package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.asf.wallet.R
import com.asfoundation.wallet.util.withNoLayoutTransition
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_carrier_verify_phone.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class CarrierVerifyFragment : DaggerFragment(), CarrierVerifyView {

  @Inject
  lateinit var presenter: CarrierVerifyPresenter

  private val phoneNumberChangedSubject = PublishSubject.create<Any>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_carrier_verify_phone, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(IS_PHONE_ERROR_VISIBLE_KEY, field_error_text?.visibility == View.VISIBLE)
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)
    if (savedInstanceState?.getBoolean(IS_PHONE_ERROR_VISIBLE_KEY, false) == true) {
      showInvalidPhoneNumberError()
    } else {
      removePhoneNumberFieldError()
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun setupUi() {
    cancel_button.setText(R.string.back_button)
    cancel_button.visibility = View.VISIBLE

    buy_button.setText(R.string.action_next)
    buy_button.visibility = View.VISIBLE
    buy_button.isEnabled = false

    country_code_picker.imageViewFlag.alpha = 0.7f
    country_code_picker.registerCarrierNumberEditText(phone_number)
    country_code_picker.textView_selectedCountry.typeface =
        Typeface.create("sans-serif-medium", Typeface.NORMAL)

    country_code_picker.setOnCountryChangeListener {
      phoneNumberChangedSubject.onNext(Unit)
    }
    phone_number.doOnTextChanged { _, _, _, _ ->
      phoneNumberChangedSubject.onNext(Unit)
    }
  }

  override fun initializeView(currency: String, fiatAmount: BigDecimal,
                              appcAmount: BigDecimal,
                              skuDescription: String, bonusAmount: BigDecimal?,
                              preselected: Boolean) {
    payment_methods_header.setDescription(skuDescription)
    payment_methods_header.setPrice(fiatAmount, appcAmount, currency)
    payment_methods_header.showPrice()
    payment_methods_header.hideSkeleton()

    purchase_bonus.withNoLayoutTransition {
      if (bonusAmount != null) {
        purchase_bonus.visibility = View.VISIBLE
        purchase_bonus.setPurchaseBonusHeaderValue(bonusAmount, mapCurrencyCodeToSymbol(currency))
        purchase_bonus.hideSkeleton()
      } else {
        purchase_bonus.visibility = View.GONE
      }
    }

    if (preselected) {
      other_payments_button.withNoLayoutTransition {
        other_payments_button.visibility = View.VISIBLE
      }
      cancel_button.setText(R.string.cancel_button)
      if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        val marginParams = purchase_bonus.layoutParams as ViewGroup.MarginLayoutParams
        marginParams.topMargin = 0
      }
    }
  }

  override fun setAppDetails(appName: String, icon: Drawable) {
    payment_methods_header.setTitle(appName)
    payment_methods_header.setIcon(icon)
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
          .symbol
  }

  override fun backEvent(): Observable<Any> = RxView.clicks(cancel_button)

  override fun nextClickEvent(): Observable<String> {
    return RxView.clicks(buy_button)
        .map { country_code_picker.fullNumberWithPlus.toString() }
  }

  override fun phoneNumberChangeEvent(): Observable<Pair<String, Boolean>> {
    return phoneNumberChangedSubject
        .map {
          Pair(country_code_picker.fullNumberWithPlus.toString(),
              country_code_picker.isValidFullNumber)
        }
  }

  override fun setLoading() {
    title.visibility = View.INVISIBLE
    disclaimer.visibility = View.INVISIBLE
    phone_number_layout.visibility = View.INVISIBLE
    progress_bar.visibility = View.VISIBLE
    removePhoneNumberFieldError()
    buy_button.isEnabled = false
  }

  override fun showInvalidPhoneNumberError() {
    buy_button.isEnabled = true
    phone_number_layout.setBackgroundResource(R.drawable.rectangle_outline_red_radius_8dp)
    field_error_text.visibility = View.VISIBLE
    title.visibility = View.VISIBLE
    disclaimer.visibility = View.VISIBLE
    phone_number_layout.visibility = View.VISIBLE
    progress_bar.visibility = View.INVISIBLE
  }

  override fun removePhoneNumberFieldError() {
    field_error_text.visibility = View.GONE
    phone_number_layout.setBackgroundResource(R.drawable.rectangle_outline_grey_radius_8dp)
  }

  override fun setNextButtonEnabled(enabled: Boolean) {
    buy_button.isEnabled = enabled
  }

  override fun unlockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun otherPaymentMethodsEvent(): Observable<Any> {
    return RxView.clicks(other_payments_button)
  }

  companion object {

    val TAG = CarrierVerifyFragment::class.java.simpleName
    const val BACKSTACK_NAME = "carrier_entry_point"

    private const val IS_PHONE_ERROR_VISIBLE_KEY = "IS_PHONE_ERROR_VISIBLE"

    internal const val PRE_SELECTED_KEY = "pre_selected"
    internal const val TRANSACTION_TYPE_KEY = "type"
    internal const val DOMAIN_KEY = "domain"
    internal const val ORIGIN_KEY = "origin"
    internal const val TRANSACTION_DATA_KEY = "transaction_data"
    internal const val APPC_AMOUNT_KEY = "appc_amount"
    internal const val FIAT_AMOUNT_KEY = "fiat_amount"
    internal const val CURRENCY_KEY = "currency"
    internal const val BONUS_AMOUNT_KEY = "bonus_amount"
    internal const val SKU_DESCRIPTION = "sku_description"
    internal const val SKU_ID = "sku_id"

    @JvmStatic
    fun newInstance(preSelected: Boolean, domain: String, origin: String?, transactionType: String,
                    transactionData: String?,
                    currency: String?, amount: BigDecimal, appcAmount: BigDecimal,
                    bonus: BigDecimal?, skuDescription: String,
                    skuId: String?): CarrierVerifyFragment {
      val fragment = CarrierVerifyFragment()

      fragment.arguments = Bundle().apply {
        putBoolean(PRE_SELECTED_KEY, preSelected)
        putString(DOMAIN_KEY, domain)
        putString(ORIGIN_KEY, origin)
        putString(TRANSACTION_TYPE_KEY, transactionType)
        putString(TRANSACTION_DATA_KEY, transactionData)
        putString(CURRENCY_KEY, currency)
        putSerializable(FIAT_AMOUNT_KEY, amount)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putSerializable(BONUS_AMOUNT_KEY, bonus)
        putString(SKU_DESCRIPTION, skuDescription)
        putString(SKU_ID, skuId)
      }
      return fragment
    }
  }
}