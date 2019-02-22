package com.asfoundation.wallet.ui.transact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.currency_choose_layout.*
import kotlinx.android.synthetic.main.transact_fragment_layout.*
import javax.inject.Inject

class TransactFragment : DaggerFragment(), TransactFragmentView {
  companion object {
    fun newInstance(): TransactFragment {
      return TransactFragment()
    }
  }

  private lateinit var presenter: TransactPresenter
  @Inject
  lateinit var interactor: TransferInteractor

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = TransactPresenter(this, CompositeDisposable(), interactor)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.transact_fragment_layout, container, false)
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
  }

  override fun getSendClick(): Observable<TransactFragmentView.PaymentData> {
    return RxView.clicks(send_button).map {
      TransactFragmentView.PaymentData(transact_fragment_recipient_address.text.toString(),
          map(currency_selector.checkedRadioButtonId),
          transact_fragment_amount.text.toString().toInt())
    }
  }

  private fun map(checkedRadioButtonId: Int): TransactFragmentView.Currency {
    return when (checkedRadioButtonId) {
      R.id.appcoins_credits_radio_button -> TransactFragmentView.Currency.APPC_C
      R.id.appcoins_radio_button -> TransactFragmentView.Currency.APPC
      R.id.ethereum_credits_radio_button -> TransactFragmentView.Currency.ETH
      else -> throw UnsupportedOperationException("Unknown selected currency")
    }
  }
}
