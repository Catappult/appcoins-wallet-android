package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.ui.airdrop.AirdropActivity;

public class AirdropRouter {

  public void open(Context context) {
    Intent intent = AirdropActivity.newIntent(context);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
