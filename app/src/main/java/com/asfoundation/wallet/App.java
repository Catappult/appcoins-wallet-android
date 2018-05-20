package com.asfoundation.wallet;

import android.app.Activity;
import android.app.Service;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import com.asf.wallet.BuildConfig;
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
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import javax.inject.Inject;

public class App extends MultiDexApplication
    implements HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector {

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

  @Override public void onCreate() {
    super.onCreate();
    Realm.init(this);
    DaggerAppComponent.builder()
        .application(this)
        .build()
        .inject(this);
    setupRxJava();

    Fabric.with(this, new Crashlytics.Builder().core(
        new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG)
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
}
