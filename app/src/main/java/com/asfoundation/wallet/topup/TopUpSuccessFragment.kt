package com.asfoundation.wallet.topup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.lottie.LottieDrawable
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentTopUpSuccessBinding
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class TopUpSuccessFragment : BasePageViewFragment(), TopUpSuccessFragmentView {

  companion object {
    @JvmStatic
    fun newInstance(
      amount: String,
      currency: String,
      bonus: String,
      currencySymbol: String,
      pendingFinalConfirmation: Boolean,
    ) = TopUpSuccessFragment().apply {
      arguments = bundleOf(
        PARAM_AMOUNT to amount,
        CURRENCY to currency,
        CURRENCY_SYMBOL to currencySymbol,
        BONUS to bonus,
        PENDING_FINAL_CONFIRMATION to pendingFinalConfirmation,
      )
    }

    private const val PARAM_AMOUNT = "amount"
    private const val CURRENCY = "currency"
    private const val CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS = "bonus"
    private const val PENDING_FINAL_CONFIRMATION = "pending_final_confirmation"
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val presenter: TopUpSuccessPresenter by lazy { TopUpSuccessPresenter(this) }

  private val topUpActivityView get() = activity as TopUpActivityView

  private val binding by viewBinding(FragmentTopUpSuccessBinding::bind)

  val amount by lazy {
    requireArguments().getString(PARAM_AMOUNT)
      ?: throw IllegalArgumentException("product name not found")
  }

  val currency by lazy {
    requireArguments().getString(CURRENCY)
      ?: throw IllegalArgumentException("currency not found")
  }

  val bonus by lazy {
    requireArguments().getString(BONUS)
      ?: throw IllegalArgumentException("bonus not found")
  }

  private val currencySymbol by lazy {
    requireArguments().getString(CURRENCY_SYMBOL)
      ?: throw IllegalArgumentException("bonus not found")
  }

  private val pendingFinalConfirmation: Boolean by lazy {
    requireArguments().getBoolean(PENDING_FINAL_CONFIRMATION, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException("Express checkout buy fragment must be attached to IAB activity")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentTopUpSuccessBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    when {
      pendingFinalConfirmation -> {
        handleBonus()
        formatPendingSuccessMessage()
        setAnimation(R.raw.wait_trasaction, LottieDrawable.INFINITE)
      }

      else -> {
        handleBonus()
        formatSuccessMessage()
        setAnimation(R.raw.top_up_success_animation, 0)
      }
    }
  }

  private fun setAnimation(@RawRes animation: Int, repeatCount: Int) {
    binding.topUpSuccessAnimation.setRepeatCount(repeatCount)
    binding.topUpSuccessAnimation.setAnimation(animation)
    binding.topUpSuccessAnimation.playAnimation()
  }

  private fun handleBonus() {
    val bonusAvailable = bonus.takeIf { it.isNotEmpty() && it != "0" }
    if (bonusAvailable != null) {
      setBonusText(isPendingSuccess = pendingFinalConfirmation)
    } else {
      binding.bonusViews.visibility = View.GONE
    }
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

  private fun setBonusText(isPendingSuccess: Boolean) {
    val formattedBonus = "$currencySymbol${formatter.formatCurrency(bonus, WalletCurrency.FIAT)}"
    val bonusText = if (isPendingSuccess)
      getString(R.string.purchase_bank_transfer_success_bonus, formattedBonus)
    else
      getString(R.string.purchase_success_bonus_received_title, formattedBonus)
    binding.bonusReceived.text = bonusText
    binding.bonusViews.visibility = View.VISIBLE
  }

  @SuppressLint("SetTextI18n")
  private fun formatSuccessMessage() {
    val initialString = getFormattedTopUpValue()
    val secondString = String.format(resources.getString(R.string.topup_completed_2_without_bonus))
    binding.value.text = "$initialString\n$secondString"
    binding.info.visibility = View.GONE
  }

  private fun formatPendingSuccessMessage() {
    val initialString = formatter.formatCurrency(amount, WalletCurrency.FIAT) + " " + currency
    val completedString = String.format(
      resources.getString(R.string.purchase_bank_transfer_success_1),
      initialString
    )
    binding.value.text = completedString
    binding.info.text = getString(R.string.purchase_bank_transfer_success_2)
    binding.success.visibility = View.GONE
  }

  private fun getFormattedTopUpValue(): String {
    val fiatValue =
      formatter.formatCurrency(amount, WalletCurrency.FIAT) + " " + currency
    return String.format(resources.getString(R.string.topup_completed_1), fiatValue)
  }

}
