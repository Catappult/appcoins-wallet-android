package com.asfoundation.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.router.OnboardingRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.viewmodel.SplashViewModel;
import com.asfoundation.wallet.viewmodel.SplashViewModelFactory;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

  @Inject SplashViewModelFactory splashViewModelFactory;
  @Inject PreferenceRepositoryType preferenceRepositoryType;
  @Inject AutoUpdateInteract autoUpdateInteract;
  SplashViewModel splashViewModel;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    splashViewModel = ViewModelProviders.of(this, splashViewModelFactory)
        .get(SplashViewModel.class);
    handleAutoUpdate();
  }

  private void handleAutoUpdate() {
    autoUpdateInteract.getAutoUpdateModel()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(updateModel -> {
          if (autoUpdateInteract.isHardUpdateRequired(updateModel.getBlackList())) {
            navigateToAutoUpdate();
          } else {
            firstScreenNavigation();
          }
        })
        .subscribe();
  }

  private void navigateToAutoUpdate() {
    Intent intent = new Intent(this, UpdateRequiredActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

  private void firstScreenNavigation() {
    if (shouldShowOnboarding()) {
      new OnboardingRouter().open(this, true);
    } else {
      new TransactionsRouter().open(this, true);
    }
    finish();
  }

  private boolean shouldShowOnboarding() {
    return !preferenceRepositoryType.hasCompletedOnboarding();
  }
}
