package com.asfoundation.wallet.billing.googlepay.usecases

import com.asfoundation.wallet.billing.googlepay.models.GooglePayUrls
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import io.reactivex.Single
import javax.inject.Inject

class GetGooglePayUrlUseCase
@Inject
constructor(
    private val googlePayWebRepository: GooglePayWebRepository,
) {

  operator fun invoke(): Single<GooglePayUrls> {
    return googlePayWebRepository.getGooglePayUrl()
  }
}
