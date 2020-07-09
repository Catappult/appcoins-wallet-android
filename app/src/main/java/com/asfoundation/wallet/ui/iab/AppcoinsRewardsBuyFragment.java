package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.TransferParser;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import javax.inject.Inject;

import static com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY;

public class AppcoinsRewardsBuyFragment extends DaggerFragment implements AppcoinsRewardsBuyView {

  private static final String PRODUCT_NAME = "product_name";
  private static final String AMOUNT_KEY = "amount";
  private static final String URI_KEY = "uri_key";
  private static final String IS_BDS = "is_bds";
  private static final String TRANSACTION_KEY = "transaction_key";
  private static final String GAMIFICATION_LEVEL = "gamification_level";

  @Inject RewardsManager rewardsManager;
  @Inject TransferParser transferParser;
  @Inject BillingMessagesMapper billingMessagesMapper;
  @Inject BillingAnalytics analytics;
  @Inject CurrencyFormatUtils formatter;
  @Inject AppcoinsRewardsBuyInteract appcoinsRewardsBuyInteract;
  private View loadingView;
  private View transactionCompletedLayout;
  private LottieAnimationView lottieTransactionComplete;
  private AppcoinsRewardsBuyPresenter presenter;
  private BigDecimal amount;
  private IabView iabView;
  private String uri;
  private boolean isBds;
  private View transactionErrorLayout;
  private TextView errorMessage;
  private Button okErrorButton;
  private View supportIcon;
  private View supportLogo;
  private int gamificationLevel;

  public static Fragment newInstance(BigDecimal amount, TransactionBuilder transactionBuilder,
      String uri, String productName, boolean isBds, int gamificationLevel) {
    AppcoinsRewardsBuyFragment fragment = new AppcoinsRewardsBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putString(AMOUNT_KEY, amount.toString());
    bundle.putParcelable(TRANSACTION_KEY, transactionBuilder);
    bundle.putString(URI_KEY, uri);
    bundle.putString(PRODUCT_NAME, productName);
    bundle.putBoolean(IS_BDS, isBds);
    bundle.putInt(GAMIFICATION_LEVEL, gamificationLevel);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle arguments = getArguments();
    amount = new BigDecimal(arguments.getString(AMOUNT_KEY));
    uri = arguments.getString(URI_KEY);
    isBds = arguments.getBoolean(IS_BDS);
    gamificationLevel = arguments.getInt(GAMIFICATION_LEVEL);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.reward_payment_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadingView = view.findViewById(R.id.loading_view);

    errorMessage = view.findViewById(R.id.error_message);
    transactionErrorLayout = view.findViewById(R.id.generic_error_layout);
    okErrorButton = view.findViewById(R.id.error_dismiss);
    supportIcon = view.findViewById(R.id.layout_support_icn);
    supportLogo = view.findViewById(R.id.layout_support_logo);
    transactionCompletedLayout = view.findViewById(R.id.iab_activity_transaction_completed);

    TransactionBuilder transactionBuilder = getArguments().getParcelable(TRANSACTION_KEY);
    String callerPackageName = transactionBuilder.getDomain();
    presenter =
        new AppcoinsRewardsBuyPresenter(this, rewardsManager, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), amount, uri, callerPackageName, transferParser, isBds,
            analytics, transactionBuilder, formatter, gamificationLevel,
            appcoinsRewardsBuyInteract);

    lottieTransactionComplete =
        transactionCompletedLayout.findViewById(R.id.lottie_transaction_success);

    setupTransactionCompleteAnimation();

    presenter.present();
  }

  @Override public void onDestroyView() {
    presenter.stop();

    super.onDestroyView();
  }

  @Override public void finish(Purchase purchase) {
    finish(purchase, null);
  }

  @Override public void showLoading() {
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.INVISIBLE);
    loadingView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    loadingView.setVisibility(View.GONE);
  }

  @Override public void showNoNetworkError() {
    hideLoading();
    okErrorButton.setText(R.string.ok);
    errorMessage.setText(R.string.activity_iab_no_network_message);
    transactionErrorLayout.setVisibility(View.VISIBLE);
  }

  @Override public Observable<Object> getOkErrorClick() {
    return RxView.clicks(okErrorButton);
  }

  @Override public Observable<Object> getSupportIconClick() {
    return RxView.clicks(supportIcon);
  }

  @Override public Observable<Object> getSupportLogoClick() {
    return RxView.clicks(supportLogo);
  }

  @Override public void close() {
    iabView.close(billingMessagesMapper.mapCancellation());
  }

  @Override public void showGenericError() {
    showError(null);
  }

  @Override public void showError(Integer message) {
    okErrorButton.setText(R.string.ok);
    errorMessage.setText(
        getString(message != null ? message : R.string.activity_iab_error_message));
    transactionErrorLayout.setVisibility(View.VISIBLE);
    hideLoading();
  }

  @Override public void finish(String uid) {
    presenter.sendPaymentEvent();
    presenter.sendRevenueEvent();
    presenter.sendPaymentSuccessEvent();
    Bundle bundle = billingMessagesMapper.successBundle(uid);
    bundle.putString(PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.APPC_CREDITS.getId());
    iabView.finish(bundle);
  }

  @Override public void errorClose() {
    iabView.close(billingMessagesMapper.genericError());
  }

  @Override public void finish(Purchase purchase, @Nullable String orderReference) {
    presenter.sendPaymentEvent();
    presenter.sendRevenueEvent();
    presenter.sendPaymentSuccessEvent();
    Bundle bundle = billingMessagesMapper.mapPurchase(purchase, orderReference);
    bundle.putString(PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.APPC_CREDITS.getId());
    iabView.finish(bundle);
  }

  @Override public void showTransactionCompleted() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.VISIBLE);
  }

  @Override public long getAnimationDuration() {
    return lottieTransactionComplete.getDuration();
  }

  @Override public void lockRotation() {
    iabView.lockRotation();
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  private void setupTransactionCompleteAnimation() {
    LottieAnimationView lottieTransactionComplete =
        transactionCompletedLayout.findViewById(R.id.lottie_transaction_success);
    lottieTransactionComplete.setAnimation(R.raw.success_animation);
  }
}
