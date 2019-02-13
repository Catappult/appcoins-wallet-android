package com.asfoundation.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.router.ManageWalletsRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.viewmodel.SplashViewModel;
import com.asfoundation.wallet.viewmodel.SplashViewModelFactory;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

  @Inject SplashViewModelFactory splashViewModelFactory;
  SplashViewModel splashViewModel;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

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
    finish();
  }
}
