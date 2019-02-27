package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val walletInteract: FindDefaultWalletInteract,
                        private val packageName: String) {
  companion object {
    private val TAG = TransactPresenter::class.java.simpleName
  }

  fun present() {
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable {
          return@flatMapCompletable when (it.currency) {
            TransactFragmentView.Currency.APPC_C -> interactor.transferCredits(it.walletAddress,
                it.amount, packageName).ignoreElement()
            TransactFragmentView.Currency.ETH -> walletInteract.find().flatMapCompletable { wallet ->
              view.openEthConfirmationView(wallet.address, it.walletAddress, it.amount)
            }
            TransactFragmentView.Currency.APPC -> walletInteract.find().flatMapCompletable { wallet ->
              view.openAppcConfirmationView(wallet.address, it.walletAddress, it.amount)
            }
          }

        }.doOnError { error -> error.printStackTrace() }
        .retry()
        .subscribe { })
  }

  fun clear() {
    disposables.clear()
  }
}