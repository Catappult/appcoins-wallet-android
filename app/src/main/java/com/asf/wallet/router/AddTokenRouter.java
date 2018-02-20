package com.asf.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asf.wallet.ui.AddTokenActivity;

public class AddTokenRouter {

  public void open(Context context) {
    Intent intent = new Intent(context, AddTokenActivity.class);
    context.startActivity(intent);
  }
}
