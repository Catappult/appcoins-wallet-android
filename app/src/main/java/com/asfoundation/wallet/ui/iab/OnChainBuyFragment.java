package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_APPC;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public class OnChainBuyFragment extends DaggerFragment implements OnChainBuyView {

  public static final String APP_PACKAGE = "app_package";
  public static final String TRANSACTION_BUILDER_KEY = "transaction_builder";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private PublishRelay<String> buyButtonClick;
  private Button buyButton;
  private Button cancelButton;
  private Button okErrorButton;
  private OnChainBuyPresenter presenter;
  private View loadingView;
  private TextView appName;
  private TextView itemDescription;
  private TextView itemHeaderDescription;
  private TextView itemPrice;
  private TextView itemFinalPrice;
  private ImageView appIcon;
  private View transactionCompletedLayout;
  private View transactionErrorLayout;
  private View buyLayout;
  private TextView errorTextView;
  private TextView loadingMessage;
  private ProgressBar buyDialogLoading;
  private ArrayAdapter<BigDecimal> adapter;
  private View infoDialog;
  private TextView walletAddressTextView;
  private IabView iabView;
  private Bundle extras;
  private String data;
  private boolean isBds;
  @Inject BillingAnalytics analytics;
  private TransactionBuilder transaction;

  public static OnChainBuyFragment newInstance(Bundle extras, String data, boolean bdsIap,
      TransactionBuilder transaction) {
    OnChainBuyFragment fragment = new OnChainBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putBundle("extras", extras);
    bundle.putString("data", data);
    bundle.putBoolean("isBds", bdsIap);
    bundle.putParcelable(TRANSACTION_BUILDER_KEY, transaction);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    buyButtonClick = PublishRelay.create();
    extras = getArguments().getBundle("extras");
    data = getArguments().getString("data");
    isBds = getArguments().getBoolean("isBds");
    transaction = getArguments().getParcelable(TRANSACTION_BUILDER_KEY);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_iab, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    buyButton = view.findViewById(R.id.buy_button);
    buyDialogLoading = view.findViewById(R.id.loading_view);
    cancelButton = view.findViewById(R.id.cancel_button);
    okErrorButton = view.findViewById(R.id.activity_iab_error_ok_button);
    loadingView = view.findViewById(R.id.loading);
    loadingMessage = view.findViewById(R.id.loading_message);
    appName = view.findViewById(R.id.app_name);
    errorTextView = view.findViewById(R.id.activity_iab_error_message);
    transactionCompletedLayout = view.findViewById(R.id.iab_activity_transaction_completed);
    buyLayout = view.findViewById(R.id.dialog_buy_app);
    infoDialog = view.findViewById(R.id.info_dialog);
    transactionErrorLayout = view.findViewById(R.id.activity_iab_error_view);
    appIcon = view.findViewById(R.id.app_icon);
    itemDescription = view.findViewById(R.id.sku_description);
    itemHeaderDescription = view.findViewById(R.id.app_sku_description);
    itemPrice = view.findViewById(R.id.sku_price);
    itemFinalPrice = view.findViewById(R.id.total_price);
    walletAddressTextView = view.findViewById(R.id.wallet_address_footer);

    presenter =
        new OnChainBuyPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), inAppPurchaseInteractor.getBillingMessagesMapper(), isBds,
            extras.getString(PRODUCT_NAME), analytics, getAppPackage(), data);
    adapter =
        new ArrayAdapter<>(getContext().getApplicationContext(), R.layout.iab_raiden_dropdown_item,
            R.id.item, new ArrayList<>());
    Single.defer(() -> Single.just(getAppPackage()))
        .observeOn(Schedulers.io())
        .map(packageName -> new Pair<>(getApplicationName(packageName),
            getContext().getPackageManager()
                .getApplicationIcon(packageName)))
        .onErrorResumeNext(throwable -> getDefaultInfo())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          appName.setText(pair.first);
          appIcon.setImageDrawable(pair.second);
        }, throwable -> {
          throwable.printStackTrace();
          showError();
        });

    buyButton.setOnClickListener(v -> buyButtonClick.accept(data));
    presenter.present(data, getAppPackage(), extras.getString(PRODUCT_NAME, ""),
        (BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT), transaction.getPayload());

    buyButton.performClick();
  }

  @Override public void onResume() {
    super.onResume();
    presenter.resume();
  }

  @Override public void onPause() {
    presenter.pause();
    super.onPause();
  }

  @Override public void onDestroyView() {
    presenter.stop();
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  private Single<Pair<CharSequence, Drawable>> getDefaultInfo() {
    return inAppPurchaseInteractor.parseTransaction(data, isBds)
        .map(transaction -> new Pair<>(transaction.getType(),
            getContext().getDrawable(R.drawable.purchase_placeholder)));
  }

  @Override public PublishRelay<String> getBuyClick() {
    return buyButtonClick;
  }

  @Override public Observable<Object> getCancelClick() {
    return RxView.clicks(cancelButton);
  }

  @Override public Observable<Object> getOkErrorClick() {
    return RxView.clicks(okErrorButton);
  }

  @Override public void showLoading() {
    showLoading(R.string.activity_aib_loading_message);
  }

  @Override public void close(Bundle data) {
    iabView.close(data);
  }

  @Override public void finish(Bundle data) {
    presenter.sendPaymentEvent(PAYMENT_METHOD_APPC);
    presenter.sendRevenueEvent();
    iabView.finish(data);
  }

  @Override public void showError() {
    showError(R.string.activity_iab_error_message);
  }

  @Override public void setup(String productName, boolean isDonation) {
    Formatter formatter = new Formatter();
    String formatedPrice = formatter.format(Locale.getDefault(), "%(,.2f",
        ((BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT)).doubleValue())
        .toString() + " APPC";
    int buyButtonText = isDonation ? R.string.action_donate : R.string.action_buy;
    buyButton.setText(getResources().getString(buyButtonText));
    itemPrice.setText(formatedPrice);
    Spannable spannable = new SpannableString(formatedPrice);
    spannable.setSpan(
        new ForegroundColorSpan(getResources().getColor(R.color.dialog_buy_total_value)), 0,
        formatedPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    itemFinalPrice.setText(spannable);
    if (isDonation) {
      itemDescription.setText(getResources().getString(R.string.item_donation));
      itemHeaderDescription.setText(getResources().getString(R.string.item_donation));
    } else if (productName != null) {
      itemDescription.setText(productName);
      itemHeaderDescription.setText(String.format(getString(R.string.buying), productName));
    }
    buyDialogLoading.setVisibility(View.GONE);
    infoDialog.setVisibility(View.VISIBLE);
  }

  @Override public void showTransactionCompleted() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    buyLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.VISIBLE);
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
    showLoading(R.string.activity_iab_buying_message);
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

  @Override public void showWallet(String wallet) {
    walletAddressTextView.setText(wallet);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("Regular buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  private void showLoading(@StringRes int message) {
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
    errorTextView.setText(error_message);
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getContext().getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    return packageManager.getApplicationLabel(packageInfo);
  }

  public String getAppPackage() {
    if (extras.containsKey(APP_PACKAGE)) {
      return extras.getString(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }
}
