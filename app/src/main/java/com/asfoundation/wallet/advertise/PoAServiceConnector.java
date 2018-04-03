package com.asfoundation.wallet.advertise;

/**
 * Created by Joao Raimundo on 28/03/2018.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class PoAServiceConnector implements ServiceConnector {

  private static final String TAG = "PoAServiceConnector";

  /** Messenger for sending messages to the service. */
  private Messenger serviceMessenger = null;
  /** Messenger for receiving messages from the service. */
  private Messenger clientMessenger = null;

  /**
   * Target we publish for clients to send messages to IncomingHandler. Note
   * that calls to its binder are sequential!
   */
  private final IncomingHandler handler;

  /**
   * Handler thread to avoid running on the main thread (UI)
   */
  private final HandlerThread handlerThread;

  /** Flag indicating whether we have called bind on the service. */
  private boolean isBound;

  /**
   * Handler of incoming messages from service.
   */
  private class IncomingHandler extends Handler {

    ArrayList<MessageListener> listeners;

    public IncomingHandler(HandlerThread thr, @NotNull ArrayList<MessageListener> listeners) {
      super(thr.getLooper());
      this.listeners = listeners;
    }

    @Override public void handleMessage(Message msg) {
      Log.d(TAG, "Message received: " + msg.what);
      for (MessageListener listener : listeners) {
        listener.handle(msg);
      }
    }
  }

  /**
   * Class for interacting with the main interface of the service.
   */
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      // This is called when the connection with the service has been
      // established, giving us the object we can use to
      // interact with the service. We are communicating with the
      // service using a Messenger, so here we get a client-side
      // representation of that from the raw IBinder object.
      serviceMessenger = new Messenger(service);
      isBound = true;
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      serviceMessenger = null;
      isBound = false;
    }
  };

  public PoAServiceConnector(ArrayList<MessageListener> listeners) {
    handlerThread = new HandlerThread("HandlerThread");
    handlerThread.start();
    handler = new IncomingHandler(handlerThread, listeners != null? listeners : new ArrayList<>());
    clientMessenger = new Messenger(handler);
  }

  /**
   * Method used for binding with the service
   */

  @Override public boolean connectToService(Context context) {
    // Note that this is an implicit Intent that must be defined in the Android Manifest.
    SharedPreferences preferences =
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    String packageName = preferences.getString(PREFERENCE_SDK_PCKG_NAME, null);
    Intent i = new Intent(ACTION_BIND);
    i.setPackage(packageName);

    boolean result = context.getApplicationContext()
        .bindService(i, mConnection, Context.BIND_AUTO_CREATE);

    if (!result) {
      context.getApplicationContext().unbindService(mConnection);
    }
    return result;
  }

  @Override public void disconnectFromService(Context context) {
    if (isBound) {
      context.getApplicationContext()
          .unbindService(mConnection);
      isBound = false;
    }
  }

  @Override public void sendMessage(Context context, int type, Bundle bundle) {
    // validate if the service is bound
    if (!isBound) {
      return;
    }

    // Create and send a message to the service, using a supported 'what'
    // value
    Message msg = Message.obtain(null, type, 0, 0);
    msg.setData(bundle);
    msg.replyTo = clientMessenger;

    try {
      serviceMessenger.send(msg);
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to send message: " + e.getMessage(), e);
    }
  }
}

