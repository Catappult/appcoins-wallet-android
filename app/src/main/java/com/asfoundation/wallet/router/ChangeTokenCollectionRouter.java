package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.TokenChangeCollectionActivity;

import static com.asfoundation.wallet.C.Key.WALLET;

public class ChangeTokenCollectionRouter {

  public void open(Context context, Wallet wallet) {
    if (wallet == null) {
      return;
    }
    Intent intent = new Intent(context, TokenChangeCollectionActivity.class);
    intent.putExtra(WALLET, wallet);
    context.startActivity(intent);
  }
}
