package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.TokensActivity;

import static com.asfoundation.wallet.C.Key.WALLET;

public class MyTokensRouter {

  public void open(Context context, Wallet wallet) {
    if (wallet == null) {
      return;
    }
    Intent intent = new Intent(context, TokensActivity.class);
    intent.putExtra(WALLET, wallet);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
