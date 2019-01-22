package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import com.asfoundation.wallet.router.TransactionsRouter;

public class MyAddressViewModel extends BaseViewModel {

  private final TransactionsRouter transactionsRouter;

  public MyAddressViewModel(TransactionsRouter transactionsRouter) {
    this.transactionsRouter = transactionsRouter;
  }

  public void showTransactions(Context context) {
    transactionsRouter.open(context, true);
  }
}
