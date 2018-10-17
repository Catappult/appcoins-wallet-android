package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.util.TransferParser;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import javax.inject.Inject;

public class AppcoinsRewardsBuyFragment extends DaggerFragment implements AppcoinsRewardsBuyView {

  public static final String AMOUNT_KEY = "amount";
  public static final String PACKAGE_NAME_KEY = "packageName";
  public static final String URI_KEY = "uri_key";
  public static final String PRODUCT_NAME = "product_name";
  @Inject RewardsManager rewardsManager;
  @Inject BdsPendingTransactionService bdsPendingTransactionService;
  @Inject TransferParser transferParser;
  @Inject BillingMessagesMapper billingMessagesMapper;
  private View buyButton;
  private View loadingView;
  private View genericLoadingView;
  private AppcoinsRewardsBuyPresenter presenter;
  private TextView amountView;
  private TextView productDescription;
  private BigDecimal amount;
  private View paymentDetailsView;
  private IabView iabView;
  private String uri;
  private TextView appName;
  private ImageView appIcon;
  private TextView currencyName;

  public static Fragment newInstance(BigDecimal amount, String packageName, String uri,
      String productName) {
    AppcoinsRewardsBuyFragment fragment = new AppcoinsRewardsBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putString(AMOUNT_KEY, amount.toString());
    bundle.putString(PACKAGE_NAME_KEY, packageName);
    bundle.putString(URI_KEY, uri);
    bundle.putString(PRODUCT_NAME, productName);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    amount = new BigDecimal(getArguments().getString(AMOUNT_KEY));
    uri = getArguments().getString(URI_KEY);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.reward_payment_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    buyButton = view.findViewById(R.id.buy_button);
    loadingView = view.findViewById(R.id.loading_view);
    genericLoadingView = view.findViewById(R.id.loading);
    appName = view.findViewById(R.id.iab_activity_app_name);
    currencyName = view.findViewById(R.id.appc);
    amountView = view.findViewById(R.id.iab_activity_item_price);
    productDescription = view.findViewById(R.id.iab_activity_item_description);
    paymentDetailsView = view.findViewById(R.id.payment_details_view);
    appIcon = view.findViewById(R.id.iab_activity_item_icon);

    presenter =
        new AppcoinsRewardsBuyPresenter(this, rewardsManager, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), amount, BuildConfig.DEFAULT_STORE_ADDRESS,
            BuildConfig.DEFAULT_OEM_ADDRESS, uri, getCallerPackageName(), transferParser,
            getProductName());

    Single.defer(() -> Single.just(getCallerPackageName()))
        .observeOn(Schedulers.io())
        .map(packageName -> new Pair<>(getApplicationName(packageName),
            getContext().getPackageManager()
                .getApplicationIcon(packageName)))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          appName.setText(pair.first);
        }, throwable -> {
          throwable.printStackTrace();
        });
  }

  @Override public void onStart() {
    super.onStart();
    presenter.present();
  }

  @Override public void onStop() {
    presenter.stop();
    super.onStop();
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getContext().getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    packageInfo.loadIcon(getContext().getPackageManager());
    appIcon.setImageResource(R.drawable.appbar_background_color);
    return packageManager.getApplicationLabel(packageInfo);
  }

  private String getProductName() {
    return getArguments().getString(PRODUCT_NAME, "");
  }

  private String getCallerPackageName() {
    return getArguments().getString(PACKAGE_NAME_KEY);
  }

  @Override public Observable<Object> getBuyClick() {
    return RxView.clicks(buyButton);
  }

  @Override public void showProcessingLoading() {
    loadingView.setVisibility(View.VISIBLE);
  }

  @Override public void setupView(String amount, String productName, String packageName) {
    amountView.setText(amount);
    currencyName.setText("APPC Rewards");
    productDescription.setText(productName);
  }

  @Override public void showPaymentDetails() {
    paymentDetailsView.setVisibility(View.VISIBLE);
  }

  @Override public void hidePaymentDetails() {
    paymentDetailsView.setVisibility(View.GONE);
  }

  @Override public void finish(Purchase purchase) {
    iabView.finish(billingMessagesMapper.mapPurchase(purchase));
  }

  @Override public void hideGenericLoading() {
    loadingView.setVisibility(View.GONE);
  }

  @Override public void showLoading() {
    genericLoadingView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    genericLoadingView.setVisibility(View.GONE);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }
}
