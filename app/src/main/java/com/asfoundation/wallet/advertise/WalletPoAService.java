package com.asfoundation.wallet.advertise;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import androidx.annotation.IntRange;
import androidx.core.app.NotificationCompat;
import com.asf.wallet.R;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.billing.analytics.PoaAnalytics;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofStatus;
import com.asfoundation.wallet.poa.ProofSubmissionFeeData;
import com.asfoundation.wallet.repository.WrongNetworkException;
import com.asfoundation.wallet.ui.TransactionsActivity;
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
  @Inject PoaAnalytics analytics;
  @Inject PoaAnalyticsController analyticsController;
  private Disposable disposable;
  private NotificationManager notificationManager;
  private Disposable timerDisposable;
  private Disposable requirementsDisposable;
  private Disposable startedEventDisposable;
  private Disposable completedEventDisposable;

  @Override public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    AndroidInjection.inject(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.hasExtra(PARAM_APP_PACKAGE_NAME)) {
      startNotifications();
      handlePoaStartToSendEvent();
      handlePoaCompletedToSendEvent();
      if (!isBound) {
        // set the chain id received from the application. If not received, it is set as the main
        String packageName = intent.getStringExtra(PARAM_APP_PACKAGE_NAME);
        requirementsDisposable =
            proofOfAttentionService.isWalletReady(intent.getIntExtra(PARAM_NETWORK_ID, -1))
                // network chain id
                .doOnSuccess(requirementsStatus -> proofOfAttentionService.setChainId(packageName,
                    intent.getIntExtra(PARAM_NETWORK_ID, -1)))
                .doOnSuccess(
                    proofSubmissionFeeData -> proofOfAttentionService.setGasSettings(packageName,
                        proofSubmissionFeeData.getGasPrice(), proofSubmissionFeeData.getGasLimit()))
                .doOnSuccess(
                    requirementsStatus -> processWalletState(requirementsStatus.getStatus(),
                        intent))
                .subscribe(requirementsStatus -> {
                }, throwable -> {
                  logger.log(throwable);
                  showGenericErrorNotificationAndStopForeground();
                });
      }
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
    stopTimeout();
  }

  private void processWalletState(ProofSubmissionFeeData.RequirementsStatus requirementsStatus,
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
      case WRONG_NETWORK:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_wrong_network_poa).build());
        stopForeground(false);
        stopTimeout();
        logger.log(new Throwable(new WrongNetworkException("Not on the correct network")));
        break;
      case UNKNOWN_NETWORK:
        logger.log(new Throwable(new WrongNetworkException("Unknown network")));
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
        Intent intent = TransactionsActivity.newIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notificationManager.notify(SERVICE_ID,
            createHeadsUpNotificationBuilder(R.string.notification_completed_poa).setContentIntent(
                pendingIntent)
                .build());
        break;
      case NO_INTERNET:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_no_network_poa).build());
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
      case NOT_AVAILABLE:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_not_available_poa).build());
        break;
      case NOT_AVAILABLE_ON_COUNTRY:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            R.string.notification_not_available_for_country_poa).build());
        break;
      case ALREADY_REWARDED:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_already_rewarded_poa).build());
        break;
      case INVALID_DATA:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(R.string.notification_submit_error_poa).build());
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

  private NotificationCompat.Builder createHeadsUpNotificationBuilder(int notificationText) {
    NotificationCompat.Builder builder;
    String channelId = "notification_channel_heads_up_id";
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      CharSequence channelName = "Notification channel";
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel notificationChannel =
          new NotificationChannel(channelId, channelName, importance);
      builder = new NotificationCompat.Builder(this, channelId);

      NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(notificationChannel);
    } else {
      builder = new NotificationCompat.Builder(this, channelId);
      builder.setVibrate(new long[0]);
    }

    return builder.setContentTitle(getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentText(getString(notificationText));
  }

  private NotificationCompat.Builder createDefaultNotificationBuilder(int notificationText) {
    NotificationCompat.Builder builder;
    String channelId = "notification_channel_id";
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      CharSequence channelName = "Notification channel";
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel notificationChannel =
          new NotificationChannel(channelId, channelName, importance);
      builder = new NotificationCompat.Builder(this, channelId);

      NotificationManager notificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(notificationChannel);
    } else {
      builder = new NotificationCompat.Builder(this, channelId);
    }

    return builder.setContentTitle(getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText(getString(notificationText));
  }

  public void setTimeout(String packageName) {
    disposeDisposable(timerDisposable);
    timerDisposable = Observable.timer(3, TimeUnit.MINUTES)
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

  public void handlePoaStartToSendEvent() {
    if (startedEventDisposable == null || startedEventDisposable.isDisposed()) {
      startedEventDisposable = proofOfAttentionService.get()
          .flatMap(proofs -> Observable.fromIterable(proofs)
              .filter(this::shouldSendStartEvent))
          .doOnNext(proof -> {
            analyticsController.setStartedEventSentFor(proof.getPackageName());
            analytics.sendPoaStartedEvent(proof.getPackageName(), proof.getCampaignId(),
                Integer.toString(proof.getChainId()));
          })
          .flatMapSingle(proof -> proofOfAttentionService.get()
              .firstOrError())
          .filter(List::isEmpty)
          .take(1)
          .subscribe();
    }
  }

  private boolean shouldSendStartEvent(Proof proof) {
    return !analyticsController.wasStartedEventSent(proof.getPackageName())
        && proof.getCampaignId() != null
        && !proof.getCampaignId()
        .isEmpty()
        && !proof.getProofComponentList()
        .isEmpty()
        && proof.getChainId() > 0;
  }

  public void handlePoaCompletedToSendEvent() {
    if (completedEventDisposable == null || completedEventDisposable.isDisposed()) {
      completedEventDisposable = proofOfAttentionService.get()
          .flatMapIterable(proofs -> proofs)
          .filter(proof -> proof.getProofStatus()
              .isTerminate())
          .doOnNext(proof -> {
            analyticsController.cleanStateFor(proof.getPackageName());
            if (proof.getProofStatus()
                .equals(ProofStatus.COMPLETED)) {
              analytics.sendPoaCompletedEvent(proof.getPackageName(), proof.getCampaignId(),
                  Integer.toString(proof.getChainId()));
            }
          })
          .flatMapSingle(proof -> proofOfAttentionService.get()
              .firstOrError())
          .filter(List::isEmpty)
          .take(1)
          .subscribe();
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
          proofOfAttentionService.setOemAddress(packageName);
          proofOfAttentionService.setStoreAddress(packageName);
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
          Log.d(TAG, "Ignoring MSG_STOP_PROCESS message.");
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}