package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.asf.wallet.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_CURRENCY;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class ExpressCheckoutBuyFragment extends DaggerFragment implements ExpressCheckoutBuyView {
  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private Bundle extras;
  private PublishRelay<Snackbar> buyButtonClick;
  private IabView iabView;
  private ExpressCheckoutBuyPresenter presenter;
  private ProgressBar loadingView;
  private View dialog;
  private TextView appName;
  private TextView itemHeaderDescription;
  private TextView itemListDescription;
  private TextView itemPrice;
  private TextView itemFinalPrice;
  private ImageView appIcon;
  private Button buyButton;
  private Button cancelButton;
  private FiatValue fiatValue;
  private View errorView;
  private TextView errorMessage;
  private Button errorDismissButton;

  public static ExpressCheckoutBuyFragment newInstance(Bundle extras) {
    ExpressCheckoutBuyFragment fragment = new ExpressCheckoutBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putBundle("extras", extras);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    extras = getArguments().getBundle("extras");
    presenter = new ExpressCheckoutBuyPresenter(this, inAppPurchaseInteractor,
        AndroidSchedulers.mainThread(), new CompositeDisposable(),
        inAppPurchaseInteractor.getBillingMessagesMapper(),
        inAppPurchaseInteractor.getBillingSerializer());
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_express_checkout_buy, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    loadingView = view.findViewById(R.id.loading_view);
    dialog = view.findViewById(R.id.info_dialog);
    appName = view.findViewById(R.id.app_name);
    itemHeaderDescription = view.findViewById(R.id.app_sku_description);
    itemListDescription = view.findViewById(R.id.sku_description);
    itemPrice = view.findViewById(R.id.sku_price);
    itemFinalPrice = view.findViewById(R.id.total_price);
    appIcon = view.findViewById(R.id.app_icon);
    buyButton = view.findViewById(R.id.buy_button);
    cancelButton = view.findViewById(R.id.cancel_button);
    errorView = view.findViewById(R.id.error_message);
    errorMessage = view.findViewById(R.id.activity_iab_error_message);
    errorDismissButton = view.findViewById(R.id.activity_iab_error_ok_button);

    Single.defer(() -> Single.just(getAppPackage()))
        .observeOn(Schedulers.io())
        .map(packageName -> new Pair<>(getApplicationName(packageName),
            getContext().getPackageManager()
                .getApplicationIcon(packageName)))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          appName.setText(pair.first);
          appIcon.setImageDrawable(pair.second);
        }, throwable -> {
          throwable.printStackTrace();
          showError();
        });
    buyButton.setOnClickListener(v -> iabView.navigateToCreditCardAuthorization());
    // TODO: 12-08-2018 neuro add currency
    presenter.present(((BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT)).doubleValue(),
        extras.getString(TRANSACTION_CURRENCY));
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    presenter.stop();
    loadingView = null;
    dialog = null;
    appName = null;
    itemHeaderDescription = null;
    itemListDescription = null;
    itemPrice = null;
    itemFinalPrice = null;
    appIcon = null;
    buyButton = null;
    cancelButton = null;
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  @Override public void setup(FiatValue response) {
    Formatter formatter = new Formatter();
    StringBuilder builder = new StringBuilder();
    String valueText = formatter.format(Locale.getDefault(), "%(,.2f",
        (BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT))
        .toString() + " APPC";
    String valueTextCompose = valueText + " = ";
    String currency = mapCurrencyCodeToSymbol(response.getCurrency());
    String priceText = currency + Double.toString(response.getAmount());
    String finalString = valueTextCompose + priceText;
    Spannable spannable = new SpannableString(finalString);
    spannable.setSpan(new AbsoluteSizeSpan(12, true), finalString.indexOf(valueTextCompose),
        finalString.indexOf(valueTextCompose) + valueTextCompose.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    spannable.setSpan(
        new ForegroundColorSpan(getResources().getColor(R.color.dialog_buy_total_value)),
        finalString.indexOf(priceText), finalString.indexOf(priceText) + priceText.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    itemPrice.setText(valueText);
    itemFinalPrice.setText(spannable, TextView.BufferType.SPANNABLE);
    fiatValue = response;
    if (extras.containsKey(PRODUCT_NAME)) {
      itemHeaderDescription.setText(extras.getString(PRODUCT_NAME));
      itemListDescription.setText(extras.getString(PRODUCT_NAME));
    }
    dialog.setVisibility(View.VISIBLE);
    loadingView.setVisibility(View.GONE);
  }

  @Override public void showError() {
    loadingView.setVisibility(View.GONE);
    dialog.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.activity_iab_error_message);
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

  public String mapCurrencyCodeToSymbol(String currencyCode) {
    return Currency.getInstance(currencyCode)
        .getSymbol();
  }
}
