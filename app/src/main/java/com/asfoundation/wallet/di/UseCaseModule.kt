package com.asfoundation.wallet.di

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.logging.send_logs.use_cases.ObserveSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.ResetSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.SendLogsUseCase
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.nfts.usecases.EstimateNFTSendGasUseCase
import com.asfoundation.wallet.nfts.usecases.GetNFTListUseCase
import com.asfoundation.wallet.nfts.usecases.SendNFTUseCase
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObserveCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.SharedPreferencesReferralLocalData
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.service.KeyStoreFileManager
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
class UseCaseModule {
  @Singleton
  @Provides
  fun providesGetPromotionsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   observeLevels: ObserveLevelsUseCase,
                                   promotionsMapper: PromotionsMapper,
                                   promotionsRepository: PromotionsRepository,
                                   getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase): GetPromotionsUseCase {
    return GetPromotionsUseCase(getCurrentWallet, observeLevels, promotionsMapper,
        promotionsRepository, getCurrentPromoCodeUseCase)
  }

  @Singleton
  @Provides
  fun providesGetCurrentWalletUseCase(
      walletRepository: WalletRepositoryType): GetCurrentWalletUseCase {
    return GetCurrentWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesObserveLevelsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   gamification: Gamification): ObserveLevelsUseCase {
    return ObserveLevelsUseCase(getCurrentWallet, gamification)
  }

  @Singleton
  @Provides
  fun providesSetSeenWalletOriginUseCase(
      userStatsLocalData: UserStatsLocalData): SetSeenWalletOriginUseCase {
    return SetSeenWalletOriginUseCase(userStatsLocalData)
  }

  @Singleton
  @Provides
  fun providesSetSeenPromotionsUseCase(
      promotionsRepository: PromotionsRepository): SetSeenPromotionsUseCase {
    return SetSeenPromotionsUseCase(promotionsRepository)
  }

  @Singleton
  @Provides
  fun providesHasSeenPromotionTooltipUseCase(
      preferencesRepositoryType: PreferencesRepositoryType): HasSeenPromotionTooltipUseCase {
    return HasSeenPromotionTooltipUseCase(preferencesRepositoryType)
  }

  @Singleton
  @Provides
  fun providesIncreaseLaunchTimesUseCase(
      preferencesRepositoryType: PreferencesRepositoryType): IncreaseLaunchCountUseCase {
    return IncreaseLaunchCountUseCase(preferencesRepositoryType)
  }

  /*
   HOME Use Cases
   */

  @Singleton
  @Provides
  fun providesShouldOpenRatingDialogUseCase(ratingRepository: RatingRepository,
                                            getUserLevelUseCase: GetUserLevelUseCase): ShouldOpenRatingDialogUseCase {
    return ShouldOpenRatingDialogUseCase(ratingRepository, getUserLevelUseCase)
  }

  @Singleton
  @Provides
  fun providesUpdateTransactionsNumberUseCase(
      ratingRepository: RatingRepository): UpdateTransactionsNumberUseCase {
    return UpdateTransactionsNumberUseCase(ratingRepository)
  }

  @Singleton
  @Provides
  fun providesFindNetworkInfoUseCase(networkInfo: NetworkInfo): FindNetworkInfoUseCase {
    return FindNetworkInfoUseCase(networkInfo)
  }

  @Singleton
  @Provides
  fun providesFetchTransactionsUseCase(
      transactionRepository: TransactionRepositoryType): FetchTransactionsUseCase {
    return FetchTransactionsUseCase(transactionRepository)
  }

  @Singleton
  @Provides
  fun providesFindDefaultWalletUseCase(
      walletRepository: WalletRepositoryType): FindDefaultWalletUseCase {
    return FindDefaultWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesObserveDefaultWalletUseCase(
      walletRepository: WalletRepositoryType): ObserveDefaultWalletUseCase {
    return ObserveDefaultWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesDismissCardNotificationUseCase(findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                             preferences: SharedPreferences,
                                             autoUpdateRepository: AutoUpdateRepository,
                                             sharedPreferencesRepository: PreferencesRepositoryType,
                                             backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                                             promotionsRepo: PromotionsRepository): DismissCardNotificationUseCase {
    return DismissCardNotificationUseCase(findDefaultWalletUseCase,
        SharedPreferencesReferralLocalData(preferences), autoUpdateRepository,
        sharedPreferencesRepository, backupRestorePreferencesRepository, promotionsRepo)
  }

  @Singleton
  @Provides
  fun providesShouldShowFingerprintTooltipUseCase(
      preferencesRepositoryType: PreferencesRepositoryType, packageManager: PackageManager,
      fingerprintPreferences: FingerprintPreferencesRepositoryContract,
      biometricManager: BiometricManager): ShouldShowFingerprintTooltipUseCase {
    return ShouldShowFingerprintTooltipUseCase(preferencesRepositoryType, packageManager,
        fingerprintPreferences, biometricManager)
  }

  @Singleton
  @Provides
  fun providesSetSeenFingerprintTooltipUseCase(
      fingerprintPreferences: FingerprintPreferencesRepositoryContract): SetSeenFingerprintTooltipUseCase {
    return SetSeenFingerprintTooltipUseCase(fingerprintPreferences)
  }

  @Singleton
  @Provides
  fun providesGetLevelsUseCase(gamification: Gamification,
                               findDefaultWalletUseCase: FindDefaultWalletUseCase): GetLevelsUseCase {
    return GetLevelsUseCase(gamification, findDefaultWalletUseCase)
  }

  @Singleton
  @Provides
  fun providesGetUserLevelUseCase(gamification: Gamification,
                                  findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                  getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase): GetUserLevelUseCase {
    return GetUserLevelUseCase(gamification, findDefaultWalletUseCase, getCurrentPromoCodeUseCase)
  }

  @Singleton
  @Provides
  fun providesGetAppcBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                    balanceRepository: BalanceRepository): GetAppcBalanceUseCase {
    return GetAppcBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetEthBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                   balanceRepository: BalanceRepository): GetEthBalanceUseCase {
    return GetEthBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetCreditsBalanceUseCase(getCurrentWalletUseCase: GetCurrentWalletUseCase,
                                       balanceRepository: BalanceRepository): GetCreditsBalanceUseCase {
    return GetCreditsBalanceUseCase(getCurrentWalletUseCase, balanceRepository)
  }

  @Singleton
  @Provides
  fun providesGetCardNotificationsUseCase(referralInteractor: ReferralInteractorContract,
                                          autoUpdateInteract: AutoUpdateInteract,
                                          backupInteract: BackupInteractContract,
                                          promotionsInteractor: PromotionsInteractor): GetCardNotificationsUseCase {
    return GetCardNotificationsUseCase(referralInteractor, autoUpdateInteract, backupInteract,
        promotionsInteractor)
  }

  @Singleton
  @Provides
  fun providesRegisterSupportUserUseCase(
      supportRepository: SupportRepository): RegisterSupportUserUseCase {
    return RegisterSupportUserUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun provideGetUnreadConversationsCountEventsUseCase() = GetUnreadConversationsCountEventsUseCase()

  @Singleton
  @Provides
  fun providesDisplayChatUseCase(supportRepository: SupportRepository): DisplayChatUseCase {
    return DisplayChatUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun providesDisplayConversationListOrChatUseCase(
      supportRepository: SupportRepository): DisplayConversationListOrChatUseCase {
    return DisplayConversationListOrChatUseCase(supportRepository)
  }

  @Singleton
  @Provides
  fun providesSetSelectedCurrencyUseCase(
      fiatCurrenciesRepository: FiatCurrenciesRepository): SetSelectedCurrencyUseCase {
    return SetSelectedCurrencyUseCase(fiatCurrenciesRepository)
  }

  @Singleton
  @Provides
  fun providesGetChangeFiatCurrencyModelUseCase(fiatCurrenciesRepository: FiatCurrenciesRepository,
                                                conversionService: LocalCurrencyConversionService): GetChangeFiatCurrencyModelUseCase {
    return GetChangeFiatCurrencyModelUseCase(fiatCurrenciesRepository, conversionService)
  }

  @Singleton
  @Provides
  fun providesGetSelectedCurrencyUseCase(
      fiatCurrenciesRepository: FiatCurrenciesRepository): GetSelectedCurrencyUseCase {
    return GetSelectedCurrencyUseCase(fiatCurrenciesRepository)
  }

  @Singleton
  @Provides
  fun providesGetAvailableAmountToWithdrawUseCase(ewt: EwtAuthenticatorService,
                                                  withdrawRepository: WithdrawRepository): GetAvailableAmountToWithdrawUseCase {
    return GetAvailableAmountToWithdrawUseCase(ewt, withdrawRepository)
  }

  @Singleton
  @Provides
  fun providesGetStoredUserEmailUseCase(
      withdrawRepository: WithdrawRepository): GetStoredUserEmailUseCase {
    return GetStoredUserEmailUseCase(withdrawRepository)
  }

  @Singleton
  @Provides
  fun providesWithdrawToFiatUseCase(ewt: EwtAuthenticatorService,
                                    withdrawRepository: WithdrawRepository): WithdrawToFiatUseCase {
    return WithdrawToFiatUseCase(ewt, withdrawRepository)
  }

  @Singleton
  @Provides
  fun providesGetNftListUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                NFTRepository: NFTRepository): GetNFTListUseCase {
    return GetNFTListUseCase(getCurrentWallet, NFTRepository)
  }

  @Singleton
  @Provides
  fun providesEstimateNFTSendGasUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                        NFTRepository: NFTRepository): EstimateNFTSendGasUseCase {
    return EstimateNFTSendGasUseCase(getCurrentWallet, NFTRepository)
  }

  @Singleton
  @Provides
  fun providesSendNFTUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                             NFTRepository: NFTRepository, passwordStore: PasswordStore,
                             context: Context): SendNFTUseCase {
    val file = File(context.filesDir, "keystore/keystore")
    val keyStore = KeyStoreFileManager(file.absolutePath, ObjectMapper())
    return SendNFTUseCase(getCurrentWallet, NFTRepository, keyStore, passwordStore)
  }

  @Singleton
  @Provides
  fun providesGetSendLogsStateUseCase(sendLogsRepository: SendLogsRepository,
                                      getCurrentWalletUseCase: GetCurrentWalletUseCase): ObserveSendLogsStateUseCase {
    return ObserveSendLogsStateUseCase(sendLogsRepository, getCurrentWalletUseCase)
  }

  @Singleton
  @Provides
  fun providesResetSendLogsStateUseCase(sendLogsRepository: SendLogsRepository): ResetSendLogsStateUseCase {
    return ResetSendLogsStateUseCase(sendLogsRepository)
  }

  @Singleton
  @Provides
  fun providesSendLogsUseCase(sendLogsRepository: SendLogsRepository,
                              ewtObtainer: EwtAuthenticatorService): SendLogsUseCase {
    return SendLogsUseCase(sendLogsRepository, ewtObtainer)
  }

  @Singleton
  @Provides
  fun providesGetVerificationInfoUseCase(walletService: WalletService,
                                         verificationRepository: VerificationRepository,
                                         adyenPaymentInteractor: AdyenPaymentInteractor,
                                         rxSchedulers: RxSchedulers): GetVerificationInfoUseCase {
    return GetVerificationInfoUseCase(walletService, verificationRepository, adyenPaymentInteractor,
        rxSchedulers)
  }

  @Singleton
  @Provides
  fun providesMakeVerificationPaymentUseCase(walletService: WalletService,
                                             verificationRepository: VerificationRepository): MakeVerificationPaymentUseCase {
    return MakeVerificationPaymentUseCase(verificationRepository, walletService)
  }

  @Singleton
  @Provides
  fun providesSetCachedVerificationUseCase(walletService: WalletService,
                                           verificationRepository: VerificationRepository): SetCachedVerificationUseCase {
    return SetCachedVerificationUseCase(walletService, verificationRepository)
  }

  @Singleton
  @Provides
  fun providesSetPromoCodeUseCase(promoCodeRepository: PromoCodeRepository): SetPromoCodeUseCase {
    return SetPromoCodeUseCase(promoCodeRepository)
  }

  @Singleton
  @Provides
  fun providesGetCurrentPromoCodeUseCase(
      promoCodeRepository: PromoCodeRepository): GetCurrentPromoCodeUseCase {
    return GetCurrentPromoCodeUseCase(promoCodeRepository)
  }

  @Singleton
  @Provides
  fun providesObserveCurrentPromoCodeUseCase(
      promoCodeRepository: PromoCodeRepository): ObserveCurrentPromoCodeUseCase {
    return ObserveCurrentPromoCodeUseCase(promoCodeRepository)
  }

  @Singleton
  @Provides
  fun providesDeletePromoCodeUseCase(
      promoCodeRepository: PromoCodeRepository): DeletePromoCodeUseCase {
    return DeletePromoCodeUseCase(promoCodeRepository)
  }
}