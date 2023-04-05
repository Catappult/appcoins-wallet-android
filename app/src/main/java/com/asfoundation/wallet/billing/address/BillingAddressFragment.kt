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
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.FragmentBillingAddressBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class BillingAddressFragment : BasePageViewFragment(), BillingAddressView {

  companion object {

    const val BILLING_ADDRESS_MODEL = "billing_address_model"
    internal const val SKU_DESCRIPTION = "sku_description"
    internal const val SKU_ID = "sku_id"
    internal const val TRANSACTION_TYPE = "transaction_type"
    internal const val DOMAIN_KEY = "domain"
    internal const val APPC_AMOUNT_KEY = "appc_amount"
    internal const val BONUS_KEY = "bonus"
    internal const val IS_DONATION_KEY = "is_donation"
    internal const val FIAT_AMOUNT_KEY = "fiat_amount"
    internal const val FIAT_CURRENCY_KEY = "fiat_currency"
    internal const val STORE_CARD_KEY = "store_card"
    internal const val IS_STORED_KEY = "is_stored"

    @JvmStatic
    fun newInstance(
      skuId: String, skuDescription: String, transactionType: String, domain: String,
      appcAmount: BigDecimal, bonus: String, fiatAmount: BigDecimal,
      fiatCurrency: String, isDonation: Boolean, shouldStoreCard: Boolean,
      isStored: Boolean
    ): BillingAddressFragment {
      return BillingAddressFragment().apply {
        arguments = Bundle().apply {
          putString(SKU_DESCRIPTION, skuDescription)
          putString(SKU_ID, skuId)
          putString(TRANSACTION_TYPE, transactionType)
          putString(DOMAIN_KEY, domain)
          putString(BONUS_KEY, bonus)
          putSerializable(APPC_AMOUNT_KEY, appcAmount)
          putSerializable(FIAT_AMOUNT_KEY, fiatAmount)
          putString(FIAT_CURRENCY_KEY, fiatCurrency)
          putBoolean(IS_DONATION_KEY, isDonation)
          putBoolean(STORE_CARD_KEY, shouldStoreCard)
          putBoolean(IS_STORED_KEY, isStored)
        }
      }
    }
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var presenter: BillingAddressPresenter

  private lateinit var iabView: IabView

  private var _binding: FragmentBillingAddressBinding? = null
  private val binding get() = _binding!!

  // fragment_billing_address.xml
  private val bonus_layout = binding.bonusLayout?.root
  private val bonus_msg = binding.bonusMsg

  // dialog_buy_buttons_payment_methods.xml
  private val buy_button = binding.dialogBuyButtons.buyButton
  private val cancel_button = binding.dialogBuyButtons.cancelButton

  // layout_billing_address.xml
  private val address = binding.contentMain.address
  private val city = binding.contentMain.city
  private val number = binding.contentMain.number
  private val zipcode = binding.contentMain.zipcode
  private val country = binding.contentMain.country
  private val state = binding.contentMain.state
  private val state_layout = binding.contentMain.stateLayout
  private val address_layout = binding.contentMain.addressLayout
  private val city_layout = binding.contentMain.cityLayout
  private val number_layout = binding.contentMain.numberLayout
  private val zipcode_layout = binding.contentMain.zipcodeLayout
  private val country_layout = binding.contentMain.countryLayout

  // payment_methods_header.xml
  private val app_icon = binding.paymentMethodsHeader.appIcon
  private val app_name = binding.paymentMethodsHeader.appName
  private val app_sku_description = binding.paymentMethodsHeader.appSkuDescription
  private val appc_price = binding.paymentMethodsHeader.appcPrice
  private val appc_price_skeleton = binding.paymentMethodsHeader.appcPriceSkeleton.root
  private val fiat_price = binding.paymentMethodsHeader.fiatPrice
  private val fiat_price_skeleton = binding.paymentMethodsHeader.fiatPriceSkeleton.root

  // view_purchase_bonus.xml
  private val bonus_value = binding.bonusLayout?.bonusValue


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentBillingAddressBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(
    bonus: String?, isDonation: Boolean,
    domain: String,
    skuDescription: String,
    appcAmount: BigDecimal, fiatAmount: BigDecimal,
    fiatCurrency: String, isStored: Boolean,
    shouldStoreCard: Boolean,
    savedBillingAddress: BillingAddressModel?
  ) {
    iabView.unlockRotation()
    showButtons(isDonation)
    setHeaderInformation(isDonation, domain, skuDescription, appcAmount, fiatAmount, fiatCurrency)
    showBonus(bonus)
    savedBillingAddress?.let { setupSavedBillingAddress(savedBillingAddress) }
    setupFieldsListener()
    setupStateAdapter()
  }

  private fun setupSavedBillingAddress(savedBillingAddress: BillingAddressModel) {
    address.setText(savedBillingAddress.address)
    city.setText(savedBillingAddress.city)
    zipcode.setText(savedBillingAddress.zipcode)
    state.setText(savedBillingAddress.state)
    country.setText(savedBillingAddress.country)
    number.setText(savedBillingAddress.number)
  }

  private fun showButtons(isDonation: Boolean) {
    cancel_button.setText(getString(R.string.back_button))

    if (isDonation) buy_button.setText(getString(R.string.action_donate))
    else buy_button.setText(getString(R.string.action_buy))

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

  override fun backClicks() = RxView.clicks(cancel_button)

  private fun setHeaderInformation(
    isDonation: Boolean, domain: String, skuDescription: String,
    appcAmount: BigDecimal, fiatAmount: BigDecimal,
    fiatCurrency: String
  ) {
    if (isDonation) {
      app_name.text = getString(R.string.item_donation)
      app_sku_description.text = getString(R.string.item_donation)
    } else {
      app_name.text = getApplicationName(domain)
      app_sku_description.text = skuDescription
    }
    try {
      app_icon.setImageDrawable(
        requireContext().packageManager
          .getApplicationIcon(domain)
      )
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
    val packageManager = requireContext().packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun showBonus(bonus: String?) {
    if (bonus?.isNotEmpty() == true) {
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

}
