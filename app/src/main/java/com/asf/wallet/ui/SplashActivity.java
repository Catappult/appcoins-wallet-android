package com.asf.wallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.router.ManageWalletsRouter;
import com.asf.wallet.router.TransactionsRouter;
import com.asf.wallet.viewmodel.SplashViewModel;
import com.asf.wallet.viewmodel.SplashViewModelFactory;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import dagger.android.AndroidInjection;
import io.fabric.sdk.android.Fabric;
import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

  @Inject SplashViewModelFactory splashViewModelFactory;
  SplashViewModel splashViewModel;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics.Builder().core(
        new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG)
            .build())
        .build());

    splashViewModel = ViewModelProviders.of(this, splashViewModelFactory)
        .get(SplashViewModel.class);
    splashViewModel.wallets()
        .observe(this, this::onWallets);
  }

  private void onWallets(Wallet[] wallets) {
    // Start home activity
    if (wallets.length == 0) {
      new ManageWalletsRouter().open(this, true);
    } else {
      new TransactionsRouter().open(this, true);
    }
  }
}
