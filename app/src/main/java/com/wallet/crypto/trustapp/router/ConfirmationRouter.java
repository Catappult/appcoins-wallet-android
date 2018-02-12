package com.wallet.crypto.trustapp.router;

import android.content.Context;
import android.content.Intent;
import com.wallet.crypto.trustapp.entity.TransactionBuilder;
import com.wallet.crypto.trustapp.ui.ConfirmationActivity;

import static com.wallet.crypto.trustapp.C.EXTRA_TRANSACTION_BUILDER;

public class ConfirmationRouter {
  public void open(Context context, TransactionBuilder transactionBuilder) {
    Intent intent = new Intent(context, ConfirmationActivity.class);
    intent.putExtra(EXTRA_TRANSACTION_BUILDER, transactionBuilder);
    context.startActivity(intent);
  }
}