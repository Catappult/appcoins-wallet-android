package com.asfoundation.wallet.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.appcoins.wallet.permissions.Permissions
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.Airdrop
import com.asfoundation.wallet.AirdropService
import com.asfoundation.wallet.abtesting.ABTestInteractor
import com.asfoundation.wallet.abtesting.ABTestRepository
import com.asfoundation.wallet.abtesting.experiments.balancewallets.BalanceWalletsExperiment
import com.asfoundation.wallet.advertise.AdvertisingThrowableCodeMapper
import com.asfoundation.wallet.advertise.CampaignInteract
import com.asfoundation.wallet.analytics.LaunchAnalytics
import com.asfoundation.wallet.analytics.LaunchInteractor
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.backup.BackupInteract
import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.*
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.referrals.ReferralInteractor
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.SharedPreferencesReferralLocalData
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.service.AccountWalletService
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.topup.TopUpInteractor
import com.asfoundation.wallet.topup.TopUpLimitValues
import com.asfoundation.wallet.topup.TopUpValuesService
import com.asfoundation.wallet.ui.FingerprintInteractor
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.ui.balance.detail.TransactionDetailInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentInteractor
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.verification.VerificationRepository
import com.asfoundation.wallet.verification.WalletVerificationInteractor
import com.asfoundation.wallet.vouchers.VouchersInteractor
import com.asfoundation.wallet.vouchers.VouchersRepository
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallet_blocked.WalletStatusRepository
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import javax.inject.Singleton

@Module
class InteractorModule {

  @Singleton
  @Provides
  fun providesLaunchInteractor(launchAnalytics: LaunchAnalytics,
                               sharedPreferences: SharedPreferences,
                               packageManager: PackageManager): LaunchInteractor {
    return LaunchInteractor(launchAnalytics, sharedPreferences, packageManager)
  }

  @Provides
  @Named("APPROVE_SERVICE_ON_CHAIN")
  fun provideApproveService(sendTransactionInteract: SendTransactionInteract,
                            errorMapper: ErrorMapper, @Named("no_wait_transaction")
                            noWaitPendingTransactionService: TrackTransactionService): ApproveService {
    return ApproveService(WatchedTransactionService(object : TransactionSender {
      override fun send(transactionBuilder: TransactionBuilder): Single<String> {
        return sendTransactionInteract.approve(transactionBuilder)
      }
    }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), errorMapper, Schedulers.io(),
        noWaitPendingTransactionService), NoValidateTransactionValidator())
  }

  @Provides
  fun provideFetchGasSettingsInteract(
      gasSettingsRepository: GasSettingsRepositoryType): FetchGasSettingsInteract {
    return FetchGasSettingsInteract(gasSettingsRepository, Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  @Provides
  fun provideFindDefaultWalletInteract(
      walletRepository: WalletRepositoryType): FindDefaultWalletInteract {
    return FindDefaultWalletInteract(walletRepository, Schedulers.io())
  }

  @Provides
  fun provideGetDefaultWalletBalance(walletRepository: WalletRepositoryType,
                                     defaultWalletInteract: FindDefaultWalletInteract,
                                     fetchCreditsInteract: FetchCreditsInteract,
                                     networkInfo: NetworkInfo,
                                     tokenRepositoryType: TokenRepositoryType): GetDefaultWalletBalanceInteract {
    return GetDefaultWalletBalanceInteract(walletRepository, defaultWalletInteract,
        fetchCreditsInteract, networkInfo, tokenRepositoryType)
  }


  @Provides
  fun provideWalletBlockedInteract(findDefaultWalletInteract: FindDefaultWalletInteract,
                                   walletStatusRepository: WalletStatusRepository): WalletBlockedInteract {
    return WalletBlockedInteract(findDefaultWalletInteract, walletStatusRepository)
  }

  @Provides
  fun provideSendTransactionInteract(transactionRepository: TransactionRepositoryType,
                                     passwordStore: PasswordStore): SendTransactionInteract {
    return SendTransactionInteract(transactionRepository, passwordStore)
  }

  @Singleton
  @Provides
  fun provideBdsInAppPurchaseInteractor(
      billingPaymentProofSubmission: BillingPaymentProofSubmission,
      @Named("ASF_BDS_IN_APP_INTERACTOR") inAppPurchaseInteractor: AsfInAppPurchaseInteractor,
      billing: Billing): BdsInAppPurchaseInteractor {
    return BdsInAppPurchaseInteractor(inAppPurchaseInteractor, billingPaymentProofSubmission,
        ApproveKeyProvider(billing), billing)
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
      billingMessagesMapper: BillingMessagesMapper): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        billingMessagesMapper, billing, currencyConversionService, bdsTransactionService,
        Schedulers.io())
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
      billingMessagesMapper: BillingMessagesMapper): AsfInAppPurchaseInteractor {
    return AsfInAppPurchaseInteractor(inAppPurchaseService, defaultWalletInteract,
        gasSettingsInteract, BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT), parser,
        billingMessagesMapper, billing, currencyConversionService, bdsTransactionService,
        Schedulers.io())
  }

  @Singleton
  @Provides
  fun provideDualInAppPurchaseInteractor(bdsInAppPurchaseInteractor: BdsInAppPurchaseInteractor,
                                         @Named("ASF_IN_APP_INTERACTOR")
                                         asfInAppPurchaseInteractor: AsfInAppPurchaseInteractor,
                                         appcoinsRewards: AppcoinsRewards, billing: Billing,
                                         sharedPreferences: SharedPreferences,
                                         packageManager: PackageManager,
                                         backupInteract: BackupInteractContract): InAppPurchaseInteractor {
    return InAppPurchaseInteractor(asfInAppPurchaseInteractor, bdsInAppPurchaseInteractor,
        ExternalBillingSerializer(), appcoinsRewards, billing, sharedPreferences, packageManager,
        backupInteract)
  }

  @Provides
  fun provideLocalPaymentInteractor(walletService: WalletService,
                                    partnerAddressService: AddressService,
                                    inAppPurchaseInteractor: InAppPurchaseInteractor,
                                    billing: Billing, billingMessagesMapper: BillingMessagesMapper,
                                    supportInteractor: SupportInteractor,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    walletVerificationInteractor: WalletVerificationInteractor,
                                    remoteRepository: RemoteRepository,
                                    vouchersInteractor: VouchersInteractor): LocalPaymentInteractor {
    return LocalPaymentInteractor(walletService, partnerAddressService,
        inAppPurchaseInteractor, billing, billingMessagesMapper, supportInteractor,
        walletBlockedInteract, walletVerificationInteractor, remoteRepository, vouchersInteractor)
  }

  @Provides
  fun provideFetchCreditsInteract(balanceGetter: BalanceGetter) =
      FetchCreditsInteract(balanceGetter)

  @Provides
  fun provideFindDefaultNetworkInteract(networkInfo: NetworkInfo) =
      FindDefaultNetworkInteract(networkInfo, AndroidSchedulers.mainThread())

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

  @Provides
  fun provideAdyenPaymentInteractor(context: Context,
                                    adyenPaymentRepository: AdyenPaymentRepository,
                                    inAppPurchaseInteractor: InAppPurchaseInteractor,
                                    partnerAddressService: AddressService, billing: Billing,
                                    walletService: WalletService,
                                    supportInteractor: SupportInteractor,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    walletVerificationInteractor: WalletVerificationInteractor,
                                    billingAddressRepository: BillingAddressRepository,
                                    vouchersInteractor: VouchersInteractor): AdyenPaymentInteractor {
    return AdyenPaymentInteractor(adyenPaymentRepository, inAppPurchaseInteractor,
        inAppPurchaseInteractor.billingMessagesMapper, partnerAddressService, billing,
        walletService, supportInteractor, walletBlockedInteract, walletVerificationInteractor,
        billingAddressRepository, vouchersInteractor)
  }

  @Provides
  fun provideWalletCreatorInteract(accountRepository: WalletRepositoryType,
                                   passwordStore: PasswordStore, syncScheduler: ExecutorScheduler) =
      WalletCreatorInteract(accountRepository, passwordStore, syncScheduler)

  @Provides
  fun provideGamificationInteractor(gamification: Gamification,
                                    defaultWallet: FindDefaultWalletInteract,
                                    conversionService: LocalCurrencyConversionService) =
      GamificationInteractor(gamification, defaultWallet, conversionService)

  @Provides
  fun providePromotionsInteractor(referralInteractor: ReferralInteractorContract,
                                  gamificationInteractor: GamificationInteractor,
                                  promotionsRepository: PromotionsRepository,
                                  vouchersRepository: VouchersRepository,
                                  findDefaultWalletInteract: FindDefaultWalletInteract,
                                  rakamAnalytics: RakamAnalytics,
                                  userStatsLocalData: UserStatsLocalData,
                                  gamificationMapper: GamificationMapper,
                                  impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType): PromotionsInteractor {
    return PromotionsInteractor(referralInteractor, gamificationInteractor,
        promotionsRepository, vouchersRepository, findDefaultWalletInteract, userStatsLocalData,
        rakamAnalytics, gamificationMapper, impressionPreferencesRepositoryType)
  }

  @Provides
  fun provideReferralInteractor(preferences: SharedPreferences,
                                findDefaultWalletInteract: FindDefaultWalletInteract,
                                promotionsRepository: PromotionsRepository): ReferralInteractorContract {
    return ReferralInteractor(SharedPreferencesReferralLocalData(preferences),
        findDefaultWalletInteract, promotionsRepository)
  }

  @Provides
  fun providesShareLinkInteractor(repository: ShareLinkRepository,
                                  interactor: FindDefaultWalletInteract,
                                  inAppPurchaseInteractor: InAppPurchaseInteractor) =
      ShareLinkInteractor(repository, interactor, inAppPurchaseInteractor)

  @Singleton
  @Provides
  fun providesTopUpInteractor(repository: BdsRepository,
                              conversionService: LocalCurrencyConversionService,
                              gamificationInteractor: GamificationInteractor,
                              topUpValuesService: TopUpValuesService,
                              walletBlockedInteract: WalletBlockedInteract,
                              inAppPurchaseInteractor: InAppPurchaseInteractor,
                              supportInteractor: SupportInteractor) =
      TopUpInteractor(repository, conversionService, gamificationInteractor, topUpValuesService,
          LinkedHashMap(), TopUpLimitValues(), walletBlockedInteract, inAppPurchaseInteractor,
          supportInteractor)

  @Singleton
  @Provides
  fun provideBalanceInteract(walletService: WalletService,
                             balanceRepository: BalanceRepository,
                             walletVerificationInteractor: WalletVerificationInteractor,
                             backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                             verificationRepository: VerificationRepository) =
      BalanceInteractor(walletService as AccountWalletService, balanceRepository,
          walletVerificationInteractor, backupRestorePreferencesRepository,
          verificationRepository, Schedulers.io())

  @Provides
  fun provideAutoUpdateInteract(autoUpdateRepository: AutoUpdateRepository,
                                @Named("local_version_code")
                                localVersionCode: Int, packageManager: PackageManager,
                                impressionPreferences: ImpressionPreferencesRepositoryType,
                                context: Context) =
      AutoUpdateInteract(autoUpdateRepository, localVersionCode, Build.VERSION.SDK_INT,
          packageManager, context.packageName, impressionPreferences)

  @Singleton
  @Provides
  fun provideFileInteract(context: Context, contentResolver: ContentResolver,
                          backupRestorePreferencesRepository: BackupRestorePreferencesRepository) =
      FileInteractor(context, contentResolver, backupRestorePreferencesRepository)

  @Provides
  fun providePaymentMethodsInteractor(supportInteractor: SupportInteractor,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteractor: BalanceInteractor,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      fingerprintPreferences: FingerprintPreferencesRepositoryContract,
                                      billing: Billing,
                                      billingMessagesMapper: BillingMessagesMapper,
                                      bdsPendingTransactionService: BdsPendingTransactionService): PaymentMethodsInteractor {
    return PaymentMethodsInteractor(supportInteractor, gamificationInteractor, balanceInteractor,
        walletBlockedInteract, inAppPurchaseInteractor, fingerprintPreferences, billing,
        billingMessagesMapper, bdsPendingTransactionService)
  }

  @Provides
  fun provideMergedAppcoinsInteractor(balanceInteractor: BalanceInteractor,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      supportInteractor: SupportInteractor,
                                      inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      fingerprintPreferences: FingerprintPreferencesRepositoryContract): MergedAppcoinsInteractor {
    return MergedAppcoinsInteractor(balanceInteractor, walletBlockedInteract, supportInteractor,
        inAppPurchaseInteractor, fingerprintPreferences)
  }

  @Provides
  fun providesAppcoinsRewardsBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                         supportInteractor: SupportInteractor,
                                         walletService: WalletService,
                                         walletBlockedInteract: WalletBlockedInteract,
                                         walletVerificationInteractor: WalletVerificationInteractor,
                                         vouchersInteractor: VouchersInteractor): AppcoinsRewardsBuyInteract {
    return AppcoinsRewardsBuyInteract(inAppPurchaseInteractor, supportInteractor, walletService,
        walletBlockedInteract, walletVerificationInteractor, vouchersInteractor)
  }

  @Provides
  fun providesOnChainBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                 supportInteractor: SupportInteractor,
                                 walletService: WalletService,
                                 walletBlockedInteract: WalletBlockedInteract,
                                 walletVerificationInteractor: WalletVerificationInteractor,
                                 vouchersInteractor: VouchersInteractor): OnChainBuyInteract {
    return OnChainBuyInteract(inAppPurchaseInteractor, supportInteractor, walletService,
        walletBlockedInteract, walletVerificationInteractor, vouchersInteractor)
  }

  @Singleton
  @Provides
  fun providesPermissionsInteractor(permissions: Permissions,
                                    walletService: FindDefaultWalletInteract): PermissionsInteractor {
    return PermissionsInteractor(permissions, walletService)
  }

  @Singleton
  @Provides
  fun provideCampaignInteract(campaignService: CampaignService, walletService: WalletService,
                              autoUpdateInteract: AutoUpdateInteract,
                              findDefaultWalletInteract: FindDefaultWalletInteract,
                              impressionPreferences: ImpressionPreferencesRepositoryType): CampaignInteract {
    return CampaignInteract(campaignService, walletService, autoUpdateInteract,
        AdvertisingThrowableCodeMapper(), findDefaultWalletInteract, impressionPreferences)
  }

  @Provides
  fun provideTransactionsViewInteract(findDefaultNetworkInteract: FindDefaultNetworkInteract,
                                      findDefaultWalletInteract: FindDefaultWalletInteract,
                                      fetchTransactionsInteract: FetchTransactionsInteract,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteractor: BalanceInteractor,
                                      promotionsInteractor: PromotionsInteractor,
                                      cardNotificationsInteractor: CardNotificationsInteractor,
                                      autoUpdateInteract: AutoUpdateInteract,
                                      ratingInteractor: RatingInteractor,
                                      impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType,
                                      packageManager: PackageManager,
                                      fingerprintInteractor: FingerprintInteractor,
                                      fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract,
                                      balanceWalletsExperiment: BalanceWalletsExperiment): TransactionViewInteractor {
    return TransactionViewInteractor(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, gamificationInteractor, balanceInteractor,
        promotionsInteractor, cardNotificationsInteractor, autoUpdateInteract, ratingInteractor,
        impressionPreferencesRepositoryType, packageManager, fingerprintInteractor,
        fingerprintPreferencesRepository, balanceWalletsExperiment)
  }

  @Provides
  fun provideFetchTransactionsInteract(
      transactionRepository: TransactionRepositoryType): FetchTransactionsInteract {
    return FetchTransactionsInteract(transactionRepository)
  }

  @Provides
  fun provideBackupInteractor(sharedPreferences: PreferencesRepositoryType,
                              backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                              gamificationInteractor: GamificationInteractor,
                              fetchTransactionsInteract: FetchTransactionsInteract,
                              balanceInteractor: BalanceInteractor,
                              findDefaultWalletInteract: FindDefaultWalletInteract): BackupInteractContract {
    return BackupInteract(sharedPreferences, backupRestorePreferencesRepository,
        fetchTransactionsInteract, balanceInteractor, gamificationInteractor,
        findDefaultWalletInteract)
  }

  @Provides
  fun provideCardNotificationInteractor(referralInteractor: ReferralInteractorContract,
                                        autoUpdateInteract: AutoUpdateInteract,
                                        backupInteract: BackupInteractContract,
                                        promotionsInteractor: PromotionsInteractor): CardNotificationsInteractor {
    return CardNotificationsInteractor(referralInteractor, autoUpdateInteract,
        backupInteract, promotionsInteractor, Schedulers.io())
  }

  @Singleton
  @Provides
  fun provideTokenRepository(defaultTokenProvider: DefaultTokenProvider,
                             walletRepositoryType: WalletRepositoryType): TokenRepository {
    return TokenRepository(defaultTokenProvider, walletRepositoryType)
  }

  @Provides
  fun provideSetDefaultAccountInteract(
      accountRepository: WalletRepositoryType): SetDefaultWalletInteractor {
    return SetDefaultWalletInteractor(
        accountRepository)
  }

  @Provides
  fun provideDeleteAccountInteract(accountRepository: WalletRepositoryType, store: PasswordStore,
                                   walletVerificationInteractor: WalletVerificationInteractor,
                                   backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                                   fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract): DeleteWalletInteract {
    return DeleteWalletInteract(accountRepository, store, walletVerificationInteractor,
        backupRestorePreferencesRepository, fingerprintPreferencesRepository)
  }

  @Provides
  fun provideFetchAccountsInteract(accountRepository: WalletRepositoryType): FetchWalletsInteract {
    return FetchWalletsInteract(accountRepository)
  }

  @Provides
  fun provideExportWalletInteract(walletRepository: WalletRepositoryType,
                                  passwordStore: PasswordStore): ExportWalletInteractor {
    return ExportWalletInteractor(walletRepository, passwordStore)
  }

  @Provides
  fun provideWalletsInteract(balanceInteractor: BalanceInteractor,
                             fetchWalletsInteract: FetchWalletsInteract,
                             walletCreatorInteract: WalletCreatorInteract,
                             supportInteractor: SupportInteractor,
                             sharedPreferencesRepository: SharedPreferencesRepository,
                             gamification: Gamification, logger: Logger): WalletsInteract {
    return WalletsInteract(balanceInteractor, fetchWalletsInteract, walletCreatorInteract,
        supportInteractor, sharedPreferencesRepository, gamification, logger)
  }

  @Provides
  fun provideWalletDetailInteract(balanceInteractor: BalanceInteractor,
                                  setDefaultWalletInteractor: SetDefaultWalletInteractor,
                                  supportInteractor: SupportInteractor,
                                  gamification: Gamification): WalletDetailsInteractor {
    return WalletDetailsInteractor(balanceInteractor, setDefaultWalletInteractor, supportInteractor,
        gamification)
  }

  @Singleton
  @Provides
  fun provideRestoreWalletInteract(
      walletRepository: WalletRepositoryType, passwordStore: PasswordStore,
      backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
      setDefaultWalletInteractor: SetDefaultWalletInteractor,
      fileInteractor: FileInteractor): RestoreWalletInteractor {
    return RestoreWalletInteractor(walletRepository, setDefaultWalletInteractor,
        passwordStore, backupRestorePreferencesRepository, fileInteractor)
  }

  @Provides
  fun provideIabInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                         autoUpdateInteract: AutoUpdateInteract,
                         supportInteractor: SupportInteractor,
                         gamificationRepository: Gamification,
                         walletBlockedInteract: WalletBlockedInteract): IabInteract {
    return IabInteract(inAppPurchaseInteractor, autoUpdateInteract, supportInteractor,
        gamificationRepository, walletBlockedInteract)
  }

  @Provides
  fun provideFingerprintInteract(biometricManager: BiometricManager,
                                 packageManager: PackageManager,
                                 fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract): FingerprintInteractor {
    return FingerprintInteractor(biometricManager, packageManager, fingerprintPreferencesRepository)
  }

  @Singleton
  @Provides
  fun providesCarrierInteractor(repository: CarrierBillingRepository, walletService: WalletService,
                                partnerAddressService: AddressService,
                                inAppPurchaseInteractor: InAppPurchaseInteractor,
                                walletBlockedInteract: WalletBlockedInteract,
                                walletVerificationInteractor: WalletVerificationInteractor,
                                billing: Billing,
                                billingMessagesMapper: BillingMessagesMapper,
                                logger: Logger,
                                vouchersInteractor: VouchersInteractor): CarrierInteractor {
    return CarrierInteractor(repository, walletService, partnerAddressService,
        inAppPurchaseInteractor, walletBlockedInteract, walletVerificationInteractor,
        vouchersInteractor, billing, billingMessagesMapper, logger, Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesSupportInteractor(supportRepository: SupportRepository,
                                walletService: WalletService,
                                gamificationRepository: Gamification): SupportInteractor {
    return SupportInteractor(supportRepository, walletService, gamificationRepository,
        AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesABTestInteractor(abTestRepository: ABTestRepository): ABTestInteractor {
    return ABTestInteractor(abTestRepository)
  }

  @Singleton
  @Provides
  fun providesRatingInteractor(ratingRepository: RatingRepository,
                               gamificationInteractor: GamificationInteractor,
                               walletService: WalletService): RatingInteractor {
    return RatingInteractor(ratingRepository, gamificationInteractor, walletService,
        Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesWalletVerificationInteractor(verificationRepository: VerificationRepository,
                                           adyenPaymentRepository: AdyenPaymentRepository,
                                           walletService: WalletService): WalletVerificationInteractor {
    return WalletVerificationInteractor(verificationRepository, adyenPaymentRepository,
        walletService)
  }

  @Singleton
  @Provides
  fun providesTransactionDetailInteractor(
      vouchersRepository: VouchersRepository): TransactionDetailInteractor {
    return TransactionDetailInteractor(vouchersRepository)
  }

  @Singleton
  @Provides
  fun providesVouchersInteractor(vouchersRepository: VouchersRepository,
                                 walletService: WalletService): VouchersInteractor {
    return VouchersInteractor(vouchersRepository, walletService)
  }
}