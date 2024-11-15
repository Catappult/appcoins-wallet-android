package com.asfoundation.wallet.iab.payment_manager.payment_methods

import androidx.navigation.NavOptions
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.model.CreditCardCVCResponse
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.emptyPaymentMethodEntity
import com.appcoins.wallet.ui.common.callAsync
import com.asf.wallet.R
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import com.asfoundation.wallet.iab.payment_manager.domain.emptyWalletData
import com.asfoundation.wallet.iab.presentation.payment_methods.credit_card.CreditCardFragment
import com.google.gson.JsonObject
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class CreditCardPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
  private val walletData: WalletData,
  private val adyenPaymentRepository: AdyenPaymentRepository,
  private val networkDispatcher: CoroutineDispatcher,
) : PaymentMethod(paymentMethod) {

  override val onPaymentMethodClick: (FragmentNavigator) -> Unit
    get() = { navigator ->
      navigator.navigateTo(
        destiny = R.id.credit_card_fragment,
        args = CreditCardFragment.createBundleArgs(id),
        navOptions = NavOptions.Builder()
          .setEnterAnim(R.anim.nav_slide_in_right)
          .setExitAnim(R.anim.nav_slide_out_left)
          .setPopEnterAnim(R.anim.nav_slide_in_left)
          .setPopExitAnim(R.anim.nav_slide_out_right)
          .build()
      )
    }

  suspend fun init(): PaymentInfoModel =
    adyenPaymentRepository.loadPaymentInfo(
      ewt = walletData.ewt,
      walletAddress = walletData.address,
      value = cost.toString(),
      currency = currency,
      methods = AdyenPaymentRepository.Methods.CREDIT_CARD,
    ).callAsync(networkDispatcher)


  override fun createTransaction() {
    TODO("Not yet implemented")
  }

  suspend fun saveCard(): Boolean {
    delay(TimeUnit.SECONDS.toMillis(2))

    return true
  }

}

val emptyCreditCardPaymentMethod = CreditCardPaymentMethod(
  paymentMethod = emptyPaymentMethodEntity,
  purchaseData = emptyPurchaseData,
  walletData = emptyWalletData,
  networkDispatcher = Dispatchers.IO,
  adyenPaymentRepository = object : AdyenPaymentRepository {
    override fun loadPaymentInfo(
      methods: AdyenPaymentRepository.Methods,
      value: String,
      currency: String,
      walletAddress: String,
      ewt: String
    ): Single<PaymentInfoModel> {
      TODO("Not yet implemented")
    }

    override fun getStoredCards(
      methods: AdyenPaymentRepository.Methods,
      value: String,
      currency: String?,
      walletAddress: String,
      ewt: String
    ): Single<List<StoredPaymentMethod>> {
      TODO("Not yet implemented")
    }

    override fun makePayment(
      adyenPaymentMethod: ModelObject,
      shouldStoreMethod: Boolean,
      hasCvc: Boolean,
      supportedShopperInteractions: List<String>,
      returnUrl: String,
      value: String,
      currency: String,
      reference: String?,
      paymentType: String,
      walletAddress: String,
      origin: String?,
      packageName: String?,
      metadata: String?,
      sku: String?,
      callbackUrl: String?,
      transactionType: String,
      entityOemId: String?,
      entityDomain: String?,
      entityPromoCode: String?,
      userWallet: String?,
      walletSignature: String,
      referrerUrl: String?,
      guestWalletId: String?
    ): Single<PaymentModel> {
      TODO("Not yet implemented")
    }

    override fun submitRedirect(
      uid: String,
      walletAddress: String,
      details: JsonObject,
      paymentData: String?
    ): Single<PaymentModel> {
      TODO("Not yet implemented")
    }

    override fun disablePayments(walletAddress: String): Single<Boolean> {
      TODO("Not yet implemented")
    }

    override fun removeSavedCard(
      walletAddress: String,
      recurringReference: String?
    ): Single<Boolean> {
      TODO("Not yet implemented")
    }

    override fun getTransaction(
      uid: String,
      walletAddress: String,
      signedWalletAddress: String
    ): Single<PaymentModel> {
      TODO("Not yet implemented")
    }

    override fun getCreditCardNeedCVC(): Single<CreditCardCVCResponse> {
      TODO("Not yet implemented")
    }

    override fun setMandatoryCVC(mandatoryCvc: Boolean) {
      TODO("Not yet implemented")
    }

  }
)
