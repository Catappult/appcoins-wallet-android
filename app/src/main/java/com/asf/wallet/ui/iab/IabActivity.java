package com.asf.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import com.asf.wallet.R;
import com.asf.wallet.repository.TransactionService;
import com.asf.wallet.ui.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabActivity extends BaseActivity implements IabView {

  @Inject TransactionService transactionService;
  private Button buyButton;
  private IabPresenter presenter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.iab_activity);
    buyButton = findViewById(R.id.buy_button);
    presenter = new IabPresenter(this, transactionService);
  }

  @Override protected void onPause() {
    presenter.stop();
    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    presenter.present();
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
}
