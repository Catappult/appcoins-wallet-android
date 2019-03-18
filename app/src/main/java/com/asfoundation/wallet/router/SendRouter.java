package com.asfoundation.wallet.router;

import android.content.Context;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.transact.TransferActivity;

public class SendRouter {

  public void open(Context context, TokenInfo tokenInfo) {
    open(context, new TransactionBuilder(tokenInfo));
  }

  public void open(Context context, TransactionBuilder transactionBuilder) {
    context.startActivity(TransferActivity.newIntent(context));
  }
}
