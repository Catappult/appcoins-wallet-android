package com.asfoundation.wallet.ui.iab;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.appcoins.wallet.billing.BillingFactory;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.asfoundation.wallet.billing.purchase.CreditCardBillingFactory;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.util.KeyboardUtils;
import com.asfoundation.wallet.view.rx.RxAlertDialog;
import com.braintreepayments.cardform.view.CardForm;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;

import static com.asfoundation.wallet.ui.iab.IabActivity.APP_PACKAGE;
import static com.asfoundation.wallet.ui.iab.IabActivity.EXTRA_DEVELOPER_PAYLOAD;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_CURRENCY;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_DATA;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class CreditCardAuthorizationFragment extends DaggerFragment
    implements CreditCardAuthorizationView {

  private static final String TAG = CreditCardAuthorizationFragment.class.getSimpleName();

  private static final String SKU_ID = "sku_id";
  private static final String TYPE = "type";
  private static final String ORIGIN = "origin";
  private static final String PACKAGE_NAME = "packageName";
  private static final String APP_NAME = "appName";
  private static final String APP_DESCRIPTION = "appDescription";
  private static final String FIAT_VALUE = "fiatValue";
  private static final String APPC_VALUE = "appcValue";
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject FindDefaultWalletInteract defaultWalletInteract;
  @Inject CreditCardBillingFactory creditCardBillingFactory;
  @Inject Adyen adyen;
  @Inject BillingFactory billingFactory;
  private View progressBar;
  private View ccInfoView;
  private IabView iabView;
  private RxAlertDialog networkErrorDialog;
  private RxAlertDialog paymentRefusedDialog;
  private CardForm cardForm;
  private Button buyButton;
  private Button cancelButton;
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
  private CreditCardAuthorizationPresenter presenter;
  private PublishRelay<Void> backButton;
  private PublishRelay<Void> keyboardBuyRelay;
  private CreditCardFragmentNavigator navigator;

  public static CreditCardAuthorizationFragment newInstance(Bundle skuDetails, String skuId,
      String type, String origin) {

    final CreditCardAuthorizationFragment fragment = new CreditCardAuthorizationFragment();
    skuDetails.putString(SKU_ID, skuId);
    skuDetails.putString(TYPE, type);
    skuDetails.putString(ORIGIN, origin);
    fragment.setArguments(skuDetails);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backButton = PublishRelay.create();
    keyboardBuyRelay = PublishRelay.create();

    navigator = new CreditCardFragmentNavigator(getFragmentManager(), iabView);

    presenter = new CreditCardAuthorizationPresenter(this, defaultWalletInteract,
        AndroidSchedulers.mainThread(), new CompositeDisposable(), adyen,
        creditCardBillingFactory.getBilling(getAppPackage()), navigator,
        inAppPurchaseInteractor.getBillingMessagesMapper(), inAppPurchaseInteractor,
        inAppPurchaseInteractor.getBillingSerializer(), getTransactionData(), getDeveloperPayload(),
        billingFactory.getBilling(getAppPackage()), getSkuId(), getType(), getOrigin(),
        getAmount().toString(),
        getCurrency());
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_credit_card_authorization, container, false);
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
    cardForm = view.findViewById(R.id.fragment_braintree_credit_card_form);
    walletAddressFooter = view.findViewById(R.id.wallet_address_footer);
    rememberCardCheckBox =
        view.findViewById(R.id.fragment_credit_card_authorization_remember_card_check_box);

    buyButton.setVisibility(View.INVISIBLE);

    cardForm.setOnCardFormValidListener(valid -> {
      if (valid) {
        if (getView() != null) {
          KeyboardUtils.hideKeyboard(getView());
        }
        buyButton.setVisibility(View.VISIBLE);
      } else {
        buyButton.setVisibility(View.INVISIBLE);
      }
    });
    cardForm.setOnCardFormSubmitListener(() -> {

    });

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
    presenter.present();
  }

  @Override public void onDestroyView() {
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

  @Override public Observable<PaymentDetails> creditCardDetailsEvent() {
    return Observable.merge(keyboardBuyRelay, RxView.clicks(buyButton))
        .map(__ -> getPaymentDetails(publicKey, generationTime));
  }

  @Override public void showNetworkError() {
    if (!networkErrorDialog.isShowing()) {
      networkErrorDialog.show();
    }
  }

  @Override public Observable<Object> cancelEvent() {
    return RxView.clicks(cancelButton);
  }

  @Override public void showCvcView(Amount amount, PaymentMethod paymentMethod) {
    cvcOnly = true;
    cardForm.findViewById(com.braintreepayments.cardform.R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE);
    this.paymentMethod = paymentMethod;
    showProductPrice(amount);
    preAuthorizedCardText.setVisibility(View.VISIBLE);
    preAuthorizedCardText.setText(paymentMethod.getName());
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

  public String getDeveloperPayload() {
    return getArguments().getString(EXTRA_DEVELOPER_PAYLOAD);
  }
}
