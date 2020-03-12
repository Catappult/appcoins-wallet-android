package com.asfoundation.wallet;

import android.text.TextUtils;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.bdsbilling.ProxyService;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary;
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.billing.BillingDependenciesProvider;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.di.AppComponent;
import com.asfoundation.wallet.di.DaggerAppComponent;
import com.asfoundation.wallet.identification.IdsRepository;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.support.SupportNotificationWorker;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.flurry.android.FlurryAgent;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.fabric.sdk.android.Fabric;
import io.intercom.android.sdk.Intercom;
import io.rakam.api.Rakam;
import io.rakam.api.RakamClient;
import io.rakam.api.TrackingOptions;
import io.reactivex.Single;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class App extends MultiDexApplication
    implements HasAndroidInjector, BillingDependenciesProvider {

  private static final String TAG = App.class.getName();
  @Inject DispatchingAndroidInjector<Object> androidInjector;
  @Inject ProofOfAttentionService proofOfAttentionService;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject AppcoinsOperationsDataSaver appcoinsOperationsDataSaver;
  @Inject RemoteRepository.BdsApi bdsApi;
  @Inject WalletService walletService;
  @Inject ProxyService proxyService;
  @Inject AppcoinsRewards appcoinsRewards;
  @Inject BillingMessagesMapper billingMessagesMapper;
  @Inject BdsApiSecondary bdsapiSecondary;
  @Inject IdsRepository idsRepository;

  @Override public void onCreate() {
    super.onCreate();
    AppComponent appComponent = DaggerAppComponent.builder()
        .application(this)
        .build();
    appComponent.inject(this);
    setupRxJava();
    setupWorkManager(appComponent);
    setupSupportNotificationWorker();

    if (!BuildConfig.DEBUG) {
      new FlurryAgent.Builder().withLogEnabled(false)
          .build(this, BuildConfig.FLURRY_APK_KEY);
    }

    Fabric.with(this, new Crashlytics.Builder().core(
        new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG)
            .build())
        .build());

    inAppPurchaseInteractor.start();
    proofOfAttentionService.start();
    appcoinsOperationsDataSaver.start();
    appcoinsRewards.start();

    Intercom.initialize(this, BuildConfig.INTERCOM_API_KEY, BuildConfig.INTERCOM_APP_ID);
    Intercom.client()
        .setInAppMessageVisibility(Intercom.Visibility.GONE);
    initializeRakam();
  }

  private void setupRxJava() {
    RxJavaPlugins.setErrorHandler(throwable -> {
      if (throwable instanceof UndeliverableException) {
        if (BuildConfig.DEBUG) {
          throwable.printStackTrace();
        } else {
          FlurryAgent.onError("ID", throwable.getMessage(), throwable);
        }
      } else {
        throw new RuntimeException(throwable);
      }
    });
  }

  private void setupWorkManager(AppComponent appComponent) {
    WorkManager.initialize(this,
        new Configuration.Builder().setWorkerFactory(appComponent.daggerWorkerFactory())
            .build());
  }

  private void setupSupportNotificationWorker() {
    Constraints workerConstraints =
        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
    PeriodicWorkRequest notificationWorkRequest =
        new PeriodicWorkRequest.Builder(SupportNotificationWorker.class,
            SupportNotificationWorker.NOTIFICATION_PERIOD, TimeUnit.MINUTES).addTag(
            SupportNotificationWorker.WORKER_TAG)
            .setConstraints(workerConstraints)
            .build();
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(SupportNotificationWorker.UNIQUE_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP, notificationWorkRequest);
  }

  private void initializeRakam() {
    Single.just(idsRepository.getAndroidId())
        .flatMap(this::startRakam)
        .flatMap(rakamClient -> idsRepository.getInstallerPackage(BuildConfig.APPLICATION_ID)
            .flatMap(installerPackage -> Single.just(idsRepository.getGamificationLevel())
                .flatMap(level -> Single.just(idsRepository.getActiveWalletAddress())
                    .doOnSuccess(
                        walletAddress -> setRakamSuperProperties(rakamClient, installerPackage,
                            level, walletAddress)))))
        .subscribeOn(Schedulers.newThread())
        .subscribe();
  }

  private Single<RakamClient> startRakam(String deviceId) {
    RakamClient instance = Rakam.getInstance();
    TrackingOptions options = new TrackingOptions();
    options.disableAdid();
    try {
      instance.initialize(this, new URL(BuildConfig.RAKAM_BASE_HOST), BuildConfig.RAKAM_API_KEY);
    } catch (MalformedURLException e) {
      Log.e(TAG, "error: ", e);
    }
    instance.setTrackingOptions(options);
    instance.setDeviceId(deviceId);
    instance.trackSessionEvents(true);
    instance.setLogLevel(Log.VERBOSE);
    instance.setEventUploadPeriodMillis(1);
    instance.enableLogging(true);

    return Single.just(instance);
  }

  private void setRakamSuperProperties(RakamClient instance, String installerPackage, int userLevel,
      String userId) {
    if (TextUtils.isEmpty(installerPackage)) installerPackage = "other";

    JSONObject superProperties = instance.getSuperProperties();
    if (superProperties == null) {
      superProperties = new JSONObject();
    }
    try {
      superProperties.put("aptoide_package", BuildConfig.APPLICATION_ID);
      superProperties.put("version_code", BuildConfig.VERSION_CODE);
      superProperties.put("entry_point", installerPackage);
      superProperties.put("user_level", userLevel);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    instance.setSuperProperties(superProperties);
    if (!userId.isEmpty()) instance.setUserId(userId);
    instance.enableForegroundTracking(this);
  }

  @Override public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }

  @Override public int getSupportedVersion() {
    return BuildConfig.BILLING_SUPPORTED_VERSION;
  }

  @NotNull @Override public RemoteRepository.BdsApi getBdsApi() {
    return bdsApi;
  }

  @NotNull @Override public WalletService getWalletService() {
    return walletService;
  }

  @NotNull @Override public ProxyService getProxyService() {
    return proxyService;
  }

  @NotNull @Override public BillingMessagesMapper getBillingMessagesMapper() {
    return billingMessagesMapper;
  }

  @NotNull @Override public BdsApiSecondary getBdsApiSecondary() {
    return bdsapiSecondary;
  }
}
