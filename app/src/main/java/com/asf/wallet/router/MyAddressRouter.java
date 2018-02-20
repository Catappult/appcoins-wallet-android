package com.asf.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.ui.MyAddressActivity;

import static com.asf.wallet.C.Key.WALLET;

public class MyAddressRouter {

  public void open(Context context, Wallet wallet) {
    Intent intent = new Intent(context, MyAddressActivity.class);
    intent.putExtra(WALLET, wallet);
    context.startActivity(intent);
  }
}
