package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.appcoins.wallet.bdsbilling.repository.entity.DeveloperPurchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_CC;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_CURRENCY;

public class PaymentMethodsFragment extends DaggerFragment implements PaymentMethodsView {

  private static final String APP_PACKAGE = "app_package";
  private static final String TAG = PaymentMethodsFragment.class.getSimpleName();
  private static final String TRANSACTION = "transaction";
  private static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
  private static final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";
  private static final String INAPP_PURCHASE_ID = "INAPP_PURCHASE_ID";

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  PaymentMethodsPresenter presenter;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject BillingAnalytics analytics;
  @Inject BdsPendingTransactionService bdsPendingTransactionService;
  @Inject Billing billing;
  @Inject WalletService walletService;
  private ProgressBar loadingView;
  private View dialog;
  private TextView errorMessage;
  private View errorView;
  private View processingDialog;
  private Button buyButton;
  private Button cancelButton;
  private IabView iabView;
  private Button errorDismissButton;
  private PublishSubject<Boolean> setupSubject;
  private TransactionBuilder transaction;
  private double transactionValue;
  private String currency;
  private TextView appcPriceTv;
  private TextView fiatPriceTv;
  private TextView appNameTv;
  private TextView appSkuDescriptionTv;
  private TextView walletAddressTv;
  private String productName;

  public static Fragment newInstance(TransactionBuilder transaction, String currency,
      String productName) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(TRANSACTION, transaction);
    bundle.putSerializable(TRANSACTION_AMOUNT, transaction.amount());
    bundle.putString(TRANSACTION_CURRENCY, currency);
    bundle.putString(APP_PACKAGE, transaction.getDomain());
    bundle.putString(PRODUCT_NAME, productName);
    Fragment fragment = new PaymentMethodsFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  public static String serializeJson(Purchase purchase) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeveloperPurchase developerPurchase = objectMapper.readValue(new Gson().toJson(
        purchase.getSignature()
            .getMessage()), DeveloperPurchase.class);
    return objectMapper.writeValueAsString(developerPurchase);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("Payment Methods Fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupSubject = PublishSubject.create();

    boolean isBds = getArguments().getBoolean("isBds");
    transaction = getArguments().getParcelable(TRANSACTION);
    transactionValue =
        ((BigDecimal) getArguments().getSerializable(TRANSACTION_AMOUNT)).doubleValue();
    currency = getArguments().getString(TRANSACTION_CURRENCY);
    productName = getArguments().getString(PRODUCT_NAME);
    String appPackage = getArguments().getString(APP_PACKAGE);

    presenter = new PaymentMethodsPresenter(this, appPackage, AndroidSchedulers.mainThread(),
        Schedulers.io(), new CompositeDisposable(), inAppPurchaseInteractor,
        inAppPurchaseInteractor.getBillingMessagesMapper(), bdsPendingTransactionService, billing,
        analytics, isBds, Single.just(transaction));
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.payment_methods_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loadingView = view.findViewById(R.id.loading_view);
    dialog = view.findViewById(R.id.payment_methods);
    errorView = view.findViewById(R.id.error_message);
    errorMessage = view.findViewById(R.id.activity_iab_error_message);
    processingDialog = view.findViewById(R.id.processing_loading);
    buyButton = view.findViewById(R.id.buy_button);
    cancelButton = view.findViewById(R.id.cancel_button);
    errorDismissButton = view.findViewById(R.id.activity_iab_error_ok_button);

    appcPriceTv = view.findViewById(R.id.appc_price);
    fiatPriceTv = view.findViewById(R.id.fiat_price);
    appNameTv = view.findViewById(R.id.app_name);
    appSkuDescriptionTv = view.findViewById(R.id.app_sku_description);
    walletAddressTv = view.findViewById(R.id.wallet_address_footer);

    presenter.present(transactionValue, currency, savedInstanceState);
  }

  @Override public void onDestroyView() {
    presenter.stop();
    super.onDestroyView();
    compositeDisposable.dispose();

    cancelButton = null;
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  @Override
  public void showPaymentMethods(@NotNull List<PaymentMethod> paymentMethods, FiatValue fiatValue,
      boolean isDonation) {
    Formatter formatter = new Formatter();
    String valueText = formatter.format(Locale.getDefault(), "%(,.2f", transaction.amount())
        .toString() + " APPC";
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    String currency = mapCurrencyCodeToSymbol(fiatValue.getCurrency());
    String priceText = decimalFormat.format(fiatValue.getAmount()) + ' ' + currency;
    String finalString = priceText;
    Spannable spannable = new SpannableString(finalString);
    spannable.setSpan(
        new ForegroundColorSpan(getResources().getColor(R.color.dialog_buy_total_value)),
        finalString.indexOf(priceText), finalString.indexOf(priceText) + priceText.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    appcPriceTv.setText(valueText);
    fiatPriceTv.setText(spannable, TextView.BufferType.SPANNABLE);
    int buyButtonText = isDonation ? R.string.action_donate : R.string.action_buy;
    buyButton.setText(getResources().getString(buyButtonText));

    if (isDonation) {
      appSkuDescriptionTv.setText(getResources().getString(R.string.item_donation));
      appNameTv.setText(getResources().getString(R.string.item_donation));
    } else if (productName != null) {
      appNameTv.setText(String.format(getString(R.string.buying), productName));
      appSkuDescriptionTv.setText(productName);
    }

    presenter.sendPurchaseDetails(PAYMENT_METHOD_CC);

    compositeDisposable.add(walletService.getWalletAddress()
        .doOnSuccess(address -> walletAddressTv.setText(address))
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
    setupSubject.onNext(true);
  }

  @Override public void showError() {
    loadingView.setVisibility(View.GONE);
    dialog.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.activity_iab_error_message);
  }

  @Override public void finish(Purchase purchase) throws IOException {
    Bundle bundle = new Bundle();
    bundle.putString(INAPP_PURCHASE_DATA, serializeJson(purchase));
    bundle.putString(INAPP_DATA_SIGNATURE, purchase.getSignature()
        .getValue());
    bundle.putString(INAPP_PURCHASE_ID, purchase.getUid());
    close(bundle);
  }

  @Override public void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
    dialog.setVisibility(View.INVISIBLE);
  }

  @Override public void hideLoading() {
    loadingView.setVisibility(View.GONE);
    if (processingDialog.getVisibility() != View.VISIBLE) {
      dialog.setVisibility(View.VISIBLE);
    }
  }

  @Override public Observable<Object> getCancelClick() {
    return RxView.clicks(cancelButton);
  }

  @Override public void close(Bundle data) {
    iabView.close(data);
  }

  @Override public Observable<Object> errorDismisses() {
    return RxView.clicks(errorDismissButton);
  }

  @Override public Observable<Boolean> setupUiCompleted() {
    return setupSubject;
  }

  @Override public void showProcessingLoadingDialog() {
    dialog.setVisibility(View.GONE);
    loadingView.setVisibility(View.GONE);
    processingDialog.setVisibility(View.VISIBLE);
  }

  public String mapCurrencyCodeToSymbol(String currencyCode) {
    return Currency.getInstance(currencyCode)
        .getCurrencyCode();
  }
}
