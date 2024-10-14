package com.asfoundation.wallet.billing.googlepay.usecases

import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import javax.inject.Inject

class GetGooglePayResultUseCase @Inject constructor(
  private val googlePayWebRepository: GooglePayWebRepository,
) {

  operator fun invoke(): String {
    return googlePayWebRepository.consumeChromeResult()
  }

}
