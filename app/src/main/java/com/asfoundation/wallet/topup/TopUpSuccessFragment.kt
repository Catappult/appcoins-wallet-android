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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.FragmentTopUpSuccessBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
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

  private val binding by viewBinding(FragmentTopUpSuccessBinding::bind)

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
                            savedInstanceState: Bundle?): View = FragmentTopUpSuccessBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    if (bonus.isNotEmpty() && bonus != "0") {
      binding.topUpSuccessAnimation.setAnimation(R.raw.top_up_bonus_success_animation)
      setAnimationText()
      formatBonusSuccessMessage()
    } else {
      binding.topUpSuccessAnimation.setAnimation(R.raw.top_up_success_animation)
      formatSuccessMessage()
    }
    binding.topUpSuccessAnimation.playAnimation()
  }

  override fun clean() {
    binding.topUpSuccessAnimation.removeAllAnimatorListeners()
    binding.topUpSuccessAnimation.removeAllUpdateListeners()
    binding.topUpSuccessAnimation.removeAllLottieOnCompositionLoadedListener()
  }

  override fun close() {
    topUpActivityView.close()
  }

  override fun getOKClicks(): Observable<Any> {
    return RxView.clicks(binding.button)
  }

  private fun setAnimationText() {
    val formattedBonus = formatter.formatCurrency(bonus, WalletCurrency.FIAT)
    val textDelegate = TextDelegate(binding.topUpSuccessAnimation)
    textDelegate.setText("bonus_value", "$currencySymbol$formattedBonus")
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    binding.topUpSuccessAnimation.setTextDelegate(textDelegate)
    binding.topUpSuccessAnimation.setFontAssetDelegate(object : FontAssetDelegate() {
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
    binding.value.text = sb
  }
}
