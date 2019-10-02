package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.balance.TransactionDetailActivity;

import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailRouter {

  public void open(Context context, Transaction transaction) {
    Intent intent = new Intent(context, TransactionDetailActivity.class);
    intent.putExtra(TRANSACTION, transaction);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
