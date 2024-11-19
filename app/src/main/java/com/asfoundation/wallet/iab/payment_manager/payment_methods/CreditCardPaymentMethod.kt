package com.asfoundation.wallet.iab.payment_manager.payment_methods

import android.net.Uri
import androidx.navigation.NavOptions
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.arch.data.DataResult
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.base.IGetPrivateKeyUseCase
import com.appcoins.wallet.core.network.base.WalletRepository
import com.appcoins.wallet.core.network.microservices.model.CreditCardCVCResponse
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.emptyPaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrenciesRepository
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SignUseCase
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.PaymentBrands
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.domain.use_case.CreateTransactionUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetStoredCardsV2UseCase
import com.asfoundation.wallet.iab.domain.use_case.LoadPaymentInfoUseCase
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionData
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import com.asfoundation.wallet.iab.payment_manager.domain.emptyWalletData
import com.asfoundation.wallet.iab.payment_manager.domain.payment_methods_transaction.CreditCardTransaction
import com.asfoundation.wallet.iab.presentation.payment_methods.credit_card.CreditCardFragment
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.google.gson.JsonObject
import ethereumj.crypto.ECKey
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import java.util.Locale

class CreditCardPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
  private val walletData: WalletData,
  private val loadPaymentInfoUseCase: LoadPaymentInfoUseCase,
  private val getStoredCardsUseCase: GetStoredCardsV2UseCase,
  private val createTransactionUseCase: CreateTransactionUseCase,
) : PaymentMethod(paymentMethod) {

  override val isReadyToPay
    get() = !creditCards.isNullOrEmpty()

  private var creditCards: List<StoredPaymentMethod>? = null

  override val onPaymentMethodClick: (FragmentNavigator) -> Unit
    get() = { navigator ->
      if (!isReadyToPay) {
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
      } else super.onPaymentMethodClick(navigator)
    }

  override fun getIconOnPreSelected(): String =
    creditCards
      ?.firstOrNull()
      ?.run { PaymentBrands.getPayment(this.brand).brandFlag }
      ?.run { Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/" + this).toString() }
      ?: super.getIconOnPreSelected()

  override fun getTitleOnPreSelected() =
    creditCards
      ?.firstOrNull()
      ?.run { this to PaymentBrands.getPayment(this.brand).brandName }
      ?.run { this.first to this.second.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
      ?.run { "${this.second} - ${this.first.lastFour}" }
      ?: super.getTitleOnPreSelected()

  override suspend fun init() {
    if (creditCards.isNullOrEmpty())
      creditCards = getStoredCardsUseCase()
  }

  suspend fun loadPaymentInfo(): PaymentInfoModel =
    loadPaymentInfoUseCase(
      ewt = walletData.ewt,
      walletAddress = walletData.address,
      value = cost.toString(),
      currency = currency,
      methods = AdyenPaymentRepository.Methods.CREDIT_CARD,
    )

  suspend fun addCard(adyenCardWrapper: AdyenCardWrapper, returnUrl: String) =
    createTransactionUseCase(
      transactionData = TransactionData(
        adyenPaymentMethod = adyenCardWrapper.cardPaymentMethod,
        shouldStoreMethod = true,
        hasCvc = adyenCardWrapper.hasCvc,
        supportedShopperInteractions = adyenCardWrapper.supportedShopperInteractions,
        returnUrl = returnUrl,
        value = "0",
        currency = "EUR",
        paymentType = "credit_card",
        walletAddress = walletData.address,
        packageName = "com.appcoins.wallet",  // necessary for the verification request
        transactionType = "VERIFICATION",
        walletSignature = walletData.signedAddress,
      )
    )

  override suspend fun createTransaction(transaction: TransactionData): CreditCardTransaction {
    TODO("Not yet implemented")
  }

}

val emptyCreditCardPaymentMethod = CreditCardPaymentMethod(
  paymentMethod = emptyPaymentMethodEntity,
  purchaseData = emptyPurchaseData,
  walletData = emptyWalletData,
  getStoredCardsUseCase = GetStoredCardsV2UseCase(
    getStoredCardsUseCase = GetStoredCardsUseCase(
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

      },
      walletService = object : WalletService {
        override fun getWalletAddress(): Single<String> {
          TODO("Not yet implemented")
        }

        override fun getWalletOrCreate(): Single<String> {
          TODO("Not yet implemented")
        }

        override fun findWalletOrCreate(): Observable<String> {
          TODO("Not yet implemented")
        }

        override fun signContent(content: String): Single<String> {
          TODO("Not yet implemented")
        }

        override fun getAndSignWalletAddress(walletAddress: String): Single<WalletAddressModel> {
          TODO("Not yet implemented")
        }

        override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> {
          TODO("Not yet implemented")
        }

        override fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel> {
          TODO("Not yet implemented")
        }

      },
      getCachedCurrencyUseCase = GetCachedCurrencyUseCase(object : FiatCurrenciesRepository {
        override suspend fun getCurrenciesList(): DataResult<List<FiatCurrency>> {
          TODO("Not yet implemented")
        }

        override suspend fun getSelectedCurrency(): DataResult<String> {
          TODO("Not yet implemented")
        }

        override fun getCachedResultSelectedCurrency(): DataResult<String> {
          TODO("Not yet implemented")
        }

        override fun getCachedSelectedCurrency(): String? {
          TODO("Not yet implemented")
        }

        override suspend fun setSelectedCurrency(currency: String) {
          TODO("Not yet implemented")
        }
      }),
      ewtObtainer = EwtAuthenticatorService(
        walletRepository = object : WalletRepository {

          override fun getDefaultWalletAddress(): String {
            TODO("Not yet implemented")
          }

        },
        getPrivateKeyUseCase = object : IGetPrivateKeyUseCase {
          override fun invoke(address: String): Single<ECKey> {
            TODO("Not yet implemented")
          }

        },
        signUseCase = SignUseCase(),
        header = "",
      ),
      rxSchedulers = object : RxSchedulers {
        override val main: Scheduler
          get() = TODO("Not yet implemented")
        override val io: Scheduler
          get() = TODO("Not yet implemented")
        override val computation: Scheduler
          get() = TODO("Not yet implemented")
      },
    ),
    networkDispatcher = Dispatchers.IO
  ),
  loadPaymentInfoUseCase = LoadPaymentInfoUseCase(
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

    },
    networkDispatcher = Dispatchers.IO
  ),
  createTransactionUseCase = CreateTransactionUseCase(
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

    },
    networkDispatcher = Dispatchers.IO
  )
)
