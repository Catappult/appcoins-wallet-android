package com.asfoundation.wallet.advertise;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import static com.asfoundation.wallet.advertise.ServiceConnector.ACTION_ACK_BROADCAST;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_REGISTER_CAMPAIGN;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_SEND_PROOF;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_PACKAGE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_SERVICE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_WALLET_PACKAGE_NAME;

/**
 * Created by Joao Raimundo on 29/03/2018.
 */

public class WalletPoAService extends Service {

  static final String TAG = WalletPoAService.class.getSimpleName();

  /**
   * Target we publish for clients to send messages to IncomingHandler.Note
   * that calls to its binder are sequential!
   */
  final Messenger serviceMessenger = new Messenger(new IncomingHandler());

  /** Boolean indicating that we are already bound */
  boolean isBound = false;

  /**
   * When binding to the service, we return an interface to our messenger for
   * sending messages to the service.
   */
  @Override public IBinder onBind(Intent intent) {
    isBound = true;
    return serviceMessenger.getBinder();
  }

  @Override public boolean onUnbind(Intent intent) {
    isBound = false;
    return super.onUnbind(intent);
  }

  @Override public void onCreate() {
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (!isBound && intent != null) {
      if (intent.hasExtra(PARAM_APP_PACKAGE_NAME)) {
        // send intent to confirm that we receive the broadcast and we want to finish the handshake
        String appPackageName = intent.getStringExtra(PARAM_APP_PACKAGE_NAME);
        String appServiceName = intent.getStringExtra(PARAM_APP_SERVICE_NAME);
        Log.e(TAG, "Received broadcast for handshake package name: "
            + appPackageName
            + " and service: "
            + appServiceName);

        // send explicit intent
        Intent i = new Intent(ACTION_ACK_BROADCAST);
        i.setComponent(new ComponentName(appPackageName, appServiceName));
        i.putExtra(PARAM_WALLET_PACKAGE_NAME, getPackageName());
        startService(i);
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    @Override public void handleMessage(Message msg) {
      Log.d(TAG, "Message received: ");
      switch (msg.what) {
        case MSG_REGISTER_CAMPAIGN:
          Log.d(TAG, "MSG_REGISTER_CAMPAIGN");
          break;
        case MSG_SEND_PROOF:
          Log.d(TAG, "MSG_SEND_PROOF");
          break;

        default:
          super.handleMessage(msg);
      }
    }
  }
}
