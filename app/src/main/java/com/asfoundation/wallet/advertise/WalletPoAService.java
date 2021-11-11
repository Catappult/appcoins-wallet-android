package com.asfoundation.wallet.advertise;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import androidx.annotation.IntRange;
import androidx.core.app.NotificationCompat;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.PoaAnalytics;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.appcoins.wallet.commons.Logger;
import com.asfoundation.wallet.main.MainActivityNavigator;
import com.asfoundation.wallet.poa.PoaInformationModel;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofStatus;
import com.asfoundation.wallet.poa.ProofSubmissionData;
import com.asfoundation.wallet.repository.WrongNetworkException;
import com.asfoundation.wallet.util.Log;
import com.asfoundation.wallet.verification.VerificationBroadcastReceiver;
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
import static com.asfoundation.wallet.verification.VerificationBroadcastReceiver.ACTION_DISMISS;
import static com.asfoundation.wallet.verification.VerificationBroadcastReceiver.ACTION_KEY;
import static com.asfoundation.wallet.verification.VerificationBroadcastReceiver.ACTION_START_VERIFICATION;

/**
 * Created by Joao Raimundo on 29/03/2018.
 */

public class WalletPoAService extends Service {

  public static final int SERVICE_ID = 77784;
  public static final int VERIFICATION_SERVICE_ID = 77785;

  private static final String TAG = WalletPoAService.class.getSimpleName();
  /**
   * Target we publish for clients to send messages to IncomingHandler.Note
   * that calls to its binder are sequential!
   */
  final Messenger serviceMessenger = new Messenger(new IncomingHandler());
  @Inject ProofOfAttentionService proofOfAttentionService;
  @Inject @Named("MAX_NUMBER_PROOF_COMPONENTS") int maxNumberProofComponents;
  @Inject Logger logger;
  @Inject PoaAnalytics analytics;
  @Inject PoaAnalyticsController analyticsController;
  @Inject NotificationManager notificationManager;
  @Inject PackageManager packageManager;
  @Inject CampaignInteract campaignInteract;
  @Inject AutoUpdateInteract autoUpdateInteract;
  @Inject @Named("heads_up") NotificationCompat.Builder headsUpNotificationBuilder;
  @Inject public MainActivityNavigator mainActivityNavigator;
  /** Boolean indicating that we are already bound */
  private boolean isBound = false;
  private Disposable disposable;
  private Disposable timerDisposable;
  private Disposable requirementsDisposable;
  private Disposable startedEventDisposable;
  private Disposable completedEventDisposable;
  private String appName;

  @Override public void onCreate() {
    super.onCreate();
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
        try {
          ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
          appName = packageManager.getApplicationLabel(appInfo)
              .toString();
        } catch (PackageManager.NameNotFoundException e) {
          e.printStackTrace();
        }
        int versionCode = getVersionCode(packageName);

        requirementsDisposable = proofOfAttentionService.handleCreateWallet()
            .flatMap(__ -> proofOfAttentionService.isWalletReady(
                intent.getIntExtra(PARAM_NETWORK_ID, -1), packageName, versionCode)
                // network chain id
                .doOnSuccess(proof -> proofOfAttentionService.setChainId(packageName,
                    intent.getIntExtra(PARAM_NETWORK_ID, -1)))
                .doOnSuccess(proof -> processWalletState(proof, intent, packageName)))
            .subscribe(requirementsStatus -> {
            }, throwable -> {
              analytics.sendRakamProofEvent(packageName, "fail", throwable.toString());
              logger.log(TAG, throwable);
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
        createDefaultNotificationBuilder(getString(R.string.notification_generic_error)).build());
    stopForeground(false);
    stopTimeout();
  }

  private void processWalletState(ProofSubmissionData proof, Intent intent, String packageName) {
    switch (proof.getStatus()) {
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
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_no_funds_poa)).build());
        stopForeground(false);
        stopTimeout();
        break;
      case NO_WALLET:
        // Show notification mentioning that we have no wallet configured on the app
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_no_wallet_poa)).build());
        stopForeground(false);
        stopTimeout();
        break;
      case NO_NETWORK:
        // Show notification mentioning that we have no wallet configured on the app
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_no_network_poa)).build());
        stopForeground(false);
        stopTimeout();
        break;
      case NOT_ELIGIBLE:
        //No campaign or already rewarded so there is no need to notify the user of anything
        proofOfAttentionService.remove(packageName);
        if (proof.hasReachedPoaLimit()) {
          if (campaignInteract.hasSeenPoaNotificationTimePassed()) {
            showPoaLimitNotification(proof);
            campaignInteract.saveSeenPoaNotification();
            stopForeground(false);
          } else {
            stopForeground(true);
          }
        } else {
          campaignInteract.clearSeenPoaNotification();
          stopForeground(true);
        }
        stopTimeout();
        break;
      case WRONG_NETWORK:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_wrong_network_poa)).build());
        stopForeground(false);
        stopTimeout();
        logger.log(TAG, new Throwable(new WrongNetworkException("Not on the correct network")));
        break;
      case UPDATE_REQUIRED:
        if (autoUpdateInteract.shouldShowNotification()) {
          showUpdateRequiredNotification();
          autoUpdateInteract.saveSeenUpdateNotification();
        }
        stopForeground(false);
        stopTimeout();
        break;
      case UNKNOWN_NETWORK:
        logger.log(TAG, new Throwable(new WrongNetworkException("Unknown network")));
        break;
    }
  }

  private void showUpdateRequiredNotification() {
    Intent intent = autoUpdateInteract.buildUpdateIntent();
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    notificationManager.notify(SERVICE_ID, headsUpNotificationBuilder.setStyle(
        new NotificationCompat.BigTextStyle().setBigContentTitle(
            getString(R.string.update_wallet_poa_notification_title))
            .bigText(getString(R.string.update_wallet_poa_notification_body)))
        .setContentIntent(pendingIntent)
        .build());
  }

  private void showPoaLimitNotification(ProofSubmissionData proof) {
    String minutes = String.format("%02d", proof.getMinutesRemaining());
    String message = getString(R.string.notification_poa_limit_reached,
        String.valueOf(proof.getHoursRemaining()), minutes);
    NotificationCompat.Builder builder =
        headsUpNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
    if (appName != null) {
      builder.setContentTitle(appName)
          .setContentText(message);
    }
    notificationManager.notify(SERVICE_ID, builder.build());
  }

  private void stopTimeout() {
    disposeDisposable(timerDisposable);
  }

  private void startNotifications() {
    startForeground(SERVICE_ID,
        createDefaultNotificationBuilder(getString(R.string.notification_ongoing_poa)).build());
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
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_submitting_poa)).build());
        stopTimeout();
        break;
      case COMPLETED:
        PoaInformationModel poaInformation = proofOfAttentionService.retrievePoaInformation()
            .blockingGet();
        String completed = getString(R.string.verification_notification_reward_received_body);
        if (!poaInformation.hasRemainingPoa()) {
          completed = buildNoPoaRemainingString(poaInformation);
        }
        NotificationCompat.Builder notificationBuilder = headsUpNotificationBuilder.setStyle(
            new NotificationCompat.BigTextStyle().bigText(completed))
            .setContentText(completed)
            .setContentIntent(mainActivityNavigator.getHomePendingIntent());
        if (appName != null) {
          notificationBuilder.setContentTitle(appName);
        }
        notificationManager.notify(SERVICE_ID, notificationBuilder.build());
        campaignInteract.clearSeenPoaNotification();
        break;
      case NO_INTERNET:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_no_network_poa)).build());
        break;
      case GENERAL_ERROR:
        notificationManager.notify(SERVICE_ID,
            createDefaultNotificationBuilder(getString(R.string.notification_error_poa)).build());
        break;
      case NO_WALLET:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_no_wallet_poa)).build());
        break;
      case CANCELLED:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_cancelled_poa)).build());
        break;
      case NOT_AVAILABLE:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_not_available_poa)).build());
        break;
      case NOT_AVAILABLE_ON_COUNTRY:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_not_available_for_country_poa)).build());
        break;
      case ALREADY_REWARDED:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_already_rewarded_poa)).build());
        break;
      case INVALID_DATA:
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_submit_error_poa)).build());
        break;
      default:
      case PROCESSING:
        int progress = calculateProgress(proof);
        notificationManager.notify(SERVICE_ID, createDefaultNotificationBuilder(
            getString(R.string.notification_ongoing_poa)).setProgress(100, progress,
            progress == 0 || progress == 100)
            .build());
        break;
      case PHONE_NOT_VERIFIED:
        stopForeground(true);
        notificationManager.cancel(SERVICE_ID);
        notificationManager.notify(VERIFICATION_SERVICE_ID,
            createVerificationNotification().build());
        break;
    }

    if (proof.getProofStatus()
        .isTerminate()) {
      stopForeground(false);
      stopTimeout();
    }
  }

  private String buildNoPoaRemainingString(PoaInformationModel poaInformation) {
    String minutesRemaining = String.format("%02d", poaInformation.getRemainingMinutes());
    return getString(R.string.notification_completed_poa,
        String.valueOf(poaInformation.getRemainingHours()), minutesRemaining);
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

  private NotificationCompat.Builder createVerificationNotification() {
    NotificationCompat.Builder builder;
    String channelId = "notification_channel_verification_id";
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      CharSequence channelName = "Notification Verification Channel";
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

    Intent okIntent = VerificationBroadcastReceiver.newIntent(this);
    okIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    okIntent.putExtra(ACTION_KEY, ACTION_START_VERIFICATION);
    PendingIntent okPendingIntent = PendingIntent.getBroadcast(this, 0, okIntent, 0);

    Intent dismissIntent = VerificationBroadcastReceiver.newIntent(this);
    dismissIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    dismissIntent.putExtra(ACTION_KEY, ACTION_DISMISS);
    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 1, dismissIntent, 0);

    return builder.setContentTitle(
        getString(R.string.verification_notification_verification_needed_title))
        .setContentIntent(okPendingIntent)
        .setAutoCancel(true)
        .setOngoing(true)
        .addAction(0, getString(R.string.ok), okPendingIntent)
        .addAction(0, getString(R.string.dismiss_button), dismissPendingIntent)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentText(getString(R.string.verification_notification_verification_needed_body));
  }

  private NotificationCompat.Builder createDefaultNotificationBuilder(String notificationText) {
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

    if (appName != null) {
      builder.setContentTitle(appName);
    } else {
      builder.setContentTitle(getString(R.string.app_name));
    }
    return builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText(notificationText);
  }

  private void setTimeout(String packageName) {
    disposeDisposable(timerDisposable);
    timerDisposable = Observable.timer(3, TimeUnit.MINUTES)
        .subscribe(__ -> {
          disposeDisposable(requirementsDisposable);
          proofOfAttentionService.cancel(packageName);
        });
  }

  private void disposeDisposable(Disposable disposable) {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private void handlePoaStartToSendEvent() {
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

  private void handlePoaCompletedToSendEvent() {
    if (completedEventDisposable == null || completedEventDisposable.isDisposed()) {
      completedEventDisposable = proofOfAttentionService.get()
          .flatMapIterable(proofs -> proofs)
          .doOnNext(this::handlePoaCompletedAnalytics)
          .filter(proof -> proof.getProofStatus()
              .isTerminate())
          .doOnNext(proof -> analyticsController.cleanStateFor(proof.getPackageName()))
          .flatMapSingle(proof -> proofOfAttentionService.get()
              .firstOrError())
          .filter(List::isEmpty)
          .take(1)
          .subscribe();
    }
  }

  private void handlePoaCompletedAnalytics(Proof proof) {
    if (proof.getProofStatus()
        .equals(ProofStatus.PHONE_NOT_VERIFIED)) {
      analytics.sendRakamProofEvent(proof.getPackageName(), "fail", proof.getProofStatus()
          .name());
    } else if (proof.getProofStatus()
        .isTerminate()) {
      if (proof.getProofStatus()
          .equals(ProofStatus.COMPLETED)) {
        analytics.sendPoaCompletedEvent(proof.getPackageName(), proof.getCampaignId(),
            Integer.toString(proof.getChainId()));
        analytics.sendRakamProofEvent(proof.getPackageName(), "success", "");
      } else {
        analytics.sendRakamProofEvent(proof.getPackageName(), "fail", proof.getProofStatus()
            .name());
      }
    }
  }

  private int getVersionCode(String packageName) {
    PackageInfo packageInfo;
    int versionCode = -1;
    try {
      packageInfo = getPackageManager().getPackageInfo(packageName, 0);
      versionCode = packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      logger.log(TAG, new Throwable("Package not found exception"));
      e.printStackTrace();
    }
    return versionCode;
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