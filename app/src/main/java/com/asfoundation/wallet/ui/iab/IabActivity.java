package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabActivity extends BaseActivity implements IabView {

  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  public static final String TRANSACTION_HASH = "transaction_hash";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private Button buyButton;
  private Button okErrorButton;
  private IabPresenter presenter;
  private View loadingView;
  private TextView appName;
  private TextView itemDescription;
  private TextView itemPrice;
  private ImageView appIcon;
  private View transactionCompletedLayout;
  private View transactionErrorLayout;
  private View buyLayout;
  private boolean isBackEnable;
  private TextView errorTextView;
  private TextView loadingMessage;

  public static Intent newIntent(Activity activity, Intent previousIntent) {
    Intent intent = new Intent(activity, IabActivity.class);
    intent.setData(previousIntent.getData());
    if (previousIntent.getExtras() != null) {
      intent.putExtras(previousIntent.getExtras());
    }
    intent.putExtra(APP_PACKAGE, activity.getCallingPackage());
    return intent;
  }

  @Override public void onBackPressed() {
    if (isBackEnable) {
      super.onBackPressed();
    }
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.iab_activity);
    buyButton = findViewById(R.id.buy_button);
    okErrorButton = findViewById(R.id.activity_iab_error_ok_button);
    loadingView = findViewById(R.id.loading);
    loadingMessage = findViewById(R.id.loading_message);
    appName = findViewById(R.id.iab_activity_app_name);
    errorTextView = findViewById(R.id.activity_iab_error_message);
    transactionCompletedLayout = findViewById(R.id.iab_activity_transaction_completed);
    buyLayout = findViewById(R.id.iab_activity_buy_layout);
    transactionErrorLayout = findViewById(R.id.activity_iab_error_view);
    appIcon = findViewById(R.id.iab_activity_item_icon);
    itemDescription = findViewById(R.id.iab_activity_item_description);
    itemPrice = findViewById(R.id.iab_activity_item_price);
    presenter = new IabPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
        new CompositeDisposable());
    Single.defer(() -> Single.just(getAppPackage()))
        .observeOn(Schedulers.io())
        .map(packageName -> new Pair<>(getApplicationName(packageName),
            getPackageManager().getApplicationIcon(packageName)))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          appName.setText(pair.first);
          appIcon.setImageDrawable(pair.second);
        }, throwable -> {
          throwable.printStackTrace();
          showError();
        });
    isBackEnable = true;
  }

  @Override protected void onStart() {
    super.onStart();
    presenter.present(getIntent().getData()
        .toString());
  }

  @Override protected void onStop() {
    presenter.stop();
    super.onStop();
  }

  @Override public Observable<String> getBuyClick() {
    return RxView.clicks(buyButton)
        .map(click -> getIntent().getData()
            .toString());
  }

  @Override public Observable<Object> getCancelClick() {
    return RxView.clicks(findViewById(R.id.cancel_button));
  }

  @Override public Observable<Object> getOkErrorClick() {
    return RxView.clicks(okErrorButton);
  }

  @Override public void finish(String hash) {
    Intent intent = new Intent();
    intent.putExtra(TRANSACTION_HASH, hash);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override public void showLoading() {
    showLoading(R.string.activity_aib_loading_message);
  }

  @Override public void showError() {
    showError(R.string.activity_iab_error_message);
  }

  @Override public void setup(TransactionBuilder transactionBuilder) {
    Formatter formatter = new Formatter();
    itemPrice.setText(formatter.format(Locale.getDefault(), "%(,.2f", transactionBuilder.amount()
        .doubleValue())
        .toString());
    if (getIntent().hasExtra(PRODUCT_NAME)) {
      itemDescription.setText(getIntent().getExtras()
          .getString(PRODUCT_NAME));
    }
  }

  @Override public void close() {
    setResult(Activity.RESULT_CANCELED, null);
    finish();
  }

  @Override public void showTransactionCompleted() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.VISIBLE);
    buyLayout.setVisibility(View.GONE);
  }

  @Override public void showBuy() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.VISIBLE);
    isBackEnable = true;
  }

  @Override public void showWrongNetworkError() {
    showError(R.string.activity_iab_wrong_network_message);
  }

  @Override public void showNoNetworkError() {
    showError(R.string.activity_iab_no_network_message);
  }

  @Override public void showApproving() {
    showLoading(R.string.activity_iab_approving_message);
  }

  @Override public void showBuying() {
    showLoading(R.string.activity_aib_buying_message);
  }

  @Override public void showNonceError() {
    showError(R.string.activity_iab_nonce_message);
  }

  @Override public void showNoTokenFundsError() {
    showError(R.string.activity_iab_no_token_funds_message);
  }

  @Override public void showNoEtherFundsError() {
    showError(R.string.activity_iab_no_ethereum_funds_message);
  }

  @Override public void showNoFundsError() {
    showError(R.string.activity_iab_no_funds_message);
  }

  private void showLoading(@StringRes int message) {
    isBackEnable = false;
    loadingView.setVisibility(View.VISIBLE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.GONE);
    loadingMessage.setText(message);
    loadingView.requestFocus();
    loadingView.setOnTouchListener((v, event) -> true);
  }

  public void showError(int error_message) {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.VISIBLE);
    transactionCompletedLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.GONE);
    isBackEnable = true;
    errorTextView.setText(error_message);
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    return packageManager.getApplicationLabel(packageInfo);
  }

  public String getAppPackage() {
    if (getIntent().hasExtra(APP_PACKAGE)) {
      return getIntent().getStringExtra(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }
}
