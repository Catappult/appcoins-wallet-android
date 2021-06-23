package com.asfoundation.wallet.withdraw.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.asf.wallet.R
import com.asfoundation.wallet.withdraw.repository.WithdrawRepository
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawFragment : DaggerFragment(), WithdrawView {

  @Inject
  lateinit var withdrawRepository: WithdrawRepository

  lateinit var presenter: WithdrawPresenter

  companion object {
    fun newInstance(): WithdrawFragment {
      val fragment = WithdrawFragment()

      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WithdrawPresenter(this, withdrawRepository, CompositeDisposable(), Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_withdraw, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun showError(error: Throwable) {
    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG)
        .show()
  }

  override fun showWithdrawSuccessMessage() {
    Toast.makeText(context, "Withdraw completed", Toast.LENGTH_LONG)
        .show()
  }

  override fun getWithdrawClicks(): Observable<Pair<String, BigDecimal>> {
    val amountEditText = view!!.findViewById<EditText>(R.id.amount)
    val emailEditText = view!!.findViewById<EditText>(R.id.paypal_email)
    return RxView.clicks(view!!.findViewById(R.id.withdraw_button))
        .map {
          Pair(emailEditText.text.toString(),
              BigDecimal(amountEditText.text.toString()))
        }
  }

}
