package com.asfoundation.wallet;

import android.app.Activity;
import android.app.Service;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import com.appcoins.wallet.billing.BillingDependenciesProvider;
import com.appcoins.wallet.billing.BillingFactory;
import com.appcoins.wallet.billing.ProxyService;
import com.appcoins.wallet.billing.WalletService;
import com.appcoins.wallet.billing.repository.RemoteRepository;
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.asfoundation.wallet.di.DaggerAppComponent;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class App extends MultiDexApplication
    implements HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector,
    BillingDependenciesProvider {

  private static final String TAG = App.class.getSimpleName();
  @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
  @Inject DispatchingAndroidInjector<Service> dispatchingServiceInjector;
  @Inject DispatchingAndroidInjector<Fragment> dispatchingFragmentInjector;
  @Inject EthereumNetworkRepositoryType ethereumNetworkRepository;
  @Inject AddTokenInteract addTokenInteract;
  @Inject DefaultTokenProvider defaultTokenProvider;
  @Inject ProofOfAttentionService proofOfAttentionService;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject AppcoinsOperationsDataSaver appcoinsOperationsDataSaver;
  @Inject RemoteRepository.BdsApi bdsApi;
  @Inject WalletService walletService;
  @Inject AppCoinsAddressProxySdk contractAddressProvider;
  @Inject BillingFactory billingFactory;
  @Inject ProxyService proxyService;
  @Inject Adyen adyen;

  @Override public void onCreate() {
    super.onCreate();
    Realm.init(this);
    DaggerAppComponent.builder()
        .application(this)
        .build()
        .inject(this);
    setupRxJava();

    Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG)
        .build())
        .build());

    inAppPurchaseInteractor.start();
    proofOfAttentionService.start();
    appcoinsOperationsDataSaver.start();
    ethereumNetworkRepository.addOnChangeDefaultNetwork(
        networkInfo -> defaultTokenProvider.getDefaultToken()
            .flatMapCompletable(
                defaultToken -> addTokenInteract.add(defaultToken.address, defaultToken.symbol,
                    defaultToken.decimals))
            .doOnError(throwable -> {
              if (!(throwable instanceof WalletNotFoundException)) {
                throwable.printStackTrace();
              }
            })
            .retry()
            .subscribe());

  }

  public Adyen getAdyen() {
    if (adyen == null) {
      adyen = new Adyen(this, Charset.forName("UTF-8"), Schedulers.io(), PublishRelay.create());
    }
    return adyen;
  }

  private void setupRxJava() {
    RxJavaPlugins.setErrorHandler(throwable -> {
      if (throwable instanceof UndeliverableException) {
        Crashlytics crashlytics = Crashlytics.getInstance();
        if (crashlytics != null && crashlytics.getFabric()
            .isDebuggable()) {
          Crashlytics.logException(throwable);
        } else {
          throwable.printStackTrace();
        }
      } else {
        throw new RuntimeException(throwable);
      }
    });
  }

  @Override public AndroidInjector<Activity> activityInjector() {
    return dispatchingActivityInjector;
  }

  @Override public AndroidInjector<Service> serviceInjector() {
    return dispatchingServiceInjector;
  }

  @Override public AndroidInjector<Fragment> supportFragmentInjector() {
    return dispatchingFragmentInjector;
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

  @NotNull @Override public BillingFactory getBillingFactory() {
    return billingFactory;
  }
}
