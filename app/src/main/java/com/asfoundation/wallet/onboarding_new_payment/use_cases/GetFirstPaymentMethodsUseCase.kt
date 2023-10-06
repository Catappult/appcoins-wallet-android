package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.microservices.model.FeeEntity
import com.appcoins.wallet.core.network.microservices.model.FeeType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.onboarding.CachedTransaction
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodFee
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GetFirstPaymentMethodsUseCase @Inject constructor(
  private val bdsRepository: BdsRepository,
  private val partnerAddressService: PartnerAddressService,
) {

  companion object {
    private const val APPC_ID = "appcoins"
    private const val CREDITS_ID = "appcoins_credits"
    private const val ASK_SOMEONE_TO_PAY_ID = "ask_friend"
  }

  operator fun invoke(cachedTransaction: CachedTransaction): Single<List<PaymentMethod>> {
    return partnerAddressService.getAttributionEntity(
      packageName = cachedTransaction.packageName ?: ""
    )
      .flatMap { attributionEntity ->
        bdsRepository.getPaymentMethods(
          cachedTransaction.value.toString(),
          cachedTransaction.currency,
          packageName = cachedTransaction.packageName,
          entityOemId = attributionEntity.oemId
        )
          .flatMap { paymentMethods ->
            removeUnavailableMethods(paymentMethods)
              .flatMap { availablePaymentMethods ->
                Observable.fromIterable(paymentMethods)
                  .map { paymentMethod ->
                    mapPaymentMethods(paymentMethod, availablePaymentMethods)
                  }
                  .toList()
              }
          }
      }
  }

  /**
   * Since this is the first payment, we won't need to show appcoins credits nor appcoins as a payment option,
   * unlike the payment in the normal IAB flow, where we hide the option if no balance is available.
   * In this case, we won't show it at all.
   */
  private fun removeUnavailableMethods(paymentList: List<PaymentMethodEntity>): Single<List<PaymentMethodEntity>> {
    val clonedPaymentMethod: MutableList<PaymentMethodEntity> =
      paymentList as MutableList<PaymentMethodEntity>
    val iterator = clonedPaymentMethod.iterator()
    while (iterator.hasNext()) {
      val method = iterator.next()
      if (method.id == CREDITS_ID || method.id == APPC_ID || !method.isAvailable()) {
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

