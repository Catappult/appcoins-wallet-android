package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.FeeEntity
import com.appcoins.wallet.bdsbilling.repository.entity.FeeType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.asfoundation.wallet.onboarding.CachedTransaction
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodFee
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * This use case should be removed after all payment methods are ready
 * and @GetFirstPaymentMethodsUseCase should be used instead
 */
class GetPaypalAndCCMethodsUseCase @Inject constructor(private val bdsRepository: BdsRepository) {

  companion object {
    private const val PAYPAL_ID = "paypal"
    private const val CC_ID = "credit_card"
  }

  operator fun invoke(cachedTransaction: CachedTransaction): Single<List<PaymentMethod>> {
    return bdsRepository.getPaymentMethods(
      cachedTransaction.value.toString(), cachedTransaction.currency
    )
      .flatMap { paymentMethods ->
        showOnlyPaypalAndCC(paymentMethods)
          .flatMap { availablePaymentMethods ->
            Observable.fromIterable(paymentMethods)
              .map { paymentMethod ->
                mapPaymentMethods(paymentMethod, availablePaymentMethods)
              }
              .toList()
          }
      }
  }

  private fun showOnlyPaypalAndCC(paymentList: List<PaymentMethodEntity>): Single<List<PaymentMethodEntity>> {
    val clonedPaymentMethod: MutableList<PaymentMethodEntity> =
      paymentList as MutableList<PaymentMethodEntity>
    val iterator = clonedPaymentMethod.iterator()
    while (iterator.hasNext()) {
      val method = iterator.next()
      if (method.id != CC_ID && method.id != PAYPAL_ID) {
        iterator.remove()
      }
    }
    return Single.just(clonedPaymentMethod)
  }

  private fun mapPaymentMethods(
    paymentMethod: PaymentMethodEntity,
    availablePaymentMethods: List<PaymentMethodEntity>
  ): PaymentMethod {
    for (availablePaymentMethod in availablePaymentMethods) {
      if (paymentMethod.id == availablePaymentMethod.id) {
        val paymentMethodFee = mapPaymentMethodFee(availablePaymentMethod.fee)
        return PaymentMethod(
          paymentMethod.id, paymentMethod.label,
          paymentMethod.iconUrl, paymentMethod.async, paymentMethodFee, true, null,
          false
        )
      }
    }
    val paymentMethodFee = mapPaymentMethodFee(paymentMethod.fee)
    return PaymentMethod(
      paymentMethod.id, paymentMethod.label,
      paymentMethod.iconUrl, paymentMethod.async, paymentMethodFee, false, null, false
    )
  }

  private fun mapPaymentMethodFee(feeEntity: FeeEntity?): PaymentMethodFee? {
    return if (feeEntity == null) {
      null
    } else {
      if (feeEntity.type === FeeType.EXACT) {
        PaymentMethodFee(
          true, feeEntity.cost?.value, feeEntity.cost?.currency
        )
      } else {
        PaymentMethodFee(false, null, null)
      }
    }
  }
}