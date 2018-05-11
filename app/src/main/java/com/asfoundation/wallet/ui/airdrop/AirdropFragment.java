package com.asfoundation.wallet.ui.airdrop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.asf.wallet.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;

public class AirdropFragment extends DaggerFragment implements AirdropView {
  private static final String TAG = AirdropFragment.class.getSimpleName();
  @Inject AirdropInteractor airdropInteractor;
  private ImageView captchaView;
  private AirdropPresenter presenter;
  private Button requestButton;
  private EditText captchaAnswerView;
  private View refreshCaptchaButton;
  private ProgressDialog loading;
  private AlertDialog genericErrorDialog;
  private AlertDialog errorDialog;

  public static AirdropFragment newInstance() {
    return new AirdropFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter = new AirdropPresenter(this, new CompositeDisposable(), airdropInteractor,
        AndroidSchedulers.mainThread());
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.airdrop_fragment_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    captchaView = view.findViewById(R.id.airdrop_fragment_captcha_image_view);
    requestButton = view.findViewById(R.id.airdrop_fragment_request_airdrop_button);
    refreshCaptchaButton = view.findViewById(R.id.airdrop_fragment_request_captcha_button);
    captchaAnswerView = view.findViewById(R.id.airdrop_fragment_captcha_answer);
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
    return RxView.clicks(requestButton)
        .map(__ -> captchaAnswerView.getText()
            .toString());
  }

  @Override public Observable<Object> getCaptchaRefreshListener() {
    return RxView.clicks(refreshCaptchaButton);
  }

  @Override public void showLoading() {
    loading = ProgressDialog.show(getContext(), "Loading", "", true, false);
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
    genericErrorDialog = new AlertDialog.Builder(getContext()).setTitle("Airdrop")
        .setMessage("An error has occurred")
        .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
        .create();
    genericErrorDialog.show();
  }

  @Override public void showError(String message) {
    Log.d(TAG, "showError() called with: message = [" + message + "]");
    errorDialog = new AlertDialog.Builder(getContext()).setTitle("Airdrop")
        .setMessage(message)
        .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
        .create();
    errorDialog.show();
  }

  @Override public void showSuccess() {
    Log.d(TAG, "showSuccess() called");
    AlertDialog successDialog = new AlertDialog.Builder(getContext()).setTitle("Airdrop")
        .setMessage("Airdrop completed")
        .setPositiveButton("ok", (dialog, which) -> {
          dialog.dismiss();
          getFragmentManager().popBackStack();
        })
        .create();
    successDialog.show();
  }
}
