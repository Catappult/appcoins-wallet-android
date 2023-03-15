package com.asfoundation.wallet.di

import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.util.CountryCodeProvider
import com.asfoundation.wallet.wallets.usecases.HasEnoughBalanceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ServiceModule {

  @Provides
  @Named("BUY_SERVICE_ON_CHAIN")
  fun provideBuyServiceOnChain(
    sendTransactionInteract: SendTransactionInteract,
    paymentErrorMapper: PaymentErrorMapper,
    pendingTransactionService: PendingTransactionService,
    defaultTokenProvider: DefaultTokenProvider,
    countryCodeProvider: CountryCodeProvider,
    addressService: AddressService,
    billingPaymentProofSubmission: BillingPaymentProofSubmission
  ): BuyService {
    return BuyService(
      WatchedTransactionService(
        object : TransactionSender {
          override fun send(transactionBuilder: TransactionBuilder): Single<String> {
            return sendTransactionInteract.buy(transactionBuilder)
          }
        },
        MemoryCache(
          BehaviorSubject.create(),
          ConcurrentHashMap()
        ), paymentErrorMapper,
        Schedulers.io(),
        pendingTransactionService
      ), NoValidateTransactionValidator(), defaultTokenProvider,
      countryCodeProvider, addressService, billingPaymentProofSubmission
    )
  }

  @Provides
  @Named("BUY_SERVICE_BDS")
  fun provideBuyServiceBds(
    sendTransactionInteract: SendTransactionInteract,
    paymentErrorMapper: PaymentErrorMapper,
    bdsPendingTransactionService: BdsPendingTransactionService,
    billingPaymentProofSubmission: BillingPaymentProofSubmission,
    defaultTokenProvider: DefaultTokenProvider,
    countryCodeProvider: CountryCodeProvider,
    addressService: AddressService
  ): BuyService {
    return BuyService(
      WatchedTransactionService(
        object : TransactionSender {
          override fun send(transactionBuilder: TransactionBuilder): Single<String> {
            return sendTransactionInteract.buy(transactionBuilder)
          }
        },
        MemoryCache(
          BehaviorSubject.create(),
          ConcurrentHashMap()
        ), paymentErrorMapper,
        Schedulers.io(),
        bdsPendingTransactionService
      ),
      BuyTransactionValidatorBds(
        sendTransactionInteract, billingPaymentProofSubmission,
        defaultTokenProvider, addressService
      ), defaultTokenProvider, countryCodeProvider, addressService, billingPaymentProofSubmission
    )
  }

  @Singleton
  @Provides
  @Named("IN_APP_PURCHASE_SERVICE")
  fun provideInAppPurchaseService(
    @Named("APPROVE_SERVICE_BDS") approveService: ApproveService,
    allowanceService: AllowanceService,
    @Named("BUY_SERVICE_BDS") buyService: BuyService,
    hasEnoughBalanceUseCase: HasEnoughBalanceUseCase,
    paymentErrorMapper: PaymentErrorMapper,
    defaultTokenProvider: DefaultTokenProvider
  ): InAppPurchaseService {
    return InAppPurchaseService(
      MemoryCache(
        BehaviorSubject.create(),
        HashMap()
      ), approveService,
      allowanceService, buyService, Schedulers.io(), paymentErrorMapper, hasEnoughBalanceUseCase,
      defaultTokenProvider
    )
  }

  @Singleton
  @Provides
  @Named("ASF_IN_APP_PURCHASE_SERVICE")
  fun provideInAppPurchaseServiceAsf(
    @Named("APPROVE_SERVICE_ON_CHAIN") approveService: ApproveService,
    allowanceService: AllowanceService, @Named("BUY_SERVICE_ON_CHAIN") buyService: BuyService,
    hasEnoughBalanceUseCase: HasEnoughBalanceUseCase,
    paymentErrorMapper: PaymentErrorMapper,
    defaultTokenProvider: DefaultTokenProvider
  ): InAppPurchaseService {
    return InAppPurchaseService(
      MemoryCache(
        BehaviorSubject.create(),
        HashMap()
      ), approveService,
      allowanceService, buyService, Schedulers.io(), paymentErrorMapper, hasEnoughBalanceUseCase,
      defaultTokenProvider
    )
  }

  @Singleton
  @Provides
  fun providesBdsTransactionService(
    bdsPendingTransactionService: BdsPendingTransactionService, rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers,
  ): BdsTransactionService {
    return BdsTransactionService(
      rxSchedulers,
      MemoryCache(
        BehaviorSubject.create(),
        HashMap()
      ),
      CompositeDisposable(), bdsPendingTransactionService
    )
  }

  @Provides
  @Named("APPROVE_SERVICE_BDS")
  fun provideApproveServiceBds(
    sendTransactionInteract: SendTransactionInteract,
    paymentErrorMapper: PaymentErrorMapper,
    noWaitPendingTransactionService: NotTrackTransactionService,
    billingPaymentProofSubmission: BillingPaymentProofSubmission,
    addressService: AddressService
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
      ),
      ApproveTransactionValidatorBds(
        sendTransactionInteract, billingPaymentProofSubmission,
        addressService
      )
    )
  }
}