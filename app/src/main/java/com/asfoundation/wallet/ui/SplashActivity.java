package com.asfoundation.wallet.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.asfoundation.wallet.interact.AutoUpdateInteract;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.router.OnboardingRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SplashActivity extends BaseActivity implements SplashView {

  @Inject PreferencesRepositoryType preferencesRepositoryType;
  @Inject AutoUpdateInteract autoUpdateInteract;
  @Inject FingerPrintInteract fingerprintInteract;

  private PublishSubject<FingerprintAuthResult> fingerprintResultSubject;

  private PublishSubject<Object> retryClickSubject;

  public static Intent newIntent(Context context) {
    return new Intent(context, SplashActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    fingerprintResultSubject = PublishSubject.create();
    retryClickSubject = PublishSubject.create();

    SplashPresenter presenter =
        new SplashPresenter(this, AndroidSchedulers.mainThread(), Schedulers.io(),
            new CompositeDisposable(), autoUpdateInteract, fingerprintInteract,
            preferencesRepositoryType);

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

  @NotNull @Override public BiometricPrompt createBiometricPrompt() {
    Executor executor = ContextCompat.getMainExecutor(this);

    return new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
      @Override public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        fingerprintResultSubject.onNext(
            new FingerprintAuthResult(errorCode, errString, null, FingerprintResult.ERROR));
        Toast.makeText(getApplicationContext(), "Authentication error: " + errString,
            Toast.LENGTH_SHORT)
            .show();
        Log.d("TAG123", "value: " + errorCode);
      }

      @Override
      public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        fingerprintResultSubject.onNext(
            new FingerprintAuthResult(null, null, result, FingerprintResult.SUCCESS));
        Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_LONG)
            .show();
      }

      @Override public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        fingerprintResultSubject.onNext(
            new FingerprintAuthResult(null, null, null, FingerprintResult.FAIL));
        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_LONG)
            .show();
      }
    });
  }

  private boolean shouldShowOnboarding() {
    return !preferencesRepositoryType.hasCompletedOnboarding();
  }

  @Override protected void onDestroy() {
    fingerprintResultSubject = null;
    retryClickSubject = null;
    super.onDestroy();
  }

  @NotNull @Override public Observable<FingerprintAuthResult> getAuthenticationResult() {
    return fingerprintResultSubject;
  }

  @NotNull @Override public Observable<Object> getRetryButtonClick() {
    return retryClickSubject;
  }

  @Override public void onRetryButtonClick() {
    retryClickSubject.onNext("");
  }

  @Override public void showBottomSheetDialogFragment(@Nullable CharSequence message) {
    AuthenticationErrorBottomSheetFragment bottomSheetFragment =
        new AuthenticationErrorBottomSheetFragment(message);
    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
  }

  @Override public void showPrompt(@NotNull BiometricPrompt biometricPrompt,
      BiometricPrompt.PromptInfo promptInfo) {

    biometricPrompt.authenticate(promptInfo);
  }

  @Override public void showFail() {

  }

  @RequiresApi(api = Build.VERSION_CODES.M) @Override public boolean checkBiometricSupport() {
    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    return keyguardManager.isDeviceSecure();
  }
}

