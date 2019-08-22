package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.TransactionViewInteract;
import com.asfoundation.wallet.navigator.TransactionViewNavigator;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.ui.AppcoinsApps;

public class TransactionsViewModelFactory implements ViewModelProvider.Factory {

  private final DefaultTokenProvider defaultTokenProvider;
  private final AppcoinsApps applications;
  private final TransactionsAnalytics analytics;
  private final TransactionViewNavigator transactionViewNavigator;
  private final TransactionViewInteract transactionViewInteract;

  public TransactionsViewModelFactory(DefaultTokenProvider defaultTokenProvider,
      AppcoinsApps applications, TransactionsAnalytics analytics,
      TransactionViewNavigator transactionViewNavigator,
      TransactionViewInteract transactionViewInteract) {
    this.defaultTokenProvider = defaultTokenProvider;
    this.applications = applications;
    this.analytics = analytics;
    this.transactionViewNavigator = transactionViewNavigator;
    this.transactionViewInteract = transactionViewInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionsViewModel(defaultTokenProvider, applications, analytics,
        transactionViewNavigator, transactionViewInteract);
  }
}
