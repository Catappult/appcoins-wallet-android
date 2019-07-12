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
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_top_up_success.*
import java.math.BigDecimal
import java.math.RoundingMode

class TopUpSuccessFragment : DaggerFragment(), TopUpSuccessFragmentView {

  companion object {
    @JvmStatic
    fun newInstance(amount: String, currency: String, bonus: String,
                    validBonus: Boolean): TopUpSuccessFragment {
      val fragment = TopUpSuccessFragment()
      val bundle = Bundle()
      bundle.putString(PARAM_AMOUNT, amount)
      bundle.putString(CURRENCY, currency)
      bundle.putString(BONUS, bonus)
      bundle.putBoolean(VALID_BONUS, validBonus)
      fragment.arguments = bundle
      return fragment
    }

    private const val PARAM_AMOUNT = "amount"
    private const val CURRENCY = "currency"
    private const val BONUS = "bonus"
    private const val VALID_BONUS = "valid_bonus"
  }

  private lateinit var presenter: TopUpSuccessPresenter

  private lateinit var topUpActivityView: TopUpActivityView
  val amount: String? by lazy {
    if (arguments!!.containsKey(PARAM_AMOUNT)) {
      arguments!!.getString(PARAM_AMOUNT)
    } else {
      throw IllegalArgumentException("product name not found")
    }
  }

  val currency: String? by lazy {
    if (arguments!!.containsKey(CURRENCY)) {
      arguments!!.getString(CURRENCY)
    } else {
      throw IllegalArgumentException("currency not found")
    }
  }

  val bonus: String? by lazy {
    if (arguments!!.containsKey(BONUS)) {
      arguments!!.getString(BONUS)
    } else {
      throw IllegalArgumentException("bonus not found")
    }
  }

  val validBonus: Boolean by lazy {
    if (arguments!!.containsKey(VALID_BONUS)) {
      arguments!!.getBoolean(VALID_BONUS)
    } else {
      throw IllegalArgumentException("valid bonus not found")
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
    topUpActivityView.showToolbar()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    if (validBonus) {
      top_up_success_animation.setAnimation(R.raw.top_up_bonus_success_animation)
      setAnimationText()
    } else {
      top_up_success_animation.setAnimation(R.raw.top_up_success_animation)
    }
    top_up_success_animation.playAnimation()
    formatBonusSuccessMessage()
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
    val textDelegate = TextDelegate(top_up_success_animation)
    textDelegate.setText("bonus_value", bonus)
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
    val fiatValue =
        BigDecimal(amount).setScale(2, RoundingMode.FLOOR).toString() + " " + currency
    val formattedInitialString = String.format(
        resources.getString(R.string.topup_completed_1), fiatValue)
    val topUpString =
        formattedInitialString + " " + resources.getString(R.string.topup_completed_2_with_bonus)
    val boldStyle = StyleSpan(Typeface.BOLD)
    val sb = SpannableString(topUpString)
    sb.setSpan(boldStyle, 0, formattedInitialString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    value.text = sb
  }

}