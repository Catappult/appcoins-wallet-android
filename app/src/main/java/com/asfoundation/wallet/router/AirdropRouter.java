package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.TokensActivity;
import com.asfoundation.wallet.ui.widget.AirDropActivity;

import static com.asfoundation.wallet.C.Key.WALLET;

public class AirdropRouter {

  public void open(Context context, Wallet wallet) {
    Intent intent = new Intent(context, AirDropActivity.class);
    intent.putExtra(WALLET, wallet);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }
}
