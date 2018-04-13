package com.asfoundation.wallet.advertise;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.asf.wallet.R;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofStatus;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

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

  public static final int SERVICE_ID = 77784;
  static final String TAG = WalletPoAService.class.getSimpleName();
  /**
   * Target we publish for clients to send messages to IncomingHandler.Note
   * that calls to its binder are sequential!
   */
  final Messenger serviceMessenger = new Messenger(new IncomingHandler());

  /** Boolean indicating that we are already bound */
  boolean isBound = false;

  @Inject ProofOfAttentionService proofOfAttentionService;
  private Disposable disposable;
  private NotificationManager notificationManager;

  @Override public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    AndroidInjection.inject(this);
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
   * When binding to the service, we return an interface to our messenger for
   * sending messages to the service.
   */
  @Override public IBinder onBind(Intent intent) {
    isBound = true;
    startForeground(SERVICE_ID, createNotification(R.string.notification_ongoing_poa));
    disposable = proofOfAttentionService.get()
        .flatMapIterable(proofs -> proofs)
        .distinctUntilChanged(Proof::getProofStatus)
        .subscribe(proof -> updateNotification(proof.getProofStatus()));
    return serviceMessenger.getBinder();
  }

  @Override public boolean onUnbind(Intent intent) {
    isBound = false;
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
    stopForeground(true);
    return super.onUnbind(intent);
  }

  private void updateNotification(ProofStatus status) {
    @StringRes int notificationText;
    switch (status) {
      case SUBMITTING:
        notificationText = R.string.notification_submitting_poa;
        break;
      case COMPLETED:
        notificationText = R.string.notification_completed_poa;
        break;
      case NO_FUNDS:
        notificationText = R.string.notification_no_funds_poa;
        break;
      case NO_INTERNET:
        notificationText = R.string.notification_no_internet_poa;
        break;
      case GENERAL_ERROR:
        notificationText = R.string.notification_error_poa;
        break;
      case NO_WALLET:
        notificationText = R.string.notification_no_wallet_poa;
        break;
      default:
      case PROCESSING:
        notificationText = R.string.notification_ongoing_poa;
        break;
    }

    notificationManager.notify(SERVICE_ID, createNotification(notificationText));
    if (status.equals(ProofStatus.COMPLETED) || status.isError()) {
      stopForeground(false);
    }
  }

  private Notification createNotification(int notificationText) {
    NotificationCompat.Builder builder;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      String channelId = "notification_channel_id";
      CharSequence channelName = "Notification channel";
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel notificationChannel =
          new NotificationChannel(channelId, channelName, importance);
      builder = new NotificationCompat.Builder(this, channelId);

      NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(notificationChannel);
    } else {
      builder = new NotificationCompat.Builder(this);
    }

    return builder.setContentTitle(getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText(getString(notificationText))
        .build();
  }

  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    @Override public void handleMessage(Message msg) {
      Log.d(TAG, "handleMessage() called with: msg = [" + msg + "]");
      switch (msg.what) {
        case MSG_REGISTER_CAMPAIGN:
          Log.d(TAG, "MSG_REGISTER_CAMPAIGN");
          proofOfAttentionService.setCampaignId(msg.getData()
              .getString("packageName"), msg.getData()
              .getString("campaignId"))
              .subscribeOn(Schedulers.computation())
              .subscribe();
          break;
        case MSG_SEND_PROOF:
          Log.d(TAG, "MSG_SEND_PROOF");
          proofOfAttentionService.registerProof(msg.getData()
              .getString("packageName"), msg.getData()
              .getLong("timeStamp"))
              .subscribeOn(Schedulers.computation())
              .subscribe();
          break;

        default:
          super.handleMessage(msg);
      }
    }
  }
}
