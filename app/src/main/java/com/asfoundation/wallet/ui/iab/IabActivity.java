package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.BaseActivity;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import javax.inject.Inject;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabActivity extends BaseActivity implements IabView {
  public static final String SKU_DETAILS = "sku_details";
  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  public static final String EXTRA_DEVELOPER_PAYLOAD = "developer_payload";
  public static final String TRANSACTION_DATA = "transaction_data";
  public static final String TRANSACTION_HASH = "transaction_hash";
  public static final String TRANSACTION_AMOUNT = "transaction_amount";
  public static final String TRANSACTION_CURRENCY = "transaction_currency";
  public static final String FIAT_VALUE = "fiat_value";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private boolean isBackEnable;
  private IabPresenter presenter;
  private Bundle savedInstanceState;
  private Bundle skuDetails;

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBundle(SKU_DETAILS, skuDetails);
  }

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

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SKU_DETAILS)) {
        skuDetails = savedInstanceState.getBundle(SKU_DETAILS);
      }
    }
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

  @Override public void finish(Bundle bundle) {
    setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
    finish();
  }

  @Override public void close(Bundle data) {
    Intent intent = null;
    if (data != null) {
      new Intent().putExtras(data);
    }
    setResult(Activity.RESULT_CANCELED, intent);
    finish();
  }

  @Override public void setup(BigDecimal amount, Boolean canBuy) {
    if (savedInstanceState == null) {
      //This is a feature toggle! If we set canBuy to true we will force the on chain buy flow
      //canBuy = true;
      Bundle bundle = new Bundle();
      bundle.putSerializable(TRANSACTION_AMOUNT, amount);
      // TODO: 12-08-2018 neuro add currency
      bundle.putSerializable(TRANSACTION_CURRENCY, "EUR");
      bundle.putString(APP_PACKAGE, getIntent().getExtras()
          .getString(APP_PACKAGE, ""));
      bundle.putString(PRODUCT_NAME, getIntent().getExtras()
          .getString(PRODUCT_NAME));
      bundle.putString(TRANSACTION_DATA, getIntent().getDataString());
      bundle.putString(EXTRA_DEVELOPER_PAYLOAD, getIntent().getExtras().getString(EXTRA_DEVELOPER_PAYLOAD));
      skuDetails = bundle;

      if (getSupportFragmentManager().getFragments()
          .isEmpty()) {
        if (canBuy) {
          getSupportFragmentManager().beginTransaction()
              .add(R.id.fragment_container, OnChainBuyFragment.newInstance(bundle,
                  getIntent().getData()
                      .toString()))
              .commit();
        } else {
          getSupportFragmentManager().beginTransaction()
              .add(R.id.fragment_container, ExpressCheckoutBuyFragment.newInstance(bundle))
              .commit();
        }
      }
    }
  }

  @Override public void navigateToCreditCardAuthorization() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, CreditCardAuthorizationFragment.newInstance(skuDetails))
        .commit();
  }

  @Override public void showError() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  public String getAppPackage() {
    if (getIntent().hasExtra(APP_PACKAGE)) {
      return getIntent().getStringExtra(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }
}
