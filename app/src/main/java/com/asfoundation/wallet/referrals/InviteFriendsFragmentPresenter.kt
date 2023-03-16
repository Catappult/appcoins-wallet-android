package com.asfoundation.wallet.referrals

import com.appcoins.wallet.core.utils.common.extensions.isNoNetworkException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class InviteFriendsFragmentPresenter(private val view: InviteFriendsFragmentView,
                                     private val activity: InviteFriendsActivityView?,
                                     private val disposable: CompositeDisposable,
                                     private val referralInteractor: ReferralInteractorContract) {

  fun present() {
    handleInfoButtonClick()
    handleShareClicks()
    handlePendingNotification()
  }

  private fun handlePendingNotification() {
    disposable.add(
        referralInteractor.getPendingBonusNotification()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.showNotificationCard(it.pendingAmount, it.symbol, it.icon) }
            .doOnComplete { view.showNotificationCard(BigDecimal.ZERO, "", null) }
            .doOnError { handlerError(it) }
            .subscribe()
    )
  }

  private fun handleShareClicks() {
    disposable.add(view.shareLinkClick()
        .doOnNext { view.showShare() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleInfoButtonClick() {
    activity?.let {
      disposable.add(it.getInfoButtonClick()
          .doOnNext { view.changeBottomSheetState() }
          .subscribe())
    }
  }

  private fun handlerError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      activity?.showNetworkErrorView()
    }
  }

  fun stop() {
    disposable.clear()
  }
}