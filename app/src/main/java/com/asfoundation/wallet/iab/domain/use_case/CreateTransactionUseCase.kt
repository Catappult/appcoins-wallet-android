package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionData
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
  private val adyenPaymentRepository: AdyenPaymentRepository,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
) {
  suspend operator fun invoke(transactionData: TransactionData) =
    adyenPaymentRepository.makePayment(
      adyenPaymentMethod = transactionData.adyenPaymentMethod,
      shouldStoreMethod = transactionData.shouldStoreMethod,
      hasCvc = transactionData.hasCvc,
      supportedShopperInteractions = transactionData.supportedShopperInteractions,
      returnUrl = transactionData.returnUrl,
      value = transactionData.value,
      currency = transactionData.currency,
      reference = transactionData.reference,
      paymentType = transactionData.paymentType,
      walletAddress = transactionData.walletAddress,
      origin = transactionData.origin,
      packageName = transactionData.packageName,
      metadata = transactionData.metadata,
      sku = transactionData.sku,
      callbackUrl = transactionData.callbackUrl,
      transactionType = transactionData.transactionType,
      entityOemId = transactionData.entityOemId,
      entityDomain = transactionData.entityDomain,
      entityPromoCode = transactionData.entityPromoCode,
      userWallet = transactionData.userWallet,
      walletSignature = transactionData.walletSignature,
      referrerUrl = transactionData.referrerUrl,
      guestWalletId = transactionData.guestWalletId,
    ).callAsync(networkDispatcher)
}