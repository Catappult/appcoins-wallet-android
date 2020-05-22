package com.asfoundation.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.router.OnboardingRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

  @Inject PreferencesRepositoryType preferencesRepositoryType;
  @Inject AutoUpdateInteract autoUpdateInteract;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    handleAutoUpdate();
  }

  private void handleAutoUpdate() {
    autoUpdateInteract.getAutoUpdateModel(true)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(updateModel -> {
          if (autoUpdateInteract.isHardUpdateRequired(updateModel.getBlackList(),
              updateModel.getUpdateVersionCode(), updateModel.getUpdateMinSdk())) {
            navigateToAutoUpdate();
          } else {
            firstScreenNavigation();
          }
        })
        .subscribe();
  }

  private void navigateToAutoUpdate() {
    Intent intent = UpdateRequiredActivity.newIntent(this);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    finish();
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
    return !preferencesRepositoryType.hasCompletedOnboarding();
  }
}
