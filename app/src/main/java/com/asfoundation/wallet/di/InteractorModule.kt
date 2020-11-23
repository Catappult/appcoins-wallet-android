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
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.permissions.Permissions
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.Airdrop
import com.asfoundation.wallet.AirdropService
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
import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.topup.TopUpInteractor
import com.asfoundation.wallet.topup.TopUpLimitValues
import com.asfoundation.wallet.topup.TopUpValuesService
import com.asfoundation.wallet.ui.FingerPrintInteractor
import com.asfoundation.wallet.ui.SettingsInteractor
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.airdrop.AirdropInteractor
import com.asfoundation.wallet.ui.airdrop.AppcoinsTransactionService
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor
import com.asfoundation.wallet.ui.transact.TransactionDataValidator
import com.asfoundation.wallet.ui.transact.TransferInteractor
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.util.TransferParser
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
                                    supportRepository: SupportRepository,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    smsValidationInteract: SmsValidationInteract,
                                    remoteRepository: RemoteRepository): LocalPaymentInteractor {
    return LocalPaymentInteractor(repository, walletService, partnerAddressService,
        inAppPurchaseInteractor, billing, billingMessagesMapper, supportRepository,
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
  fun provideAdyenPaymentInteractor(context: Context,
                                    adyenPaymentRepository: AdyenPaymentRepository,
                                    inAppPurchaseInteractor: InAppPurchaseInteractor,
                                    partnerAddressService: AddressService, billing: Billing,
                                    walletService: WalletService,
                                    supportRepository: SupportRepository,
                                    walletBlockedInteract: WalletBlockedInteract,
                                    smsValidationInteract: SmsValidationInteract): AdyenPaymentInteractor {
    return AdyenPaymentInteractor(adyenPaymentRepository, inAppPurchaseInteractor,
        inAppPurchaseInteractor.billingMessagesMapper, partnerAddressService, billing,
        walletService, supportRepository, walletBlockedInteract, smsValidationInteract)
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
                                  findDefaultWalletInteract: FindDefaultWalletInteract,
                                  gamificationMapper: GamificationMapper): PromotionsInteractorContract {
    return PromotionsInteractor(referralInteractor, gamificationInteractor,
        promotionsRepository, findDefaultWalletInteract, gamificationMapper)
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
                              supportRepository: SupportRepository) =
      TopUpInteractor(repository, conversionService, gamificationInteractor, topUpValuesService,
          LinkedHashMap(), TopUpLimitValues(), walletBlockedInteract, inAppPurchaseInteractor,
          supportRepository)

  @Singleton
  @Provides
  fun provideSmsValidationInteract(smsValidationRepository: SmsValidationRepositoryType,
                                   preferencesRepositoryType: PreferencesRepositoryType) =
      SmsValidationInteract(smsValidationRepository, preferencesRepositoryType)

  @Singleton
  @Provides
  fun provideBalanceInteract(findDefaultWalletInteract: FindDefaultWalletInteract,
                             balanceRepository: BalanceRepository,
                             preferencesRepositoryType: PreferencesRepositoryType,
                             smsValidationInteract: SmsValidationInteract) =
      BalanceInteractor(findDefaultWalletInteract, balanceRepository,
          preferencesRepositoryType, smsValidationInteract)

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
                                      supportRepository: SupportRepository,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteractor: BalanceInteractor,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      preferencesRepositoryType: PreferencesRepositoryType,
                                      billing: Billing,
                                      bdsPendingTransactionService: BdsPendingTransactionService): PaymentMethodsInteractor {
    return PaymentMethodsInteractor(walletService, supportRepository, gamificationInteractor,
        balanceInteractor, walletBlockedInteract, inAppPurchaseInteractor,
        preferencesRepositoryType,
        billing, bdsPendingTransactionService)
  }

  @Provides
  fun provideMergedAppcoinsInteractor(balanceInteractor: BalanceInteractor,
                                      walletBlockedInteract: WalletBlockedInteract,
                                      supportRepository: SupportRepository,
                                      inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      walletService: WalletService,
                                      preferencesRepositoryType: PreferencesRepositoryType): MergedAppcoinsInteractor {
    return MergedAppcoinsInteractor(balanceInteractor, walletBlockedInteract, supportRepository,
        inAppPurchaseInteractor, walletService, preferencesRepositoryType)
  }

  @Provides
  fun providesAppcoinsRewardsBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                         supportRepository: SupportRepository,
                                         walletService: WalletService,
                                         walletBlockedInteract: WalletBlockedInteract,
                                         smsValidationInteract: SmsValidationInteract): AppcoinsRewardsBuyInteract {
    return AppcoinsRewardsBuyInteract(inAppPurchaseInteractor, supportRepository, walletService,
        walletBlockedInteract, smsValidationInteract)
  }

  @Provides
  fun providesOnChainBuyInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                                 supportRepository: SupportRepository,
                                 walletService: WalletService,
                                 walletBlockedInteract: WalletBlockedInteract,
                                 smsValidationInteract: SmsValidationInteract): OnChainBuyInteract {
    return OnChainBuyInteract(inAppPurchaseInteractor, supportRepository, walletService,
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

  @Provides
  fun provideTransactionsViewInteract(findDefaultNetworkInteract: FindDefaultNetworkInteract,
                                      findDefaultWalletInteract: FindDefaultWalletInteract,
                                      fetchTransactionsInteract: FetchTransactionsInteract,
                                      gamificationInteractor: GamificationInteractor,
                                      balanceInteractor: BalanceInteractor,
                                      promotionsInteractorContract: PromotionsInteractorContract,
                                      cardNotificationsInteractor: CardNotificationsInteractor,
                                      autoUpdateInteract: AutoUpdateInteract): TransactionViewInteract {
    return TransactionViewInteract(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, gamificationInteractor, balanceInteractor,
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
                              balanceInteractor: BalanceInteractor,
                              findDefaultWalletInteract: FindDefaultWalletInteract): BackupInteractContract {
    return BackupInteract(sharedPreferences, fetchTransactionsInteract, balanceInteractor,
        gamificationInteractor, findDefaultWalletInteract)
  }

  @Provides
  fun provideCardNotificationInteractor(referralInteractor: ReferralInteractorContract,
                                        autoUpdateInteract: AutoUpdateInteract,
                                        backupInteract: BackupInteractContract,
                                        promotionsInteractorContract: PromotionsInteractorContract): CardNotificationsInteractor {
    return CardNotificationsInteractor(referralInteractor, autoUpdateInteract,
        backupInteract, promotionsInteractorContract)
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
  fun provideWalletsInteract(balanceInteractor: BalanceInteractor,
                             fetchWalletsInteract: FetchWalletsInteract,
                             walletCreatorInteract: WalletCreatorInteract,
                             supportRepository: SupportRepository,
                             sharedPreferencesRepository: SharedPreferencesRepository,
                             gamification: Gamification, logger: Logger): WalletsInteract {
    return WalletsInteract(balanceInteractor, fetchWalletsInteract, walletCreatorInteract,
        supportRepository, sharedPreferencesRepository, gamification, logger)
  }

  @Provides
  fun provideWalletDetailInteract(balanceInteractor: BalanceInteractor,
                                  setDefaultWalletInteractor: SetDefaultWalletInteractor,
                                  supportRepository: SupportRepository,
                                  gamification: Gamification): WalletDetailsInteractor {
    return WalletDetailsInteractor(balanceInteractor, setDefaultWalletInteractor, supportRepository,
        gamification)
  }

  @Singleton
  @Provides
  fun provideRestoreWalletInteract(
      walletRepository: WalletRepositoryType, passwordStore: PasswordStore,
      preferencesRepositoryType: PreferencesRepositoryType,
      setDefaultWalletInteractor: SetDefaultWalletInteractor,
      fileInteractor: FileInteractor): RestoreWalletInteractor {
    return RestoreWalletInteractor(walletRepository, setDefaultWalletInteractor,
        passwordStore, preferencesRepositoryType, fileInteractor)
  }

  @Provides
  fun providesSettingsInteract(findDefaultWalletInteract: FindDefaultWalletInteract,
                               supportRepository: SupportRepository,
                               walletsInteract: WalletsInteract,
                               autoUpdateInteract: AutoUpdateInteract,
                               fingerPrintInteractor: FingerPrintInteractor,
                               walletsEventSender: WalletsEventSender,
                               preferencesRepositoryType: PreferencesRepositoryType): SettingsInteractor {
    return SettingsInteractor(findDefaultWalletInteract, supportRepository, walletsInteract,
        autoUpdateInteract, fingerPrintInteractor, walletsEventSender, preferencesRepositoryType)
  }

  @Provides
  fun provideIabInteract(inAppPurchaseInteractor: InAppPurchaseInteractor,
                         autoUpdateInteract: AutoUpdateInteract,
                         supportRepository: SupportRepository,
                         gamificationRepository: Gamification,
                         walletBlockedInteract: WalletBlockedInteract): IabInteract {
    return IabInteract(inAppPurchaseInteractor, autoUpdateInteract, supportRepository,
        gamificationRepository, walletBlockedInteract)
  }

  @Provides
  fun provideFingerprintInteract(biometricManager: BiometricManager,
                                 packageManager: PackageManager,
                                 preferencesRepositoryType: PreferencesRepositoryType): FingerPrintInteractor {
    return FingerPrintInteractor(biometricManager, packageManager, preferencesRepositoryType)
  }

}