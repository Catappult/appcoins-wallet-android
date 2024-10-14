package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
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
      amount: String, currency: String, bonus: String,
      currencySymbol: String, pendingFinalConfirmation: Boolean,
    ): TopUpSuccessFragment {
      return TopUpSuccessFragment().apply {
        arguments = Bundle().apply {
          putString(PARAM_AMOUNT, amount)
          putString(CURRENCY, currency)
          putString(CURRENCY_SYMBOL, currencySymbol)
          putString(BONUS, bonus)
          putBoolean(PENDING_FINAL_CONFIRMATION, pendingFinalConfirmation)
        }
      }
    }

    private const val PARAM_AMOUNT = "amount"
    private const val CURRENCY = "currency"
    private const val CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS = "bonus"
    private const val PENDING_FINAL_CONFIRMATION = "pending_final_confirmation"
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

  private val pendingFinalConfirmation: Boolean by lazy {
    if (requireArguments().containsKey(PENDING_FINAL_CONFIRMATION)) {
      requireArguments().getBoolean(PENDING_FINAL_CONFIRMATION)
    } else {
      false
    }
  }

  private val binding by viewBinding(FragmentTopUpSuccessBinding::bind)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
        "Express checkout buy fragment must be attached to IAB activity"
      )
    }
    topUpActivityView = context
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = TopUpSuccessPresenter(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentTopUpSuccessBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    val bonusAvailable = bonus.takeIf { it.isNotEmpty() && it != "0" }
    when {
      pendingFinalConfirmation -> {
        binding.topUpSuccessAnimation.visibility = View.GONE
        binding.topUpPendingSuccessAnimation.visibility = View.VISIBLE
        formatPendingSuccessMessage()
        bonusAvailable?.let {
          setBonusText(isPendingSuccess = true)
        } ?: run { binding.bonusViews.visibility = View.GONE }
      }

      else -> {
        bonusAvailable?.let {
          setBonusText(isPendingSuccess = false)
        } ?: run { binding.bonusViews.visibility = View.GONE }
        formatSuccessMessage()
        binding.topUpSuccessAnimation.setAnimation(R.raw.top_up_success_animation)
        binding.topUpSuccessAnimation.playAnimation()
      }
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

  private fun formatSuccessMessage() {
    val initialString = getFormattedTopUpValue()
    val secondString = String.format(resources.getString(R.string.topup_completed_2_without_bonus))
    binding.value.text = "$initialString\n$secondString"
  }

  private fun formatPendingSuccessMessage() {
    val initialString = formatter.formatCurrency(amount!!, WalletCurrency.FIAT) + " " + currency
    val completedString = String.format(
      resources.getString(R.string.purchase_bank_transfer_success_disclaimer),
      initialString
    )
    binding.value.text = completedString
  }

  private fun getFormattedTopUpValue(): String {
    val fiatValue =
      formatter.formatCurrency(amount!!, WalletCurrency.FIAT) + " " + currency
    return String.format(resources.getString(R.string.topup_completed_1), fiatValue)
  }

}
