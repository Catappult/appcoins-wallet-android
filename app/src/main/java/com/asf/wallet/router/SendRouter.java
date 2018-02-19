package com.asf.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asf.wallet.entity.TokenInfo;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.ui.SendActivity;

import static com.asf.wallet.C.EXTRA_TRANSACTION_BUILDER;

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
