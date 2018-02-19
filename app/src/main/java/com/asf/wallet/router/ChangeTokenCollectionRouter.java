package com.asf.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.ui.TokenChangeCollectionActivity;

import static com.asf.wallet.C.Key.WALLET;

public class ChangeTokenCollectionRouter {

  public void open(Context context, Wallet wallet) {
    Intent intent = new Intent(context, TokenChangeCollectionActivity.class);
    intent.putExtra(WALLET, wallet);
    context.startActivity(intent);
  }
}
