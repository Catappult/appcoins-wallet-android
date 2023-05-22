package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Single
import javax.inject.Inject

class GetTransactionOriginUseCase @Inject constructor(
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
) {

  operator fun invoke(transactionBuilder: TransactionBuilder): Single<String?> {
    return inAppPurchaseInteractor.isWalletFromBds(
      transactionBuilder.domain,
      transactionBuilder.toAddress()
    ).map { isBds ->
      return@map if (transactionBuilder.origin == null) {
        if (isBds) "BDS" else null
      } else {
        transactionBuilder.origin
      }
    }
  }
}


