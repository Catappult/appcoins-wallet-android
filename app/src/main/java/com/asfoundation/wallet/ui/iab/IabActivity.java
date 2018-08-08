package com.asfoundation.wallet.ui.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.BaseActivity;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabActivity extends BaseActivity implements IabView {

  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  public static final String TRANSACTION_HASH = "transaction_hash";
  private static final String TAG = IabActivity.class.getSimpleName();
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private BehaviorSubject<Object> raidenMoreInfoOkButtonClick;
  private BehaviorSubject<Boolean> createChannelClick;
  private PublishRelay<IabPresenter.BuyData> buyButtonClick;
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
  private Spinner dropdown;
  private ArrayAdapter<BigDecimal> adapter;
  private CheckBox checkbox;
  private View raidenMoreInfoView;
  private Group amountGroup;
  private View raidenLayout;
  private Group createChannelGroup;
  private TextView walletAddressTextView;
  private View channelNoFundsView;

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
    raidenLayout = findViewById(R.id.raiden_layout);
    itemPrice = findViewById(R.id.iab_activity_item_price);
    dropdown = findViewById(R.id.channel_amount_dropdown);
    amountGroup = findViewById(R.id.amount_group);
    createChannelGroup = findViewById(R.id.create_channel_group);
    walletAddressTextView = findViewById(R.id.wallet_address);
    presenter = new IabPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
        new CompositeDisposable(), inAppPurchaseInteractor.getBillingMessagesMapper());
    adapter =
        new ArrayAdapter<>(getApplicationContext(), R.layout.iab_raiden_dropdown_item, R.id.item,
            new ArrayList<>());
    dropdown.setAdapter(adapter);
    checkbox = findViewById(R.id.iab_activity_create_channel);
    createChannelClick = BehaviorSubject.create();
    buyButtonClick = PublishRelay.create();
    raidenMoreInfoOkButtonClick = BehaviorSubject.create();
    raidenMoreInfoView = View.inflate(new ContextThemeWrapper(this, R.style.AppTheme),
        R.layout.iab_activity_raiden_more_info, null);
    channelNoFundsView = View.inflate(new ContextThemeWrapper(this, R.style.AppTheme),
        R.layout.iab_activity_no_channel_funds, null);
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
    buyButton.setOnClickListener(v -> buyButtonClick.accept(
        new IabPresenter.BuyData(checkbox.isChecked(), getIntent().getData()
            .toString(), getChannelBudget())));
    isBackEnable = true;
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

  @Override public Observable<IabPresenter.BuyData> getBuyClick() {
    return buyButtonClick;
  }

  @Override public Observable<Object> getCancelClick() {
    return RxView.clicks(findViewById(R.id.cancel_button));
  }

  @Override public Observable<Object> getOkErrorClick() {
    return RxView.clicks(okErrorButton);
  }

  @Override public void finish(Bundle bundle) {
    setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
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

  @Override public void close(Bundle data) {
    Intent intent = null;
    if (data != null) {
      new Intent().putExtras(data);
    }
    setResult(Activity.RESULT_CANCELED, intent);
    finish();
  }

  @Override public void showTransactionCompleted() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.GONE);
    raidenLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.VISIBLE);
  }

  @Override public void showBuy() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.VISIBLE);
    raidenLayout.setVisibility(View.VISIBLE);
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

  @Override public void showRaidenChannelValues(List<BigDecimal> values) {
    adapter.clear();
    adapter.addAll(values);
    adapter.notifyDataSetChanged();
  }

  @Override public Observable<Boolean> getCreateChannelClick() {
    return createChannelClick;
  }

  @Override public void showRaidenInfo() {
    AlertDialog dialog = new AlertDialog.Builder(this).setView(raidenMoreInfoView)
        .show();

    raidenMoreInfoView.findViewById(R.id.iab_activity_raiden_ok_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          ((ViewGroup) raidenMoreInfoView.getParent()).removeView(raidenMoreInfoView);
          raidenMoreInfoOkButtonClick.onNext(new Object());
        });
  }

  @Override public Observable<Object> getDontShowAgainClick() {
    return raidenMoreInfoOkButtonClick.filter(o -> ((CheckBox) raidenMoreInfoView.findViewById(
        R.id.iab_activity_raiden_dont_show_again)).isChecked());
  }

  @Override public void showChannelAmount() {
    amountGroup.setVisibility(View.VISIBLE);
  }

  @Override public void hideChannelAmount() {
    amountGroup.setVisibility(View.GONE);
  }

  @Override public void showChannelAsDefaultPayment() {
    checkbox.setChecked(true);
    checkbox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> createChannelClick.onNext(isChecked));
    createChannelGroup.setVisibility(View.VISIBLE);
  }

  @Override public void showDefaultAsDefaultPayment() {
    checkbox.setChecked(false);
    checkbox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> createChannelClick.onNext(isChecked));
    createChannelGroup.setVisibility(View.VISIBLE);
  }

  @Override public void showWallet(String wallet) {
    walletAddressTextView.setText(wallet);
  }

  @Override public void showNoChannelFundsError() {
    showBuy();
    AlertDialog dialog = new AlertDialog.Builder(this).setView(channelNoFundsView)
        .show();

    channelNoFundsView.findViewById(R.id.iab_activity_raiden_no_funds_ok_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          ((ViewGroup) channelNoFundsView.getParent()).removeView(channelNoFundsView);
          buyButtonClick.accept(new IabPresenter.BuyData(false, getIntent().getData()
              .toString(), getChannelBudget()));
        });
    channelNoFundsView.findViewById(R.id.iab_activity_raiden_no_funds_cancel_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          ((ViewGroup) channelNoFundsView.getParent()).removeView(channelNoFundsView);
        });
  }

  @NonNull private BigDecimal getChannelBudget() {
    return new BigDecimal(dropdown.getSelectedItem() == null ? "0" : dropdown.getSelectedItem()
        .toString());
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
