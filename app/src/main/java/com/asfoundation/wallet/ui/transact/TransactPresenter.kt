package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val walletInteract: FindDefaultWalletInteract,
                        private val packageName: String) {

  fun present() {
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .doOnNext { view.showLoading() }
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable {
          return@flatMapCompletable when (it.currency) {
            TransactFragmentView.Currency.APPC_C -> handleCreditsTransfer(it.walletAddress,
                it.amount)
            TransactFragmentView.Currency.ETH -> walletInteract.find().flatMapCompletable { wallet ->
              view.openEthConfirmationView(wallet.address, it.walletAddress, it.amount)
            }
            TransactFragmentView.Currency.APPC -> walletInteract.find().flatMapCompletable { wallet ->
              view.openAppcConfirmationView(wallet.address, it.walletAddress, it.amount)
            }
          }
        }

        .doOnError { error ->
          error.printStackTrace()
          view.hideLoading()
        }
        .retry()
        .subscribe { })
  }

  private fun handleCreditsTransfer(walletAddress: String, amount: BigDecimal): Completable {
    return Completable.mergeArray(
        Completable.timer(1, TimeUnit.SECONDS),
        interactor.transferCredits(walletAddress, amount, packageName).ignoreElement())
        .observeOn(viewScheduler)
        .andThen(view.openAppcCreditsConfirmationView())
        .andThen { view.hideLoading() }
  }

  fun clear() {
    disposables.clear()
  }
}