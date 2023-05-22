package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.EarnAppcoinsLayoutBinding
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class EarnAppcoinsFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), EarnAppcoinsView {

  private lateinit var presenter: EarnAppcoinsPresenter
  private lateinit var iabView: IabView

  @Inject
  lateinit var analytics: BillingAnalytics

  private val binding by viewBinding(EarnAppcoinsLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      analytics.sendPaymentEvent(
        domain, skuId, amount.toString(),
        PAYMENT_METHOD_NAME, type
      )
    }
    presenter = EarnAppcoinsPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    super.onCreate(savedInstanceState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Earn Appcoins fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.dialogBuyButtonsPaymentMethods.buyButton.setText(getString(R.string.discover_button))
    binding.dialogBuyButtonsPaymentMethods.cancelButton.setText(getString(R.string.back_button))
    iabView.disableBack()
    presenter.present()
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = EarnAppcoinsLayoutBinding.inflate(inflater).root

  override fun backButtonClick(): Observable<Any> {
    return RxView.clicks(binding.dialogBuyButtonsPaymentMethods.cancelButton)
  }

  override fun discoverButtonClick(): Observable<Any> {
    return RxView.clicks(binding.dialogBuyButtonsPaymentMethods.buyButton)
  }

  override fun navigateBack() {
    iabView.showPaymentMethodsView()
  }

  override fun backPressed() = iabView.backButtonPress()

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.destroy()
    super.onDestroyView()
  }

  val domain: String by lazy {
    if (requireArguments().containsKey(PARAM_DOMAIN)) {
      requireArguments().getString(PARAM_DOMAIN, "")
    } else {
      throw IllegalArgumentException("Domain not found")
    }
  }

  val skuId: String? by lazy {
    if (requireArguments().containsKey(PARAM_SKUID)) {
      val value = requireArguments().getString(PARAM_SKUID) ?: return@lazy null
      value
    } else {
      throw IllegalArgumentException("SkuId not found")
    }
  }

  val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(PARAM_AMOUNT)) {
      val value = requireArguments().getSerializable(PARAM_AMOUNT) as BigDecimal
      value
    } else {
      throw IllegalArgumentException("amount not found")
    }
  }

  val type: String by lazy {
    if (requireArguments().containsKey(PARAM_TRANSACTION_TYPE)) {
      requireArguments().getString(PARAM_TRANSACTION_TYPE, "")
    } else {
      throw IllegalArgumentException("type not found")
    }
  }

  companion object {

    @JvmStatic
    fun newInstance(
      domain: String, skuId: String?, amount: BigDecimal,
      type: String
    ): EarnAppcoinsFragment = EarnAppcoinsFragment().apply {
      arguments = Bundle().apply {
        putString(PARAM_DOMAIN, domain)
        putString(PARAM_SKUID, skuId)
        putString(PARAM_TRANSACTION_TYPE, type)
        putSerializable(PARAM_AMOUNT, amount)
      }
    }

    private const val APTOIDE_EARN_APPCOINS_DEEP_LINK =
      "aptoide://cm.aptoide.pt/deeplink?name=appcoins_ads"
    private const val PARAM_DOMAIN = "AMOUNT_DOMAIN"
    private const val PARAM_SKUID = "AMOUNT_SKUID"
    private const val PARAM_AMOUNT = "PARAM_AMOUNT"
    private const val PARAM_TRANSACTION_TYPE = "PARAM_TRANSACTION_TYPE"
    private const val PAYMENT_METHOD_NAME = "EARN_APPCOINS_BUNDLE"
  }
}
