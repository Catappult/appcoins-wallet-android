package com.asfoundation.wallet.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.permissions.Permissions
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.Airdrop
import com.asfoundation.wallet.AirdropService
import com.asfoundation.wallet.App
import com.asfoundation.wallet.advertise.AdvertisingThrowableCodeMapper
import com.asfoundation.wallet.advertise.CampaignInteract
import com.asfoundation.wallet.backup.BackupInteract
import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.*
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.promotions.PromotionsInteractorContract
import com.asfoundation.wallet.referrals.ReferralInteractor
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.SharedPreferencesReferralLocalData
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.support.SupportSharedPreferences
import com.asfoundation.wallet.topup.TopUpInteractor
import com.asfoundation.wallet.topup.TopUpLimitValues
import com.asfoundation.wallet.topup.TopUpValuesService
import com.asfoundation.wallet.ui.FingerPrintInteract
import com.asfoundation.wallet.ui.SettingsInteract
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.ui.balance.RestoreWalletPasswordInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor
import com.asfoundation.wallet.ui.onboarding.OnboardingInteract
import com.asfoundation.wallet.ui.transact.TransactionDataValidator
import com.asfoundation.wallet.ui.transact.TransferInteractor
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallet_blocked.WalletStatusRepository
import com.google.gson.Gson
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
class InteractModule {

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
  fun provideLocalPaymentInteractor(repository: InAppDeepLinkRepository,
                                    walletService: WalletService,
                                    partnerAddressService: AddressService,
                                    inAppPurchaseInteractor: InAppPurchaseInteractor,
                                    billing: Billing, billingMessagesMapper: BillingMessagesMapper,
                                    supportInteractor: SupportInteractor,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    smsValidationInteract: SmsValidationInteract,
                                    remoteRepository: RemoteRepository): LocalPaymentInteractor {
    return LocalPaymentInteractor(repository, walletService, partnerAddressService,
        inAppPurchaseInteractor, billing, billingMessagesMapper, supportInteractor,
        walletBlockedInteract, smsValidationInteract, remoteRepository)
  }

  @Provides
  fun provideFetchCreditsInteract(balanceGetter: BalanceGetter) =
      FetchCreditsInteract(balanceGetter)

  @Provides
  fun provideFindDefaultNetworkInteract(networkInfo: NetworkInfo) =
      FindDefaultNetworkInteract(networkInfo, AndroidSchedulers.mainThread())

  @Singleton
  @Provides
  fun provideTransferInteractor(rewardsManager: RewardsManager,
                                balance: GetDefaultWalletBalanceInteract,
                                findWallet: FindDefaultWalletInteract) =
      TransferInteractor(rewardsManager, TransactionDataValidator(), balance, findWallet)

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
  fun provideAdyenPaymentInteractor(adyenPaymentRepository: AdyenPaymentRepository,
                                    inAppPurchaseInteractor: InAppPurchaseInteractor,
                                    partnerAddressService: AddressService, billing: Billing,
                                    walletService: WalletService,
                                    supportInteractor: SupportInteractor,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    smsValidationInteract: SmsValidationInteract): AdyenPaymentInteractor {
    return AdyenPaymentInteractor(adyenPaymentRepository, inAppPurchaseInteractor,
        inAppPurchaseInteractor.billingMessagesMapper, partnerAddressService, billing,
        walletService, supportInteractor, walletBlockedInteract, smsValidationInteract)
  }

  @Provides
  fun provideWalletCreatorInteract(accountRepository: WalletRepositoryType,
                                   passwordStore: PasswordStore, syncScheduler: ExecutorScheduler) =
      WalletCreatorInteract(accountRepository, passwordStore, syncScheduler)

  @Provides
  fun provideOnboardingInteract(walletService: WalletService,
                                preferencesRepositoryType: PreferencesRepositoryType,
                                supportInteractor: SupportInteractor, gamification: Gamification,
                                smsValidationInteract: SmsValidationInteract,
                                referralInteractor: ReferralInteractorContract,
                                bdsRepository: BdsRepository) =
      OnboardingInteract(walletService, preferencesRepositoryType, supportInteractor, gamification,
          smsValidationInteract, referralInteractor, bdsRepository)

  @Provides
  fun provideGamificationInteractor(gamification: Gamification,
                                    defaultWallet: FindDefaultWalletInteract,
                                    conversionService: LocalCurrencyConversionService) =
      GamificationInteractor(gamification, defaultWallet, conversionService)

  @Provides
  fun providePromotionsInteractor(referralInteractor: ReferralInteractorContract,
                                  gamificationInteractor: GamificationInteractor,
                                  promotionsRepository: PromotionsRepository,
                                  findDefaultWalletInteract: FindDefaultWalletInteract): PromotionsInteractorContract {
    return PromotionsInteractor(referralInteractor, gamificationInteractor,
        promotionsRepository, findDefaultWalletInteract)
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
  fun provideSmsValidationInteract(smsValidationRepository: SmsValidationRepositoryType,
                                   preferencesRepositoryType: PreferencesRepositoryType) =
      SmsValidationInteract(smsValidationRepository, preferencesRepositoryType)

  @Singleton
  @Provides
  fun provideBalanceInteract(findDefaultWalletInteract: FindDefaultWalletInteract,
                             balanceRepository: BalanceRepository,
                             preferencesRepositoryType: PreferencesRepositoryType) =
      BalanceInteract(findDefaultWalletInteract, balanceRepository,
          preferencesRepositoryType)

  @Provides
  fun provideAutoUpdateInteract(autoUpdateRepository: AutoUpdateRepository,
                                @Named("local_version_code")
                                localVersionCode: Int, packageManager: PackageManager,
                                sharedPreferences: PreferencesRepositoryType,
                                context: Context) =
      AutoUpdateInteract(autoUpdateRepository, localVersionCode, Build.VERSION.SDK_INT,
          packageManager, context.packageName, sharedPreferences)

  @Singleton
  @Provides
  fun provideFileInteract(context: Context, contentResolver: ContentResolver,
                          preferencesRepositoryType: PreferencesRepositoryType) =
      FileInteractor(context, contentResolver, preferencesRepositoryType)

  @Provides
  fun providePaymentMethodsInteractor(walletService: WalletService,
                                      supportInteractor: SupportInteractor,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteract: BalanceInteract,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      inAppPurchaseInteractor: InAppPurchaseInteractor): PaymentMethodsInteract {
    return PaymentMethodsInteract(walletService, supportInteractor, gamificationInteractor,
        balanceInteract, walletBlockedInteract, inAppPurchaseInteractor)
  }

  @Provides
  fun provideMergedAppcoinsInteractor(balanceInteract: BalanceInteract,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      supportInteractor: SupportInteractor,
                                      walletService: WalletService): MergedAppcoinsInteract {
    return MergedAppcoinsInteract(balanceInteract, walletBlockedInteract, supportInteractor,
        walletService)
  }

  @Provides
  fun providesAppcoinsRewardsBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                         supportInteractor: SupportInteractor,
                                         walletService: WalletService,
                                         walletBlockedInteract: WalletBlockedInteract,
                                         smsValidationInteract: SmsValidationInteract): AppcoinsRewardsBuyInteract {
    return AppcoinsRewardsBuyInteract(inAppPurchaseInteractor, supportInteractor, walletService,
        walletBlockedInteract, smsValidationInteract)
  }

  @Provides
  fun providesOnChainBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                 supportInteractor: SupportInteractor,
                                 walletService: WalletService,
                                 walletBlockedInteract: WalletBlockedInteract,
                                 smsValidationInteract: SmsValidationInteract): OnChainBuyInteract {
    return OnChainBuyInteract(inAppPurchaseInteractor, supportInteractor, walletService,
        walletBlockedInteract, smsValidationInteract)
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
                              sharedPreferences: PreferencesRepositoryType): CampaignInteract {
    return CampaignInteract(campaignService, walletService, autoUpdateInteract,
        AdvertisingThrowableCodeMapper(), findDefaultWalletInteract, sharedPreferences)
  }

  @Singleton
  @Provides
  fun provideSupportInteractor(preferences: SupportSharedPreferences, app: App): SupportInteractor {
    return SupportInteractor(preferences, app)
  }

  @Provides
  fun provideTransactionsViewInteract(findDefaultNetworkInteract: FindDefaultNetworkInteract,
                                      findDefaultWalletInteract: FindDefaultWalletInteract,
                                      fetchTransactionsInteract: FetchTransactionsInteract,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteract: BalanceInteract,
                                      promotionsInteractorContract: PromotionsInteractorContract,
                                      cardNotificationsInteractor: CardNotificationsInteractor,
                                      autoUpdateInteract: AutoUpdateInteract): TransactionViewInteract {
    return TransactionViewInteract(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, gamificationInteractor, balanceInteract,
        promotionsInteractorContract, cardNotificationsInteractor, autoUpdateInteract)
  }

  @Provides
  fun provideFetchTransactionsInteract(
      transactionRepository: TransactionRepositoryType): FetchTransactionsInteract {
    return FetchTransactionsInteract(transactionRepository)
  }

  @Provides
  fun provideBackupInteractor(sharedPreferences: PreferencesRepositoryType,
                              gamificationInteractor: GamificationInteractor,
                              fetchTransactionsInteract: FetchTransactionsInteract,
                              balanceInteract: BalanceInteract,
                              findDefaultWalletInteract: FindDefaultWalletInteract): BackupInteractContract {
    return BackupInteract(sharedPreferences, fetchTransactionsInteract, balanceInteract,
        gamificationInteractor, findDefaultWalletInteract)
  }

  @Provides
  fun provideCardNotificationInteractor(referralInteractor: ReferralInteractorContract,
                                        autoUpdateInteract: AutoUpdateInteract,
                                        backupInteract: BackupInteractContract): CardNotificationsInteractor {
    return CardNotificationsInteractor(referralInteractor, autoUpdateInteract,
        backupInteract)
  }

  @Singleton
  @Provides
  fun provideTokenRepository(defaultTokenProvider: DefaultTokenProvider,
                             walletRepositoryType: WalletRepositoryType): TokenRepository {
    return TokenRepository(defaultTokenProvider, walletRepositoryType)
  }

  @Provides
  fun provideSetDefaultAccountInteract(
      accountRepository: WalletRepositoryType): SetDefaultWalletInteract {
    return SetDefaultWalletInteract(accountRepository)
  }

  @Provides
  fun provideDeleteAccountInteract(accountRepository: WalletRepositoryType, store: PasswordStore,
                                   preferencesRepositoryType: PreferencesRepositoryType): DeleteWalletInteract {
    return DeleteWalletInteract(accountRepository, store, preferencesRepositoryType)
  }

  @Provides
  fun provideFetchAccountsInteract(accountRepository: WalletRepositoryType): FetchWalletsInteract {
    return FetchWalletsInteract(accountRepository)
  }

  @Provides
  fun provideExportWalletInteract(walletRepository: WalletRepositoryType,
                                  passwordStore: PasswordStore): ExportWalletInteract {
    return ExportWalletInteract(walletRepository, passwordStore)
  }

  @Provides
  fun provideWalletsInteract(balanceInteract: BalanceInteract,
                             fetchWalletsInteract: FetchWalletsInteract,
                             walletcreatorInteract: WalletCreatorInteract,
                             supportInteractor: SupportInteractor,
                             sharedPreferencesRepository: SharedPreferencesRepository,
                             gamification: Gamification, logger: Logger): WalletsInteract {
    return WalletsInteract(balanceInteract, fetchWalletsInteract, walletcreatorInteract,
        supportInteractor, sharedPreferencesRepository, gamification, logger)
  }

  @Provides
  fun provideWalletDetailInteract(balanceInteract: BalanceInteract,
                                  setDefaultWalletInteract: SetDefaultWalletInteract,
                                  supportInteractor: SupportInteractor,
                                  gamification: Gamification): WalletDetailsInteractor {
    return WalletDetailsInteractor(balanceInteract, setDefaultWalletInteract, supportInteractor,
        gamification)
  }

  @Singleton
  @Provides
  fun provideRestoreWalletInteract(
      walletRepository: WalletRepositoryType, passwordStore: PasswordStore,
      preferencesRepositoryType: PreferencesRepositoryType,
      setDefaultWalletInteract: SetDefaultWalletInteract,
      fileInteractor: FileInteractor): RestoreWalletInteractor {
    return RestoreWalletInteractor(walletRepository, setDefaultWalletInteract,
        passwordStore, preferencesRepositoryType, fileInteractor)
  }

  @Singleton
  @Provides
  fun provideRestoreWalletInteractor(gson: Gson, balanceInteract: BalanceInteract,
                                     restoreWalletInteractor: RestoreWalletInteractor): RestoreWalletPasswordInteractor {
    return RestoreWalletPasswordInteractor(gson, balanceInteract, restoreWalletInteractor)
  }


  @Provides
  fun providesSettingsInteract(findDefaultWalletInteract: FindDefaultWalletInteract,
                               smsValidationInteract: SmsValidationInteract,
                               preferencesRepositoryType: PreferencesRepositoryType,
                               supportInteractor: SupportInteractor,
                               walletsInteract: WalletsInteract,
                               autoUpdateInteract: AutoUpdateInteract,
                               walletsEventSender: WalletsEventSender): SettingsInteract {
    return SettingsInteract(findDefaultWalletInteract, smsValidationInteract,
        preferencesRepositoryType, supportInteractor, walletsInteract, autoUpdateInteract,
        walletsEventSender)
  }

  @Provides
  fun provideIabInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                         autoUpdateInteract: AutoUpdateInteract,
                         supportInteractor: SupportInteractor,
                         gamificationRepository: Gamification): IabInteract {
    return IabInteract(inAppPurchaseInteractor, autoUpdateInteract, supportInteractor,
        gamificationRepository)
  }

  @Provides
  fun provideFingerprintInteract(context: Context): FingerPrintInteract {
    return FingerPrintInteract(context)
  }

}