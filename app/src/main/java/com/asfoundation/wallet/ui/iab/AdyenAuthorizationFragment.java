package com.asfoundation.wallet.ui.iab;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import com.airbnb.lottie.FontAssetDelegate;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.TextDelegate;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.adyen.Adyen;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.purchase.BillingFactory;
import com.asfoundation.wallet.navigator.UriNavigator;
import com.asfoundation.wallet.util.KeyboardUtils;
import com.braintreepayments.cardform.view.CardForm;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.Locale;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_CC;
import static com.asfoundation.wallet.ui.iab.IabActivity.APP_PACKAGE;
import static com.asfoundation.wallet.ui.iab.IabActivity.PRODUCT_NAME;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_AMOUNT;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_CURRENCY;
import static com.asfoundation.wallet.ui.iab.IabActivity.TRANSACTION_DATA;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
  private static final String BONUS_KEY = "bonus";
  private static final String PRE_SELECTED_KEY = "pre_selected";
  private static final String ICON_URL_KEY = "icon_url";
  private final CompositeDisposable compositeDisposable = new CompositeDisposable();
  @Inject InAppPurchaseInteractor inAppPurchaseInteractor;
  @Inject BillingFactory billingFactory;
  @Inject Adyen adyen;
  @Inject Billing billing;
  @Inject BillingAnalytics analytics;
  private View progressBar;
  private View ccInfoView;
  private IabView iabView;
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
  private CheckBox rememberCardCheckBox;
  private AdyenAuthorizationPresenter presenter;
  private PublishRelay<Boolean> backButton;
  private PublishRelay<Boolean> keyboardBuyRelay;
  private View transactionCompletedLayout;
  private View creditCardInformationLayout;
  private LottieAnimationView lottieTransactionComplete;
  private View errorView;
  private TextView errorMessage;
  private View errorOkButton;
  private View mainView;

  private PublishSubject validationSubject;
  private ImageView preSelectedIcon;
  private View bonusView;
  private View bonusMsg;
  private TextView bonusValue;
  private TextView morePaymentMethods;
  private View dialog;

  public static AdyenAuthorizationFragment newInstance(String skuId, String type, String origin,
      PaymentType paymentType, String domain, String transactionData, BigDecimal amount,
      String currency, String payload, String bonus, boolean isPreSelected, String iconUrl) {
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
    bundle.putString(BONUS_KEY, bonus);
    bundle.putBoolean(PRE_SELECTED_KEY, isPreSelected);
    bundle.putString(ICON_URL_KEY, iconUrl);
    AdyenAuthorizationFragment fragment = new AdyenAuthorizationFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backButton = PublishRelay.create();
    keyboardBuyRelay = PublishRelay.create();
    validationSubject = PublishSubject.create();

    FragmentNavigator navigator = new FragmentNavigator((UriNavigator) getActivity(), iabView);

    presenter =
        new AdyenAuthorizationPresenter(this, getAppPackage(), AndroidSchedulers.mainThread(),
            new CompositeDisposable(), adyen, billingFactory.getBilling(getAppPackage()), navigator,
            inAppPurchaseInteractor.getBillingMessagesMapper(), inAppPurchaseInteractor,
            getTransactionData(), getDeveloperPayload(), billing, getSkuId(), getType(),
            getOrigin(), getAmount().toString(), getCurrency(), getPaymentType(), analytics,
            Schedulers.io(), isPreSelected());
  }

  @Override public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (isPreSelected()) {
      return inflater.inflate(R.layout.dialog_credit_card_authorization_pre_selected, container,
          false);
    } else {
      return inflater.inflate(R.layout.dialog_credit_card_authorization, container, false);
    }
  }

  @Override public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    preAuthorizedCardText =
        view.findViewById(R.id.fragment_credit_card_authorization_pre_authorized_card);
    progressBar = view.findViewById(R.id.fragment_credit_card_authorization_progress_bar);
    ccInfoView = view.findViewById(R.id.cc_info_view);
    productIcon = view.findViewById(R.id.app_icon);
    productName = view.findViewById(R.id.app_name);
    productDescription = view.findViewById(R.id.app_sku_description);
    fiatPrice = view.findViewById(R.id.fiat_price);
    appcPrice = view.findViewById(R.id.appc_price);
    cancelButton = view.findViewById(R.id.cancel_button);
    buyButton = view.findViewById(R.id.buy_button);
    changeCardButton = view.findViewById(R.id.change_card_button);
    cardForm = view.findViewById(R.id.fragment_braintree_credit_card_form);
    transactionCompletedLayout = view.findViewById(R.id.iab_activity_transaction_completed);
    creditCardInformationLayout = view.findViewById(R.id.credit_card_info);
    mainView = view.findViewById(R.id.main_view);
    errorView = view.findViewById(R.id.fragment_iab_error);
    errorMessage = errorView.findViewById(R.id.activity_iab_error_message);
    errorOkButton = errorView.findViewById(R.id.activity_iab_error_ok_button);

    lottieTransactionComplete =
        transactionCompletedLayout.findViewById(R.id.lottie_transaction_success);

    setupTransactionCompleteAnimation();

    // removing additional margin top of the credit card form to help in the layout build
    fixCardFormLayout();

    rememberCardCheckBox =
        view.findViewById(R.id.fragment_credit_card_authorization_remember_card_check_box);

    buyButton.setVisibility(View.INVISIBLE);

    if (getType().equalsIgnoreCase(TransactionData.TransactionType.DONATION.name())) {
      buyButton.setText(R.string.action_donate);
    } else {
      buyButton.setText(R.string.action_buy);
    }

    cardForm.setOnCardFormSubmitListener(() -> {
      if (cardForm.isValid()) {
        keyboardBuyRelay.accept(true);
        if (getView() != null) {
          KeyboardUtils.hideKeyboard(getView());
        }
      }
    });

    if (isPreSelected()) {
      dialog = view.findViewById(R.id.payment_methods);
      preSelectedIcon = view.findViewById(R.id.payment_method_ic);
      bonusView = view.findViewById(R.id.bonus_layout);
      bonusMsg = view.findViewById(R.id.bonus_msg);
      bonusValue = view.findViewById(R.id.bonus_value);
      morePaymentMethods = view.findViewById(R.id.more_payment_methods);

      showBonus();
      loadIcon();
    } else {
      cancelButton.setText(R.string.back_button);
      setBackListener(view);
    }

    if (isNotBlank(getBonus())) {
      lottieTransactionComplete.setAnimation(R.raw.transaction_complete_bonus_animation);
      setupTransactionCompleteAnimation();
    } else {
      lottieTransactionComplete.setAnimation(R.raw.success_animation);
    }
    showProduct();
    presenter.present(savedInstanceState);
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    presenter.onSaveInstanceState(outState);
  }

  @Override public void onDestroyView() {
    iabView.enableBack();
    presenter.stop();
    validationSubject = null;
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
    creditCardInformationLayout = null;
    lottieTransactionComplete.removeAllAnimatorListeners();
    lottieTransactionComplete.removeAllUpdateListeners();
    lottieTransactionComplete.removeAllLottieOnCompositionLoadedListener();
    lottieTransactionComplete = null;
    transactionCompletedLayout = null;
    mainView = null;
    errorView = null;
    errorMessage = null;
    errorOkButton = null;

    preSelectedIcon = null;
    bonusView = null;
    bonusMsg = null;
    bonusValue = null;
    morePaymentMethods = null;
    dialog = null;
    super.onDestroyView();
  }

  @Override public void onDetach() {
    super.onDetach();
    iabView = null;
  }

  private void fixCardFormLayout() {
    int marginTop = isPreSelected() ? -10 : 0;
    View cardNumberParent = (View) cardForm.findViewById(R.id.bt_card_form_card_number)
        .getParent();
    ViewGroup.MarginLayoutParams lp =
        (ViewGroup.MarginLayoutParams) cardNumberParent.getLayoutParams();
    lp.setMargins(0, marginTop, 0, 0);
    cardNumberParent.setLayoutParams(lp);

    View expirationParent = (View) cardForm.findViewById(R.id.bt_card_form_expiration)
        .getParent();
    lp = (ViewGroup.MarginLayoutParams) expirationParent.getLayoutParams();
    lp.setMargins(0, marginTop, 0, 0);
    expirationParent.setLayoutParams(lp);

    View cvvParent = (View) cardForm.findViewById(R.id.bt_card_form_cvv)
        .getParent();
    lp = (ViewGroup.MarginLayoutParams) cvvParent.getLayoutParams();
    lp.setMargins(0, marginTop, 0, 0);
    cvvParent.setLayoutParams(lp);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof IabView)) {
      throw new IllegalStateException(
          "adyen authorization fragment must be attached to IAB activity");
    }
    iabView = ((IabView) context);
  }

  @Override public long getAnimationDuration() {
    return lottieTransactionComplete.getDuration();
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
    if (isPreSelected()) {
      progressBar.setVisibility(View.VISIBLE);
      dialog.setVisibility(View.INVISIBLE);
    } else {
      progressBar.setVisibility(View.VISIBLE);
      cardForm.setVisibility(View.GONE);
      ccInfoView.setVisibility(View.INVISIBLE);
      buyButton.setVisibility(View.INVISIBLE);
      cancelButton.setVisibility(View.INVISIBLE);
      changeCardButton.setVisibility(View.INVISIBLE);
    }
  }

  @Override public void hideLoading() {
    buyButton.setVisibility(cardForm.isValid() ? View.VISIBLE : View.INVISIBLE);

    cardForm.setOnCardFormValidListener(valid -> validationSubject.onNext(valid));

    if (isPreSelected()) {
      progressBar.setVisibility(View.GONE);
      dialog.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
      cardForm.setVisibility(View.VISIBLE);
      ccInfoView.setVisibility(View.VISIBLE);
      cancelButton.setVisibility(View.VISIBLE);
    }
  }

  @NotNull @Override public Observable<Object> errorDismisses() {
    return RxView.clicks(errorOkButton);
  }

  @NotNull @Override public Observable<PaymentDetails> paymentMethodDetailsEvent() {
    return Observable.merge(keyboardBuyRelay, RxView.clicks(buyButton))
        .map(__ -> {
          if (getView() != null) {
            KeyboardUtils.hideKeyboard(getView());
          }
          return getPaymentDetails(publicKey, generationTime);
        });
  }

  @NotNull @Override public Observable<PaymentMethod> changeCardMethodDetailsEvent() {
    return RxView.clicks(changeCardButton)
        .map(__ -> paymentMethod);
  }

  @Override public void showNetworkError() {
    mainView.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.notification_no_network_poa);
  }

  @NotNull @Override public Observable<Object> backEvent() {
    return RxView.clicks(cancelButton)
        .mergeWith(backButton);
  }

  @Override public void showCvcView(@NotNull Amount amount, PaymentMethod paymentMethod) {
    cvcOnly = true;
    cardForm.findViewById(com.braintreepayments.cardform.R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE);
    this.paymentMethod = paymentMethod;
    showProductPrice(amount);
    preAuthorizedCardText.setVisibility(View.VISIBLE);
    preAuthorizedCardText.setText(paymentMethod.getName());
    if (!isPreSelected()) {
      changeCardButton.setVisibility(View.VISIBLE);
    }
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
  public void showCreditCardView(@NotNull PaymentMethod paymentMethod, @NotNull Amount amount,
      boolean cvcStatus, boolean allowSave, @NotNull String publicKey,
      @NotNull String generationTime) {
    this.paymentMethod = paymentMethod;
    this.publicKey = publicKey;
    this.generationTime = generationTime;
    cvcOnly = false;
    preAuthorizedCardText.setVisibility(View.GONE);
    if (!isPreSelected()) {
      changeCardButton.setVisibility(View.GONE);
    }
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

  @Override public void showSuccess() {
    if (isPreSelected()) {
      mainView.setVisibility(View.GONE);
      transactionCompletedLayout.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
      creditCardInformationLayout.setVisibility(View.GONE);
      transactionCompletedLayout.setVisibility(View.VISIBLE);
      errorView.setVisibility(View.GONE);
    }
  }

  @Override public void showPaymentRefusedError(@NotNull AdyenAuthorization adyenAuthorization) {
    mainView.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.notification_payment_refused);
  }

  @Override public void showGenericError() {
    mainView.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
    errorMessage.setText(R.string.unknown_error);
  }

  @NotNull @Override public Observable<Object> getMorePaymentMethodsClicks() {
    return RxView.clicks(morePaymentMethods);
  }

  @Override public void showMoreMethods() {
    KeyboardUtils.hideKeyboard(mainView);
    iabView.showPaymentMethodsView();
  }

  @Override public Observable<Boolean> onValidFieldStateChange() {
    return validationSubject;
  }

  @Override public void updateButton(boolean valid) {
    buyButton.setVisibility(valid ? View.VISIBLE : View.INVISIBLE);
  }

  @Override public void lockRotation() {
    iabView.lockRotation();
  }

  private void finishSetupView() {
    int paddingTop = isPreSelected() ? 0 : 50;
    int paddingLeft = isPreSelected() ? 0 : 24;
    cardForm.findViewById(R.id.bt_card_form_card_number_icon)
        .setVisibility(View.GONE);

    //CardEditText card_number
    cardForm.findViewById(R.id.bt_card_form_card_number)
        .setPadding(0, 4, 0, 0);

    //TextInputLayout card_number
    TextInputLayout textInputLayout =
        (TextInputLayout) cardForm.findViewById(R.id.bt_card_form_card_number)
            .getParent()
            .getParent();

    textInputLayout.setPadding(paddingLeft, paddingTop, 0, 0);
    TextInputLayout.LayoutParams paramsText =
        (TextInputLayout.LayoutParams) textInputLayout.getLayoutParams();
    paramsText.setMargins(0, 8, 0, 0);
    textInputLayout.setLayoutParams(paramsText);

    //CardEditText expiration date
    cardForm.findViewById(R.id.bt_card_form_expiration)
        .setPadding(0, 4, 0, 0);

    //LinearLayout expiration date
    ((LinearLayout) cardForm.findViewById(R.id.bt_card_form_expiration)
        .getParent()
        .getParent()
        .getParent()).setPadding(paddingLeft, 0, 0, 0);

    //CardEditText expiration date
    cardForm.findViewById(R.id.bt_card_form_cvv)
        .setPadding(0, 4, 0, 0);

    presenter.sendPaymentMethodDetailsEvent(PAYMENT_METHOD_CC);
  }

  private void setBackListener(View view) {
    iabView.disableBack();
    view.setFocusableInTouchMode(true);
    view.requestFocus();
    view.setOnKeyListener((view1, i, keyEvent) -> {
      if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
          && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
        backButton.accept(true);
      }
      return true;
    });
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
    String amountValue = AmountUtil.format(amount, false, StringUtils.getLocale(getActivity()));
    fiatPrice.setText(amountValue + ' ' + amount.getCurrency());
  }

  private CharSequence getApplicationName(String appPackage)
      throws PackageManager.NameNotFoundException {
    PackageManager packageManager = getContext().getPackageManager();
    ApplicationInfo packageInfo = packageManager.getApplicationInfo(appPackage, 0);
    return packageManager.getApplicationLabel(packageInfo);
  }

  private String getAppPackage() {
    if (getArguments().containsKey(APP_PACKAGE)) {
      return getArguments().getString(APP_PACKAGE);
    }
    throw new IllegalArgumentException("previous app package name not found");
  }

  private String getTransactionData() {
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

  private String getBonus() {
    if (getArguments().containsKey(BONUS_KEY)) {
      return getArguments().getString(BONUS_KEY);
    } else {
      throw new IllegalArgumentException("bonus amount data not found");
    }
  }

  private String getIconUrl() {
    if (getArguments().containsKey(ICON_URL_KEY)) {
      return getArguments().getString(ICON_URL_KEY);
    } else {
      throw new IllegalArgumentException("icon url data not found");
    }
  }

  private boolean isPreSelected() {
    if (getArguments().containsKey(PRE_SELECTED_KEY)) {
      return getArguments().getBoolean(PRE_SELECTED_KEY);
    } else {
      throw new IllegalArgumentException("pre selected data not found");
    }
  }

  private void setupTransactionCompleteAnimation() {
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

  private void showBonus() {
    bonusView.setVisibility(View.VISIBLE);
    bonusMsg.setVisibility(View.VISIBLE);
    bonusValue.setText(getString(R.string.gamification_purchase_header_part_2, getBonus()));
  }

  private void loadIcon() {
    compositeDisposable.add(Observable.fromCallable(() -> {
      try {
        Context context = getContext();
        return Picasso.with(context)
            .load(getIconUrl())
            .get();
      } catch (IOException e) {
        Log.w(TAG, "setupPaymentMethods: Failed to load icons!");
        throw new RuntimeException(e);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(preSelectedIcon::setImageBitmap)
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }
}
