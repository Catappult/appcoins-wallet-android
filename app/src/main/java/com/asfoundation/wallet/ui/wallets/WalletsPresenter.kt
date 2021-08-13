package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class WalletsPresenter(private val view: WalletsView,
                       private val walletsInteract: WalletsInteract,
                       private val logger: Logger,
                       private val disposables: CompositeDisposable,
                       private val viewScheduler: Scheduler,
                       private val networkScheduler: Scheduler,
                       private val walletsEventSender: WalletsEventSender) {
  fun present() {
    retrieveViewInformation()
    handleActiveWalletCardClick()
    handleOtherWalletCardClick()
    handleCreateNewWalletClick()
    handleRestoreWalletClick()
    handleBottomSheetHeaderClick()
  }

  private fun handleBottomSheetHeaderClick() {
    disposables.add(view.onBottomSheetHeaderClicked()
        .doOnNext { view.changeBottomSheetState() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRestoreWalletClick() {
    disposables.add(view.restoreWalletClicked()
        .doOnNext { walletsEventSender.sendAction("wallet_list_recover_wallet") }
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToRestoreView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleCreateNewWalletClick() {
    disposables.add(view.createNewWalletClicked()
        .doOnNext { walletsEventSender.sendAction("wallet_list_create_wallet") }
        .throttleFirst(100, TimeUnit.MILLISECONDS)
        .doOnNext { view.showCreatingAnimation() }
        .observeOn(networkScheduler)
        .flatMapCompletable {
          walletsInteract.createWallet()
              .observeOn(viewScheduler)
              .andThen { view.showWalletCreatedAnimation() }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleOtherWalletCardClick() {
    disposables.add(view.otherWalletCardClicked()
        .doOnNext { walletsEventSender.sendAction("wallet_list_wallet_details") }
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, false) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleActiveWalletCardClick() {
    disposables.add(view.activeWalletCardClicked()
        .doOnNext { walletsEventSender.sendAction("wallet_list_wallet_details") }
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, true) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun retrieveViewInformation() {
    disposables.add(walletsInteract.retrieveWalletsModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setupUi(it.totalWallets, it.totalBalance, it.otherWallets) }
        .subscribe({}, { logger.log("WalletsPresenter", it) }))
  }

  fun stop() = disposables.clear()
}
