package com.asfoundation.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract;
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

  private static final int AUTHENTICATION_REQUEST_CODE = 33;
  @Inject FingerprintPreferencesRepositoryContract fingerprintPreferences;
  @Inject PreferencesRepositoryType preferencesRepositoryType;
  @Inject AutoUpdateInteract autoUpdateInteract;
  private SplashPresenter presenter;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    presenter = new SplashPresenter(this, fingerprintPreferences, AndroidSchedulers.mainThread(),
        Schedulers.io(), new CompositeDisposable(), autoUpdateInteract);

    presenter.present(savedInstanceState);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SplashActivity.AUTHENTICATION_REQUEST_CODE) {
      if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
        firstScreenNavigation();
      } else {
        finish();
      }
    }
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

  @Override public void showAuthenticationActivity() {
    Intent intent = AuthenticationPromptActivity.newIntent(this);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE);
  }

  private boolean shouldShowOnboarding() {
    return !preferencesRepositoryType.hasCompletedOnboarding();
  }

  @Override protected void onDestroy() {
    presenter.stop();
    super.onDestroy();
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    presenter.onSaveInstance(outState);
  }
}

