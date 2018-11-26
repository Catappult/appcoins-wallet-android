package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.appcoins.wallet.billing.util.PayloadHelper;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.BaseActivity;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabActivity extends BaseActivity implements IabView {

  private static final String BDS = "BDS";

  public static final String RESPONSE_CODE = "RESPONSE_CODE";
  public static final int RESULT_USER_CANCELED = 1;
  public static final String SKU_DETAILS = "sku_details";
  public static final String APP_PACKAGE = "app_package";
  public static final String TRANSACTION_EXTRA = "transaction_extra";
  public static final String PRODUCT_NAME = "product_name";
  public static final String EXTRA_DEVELOPER_PAYLOAD = "developer_payload";
  public static final String TRANSACTION_DATA = "transaction_data";
  public static final String TRANSACTION_HASH = "transaction_hash";
  public static final String TRANSACTION_AMOUNT = "transaction_amount";
  public static final String TRANSACTION_CURRENCY = "transaction_currency";
  public static final String FIAT_VALUE = "fiat_value";
  private static final String TAG = IabActivity.class.getSimpleName();
  private static final String IS_BDS_EXTRA = "is_bds_extra";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private boolean isBackEnable;
  private IabPresenter presenter;
  private Bundle savedInstanceState;
  private Bundle skuDetails;
  private TransactionBuilder transaction;
  private boolean isBds;

  public static Intent newIntent(Activity activity, Intent previousIntent,
      TransactionBuilder transaction, Boolean isBds) {
    Intent intent = new Intent(activity, IabActivity.class);
    intent.setData(previousIntent.getData());
    if (previousIntent.getExtras() != null) {
      intent.putExtras(previousIntent.getExtras());
    }
    intent.putExtra(TRANSACTION_EXTRA, transaction);
    intent.putExtra(IS_BDS_EXTRA, isBds);
    intent.putExtra(APP_PACKAGE, transaction.getDomain());
    return intent;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_iab);
    this.savedInstanceState = savedInstanceState;
    isBds = getIntent().getBooleanExtra(IS_BDS_EXTRA, false);
    transaction = getIntent().getParcelableExtra(TRANSACTION_EXTRA);
    isBackEnable = true;
    presenter = new IabPresenter(this);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SKU_DETAILS)) {
        skuDetails = savedInstanceState.getBundle(SKU_DETAILS);
      }
    }
    presenter.present(savedInstanceState);
  }

  @Override public void onBackPressed() {
    if (isBackEnable) {
      Bundle bundle = new Bundle();
      bundle.putInt(RESPONSE_CODE, RESULT_USER_CANCELED);
      close(bundle);
      super.onBackPressed();
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBundle(SKU_DETAILS, skuDetails);
  }

  @Override public void finish(Bundle bundle) {
    setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
    finish();
  }

  @Override public void showError() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  @Override public void close(Bundle data) {
    Intent intent = new Intent();
    if (data != null) {
      intent.putExtras(data);
    }
    setResult(Activity.RESULT_CANCELED, intent);
    finish();
  }

  @Override public void navigateToCreditCardAuthorization(boolean isBds) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container,
            CreditCardAuthorizationFragment.newInstance(skuDetails, transaction.getSkuId(),
                transaction.getType(), isBds ? BDS : null))
        .commit();
  }

  @Override public void showPaymentMethodsView() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, PaymentMethodsFragment.newInstance())
        .commit();
  }

  @Override public void showOnChain(BigDecimal amount, boolean isBds) {
    if (savedInstanceState == null && getSupportFragmentManager().getFragments()
        .isEmpty()) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, OnChainBuyFragment.newInstance(createBundle(amount),
              getIntent().getData()
                  .toString(), isBds))
          .commit();
    }
  }

  @Override public void showCcPayment(BigDecimal amount, String currency, boolean isBds) {
    if (savedInstanceState == null && getSupportFragmentManager().getFragments()
        .isEmpty()) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, ExpressCheckoutBuyFragment.newInstance(
              createBundle(BigDecimal.valueOf(amount.doubleValue()), currency), isBds))
          .commit();
    }
  }

  @Override public void showAppcoinsCreditsPayment(BigDecimal amount) {
    if (savedInstanceState == null && getSupportFragmentManager().getFragments()
        .isEmpty()) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container,
              AppcoinsRewardsBuyFragment.newInstance(amount, transaction.getDomain(),
                  getIntent().getData()
                      .toString(), getIntent().getExtras()
                      .getString(PRODUCT_NAME, ""), isBds))
          .commit();
    }
  }

  @Override public void showPaymentMethods(List<PaymentMethod> paymentMethods) {
    Log.d(TAG, "showPaymentMethods() called with: paymentMethods = [" + paymentMethods + "]");
  }

  @NonNull private Bundle createBundle(BigDecimal amount, String currency) {
    Bundle bundle = createBundle(amount);

    bundle.putSerializable(TRANSACTION_CURRENCY, currency);

    return bundle;
  }

  @NonNull private Bundle createBundle(BigDecimal amount) {
    Bundle bundle = new Bundle();
    bundle.putSerializable(TRANSACTION_AMOUNT, amount);
    bundle.putString(APP_PACKAGE, transaction.getDomain());
    bundle.putString(PRODUCT_NAME, getIntent().getExtras()
        .getString(PRODUCT_NAME));
    bundle.putString(TRANSACTION_DATA, getIntent().getDataString());
    String developerPayload = PayloadHelper.INSTANCE.getPayload(getIntent().getExtras()
        .getString(EXTRA_DEVELOPER_PAYLOAD));
    if (developerPayload != null) {
      bundle.putString(EXTRA_DEVELOPER_PAYLOAD, developerPayload);
    } else {
      bundle.putString(EXTRA_DEVELOPER_PAYLOAD, transaction.getPayload());
    }
    skuDetails = bundle;
    return bundle;
  }
}