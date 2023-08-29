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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentBillingAddressBinding
import com.asfoundation.wallet.ui.iab.IabView
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
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

  private val views by viewBinding(FragmentBillingAddressBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentBillingAddressBinding.inflate(inflater).root

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
    views.contentMain.address.setText(savedBillingAddress.address)
    views.contentMain.city.setText(savedBillingAddress.city)
    views.contentMain.zipcode.setText(savedBillingAddress.zipcode)
    views.contentMain.state.setText(savedBillingAddress.state)
    views.contentMain.country.setText(savedBillingAddress.country)
    views.contentMain.number.setText(savedBillingAddress.number)
  }

  private fun showButtons(isDonation: Boolean) {
    views.dialogBuyButtons.cancelButton.setText(getString(R.string.back_button))

    if (isDonation) views.dialogBuyButtons.buyButton.setText(getString(R.string.action_donate))
    else views.dialogBuyButtons.buyButton.setText(getString(R.string.action_buy))

    views.dialogBuyButtons.buyButton.isEnabled = true
    views.dialogBuyButtons.buyButton.visibility = VISIBLE
    views.dialogBuyButtons.cancelButton.visibility = VISIBLE
  }

  private fun setupFieldsListener() {
    views.contentMain.address.addTextChangedListener(BillingAddressTextWatcher(views.contentMain.addressLayout))
    views.contentMain.number.addTextChangedListener(BillingAddressTextWatcher(views.contentMain.numberLayout))
    views.contentMain.city.addTextChangedListener(BillingAddressTextWatcher(views.contentMain.cityLayout))
    views.contentMain.zipcode.addTextChangedListener(BillingAddressTextWatcher(views.contentMain.zipcodeLayout))
  }

  private fun setupStateAdapter() {
    val languages = resources.getStringArray(R.array.states)
    val adapter = ArrayAdapter(requireContext(), R.layout.item_billing_address_state, languages)
    views.contentMain.state.setAdapter(adapter)
  }

  override fun submitClicks(): Observable<BillingAddressModel> {
    return RxView.clicks(views.dialogBuyButtons.buyButton)
      .filter { validateFields() }
      .map {
        BillingAddressModel(
          views.contentMain.address.text.toString(),
          views.contentMain.city.text.toString(),
          views.contentMain.zipcode.text.toString(),
          views.contentMain.state.text.toString(),
          views.contentMain.country.text.toString(),
          views.contentMain.number.text.toString(),
          false
        )
      }
  }

  private fun validateFields(): Boolean {
    var valid = true
    if (views.contentMain.address.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.addressLayout.error = getString(R.string.error_field_required)
    }

    if (views.contentMain.number.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.numberLayout.error = getString(R.string.error_field_required)
    }

    if (views.contentMain.city.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.cityLayout.error = getString(R.string.error_field_required)
    }

    if (views.contentMain.zipcode.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.zipcodeLayout.error = getString(R.string.error_field_required)
    }

    if (views.contentMain.state.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.stateLayout.error = getString(R.string.error_field_required)
    }

    if (views.contentMain.country.text.isNullOrEmpty()) {
      valid = false
      views.contentMain.countryLayout.error = getString(R.string.error_field_required)
    }

    return valid
  }

  override fun backClicks() = RxView.clicks(views.dialogBuyButtons.cancelButton)

  private fun setHeaderInformation(
    isDonation: Boolean, domain: String, skuDescription: String,
    appcAmount: BigDecimal, fiatAmount: BigDecimal,
    fiatCurrency: String
  ) {
    if (isDonation) {
      views.paymentMethodsHeader.appName.text = getString(R.string.item_donation)
      views.paymentMethodsHeader.appSkuDescription.text = getString(R.string.item_donation)
    } else {
      views.paymentMethodsHeader.appName.text = getApplicationName(domain)
      views.paymentMethodsHeader.appSkuDescription.text = skuDescription
    }
    try {
      views.paymentMethodsHeader.appIcon.setImageDrawable(
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
    views.paymentMethodsHeader.fiatPrice.text = fiatText
    views.paymentMethodsHeader.appcPrice.text = appcText
    views.paymentMethodsHeader.fiatPriceSkeleton.root.visibility = GONE
    views.paymentMethodsHeader.appcPriceSkeleton.root.visibility = GONE
    views.paymentMethodsHeader.fiatPrice.visibility = VISIBLE
    views.paymentMethodsHeader.appcPrice.visibility = VISIBLE
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
      views.bonusLayout?.root?.visibility = VISIBLE
      views.bonusMsg?.visibility = VISIBLE
      views.bonusLayout?.bonusValue?.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      views.bonusLayout?.root?.visibility = GONE
      views.bonusMsg?.visibility = GONE
    }
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

}
