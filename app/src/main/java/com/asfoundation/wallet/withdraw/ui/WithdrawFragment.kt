package com.asfoundation.wallet.withdraw.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.asf.wallet.R
import com.asfoundation.wallet.withdraw.usecase.WithdrawFiatUseCase
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
  lateinit var withdrawUseCase: WithdrawFiatUseCase

  lateinit var presenter: WithdrawPresenter
  lateinit var amountEditText: EditText
  lateinit var emailEditText: EditText
  lateinit var loadingView: View
  lateinit var withdrawButton: Button

  companion object {
    fun newInstance(): WithdrawFragment {
      return WithdrawFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WithdrawPresenter(
      this, withdrawUseCase, CompositeDisposable(), Schedulers.io(),
      AndroidSchedulers.mainThread()
    )
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
    amountEditText = view.findViewById(R.id.amount)
    emailEditText = view.findViewById(R.id.paypal_email)
    loadingView = view.findViewById(R.id.loading_layout)
    withdrawButton = view.findViewById(R.id.withdraw_button)
    presenter.present()
  }

  override fun showInvalidEmailError() {
    emailEditText.error = getString(R.string.e_skills_withdraw_invalid_email_error_message)
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun showError(error: Throwable) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.unknown_error)
      .setPositiveButton(
        R.string.ok
      ) { dialog, _ -> dialog.dismiss() }
      .show()
  }

  override fun showWithdrawSuccessMessage() {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.transaction_status_success)
      .setPositiveButton(
        R.string.ok
      ) { dialog, _ ->
        dialog.dismiss()
        activity?.onBackPressed()
      }
      .show()
  }

  override fun showNotEnoughBalanceError() {
    amountEditText.error = getString(R.string.e_skills_withdraw_invalid_email_error_message)
  }

  override fun showNotEnoughEarningsBalanceError() {
    amountEditText.error = getString(R.string.e_skills_withdraw_invalid_email_error_message)
  }

  override fun showLoading() {
    withdrawButton.isEnabled = false
    loadingView.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    withdrawButton.isEnabled = true
    loadingView.visibility = View.GONE

  }

  override fun showNoNetworkError() {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.activity_iab_no_network_message)
      .setPositiveButton(
        R.string.ok
      ) { dialog, _ -> dialog.dismiss() }
      .show()
  }

  override fun getWithdrawClicks(): Observable<Pair<String, BigDecimal>> {
    return RxView.clicks(withdrawButton)
      .map {
        Pair(
          emailEditText.text.toString(),
          BigDecimal(amountEditText.text.toString())
        )
      }
  }

}
