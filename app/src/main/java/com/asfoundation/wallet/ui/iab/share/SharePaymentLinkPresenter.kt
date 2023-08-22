package com.asfoundation.wallet.ui.iab.share

import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class SharePaymentLinkPresenter(private val view: SharePaymentLinkFragmentView,
                                private val interactor: ShareLinkInteractor,
                                private val viewScheduler: Scheduler,
                                private val networkScheduler: Scheduler,
                                private val disposables: CompositeDisposable,
                                private val analytics: BillingAnalytics) {


  fun present() {
    handleStop()
    handleShare()
  }

  private fun handleShare() {
    disposables.add(view.getShareButtonClick()
        .doOnNext { view.showFetchingLinkInfo() }
        .flatMapSingle {
          analytics.sendPaymentConfirmationEvent(it.domain, it.skuId ?: "", it.amount,
              it.paymentMethod, it.type, "share")
          getLink(it)
        }
        .observeOn(viewScheduler)
        .doOnNext {
          interactor.savePreSelectedPaymentMethod(PaymentMethodsView.PaymentMethodId.ASK_FRIEND.id)
          view.shareLink(it)
        }
        .subscribe({}, {
          it.printStackTrace()
          view.showErrorInfo()
        }))
  }

  private fun handleStop() {
    disposables.add(view.getCancelButtonClick()
        .doOnNext {
          analytics.sendPaymentConfirmationEvent(it.domain, it.skuId ?: "", it.amount,
              it.paymentMethod, it.type, "close")
          view.close()
        }
        .subscribe())
  }

  fun stop() {
    disposables.clear()
  }

  private fun getLink(data: SharePaymentLinkFragmentView.SharePaymentData): Single<String> {
    return Single.zip(
        Single.timer(1, TimeUnit.SECONDS),
        interactor.getLinkToShare(data.domain, data.skuId, data.message, data.originalAmount,
            data.originalCurrency, data.paymentMethod)
            .subscribeOn(networkScheduler),
        BiFunction { _: Long, url: String -> url })
  }

}
