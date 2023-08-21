package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.asfoundation.wallet.ui.MyAddressActivity;

import static com.appcoins.wallet.core.utils.jvm_common.C.Key.WALLET;

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
