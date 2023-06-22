package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.TransactSuccessFragmentLayoutBinding
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class AppcoinsCreditsTransferSuccessFragment : BasePageViewFragment(),
    AppcoinsCreditsTransactSuccessView {
  companion object {
    const val AMOUNT_SENT_KEY = "amount_sent"
    const val CURRENCY_KEY = "currency_key"
    const val TO_ADDRESS_KEY = "to_address"

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

  private val binding by viewBinding(TransactSuccessFragmentLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val amount = requireArguments().getSerializable(AMOUNT_SENT_KEY) as BigDecimal
    val currency = requireArguments().getString(CURRENCY_KEY)!!
    val toAddress = requireArguments().getString(TO_ADDRESS_KEY)!!
    presenter = AppcoinsCreditsTransactSuccessPresenter(this, amount, currency, toAddress,
        CompositeDisposable(), formatter)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = TransactSuccessFragmentLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(binding.transferSuccessOkButton)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
//    when (context) {
//      is TransactNavigator -> navigator = context
//      else -> throw IllegalArgumentException(
//          "${this.javaClass.simpleName} has to be attached to an activity that implements ${ActivityResultSharer::class}")
//    }
  }

  override fun close() {
    findNavController().popBackStack()
  }

  override fun setup(amount: String, currency: String, toAddress: String) {
    binding.transferSuccessWallet.text = toAddress
    binding.transferSuccessMessage.text =
        getString(R.string.p2p_send_confirmation_message, amount, currency)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
