package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchTransactionsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.repository.OffChainTransactions;
import com.asfoundation.wallet.router.AirdropRouter;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.MyAddressRouter;
import com.asfoundation.wallet.router.MyTokensRouter;
import com.asfoundation.wallet.router.RewardsLevelRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.SettingsRouter;
import com.asfoundation.wallet.router.TopUpRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.LocalCurrencyConversionService;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;

public class TransactionsViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchTransactionsInteract fetchTransactionsInteract;
  private final SettingsRouter settingsRouter;
  private final SendRouter sendRouter;
  private final TransactionDetailRouter transactionDetailRouter;
  private final MyAddressRouter myAddressRouter;
  private final MyTokensRouter myTokensRouter;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final DefaultTokenProvider defaultTokenProvider;
  private final GetDefaultWalletBalance getDefaultWalletBalance;
  private final TransactionsMapper transactionsMapper;
  private final AirdropRouter airdropRouter;
  private final AppcoinsApps applications;
  private final OffChainTransactions offChainTransactions;
  private final RewardsLevelRouter rewardsLevelRouter;
  private final GamificationInteractor gamificationInteractor;
  private final TopUpRouter topUpRouter;
  private final TransactionsAnalytics analytics;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public TransactionsViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      FetchTransactionsInteract fetchTransactionsInteract, SettingsRouter settingsRouter,
      SendRouter sendRouter, TransactionDetailRouter transactionDetailRouter,
      MyAddressRouter myAddressRouter, MyTokensRouter myTokensRouter,
      ExternalBrowserRouter externalBrowserRouter, DefaultTokenProvider defaultTokenProvider,
      GetDefaultWalletBalance getDefaultWalletBalance, TransactionsMapper transactionsMapper,
      AirdropRouter airdropRouter, AppcoinsApps applications,
      OffChainTransactions offChainTransactions, RewardsLevelRouter rewardsLevelRouter,
      GamificationInteractor gamificationInteractor, TopUpRouter topUpRouter,
      TransactionsAnalytics analytics,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchTransactionsInteract = fetchTransactionsInteract;
    this.settingsRouter = settingsRouter;
    this.sendRouter = sendRouter;
    this.transactionDetailRouter = transactionDetailRouter;
    this.myAddressRouter = myAddressRouter;
    this.myTokensRouter = myTokensRouter;
    this.externalBrowserRouter = externalBrowserRouter;
    this.defaultTokenProvider = defaultTokenProvider;
    this.getDefaultWalletBalance = getDefaultWalletBalance;
    this.transactionsMapper = transactionsMapper;
    this.airdropRouter = airdropRouter;
    this.applications = applications;
    this.offChainTransactions = offChainTransactions;
    this.rewardsLevelRouter = rewardsLevelRouter;
    this.gamificationInteractor = gamificationInteractor;
    this.topUpRouter = topUpRouter;
    this.analytics = analytics;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionsViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, settingsRouter, sendRouter, transactionDetailRouter,
        myAddressRouter, myTokensRouter, externalBrowserRouter, defaultTokenProvider,
        getDefaultWalletBalance, transactionsMapper, airdropRouter, applications,
        offChainTransactions, rewardsLevelRouter, gamificationInteractor, topUpRouter, analytics,
        localCurrencyConversionService);
  }
}
