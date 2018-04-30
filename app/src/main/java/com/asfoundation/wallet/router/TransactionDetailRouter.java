package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.TransactionDetailActivity;

import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailRouter {

  public void open(Context context, RawTransaction transaction) {
    Intent intent = new Intent(context, TransactionDetailActivity.class);
    intent.putExtra(TRANSACTION, transaction);
    context.startActivity(intent);
  }
}
