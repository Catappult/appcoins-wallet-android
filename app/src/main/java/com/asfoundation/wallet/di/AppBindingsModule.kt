package com.asfoundation.wallet.di

import com.appcoins.wallet.billing.carrierbilling.CarrierBillingPreferencesRepository
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.appcoins.wallet.ui.widgets.top_bar.use_case.GetBadgeVisibleUseCase
import com.asfoundation.wallet.app_start.AppStartRepository
import com.asfoundation.wallet.app_start.AppStartRepositoryImpl
import com.asfoundation.wallet.app_start.GPInstallUseCase
import com.asfoundation.wallet.app_start.GPInstallUseCaseImpl
import com.asfoundation.wallet.app_start.GooglePlayInstallRepository
import com.asfoundation.wallet.app_start.GooglePlayInstallRepositoryImpl
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.feature_flags.FeatureFlagsRepository
import com.asfoundation.wallet.feature_flags.FeatureFlagsRepositoryImpl
import com.asfoundation.wallet.feature_flags.di.TopUpProbe
import com.asfoundation.wallet.feature_flags.topup.AndroidIdRepository
import com.asfoundation.wallet.feature_flags.topup.AndroidIdRepositoryImpl
import com.asfoundation.wallet.feature_flags.topup.TopUpDefaultValueProbe
import com.asfoundation.wallet.firebase_messaging.repository.FirebaseMessagingRepository
import com.asfoundation.wallet.firebase_messaging.repository.FirebaseMessagingRepositoryImpl
import com.asfoundation.wallet.home.usecases.GetBadgeVisibleUseCaseImpl
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.manage_wallets.usecase.SetActiveWalletUseCaseImpl
import com.asfoundation.wallet.onboarding.use_cases.PendingPurchaseFlowUseCase
import com.asfoundation.wallet.onboarding.use_cases.PendingPurchaseFlowUseCaseImpl
import com.asfoundation.wallet.onboarding.use_cases.RestoreGuestWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.RestoreGuestWalletUseCaseImpl
import com.asfoundation.wallet.repository.BackendTransactionRepository
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.repository.EthereumService
import com.asfoundation.wallet.repository.GasSettingsRepository
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import com.asfoundation.wallet.repository.GamesRepository
import com.asfoundation.wallet.repository.GamesRepositoryType
import com.asfoundation.wallet.repository.IpCountryCodeProvider
import com.asfoundation.wallet.repository.NotTrackTransactionService
import com.asfoundation.wallet.repository.PendingTransactionService
import com.asfoundation.wallet.repository.TrackPendingTransactionService
import com.asfoundation.wallet.repository.TrackTransactionService
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import com.asfoundation.wallet.repository.TransactionsLocalRepository
import com.asfoundation.wallet.repository.TransactionsRepository
import com.asfoundation.wallet.repository.Web3jService
import com.asfoundation.wallet.repository.DefaultTransactionsHistoryRepository
import com.asfoundation.wallet.ui.iab.payments.carrier.SecureCarrierBillingPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

  @Binds
  abstract fun bindAppStartRepository(impl: AppStartRepositoryImpl): AppStartRepository

  @Binds
  abstract fun bindGooglePlayInstallRepository(impl: GooglePlayInstallRepositoryImpl): GooglePlayInstallRepository

  @Binds
  abstract fun bindGPInstallUseCase(impl: GPInstallUseCaseImpl): GPInstallUseCase

  @Binds
  abstract fun bindPendingPurchaseFlowUseCase(impl: PendingPurchaseFlowUseCaseImpl): PendingPurchaseFlowUseCase

  @Binds
  abstract fun bindRestoreGuestWalletUseCase(impl: RestoreGuestWalletUseCaseImpl): RestoreGuestWalletUseCase

  @Binds
  @Singleton
  abstract fun bindSetActiveWalletUseCase(impl: SetActiveWalletUseCaseImpl): SetActiveWalletUseCase

  @Binds
  @Singleton
  abstract fun bindGetBadgeVisibleUseCase(impl: GetBadgeVisibleUseCaseImpl): GetBadgeVisibleUseCase

  @Binds
  abstract fun bindFirebaseMessagingRepository(impl: FirebaseMessagingRepositoryImpl): FirebaseMessagingRepository

  @Binds
  abstract fun bindTransactionRepositoryType(impl: BackendTransactionRepository): TransactionRepositoryType

  @Binds
  abstract fun bindDefaultTokenProvider(impl: BuildConfigDefaultTokenProvider): DefaultTokenProvider

  @Binds
  abstract fun bindEthereumService(impl: Web3jService): EthereumService

  @Binds
  abstract fun bindCountryCodeProvider(impl: IpCountryCodeProvider): CountryCodeProvider

  @Binds
  abstract fun bindGasSettingsRepositoryType(impl: GasSettingsRepository): GasSettingsRepositoryType

  @Binds
  abstract fun bindGamesRepositoryType(impl: GamesRepository): GamesRepositoryType

  @Binds
  abstract fun bindTransactionsHistoryRepository(impl: DefaultTransactionsHistoryRepository): TransactionsHistoryRepository

  @Binds
  abstract fun bindTransactionsRepository(impl: TransactionsLocalRepository): TransactionsRepository

  @Binds
  abstract fun bindShareLinkRepository(impl: BdsShareLinkRepository): ShareLinkRepository

  @Binds
  abstract fun bindCarrierBillingPreferencesRepository(impl: SecureCarrierBillingPreferencesRepository): CarrierBillingPreferencesRepository

  @Binds
  abstract fun bindFeatureFlagsRepository(impl: FeatureFlagsRepositoryImpl): FeatureFlagsRepository

  @Binds
  @TopUpProbe
  abstract fun bindTopUpFeatureFlagsRepository(impl: TopUpDefaultValueProbe): FeatureFlagsRepository

  @Binds
  abstract fun bindAndroidIdRepository(impl: AndroidIdRepositoryImpl): AndroidIdRepository

  @Binds
  @Named("TrackPendingTransactionService")
  abstract fun bindTrackPendingTransactionService(impl: TrackPendingTransactionService): TrackTransactionService

  @Binds
  @Named("PendingTransactionService")
  abstract fun bindPendingTransactionService(impl: PendingTransactionService): TrackTransactionService

  @Binds
  @Named("NotTrackTransactionService")
  abstract fun bindNotTrackTransactionService(impl: NotTrackTransactionService): TrackTransactionService

  @Binds
  @Named("BdsPendingTransactionService")
  abstract fun bindBdsPendingTransactionService(impl: BdsPendingTransactionService): TrackTransactionService
}