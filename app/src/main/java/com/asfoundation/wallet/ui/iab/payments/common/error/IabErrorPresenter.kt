package com.asfoundation.wallet.ui.iab.payments.common.error

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.support.SupportRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit

class IabErrorPresenter(
    private val view: IabErrorView,
    private val data: IabErrorData,
    private val navigator: IabErrorNavigator,
    private val walletService: WalletService,
    private val supportRepository: SupportRepository,
    private val gamificationRepository: Gamification,
    private val disposables: CompositeDisposable) {

  fun present() {
    initializeView()
    handleBackClick()
    handleCancelClick()
    handleSupportClick()
  }

  private fun initializeView() {
    view.setErrorMessage(data.errorMessage)
  }

  private fun handleCancelClick() {
    disposables.add(
        view.cancelClickEvent()
            .doOnNext { navigator.cancelPayment() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackClick() {
    disposables.add(
        view.backClickEvent()
            .doOnNext { navigator.navigateBackToPayment(data.backStackEntryName) }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleSupportClick() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable { showSupport() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun showSupport(): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable { address ->
          return@flatMapCompletable gamificationRepository.getUserStats(address)
              .flatMapCompletable { gamificationStats ->
                Completable.fromAction {
                  supportRepository.registerUser(gamificationStats.level,
                      address.toLowerCase(Locale.ROOT))
                  supportRepository.displayChatScreen()
                }
              }
        }
  }

  fun stop() = disposables.clear()
}