package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.topup.TopUpActivity;

public class TopUpRouter {

  public void open(Context context) {
    Intent intent = TopUpActivity.newIntent(context);
    context.startActivity(intent);
  }
}
