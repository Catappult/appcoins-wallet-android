package com.asfoundation.wallet.ui.iab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public class OnChainBuyFragment extends DaggerFragment implements OnChainBuyView {

  public static final String APP_PACKAGE = "app_package";
  public static final String PRODUCT_NAME = "product_name";
  public static final String TRANSACTION_HASH = "transaction_hash";
  private static final String TAG = OnChainBuyFragment.class.getSimpleName();
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  private BehaviorSubject<Object> raidenMoreInfoOkButtonClick;
  private BehaviorSubject<Boolean> createChannelClick;
  private PublishRelay<OnChainBuyPresenter.BuyData> buyButtonClick;
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
  private boolean isBackEnable;
  private TextView errorTextView;
  private TextView loadingMessage;
  private Spinner dropdown;
  private ProgressBar buyDialogLoading;
  private ArrayAdapter<BigDecimal> adapter;
  private CheckBox checkbox;
  private View raidenMoreInfoView;
  private Group amountGroup;
  private View raidenLayout;
  private View infoDialog;
  private Group createChannelGroup;
  private TextView walletAddressTextView;
  private View channelNoFundsView;
  private IabView iabView;
  private Bundle extras;
  private String data;

  public static OnChainBuyFragment newInstance(Bundle extras, String data) {
    OnChainBuyFragment fragment = new OnChainBuyFragment();
    Bundle bundle = new Bundle();
    bundle.putBundle("extras", extras);
    bundle.putString("data", data);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    createChannelClick = BehaviorSubject.create();
    buyButtonClick = PublishRelay.create();
    raidenMoreInfoOkButtonClick = BehaviorSubject.create();
    isBackEnable = true;
    extras = getArguments().getBundle("extras");
    data = getArguments().getString("data");
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
    dropdown = view.findViewById(R.id.channel_amount_dropdown);
    amountGroup = view.findViewById(R.id.amount_group);
    createChannelGroup = view.findViewById(R.id.create_channel_group);
    raidenLayout = view.findViewById(R.id.raiden_layout);
    walletAddressTextView = view.findViewById(R.id.wallet_address);

    presenter =
        new OnChainBuyPresenter(this, inAppPurchaseInteractor, AndroidSchedulers.mainThread(),
            new CompositeDisposable(), inAppPurchaseInteractor.getBillingMessagesMapper(),
            inAppPurchaseInteractor.getBillingSerializer());
    adapter =
        new ArrayAdapter<>(getContext().getApplicationContext(), R.layout.iab_raiden_dropdown_item,
            R.id.item, new ArrayList<>());
    dropdown.setAdapter(adapter);
    checkbox = view.findViewById(R.id.iab_activity_create_channel);
    channelNoFundsView = inflateView(R.layout.iab_activity_no_channel_funds);
    raidenMoreInfoView = inflateView(R.layout.iab_activity_raiden_more_info);

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

    buyButton.setOnClickListener(v -> buyButtonClick.accept(
        new OnChainBuyPresenter.BuyData(checkbox.isChecked(), data, getChannelBudget())));
  }

  @Override public void onStart() {
    super.onStart();
    presenter.present(data, getAppPackage(), extras.getString(PRODUCT_NAME),
        (BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT));
  }

  @Override public void onStop() {
    presenter.stop();
    super.onStop();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  @Override public Observable<OnChainBuyPresenter.BuyData> getBuyClick() {
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
    iabView.finish(data);
  }

  @Override public void showError() {
    showError(R.string.activity_iab_error_message);
  }

  @Override public void setup() {
    Formatter formatter = new Formatter();
    String formatedPrice = formatter.format(Locale.getDefault(), "%(,.2f",
        ((BigDecimal) extras.getSerializable(TRANSACTION_AMOUNT)).doubleValue())
        .toString() + " APPC";
    buyButton.setText(getResources().getString(R.string.action_buy));
    itemPrice.setText(formatedPrice);
    Spannable spannable = new SpannableString(formatedPrice);
    spannable.setSpan(
        new ForegroundColorSpan(getResources().getColor(R.color.dialog_buy_total_value)), 0,
        formatedPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    itemFinalPrice.setText(spannable);
    if (extras.containsKey(PRODUCT_NAME)) {
      itemDescription.setText(extras.getString(PRODUCT_NAME));
      itemHeaderDescription.setText(extras.getString(PRODUCT_NAME));
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

  @Override public void showRaidenChannelValues(List<BigDecimal> values) {
    adapter.clear();
    adapter.addAll(values);
    adapter.notifyDataSetChanged();
  }

  @Override public Observable<Boolean> getCreateChannelClick() {
    return createChannelClick;
  }

  @Override public void showRaidenInfo() {
    AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(raidenMoreInfoView)
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
    AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(channelNoFundsView)
        .show();

    channelNoFundsView.findViewById(R.id.iab_activity_raiden_no_funds_ok_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          ((ViewGroup) channelNoFundsView.getParent()).removeView(channelNoFundsView);
          buyButtonClick.accept(new OnChainBuyPresenter.BuyData(false, data, getChannelBudget()));
        });
    channelNoFundsView.findViewById(R.id.iab_activity_raiden_no_funds_cancel_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          ((ViewGroup) channelNoFundsView.getParent()).removeView(channelNoFundsView);
        });
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("Regular buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
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

  private View inflateView(int resourceId) {
    return View.inflate(
        new ContextThemeWrapper(getContext().getApplicationContext(), R.style.AppTheme), resourceId,
        null);
  }
}
