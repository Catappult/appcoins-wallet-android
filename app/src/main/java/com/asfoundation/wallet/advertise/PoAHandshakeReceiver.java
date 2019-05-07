package com.asfoundation.wallet.advertise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_PACKAGE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_SERVICE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_NETWORK_ID;

/**
 * Created by Joao Raimundo on 29/03/2018.
 */

/** Receiver for the handshake broadcast */
public class PoAHandshakeReceiver extends BroadcastReceiver {

  @Override public void onReceive(Context context, Intent intent) {
    Log.d("PoAHandshakeReceiver", "Broadcast received");

    Intent serviceIntent = new Intent(context, WalletPoAService.class);
    serviceIntent.putExtra(PARAM_APP_PACKAGE_NAME, intent.getStringExtra(PARAM_APP_PACKAGE_NAME));
    serviceIntent.putExtra(PARAM_APP_SERVICE_NAME, intent.getStringExtra(PARAM_APP_SERVICE_NAME));
    serviceIntent.putExtra(PARAM_NETWORK_ID, intent.getIntExtra(PARAM_NETWORK_ID, -1));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(serviceIntent);
    } else {
      context.startService(serviceIntent);
    }
  }
}
