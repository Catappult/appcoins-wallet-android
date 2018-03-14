package com.asf.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.repository.TransactionService;
import com.asf.wallet.ui.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabActivity extends BaseActivity implements IabView {

  @Inject TransactionService transactionService;
  private Button buyButton;
  private IabPresenter presenter;
  private View loadingView;
  private TextView appName;
  private TextView itemDescription;
  private TextView itemPrice;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.iab_activity);
    buyButton = findViewById(R.id.buy_button);
    loadingView = findViewById(R.id.loading);
    appName = findViewById(R.id.iab_activity_app_name);
    itemDescription = findViewById(R.id.iab_activity_item_description);
    itemPrice = findViewById(R.id.iab_activity_item_price);
    presenter = new IabPresenter(this, transactionService, AndroidSchedulers.mainThread());
  }

  @Override protected void onPause() {
    presenter.stop();
    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    presenter.present(getIntent().getData()
        .toString());
  }

  @Override public Observable<String> getBuyClick() {
    return RxView.clicks(buyButton)
        .map(click -> getIntent().getData()
            .toString());
  }

  @Override public void finish(String hash) {
    Intent intent = new Intent();
    intent.putExtra("transaction_hash", hash);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override public void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.requestFocus();
    loadingView.setOnTouchListener((v, event) -> true);
  }

  @Override public void showError() {
    Snackbar.make(loadingView, "Error", Snackbar.LENGTH_LONG)
        .show();
    buyButton.setText(R.string.iab_activity_retry_button_text);
  }

  @Override public void lockOrientation() {
    int currentOrientation = getResources().getConfiguration().orientation;
    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }
  }

  @Override public void unlockOrientation() {
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  @Override public void setup(TransactionBuilder transactionBuilder) {
    Formatter formatter = new Formatter();
    itemPrice.setText(formatter.format(Locale.getDefault(), "%(,.2f", transactionBuilder.amount()
        .doubleValue())
        .toString());
  }
}
