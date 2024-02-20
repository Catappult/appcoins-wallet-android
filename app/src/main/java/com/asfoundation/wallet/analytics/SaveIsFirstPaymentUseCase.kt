package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.app_start.AppStartRepositoryImpl
import javax.inject.Inject

class SaveIsFirstPaymentUseCase
@Inject
constructor(
    private val appStartRepositoryImpl: AppStartRepositoryImpl,
) {
  operator fun invoke(isFirstPayment: Boolean) {
    appStartRepositoryImpl.saveIsFirstPayment(isFirstPayment)
  }
}
