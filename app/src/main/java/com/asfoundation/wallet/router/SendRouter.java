package com.asfoundation.wallet.router;

import android.content.Context;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.transact.TransactActivity;

public class SendRouter {

  public void open(Context context, TokenInfo tokenInfo) {
    open(context, new TransactionBuilder(tokenInfo));
  }

  public void open(Context context, TransactionBuilder transactionBuilder) {
    //Intent intent = new Intent(context, SendActivity.class);
    //intent.putExtra(EXTRA_TRANSACTION_BUILDER, transactionBuilder);
    context.startActivity(TransactActivity.newIntent(context));
  }
}
