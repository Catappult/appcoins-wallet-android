package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ConfirmationRouter
import com.asfoundation.wallet.ui.ActivityResultSharer
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.currency_choose_layout.*
import kotlinx.android.synthetic.main.transact_fragment_layout.*
import java.math.BigDecimal
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
  @Inject
  lateinit var confirmationRouter: ConfirmationRouter
  @Inject
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  @Inject
  lateinit var defaultTokenInfoProvider: DefaultTokenProvider

  lateinit var navigator: TransactNavigator
  private lateinit var activityResultSharer: ActivityResultSharer
  private var disposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    disposable =
        confirmationRouter.transactionResult
            .doOnNext { activity?.onBackPressed() }
            .subscribe()
    presenter = TransactPresenter(this, CompositeDisposable(), interactor, Schedulers.io(),
        AndroidSchedulers.mainThread(), findDefaultWalletInteract, context!!.packageName)
  }

  override fun openEthConfirmationView(walletAddress: String, toWalletAddress: String,
                                       amount: BigDecimal): Completable {
    return Completable.fromAction {
      val transaction = TransactionBuilder(TokenInfo(null, "Ethereum", "ETH", 18, true, false))
      transaction.amount(amount)
      transaction.toAddress(toWalletAddress)
      transaction.fromAddress(walletAddress)
      confirmationRouter.open(activity, transaction)
    }
  }

  override fun openAppcConfirmationView(walletAddress: String, toWalletAddress: String,
                                        amount: BigDecimal): Completable {

    return defaultTokenInfoProvider.defaultToken.doOnSuccess {
      val transaction = TransactionBuilder(it)
      transaction.amount(amount)
      transaction.toAddress(toWalletAddress)
      transaction.fromAddress(walletAddress)
      confirmationRouter.open(activity, transaction)
    }.ignoreElement()
  }

  override fun openAppcCreditsConfirmationView(walletAddress: String,
                                               amount: BigDecimal,
                                               currency: TransactFragmentView.Currency): Completable {
    return Completable.fromAction {
      val currencyName = when (currency) {
        TransactFragmentView.Currency.APPC_C -> getString(R.string.p2p_send_currency_appc_c)
        TransactFragmentView.Currency.APPC -> getString(R.string.p2p_send_currency_appc)
        TransactFragmentView.Currency.ETH -> getString(R.string.p2p_send_currency_eth)
      }
      navigator.openAppcoinsCreditsSuccess(walletAddress, amount, currencyName)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.transact_fragment_layout, container, false)
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
  }

  override fun onAttach(context: Context?) {
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
    activityResultSharer.addOnActivityListener(confirmationRouter)
  }

  override fun getSendClick(): Observable<TransactFragmentView.TransactData> {
    return RxView.clicks(send_button).map {
      var amount = BigDecimal.ZERO
      if (!transact_fragment_amount.text.toString().isEmpty()) {
        amount = transact_fragment_amount.text.toString().toBigDecimal()
      }
      TransactFragmentView.TransactData(transact_fragment_recipient_address.text.toString(),
          map(currency_selector.checkedRadioButtonId), amount)
    }
  }

  override fun showInvalidWalletAddress() {
    transact_fragment_amount_layout.error = null
    transact_fragment_recipient_address_layout.error = getString(R.string.p2p_send_error_address)
  }

  override fun showUnknownError() {
    Snackbar.make(title, R.string.error_general, Snackbar.LENGTH_LONG).show()
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

  private fun map(checkedRadioButtonId: Int): TransactFragmentView.Currency {
    return when (checkedRadioButtonId) {
      R.id.appcoins_credits_radio_button -> TransactFragmentView.Currency.APPC_C
      R.id.appcoins_radio_button -> TransactFragmentView.Currency.APPC
      R.id.ethereum_credits_radio_button -> TransactFragmentView.Currency.ETH
      else -> throw UnsupportedOperationException("Unknown selected currency")
    }
  }

  override fun onDestroy() {
    disposable?.takeIf {
      !it.isDisposed
    }.let { it?.dispose() }
    super.onDestroy()
  }
}
