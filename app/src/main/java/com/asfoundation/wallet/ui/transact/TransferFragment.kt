package com.asfoundation.wallet.ui.transact

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.TransactFragmentLayoutBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TransferFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), TransferFragmentView {

  companion object {
    fun newInstance() = TransferFragment()
  }

  @Inject
  lateinit var presenter: TransferFragmentPresenter

  private lateinit var doneClick: PublishSubject<Any>
  private lateinit var qrCodeResult: BehaviorSubject<Barcode>

  private val binding by viewBinding(TransactFragmentLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    doneClick = PublishSubject.create()
    qrCodeResult = BehaviorSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = TransactFragmentLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
    binding.transactFragmentAmount.setOnEditorActionListener(
        TextView.OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            doneClick.onNext(Any())
            return@OnEditorActionListener true
          }
          return@OnEditorActionListener false
        })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data)
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun getCurrencyChange(): Observable<TransferFragmentView.Currency> {
    return RxRadioGroup.checkedChanges(binding.currencyChooseLayout.currencySelector)
        .map { map(binding.currencyChooseLayout.currencySelector.checkedRadioButtonId) }
  }

  override fun showBalance(balance: String,
                           currency: WalletCurrency) {
    binding.transactFragmentBalance.text =
        getString(R.string.p2p_send_current_balance_message, balance, currency.symbol)
  }

  override fun showCameraErrorToast() {
    Toast.makeText(context, R.string.toast_qr_code_no_address, LENGTH_SHORT)
        .show()
  }

  override fun showAddress(address: String) {
    binding.transactFragmentRecipientAddress.setText(address)
  }

  override fun getSendClick(): Observable<TransferFragmentView.TransferData> {
    return Observable.merge(doneClick, RxView.clicks(binding.sendButton))
        .map {
          var amount = BigDecimal.ZERO
          if (binding.transactFragmentAmount.text.toString()
                  .isNotEmpty()) {
            amount = binding.transactFragmentAmount.text.toString()
                .toBigDecimal()
          }
          TransferFragmentView.TransferData(
            binding.transactFragmentRecipientAddress.text.toString()
                  .toLowerCase(Locale.ROOT),
              map(binding.currencyChooseLayout.currencySelector.checkedRadioButtonId), amount)
        }
  }

  override fun showInvalidWalletAddress() {
    binding.transactFragmentAmountLayout.error = null
    binding.transactFragmentRecipientAddressLayout.error = getString(R.string.p2p_send_error_address)
    binding.scanBarcodeButton.visibility = View.GONE
  }

  override fun getQrCodeResult(): Observable<Barcode> = qrCodeResult

  override fun getQrCodeButtonClick(): Observable<Any> {
    return RxView.clicks(binding.scanBarcodeButton)
  }

  override fun showUnknownError() {
    Snackbar.make(binding.title, R.string.error_general, Snackbar.LENGTH_LONG)
        .show()
  }

  override fun showNoNetworkError() {
    Snackbar.make(binding.title, R.string.connection_error_body, Snackbar.LENGTH_LONG)
        .show()
  }

  override fun showInvalidAmountError() {
    binding.transactFragmentRecipientAddressLayout.error = null
    binding.transactFragmentAmountLayout.error = getString(R.string.p2p_send_error_amount_zero)
  }

  override fun showNotEnoughFunds() {
    binding.transactFragmentRecipientAddressLayout.error = null
    binding.transactFragmentAmountLayout.error = getString(R.string.p2p_send_error_not_enough_funds)
  }

  override fun hideKeyboard() {
    val inputMethodManager =
        activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity?.currentFocus
    if (view == null) view = View(activity)
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
  }

  override fun lockOrientation() {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockOrientation() {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  private fun map(checkedRadioButtonId: Int): TransferFragmentView.Currency {
    return when (checkedRadioButtonId) {
      R.id.appcoins_credits_radio_button -> TransferFragmentView.Currency.APPC_C
      R.id.appcoins_radio_button -> TransferFragmentView.Currency.APPC
      R.id.ethereum_credits_radio_button -> TransferFragmentView.Currency.ETH
      else -> throw UnsupportedOperationException("Unknown selected currency")
    }
  }

  override fun onPause() {
    presenter.clearOnPause()
    super.onPause()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
