package com.asf.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asf.wallet.entity.Transaction;
import com.asf.wallet.ui.TransactionDetailActivity;

import static com.asf.wallet.C.Key.TRANSACTION;

public class TransactionDetailRouter {

  public void open(Context context, Transaction transaction) {
    Intent intent = new Intent(context, TransactionDetailActivity.class);
    intent.putExtra(TRANSACTION, transaction);
    context.startActivity(intent);
  }
}
