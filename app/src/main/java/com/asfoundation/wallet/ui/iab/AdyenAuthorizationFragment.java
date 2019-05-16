package com.asfoundation.wallet.ui.iab;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.adyen.Adyen;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.purchase.BillingFactory;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.navigator.UriNavigator;
import com.asfoundation.wallet.util.KeyboardUtils;
import com.asfoundation.wallet.view.rx.RxAlertDialog;
import com.braintreepayments.cardform.view.CardForm;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;

import static com.asfoundation.wallet.ui.iab.IabActivity.APP_PACKAGE;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_CURRENCY;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_DATA;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class AdyenAuthorizationFragment extends DaggerFragment implements AdyenAuthorizationView {

  private static final String TAG = AdyenAuthorizationFragment.class.getSimpleName();

  private static final String SKU_ID = "sku_id";
  private static final String TYPE = "type";
  private static final String ORIGIN = "origin";
  private static final String PAYMENT_TYPE = "paymentType";
  private static final String DEVELOPER_PAYLOAD_KEY = "developer_payload";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject FindDefaultWalletInteract defaultWalletInteract;
  @Inject BillingFactory billingFactory;
  @Inject Adyen adyen;
  @Inject Billing billing;
  @Inject BillingAnalytics analytics;
  private View progressBar;
  private View ccInfoView;
  private IabView iabView;
  private RxAlertDialog genericErrorDialog;
  private RxAlertDialog networkErrorDialog;
  private RxAlertDialog paymentRefusedDialog;
  private CardForm cardForm;
  private Button buyButton;
  private Button cancelButton;
  private Button changeCardButton;
  private ImageView productIcon;
  private TextView productName;
  private TextView productDescription;
  private String publicKey;
  private String generationTime;
  private PaymentMethod paymentMethod;
  private boolean cvcOnly;
  private TextView fiatPrice;
  private TextView appcPrice;
  private TextView preAuthorizedCardText;
  private TextView walletAddressFooter;
  private CheckBox rememberCardCheckBox;
  private AdyenAuthorizationPresenter presenter;
  private PublishRelay<Boolean> backButton;
  private PublishRelay<Boolean> keyboardBuyRelay;
  private FragmentNavigator navigator;

  public static AdyenAuthorizationFragment newInstance(String skuId, String type, String origin,
      PaymentType paymentType, String domain, String transactionData, BigDecimal amount,
      String currency, String payload) {
    Bundle bundle = new Bundle();
    bundle.putString(SKU_ID, skuId);
    bundle.putString(TYPE, type);
    bundle.putString(ORIGIN, origin);
    bundle.putString(PAYMENT_TYPE, paymentType.name());
    bundle.putString(APP_PACKAGE, domain);
    bundle.putString(TRANSACTION_DATA, transactionData);
    bundle.putSerializable(TRANSACTION_AMOUNT, amount);
    bundle.putString(TRANSACTION_CURRENCY, currency);
    bundle.putString(DEVELOPER_PAYLOAD_KEY, payload);
    AdyenAuthorizationFragment fragment = new AdyenAuthorizationFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backButton = PublishRelay.create();
    keyboardBuyRelay = PublishRelay.create();

    navigator = new FragmentNavigator((UriNavigator) getActivity(), iabView);

    presenter = new AdyenAuthorizationPresenter(this, getAppPackage(), defaultWalletInteract,
        AndroidSchedulers.mainThread(), new CompositeDisposable(), adyen,
        billingFactory.getBilling(getAppPackage()), navigator,
        inAppPurchaseInteractor.getBillingMessagesMapper(), inAppPurchaseInteractor,
        getTransactionData(), getDeveloperPayload(), billing, getSkuId(), getType(), getOrigin(),
        getAmount().toString(), getCurrency(), getPaymentType(), analytics, Schedulers.io());
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    preAuthorizedCardText =
        view.findViewById(R.id.fragment_credit_card_authorization_pre_authorized_card);
    progressBar = view.findViewById(R.id.fragment_credit_card_authorization_progress_bar);
    ccInfoView = view.findViewById(R.id.cc_info_view);
    ccInfoView.setVisibility(View.INVISIBLE);
    productIcon = view.findViewById(R.id.app_icon);
    productName = view.findViewById(R.id.app_name);
    productDescription = view.findViewById(R.id.app_sku_description);
    fiatPrice = view.findViewById(R.id.fiat_price);
    appcPrice = view.findViewById(R.id.appc_price);
    cancelButton = view.findViewById(R.id.cancel_button);
    buyButton = view.findViewById(R.id.buy_button);
    changeCardButton = view.findViewById(R.id.change_card_button);
    cardForm = view.findViewById(R.id.fragment_braintree_credit_card_form);

    // removing additional margin top of the credit card form to help in the layout build
    View cardNumberParent = (View) cardForm.findViewById(R.id.bt_card_form_card_number).getParent();
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) cardNumberParent.getLayoutParams();
    lp.setMargins(0,0,0,0);
    cardNumberParent.setLayoutParams(lp);

    walletAddressFooter = view.findViewById(R.id.wallet_address_footer);
    rememberCardCheckBox =
        view.findViewById(R.id.fragment_credit_card_authorization_remember_card_check_box);

    buyButton.setVisibility(View.INVISIBLE);

    if (getType().equalsIgnoreCase(TransactionData.TransactionType.DONATION.name())) {
      buyButton.setText(R.string.action_donate);
    } else {
      buyButton.setText(R.string.action_buy);
    }

    cardForm.setOnCardFormValidListener(valid -> {
      if (valid) {
        buyButton.setVisibility(View.VISIBLE);
      } else {
        buyButton.setVisibility(View.INVISIBLE);
      }
    });
    cardForm.setOnCardFormSubmitListener(() -> {
      if (cardForm.isValid()) {
        keyboardBuyRelay.accept(true);
        if (getView() != null) {
          KeyboardUtils.hideKeyboard(getView());
        }
      }
    });

    genericErrorDialog = new RxAlertDialog.Builder(getContext()).setMessage(R.string.unknown_error)
        .setPositiveButton(R.string.ok)
        .build();
    networkErrorDialog =
        new RxAlertDialog.Builder(getContext()).setMessage(R.string.notification_no_network_poa)
            .setPositiveButton(R.string.ok)
            .build();
    paymentRefusedDialog =
        new RxAlertDialog.Builder(getContext()).setMessage(R.string.notification_payment_refused)
            .setPositiveButton(R.string.ok)
            .build();

    paymentRefusedDialog.positiveClicks()
        .subscribe(dialogInterface -> navigator.popViewWithError(), Throwable::printStackTrace);

    showProduct();
    presenter.present(savedInstanceState);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_credit_card_authorization, container, false);
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    presenter.onSaveInstanceState(outState);
  }

  @Override public void onDestroyView() {
    presenter.stop();

    progressBar = null;
    productIcon = null;
    productName = null;
    productDescription = null;
    fiatPrice = null;
    appcPrice = null;
    cancelButton = null;
    rememberCardCheckBox = null;
    buyButton = null;
    preAuthorizedCardText = null;
    ccInfoView = null;
    cardForm.setOnCardFormSubmitListener(null);
    cardForm.setOnCardFormValidListener(null);
    cardForm = null;
    changeCardButton = null;
    super.onDestroyView();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("Regular buy fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  @Override public void showProduct() {
    Formatter formatter = new Formatter();

    try {
      productIcon.setImageDrawable(getContext().getPackageManager()
          .getApplicationIcon(getAppPackage()));
      productName.setText(getApplicationName(getAppPackage()));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    productDescription.setText(getArguments().getString(PRODUCT_NAME));
    String appcValue = formatter.format(Locale.getDefault(), "%(,.2f",
        ((BigDecimal) getArguments().getSerializable(TRANSACTION_AMOUNT)).doubleValue())
        .toString() + " APPC";
    appcPrice.setText(appcValue);
  }

  @Override public void showLoading() {
    progressBar.setVisibility(View.VISIBLE);
    cardForm.setVisibility(View.GONE);
    ccInfoView.setVisibility(View.INVISIBLE);
    buyButton.setVisibility(View.INVISIBLE);
    cancelButton.setVisibility(View.INVISIBLE);
    changeCardButton.setVisibility(View.INVISIBLE);
  }

  @Override public void hideLoading() {
    progressBar.setVisibility(View.GONE);
    cardForm.setVisibility(View.VISIBLE);
    ccInfoView.setVisibility(View.VISIBLE);
    cancelButton.setVisibility(View.VISIBLE);
  }

  @Override public Observable<Object> errorDismisses() {
    return Observable.merge(networkErrorDialog.dismisses(), paymentRefusedDialog.dismisses())
        .map(dialogInterface -> new Object());
  }

  @Override public Observable<PaymentDetails> paymentMethodDetailsEvent() {
    return Observable.merge(keyboardBuyRelay, RxView.clicks(buyButton))
        .map(__ -> getPaymentDetails(publicKey, generationTime));
  }

  @Override public Observable<PaymentMethod> changeCardMethodDetailsEvent() {
    return RxView.clicks(changeCardButton)
        .map(__ -> paymentMethod);
  }

  @Override public void showNetworkError() {
    if (!networkErrorDialog.isShowing()) {
      networkErrorDialog.show();
    }
  }

  @Override public Observable<Object> cancelEvent() {
    return RxView.clicks(cancelButton)
        .mergeWith(backButton);
  }

  @Override public void showCvcView(Amount amount, PaymentMethod paymentMethod) {
    cvcOnly = true;
    cardForm.findViewById(com.braintreepayments.cardform.R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE);
    this.paymentMethod = paymentMethod;
    showProductPrice(amount);
    preAuthorizedCardText.setVisibility(View.VISIBLE);
    preAuthorizedCardText.setText(paymentMethod.getName());
    changeCardButton.setVisibility(View.VISIBLE);
    rememberCardCheckBox.setVisibility(View.GONE);
    cardForm.cardRequired(false)
        .expirationRequired(false)
        .cvvRequired(true)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.action_buy))
        .setup(getActivity());

    hideLoading();
    finishSetupView();
  }

  @Override
  public void showCreditCardView(PaymentMethod paymentMethod, Amount amount, boolean cvcStatus,
      boolean allowSave, String publicKey, String generationTime) {
    this.paymentMethod = paymentMethod;
    this.publicKey = publicKey;
    this.generationTime = generationTime;
    cvcOnly = false;
    preAuthorizedCardText.setVisibility(View.GONE);
    changeCardButton.setVisibility(View.GONE);
    rememberCardCheckBox.setVisibility(View.VISIBLE);
    showProductPrice(amount);
    cardForm.setCardNumberIcon(0);
    cardForm.cardRequired(true)
        .expirationRequired(true)
        .cvvRequired(cvcStatus)
        .postalCodeRequired(false)
        .mobileNumberRequired(false)
        .actionLabel(getString(R.string.action_buy))
        .setup(getActivity());

    hideLoading();
    finishSetupView();
  }

  @Override public void close(Bundle bundle) {
    iabView.close(bundle);
  }

  @Override public void showWalletAddress(String address) {
    walletAddressFooter.setText(address);
  }

  @Override public void showSuccess() {

  }

  @Override public void showPaymentRefusedError(AdyenAuthorization adyenAuthorization) {
    if (!paymentRefusedDialog.isShowing()) {
      paymentRefusedDialog.show();
    }
  }

  @Override public void showGenericError() {
    if (!genericErrorDialog.isShowing()) {
      genericErrorDialog.show();
    }
  }

  private void finishSetupView() {
    cardForm.findViewById(R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE);
    ((TextInputLayout) cardForm.findViewById(R.id.bt_card_form_card_number)
        .getParent()
        .getParent()).setPadding(24, 50, 0, 0);
    ((LinearLayout) cardForm.findViewById(R.id.bt_card_form_expiration)
        .getParent()
        .getParent()
        .getParent()).setPadding(24, 0, 0, 0);
    presenter.sendPaymentMethodDetailsEvent();
  }

  private PaymentDetails getPaymentDetails(String publicKey, String generationTime) {
    if (cvcOnly) {
      final PaymentDetails paymentDetails = new PaymentDetails(paymentMethod.getInputDetails());
      paymentDetails.fill("cardDetails.cvc", cardForm.getCvv());
      return paymentDetails;
    }

    final CreditCardPaymentDetails creditCardPaymentDetails =
        new CreditCardPaymentDetails(paymentMethod.getInputDetails());
    try {
      final JSONObject sensitiveData = new JSONObject();

      sensitiveData.put("holderName", "Checkout Shopper Placeholder");
      sensitiveData.put("number", cardForm.getCardNumber());
      sensitiveData.put("expiryMonth", cardForm.getExpirationMonth());
      sensitiveData.put("expiryYear", cardForm.getExpirationYear());
      sensitiveData.put("generationtime", generationTime);
      sensitiveData.put("cvc", cardForm.getCvv());
      creditCardPaymentDetails.fillCardToken(
          new ClientSideEncrypter(publicKey).encrypt(sensitiveData.toString()));
    } catch (JSONException e) {
      Log.e(TAG, "JSON Exception occurred while generating token.", e);
    } catch (EncrypterException e) {
      Log.e(TAG, "EncrypterException occurred while generating token.", e);
    }
    creditCardPaymentDetails.fillStoreDetails(rememberCardCheckBox.isChecked());
    return creditCardPaymentDetails;
  }

  private void showProductPrice(Amount amount) {
    fiatPrice.setText(AmountUtil.format(amount, true, StringUtils.getLocale(getActivity())));
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getContext().getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    return packageManager.getApplicationLabel(packageInfo);
  }

  public String getAppPackage() {
    if (getArguments().containsKey(APP_PACKAGE)) {
      return getArguments().getString(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }

  public String getTransactionData() {
    if (getArguments().containsKey(TRANSACTION_DATA)) {
      return getArguments().getString(TRANSACTION_DATA);
    }
    throw new IllegalArgumentException("previous transaction data not found");
  }

  public String getSkuId() {
    if (getArguments().containsKey(SKU_ID)) {
      return getArguments().getString(SKU_ID);
    }
    throw new IllegalArgumentException("sku id not found");
  }

  public String getType() {
    if (getArguments().containsKey(TYPE)) {
      return getArguments().getString(TYPE);
    }
    throw new IllegalArgumentException("type not found");
  }

  public String getOrigin() {
    if (getArguments().containsKey(ORIGIN)) {
      return getArguments().getString(ORIGIN);
    }
    throw new IllegalArgumentException("origin not found");
  }

  public String getCurrency() {
    if (getArguments().containsKey(TRANSACTION_CURRENCY)) {
      return getArguments().getString(TRANSACTION_CURRENCY);
    }
    throw new IllegalArgumentException("transaction currency not found");
  }

  public BigDecimal getAmount() {
    if (getArguments().containsKey(TRANSACTION_AMOUNT)) {
      return (BigDecimal) getArguments().getSerializable(TRANSACTION_AMOUNT);
    }
    throw new IllegalArgumentException("transaction currency not found");
  }

  private PaymentType getPaymentType() {
    if (getArguments().containsKey(PAYMENT_TYPE)) {
      return PaymentType.valueOf(getArguments().getString(PAYMENT_TYPE));
    }
    throw new IllegalArgumentException("Payment Type not found");
  }

  public String getDeveloperPayload() {
    return getArguments().getString(DEVELOPER_PAYLOAD_KEY);
  }
}
