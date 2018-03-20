package com.asfoundation.wallet;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;
import com.asfoundation.wallet.di.DaggerAppComponent;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.TransactionService;
import com.asfoundation.wallet.token.Erc20Token;
import com.crashlytics.android.Crashlytics;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import javax.inject.Inject;

public class App extends MultiDexApplication implements HasActivityInjector {

  @Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
  @Inject TransactionService transactionService;
  @Inject EthereumNetworkRepositoryType ethereumNetworkRepository;
  @Inject AddTokenInteract addTokenInteract;

  @Override public void onCreate() {
    super.onCreate();
    Realm.init(this);
    DaggerAppComponent.builder()
        .application(this)
        .build()
        .inject(this);
    setupRxJava();

    transactionService.start();

    ethereumNetworkRepository.addOnChangeDefaultNetwork(
        networkInfo -> addTokenInteract.add(Erc20Token.APPC.getAddress(),
            Erc20Token.APPC.getSymbol(), Erc20Token.APPC.getDecimals())
            .subscribe());

    // enable pin code for the application
    //		LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
    //		lockManager.enableAppLock(this, CustomPinActivity.class);
    //		lockManager.getAppLock().setShouldShowForgot(false);
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
    return dispatchingAndroidInjector;
  }
}
