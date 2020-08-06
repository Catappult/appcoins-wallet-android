package com.asfoundation.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.router.OnboardingRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class SplashActivity extends BaseActivity implements SplashView {

  @Inject PreferencesRepositoryType preferencesRepositoryType;
  @Inject AutoUpdateInteract autoUpdateInteract;

  private static final int AUTHENTICATION_REQUEST_CODE = 15;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

      SplashPresenter presenter =
          new SplashPresenter(this, preferencesRepositoryType, AndroidSchedulers.mainThread(), Schedulers.io(),
              new CompositeDisposable(), autoUpdateInteract);

      presenter.present();

  }

  @Override public void navigateToAutoUpdate() {
    Intent intent = UpdateRequiredActivity.newIntent(this);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    finish();
  }

  @Override public void firstScreenNavigation() {
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

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override public void showAuthenticationActivity() {
    Intent intent = AuthenticationPromptActivity.newIntent(this);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SplashActivity.AUTHENTICATION_REQUEST_CODE) {
      Log.d("TAG123", "CHEGOU 1");
      if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
        Log.d("TAG123", "CHEGOU 2");
        firstScreenNavigation();
      }
      if (resultCode == AuthenticationPromptActivity.RESULT_CANCELED) {
        Log.d("TAG123", "CHEGOU 3");
        finish();
      }
    }
  }
}

