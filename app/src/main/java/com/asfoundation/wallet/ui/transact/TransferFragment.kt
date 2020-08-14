package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ConfirmationRouter
import com.asfoundation.wallet.ui.ActivityResultSharer
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.currency_choose_layout.*
import kotlinx.android.synthetic.main.transact_fragment_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class TransferFragment : BasePageViewFragment(), TransferFragmentView {
  companion object {
    fun newInstance(): TransferFragment {
      return TransferFragment()
    }
  }

  private lateinit var presenter: TransferPresenter

  @Inject
  lateinit var interactor: TransferInteractor

  @Inject
  lateinit var confirmationRouter: ConfirmationRouter

  @Inject
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract

  @Inject
  lateinit var defaultTokenInfoProvider: DefaultTokenProvider

  @Inject
  lateinit var walletBlockedInteract: WalletBlockedInteract

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  lateinit var navigator: TransactNavigator
  lateinit var transferActivity: TransferActivityView
  private lateinit var activityResultSharer: ActivityResultSharer
  private lateinit var doneClick: PublishSubject<Any>
  private lateinit var qrCodeResult: BehaviorSubject<Barcode>
  private var disposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    doneClick = PublishSubject.create()
    qrCodeResult = BehaviorSubject.create()
    disposable =
        confirmationRouter.transactionResult
            .doOnNext { activity?.onBackPressed() }
            .subscribe()
    presenter = TransferPresenter(this, CompositeDisposable(), interactor, Schedulers.io(),
        AndroidSchedulers.mainThread(), findDefaultWalletInteract, walletBlockedInteract,
        context!!.packageName, formatter)
  }

  override fun openEthConfirmationView(walletAddress: String, toWalletAddress: String,
                                       amount: BigDecimal): Completable {
    return Completable.fromAction {
      val transaction = TransactionBuilder(TokenInfo(null, "Ethereum", "ETH", 18))
      transaction.amount(amount)
      transaction.toAddress(toWalletAddress)
      transaction.fromAddress(walletAddress)
      confirmationRouter.open(activity, transaction)
    }
  }

  override fun openAppcConfirmationView(walletAddress: String, toWalletAddress: String,
                                        amount: BigDecimal): Completable {

    return defaultTokenInfoProvider.defaultToken.doOnSuccess {
      with(TransactionBuilder(it)) {
        amount(amount)
        toAddress(toWalletAddress)
        fromAddress(walletAddress)
        confirmationRouter.open(activity, this)
      }
    }
        .ignoreElement()
  }

  override fun openAppcCreditsConfirmationView(walletAddress: String,
                                               amount: BigDecimal,
                                               currency: TransferFragmentView.Currency): Completable {
    return Completable.fromAction {
      val currencyName = when (currency) {
        TransferFragmentView.Currency.APPC_C -> getString(R.string.p2p_send_currency_appc_c)
        TransferFragmentView.Currency.APPC -> getString(R.string.p2p_send_currency_appc)
        TransferFragmentView.Currency.ETH -> getString(R.string.p2p_send_currency_eth)
      }
      navigator.openAppcoinsCreditsSuccess(walletAddress, amount, currencyName)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.transact_fragment_layout, container, false)
  }

  override fun getCurrencyChange(): Observable<TransferFragmentView.Currency> {
    return RxRadioGroup.checkedChanges(currency_selector)
        .map { map(currency_selector.checkedRadioButtonId) }
  }

  override fun showBalance(balance: String,
                           currency: WalletCurrency) {
    transact_fragment_balance.text =
        getString(R.string.p2p_send_current_balance_message, balance, currency.symbol)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    transact_fragment_amount.setOnEditorActionListener(
        TextView.OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            navigator.hideKeyboard()
            doneClick.onNext(Any())
            return@OnEditorActionListener true
          }
          return@OnEditorActionListener false
        })
  }

  override fun showWalletBlocked() {
    navigator.showWalletBlocked()
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    when (context) {
      is ActivityResultSharer -> activityResultSharer = context
      else -> throw IllegalArgumentException(
          "${this.javaClass.simpleName} has to be attached to an activity that implements ${ActivityResultSharer::class}")
    }
    when (context) {
      is TransactNavigator -> navigator = context
      else -> throw IllegalArgumentException(
          "${this.javaClass.simpleName} has to be attached to an activity that implements ${ActivityResultSharer::class}")
    }
    when (context) {
      is TransferActivityView -> transferActivity = context
      else -> throw IllegalArgumentException(
          "${this.javaClass.simpleName} has to be attached to an activity that implements ${ActivityResultSharer::class}")
    }
    activityResultSharer.addOnActivityListener(confirmationRouter)
    activityResultSharer.addOnActivityListener(object :
        ActivityResultSharer.ActivityResultListener {
      override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode == CommonStatusCodes.SUCCESS) {
          data?.let {
            val barcode = it.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
            println(barcode)
            qrCodeResult.onNext(barcode)
            return true
          }
        }
        return false
      }
    })
  }


  override fun showCameraErrorToast() {
    Toast.makeText(context, R.string.toast_qr_code_no_address, LENGTH_SHORT)
        .show()
  }

  override fun showAddress(address: String) {
    transact_fragment_recipient_address.setText(address)
  }

  override fun getQrCodeResult(): Observable<Barcode> {
    return qrCodeResult
  }

  override fun getSendClick(): Observable<TransferFragmentView.TransferData> {
    return Observable.merge(doneClick, RxView.clicks(send_button))
        .map {
          var amount = BigDecimal.ZERO
          if (transact_fragment_amount.text.toString()
                  .isNotEmpty()) {
            amount = transact_fragment_amount.text.toString()
                .toBigDecimal()
          }
          TransferFragmentView.TransferData(
              transact_fragment_recipient_address.text.toString()
                  .toLowerCase(),
              map(currency_selector.checkedRadioButtonId), amount)
        }
  }

  override fun showInvalidWalletAddress() {
    transact_fragment_amount_layout.error = null
    transact_fragment_recipient_address_layout.error = getString(R.string.p2p_send_error_address)
  }

  override fun getQrCodeButtonClick(): Observable<Any> {
    return RxView.clicks(scan_barcode_button)
  }

  override fun showQrCodeScreen() {
    navigator.openQrCodeScreen()
  }

  override fun showUnknownError() {
    Snackbar.make(title, R.string.error_general, Snackbar.LENGTH_LONG)
        .show()
  }

  override fun showNoNetworkError() {
    Snackbar.make(title, R.string.connectoin_error_body, Snackbar.LENGTH_LONG)
        .show()
  }

  override fun showInvalidAmountError() {
    transact_fragment_recipient_address_layout.error = null
    transact_fragment_amount_layout.error = getString(R.string.p2p_send_error_amount_zero)
  }

  override fun showNotEnoughFunds() {
    transact_fragment_recipient_address_layout.error = null
    transact_fragment_amount_layout.error = getString(R.string.p2p_send_error_not_enough_funds)
  }

  override fun showLoading() {
    navigator.showLoading()
  }

  override fun hideLoading() {
    navigator.hideLoading()
  }

  override fun onDetach() {
    activityResultSharer.remove(confirmationRouter)
    super.onDetach()
  }

  override fun onPause() {
    presenter.clear()
    super.onPause()
  }

  private fun map(checkedRadioButtonId: Int): TransferFragmentView.Currency {
    return when (checkedRadioButtonId) {
      R.id.appcoins_credits_radio_button -> TransferFragmentView.Currency.APPC_C
      R.id.appcoins_radio_button -> TransferFragmentView.Currency.APPC
      R.id.ethereum_credits_radio_button -> TransferFragmentView.Currency.ETH
      else -> throw UnsupportedOperationException("Unknown selected currency")
    }
  }

  override fun onDestroy() {
    disposable?.takeIf {
      !it.isDisposed
    }
        .let { it?.dispose() }
    super.onDestroy()
  }
}
