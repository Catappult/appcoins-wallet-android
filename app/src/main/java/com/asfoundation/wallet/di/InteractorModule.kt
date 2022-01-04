package com.asfoundation.wallet.di

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.Airdrop
import com.asfoundation.wallet.AirdropService
import com.asfoundation.wallet.abtesting.ABTestInteractor
import com.asfoundation.wallet.abtesting.ABTestRepository
import com.asfoundation.wallet.abtesting.experiments.topup.TopUpDefaultValueExperiment
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.subscriptions.UserSubscriptionRepository
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.topup.TopUpInteractor
import com.asfoundation.wallet.topup.TopUpLimitValues
import com.asfoundation.wallet.topup.TopUpValuesService
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class InteractorModule {

  @Provides
  @Named("APPROVE_SERVICE_ON_CHAIN")
  fun provideApproveService(sendTransactionInteract: SendTransactionInteract,
                            paymentErrorMapper: PaymentErrorMapper,
                            noWaitPendingTransactionService: NotTrackTransactionService): ApproveService {
    return ApproveService(WatchedTransactionService(object : TransactionSender {
      override fun send(transactionBuilder: TransactionBuilder): Single<String> {
        return sendTransactionInteract.approve(transactionBuilder)
      }
    }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), paymentErrorMapper,
        Schedulers.io(),
        noWaitPendingTransactionService), NoValidateTransactionValidator())
  }

  @Singleton
  @Provides
  @Named("ASF_BDS_IN_APP_INTERACTOR")
  fun provideAsfBdsInAppPurchaseInteractor(
      @Named("IN_APP_PURCHASE_SERVICE") inAppPurchaseService: InAppPurchaseService,
      defaultWalletInteract: FindDefaultWalletInteract,
      gasSettingsInteract: FetchGasSettingsInteract, parser: TransferParser, billing: Billing,
      currencyConversionService: CurrencyConversionService,
      bdsTransactionService: BdsTransactionService,
      billingMessagesMapper: BillingMessagesMapper,
      rxSchedulers: RxSchedulers): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, parser, billingMessagesMapper, billing, currencyConversionService,
        bdsTransactionService,
        rxSchedulers)
  }

  @Singleton
  @Provides
  @Named("ASF_IN_APP_INTERACTOR")
  fun provideAsfInAppPurchaseInteractor(
      @Named("ASF_IN_APP_PURCHASE_SERVICE") inAppPurchaseService: InAppPurchaseService,
      defaultWalletInteract: FindDefaultWalletInteract,
      gasSettingsInteract: FetchGasSettingsInteract,
      parser: TransferParser, billing: Billing,
      currencyConversionService: CurrencyConversionService,
      bdsTransactionService: BdsTransactionService,
      billingMessagesMapper: BillingMessagesMapper,
      rxSchedulers: RxSchedulers): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, parser, billingMessagesMapper, billing, currencyConversionService,
        bdsTransactionService, rxSchedulers)
  }

  @Singleton
  @Provides
  fun provideAirdropInteractor(pendingTransactionService: PendingTransactionService,
                               airdropService: AirdropService,
                               findDefaultWalletInteract: FindDefaultWalletInteract,
                               airdropChainIdMapper: AirdropChainIdMapper): AirdropInteractor {
    return AirdropInteractor(
        Airdrop(AppcoinsTransactionService(pendingTransactionService), BehaviorSubject.create(),
            airdropService), findDefaultWalletInteract, airdropChainIdMapper)
  }

  @Singleton
  @Provides
  fun providesTopUpInteractor(repository: BdsRepository,
                              conversionService: LocalCurrencyConversionService,
                              gamificationInteractor: GamificationInteractor,
                              topUpValuesService: TopUpValuesService,
                              walletBlockedInteract: WalletBlockedInteract,
                              inAppPurchaseInteractor: InAppPurchaseInteractor,
                              supportInteractor: SupportInteractor,
                              topUpDefaultValueExperiment: TopUpDefaultValueExperiment,
                              getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) =
      TopUpInteractor(repository, conversionService, gamificationInteractor, topUpValuesService,
          LinkedHashMap(), TopUpLimitValues(), walletBlockedInteract, inAppPurchaseInteractor,
          supportInteractor, topUpDefaultValueExperiment, getCurrentPromoCodeUseCase)
}