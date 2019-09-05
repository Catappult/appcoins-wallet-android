package com.asfoundation.wallet.router;

import android.content.Context;
import android.content.Intent;
import com.asfoundation.wallet.ui.WalletsActivity;

public class ManageWalletsRouter {

  public void open(Context context, boolean isClearStack) {
    Intent intent = new Intent(context, WalletsActivity.class);
    if (isClearStack) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    context.startActivity(intent);
  }
}
