package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.BaseActivity;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabActivity extends BaseActivity implements IabView {
  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  public static final String TRANSACTION_HASH = "transaction_hash";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private boolean isBackEnable;
  private IabPresenter presenter;
  private Bundle savedInstanceState;

  public static Intent newIntent(Activity activity, Intent previousIntent) {
    Intent intent = new Intent(activity, IabActivity.class);
    intent.setData(previousIntent.getData());
    if (previousIntent.getExtras() != null) {
      intent.putExtras(previousIntent.getExtras());
    }
    intent.putExtra(APP_PACKAGE, activity.getCallingPackage());
    return intent;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_iab);
    this.savedInstanceState = savedInstanceState;
    isBackEnable = true;
    presenter = new IabPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
        new CompositeDisposable());
  }

  @Override public void onBackPressed() {
    if (isBackEnable) {
      super.onBackPressed();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    presenter.present(getIntent().getData()
        .toString(), getAppPackage(), getIntent().getExtras()
        .getString(PRODUCT_NAME));
  }

  @Override protected void onStop() {
    presenter.stop();
    super.onStop();
  }

  @Override public void finish(String hash) {
    Intent intent = new Intent();
    intent.putExtra(TRANSACTION_HASH, hash);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override public void close() {
    setResult(Activity.RESULT_CANCELED, null);
    finish();
  }

  @Override
  public void setup(TransactionBuilder transactionBuilder, Boolean canBuy, String uriString) {
    if (savedInstanceState == null) {
      if (true) {
        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container,
                RegularBuyFragment.newInstance(getIntent().getExtras(), uriString))
            .commit();
      } else {
        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container,
                ExpressCheckoutBuyFragment.newInstance(getIntent().getExtras(), uriString))
            .commit();
      }
    }

  }

  public String getAppPackage() {
    if (getIntent().hasExtra(APP_PACKAGE)) {
      return getIntent().getStringExtra(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }
}
