package com.asfoundation.wallet.topup

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_top_up_success.*
import javax.inject.Inject

@AndroidEntryPoint
class TopUpSuccessFragment : BasePageViewFragment(), TopUpSuccessFragmentView {

  companion object {
    @JvmStatic
    fun newInstance(amount: String, currency: String, bonus: String,
                    currencySymbol: String): TopUpSuccessFragment {
      return TopUpSuccessFragment().apply {
        arguments = Bundle().apply {
          putString(PARAM_AMOUNT, amount)
          putString(CURRENCY, currency)
          putString(CURRENCY_SYMBOL, currencySymbol)
          putString(BONUS, bonus)
        }
      }
    }

    private const val PARAM_AMOUNT = "amount"
    private const val CURRENCY = "currency"
    private const val CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS = "bonus"
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private lateinit var presenter: TopUpSuccessPresenter
  private lateinit var topUpActivityView: TopUpActivityView

  val amount: String? by lazy {
    if (requireArguments().containsKey(PARAM_AMOUNT)) {
      requireArguments().getString(PARAM_AMOUNT)
    } else {
      throw IllegalArgumentException("product name not found")
    }
  }

  val currency: String? by lazy {
    if (requireArguments().containsKey(CURRENCY)) {
      requireArguments().getString(CURRENCY)
    } else {
      throw IllegalArgumentException("currency not found")
    }
  }

  val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS)) {
      requireArguments().getString(BONUS, "")
    } else {
      throw IllegalArgumentException("bonus not found")
    }
  }

  private val currencySymbol: String by lazy {
    if (requireArguments().containsKey(CURRENCY_SYMBOL)) {
      requireArguments().getString(CURRENCY_SYMBOL, "")
    } else {
      throw IllegalArgumentException("bonus not found")
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    topUpActivityView = context
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = TopUpSuccessPresenter(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    if (bonus.isNotEmpty() && bonus != "0") {
      top_up_success_animation.setAnimation(R.raw.top_up_bonus_success_animation)
      setAnimationText()
      formatBonusSuccessMessage()
    } else {
      top_up_success_animation.setAnimation(R.raw.top_up_success_animation)
      formatSuccessMessage()
    }
    top_up_success_animation.playAnimation()
  }

  override fun clean() {
    top_up_success_animation.removeAllAnimatorListeners()
    top_up_success_animation.removeAllUpdateListeners()
    top_up_success_animation.removeAllLottieOnCompositionLoadedListener()
  }

  override fun close() {
    topUpActivityView.close()
  }

  override fun getOKClicks(): Observable<Any> {
    return RxView.clicks(button)
  }

  private fun setAnimationText() {
    val formattedBonus = formatter.formatCurrency(bonus, WalletCurrency.FIAT)
    val textDelegate = TextDelegate(top_up_success_animation)
    textDelegate.setText("bonus_value", "$currencySymbol$formattedBonus")
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    top_up_success_animation.setTextDelegate(textDelegate)
    top_up_success_animation.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String?): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private fun formatBonusSuccessMessage() {
    val formattedInitialString = getFormattedTopUpValue()
    val topUpString =
        formattedInitialString + " " + resources.getString(R.string.topup_completed_2_with_bonus)
    setSpannableString(topUpString, formattedInitialString.length)

  }

  private fun formatSuccessMessage() {
    val formattedInitialString = getFormattedTopUpValue()
    val secondStringFormat =
        String.format(resources.getString(R.string.askafriend_notification_received_body),
            formattedInitialString, "\n")
    setSpannableString(secondStringFormat, formattedInitialString.length)
  }

  private fun getFormattedTopUpValue(): String {
    val fiatValue =
        formatter.formatCurrency(amount!!, WalletCurrency.FIAT) + " " + currency
    return String.format(resources.getString(R.string.topup_completed_1), fiatValue)
  }

  private fun setSpannableString(secondStringFormat: String, firstStringLength: Int) {
    val boldStyle = StyleSpan(Typeface.BOLD)
    val sb = SpannableString(secondStringFormat)
    sb.setSpan(boldStyle, 0, firstStringLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    value.text = sb
  }
}
