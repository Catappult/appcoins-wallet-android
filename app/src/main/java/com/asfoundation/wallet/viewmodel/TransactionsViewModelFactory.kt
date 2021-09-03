package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.TransactionViewInteractor
import com.asfoundation.wallet.navigator.TransactionViewNavigator
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TransactionsViewModelFactory(private val applications: AppcoinsApps,
                                   private val analytics: TransactionsAnalytics,
                                   private val transactionViewNavigator: TransactionViewNavigator,
                                   private val transactionViewInteractor: TransactionViewInteractor,
                                   private val walletsEventSender: WalletsEventSender,
                                   private val supportInteractor: SupportInteractor,
                                   private val formatter: CurrencyFormatUtils) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TransactionsViewModel(applications, analytics, transactionViewNavigator,
        transactionViewInteractor, supportInteractor, walletsEventSender, formatter,
        AndroidSchedulers.mainThread(), Schedulers.io()) as T
  }
}