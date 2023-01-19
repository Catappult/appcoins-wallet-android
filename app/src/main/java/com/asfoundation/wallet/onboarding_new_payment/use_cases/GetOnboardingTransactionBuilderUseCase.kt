package com.asfoundation.wallet.onboarding_new_payment.use_cases

import android.net.Uri
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.util.OneStepTransactionParser
import com.asfoundation.wallet.util.parseOneStep
import io.reactivex.Single
import javax.inject.Inject

class GetOnboardingTransactionBuilderUseCase @Inject constructor(
  private val cachedTransactionRepository: CachedTransactionRepository,
  private val oneStepTransactionParser: OneStepTransactionParser
) {

  operator fun invoke(): Single<TransactionBuilder> {
    return cachedTransactionRepository.getCachedTransaction()
      .flatMap { cachedTransaction ->
        Single.just(parseOneStep(Uri.parse(cachedTransaction.referrerUrl)))
          .flatMap { oneStepUri ->
            cachedTransaction.referrerUrl?.let { referrerUrl ->
              oneStepTransactionParser.buildTransaction(
                oneStepUri,
                referrerUrl
              )
            }
          }
      }
  }
}

