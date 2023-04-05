package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.ActivityResultSharer
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.TransactSuccessFragmentLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class AppcoinsCreditsTransferSuccessFragment : BasePageViewFragment(),
    AppcoinsCreditsTransactSuccessView {
  companion object {
    private const val AMOUNT_SENT_KEY = "AMOUNT_SENT"
    private const val CURRENCY_KEY = "CURRENCY"
    private const val TO_ADDRESS_KEY = "TO_ADDRESS"

    fun newInstance(amount: BigDecimal, currency: String,
                    toAddress: String): AppcoinsCreditsTransferSuccessFragment =
        AppcoinsCreditsTransferSuccessFragment().apply {
          arguments = Bundle(3).apply {
            putSerializable(AMOUNT_SENT_KEY, amount)
            putString(CURRENCY_KEY, currency)
            putString(TO_ADDRESS_KEY, toAddress)
          }
        }
  }

  @Inject
  lateinit var formatter: CurrencyFormatUtils
  private lateinit var presenter: AppcoinsCreditsTransactSuccessPresenter
  private lateinit var navigator: TransactNavigator

  private var _binding: TransactSuccessFragmentLayoutBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val transfer_success_ok_button get() = binding.transferSuccessOkButton
  private val transfer_success_wallet get() = binding.transferSuccessWallet
  private val transfer_success_message get() = binding.transferSuccessMessage

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val amount = requireArguments().getSerializable(AMOUNT_SENT_KEY) as BigDecimal
    val currency = requireArguments().getString(CURRENCY_KEY)!!
    val toAddress = requireArguments().getString(TO_ADDRESS_KEY)!!
    presenter = AppcoinsCreditsTransactSuccessPresenter(this, amount, currency, toAddress,
        CompositeDisposable(), formatter)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = TransactSuccessFragmentLayoutBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(transfer_success_ok_button)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    when (context) {
      is TransactNavigator -> navigator = context
      else -> throw IllegalArgumentException(
          "${this.javaClass.simpleName} has to be attached to an activity that implements ${ActivityResultSharer::class}")
    }
  }

  override fun close() {
    navigator.closeScreen()
  }

  override fun setup(amount: String, currency: String, toAddress: String) {
    transfer_success_wallet.text = toAddress
    transfer_success_message.text =
        getString(R.string.p2p_send_confirmation_message, amount, currency)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
    _binding = null
  }
}
