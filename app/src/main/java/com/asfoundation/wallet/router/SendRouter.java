package com.asfoundation.wallet.router;

import android.content.Context;
import com.asfoundation.wallet.ui.transact.TransferActivity;

public class SendRouter {

  public void open(Context context) {
    context.startActivity(TransferActivity.newIntent(context));
  }
}
