package com.asf.wallet.router;

import android.app.Activity;
import android.content.Intent;
import com.asf.wallet.C;
import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.ui.GasSettingsActivity;
import com.asf.wallet.viewmodel.GasSettingsViewModel;

public class GasSettingsRouter {
  public void open(Activity context, GasSettings gasSettings) {
    Intent intent = new Intent(context, GasSettingsActivity.class);
    intent.putExtra(C.EXTRA_GAS_PRICE, gasSettings.gasPrice.toString());
    intent.putExtra(C.EXTRA_GAS_LIMIT, gasSettings.gasLimit.toString());
    context.startActivityForResult(intent, GasSettingsViewModel.SET_GAS_SETTINGS);
  }
}
