package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.ui.transact.TransferActivity;

public class SendRouter {

  public void open(Context context) {
    Intent intent = TransferActivity.newIntent(context);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
