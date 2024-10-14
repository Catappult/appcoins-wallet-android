package com.asfoundation.wallet.router;

import android.app.Activity;
import android.content.Intent;
import com.appcoins.wallet.core.utils.jvm_common.C;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.ui.GasSettingsActivity;
import com.asfoundation.wallet.viewmodel.GasSettingsViewModel;
import javax.inject.Inject;

public class GasSettingsRouter {

  public @Inject GasSettingsRouter() {
  }

  public void open(Activity context, GasSettings gasSettings) {
    Intent intent = new Intent(context, GasSettingsActivity.class);
    intent.putExtra(C.EXTRA_GAS_PRICE, gasSettings.gasPrice.toString());
    intent.putExtra(C.EXTRA_GAS_LIMIT, gasSettings.gasLimit.toString());
    context.startActivityForResult(intent, GasSettingsViewModel.SET_GAS_SETTINGS);
  }
}
