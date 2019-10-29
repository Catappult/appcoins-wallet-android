package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.AutoUpdateModel
import io.reactivex.Scheduler

/**
 * Created by franciscocalado on 20/07/2018.
 */

class IabPresenter(private val view: IabView, private val autoUpdateInteract: AutoUpdateInteract,
                   private val networkScheduler: Scheduler, private val viewScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {
    handleAutoUpdate(savedInstanceState)
  }

  private fun handleAutoUpdate(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      autoUpdateInteract.getAutoUpdateModel()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess { launchInitialView(it) }
          .subscribe()
    }
  }

  private fun launchInitialView(updateModel: AutoUpdateModel) {
    if (autoUpdateInteract.isHardUpdateRequired(updateModel.blackList,
            updateModel.updateVersionCode, updateModel.updateMinSdk)) {
      view.showUpdateRequiredView()
    } else {
      view.showPaymentMethodsView(PaymentMethodsView.SelectedPaymentMethod.CREDIT_CARD)
    }
  }
}