package com.asfoundation.wallet.advertise;

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
import android.support.annotation.IntRange;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofSubmissionFeeData;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;

import static com.asfoundation.wallet.advertise.ServiceConnector.ACTION_ACK_BROADCAST;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_REGISTER_CAMPAIGN;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_SEND_PROOF;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_SET_NETWORK;
import static com.asfoundation.wallet.advertise.ServiceConnector.MSG_STOP_PROCESS;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_PACKAGE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_APP_SERVICE_NAME;
import static com.asfoundation.wallet.advertise.ServiceConnector.PARAM_NETWORK_ID;
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
  @Inject @Named("MAX_NUMBER_PROOF_COMPONENTS") int maxNumberProofComponents;
  @Inject Logger logger;
  private Disposable disposable;
  private NotificationManager notificationManager;
  private Disposable timerDisposable;
  private Disposable requirementsDisposable;

  @Override public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    AndroidInjection.inject(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    startNotifications();
    if (!isBound && intent != null) {
      if (intent.hasExtra(PARAM_APP_PACKAGE_NAME)) {
        // set the chain id received from the application. If not received, it is set as the main
        String packageName = intent.getStringExtra(PARAM_APP_PACKAGE_NAME);
        requirementsDisposable =
            proofOfAttentionService.isWalletReady(intent.getIntExtra(PARAM_NETWORK_ID, 1))
                // network chain id
                .doOnSuccess(requirementsStatus -> proofOfAttentionService.setChainId(packageName,
                    intent.getIntExtra(PARAM_NETWORK_ID, 1)))
                .doOnSuccess(
                    proofSubmissionFeeData -> proofOfAttentionService.setGasSettings(packageName,
                        proofSubmissionFeeData.getGasPrice(), proofSubmissionFeeData.getGasLimit()))
                .doOnSuccess(
                    requirementsStatus -> processWalletSate(requirementsStatus.getStatus(), intent))
                .subscribe(requirementsStatus -> {
                }, throwable -> {
                  logger.log(throwable);
                  showGenericErrorNotificationAndStopForeground();
                });
      }
    }
    if (intent != null && intent.hasExtra(PARAM_APP_PACKAGE_NAME)) {
      setTimeout(intent.getStringExtra(PARAM_APP_PACKAGE_NAME));
    }
    return super.onStartCommand(intent, flags, startId);
  }

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
    return true;
  }

  @Override public void onRebind(Intent intent) {
    isBound = true;
    super.onRebind(intent);
  }

  private void showGenericErrorNotificationAndStopForeground() {
    notificationManager.notify(SERVICE_ID,
        createDefaultNotificationBuilder(R.string.notification_generic_error).build());
    stopForeground(false);
  }

  private void processWalletSate(ProofSubmissionFeeData.RequirementsStatus requirementsStatus,
      Intent intent) {
    switch (requirementsStatus) {
      case READY:
        // send intent to confirm that we receive the broadcast and we want to finish the handshake
        String appPackageName = intent.getStringExtra(PARAM_APP_PACKAGE_NAME);
        String appServiceName = intent.getStringExtra(PARAM_APP_SERVICE_NAME);
        Log.d(TAG, "Received broadcast for handshake package name: "
            + appPackageName
            + " and service: "
            + appServiceName);
        // send explicit intent
        Intent i = new Intent(ACTION_ACK_BROADCAST);
        i.setComponent(new ComponentName(appPackageName, appServiceName));
        i.putExtra(PARAM_WALLET_PACKAGE_NAME, getPackageName());
        startService(i);
        break;
      case NO_FUNDS:
        // show notification mentioning that we have no fund to register the PoA
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_funds_poa).build());
        stopForeground(false);
        stopTimeout();
        break;
      case NO_WALLET:
        // Show notification mentioning that we have no wallet configured on the app
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_wallet_poa).build());
        stopForeground(false);
        stopTimeout();
        break;
      case NO_NETWORK:
        // Show notification mentioning that we have no wallet configured on the app
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_network_poa).build());
        stopForeground(false);
        stopTimeout();
        break;
    }
  }

  private void stopTimeout() {
    disposeDisposable(timerDisposable);
  }

  public void startNotifications() {
    startForeground(SERVICE_ID,
        createDefaultNotificationBuilder(R.string.notification_ongoing_poa).build());
    if (disposable == null || disposable.isDisposed()) {
      disposable = proofOfAttentionService.get()
          .flatMapIterable(proofs -> proofs)
          .doOnNext(this::updateNotification)
          .filter(proof -> proof.getProofStatus()
              .isTerminate())
          .doOnNext(proof -> proofOfAttentionService.remove(proof.getPackageName()))
          .flatMapSingle(proof -> proofOfAttentionService.get()
              .firstOrError())
          .filter(List::isEmpty)
          .take(1)
          .subscribe(proof -> {
          });
    }
  }

  private void updateNotification(Proof proof) {
    switch (proof.getProofStatus()) {
      case SUBMITTING:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_submitting_poa).build());
        break;
      case COMPLETED:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_completed_poa).build());
        break;
      case NO_FUNDS:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_funds_poa).build());
        break;
      case NO_INTERNET:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_internet_poa).build());
        break;
      case GENERAL_ERROR:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_error_poa).build());
        break;
      case NO_WALLET:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_wallet_poa).build());
        break;
      case CANCELLED:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_cancelled_poa).build());
        break;
      default:
      case PROCESSING:
        int progress = calculateProgress(proof);
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_ongoing_poa).setProgress(100,
                progress, progress == 0 || progress == 100)
                .build());
        break;
    }

    if (proof.getProofStatus()
        .isTerminate()) {
      stopForeground(false);
      stopTimeout();
    }
  }

  private @IntRange(from = 0, to = 100) int calculateProgress(Proof proof) {
    int progress = 0;
    progress += proof.getProofComponentList()
        .size();
    if (proof.getCampaignId() != null) {
      progress++;
    }
    if (proof.getStoreAddress() != null) {
      progress++;
    }
    if (proof.getOemAddress() != null) {
      progress++;
    }
    return progress * 100 / (maxNumberProofComponents + 3);
  }

  private NotificationCompat.Builder createDefaultNotificationBuilder(int notificationText) {
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
        .setContentText(getString(notificationText));
  }

  public void setTimeout(String packageName) {
    disposeDisposable(timerDisposable);
    timerDisposable = Observable.timer(30, TimeUnit.SECONDS)
        .subscribe(__ -> {
          disposeDisposable(requirementsDisposable);
          proofOfAttentionService.cancel(packageName);
        });
  }

  public void disposeDisposable(Disposable disposable) {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    @Override public void handleMessage(Message msg) {
      String packageName = msg.getData()
          .getString("packageName");
      setTimeout(packageName);
      Log.d(TAG, "handleMessage() called with: msg = [" + msg + "] " + "");
      switch (msg.what) {
        case MSG_REGISTER_CAMPAIGN:
          Log.d(TAG, "MSG_REGISTER_CAMPAIGN");
          proofOfAttentionService.setCampaignId(packageName, msg.getData()
              .getString("campaignId"));
          proofOfAttentionService.setOemAddress(packageName, BuildConfig.DEFAULT_OEM_ADREESS);
          proofOfAttentionService.setStoreAddress(packageName, BuildConfig.DEFAULT_STORE_ADREESS);
          break;
        case MSG_SEND_PROOF:
          Log.d(TAG, "MSG_SEND_PROOF");
          proofOfAttentionService.registerProof(packageName, msg.getData()
              .getLong("timeStamp"));
          break;
        case MSG_SET_NETWORK:
          Log.d(TAG, "MSG_SET_NETWORK");
          proofOfAttentionService.setChainId(packageName, msg.getData()
              .getInt("networkId"));
          break;
        case MSG_STOP_PROCESS:
          Log.d(TAG, "MSG_STOP_PROCESS");
          proofOfAttentionService.cancel(packageName);
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}
