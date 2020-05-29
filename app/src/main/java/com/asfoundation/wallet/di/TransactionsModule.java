package com.asfoundation.wallet.di;

import com.asfoundation.wallet.backup.BackupInteract;
import com.asfoundation.wallet.backup.BackupInteractContract;
import com.asfoundation.wallet.billing.analytics.WalletEventSender;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.interact.CardNotificationsInteractor;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchTransactionsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.TransactionViewInteract;
import com.asfoundation.wallet.navigator.TransactionViewNavigator;
import com.asfoundation.wallet.navigator.UpdateNavigator;
import com.asfoundation.wallet.promotions.PromotionsInteractorContract;
import com.asfoundation.wallet.referrals.ReferralInteractorContract;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.TokenRepository;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.router.AirdropRouter;
import com.asfoundation.wallet.router.BalanceRouter;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.MyAddressRouter;
import com.asfoundation.wallet.router.RewardsLevelRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.SettingsRouter;
import com.asfoundation.wallet.router.TopUpRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.balance.BalanceInteract;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module class TransactionsModule {

  @Provides TransactionsViewModelFactory provideTransactionsViewModelFactory(
      AppcoinsApps applications, TransactionsAnalytics analytics,
      TransactionViewNavigator transactionViewNavigator,
      TransactionViewInteract transactionViewInteract, WalletEventSender walletEventSender,
      SupportInteractor supportInteractor,
      CurrencyFormatUtils formatter) {
    return new TransactionsViewModelFactory(applications, analytics, transactionViewNavigator,
        transactionViewInteract, walletEventSender, supportInteractor, formatter);
  }

  @Provides TransactionViewNavigator provideTransactionsViewNavigator(SettingsRouter settingsRouter,
      SendRouter sendRouter, TransactionDetailRouter transactionDetailRouter,
      MyAddressRouter myAddressRouter, BalanceRouter balanceRouter,
      ExternalBrowserRouter externalBrowserRouter, TopUpRouter topUpRouter,
      UpdateNavigator updateNavigator) {
    return new TransactionViewNavigator(settingsRouter, sendRouter, transactionDetailRouter,
        myAddressRouter, balanceRouter, externalBrowserRouter, topUpRouter, updateNavigator);
  }

  @Provides TransactionViewInteract provideTransactionsViewInteract(
      FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      FetchTransactionsInteract fetchTransactionsInteract,
      GamificationInteractor gamificationInteractor, BalanceInteract balanceInteract,
      PromotionsInteractorContract promotionsInteractorContract,
      CardNotificationsInteractor cardNotificationsInteractor,
      AutoUpdateInteract autoUpdateInteract) {
    return new TransactionViewInteract(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, gamificationInteractor, balanceInteract,
        promotionsInteractorContract, cardNotificationsInteractor, autoUpdateInteract);
  }

  @Provides FetchTransactionsInteract provideFetchTransactionsInteract(
      TransactionRepositoryType transactionRepository) {
    return new FetchTransactionsInteract(transactionRepository);
  }

  @Provides BackupInteractContract provideBackupInteractor(
      PreferencesRepositoryType sharedPreferences, GamificationInteractor gamificationInteractor,
      FetchTransactionsInteract fetchTransactionsInteract, BalanceInteract balanceInteract,
      FindDefaultWalletInteract findDefaultWalletInteract) {
    return new BackupInteract(sharedPreferences, fetchTransactionsInteract, balanceInteract,
        gamificationInteractor, findDefaultWalletInteract);
  }

  @Provides CardNotificationsInteractor provideCardNotificationInteractor(
      ReferralInteractorContract referralInteractor, AutoUpdateInteract autoUpdateInteract,
      BackupInteractContract backupInteract) {
    return new CardNotificationsInteractor(referralInteractor, autoUpdateInteract, backupInteract);
  }

  @Provides SettingsRouter provideSettingsRouter() {
    return new SettingsRouter();
  }

  @Provides SendRouter provideSendRouter() {
    return new SendRouter();
  }

  @Provides TopUpRouter provideSendRouterTopUpRouter() {
    return new TopUpRouter();
  }

  @Provides TransactionDetailRouter provideTransactionDetailRouter() {
    return new TransactionDetailRouter();
  }

  @Provides MyAddressRouter provideMyAddressRouter() {
    return new MyAddressRouter();
  }

  @Provides BalanceRouter provideMyTokensRouter() {
    return new BalanceRouter();
  }

  @Provides ExternalBrowserRouter provideExternalBrowserRouter() {
    return new ExternalBrowserRouter();
  }

  @Singleton @Provides TokenRepository provideTokenRepository(
      DefaultTokenProvider defaultTokenProvider, WalletRepositoryType walletRepositoryType) {
    return new TokenRepository(defaultTokenProvider, walletRepositoryType);
  }

  @Provides AirdropRouter provideAirdropRouter() {
    return new AirdropRouter();
  }

  @Provides RewardsLevelRouter providerRewardsLevelRouter() {
    return new RewardsLevelRouter();
  }
}
