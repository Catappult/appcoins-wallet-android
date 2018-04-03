package com.asfoundation.wallet.advertise;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import static com.asfoundation.wallet.advertise.ServiceConnector.ACTION_ACK_BROADCAST;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_PACKAGE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_SERVICE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_WALLET_PACKAGE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PREFERENCE_SDK_PCKG_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.SHARED_PREFS;

/**
 * Created by Joao Raimundo on 29/03/2018.
 */

public class WalletPoAService extends Service implements MessageListener {

  static final String TAG = WalletPoAService.class.getSimpleName();

  /**
   * Target we publish for clients to send messages to IncomingHandler.Note
   * that calls to its binder are sequential!
   */
  final Messenger serviceMessenger = new Messenger(new IncomingHandler());

  boolean isBound = false;

  ServiceConnector serviceConnector;

  private String appPackageName;

  private String appServiceName;

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
    ArrayList<MessageListener> listeners = new ArrayList<>();
    listeners.add(this);
    serviceConnector = new PoAServiceConnector(listeners);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (!isBound && intent != null) {
      if (intent.hasExtra(PARAM_APP_PACKAGE_NAME)) {
        // Store the package name of the sdk that triggered the handshake for later use on
        // the PoA process
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_SDK_PCKG_NAME, intent.getStringExtra(PARAM_APP_PACKAGE_NAME));
        editor.commit();

        // send intent to confirm that we receive the broadcast and we want to finish the handshake
        appPackageName = intent.getStringExtra(PARAM_APP_PACKAGE_NAME);
        appServiceName = intent.getStringExtra(PARAM_APP_SERVICE_NAME);
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

  @Override public void handle(Message message) {
    switch (message.what) {
      case MSG_REGISTER_CAMPAIGN:
        Log.d(TAG, "MSG_REGISTER_CAMPAIGN");
        break;
      case MSG_SEND_PROOF:
        Log.d(TAG, "MSG_SEND_PROOF");
        break;
      case MSG_SIGN_PROOF:
        Log.d(TAG, "MSG_SIGN_PROOF");
        break;
    }
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
