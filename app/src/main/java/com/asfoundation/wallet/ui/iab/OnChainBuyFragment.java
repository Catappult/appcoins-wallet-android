package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.airbnb.lottie.FontAssetDelegate;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.TextDelegate;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_APPC;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public class OnChainBuyFragment extends DaggerFragment implements OnChainBuyView {

  private static final String APP_PACKAGE = "app_package";
  private static final String TRANSACTION_BUILDER_KEY = "transaction_builder";
  private static final String BONUS_KEY = "bonus";
  private static final String VALID_BONUS = "valid_bonus";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject BillingAnalytics analytics;
  private Button okErrorButton;
  private OnChainBuyPresenter presenter;
  private View loadingView;
  private View transactionCompletedLayout;
  private View transactionErrorLayout;
  private TextView errorTextView;
  private TextView loadingMessage;
  private ArrayAdapter<BigDecimal> adapter;
  private IabView iabView;
  private Bundle extras;
  private String data;
  private boolean isBds;
  private boolean validBonus;
  private TransactionBuilder transaction;
  private LottieAnimationView lottieTransactionComplete;

  public static OnChainBuyFragment newInstance(Bundle extras, String data, boolean bdsIap,
      TransactionBuilder transaction, String bonus, boolean validBonus) {
    OnChainBuyFragment fragment = new OnChainBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putBundle("extras", extras);
    bundle.putString("data", data);
    bundle.putBoolean("isBds", bdsIap);
    bundle.putParcelable(TRANSACTION_BUILDER_KEY, transaction);
    bundle.putString(BONUS_KEY, bonus);
    bundle.putBoolean(VALID_BONUS, validBonus);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    extras = getArguments().getBundle("extras");
    data = getArguments().getString("data");
    isBds = getArguments().getBoolean("isBds");
    transaction = getArguments().getParcelable(TRANSACTION_BUILDER_KEY);
    validBonus = getValidBonus();
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_iab, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    okErrorButton = view.findViewById(R.id.activity_iab_error_ok_button);
    loadingView = view.findViewById(R.id.loading);
    loadingMessage = view.findViewById(R.id.loading_message);
    errorTextView = view.findViewById(R.id.activity_iab_error_message);
    transactionCompletedLayout = view.findViewById(R.id.iab_activity_transaction_completed);
    transactionErrorLayout = view.findViewById(R.id.activity_iab_error_view);

    lottieTransactionComplete =
        transactionCompletedLayout.findViewById(R.id.lottie_transaction_success);

    presenter =
        new OnChainBuyPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), inAppPurchaseInteractor.getBillingMessagesMapper(), isBds,
            analytics, getAppPackage(), data);
    adapter =
        new ArrayAdapter<>(getContext().getApplicationContext(), R.layout.iab_raiden_dropdown_item,
            R.id.item, new ArrayList<>());

    presenter.present(data, getAppPackage(), extras.getString(PRODUCT_NAME, ""),
        (BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT), transaction.getPayload());

    if (validBonus) {
      lottieTransactionComplete.setAnimation(R.raw.transaction_complete_bonus_animation);
      setupTransactionCompleteAnimation();
    } else {
      lottieTransactionComplete.setAnimation(R.raw.success_animation);
    }
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
    lottieTransactionComplete.removeAllAnimatorListeners();
    lottieTransactionComplete.removeAllUpdateListeners();
    lottieTransactionComplete.removeAllLottieOnCompositionLoadedListener();
    lottieTransactionComplete = null;
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
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

  @Override public void showTransactionCompleted() {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.GONE);
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

  @Override public long getAnimationDuration() {
    return lottieTransactionComplete.getDuration();
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("On chain buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  private void showLoading(@StringRes int message) {
    loadingView.setVisibility(View.VISIBLE);
    transactionErrorLayout.setVisibility(View.GONE);
    transactionCompletedLayout.setVisibility(View.GONE);
    loadingMessage.setText(message);
    loadingView.requestFocus();
    loadingView.setOnTouchListener((v, event) -> true);
  }

  public void showError(int error_message) {
    loadingView.setVisibility(View.GONE);
    transactionErrorLayout.setVisibility(View.VISIBLE);
    transactionCompletedLayout.setVisibility(View.GONE);
    errorTextView.setText(error_message);
  }

  private String getAppPackage() {
    if (extras.containsKey(APP_PACKAGE)) {
      return extras.getString(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }

  private String getBonus() {
    if (getArguments().containsKey(BONUS_KEY)) {
      return getArguments().getString(BONUS_KEY);
    } else {
      throw new IllegalArgumentException("bonus amount data not found");
    }
  }

  private Boolean getValidBonus() {
    if (getArguments().containsKey(VALID_BONUS)) {
      return getArguments().getBoolean(VALID_BONUS);
    } else {
      throw new IllegalArgumentException("bonus amount data not found");
    }
  }

  private void setupTransactionCompleteAnimation() {
    LottieAnimationView lottieTransactionComplete =
        transactionCompletedLayout.findViewById(R.id.lottie_transaction_success);
    TextDelegate textDelegate = new TextDelegate(lottieTransactionComplete);
    textDelegate.setText("bonus_value", getBonus());
    textDelegate.setText("bonus_received",
        getResources().getString(R.string.gamification_purchase_completed_bonus_received));
    lottieTransactionComplete.setTextDelegate(textDelegate);
    lottieTransactionComplete.setFontAssetDelegate(new FontAssetDelegate() {
      @Override public Typeface fetchFont(String fontFamily) {
        return Typeface.create("sans-serif-medium", Typeface.BOLD);
      }
    });
  }
}
