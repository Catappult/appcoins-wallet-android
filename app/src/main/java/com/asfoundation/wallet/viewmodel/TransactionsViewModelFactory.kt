package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.TransactionViewInteract
import com.asfoundation.wallet.navigator.TransactionViewNavigator
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.util.CurrencyFormatUtils

class TransactionsViewModelFactory(private val applications: AppcoinsApps,
                                   private val analytics: TransactionsAnalytics,
                                   private val transactionViewNavigator: TransactionViewNavigator,
                                   private val transactionViewInteract: TransactionViewInteract,
                                   private val walletsEventSender: WalletsEventSender,
                                   private val supportRepository: SupportRepository,
                                   private val formatter: CurrencyFormatUtils) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransactionsViewModel(applications, analytics, transactionViewNavigator,
        transactionViewInteract, supportRepository, walletsEventSender, formatter) as T
  }
}