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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.ui.common.withNoLayoutTransition
import com.asf.wallet.databinding.FragmentCarrierVerifyPhoneBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CarrierVerifyFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), CarrierVerifyView {

  @Inject
  lateinit var presenter: CarrierVerifyPresenter

  private val phoneNumberChangedSubject = PublishSubject.create<Any>()

  private val binding by viewBinding(FragmentCarrierVerifyPhoneBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentCarrierVerifyPhoneBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(IS_PHONE_ERROR_VISIBLE_KEY, binding.fieldErrorText.visibility == View.VISIBLE)
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
    (binding.dialogBuyButtonsPaymentMethods?.cancelButton ?: binding.dialogBuyButtons?.cancelButton)?.run {
      setText(getString(R.string.back_button))
      visibility = View.VISIBLE
    }

    (binding.dialogBuyButtonsPaymentMethods?.buyButton ?: binding.dialogBuyButtons?.buyButton)?.run {
      setText(getString(R.string.action_next))
      visibility = View.VISIBLE
      isEnabled = false
    }

    binding.countryCodePicker.imageViewFlag.alpha = 0.7f
    binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumber)
    binding.countryCodePicker.textView_selectedCountry.typeface =
        Typeface.create("sans-serif-medium", Typeface.NORMAL)

    binding.countryCodePicker.setOnCountryChangeListener {
      phoneNumberChangedSubject.onNext(Unit)
    }
    binding.phoneNumber.doOnTextChanged { _, _, _, _ ->
      phoneNumberChangedSubject.onNext(Unit)
    }
  }

  override fun initializeView(currency: String, fiatAmount: BigDecimal,
                              appcAmount: BigDecimal,
                              skuDescription: String, bonusAmount: BigDecimal?,
                              preselected: Boolean) {
    binding.paymentMethodsHeader.setDescription(skuDescription)
    binding.paymentMethodsHeader.setPrice(fiatAmount, appcAmount, currency)
    binding.paymentMethodsHeader.showPrice()
    binding.paymentMethodsHeader.hideSkeleton()

    binding.purchaseBonus.withNoLayoutTransition {
      if (bonusAmount != null) {
        binding.purchaseBonus.visibility = View.VISIBLE
        binding.purchaseBonus.setPurchaseBonusHeaderValue(bonusAmount, mapCurrencyCodeToSymbol(currency))
        binding.purchaseBonus.hideSkeleton()
      } else {
        binding.purchaseBonus.visibility = View.GONE
      }
    }

    if (preselected) {
      binding.otherPaymentsButton.withNoLayoutTransition {
        binding.otherPaymentsButton.visibility = View.VISIBLE
      }
      (binding.dialogBuyButtonsPaymentMethods?.cancelButton ?: binding.dialogBuyButtons?.cancelButton)?.setText(getString(R.string.cancel_button))
      if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        val marginParams = binding.purchaseBonus.layoutParams as ViewGroup.MarginLayoutParams
        marginParams.topMargin = 0
      }
    }
  }

  override fun setAppDetails(appName: String, icon: Drawable) {
    binding.paymentMethodsHeader.setTitle(appName)
    binding.paymentMethodsHeader.setIcon(icon)
  }

  override fun filterCountries(countryListString: String, defaultCountry: String?) {
    binding.countryCodePicker.setCustomMasterCountries(countryListString)
    defaultCountry?.let { country ->
      binding.countryCodePicker.setCountryForNameCode(country)
      binding.countryCodePicker.holder.requestLayout()
    }
  }

  override fun showSavedPhoneNumber(phoneNumber: String) {
    binding.countryCodePicker.fullNumber = phoneNumber
    binding.savedPhoneNumberConfirmed.visibility = View.VISIBLE
    binding.changePhoneNumberButton.visibility = View.VISIBLE
    binding.title.text = getString(R.string.carrier_billing_insert_phone_previously_used)
    setPhoneNumberViewsEditable(false)
  }

  override fun hideSavedPhoneNumber(clearText: Boolean) {
    if (clearText) binding.phoneNumber.setText("")
    binding.title.text = getString(R.string.carrier_billing_insert_phone_body)
    binding.savedPhoneNumberConfirmed.visibility = View.GONE
    binding.changePhoneNumberButton.visibility = View.GONE
    setPhoneNumberViewsEditable(true)
  }

  private fun setPhoneNumberViewsEditable(editable: Boolean) {
    binding.countryCodePicker.setCcpClickable(editable)
    binding.phoneNumber.isFocusable = editable
    binding.phoneNumber.isClickable = editable
  }

  override fun showPhoneNumberLayout() {
    binding.phoneNumberSkeleton.root.visibility = View.GONE
    binding.phoneNumberLayout.visibility = View.VISIBLE
  }

  override fun changeButtonClick(): Observable<Any> {
    return RxView.clicks(binding.changePhoneNumberButton)
  }

  override fun focusOnPhoneNumber() {
    binding.phoneNumber.isFocusableInTouchMode = true
    binding.phoneNumber.requestFocus()
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
          .symbol
  }

  override fun backEvent(): Observable<Any> = RxView.clicks(binding.dialogBuyButtonsPaymentMethods?.cancelButton ?: binding.dialogBuyButtons?.cancelButton!!)

  override fun nextClickEvent(): Observable<String> {
    return RxView.clicks(binding.dialogBuyButtonsPaymentMethods?.buyButton ?: binding.dialogBuyButtons?.buyButton!!)
        .map { binding.countryCodePicker.fullNumberWithPlus.toString() }
  }

  override fun phoneNumberChangeEvent(): Observable<Pair<String, Boolean>> {
    return phoneNumberChangedSubject
        .map {
          Pair(binding.countryCodePicker.fullNumberWithPlus.toString(),
            binding.countryCodePicker.isValidFullNumber)
        }
  }

  override fun setLoading() {
    binding.title.visibility = View.INVISIBLE
    binding.disclaimer.visibility = View.INVISIBLE
    binding.phoneNumberLayout.visibility = View.INVISIBLE
    if (binding.changePhoneNumberButton.visibility == View.VISIBLE) {
      binding.changePhoneNumberButton.visibility = View.INVISIBLE
    }
    binding.progressBar.visibility = View.VISIBLE
    removePhoneNumberFieldError()
    (binding.dialogBuyButtonsPaymentMethods?.buyButton ?: binding.dialogBuyButtons?.buyButton!!).isEnabled = false
  }

  override fun showInvalidPhoneNumberError() {
    (binding.dialogBuyButtonsPaymentMethods?.buyButton ?: binding.dialogBuyButtons?.buyButton!!).isEnabled = true
    binding.phoneNumberLayout.setBackgroundResource(R.drawable.rectangle_outline_red_radius_8dp)
    binding.fieldErrorText.visibility = View.VISIBLE
    binding.title.visibility = View.VISIBLE
    binding.disclaimer.visibility = View.VISIBLE
    binding.phoneNumberLayout.visibility = View.VISIBLE
    if (binding.changePhoneNumberButton.visibility == View.INVISIBLE) {
      binding.changePhoneNumberButton.visibility = View.VISIBLE
    }
    binding.progressBar.visibility = View.INVISIBLE
  }

  override fun removePhoneNumberFieldError() {
    binding.fieldErrorText.visibility = View.GONE
    binding.phoneNumberLayout.setBackgroundResource(R.drawable.rectangle_outline_grey_radius_8dp)
  }

  override fun setNextButtonEnabled(enabled: Boolean) {
    (binding.dialogBuyButtonsPaymentMethods?.buyButton ?: binding.dialogBuyButtons?.buyButton!!).isEnabled = enabled
  }

  override fun unlockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun otherPaymentMethodsEvent(): Observable<Any> {
    return RxView.clicks(binding.otherPaymentsButton)
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