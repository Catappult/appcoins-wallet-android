package com.asfoundation.wallet.ui.iab.payments.carrier.status

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentCarrierPaymentStatusBinding
import com.asfoundation.wallet.ui.iab.IabView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class CarrierPaymentFragment : BasePageViewFragment(), CarrierPaymentView {

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: CarrierPaymentPresenter
  lateinit var iabView: IabView

  private val views by lazy { FragmentCarrierPaymentStatusBinding.bind(requireView()) }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentCarrierPaymentStatusBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  private fun setupUi() {
    iabView.disableBack()
    lockRotation()

  }

  override fun initializeView(bonusValue: BigDecimal?, currency: String) {
    if (bonusValue != null) {
      views.completePaymentView.lottieTransactionSuccess.setAnimation(
          R.raw.transaction_complete_bonus_animation_new)
      val textDelegate = TextDelegate(views.completePaymentView.lottieTransactionSuccess)
      textDelegate.setText("bonus_value", getBonusMessage(bonusValue, currency))
      textDelegate.setText("bonus_received",
          resources.getString(R.string.gamification_purchase_completed_bonus_received))
      views.completePaymentView.lottieTransactionSuccess.setTextDelegate(textDelegate)
      views.completePaymentView.lottieTransactionSuccess.setFontAssetDelegate(object :
          FontAssetDelegate() {
        override fun fetchFont(fontFamily: String?) =
            Typeface.create("sans-serif-medium", Typeface.BOLD)
      })
    } else {
      views.completePaymentView.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    }
  }

  private fun getBonusMessage(bonusValue: BigDecimal, currency: String): String {
    var scaledBonus = bonusValue.stripTrailingZeros()
        .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currency
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currency"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    return newCurrencyString + formattedBonus
  }

  override fun onDestroyView() {
    iabView.enableBack()
    unlockRotation()
    presenter.stop()
    super.onDestroyView()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "CarrierPaymentStatusFragment must be attached to IAB activity" }
    iabView = context
  }

  private fun unlockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  private fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun setLoading() {
    views.progressBar.visibility = View.VISIBLE
    views.completePaymentView.root.visibility = View.INVISIBLE
  }

  override fun showFinishedTransaction() {
    views.completePaymentView.root.visibility = View.VISIBLE
    views.progressBar.visibility = View.INVISIBLE
  }

  override fun getFinishedDuration(): Long =
      views.completePaymentView.lottieTransactionSuccess.duration

  companion object {
    val TAG = CarrierPaymentFragment::class.java.simpleName

    internal const val DOMAIN_KEY = "domain"
    internal const val TRANSACTION_DATA_KEY = "transaction_data"
    internal const val TRANSACTION_TYPE_KEY = "transaction_type"
    internal const val SKU_ID_KEY = "sku_id"
    internal const val APPC_AMOUNT_KEY = "appc_amount"
    internal const val PAYMENT_URL = "payment_url"
    internal const val CURRENCY_KEY = "currency"
    internal const val BONUS_AMOUNT_KEY = "bonus_amount"
    internal const val PHONE_NUMBER_KEY = "phone_number"

    @JvmStatic
    fun newInstance(domain: String, transactionData: String,
                    transactionType: String, skuId: String?, paymentUrl: String,
                    appcAmount: BigDecimal, currency: String, bonus: BigDecimal?,
                    phoneNumber: String): CarrierPaymentFragment {
      val fragment = CarrierPaymentFragment()
      fragment.arguments = Bundle().apply {
        putString(DOMAIN_KEY, domain)
        putString(TRANSACTION_DATA_KEY, transactionData)
        putString(TRANSACTION_TYPE_KEY, transactionType)
        putString(SKU_ID_KEY, skuId)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putString(PAYMENT_URL, paymentUrl)
        putString(CURRENCY_KEY, currency)
        putSerializable(BONUS_AMOUNT_KEY, bonus)
        putString(PHONE_NUMBER_KEY, phoneNumber)
      }
      return fragment
    }
  }
}