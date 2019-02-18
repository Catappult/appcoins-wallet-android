package com.asfoundation.wallet.ui.airdrop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.asf.wallet.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import javax.inject.Inject;

public class AirdropFragment extends DaggerFragment implements AirdropView {
  private static final String TAG = AirdropFragment.class.getSimpleName();
  @Inject AirdropInteractor airdropInteractor;
  private ImageView captchaView;
  private AirdropPresenter presenter;
  private Button submitButton;
  private TextInputEditText captchaAnswerView;
  private View refreshButton;
  private AlertDialog loading;
  private AlertDialog genericErrorDialog;
  private AlertDialog errorDialog;
  private BehaviorSubject<Object> terminateStateConsumed;
  private AirdropBack airdropBack;

  public static AirdropFragment newInstance() {
    return new AirdropFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter = new AirdropPresenter(this, new CompositeDisposable(), airdropInteractor,
        AndroidSchedulers.mainThread());
    terminateStateConsumed = BehaviorSubject.create();
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_airdrop, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    captchaView = view.findViewById(R.id.captcha_img);
    submitButton = view.findViewById(R.id.submit_btn);
    refreshButton = view.findViewById(R.id.refresh_btn);
    captchaAnswerView = view.findViewById(R.id.answer_text);
    captchaAnswerView.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!submitButton.isEnabled() && s.length() > 0) {
          submitButton.setEnabled(true);
        } else if (submitButton.isEnabled() && s.length() == 0) {
          submitButton.setEnabled(false);
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });
    presenter.present();
  }

  @Override public void onDestroyView() {
    presenter.stop();
    dismissDialog(loading);
    loading = null;
    dismissDialog(genericErrorDialog);
    genericErrorDialog = null;
    dismissDialog(errorDialog);
    errorDialog = null;
    super.onDestroyView();
  }

  private void dismissDialog(Dialog dialog) {
    if (dialog != null) {
      dialog.dismiss();
    }
  }

  @Override public void showCaptcha(String captchaUrl) {
    Log.d(TAG, "showCaptcha() called with: captchaUrl = [" + captchaUrl + "]");
    Picasso.with(getContext())
        .invalidate(captchaUrl);
    Picasso.with(getContext())
        .load(captchaUrl)
        .into(captchaView);
  }

  @Override public Observable<String> getAirdropClick() {
    return RxView.clicks(submitButton)
        .map(__ -> captchaAnswerView.getText()
            .toString());
  }

  @Override public Observable<Object> getCaptchaRefreshListener() {
    return RxView.clicks(refreshButton);
  }

  @Override public void showLoading() {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
    LayoutInflater inflater = this.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.dialog_loading, null);
    dialogBuilder.setCancelable(false);
    dialogBuilder.setView(dialogView);
    loading = dialogBuilder.create();
    loading.getWindow()
        .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    loading.show();

    Log.d(TAG, "showLoading() called");
  }

  @Override public void hideLoading() {
    Log.d(TAG, "hideLoading() called");
    if (loading != null) {
      loading.dismiss();
    }
  }

  @Override public void showGenericError() {
    Log.d(TAG, "showGenericError() called");
    genericErrorDialog =
        new AlertDialog.Builder(getContext()).setTitle(R.string.activity_airdrop_message_title)
            .setMessage(R.string.activity_airdrop_generic_error_message)
            .setPositiveButton(R.string.activity_airdrop_ok, (dialog, which) -> dialog.dismiss())
            .setOnDismissListener(dialog -> terminateStateConsumed.onNext(true))
            .create();
    genericErrorDialog.show();
  }

  @Override public void showError(String message) {
    Log.d(TAG, "showError() called with: message = [" + message + "]");
    errorDialog =
        new AlertDialog.Builder(getContext()).setTitle(R.string.activity_airdrop_message_title)
            .setMessage(message)
            .setPositiveButton(R.string.activity_airdrop_ok, (dialog, which) -> dialog.dismiss())
            .setOnDismissListener(dialog -> terminateStateConsumed.onNext(true))
            .create();
    errorDialog.show();
  }

  @Override public void showSuccess() {
    Log.d(TAG, "showSuccess() called");
    AlertDialog successDialog =
        new AlertDialog.Builder(getContext()).setTitle(R.string.activity_airdrop_message_title)
            .setMessage(R.string.activity_airdrop_success_message)
            .setOnDismissListener(dialog -> terminateStateConsumed.onNext(true))
            .setPositiveButton(R.string.activity_airdrop_ok, (dialog, which) -> {
              dialog.dismiss();
              airdropBack.onAirdropFinish();
            })
            .create();
    successDialog.show();
  }

  @Override public Observable<Object> getTerminateStateConsumed() {
    return terminateStateConsumed;
  }

  @Override public void clearCaptchaText() {
    captchaAnswerView.setText("");
  }

  @Override public void showCaptchaError() {
    captchaAnswerView.setError(getString(R.string.activity_airdrop_wrong_captcha));
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof AirdropBack)) {
      throw new IllegalArgumentException(context.getClass()
          .getSimpleName() + " should implement " + AirdropBack.class.getSimpleName());
    }
    airdropBack = ((AirdropBack) context);
  }

  public interface AirdropBack {
    void onAirdropFinish();
  }
}
