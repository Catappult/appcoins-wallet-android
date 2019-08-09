package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import com.asfoundation.wallet.ui.gamification.GamificationInteractor;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;
import com.squareup.picasso.Picasso;
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
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

import static com.asfoundation.wallet.ui.iab.IabActivity.DEVELOPER_PAYLOAD;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.URI;

public class PaymentMethodsFragment extends DaggerFragment implements PaymentMethodsView {

  private static final String IS_BDS = "isBds";
  private static final String APP_PACKAGE = "app_package";
  private static final String TAG = PaymentMethodsFragment.class.getSimpleName();
  private static final String TRANSACTION = "transaction";
  private static final String ITEM_ALREADY_OWNED = "item_already_owned";
  private static final String PRE_SELECTED_METHOD = "pre_selected_method";
  private static final String IS_DONATION = "is_donation";

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();
  private final Map<String, Bitmap> loadedBitmaps = new HashMap<>();
  PaymentMethodsPresenter presenter;
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject BillingAnalytics analytics;
  @Inject BdsPendingTransactionService bdsPendingTransactionService;
  @Inject Billing billing;
  @Inject WalletService walletService;
  @Inject GamificationInteractor gamification;
  @Inject PaymentMethodsMapper paymentMethodsMapper;
  private List<String> paymentMethodList = new ArrayList<>();
  private ProgressBar loadingView;
  private View dialog;
  private View addressFooter;
  private TextView errorMessage;
  private View errorView;
  private View processingDialog;
  private ImageView appIcon;
  private Button buyButton;
  private Button cancelButton;
  private IabView iabView;
  private Button errorDismissButton;
  private PublishSubject<Boolean> setupSubject;
  private TransactionBuilder transaction;
  private double transactionValue;
  private String bonusMessageValue = "";
  private TextView appcPriceTv;
  private TextView fiatPriceTv;
  private TextView appNameTv;
  private TextView appSkuDescriptionTv;
  private TextView walletAddressTv;
  private View mainView;
  private String productName;
  private RadioGroup radioGroup;
  private FiatValue fiatValue;
  private boolean isBds;
  private View bonusView;
  private View bonusMsg;
  private TextView bonusValue;
  private boolean itemAlreadyOwnedError;
  private PublishSubject<Boolean> onBackPressSubject;
  private int iconSize;
  private boolean appcEnabled;
  private boolean creditsEnabled;
  private SelectedPaymentMethod preSelectedMethod;
  private boolean isDonation;

  public static Fragment newInstance(TransactionBuilder transaction, String productName,
      boolean isBds, boolean isDonation, String developerPayload, String uri,
      SelectedPaymentMethod preSelectedMethod) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(TRANSACTION, transaction);
    bundle.putSerializable(TRANSACTION_AMOUNT, transaction.amount());
    bundle.putString(APP_PACKAGE, transaction.getDomain());
    bundle.putString(PRODUCT_NAME, productName);
    bundle.putString(DEVELOPER_PAYLOAD, developerPayload);
    bundle.putString(URI, uri);
    bundle.putBoolean(IS_BDS, isBds);
    bundle.putBoolean(IS_DONATION, isDonation);
    bundle.putSerializable(PRE_SELECTED_METHOD, preSelectedMethod);
    Fragment fragment = new PaymentMethodsFragment();
    fragment.setArguments(bundle);
    return fragment;
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
    isBds = getArguments().getBoolean(IS_BDS);
    isDonation = getArguments().getBoolean(IS_DONATION, false);
    transaction = getArguments().getParcelable(TRANSACTION);
    transactionValue =
        ((BigDecimal) getArguments().getSerializable(TRANSACTION_AMOUNT)).doubleValue();
    productName = getArguments().getString(PRODUCT_NAME);
    itemAlreadyOwnedError = getArguments().getBoolean(ITEM_ALREADY_OWNED, false);
    preSelectedMethod =
        ((SelectedPaymentMethod) getArguments().getSerializable(PRE_SELECTED_METHOD));
    onBackPressSubject = PublishSubject.create();
    String appPackage = getArguments().getString(APP_PACKAGE);
    String developerPayload = getArguments().getString(DEVELOPER_PAYLOAD);
    String uri = getArguments().getString(URI);

    presenter = new PaymentMethodsPresenter(this, appPackage, AndroidSchedulers.mainThread(),
        Schedulers.io(), new CompositeDisposable(), inAppPurchaseInteractor,
        inAppPurchaseInteractor.getBillingMessagesMapper(), bdsPendingTransactionService, billing,
        analytics, isBds, developerPayload, uri, walletService, gamification, transaction,
        paymentMethodsMapper);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.payment_methods_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mainView = view.findViewById(R.id.payment_method_main_view);
    radioGroup = view.findViewById(R.id.payment_methods_radio_group);
    loadingView = view.findViewById(R.id.loading_view);
    dialog = view.findViewById(R.id.payment_methods);
    addressFooter = view.findViewById(R.id.address_footer);
    errorView = view.findViewById(R.id.error_message);
    errorMessage = view.findViewById(R.id.activity_iab_error_message);
    processingDialog = view.findViewById(R.id.processing_loading);
    appIcon = view.findViewById(R.id.app_icon);
    buyButton = view.findViewById(R.id.buy_button);
    cancelButton = view.findViewById(R.id.cancel_button);
    errorDismissButton = view.findViewById(R.id.activity_iab_error_ok_button);

    appcPriceTv = view.findViewById(R.id.appc_price);
    fiatPriceTv = view.findViewById(R.id.fiat_price);
    appNameTv = view.findViewById(R.id.app_name);
    appSkuDescriptionTv = view.findViewById(R.id.app_sku_description);
    walletAddressTv = view.findViewById(R.id.wallet_address_footer);

    bonusView = view.findViewById(R.id.bonus_layout);
    bonusMsg = view.findViewById(R.id.bonus_msg);

    bonusValue = view.findViewById(R.id.bonus_value);
    buyButton.setEnabled(false);
    iconSize = getResources().getDimensionPixelSize(R.dimen.payment_method_icon_size);

    setupAppNameAndIcon();

    presenter.present(transactionValue);
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(ITEM_ALREADY_OWNED, itemAlreadyOwnedError);
  }

  @Override public void onDestroyView() {
    presenter.stop();
    compositeDisposable.clear();
    onBackPressSubject = null;
    mainView = null;
    radioGroup = null;
    loadingView = null;
    dialog = null;
    addressFooter = null;
    errorView = null;
    errorMessage = null;
    processingDialog = null;
    appIcon = null;
    buyButton = null;
    cancelButton = null;
    errorDismissButton = null;
    appcPriceTv = null;
    fiatPriceTv = null;
    appNameTv = null;
    appSkuDescriptionTv = null;
    walletAddressTv = null;
    bonusView = null;
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();

    for (Bitmap bitmap : loadedBitmaps.values()) {
      bitmap.recycle();
    }
    loadedBitmaps.clear();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  private void setupAppNameAndIcon() {
    compositeDisposable.add(Single.defer(() -> Single.just(getAppPackage()))
        .observeOn(Schedulers.io())
        .map(packageName -> new Pair<>(getApplicationName(packageName),
            getContext().getPackageManager()
                .getApplicationIcon(packageName)))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          appNameTv.setText(pair.first);
          appIcon.setImageDrawable(pair.second);
        }, throwable -> {
          throwable.printStackTrace();
        }));
  }

  public String getAppPackage() {
    if (getArguments().containsKey(APP_PACKAGE)) {
      return getArguments().getString(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getContext().getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    return packageManager.getApplicationLabel(packageInfo);
  }

  @Override
  public void showPaymentMethods(@NotNull List<PaymentMethod> paymentMethods, FiatValue fiatValue,
      String currency) {
    this.fiatValue = fiatValue;
    Formatter formatter = new Formatter();
    String valueText = formatter.format(Locale.getDefault(), "%(,.2f", transaction.amount())
        .toString() + " APPC";
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    String priceText = decimalFormat.format(fiatValue.getAmount()) + ' ' + currency;
    appcPriceTv.setText(valueText);
    fiatPriceTv.setText(priceText);
    setBuyButtonText();
    if (isDonation) {
      appSkuDescriptionTv.setText(getResources().getString(R.string.item_donation));
      appNameTv.setText(getResources().getString(R.string.item_donation));
    } else if (productName != null) {
      appSkuDescriptionTv.setText(productName);
    }

    setupPaymentMethods(paymentMethods, paymentMethodsMapper.map(preSelectedMethod));

    presenter.sendPurchaseDetailsEvent();

    setupSubject.onNext(true);
  }

  @Override public void showError(int message) {
    loadingView.setVisibility(View.GONE);
    dialog.setVisibility(View.GONE);
    addressFooter.setVisibility(View.GONE);
    mainView.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(message);
  }

  @Override public void showItemAlreadyOwnedError() {
    loadingView.setVisibility(View.GONE);
    dialog.setVisibility(View.GONE);
    addressFooter.setVisibility(View.GONE);
    mainView.setVisibility(View.GONE);
    itemAlreadyOwnedError = true;
    iabView.disableBack();
    View view = getView();
    if (view != null) {
      view.setFocusableInTouchMode(true);
      view.requestFocus();
      view.setOnKeyListener((view1, keyCode, keyEvent) -> {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          onBackPressSubject.onNext(itemAlreadyOwnedError);
        }
        return true;
      });
    }
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.purchase_error_incomplete_transaction_body);
  }

  @Override public void finish(Bundle bundle) {
    iabView.finish(bundle);
  }

  @Override public void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
    dialog.setVisibility(View.INVISIBLE);
    addressFooter.setVisibility(View.INVISIBLE);
  }

  @Override public void hideLoading() {
    loadingView.setVisibility(View.GONE);
    buyButton.setEnabled(true);
    if (processingDialog.getVisibility() != View.VISIBLE) {
      dialog.setVisibility(View.VISIBLE);
      addressFooter.setVisibility(View.VISIBLE);
    }
  }

  @Override public Observable<Object> getCancelClick() {
    return RxView.clicks(cancelButton);
  }

  @Override public void close(Bundle data) {
    iabView.close(data);
  }

  @Override public Observable<Boolean> errorDismisses() {
    return RxView.clicks(errorDismissButton)
        .map(__ -> itemAlreadyOwnedError);
  }

  @Override public Observable<Boolean> setupUiCompleted() {
    return setupSubject;
  }

  @Override public void showProcessingLoadingDialog() {
    dialog.setVisibility(View.INVISIBLE);
    addressFooter.setVisibility(View.GONE);
    loadingView.setVisibility(View.GONE);
    processingDialog.setVisibility(View.VISIBLE);
  }

  @Override public void setWalletAddress(String address) {
    walletAddressTv.setText(address);
  }

  @Override public Observable<String> getBuyClick() {
    return RxView.clicks(buyButton)
        .map(__ -> {
          if (paymentMethodList.isEmpty() || radioGroup.getCheckedRadioButtonId() == -1) {
            return "";
          } else {
            return paymentMethodList.get(radioGroup.getCheckedRadioButtonId());
          }
        });
  }

  @Override public void showPaypal() {
    iabView.showAdyenPayment(fiatValue.getAmount(), fiatValue.getCurrency(), isBds,
        PaymentType.PAYPAL, bonusMessageValue);
  }

  @Override public void showCreditCard() {
    iabView.showAdyenPayment(fiatValue.getAmount(), fiatValue.getCurrency(), isBds,
        PaymentType.CARD, bonusMessageValue);
  }

  @Override public void showAppCoins() {
    iabView.showOnChain(transaction.amount(), isBds, bonusMessageValue);
  }

  @Override public void showCredits() {
    iabView.showAppcoinsCreditsPayment(transaction.amount());
  }

  @Override public void showShareLink(String selectedPaymentMethod) {
    boolean isOneStep = transaction.getType()
        .equalsIgnoreCase("INAPP_UNMANAGED");
    iabView.showShareLinkPayment(transaction.getDomain(), transaction.getSkuId(),
        isOneStep ? transaction.getOriginalOneStepValue() : null,
        isOneStep ? transaction.getOriginalOneStepCurrency() : null, transaction.amount(),
        transaction.getType(), selectedPaymentMethod);
  }

  @Override public void hideBonus() {
    bonusView.setVisibility(View.INVISIBLE);
    bonusMsg.setVisibility(View.INVISIBLE);
  }

  @NotNull @Override public Observable<String> getPaymentSelection() {
    return RxRadioGroup.checkedChanges(radioGroup)
        .filter(checkedRadioButtonId -> checkedRadioButtonId >= 0)
        .map(checkedRadioButtonId -> paymentMethodList.get(checkedRadioButtonId));
  }

  @Override public void showLocalPayment(@NotNull String selectedPaymentMethod) {
    boolean isOneStep = transaction.getType()
        .equalsIgnoreCase("INAPP_UNMANAGED");
    iabView.showLocalPayment(transaction.getDomain(), transaction.getSkuId(),
        isOneStep ? transaction.getOriginalOneStepValue() : null,
        isOneStep ? transaction.getOriginalOneStepCurrency() : null, bonusMessageValue,
        selectedPaymentMethod, transaction.toAddress(), transaction.getType(), transaction.amount(),
        transaction.getCallbackUrl(), transaction.getOrderReference(), transaction.getPayload());
  }

  @Override public void setBonus(@NotNull BigDecimal bonus, @NotNull String currency) {
    BigDecimal scaledBonus = bonus.stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_DOWN);
    if (scaledBonus.compareTo(new BigDecimal(0.01)) < 0) {
      currency = "~" + currency;
    }
    scaledBonus = scaledBonus.max(new BigDecimal("0.01"));
    bonusMessageValue = currency + scaledBonus.toPlainString();
    bonusValue.setText(getString(R.string.gamification_purchase_header_part_2, bonusMessageValue));
    showBonus();
  }

  @Override public Observable<Boolean> onBackPressed() {
    return onBackPressSubject;
  }

  @Override public void showNext() {
    buyButton.setText(R.string.action_next);
  }

  @Override public void showBuy() {
    setBuyButtonText();
  }

  @Override public void showMergedAppcoins() {
    iabView.showMergedAppcoins(fiatValue.getAmount(), fiatValue.getCurrency(), bonusMessageValue,
        productName, appcEnabled, creditsEnabled, isBds, isDonation);
  }

  private void setBuyButtonText() {
    int buyButtonText = isDonation ? R.string.action_donate : R.string.action_buy;
    buyButton.setText(getResources().getString(buyButtonText));
  }

  private void showBonus() {
    bonusView.setVisibility(View.VISIBLE);
    bonusMsg.setVisibility(View.VISIBLE);
  }

  private void loadIcons(PaymentMethod paymentMethod, RadioButton radioButton, boolean showNew) {
    compositeDisposable.add(Observable.fromCallable(() -> {
      try {
        Context context = getContext();
        Bitmap bitmap = Picasso.with(context)
            .load(paymentMethod.getIconUrl())
            .get();
        loadedBitmaps.put(paymentMethod.getId(), bitmap);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
            Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true));
        return drawable.getCurrent();
      } catch (IOException e) {
        Log.w(TAG, "setupPaymentMethods: Failed to load icons!");
        throw new RuntimeException(e);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(drawable -> {
          Drawable newOptionIcon = showNew ? getContext().getResources()
              .getDrawable(R.drawable.ic_new_option) : null;
          radioButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, newOptionIcon, null);
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void setupPaymentMethods(List<PaymentMethod> paymentMethods, String preSelectedMethod) {
    AppCompatRadioButton radioButton;
    if (isBds) {
      for (int index = 0; index < paymentMethods.size(); index++) {
        PaymentMethod paymentMethod = paymentMethods.get(index);
        radioButton = createPaymentRadioButton(paymentMethod, index);
        radioButton.setEnabled(paymentMethod.isEnabled());
        if (paymentMethod.getId()
            .equals(preSelectedMethod)) {
          radioButton.setChecked(true);
        }
        if (paymentMethod instanceof AppCoinsPaymentMethod) {
          appcEnabled = ((AppCoinsPaymentMethod) paymentMethod).isAppcEnabled();
          creditsEnabled = ((AppCoinsPaymentMethod) paymentMethod).isCreditsEnabled();
        }
        paymentMethodList.add(paymentMethod.getId());
        radioGroup.addView(radioButton);
      }
    } else {
      for (PaymentMethod paymentMethod : paymentMethods) {
        if (paymentMethod.getId()
            .equals(paymentMethodsMapper.map(SelectedPaymentMethod.APPC))) {
          radioButton = createPaymentRadioButton(paymentMethod, 0);
          radioButton.setEnabled(true);
          radioButton.setChecked(true);
          paymentMethodList.add(paymentMethod.getId());
          radioGroup.addView(radioButton);
        }
      }
    }
  }

  private AppCompatRadioButton createPaymentRadioButton(PaymentMethod paymentMethod, int index) {
    AppCompatRadioButton radioButton = (AppCompatRadioButton) getActivity().getLayoutInflater()
        .inflate(R.layout.payment_radio_button, null);
    radioButton.setText(paymentMethod.getLabel());
    radioButton.setId(index);
    loadIcons(paymentMethod, radioButton, false);
    return radioButton;
  }
}
