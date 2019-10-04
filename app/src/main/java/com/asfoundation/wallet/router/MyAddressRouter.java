package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.MyAddressActivity;

import static com.asfoundation.wallet.C.Key.WALLET;

public class MyAddressRouter {

  public void open(Context context, Wallet wallet) {
    if (wallet == null) {
      return;
    }
    Intent intent = new Intent(context, MyAddressActivity.class);
    intent.putExtra(WALLET, wallet);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
