package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.util.TransferParser;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import javax.inject.Inject;

public class AppcoinsRewardsBuyFragment extends DaggerFragment implements AppcoinsRewardsBuyView {

  public static final String AMOUNT_KEY = "amount";
  public static final String PACKAGE_NAME_KEY = "packageName";
  public static final String URI_KEY = "uri_key";
  @Inject RewardsManager rewardsManager;
  @Inject BdsPendingTransactionService bdsPendingTransactionService;
  @Inject TransferParser transferParser;
  private View buyButton;
  private View loadingView;
  private AppcoinsRewardsBuyPresenter presenter;
  private TextView amountView;
  private BigDecimal amount;
  private View paymentDetailsView;
  private IabView iabView;
  private String uri;

  public static Fragment newInstance(BigDecimal amount, String packageName, String uri) {
    AppcoinsRewardsBuyFragment fragment = new AppcoinsRewardsBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putString(AMOUNT_KEY, amount.toString());
    bundle.putString(PACKAGE_NAME_KEY, packageName);
    bundle.putString(URI_KEY, uri);
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
    amountView = view.findViewById(R.id.spending_amount);
    paymentDetailsView = view.findViewById(R.id.payment_details_view);

    presenter =
        new AppcoinsRewardsBuyPresenter(this, rewardsManager, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), amount, BuildConfig.DEFAULT_STORE_ADDRESS,
            BuildConfig.DEFAULT_OEM_ADDRESS, uri, getCallerPackageName(), transferParser);
  }

  @Override public void onStart() {
    super.onStart();
    presenter.present();
  }

  @Override public void onStop() {
    presenter.stop();
    super.onStop();
  }

  private String getCallerPackageName() {
    return getArguments().getString(PACKAGE_NAME_KEY);
  }

  @Override public Observable<Object> getBuyClick() {
    return RxView.clicks(buyButton);
  }

  @Override public void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
  }

  @Override public void setAmount(String amount) {
    amountView.setText(amount);
  }

  @Override public void showPaymentDetails() {
    paymentDetailsView.setVisibility(View.VISIBLE);
  }

  @Override public void hidePaymentDetails() {
    paymentDetailsView.setVisibility(View.GONE);
  }

  @Override public void finish() {
    iabView.close(null);
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
