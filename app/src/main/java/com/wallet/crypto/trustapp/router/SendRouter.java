package com.wallet.crypto.trustapp.router;

import android.content.Context;
import android.content.Intent;
import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.TransactionBuilder;
import com.wallet.crypto.trustapp.ui.SendActivity;

import static com.wallet.crypto.trustapp.C.EXTRA_TRANSACTION_BUILDER;

public class SendRouter {

  public void open(Context context, String symbol) {
    open(context, new TransactionBuilder(symbol));
  }

  public void open(Context context, TokenInfo tokenInfo) {
    open(context, new TransactionBuilder(tokenInfo));
  }

  public void open(Context context, TransactionBuilder transactionBuilder) {
    Intent intent = new Intent(context, SendActivity.class);
    intent.putExtra(EXTRA_TRANSACTION_BUILDER, transactionBuilder);
    context.startActivity(intent);
  }
}
