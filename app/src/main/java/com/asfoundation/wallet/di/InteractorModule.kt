package com.asfoundation.wallet.di

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.core.network.airdrop.AirdropService
import com.asfoundation.wallet.Airdrop
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor
import com.asfoundation.wallet.util.TransferParser
import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class InteractorModule {

  @Provides
  @Named("APPROVE_SERVICE_ON_CHAIN")
  fun provideApproveService(
    sendTransactionInteract: SendTransactionInteract,
    paymentErrorMapper: PaymentErrorMapper,
    noWaitPendingTransactionService: NotTrackTransactionService
  ): ApproveService {
    return ApproveService(
      WatchedTransactionService(
        object : TransactionSender {
          override fun send(transactionBuilder: TransactionBuilder): Single<String> {
            return sendTransactionInteract.approve(transactionBuilder)
          }
        },
        MemoryCache(
          BehaviorSubject.create(),
          ConcurrentHashMap()
        ), paymentErrorMapper,
        Schedulers.io(),
        noWaitPendingTransactionService
      ), NoValidateTransactionValidator()
    )
  }

  @Singleton
  @Provides
  @Named("ASF_BDS_IN_APP_INTERACTOR")
  fun provideAsfBdsInAppPurchaseInteractor(
      @Named("IN_APP_PURCHASE_SERVICE") inAppPurchaseService: InAppPurchaseService,
      defaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
      gasSettingsInteract: FetchGasSettingsInteract,
      @Named("payment-gas-limit") paymentGasLimit: BigDecimal, parser: TransferParser,
      billing: Billing, currencyConversionService: CurrencyConversionService,
      bdsTransactionService: BdsTransactionService,
      billingMessagesMapper: BillingMessagesMapper,
      rxSchedulers: RxSchedulers
  ): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(
      inAppPurchaseService, defaultWalletInteract,
      gasSettingsInteract, paymentGasLimit, parser, billingMessagesMapper, billing,
      currencyConversionService,
      bdsTransactionService,
      rxSchedulers
    )
  }

  @Singleton
  @Provides
  @Named("ASF_IN_APP_INTERACTOR")
  fun provideAsfInAppPurchaseInteractor(
      @Named("ASF_IN_APP_PURCHASE_SERVICE") inAppPurchaseService: InAppPurchaseService,
      defaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
      gasSettingsInteract: FetchGasSettingsInteract,
      @Named("payment-gas-limit") paymentGasLimit: BigDecimal, parser: TransferParser,
      billing: Billing, currencyConversionService: CurrencyConversionService,
      bdsTransactionService: BdsTransactionService,
      billingMessagesMapper: BillingMessagesMapper,
      rxSchedulers: RxSchedulers
  ): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(
      inAppPurchaseService, defaultWalletInteract,
      gasSettingsInteract, paymentGasLimit, parser, billingMessagesMapper, billing,
      currencyConversionService,
      bdsTransactionService, rxSchedulers
    )
  }

  @Singleton
  @Provides
  fun provideAirdropInteractor(
      pendingTransactionService: PendingTransactionService,
      airdropService: AirdropService,
      findDefaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
      airdropChainIdMapper: AirdropChainIdMapper
  ): AirdropInteractor {
    return AirdropInteractor(
      Airdrop(
        AppcoinsTransactionService(pendingTransactionService), BehaviorSubject.create(),
        airdropService
      ), findDefaultWalletInteract, airdropChainIdMapper
    )
  }
}